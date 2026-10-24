package com.serviseyem.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class OfflineRequest(
    val id: String = UUID.randomUUID().toString(),
    val type: String, // "BOOKING", "REQUEST", "MESSAGE", "OFFER", "ADMIN_UPDATE"
    val data: Map<String, Any?>,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: Int = 3, // 1 (Highest) to 5 (Lowest)
    val retryCount: Int = 0,
    val status: String = "PENDING" // "PENDING", "PROCESSING", "COMPLETED", "FAILED"
)

/**
 * 📦 OfflineQueueManager
 * إدارة وتخزين الطلبات في وضع الأوفلاين وإعادة جدولتها ومعالجتها تلقائياً عند استعادة الاتصال بالإنترنت.
 */
@Singleton
class OfflineQueueManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_offline_queue_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _pendingRequests = MutableStateFlow<List<OfflineRequest>>(emptyList())
    val pendingRequests: StateFlow<List<OfflineRequest>> = _pendingRequests.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    companion object {
        private const val TAG = "OfflineQueueManager"
        private const val KEY_QUEUE = "key_offline_requests_queue_json"
        private const val MAX_RETRIES = 5
        private const val BASE_RETRY_DELAY_MS = 2000L
        private const val INTER_REQUEST_DELAY_MS = 2500L
    }

    init {
        loadQueueFromStorage()
    }

    private fun loadQueueFromStorage() {
        val jsonStr = prefs.getString(KEY_QUEUE, null) ?: return
        try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<OfflineRequest>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id", UUID.randomUUID().toString())
                val type = obj.optString("type", "BOOKING")
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                val priority = obj.optInt("priority", 3)
                val retryCount = obj.optInt("retryCount", 0)
                val status = obj.optString("status", "PENDING")

                val dataObj = obj.optJSONObject("data")
                val dataMap = mutableMapOf<String, Any?>()
                if (dataObj != null) {
                    val keys = dataObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        dataMap[k] = dataObj.opt(k)
                    }
                }

                list.add(
                    OfflineRequest(
                        id = id,
                        type = type,
                        data = dataMap,
                        timestamp = timestamp,
                        priority = priority,
                        retryCount = retryCount,
                        status = status
                    )
                )
            }
            _pendingRequests.value = list
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing saved offline queue: ${e.message}")
        }
    }

    private fun saveQueueToStorage() {
        try {
            val arr = JSONArray()
            _pendingRequests.value.forEach { req ->
                val obj = JSONObject()
                obj.put("id", req.id)
                obj.put("type", req.type)
                obj.put("timestamp", req.timestamp)
                obj.put("priority", req.priority)
                obj.put("retryCount", req.retryCount)
                obj.put("status", req.status)

                val dataObj = JSONObject()
                req.data.forEach { (k, v) ->
                    dataObj.put(k, v ?: JSONObject.NULL)
                }
                obj.put("data", dataObj)
                arr.put(obj)
            }
            prefs.edit().putString(KEY_QUEUE, arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving queue: ${e.message}")
        }
    }

    /**
     * 1. إضافة طلب إلى قائمة الانتظار
     */
    fun addToQueue(request: OfflineRequest) {
        val updated = (_pendingRequests.value + request).sortedBy { it.priority }
        _pendingRequests.value = updated
        saveQueueToStorage()
        Log.d(TAG, "Request added to offline queue: ${request.id} (Type: ${request.type})")

        if (isOnline()) {
            processQueue()
        }
    }

    /**
     * 2. الحصول على الطلبات المعلقة
     */
    fun getPendingRequests(): List<OfflineRequest> {
        return _pendingRequests.value.filter { it.status == "PENDING" || it.status == "FAILED" }
    }

    /**
     * 3. معالجة قائمة الانتظار وإرسال الطلبات للسيرفر (غير معلقة للاستدعاء من الواجهة)
     */
    fun processQueue(onItemProcessed: ((OfflineRequest, Boolean) -> Unit)? = null) {
        if (!isOnline() || _isProcessing.value) return
        scope.launch {
            processQueueSuspend(onItemProcessed)
        }
    }

    /**
     * 3-ب. معالجة قائمة الانتظار بشكل معلّق (Suspend) متوافق مع WorkManager (`SyncWorker`)
     * يعيد `true` إذا تمت معالجة جميع الطلبات بنجاح أو كانت القائمة فارغة، و `false` إذا بقيت طلبات معلقة لإعادة المحاولة.
     */
    suspend fun processQueueSuspend(
        onItemProcessed: ((OfflineRequest, Boolean) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isOnline()) return@withContext _pendingRequests.value.isEmpty()
        if (!_isProcessing.compareAndSet(expect = false, update = true)) {
            return@withContext true
        }

        try {
            val currentList = _pendingRequests.value.filter { it.status == "PENDING" || it.status == "FAILED" }
            if (currentList.isEmpty()) return@withContext true

            val completedOrDroppedIds = mutableSetOf<String>()
            val updatedFailedItems = mutableMapOf<String, OfflineRequest>()

            for ((index, req) in currentList.withIndex()) {
                try {
                    if (!isOnline()) {
                        continue
                    }

                    // Throttle between consecutive requests (2.5s delay between items) to protect network & quota
                    if (index > 0) {
                        delay(INTER_REQUEST_DELAY_MS)
                    }

                    val success = try {
                        executeRequest(req)
                    } catch (e: Exception) {
                        Log.e(TAG, "Execution error for request ${req.id}: ${e.message}")
                        false
                    }

                    if (success) {
                        completedOrDroppedIds.add(req.id)
                        onItemProcessed?.invoke(req, true)
                    } else {
                        val nextRetry = req.retryCount + 1
                        if (nextRetry < MAX_RETRIES) {
                            updatedFailedItems[req.id] = req.copy(retryCount = nextRetry, status = "FAILED")
                            val backoffDelayMs = BASE_RETRY_DELAY_MS * (1L shl (nextRetry - 1).coerceAtMost(4))
                            delay(backoffDelayMs)
                        } else {
                            Log.w(TAG, "Request ${req.id} exceeded max retries and will be dropped.")
                            completedOrDroppedIds.add(req.id)
                        }
                        onItemProcessed?.invoke(req, false)
                    }
                } catch (itemEx: Exception) {
                    Log.e(TAG, "Unexpected queue item error for ${req.id}: ${itemEx.message}")
                }
            }

            val mergedList = _pendingRequests.value
                .filterNot { it.id in completedOrDroppedIds }
                .map { updatedFailedItems[it.id] ?: it }
                .sortedBy { it.priority }
            _pendingRequests.value = mergedList
            saveQueueToStorage()
            mergedList.none { it.status == "PENDING" || it.status == "FAILED" }
        } finally {
            _isProcessing.value = false
        }
    }

    private suspend fun executeRequest(req: OfflineRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val collection = when (req.type) {
                "BOOKING" -> "bookings"
                "REQUEST" -> "instant_requests"
                "MESSAGE" -> "chat_messages"
                "OFFER" -> "offers"
                else -> "offline_sync_logs"
            }
            val targetDoc = if (req.data.containsKey("id") && req.data["id"] is String) {
                req.data["id"] as String
            } else {
                req.id
            }

            val chId = req.data["channelId"] as? String
            val docRef = if (req.type == "MESSAGE" && !chId.isNullOrBlank()) {
                db.collection("chat_channels").document(chId).collection("messages").document(targetDoc)
            } else {
                db.collection(collection).document(targetDoc)
            }

            withTimeoutOrNull(8000L) {
                suspendCancellableCoroutine { continuation ->
                    docRef.set(req.data, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            if (continuation.isActive) continuation.resume(true)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(false)
                        }
                }
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 4. إلغاء طلب من القائمة
     */
    fun cancelRequest(requestId: String) {
        _pendingRequests.value = _pendingRequests.value.filterNot { it.id == requestId }
        saveQueueToStorage()
    }

    /**
     * 5. إعادة محاولة الطلبات الفاشلة
     */
    fun retryFailedRequests() {
        val updated = _pendingRequests.value.map {
            if (it.status == "FAILED") it.copy(status = "PENDING", retryCount = 0) else it
        }
        _pendingRequests.value = updated
        saveQueueToStorage()
        if (isOnline()) {
            processQueue()
        }
    }

    /**
     * 6. مسح قائمة الانتظار
     */
    fun clearQueue() {
        _pendingRequests.value = emptyList()
        prefs.edit().remove(KEY_QUEUE).apply()
    }

    /**
     * 7. حجم قائمة الانتظار
     */
    fun getQueueSize(): Int = _pendingRequests.value.size

    /**
     * 8. التحقق من توفر الإنترنت
     */
    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

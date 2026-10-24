package com.serviseyem.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

sealed class BaseUiState<out T> {
    object Idle : BaseUiState<Nothing>()
    object Loading : BaseUiState<Nothing>()
    data class Success<T>(val data: T) : BaseUiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : BaseUiState<Nothing>()
    
    fun isLoading(): Boolean = this is Loading
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
}

open class BaseViewModel : ViewModel() {

    open var appContext: android.content.Context? = null

    open val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    open val firestoreListeners = java.util.concurrent.CopyOnWriteArrayList<ListenerRegistration>()

    val coroutineExceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        com.serviseyem.utils.AppErrorLogManager.logFirestoreError(
            "CoroutineUncaught",
            throwable.localizedMessage ?: "Unhandled Coroutine Exception",
            Exception(throwable)
        )
    }

    override fun onCleared() {
        super.onCleared()
        try {
            val iterator = firestoreListeners.iterator()
            while (iterator.hasNext()) {
                try {
                    iterator.next().remove()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            firestoreListeners.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun launchSafe(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ): kotlinx.coroutines.Job = viewModelScope.launch(coroutineExceptionHandler) {
        try {
            block()
        } catch (e: Throwable) {
            e.printStackTrace()
            com.serviseyem.utils.AppErrorLogManager.logFirestoreError(
                "LaunchSafe",
                e.localizedMessage ?: "Error in launchSafe",
                Exception(e)
            )
            onError?.invoke(e)
        }
    }

    protected val _baseUiState = MutableStateFlow<BaseUiState<Any?>>(BaseUiState.Idle)
    val baseUiState: StateFlow<BaseUiState<Any?>> = _baseUiState.asStateFlow()
    
    protected fun setLoading() {
        _baseUiState.value = BaseUiState.Loading
    }
    
    protected fun setSuccess(data: Any? = null) {
        _baseUiState.value = BaseUiState.Success(data)
    }
    
    protected fun setError(message: String, throwable: Throwable? = null) {
        _baseUiState.value = BaseUiState.Error(message, throwable)
    }

    open fun com.google.firebase.firestore.Query.addSnapshotListenerReg(listener: (com.google.firebase.firestore.QuerySnapshot?, com.google.firebase.firestore.FirebaseFirestoreException?) -> Unit) {
        reg(this.addSnapshotListener(listener))
    }
    open fun com.google.firebase.firestore.DocumentReference.addSnapshotListenerReg(listener: (com.google.firebase.firestore.DocumentSnapshot?, com.google.firebase.firestore.FirebaseFirestoreException?) -> Unit) {
        reg(this.addSnapshotListener(listener))
    }

    open fun reg(listener: ListenerRegistration) {
        firestoreListeners.add(listener)
    }

    open val _toastMessage = MutableStateFlow<String?>(null)
    open val toastFlow: StateFlow<String?> = _toastMessage.asStateFlow()

    open fun triggerToast(msg: String) {
        _toastMessage.value = msg
    }

    open fun triggerNotification(msg: String) {
        _toastMessage.value = msg
    }

    open fun clearToast() {
        _toastMessage.value = null
    }

    // دالة آمنة لعمليات Firestore مع معالجة الأخطاء
    protected open suspend fun <T> safeFirestoreCall(
        operation: suspend () -> T,
        onSuccess: (T) -> Unit = {},
        onError: (Exception) -> Unit = {},
        errorMessage: String = "حدث خطأ أثناء تنفيذ العملية"
    ) {
        try {
            val result = operation()
            onSuccess(result)
        } catch (e: Exception) {
            e.printStackTrace()
            com.serviseyem.utils.AppErrorLogManager.logFirestoreError("FirestoreCall", errorMessage, e)
            triggerNotification("❌ $errorMessage: ${e.localizedMessage}")
            onError(e)
        }
    }

    // دالة آمنة لعمليات Firestore مع Coroutine Context (Dispatchers.IO)
    protected open suspend fun <T> safeFirestoreCallWithCallback(
        operation: suspend () -> T,
        onSuccess: (T) -> Unit = {},
        onError: (Exception) -> Unit = {},
        errorMessage: String = "حدث خطأ أثناء تنفيذ العملية"
    ) {
        try {
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                operation()
            }
            onSuccess(result)
        } catch (e: Exception) {
            e.printStackTrace()
            com.serviseyem.utils.AppErrorLogManager.logFirestoreError("FirestoreCoroutineException", errorMessage, e)
            triggerNotification("❌ $errorMessage: ${e.localizedMessage}")
            onError(e)
        }
    }

    // دالة آمنة لعمليات Firestore مع callback
    protected open fun safeFirestoreCallWithCallback(
        operation: (onSuccess: () -> Unit, onFailure: (Exception) -> Unit) -> Unit,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {},
        errorMessage: String = "حدث خطأ أثناء تنفيذ العملية"
    ) {
        try {
            operation(
                {
                    onSuccess()
                },
                { exception ->
                    com.serviseyem.utils.AppErrorLogManager.logFirestoreError("FirestoreCallback", errorMessage, exception)
                    triggerNotification("❌ $errorMessage: ${exception.localizedMessage}")
                    onError(exception)
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            com.serviseyem.utils.AppErrorLogManager.logFirestoreError("FirestoreException", errorMessage, e)
            triggerNotification("❌ $errorMessage: ${e.localizedMessage}")
            onError(e)
        }
    }

    // دالة لتحديث الحالة المحلية بأمان
    protected open fun <T> safeUpdateState(
        currentState: List<T>,
        newItem: T,
        filterCondition: (T) -> Boolean = { it == newItem }
    ): List<T> {
        return currentState.filter { !filterCondition(it) } + newItem
    }

    // دالة لعرض رسائل المزامنة
    protected open fun showSyncMessage(success: Boolean, action: String) {
        if (success) {
            triggerNotification("✅ تم $action بنجاح")
        } else {
            triggerNotification("⚠️ تم حفظ $action محلياً وسيتم المزامنة تلقائياً")
        }
    }

    open val _isRefreshing = MutableStateFlow(false)
    open val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    open fun setRefreshing(refreshing: Boolean) {
        _isRefreshing.value = refreshing
    }

    open val _uiErrorMessage = MutableStateFlow<String?>(null)
    open val uiErrorMessage: StateFlow<String?> = _uiErrorMessage.asStateFlow()

    open fun setUiError(message: String) {
        _uiErrorMessage.value = message
    }

    open fun clearUiError() {
        _uiErrorMessage.value = null
    }

    open suspend fun uploadImageStringOrUri(
        context: android.content.Context,
        input: String,
        storagePath: String,
        maxSizeBytes: Long = 300 * 1024L
    ): String {
        if (input.isBlank()) return ""
        if (input.startsWith("http://") || input.startsWith("https://")) return input
        return try {
            if (input.startsWith("content://") || input.startsWith("file://")) {
                val uri = android.net.Uri.parse(input)
                val res = com.serviseyem.utils.FirebaseStorageUploader.uploadImageUri(
                    context, uri, storagePath, maxDimension = 800, maxSizeBytes = maxSizeBytes
                )
                res.getOrDefault(input)
            } else {
                val cleanBase64 = if (input.contains(",")) input.substringAfter(",") else input
                val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    val res = com.serviseyem.utils.FirebaseStorageUploader.uploadBitmap(
                        bitmap, storagePath, maxDimension = 800, maxSizeBytes = maxSizeBytes
                    )
                    res.getOrDefault(input)
                } else {
                    input
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            input
        }
    }

    open fun getAuthEmailForPhone(phone: String): String {
        val cleanPhone = phone.replace("+", "").replace(" ", "")
        return "user_$cleanPhone@yemen-services.app"
    }

    open fun getDefaultStoresList(): List<com.serviseyem.data.StoreEntity> = com.serviseyem.data.helpers.DefaultDemoData.getDemoStores()
    open fun getDefaultPropertiesList(): List<com.serviseyem.data.PropertyEntity> = com.serviseyem.data.helpers.DefaultDemoData.getDemoProperties()
    open fun getDefaultProvidersList(): List<com.serviseyem.data.ProviderEntity> = com.serviseyem.data.helpers.DefaultDemoData.getDemoProviders()
}

class FirestorePaginationHelper<T : Any>(
    private val collection: String,
    private val limit: Int = 20,
    private val orderBy: String = "createdAt",
    private val descending: Boolean = true
) {
    private var lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private var hasMoreData = true
    
    suspend fun loadNextPage(
        db: com.google.firebase.firestore.FirebaseFirestore,
        mapper: (com.google.firebase.firestore.DocumentSnapshot) -> T?
    ): List<T> {
        if (!hasMoreData) return emptyList()
        
        try {
            var query = db.collection(collection)
                .orderBy(orderBy, if (descending) com.google.firebase.firestore.Query.Direction.DESCENDING else com.google.firebase.firestore.Query.Direction.ASCENDING)
                .limit(limit.toLong())
            
            lastDocument?.let {
                query = query.startAfter(it)
            }
            
            val snapshot = query.get().await()
            
            if (snapshot.documents.isEmpty()) {
                hasMoreData = false
                return emptyList()
            }
            
            val items = snapshot.documents.mapNotNull { mapper(it) }
            lastDocument = snapshot.documents.lastOrNull()
            hasMoreData = snapshot.documents.size == limit
            
            return items
            
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    
    fun reset() {
        lastDocument = null
        hasMoreData = true
    }
}


package com.serviseyem.data.repositories

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.utils.AnalyticsEventsHelper
import com.serviseyem.utils.AppConstants
import com.serviseyem.utils.SecureHasher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import java.security.SecureRandom

/**
 * إنشاء الطلبات الفورية وتدفقاتها الحية (طلبات العميل والطلبات المتاحة للفنيين).
 */
internal class InstantRequestQueryAndCreationDelegate(
    private val context: Context?,
    private val firestoreProvider: () -> FirebaseFirestore,
    private val requestsState: MutableStateFlow<List<InstantRequestEntity>>
) {
    fun createInstantRequest(
        request: InstantRequestEntity,
        onSuccess: (InstantRequestEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val firestore = firestoreProvider()
            val docId = if (request.id.isNotBlank()) request.id else firestore.collection(AppConstants.COL_INSTANT_REQUESTS).document().id
            val secureRandom = SecureRandom()
            val requestCode = if (request.requestCode.isNotBlank()) request.requestCode else "URG-${100000 + secureRandom.nextInt(900000)}"
            val inputRawPin = request.rawPin
            val rawPin = if (inputRawPin.isNotBlank()) inputRawPin else "${1000 + secureRandom.nextInt(9000)}"
            val pin = if (rawPin.startsWith("$2a$") || rawPin.startsWith("$2b$") || rawPin.contains(":")) {
                rawPin
            } else {
                SecureHasher.hashPin(rawPin)
            }
            val rawCancelPass = if (request.cancellationPassword.isNotBlank()) request.cancellationPassword else rawPin
            val cancelPass = if (rawCancelPass.startsWith("$2a$") || rawCancelPass.startsWith("$2b$") || rawCancelPass.contains(":")) {
                rawCancelPass
            } else {
                SecureHasher.hashPin(rawCancelPass)
            }
            val now = System.currentTimeMillis()
            val expiresAt = if (request.expiresAt > now) request.expiresAt else now + (30 * 60 * 1000L) // 30 mins

            val newEntity = request.copy(
                id = docId,
                requestCode = requestCode,
                pinHash = pin,
                rawPin = rawPin,
                cancellationPassword = cancelPass,
                status = if (request.status.isBlank()) "WAITING_FOR_OFFERS" else request.status,
                createdAt = if (request.createdAt > 0) request.createdAt else now,
                expiresAt = expiresAt
            )
            val entityForFirestore = newEntity.copy(rawPin = "")

            firestore.collection(AppConstants.COL_INSTANT_REQUESTS).document(docId).set(entityForFirestore)
                .addOnSuccessListener {
                    val current = requestsState.value.toMutableList()
                    current.removeAll { it.id == docId }
                    current.add(0, newEntity)
                    requestsState.value = current
                    AnalyticsEventsHelper.logUrgentRequestCreated(context, docId, newEntity.categoryName.ifBlank { newEntity.serviceTitle })
                    onSuccess(newEntity)
                }
                .addOnFailureListener {
                    onError(it.localizedMessage ?: "فشل إنشاء الطلب الفوري")
                }
        } catch (e: Exception) {
            Log.e("InstantRequestRepo", "Error creating instant request", e)
            onError(e.localizedMessage ?: "خطأ غير متوقع أثناء معالجة الطلب")
        }
    }

    fun getUserInstantRequests(userId: String): Flow<List<InstantRequestEntity>> = callbackFlow {
        val firestore = firestoreProvider()
        val listener: ListenerRegistration = firestore.collection(AppConstants.COL_INSTANT_REQUESTS)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(requestsState.value.filter { it.userId == userId })
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(InstantRequestEntity::class.java)?.copy(id = doc.id)
                    } catch (_: Exception) {
                        null
                    }
                }
                requestsState.value = list
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    fun getAvailableInstantRequests(category: String, city: String): Flow<List<InstantRequestEntity>> = callbackFlow {
        val firestore = firestoreProvider()
        val now = System.currentTimeMillis()
        var query: Query = firestore.collection(AppConstants.COL_INSTANT_REQUESTS)
            .whereEqualTo("status", "WAITING_FOR_OFFERS")
            .whereGreaterThan("expiresAt", now)

        if (city.isNotBlank()) {
            query = query.whereEqualTo("userCity", city.trim())
        }
        if (category.isNotBlank()) {
            query = query.whereEqualTo("categoryName", category.trim())
        }

        query = query.orderBy("expiresAt", Query.Direction.DESCENDING).limit(50)

        var fallbackRegistration: ListenerRegistration? = null
        val listener: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Fallback query if composite index is still building in Firestore
                if (fallbackRegistration == null) {
                    fallbackRegistration = firestore.collection(AppConstants.COL_INSTANT_REQUESTS)
                        .whereEqualTo("status", "WAITING_FOR_OFFERS")
                        .whereGreaterThan("expiresAt", System.currentTimeMillis())
                        .limit(50)
                        .addSnapshotListener { fbSnap, fbErr ->
                            if (fbErr != null || fbSnap == null) {
                                trySend(emptyList())
                                return@addSnapshotListener
                            }
                            val currentMillis = System.currentTimeMillis()
                            val fbList = fbSnap.documents.mapNotNull { doc ->
                                runCatching { doc.toObject(InstantRequestEntity::class.java)?.copy(id = doc.id) }.getOrNull()
                            }.filter { item ->
                                (category.isBlank() || item.categoryName.contains(category, ignoreCase = true) || item.serviceTitle.contains(category, ignoreCase = true)) &&
                                    (city.isBlank() || item.userCity.contains(city, ignoreCase = true)) &&
                                    (item.expiresAt > currentMillis)
                            }
                            trySend(fbList)
                        }
                }
                return@addSnapshotListener
            }

            if (snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val currentMillis = System.currentTimeMillis()
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(InstantRequestEntity::class.java)?.copy(id = doc.id)
                } catch (_: Exception) {
                    null
                }
            }.filter { item ->
                item.expiresAt > currentMillis
            }

            trySend(list)
        }

        awaitClose {
            listener.remove()
            fallbackRegistration?.remove()
        }
    }
}

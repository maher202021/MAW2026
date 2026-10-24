package com.serviseyem.data.repositories

import android.content.Context
import android.util.Log

import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.data.models.RequestOfferEntity
import com.serviseyem.security.BookingSecurityHelper
import com.serviseyem.utils.AnalyticsEventsHelper
import com.serviseyem.utils.AppConstants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

/**
 * ⚡ InstantRequestRepository
 * مستودع إدارة الطلبات الفورية والعاجلة مع دعم التدفق الحي (Flow)،
 * المؤقت التنازلي للطلب (30 دقيقة)، تقديم العروض، والمصادقة الأمنية.
 */
class InstantRequestRepository(private val context: Context? = null) {

    private val repositoryScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _requests = MutableStateFlow<List<InstantRequestEntity>>(emptyList())
    val requests: StateFlow<List<InstantRequestEntity>> = _requests.asStateFlow()

    /**
     * 1. إنشاء طلب فوري جديد مع مؤقت زمني مدته 30 دقيقة
     */
    fun createInstantRequest(
        request: InstantRequestEntity,
        onSuccess: (InstantRequestEntity) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        try {
            val docId = if (request.id.isNotBlank()) request.id else firestore.collection(AppConstants.COL_INSTANT_REQUESTS).document().id
            val secureRandom = java.security.SecureRandom()
            val requestCode = if (request.requestCode.isNotBlank()) request.requestCode else "URG-${100000 + secureRandom.nextInt(900000)}"
            val inputRawPin = request.rawPin
            val rawPin = if (inputRawPin.isNotBlank()) inputRawPin else "${1000 + secureRandom.nextInt(9000)}"
            val pin = if (rawPin.startsWith("$2a$") || rawPin.startsWith("$2b$") || rawPin.contains(":")) {
                rawPin
            } else {
                com.serviseyem.utils.SecureHasher.hashPin(rawPin)
            }
            val rawCancelPass = if (request.cancellationPassword.isNotBlank()) request.cancellationPassword else rawPin
            val cancelPass = if (rawCancelPass.startsWith("$2a$") || rawCancelPass.startsWith("$2b$") || rawCancelPass.contains(":")) {
                rawCancelPass
            } else {
                com.serviseyem.utils.SecureHasher.hashPin(rawCancelPass)
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
                    val current = _requests.value.toMutableList()
                    current.removeAll { it.id == docId }
                    current.add(0, newEntity)
                    _requests.value = current
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

    /**
     * 2. تدفق حي لطلبات مستخدم معين (العميل)
     */
    fun getUserInstantRequests(userId: String): Flow<List<InstantRequestEntity>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection(AppConstants.COL_INSTANT_REQUESTS)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(_requests.value.filter { it.userId == userId })
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(InstantRequestEntity::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                _requests.value = list
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    /**
     * 3. تدفق حي للطلبات المتاحة للفنيين في مدينة/تصنيف معين (فلترة على السيرفر)
     */
    fun getAvailableInstantRequests(category: String = "", city: String = ""): Flow<List<InstantRequestEntity>> = callbackFlow {
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
                } catch (e: Exception) {
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

    /**
     * 4. تقديم عرض سعر من مزود خدمة على طلب فوري
     */
    fun submitOffer(
        offer: RequestOfferEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val offerId = if (offer.id.isNotBlank()) offer.id else com.serviseyem.utils.EntityIdGenerator.generateOfferId()
        val finalOffer = offer.copy(id = offerId, createdAt = System.currentTimeMillis())

        val offerRef = firestore.collection(AppConstants.COL_INSTANT_REQUESTS)
            .document(offer.requestId)
            .collection("offers")
            .document(offerId)

        val topLevelOfferRef = firestore.collection("request_offers").document(offerId)
        val requestRef = firestore.collection(AppConstants.COL_INSTANT_REQUESTS).document(offer.requestId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(requestRef)
            if (!snapshot.exists()) {
                throw FirebaseFirestoreException("الطلب الفوري غير موجود", FirebaseFirestoreException.Code.NOT_FOUND)
            }
            val status = snapshot.getString("status") ?: "WAITING_FOR_OFFERS"
            val expiresAt = snapshot.getLong("expiresAt") ?: 0L
            if (status != "WAITING_FOR_OFFERS") {
                throw FirebaseFirestoreException("لا يمكن تقديم عرض على طلب بحالة: $status", FirebaseFirestoreException.Code.ABORTED)
            }
            if (expiresAt > 0L && expiresAt <= System.currentTimeMillis()) {
                throw FirebaseFirestoreException("عذراً، انتهت صلاحية هذا الطلب الفوري", FirebaseFirestoreException.Code.ABORTED)
            }
            val currentOffers = snapshot.getLong("offersCount") ?: 0L
            transaction.set(offerRef, finalOffer)
            transaction.set(topLevelOfferRef, finalOffer)
            transaction.update(requestRef, "offersCount", currentOffers + 1)
        }.addOnSuccessListener {
            AnalyticsEventsHelper.logOfferSubmitted(context, offer.requestId, offer.technicianId, offer.price)
            onSuccess()
        }.addOnFailureListener {
            onError(it.localizedMessage ?: "فشل تقديم العرض")
        }
    }

    /**
     * 5. قبول عرض السعر وبدء الخدمة
     */
    fun acceptOffer(
        requestId: String,
        offerId: String,
        providerId: String,
        providerName: String,
        providerPhone: String,
        acceptedPrice: Double,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val now = System.currentTimeMillis()
        val updates = mapOf(
            "status" to "ACCEPTED",
            "acceptedOfferId" to offerId,
            "acceptedTechnicianId" to providerId,
            "acceptedTechnicianName" to providerName,
            "acceptedTechnicianPhone" to providerPhone,
            "acceptedPrice" to acceptedPrice,
            "acceptedAt" to now,
            "updatedAt" to now
        )

        val requestRef = firestore.collection(AppConstants.COL_INSTANT_REQUESTS).document(requestId)
        val offerRef = requestRef.collection("offers").document(offerId)
        val topLevelOfferRef = firestore.collection("request_offers").document(offerId)

        // تنفيذ عملية القبول عبر Transaction واحدة موحدة لتقليل استهلاك Firebase Spark
        firestore.runTransaction { tx ->
            val snapshot = tx.get(requestRef)
            if (snapshot.exists()) {
                val currentStatus = snapshot.getString("status")?.uppercase() ?: "WAITING_FOR_OFFERS"
                val currentAcceptedOffer = snapshot.getString("acceptedOfferId") ?: ""
                if (currentStatus in setOf("CANCELLED", "COMPLETED", "EXPIRED") ||
                    (currentStatus == "ACCEPTED" && currentAcceptedOffer.isNotBlank() && currentAcceptedOffer != offerId)
                ) {
                    throw IllegalStateException("لا يمكن قبول العرض لأن حالة الطلب الحالية هي: $currentStatus")
                }
            }
            tx.update(requestRef, updates)
            tx.update(offerRef, "status", "ACCEPTED")
            tx.update(topLevelOfferRef, "status", "ACCEPTED")
        }.addOnSuccessListener {
            // رفض العروض المنافسة المعلقة في Batch واحد محدود لتوفير استهلاك الكوتا
            requestRef.collection("offers")
                .whereEqualTo("status", "PENDING")
                .limit(FirebaseOptimizationManager.URGENT_REQUEST_PAGE_SIZE.toLong())
                .get()
                .addOnSuccessListener { snap ->
                    if (!snap.isEmpty) {
                        val rejectBatch = firestore.batch()
                        var hasRejects = false
                        snap.documents.forEach { doc ->
                            if (doc.id != offerId) {
                                rejectBatch.update(doc.reference, "status", "REJECTED")
                                rejectBatch.update(firestore.collection("request_offers").document(doc.id), "status", "REJECTED")
                                hasRejects = true
                            }
                        }
                        if (hasRejects) {
                            rejectBatch.commit()
                        }
                    }
                }

            _requests.value = _requests.value.map {
                if (it.id == requestId) it.copy(
                    status = "ACCEPTED",
                    acceptedOfferId = offerId,
                    acceptedTechnicianId = providerId,
                    acceptedTechnicianName = providerName,
                    acceptedTechnicianPhone = providerPhone,
                    acceptedPrice = acceptedPrice
                ) else it
            }
            AnalyticsEventsHelper.logOfferAccepted(context, requestId, providerId)
            onSuccess()
        }.addOnFailureListener { error ->
            onError(error.localizedMessage ?: "فشل قبول العرض")
        }
    }

    /**
     * 6. إلغاء الطلب الفوري مع التحقق الأمني
     */
    fun cancelInstantRequest(
        requestId: String,
        userPin: String = "",
        cancelReason: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        firestore.collection(AppConstants.COL_INSTANT_REQUESTS).document(requestId).get()
            .addOnSuccessListener { doc ->
                val request = doc.toObject(InstantRequestEntity::class.java)
                if (request == null) {
                    onError("الطلب غير موجود")
                    return@addOnSuccessListener
                }

                val expectedPass = request.effectivePinHash
                if (expectedPass.isNotBlank()) {
                    if (userPin.isBlank()) {
                        onError("يرجى إدخال رمز PIN لإلغاء الطلب")
                        return@addOnSuccessListener
                    }
                    val isValid = com.serviseyem.utils.SecureHasher.verifyPin(userPin, expectedPass) ||
                            BookingSecurityHelper.verifyPassword(userPin, expectedPass)
                    if (!isValid) {
                        onError("رمز PIN للإلغاء غير صحيح")
                        return@addOnSuccessListener
                    }
                }

                val updates = mutableMapOf<String, Any>(
                    "status" to "CANCELLED",
                    "cancelledAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
                if (cancelReason.isNotBlank()) {
                    updates["cancelReason"] = cancelReason
                }

                firestore.collection(AppConstants.COL_INSTANT_REQUESTS).document(requestId).update(updates)
                    .addOnSuccessListener {
                        _requests.value = _requests.value.map {
                            if (it.id == requestId) it.copy(status = "CANCELLED", cancelReason = cancelReason) else it
                        }
                        onSuccess()
                    }
                    .addOnFailureListener { onError(it.localizedMessage ?: "فشل إلغاء الطلب") }
            }
            .addOnFailureListener { onError(it.localizedMessage ?: "فشل الوصول لبيانات الطلب") }
    }

    /**
     * 7. إكمال الطلب الفوري بنجاح
     */
    fun completeInstantRequest(
        requestId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val updates = mapOf(
            "status" to "COMPLETED",
            "completedAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection(AppConstants.COL_INSTANT_REQUESTS).document(requestId).update(updates)
            .addOnSuccessListener {
                _requests.value = _requests.value.map {
                    if (it.id == requestId) it.copy(status = "COMPLETED") else it
                }
                onSuccess()
            }
            .addOnFailureListener { onError(it.localizedMessage ?: "فشل إكمال الطلب") }
    }
}

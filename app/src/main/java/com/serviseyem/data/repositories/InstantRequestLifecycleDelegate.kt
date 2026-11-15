package com.serviseyem.data.repositories

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.data.models.RequestOfferEntity
import com.serviseyem.security.BookingSecurityHelper
import com.serviseyem.utils.AnalyticsEventsHelper
import com.serviseyem.utils.AppConstants
import com.serviseyem.utils.EntityIdGenerator
import com.serviseyem.utils.SecureHasher
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * إدارة عروض الأسعار للطلبات الفورية ودورة حياتها (تقديم، قبول، إلغاء آمن، إكمال).
 */
internal class InstantRequestLifecycleDelegate(
    private val context: Context?,
    private val firestoreProvider: () -> FirebaseFirestore,
    private val requestsState: MutableStateFlow<List<InstantRequestEntity>>
) {
    fun submitOffer(
        offer: RequestOfferEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val firestore = firestoreProvider()
        val offerId = if (offer.id.isNotBlank()) offer.id else EntityIdGenerator.generateOfferId()
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

    fun acceptOffer(
        requestId: String,
        offerId: String,
        providerId: String,
        providerName: String,
        providerPhone: String,
        acceptedPrice: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val firestore = firestoreProvider()
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

            requestsState.value = requestsState.value.map {
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

    fun cancelInstantRequest(
        requestId: String,
        userPin: String,
        cancelReason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val firestore = firestoreProvider()
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
                    val isValid = SecureHasher.verifyPin(userPin, expectedPass) ||
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
                        requestsState.value = requestsState.value.map {
                            if (it.id == requestId) it.copy(status = "CANCELLED", cancelReason = cancelReason) else it
                        }
                        onSuccess()
                    }
                    .addOnFailureListener { onError(it.localizedMessage ?: "فشل إلغاء الطلب") }
            }
            .addOnFailureListener { onError(it.localizedMessage ?: "فشل الوصول لبيانات الطلب") }
    }

    fun completeInstantRequest(
        requestId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val firestore = firestoreProvider()
        val updates = mapOf(
            "status" to "COMPLETED",
            "completedAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection(AppConstants.COL_INSTANT_REQUESTS).document(requestId).update(updates)
            .addOnSuccessListener {
                requestsState.value = requestsState.value.map {
                    if (it.id == requestId) it.copy(status = "COMPLETED") else it
                }
                onSuccess()
            }
            .addOnFailureListener { onError(it.localizedMessage ?: "فشل إكمال الطلب") }
    }
}

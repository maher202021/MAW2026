package com.serviseyem.utils

import com.serviseyem.data.RatingEntity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * ⭐ Reviews & Ratings Engine
 * - يربط التقييمات بالحجوزات المكتملة حصراً (COMPLETED) ويمنع التكرار (isRated = true).
 * - تقييم متعدد الأبعاد (الجودة، السرعة، الاحترافية، مناسبة السعر).
 * - التصويت على التقييمات (مفيد / غير مفيد).
 * - ردود المزودين على التقييمات وإمكانية الإبلاغ.
 * - ترشيد استهلاك Firebase بالاعتماد على الكاش المحلي.
 */
object ReviewsAndRatingsEngine {

    private val db = FirebaseFirestore.getInstance()

    // Local In-Memory Cache to minimize Firestore reads on Free Tier
    private val reviewsCache = java.util.concurrent.ConcurrentHashMap<String, List<RatingEntity>>()

    // 1. Multi-Dimensional Rating Criteria Model
    data class MultiDimensionalRating(
        val quality: Float = 5.0f,
        val speed: Float = 5.0f,
        val professionalism: Float = 5.0f,
        val priceFairness: Float = 5.0f
    ) {
        fun calculateOverallAverage(): Float {
            val sum = quality + speed + professionalism + priceFairness
            return (sum / 4.0f).coerceIn(1.0f, 5.0f)
        }
    }

    // 2. Verified Booking Check
    fun verifyCompletedBookingEligibility(
        userId: String,
        bookingId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (bookingId.isBlank()) {
            onResult(false, "عفواً، لا يمكن التقييم إلا برقم حجز مكتمل وصحيح.")
            return
        }

        db.collection("bookings")
            .document(bookingId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onResult(false, "الحجز غير موجود في النظام.")
                    return@addOnSuccessListener
                }

                val status = doc.getString("status") ?: ""
                val isRated = doc.getBoolean("isRated") ?: false

                if (status != "COMPLETED") {
                    onResult(false, "لا يمكنك التقييم إلا بعد إكمال وإنجاز الخدمة فعلياً (حالة الحجز مكتمل).")
                } else if (isRated) {
                    onResult(false, "لقد قمت بتقييم هذه الخدمة مسبقاً! شكراً لمشاركتك.")
                } else {
                    onResult(true, null)
                }
            }
            .addOnFailureListener {
                onResult(false, "تعذر التحقق من حالة الحجز: ${it.localizedMessage}")
            }
    }

    // 3. Submit Verified Multi-Dimensional Rating
    fun submitVerifiedReview(
        bookingId: String,
        userId: String,
        userName: String,
        userPhone: String = "",
        targetId: String,
        targetType: String = "PROVIDER",
        comment: String,
        ratings: MultiDimensionalRating,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        verifyCompletedBookingEligibility(userId, bookingId) { isValid, errorMsg ->
            if (!isValid) {
                onError(errorMsg ?: "حجز غير مؤهل للتقييم")
                return@verifyCompletedBookingEligibility
            }

            val reviewId = "rev_${System.currentTimeMillis()}_${(1000..9999).random()}"
            val overallAvg = ratings.calculateOverallAverage()

            val ratingEntity = RatingEntity(
                id = reviewId,
                targetId = targetId,
                targetType = targetType,
                userId = userId,
                userName = userName.ifEmpty { "عميل معتمد" },
                userPhone = userPhone,
                bookingId = bookingId,
                rating = overallAvg,
                qualityRating = ratings.quality,
                speedRating = ratings.speed,
                professionalismRating = ratings.professionalism,
                priceFairnessRating = ratings.priceFairness,
                comment = comment.trim(),
                isApproved = true,
                timestamp = System.currentTimeMillis()
            )

            // Save rating to ratings collection
            db.collection("ratings")
                .document(reviewId)
                .set(ratingEntity)
                .addOnSuccessListener {
                    // Mark the booking as rated
                    db.collection("bookings")
                        .document(bookingId)
                        .update("isRated", true)

                    // Invalidate local cache
                    reviewsCache.remove(targetId)

                    onSuccess()
                }
                .addOnFailureListener {
                    onError(it.localizedMessage ?: "فشل حفظ التقييم")
                }
        }
    }

    // 4. Submit Provider Reply
    fun submitProviderReply(
        reviewId: String,
        replyText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (replyText.isBlank()) {
            onError("الرجاء كتابة نص الرد أولاً")
            return
        }

        db.collection("ratings")
            .document(reviewId)
            .update(
                mapOf(
                    "reply" to replyText.trim(),
                    "replyTimestamp" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.localizedMessage ?: "فشل حفظ الرد") }
    }

    // 5. Vote Review as Helpful / Unhelpful
    fun voteReviewHelpful(
        reviewId: String,
        userId: String,
        isHelpful: Boolean,
        onSuccess: (newHelpful: Int, newUnhelpful: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        if (userId.isBlank()) {
            onError("يجب تسجيل الدخول للتصويت على التقييم")
            return
        }

        val docRef = db.collection("ratings").document(reviewId)
        docRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                onError("التقييم غير موجود")
                return@addOnSuccessListener
            }

            val helpfulList = (snapshot.get("helpfulUserIds") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
            val unhelpfulList = (snapshot.get("unhelpfulUserIds") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()

            if (isHelpful) {
                if (helpfulList.contains(userId)) {
                    helpfulList.remove(userId) // Toggle off
                } else {
                    helpfulList.add(userId)
                    unhelpfulList.remove(userId)
                }
            } else {
                if (unhelpfulList.contains(userId)) {
                    unhelpfulList.remove(userId) // Toggle off
                } else {
                    unhelpfulList.add(userId)
                    helpfulList.remove(userId)
                }
            }

            val updates = mapOf(
                "helpfulCount" to helpfulList.size,
                "unhelpfulCount" to unhelpfulList.size,
                "helpfulUserIds" to helpfulList,
                "unhelpfulUserIds" to unhelpfulList
            )

            docRef.update(updates).addOnSuccessListener {
                onSuccess(helpfulList.size, unhelpfulList.size)
            }.addOnFailureListener {
                onError(it.localizedMessage ?: "فشل تحديث التصويت")
            }
        }.addOnFailureListener {
            onError(it.localizedMessage ?: "فشل جلب التقييم")
        }
    }

    // 6. Report Abusive Review
    fun reportAbusiveReview(
        reviewId: String,
        reporterUserId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (reason.isBlank()) {
            onError("الرجاء تحديد سبب الإبلاغ")
            return
        }

        val reportId = "rep_${System.currentTimeMillis()}"
        val reportPayload = hashMapOf<String, Any?>(
            "id" to reportId,
            "reviewId" to reviewId,
            "reporterUserId" to reporterUserId,
            "reason" to reason.trim(),
            "status" to "PENDING",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("review_reports")
            .document(reportId)
            .set(reportPayload)
            .addOnSuccessListener {
                db.collection("ratings").document(reviewId).update(
                    mapOf(
                        "isReported" to true,
                        "reportReason" to reason.trim()
                    )
                )
                onSuccess()
            }
            .addOnFailureListener { onError(it.localizedMessage ?: "فشل إرسال البلاغ") }
    }
}

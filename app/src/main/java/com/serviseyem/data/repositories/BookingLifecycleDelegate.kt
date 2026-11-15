package com.serviseyem.data.repositories

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.serviseyem.data.BookingEntity
import com.serviseyem.domain.usecases.ValidatePhoneUseCase
import com.serviseyem.security.BookingSecurityHelper
import com.serviseyem.utils.AnalyticsEventsHelper
import com.serviseyem.utils.AppConstants
import com.serviseyem.utils.BookingStateMachine
import com.serviseyem.utils.BookingStatus
import com.serviseyem.utils.BookingUtils
import kotlinx.coroutines.tasks.await

/**
 * إدارة دورة حياة الحجز: تحديث الحالة، الإلغاء الآمن، تعديل التفاصيل، الحذف، والقفل المؤقت.
 */
internal class BookingLifecycleDelegate(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val getCachedBookings: () -> List<BookingEntity>,
    private val saveToCache: (List<BookingEntity>) -> Unit
) {
    fun updateBookingStatus(
        bookingId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val now = System.currentTimeMillis()
        val updates = mutableMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to now
        )

        val existingBooking = getCachedBookings().find { it.id == bookingId }
        if (existingBooking != null && existingBooking.status.isNotBlank()) {
            if (!BookingStateMachine.canTransition(existingBooking.status, newStatus)) {
                onError("انتقال غير مسموح من الحالة (${existingBooking.status}) إلى ($newStatus)")
                return
            }
        }
        if (newStatus == "COMPLETED" && (existingBooking == null || existingBooking.completedAt == 0L)) {
            updates["completedAt"] = now
        }

        if (newStatus == "APPROVED" || newStatus == "ACCEPTED") {
            AnalyticsEventsHelper.logBookingAccepted(context, bookingId)
        }

        val previousBookings = getCachedBookings()
        val current = previousBookings.map {
            if (it.id == bookingId) {
                it.copy(
                    status = newStatus,
                    updatedAt = now,
                    completedAt = if (newStatus == "COMPLETED" && it.completedAt == 0L) now else it.completedAt
                )
            } else it
        }
        saveToCache(current)

        firestore.collection(AppConstants.COL_BOOKINGS).document(bookingId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                saveToCache(previousBookings)
                onError(it.localizedMessage ?: "فشل تحديث حالة الحجز")
            }
    }

    fun cancelBookingWithSecurity(
        booking: BookingEntity,
        inputPinOrPassword: String,
        cancellationReason: String,
        cancelledBy: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // 1. Check if locked out (after 3 failed attempts)
        if (BookingSecurityHelper.isBookingLocked(context, booking.id)) {
            val remainingSec = BookingSecurityHelper.getRemainingLockoutSeconds(context, booking.id)
            onError("تم قفل هذا الحجز مؤقتاً بسبب 3 محاولات خاطئة. يرجى المحاولة بعد ${remainingSec / 60} دقيقة و ${remainingSec % 60} ثانية.")
            return
        }

        // 2. Check 8-hour rule
        val canCancel = BookingUtils.canModifyOrCancelBooking(
            scheduledAtTimestamp = booking.scheduledAt,
            dateString = booking.date.ifBlank { booking.dateString },
            timeString = booking.time.ifBlank { booking.timeString }
        )
        if (!canCancel) {
            onError("عذراً، تنص سياسة الخدمة على عدم إمكانية تعديل أو إلغاء الحجز إذا تبقى أقل من 8 ساعات على الموعد المحدد.")
            return
        }

        // 3. Verify Password / PIN
        val expectedTarget = booking.effectivePinCode
        val isVerified = BookingSecurityHelper.verifyPassword(inputPinOrPassword, expectedTarget)

        if (!isVerified) {
            val attemptsLeft = BookingSecurityHelper.recordFailedAttempt(context, booking.id)
            if (attemptsLeft == 0) {
                onError("رمز التحقق غير صحيح. تم استنفاد 3 محاولات وقفل الحجز لمدة ثلاثين دقيقة لأسباب أمنية.")
            } else {
                onError("رمز التحقق غير صحيح. متبقي لديك $attemptsLeft محاولة فقط قبل القفل المؤقت.")
            }
            return
        }

        // Verification successful -> reset attempts and perform cancellation
        BookingSecurityHelper.resetAttempts(context, booking.id)

        val updates = mapOf(
            "status" to "CANCELLED",
            "cancellationReason" to cancellationReason,
            "cancelledAt" to System.currentTimeMillis(),
            "cancelledBy" to cancelledBy,
            "updatedAt" to System.currentTimeMillis()
        )

        val previousBookings = getCachedBookings()
        val current = previousBookings.map {
            if (it.id == booking.id) it.copy(
                status = BookingStatus.CANCELLED.code,
                cancellationReason = cancellationReason,
                cancelledAt = System.currentTimeMillis(),
                cancelledBy = cancelledBy,
                updatedAt = System.currentTimeMillis()
            ) else it
        }
        saveToCache(current)

        firestore.collection(AppConstants.COL_BOOKINGS).document(booking.id)
            .update(updates)
            .addOnSuccessListener {
                AnalyticsEventsHelper.logBookingCancelled(context, booking.id, cancellationReason)
                sendCancellationNotifications(booking, cancellationReason, cancelledBy)
                onSuccess()
            }
            .addOnFailureListener {
                saveToCache(previousBookings)
                onError(it.localizedMessage ?: "فشل إلغاء الحجز")
            }
    }

    private fun sendCancellationNotifications(
        booking: BookingEntity,
        cancellationReason: String,
        cancelledBy: String
    ) {
        val notifTime = System.currentTimeMillis()

        val userTitle = when (cancelledBy) {
            "USER" -> "تم إلغاء حجزك بنجاح"
            "PROVIDER" -> "اعتذر الفني عن الحجز"
            else -> "تم إلغاء حجزك من قبل الإدارة"
        }
        val userMsg = when (cancelledBy) {
            "USER" -> "تم إلغاء طلب الحجز #${booking.bookingNumber} بناءً على طلبك. السبب: $cancellationReason"
            "PROVIDER" -> "قام الفني بإلغاء الحجز #${booking.bookingNumber}. السبب: $cancellationReason"
            else -> "قامت الإدارة بإلغاء الحجز #${booking.bookingNumber}. السبب: $cancellationReason"
        }

        val cleanUserPhone = ValidatePhoneUseCase.normalizePhone(booking.effectiveCustomerPhone)
        if (cleanUserPhone.isNotBlank()) {
            val uDedup = "CANCEL_BOOKING_USER_${booking.id}"
            val uId = "notif_$uDedup"
            firestore.collection(AppConstants.COL_NOTIFICATIONS).document(uId).set(
                mapOf(
                    "id" to uId,
                    "dedupKey" to uDedup,
                    "title" to userTitle,
                    "message" to userMsg,
                    "targetType" to "USER",
                    "targetValue" to cleanUserPhone,
                    "bookingId" to booking.id,
                    "timestamp" to notifTime
                )
            )
        }

        val providerTitle = when (cancelledBy) {
            "USER" -> "قام العميل بإلغاء الحجز"
            "PROVIDER" -> "تم إلغاء الحجز بنجاح"
            else -> "قامت الإدارة بإلغاء الحجز"
        }
        val providerMsg = when (cancelledBy) {
            "USER" -> "قام العميل بإلغاء الحجز #${booking.bookingNumber}. السبب: $cancellationReason"
            "PROVIDER" -> "تم إلغاء الحجز #${booking.bookingNumber} بناءً على طلبك. السبب: $cancellationReason"
            else -> "قامت الإدارة بإلغاء الحجز #${booking.bookingNumber}. السبب: $cancellationReason"
        }

        val pTarget = ValidatePhoneUseCase.normalizePhone(booking.providerPhone)
            .ifBlank { booking.providerId.trim() }
        if (pTarget.isNotBlank()) {
            val pDedup = "CANCEL_BOOKING_PROVIDER_${booking.id}"
            val pId = "notif_$pDedup"
            firestore.collection(AppConstants.COL_NOTIFICATIONS).document(pId).set(
                mapOf(
                    "id" to pId,
                    "dedupKey" to pDedup,
                    "title" to providerTitle,
                    "message" to providerMsg,
                    "targetType" to "PROVIDER",
                    "targetValue" to pTarget,
                    "bookingId" to booking.id,
                    "timestamp" to notifTime
                )
            )
        }

        val aDedup = "CANCEL_BOOKING_ADMIN_${booking.id}"
        val aId = "notif_$aDedup"
        firestore.collection(AppConstants.COL_NOTIFICATIONS).document(aId).set(
            mapOf(
                "id" to aId,
                "dedupKey" to aDedup,
                "title" to "إشعار إداري: إلغاء حجز",
                "message" to "تم إلغاء الحجز #${booking.bookingNumber} من قبل $cancelledBy. السبب: $cancellationReason",
                "targetType" to "ADMIN_ONLY",
                "targetValue" to "ALL",
                "bookingId" to booking.id,
                "timestamp" to notifTime
            )
        )
    }

    fun updateBookingDetails(
        updatedBooking: BookingEntity,
        inputPin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (BookingSecurityHelper.isBookingLocked(context, updatedBooking.id)) {
            val remainingSec = BookingSecurityHelper.getRemainingLockoutSeconds(context, updatedBooking.id)
            onError("الحجز مقفل مؤقتاً. انتظر ${remainingSec / 60} دقيقة.")
            return
        }

        val existingBooking = getCachedBookings().find { it.id == updatedBooking.id } ?: updatedBooking
        val canModify = BookingUtils.canModifyOrCancelBooking(
            scheduledAtTimestamp = existingBooking.scheduledAt,
            dateString = existingBooking.date.ifBlank { existingBooking.dateString },
            timeString = existingBooking.time.ifBlank { existingBooking.timeString }
        )
        if (!canModify) {
            onError("لا يمكن تعديل الحجز عند بقاء أقل من 8 ساعات على الموعد.")
            return
        }

        val expectedTarget = existingBooking.effectivePinCode
        val isVerified = BookingSecurityHelper.verifyPassword(inputPin, expectedTarget)

        if (!isVerified) {
            val left = BookingSecurityHelper.recordFailedAttempt(context, updatedBooking.id)
            onError("رمز التحقق غير صحيح. متبقي $left محاولات.")
            return
        }

        BookingSecurityHelper.resetAttempts(context, updatedBooking.id)

        val newDate = updatedBooking.date.ifBlank { updatedBooking.dateString }
        val newTime = updatedBooking.time.ifBlank { updatedBooking.timeString }
        val recomputedScheduledAt = BookingUtils.parseScheduledTimestamp(newDate, newTime)
            .takeIf { it > 0L } ?: updatedBooking.scheduledAt
        val resolvedPinHash = when {
            updatedBooking.pinCode.isNotBlank() && BookingSecurityHelper.isSha256Hash(updatedBooking.pinCode) -> updatedBooking.pinCode
            existingBooking.pinCode.isNotBlank() && BookingSecurityHelper.isSha256Hash(existingBooking.pinCode) -> existingBooking.pinCode
            inputPin.isNotBlank() -> BookingSecurityHelper.hashPin(inputPin)
            else -> updatedBooking.pinCode
        }

        val itemToSave = updatedBooking.copy(
            scheduledAt = recomputedScheduledAt,
            pinCode = resolvedPinHash,
            updatedAt = System.currentTimeMillis()
        )
        val previousBookings = getCachedBookings()
        val current = previousBookings.map { if (it.id == itemToSave.id) itemToSave else it }
        saveToCache(current)

        firestore.collection(AppConstants.COL_BOOKINGS).document(itemToSave.id)
            .set(itemToSave)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                saveToCache(previousBookings)
                onError(it.localizedMessage ?: "فشل تحديث البيانات")
            }
    }

    fun deleteBooking(bookingId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val previousBookings = getCachedBookings()
        val current = previousBookings.filter { it.id != bookingId }
        saveToCache(current)

        firestore.collection(AppConstants.COL_BOOKINGS).document(bookingId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                saveToCache(previousBookings)
                onError(it.localizedMessage ?: "فشل حذف الحجز")
            }
    }

    suspend fun lockBooking(bookingId: String, lockDurationMs: Long): Result<Unit> {
        return try {
            val updateData = mapOf(
                "isLocked" to true,
                "lockedUntil" to System.currentTimeMillis() + lockDurationMs
            )
            firestore.collection(AppConstants.COL_BOOKINGS).document(bookingId).update(updateData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

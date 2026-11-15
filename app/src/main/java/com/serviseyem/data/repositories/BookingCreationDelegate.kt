package com.serviseyem.data.repositories

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.serviseyem.data.BookingEntity
import com.serviseyem.domain.usecases.ValidatePhoneUseCase
import com.serviseyem.security.BookingSecurityHelper
import com.serviseyem.utils.AnalyticsEventsHelper
import com.serviseyem.utils.AppConstants
import com.serviseyem.utils.BookingNotificationManager
import com.serviseyem.utils.BookingUtils
import java.util.UUID

/**
 * إنشاء الحجوزات الجديدة والتحقق من تعارض مواعيد الفني وتوليد الإشعارات.
 */
internal class BookingCreationDelegate(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val getCachedBookings: () -> List<BookingEntity>,
    private val saveToCache: (List<BookingEntity>) -> Unit
) {
    fun createBooking(
        booking: BookingEntity,
        rawPasswordPin: String,
        onSuccess: (BookingEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Check technician schedule conflict
            val targetDate = booking.date.ifBlank { booking.dateString }
            val targetTime = booking.time.ifBlank { booking.timeString }
            if (booking.technicianId.isNotBlank() &&
                BookingUtils.hasTechnicianConflict(booking.technicianId, targetDate, targetTime, getCachedBookings())
            ) {
                onError("عذراً، الفني لديه حجز آخر مجدول في نفس التوقيت المختار (+/- ساعتين). يرجى اختيار موعد آخر أو التواصل مع الفني.")
                return
            }

            val docId = if (booking.id.isNotBlank()) booking.id else firestore.collection(AppConstants.COL_BOOKINGS).document().id
            val finalCode = if (booking.bookingNumber.isNotBlank()) booking.bookingNumber else BookingUtils.generateBookingNumber()
            val rawPin = if (rawPasswordPin.isNotBlank()) rawPasswordPin else BookingUtils.generateBookingPassword()
            val hashedPin = BookingSecurityHelper.hashPin(rawPin)

            val scheduledTs = if (booking.scheduledAt > 0) booking.scheduledAt
            else BookingUtils.parseScheduledTimestamp(booking.date.ifBlank { booking.dateString }, booking.time.ifBlank { booking.timeString })

            val finalBooking = booking.copy(
                id = docId,
                bookingNumber = finalCode,
                bookingCode = finalCode,
                pinCode = hashedPin,
                scheduledAt = scheduledTs,
                status = if (booking.status.isBlank()) "PENDING" else booking.status,
                createdAt = if (booking.createdAt > 0) booking.createdAt else System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Save immediately in local cache
            val current = getCachedBookings().toMutableList()
            current.removeAll { it.id == docId }
            current.add(0, finalBooking)
            saveToCache(current)
            AnalyticsEventsHelper.logBookingCreated(context, docId, finalBooking.serviceType.ifBlank { finalBooking.category }, finalBooking.totalAmount)

            // Sync to Firestore directly with .set() to save read costs
            firestore.collection(AppConstants.COL_BOOKINGS).document(docId).set(finalBooking)
                .addOnSuccessListener {
                    sendCreationNotifications(docId, finalBooking)
                    AnalyticsEventsHelper.logBookingCreated(context, finalBooking.id, finalBooking.serviceType, finalBooking.totalAmount)
                    onSuccess(finalBooking)
                }
                .addOnFailureListener { ex ->
                    val firestoreEx = ex as? FirebaseFirestoreException
                    val isOffline = firestoreEx?.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                        ex.message?.contains("offline", ignoreCase = true) == true ||
                        ex.message?.contains("network", ignoreCase = true) == true

                    if (isOffline) {
                        // Queue for offline sync and notify caller that cloud sync is pending
                        onError("تعذر الاتصال بالخادم حالياً؛ يرجى التأكد من اتصال الإنترنت والمحاولة مرة أخرى.")
                    } else {
                        // Rollback optimistic local cache on permanent server rejection
                        val rolledBack = getCachedBookings().filterNot { it.id == finalBooking.id }
                        saveToCache(rolledBack)
                        onError(ex.localizedMessage ?: "فشل إنشاء الحجز في الخادم، يرجى المحاولة مرة أخرى.")
                    }
                }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "فشل إنشاء الحجز")
        }
    }

    private fun sendCreationNotifications(docId: String, finalBooking: BookingEntity) {
        val userNotifId = UUID.randomUUID().toString()
        val userNotif = mapOf(
            "id" to userNotifId,
            "title" to "تم استلام طلب حجزك بنجاح",
            "message" to "مرحباً! تم استلام طلب حجزك برقم #${finalBooking.bookingNumber} بنجاح وهو قيد المراجعة. رمز PIN سيُرسل عبر قناة منفصلة.",
            "targetType" to "USER",
            "targetValue" to finalBooking.customerPhone,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection(AppConstants.COL_NOTIFICATIONS).document(userNotifId).set(userNotif)

        // Resolve provider phone if blank
        if (finalBooking.providerPhone.isBlank() && finalBooking.providerId.isNotBlank()) {
            firestore.collection("providers").document(finalBooking.providerId).get()
                .addOnSuccessListener { pDoc ->
                    val pPhone = pDoc.getString("phone") ?: ""
                    if (pPhone.isNotBlank()) {
                        firestore.collection(AppConstants.COL_BOOKINGS).document(docId).update("providerPhone", pPhone)
                    }
                }
        }

        val normalizedProviderPhone = ValidatePhoneUseCase.normalizePhone(finalBooking.providerPhone)
        val canonicalProviderTarget = normalizedProviderPhone.ifBlank { finalBooking.providerId.trim() }
        if (canonicalProviderTarget.isNotBlank()) {
            val dedupKey = "NEW_BOOKING_${finalBooking.id}"
            val providerNotifId = "notif_$dedupKey"
            val providerNotif = mapOf(
                "id" to providerNotifId,
                "dedupKey" to dedupKey,
                "title" to "طلب حجز جديد برقم #${finalBooking.bookingNumber}",
                "message" to "لديك طلب حجز جديد برقم #${finalBooking.bookingNumber} من العميل ${finalBooking.customerName.ifEmpty { finalBooking.clientName }}.",
                "targetType" to "PROVIDER",
                "targetValue" to canonicalProviderTarget,
                "targetPhone" to normalizedProviderPhone,
                "bookingId" to finalBooking.id,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection(AppConstants.COL_NOTIFICATIONS).document(providerNotifId).set(providerNotif)
        }

        try {
            BookingNotificationManager(context, firestore).notifyBookingCreated(finalBooking)
        } catch (_: Exception) {}

        val adminNotifId = "notif_admin_NEW_BOOKING_${finalBooking.id}"
        val adminNotif = mapOf(
            "id" to adminNotifId,
            "dedupKey" to "ADMIN_NEW_BOOKING_${finalBooking.id}",
            "title" to "إشعار للإدارة بالحجز الجديد",
            "message" to "تم إنشاء حجز جديد #${finalBooking.bookingNumber} للخدمة ${finalBooking.serviceName}.",
            "targetType" to "ADMIN_ONLY",
            "targetValue" to "ALL",
            "bookingId" to finalBooking.id,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection(AppConstants.COL_NOTIFICATIONS).document(adminNotifId).set(adminNotif)
    }
}

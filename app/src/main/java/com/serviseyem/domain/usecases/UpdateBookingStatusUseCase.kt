package com.serviseyem.domain.usecases

import com.serviseyem.data.repositories.BookingRepository
import java.util.Locale
import javax.inject.Inject

/**
 * 🎯 UpdateBookingStatusUseCase
 * Updates the progression state of a booking (ACCEPTED/APPROVED, IN_PROGRESS, COMPLETED, REJECTED).
 */
class UpdateBookingStatusUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {

    operator fun invoke(
        bookingId: String,
        newStatus: String,
        currentStatus: String = "",
        userRole: String = "PROVIDER",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanId = bookingId.trim()
        if (cleanId.isBlank()) {
            onError("معرف الحجز غير صالح")
            return
        }
        val cleanNewStatus = newStatus.trim().uppercase(Locale.ROOT).let {
            if (it == "APPROVED") "ACCEPTED" else it
        }
        if (cleanNewStatus.isBlank()) {
            onError("حالة الحجز الجديدة غير صالحة")
            return
        }

        val roleUpper = userRole.trim().uppercase(Locale.ROOT)
        if (roleUpper in listOf("CLIENT", "GUEST", "USER", "CUSTOMER") &&
            cleanNewStatus in listOf("ACCEPTED", "IN_PROGRESS", "REJECTED", "CLOSED", "COMPLETED", "PAID")
        ) {
            onError("ليس لديك صلاحية لتغيير حالة الحجز إلى ($cleanNewStatus)")
            return
        }

        val resolvedCurrentStatus = currentStatus.trim().ifBlank {
            bookingRepository.cachedBookings?.value?.find { it.id == cleanId }?.status.orEmpty()
        }
        if (resolvedCurrentStatus.isNotBlank() &&
            !com.serviseyem.utils.BookingStateMachine.canTransition(resolvedCurrentStatus, cleanNewStatus)
        ) {
            onError("انتقال غير مسموح من الحالة ($resolvedCurrentStatus) إلى ($cleanNewStatus)")
            return
        }
        bookingRepository.updateBookingStatus(
            bookingId = cleanId,
            newStatus = cleanNewStatus,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}

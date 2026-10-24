package com.serviseyem.domain.usecases

import com.serviseyem.data.BookingEntity
import com.serviseyem.data.repositories.BookingRepository
import javax.inject.Inject

class CancelBookingUseCase @Inject constructor(
    private val repository: BookingRepository
) {
    operator fun invoke(
        booking: BookingEntity,
        inputPin: String,
        reason: String,
        cancelledBy: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = execute(booking, inputPin, reason, cancelledBy, onSuccess, onError)

    fun execute(
        booking: BookingEntity,
        inputPin: String,
        reason: String,
        cancelledBy: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (booking.id.isBlank()) {
            onError("معرف الحجز غير صالح")
            return
        }
        repository.cancelBookingWithSecurity(booking, inputPin.trim(), reason.trim(), cancelledBy.trim(), onSuccess, onError)
    }
}

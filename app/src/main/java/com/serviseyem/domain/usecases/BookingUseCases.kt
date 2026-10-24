package com.serviseyem.domain.usecases

import com.serviseyem.data.BookingEntity
import com.serviseyem.data.repositories.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookingsUseCase @Inject constructor(private val repository: BookingRepository) {
    operator fun invoke(userId: String, isProvider: Boolean = false): Flow<List<BookingEntity>> {
        val cleanUserId = userId.trim()
        if (cleanUserId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        return repository.getBookingsFlow(cleanUserId, isProvider)
    }
}

class UpdateBookingUseCase @Inject constructor(private val repository: BookingRepository) {
    operator fun invoke(
        updatedBooking: BookingEntity,
        inputPin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (updatedBooking.id.isBlank()) {
            onError("معرف الحجز غير صالح")
            return
        }
        repository.updateBookingDetails(updatedBooking, inputPin.trim(), onSuccess, onError)
    }
}

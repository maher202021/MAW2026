package com.serviseyem.data.repositories

import com.serviseyem.data.BookingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 📦 IBookingRepository
 * Interface for booking operations.
 */
interface IBookingRepository {
    val cachedBookings: StateFlow<List<BookingEntity>>

    fun getUserBookings(userId: String, pageLimit: Long = 50): Flow<List<BookingEntity>>
    fun getProviderBookings(providerId: String, pageLimit: Long = 50): Flow<List<BookingEntity>>
    fun getBookingsFlow(userId: String, isProvider: Boolean = false): Flow<List<BookingEntity>>
    
    fun createBooking(
        booking: BookingEntity,
        rawPasswordPin: String = "",
        onSuccess: (BookingEntity) -> Unit,
        onError: (String) -> Unit
    )

    fun updateBookingStatus(
        bookingId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    fun cancelBookingWithSecurity(
        booking: BookingEntity,
        inputPinOrPassword: String,
        cancellationReason: String,
        cancelledBy: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    fun deleteBooking(bookingId: String, onSuccess: () -> Unit, onError: (String) -> Unit)
    
    fun updateBookingDetails(
        updatedBooking: BookingEntity,
        inputPin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}

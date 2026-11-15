package com.serviseyem.data.repositories

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.serviseyem.data.BookingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 📦 BookingRepository
 * Offline-First repository implementation for bookings.
 * Coordinates local memory caching and delegates streaming, creation, and lifecycle transitions.
 */
class BookingRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IBookingRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _cachedBookings = MutableStateFlow<List<BookingEntity>>(emptyList())
    override val cachedBookings: StateFlow<List<BookingEntity>> = _cachedBookings.asStateFlow()

    private val streamDelegate = BookingStreamDelegate(
        firestore = firestore,
        getCachedBookings = { _cachedBookings.value },
        loadFromCache = ::loadFromCache,
        saveToCache = ::saveToCache
    )

    private val creationDelegate = BookingCreationDelegate(
        context = context,
        firestore = firestore,
        getCachedBookings = { _cachedBookings.value },
        saveToCache = ::saveToCache
    )

    private val lifecycleDelegate = BookingLifecycleDelegate(
        context = context,
        firestore = firestore,
        getCachedBookings = { _cachedBookings.value },
        saveToCache = ::saveToCache
    )

    init {
        repositoryScope.launch {
            loadFromCache()
        }
    }

    private fun loadFromCache(): List<BookingEntity> {
        val now = System.currentTimeMillis()
        return _cachedBookings.value.map { booking ->
            if (booking.isLocked && booking.lockedUntil != null && now > booking.lockedUntil) {
                booking.copy(isLocked = false, lockedUntil = null)
            } else {
                booking
            }
        }
    }

    private fun saveToCache(list: List<BookingEntity>) {
        _cachedBookings.value = list
    }

    /**
     * Realtime flow of all bookings for a user with offline fallback.
     */
    override fun getUserBookings(userId: String, pageLimit: Long): Flow<List<BookingEntity>> =
        streamDelegate.getBookingsFlow(userId, isProvider = false, pageLimit = pageLimit)

    /**
     * Realtime flow of all bookings for a provider with offline fallback.
     */
    override fun getProviderBookings(providerId: String, pageLimit: Long): Flow<List<BookingEntity>> =
        streamDelegate.getBookingsFlow(providerId, isProvider = true, pageLimit = pageLimit)

    /**
     * Realtime flow of all bookings for a user or provider with offline fallback.
     */
    override fun getBookingsFlow(userId: String, isProvider: Boolean): Flow<List<BookingEntity>> =
        streamDelegate.getBookingsFlow(userId, isProvider = isProvider, pageLimit = 100L)

    /**
     * Creates a new booking with auto-generated booking code and hashed PIN.
     */
    override fun createBooking(
        booking: BookingEntity,
        rawPasswordPin: String,
        onSuccess: (BookingEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        creationDelegate.createBooking(booking, rawPasswordPin, onSuccess, onError)
    }

    /**
     * Updates status of a booking (e.g. APPROVED, IN_PROGRESS, COMPLETED).
     */
    override fun updateBookingStatus(
        bookingId: String,
        newStatus: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        lifecycleDelegate.updateBookingStatus(bookingId, newStatus, onSuccess, onError)
    }

    /**
     * Cancels booking enforcing 8-hour rule and PIN verification.
     */
    override fun cancelBookingWithSecurity(
        booking: BookingEntity,
        inputPinOrPassword: String,
        cancellationReason: String,
        cancelledBy: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        lifecycleDelegate.cancelBookingWithSecurity(
            booking = booking,
            inputPinOrPassword = inputPinOrPassword,
            cancellationReason = cancellationReason,
            cancelledBy = cancelledBy,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * Direct cancellation of a booking by ID (Deprecated: enforces security checks via cancelBookingWithSecurity).
     */
    @Deprecated(
        "Use cancelBookingWithSecurity to enforce 8-hour rule and PIN verification",
        ReplaceWith("cancelBookingWithSecurity(booking, inputPinOrPassword, cancellationReason, cancelledBy, onSuccess, onError)")
    )
    fun cancelBooking(
        bookingId: String,
        cancellationReason: String = "إلغاء من قبل المستخدم",
        cancelledBy: String = "USER",
        inputPinOrPassword: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val booking = _cachedBookings.value.find { it.id == bookingId }
        if (booking == null) {
            onError("لم يتم العثور على الحجز المطلوب")
            return
        }
        cancelBookingWithSecurity(
            booking = booking,
            inputPinOrPassword = inputPinOrPassword,
            cancellationReason = cancellationReason,
            cancelledBy = cancelledBy,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    /**
     * Updates booking details (date, time, address, details) with 8-hour check.
     */
    override fun updateBookingDetails(
        updatedBooking: BookingEntity,
        inputPin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        lifecycleDelegate.updateBookingDetails(updatedBooking, inputPin, onSuccess, onError)
    }

    /**
     * Deletes booking from database.
     */
    override fun deleteBooking(bookingId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        lifecycleDelegate.deleteBooking(bookingId, onSuccess, onError)
    }

    suspend fun lockBooking(bookingId: String, lockDurationMs: Long): Result<Unit> =
        lifecycleDelegate.lockBooking(bookingId, lockDurationMs)
}

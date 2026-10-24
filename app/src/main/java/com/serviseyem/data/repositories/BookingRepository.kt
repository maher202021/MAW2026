package com.serviseyem.data.repositories

import android.content.Context
import android.util.Log
import com.serviseyem.data.BookingEntity

import com.serviseyem.security.BookingSecurityHelper
import com.serviseyem.utils.AnalyticsEventsHelper
import com.serviseyem.utils.AppConstants
import com.serviseyem.utils.BookingNotificationManager
import com.serviseyem.utils.BookingUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * 📦 BookingRepository
 * Offline-First repository implementation for bookings.
 * Synchronizes with Firestore while caching locally via LocalAppCacheManager and Moshi.
 * Enforces security validations, 8-hour countdown rule, and SHA-256 PIN hashing.
 */
class BookingRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IBookingRepository {

    private val repositoryScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)


    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val _cachedBookings = MutableStateFlow<List<BookingEntity>>(emptyList())
    override val cachedBookings: StateFlow<List<BookingEntity>> = _cachedBookings.asStateFlow()

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

    private val cacheMutex = kotlinx.coroutines.sync.Mutex()

    private fun saveToCache(list: List<BookingEntity>) {
        _cachedBookings.value = list
    }

    /**
     * Realtime flow of all bookings for a user with offline fallback.
     */
    override fun getUserBookings(userId: String, pageLimit: Long): Flow<List<BookingEntity>> =
        getBookingsFlowInternal(userId, isProvider = false, pageLimit = pageLimit)

    /**
     * Realtime flow of all bookings for a provider with offline fallback.
     */
    override fun getProviderBookings(providerId: String, pageLimit: Long): Flow<List<BookingEntity>> =
        getBookingsFlowInternal(providerId, isProvider = true, pageLimit = pageLimit)

    /**
     * Realtime flow of all bookings for a user or provider with offline fallback.
     */
    override fun getBookingsFlow(userId: String, isProvider: Boolean): Flow<List<BookingEntity>> =
        getBookingsFlowInternal(userId, isProvider = isProvider, pageLimit = 100L)

    private fun getBookingsFlowInternal(
        userId: String,
        isProvider: Boolean,
        pageLimit: Long
    ): Flow<List<BookingEntity>> = callbackFlow {
        val safeLimit = pageLimit.coerceIn(1L, 500L)
        val cleanUser = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(userId).ifBlank { userId.trim() }
        fun filterLocal(items: List<BookingEntity>): List<BookingEntity> {
            return if (userId.isNotBlank()) {
                items.filter {
                    if (isProvider) {
                        it.providerId == userId ||
                            com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(it.providerPhone) == cleanUser
                    } else {
                        it.clientId == userId ||
                            it.customerPhone == userId ||
                            com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(it.effectiveCustomerPhone) == cleanUser
                    }
                }
            } else items
        }

        // Emit memory cache immediately for instant rendering
        val local = loadFromCache()
        if (local.isNotEmpty()) {
            trySend(filterLocal(local))
        }

        val collection = firestore.collection(AppConstants.COL_BOOKINGS)
        val query = if (userId.isNotBlank()) {
            val baseQuery = if (isProvider) collection.whereEqualTo("providerId", userId)
            else collection.whereEqualTo("clientId", userId)
            baseQuery.limit(safeLimit)
        } else {
            collection.orderBy("createdAt", Query.Direction.DESCENDING).limit(safeLimit)
        }

        val listener: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("BookingRepository", "Firestore listener failed: ${error.message}")
                trySend(filterLocal(loadFromCache()))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(BookingEntity::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (userId.isBlank()) {
                    saveToCache(list)
                } else {
                    val fetchedIds = list.map { it.id }.toSet()
                    val merged = list + _cachedBookings.value.filterNot { it.id in fetchedIds }
                    saveToCache(merged)
                }
                trySend(list)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Creates a new booking with auto-generated booking code and hashed PIN.
     */
    override fun createBooking(
        booking: BookingEntity,
        rawPasswordPin: String,
        onSuccess: (BookingEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Check technician schedule conflict
            val targetDate = booking.date.ifBlank { booking.dateString }
            val targetTime = booking.time.ifBlank { booking.timeString }
            if (booking.technicianId.isNotBlank() && BookingUtils.hasTechnicianConflict(booking.technicianId, targetDate, targetTime, _cachedBookings.value)) {
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
            val current = _cachedBookings.value.toMutableList()
            current.removeAll { it.id == docId }
            current.add(0, finalBooking)
            saveToCache(current)
            AnalyticsEventsHelper.logBookingCreated(context, docId, finalBooking.serviceType.ifBlank { finalBooking.category }, finalBooking.totalAmount)

            // Sync to Firestore directly with .set() to save read costs
            firestore.collection(AppConstants.COL_BOOKINGS).document(docId).set(finalBooking)
                .addOnSuccessListener {
                    // Write Notification payloads to "notifications" collection
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

                    val normalizedProviderPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(finalBooking.providerPhone)
                    val canonicalProviderTarget = normalizedProviderPhone.ifBlank { finalBooking.providerId.trim() }
                    if (canonicalProviderTarget.isNotBlank()) {
                        val dedupKey = "NEW_BOOKING_${finalBooking.id}"
                        val providerNotifId = "notif_${dedupKey}"
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
                    } catch (e: Exception) {}

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

                    AnalyticsEventsHelper.logBookingCreated(context, finalBooking.id, finalBooking.serviceType, finalBooking.totalAmount)

                    onSuccess(finalBooking)
                }
                .addOnFailureListener { ex ->
                    val firestoreEx = ex as? com.google.firebase.firestore.FirebaseFirestoreException
                    val isOffline = firestoreEx?.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE ||
                        ex.message?.contains("offline", ignoreCase = true) == true ||
                        ex.message?.contains("network", ignoreCase = true) == true

                    if (isOffline) {
                        // Queue for offline sync and notify caller that cloud sync is pending
                        onError("تعذر الاتصال بالخادم حالياً؛ يرجى التأكد من اتصال الإنترنت والمحاولة مرة أخرى.")
                    } else {
                        // Rollback optimistic local cache on permanent server rejection
                        val rolledBack = _cachedBookings.value.filterNot { it.id == finalBooking.id }
                        saveToCache(rolledBack)
                        onError(ex.localizedMessage ?: "فشل إنشاء الحجز في الخادم، يرجى المحاولة مرة أخرى.")
                    }
                }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "فشل إنشاء الحجز")
        }
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
        val now = System.currentTimeMillis()
        val updates = mutableMapOf<String, Any>(
            "status" to newStatus,
            "updatedAt" to now
        )

        val existingBooking = _cachedBookings.value.find { it.id == bookingId }
        if (existingBooking != null && existingBooking.status.isNotBlank()) {
            if (!com.serviseyem.utils.BookingStateMachine.canTransition(existingBooking.status, newStatus)) {
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

        val previousBookings = _cachedBookings.value
        // Optimistic local update
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
                // Rollback local cache on failure
                saveToCache(previousBookings)
                onError(it.localizedMessage ?: "فشل تحديث حالة الحجز")
            }
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

        val previousBookings = _cachedBookings.value
        // Optimistic local update
        val current = previousBookings.map {
            if (it.id == booking.id) it.copy(
                status = com.serviseyem.utils.BookingStatus.CANCELLED.code,
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
                val notifTime = System.currentTimeMillis()
                
                val userTitle = when(cancelledBy) {
                    "USER" -> "تم إلغاء حجزك بنجاح"
                    "PROVIDER" -> "اعتذر الفني عن الحجز"
                    else -> "تم إلغاء حجزك من قبل الإدارة"
                }
                val userMsg = when(cancelledBy) {
                    "USER" -> "تم إلغاء طلب الحجز #${booking.bookingNumber} بناءً على طلبك. السبب: $cancellationReason"
                    "PROVIDER" -> "قام الفني بإلغاء الحجز #${booking.bookingNumber}. السبب: $cancellationReason"
                    else -> "قامت الإدارة بإلغاء الحجز #${booking.bookingNumber}. السبب: $cancellationReason"
                }

                val cleanUserPhone = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(booking.effectiveCustomerPhone)
                if (cleanUserPhone.isNotBlank()) {
                    val uDedup = "CANCEL_BOOKING_USER_${booking.id}"
                    val uId = "notif_$uDedup"
                    firestore.collection(AppConstants.COL_NOTIFICATIONS).document(uId).set(mapOf(
                        "id" to uId,
                        "dedupKey" to uDedup,
                        "title" to userTitle,
                        "message" to userMsg,
                        "targetType" to "USER",
                        "targetValue" to cleanUserPhone,
                        "bookingId" to booking.id,
                        "timestamp" to notifTime
                    ))
                }

                val providerTitle = when(cancelledBy) {
                    "USER" -> "قام العميل بإلغاء الحجز"
                    "PROVIDER" -> "تم إلغاء الحجز بنجاح"
                    else -> "قامت الإدارة بإلغاء الحجز"
                }
                val providerMsg = when(cancelledBy) {
                    "USER" -> "قام العميل بإلغاء الحجز #${booking.bookingNumber}. السبب: $cancellationReason"
                    "PROVIDER" -> "تم إلغاء الحجز #${booking.bookingNumber} بناءً على طلبك. السبب: $cancellationReason"
                    else -> "قامت الإدارة بإلغاء الحجز #${booking.bookingNumber}. السبب: $cancellationReason"
                }

                val pTarget = com.serviseyem.domain.usecases.ValidatePhoneUseCase.normalizePhone(booking.providerPhone)
                    .ifBlank { booking.providerId.trim() }
                if (pTarget.isNotBlank()) {
                    val pDedup = "CANCEL_BOOKING_PROVIDER_${booking.id}"
                    val pId = "notif_$pDedup"
                    firestore.collection(AppConstants.COL_NOTIFICATIONS).document(pId).set(mapOf(
                        "id" to pId,
                        "dedupKey" to pDedup,
                        "title" to providerTitle,
                        "message" to providerMsg,
                        "targetType" to "PROVIDER",
                        "targetValue" to pTarget,
                        "bookingId" to booking.id,
                        "timestamp" to notifTime
                    ))
                }

                val aDedup = "CANCEL_BOOKING_ADMIN_${booking.id}"
                val aId = "notif_$aDedup"
                firestore.collection(AppConstants.COL_NOTIFICATIONS).document(aId).set(mapOf(
                    "id" to aId,
                    "dedupKey" to aDedup,
                    "title" to "إشعار إداري: إلغاء حجز",
                    "message" to "تم إلغاء الحجز #${booking.bookingNumber} من قبل $cancelledBy. السبب: $cancellationReason",
                    "targetType" to "ADMIN_ONLY",
                    "targetValue" to "ALL",
                    "bookingId" to booking.id,
                    "timestamp" to notifTime
                ))
                onSuccess()
            }
            .addOnFailureListener {
                // Rollback local cache on failure
                saveToCache(previousBookings)
                onError(it.localizedMessage ?: "فشل إلغاء الحجز")
            }
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
        if (BookingSecurityHelper.isBookingLocked(context, updatedBooking.id)) {
            val remainingSec = BookingSecurityHelper.getRemainingLockoutSeconds(context, updatedBooking.id)
            onError("الحجز مقفل مؤقتاً. انتظر ${remainingSec / 60} دقيقة.")
            return
        }

        val existingBooking = _cachedBookings.value.find { it.id == updatedBooking.id } ?: updatedBooking
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
        val previousBookings = _cachedBookings.value
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

    /**
     * Deletes booking from database.
     */
    override fun deleteBooking(bookingId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val previousBookings = _cachedBookings.value
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

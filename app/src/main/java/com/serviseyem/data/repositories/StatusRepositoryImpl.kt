package com.serviseyem.data.repositories

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.NotificationEntity
import com.serviseyem.data.PendingProviderEntity
import com.serviseyem.data.models.InstantRequestEntity
import kotlinx.coroutines.flow.Flow

/**
 * 📦 StatusRepositoryImpl
 * Implements IStatusRepository for system stats, pending join requests, notifications, and system bookings.
 */
class StatusRepositoryImpl(
    @Suppress("UNUSED_PARAMETER") context: Context? = null
) : IStatusRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val metricsAndFeedsDelegate = StatusMetricsAndFeedsDelegate(firestore)
    private val moderationDelegate = JoinRequestModerationDelegate(firestore)

    override fun getSystemMetrics(): Flow<SystemStatusMetrics> =
        metricsAndFeedsDelegate.getSystemMetrics()

    override fun getSystemMetricsFlow(): Flow<SystemStatusMetrics> = getSystemMetrics()

    override fun getPendingJoinRequests(): Flow<List<PendingProviderEntity>> =
        metricsAndFeedsDelegate.getPendingJoinRequests()

    override fun getPendingJoinRequestsFlow(): Flow<List<PendingProviderEntity>> = getPendingJoinRequests()

    override fun getSystemBookings(): Flow<List<BookingEntity>> =
        metricsAndFeedsDelegate.getSystemBookings()

    override fun getSystemBookingsFlow(): Flow<List<BookingEntity>> = getSystemBookings()

    override fun getInstantRequests(): Flow<List<InstantRequestEntity>> =
        metricsAndFeedsDelegate.getInstantRequests()

    override fun getInstantRequestsFlow(): Flow<List<InstantRequestEntity>> = getInstantRequests()

    override fun getNotifications(): Flow<List<NotificationEntity>> =
        metricsAndFeedsDelegate.getNotifications()

    override fun getNotificationsFlow(): Flow<List<NotificationEntity>> = getNotifications()

    override suspend fun approveJoinRequest(request: PendingProviderEntity): Result<Unit> =
        moderationDelegate.approveJoinRequest(request)

    override suspend fun rejectJoinRequest(request: PendingProviderEntity, reason: String): Result<Unit> =
        moderationDelegate.rejectJoinRequest(request, reason)

    override suspend fun clearNotifications(): Result<Unit> =
        metricsAndFeedsDelegate.clearNotifications()

    override suspend fun refreshSystemStatus(): Result<Unit> =
        metricsAndFeedsDelegate.refreshSystemStatus()
}

package com.serviseyem.data.repositories

import com.serviseyem.data.BookingEntity
import com.serviseyem.data.NotificationEntity
import com.serviseyem.data.PendingProviderEntity
import com.serviseyem.data.models.InstantRequestEntity
import kotlinx.coroutines.flow.Flow

/**
 * 🏛️ واجهة مستودع الحالة والمؤشرات العامة للنظام (IStatusRepository)
 * تفصل منطق الوصول للبيانات عن واجهة المستخدم وفق Clean Architecture
 */
interface IStatusRepository {
    // التدفقات الحية للمؤشرات والطلبات
    fun getSystemMetrics(): Flow<SystemStatusMetrics>
    fun getSystemMetricsFlow(): Flow<SystemStatusMetrics> = getSystemMetrics()

    fun getPendingJoinRequests(): Flow<List<PendingProviderEntity>>
    fun getPendingJoinRequestsFlow(): Flow<List<PendingProviderEntity>> = getPendingJoinRequests()

    fun getSystemBookings(): Flow<List<BookingEntity>>
    fun getSystemBookingsFlow(): Flow<List<BookingEntity>> = getSystemBookings()

    fun getInstantRequests(): Flow<List<InstantRequestEntity>>
    fun getInstantRequestsFlow(): Flow<List<InstantRequestEntity>> = getInstantRequests()

    fun getNotifications(): Flow<List<NotificationEntity>>
    fun getNotificationsFlow(): Flow<List<NotificationEntity>> = getNotifications()

    // الإجراءات الإدارية وتحديث الحالات
    suspend fun approveJoinRequest(request: PendingProviderEntity): Result<Unit>
    suspend fun rejectJoinRequest(request: PendingProviderEntity, reason: String = ""): Result<Unit>
    suspend fun clearNotifications(): Result<Unit>
    suspend fun refreshSystemStatus(): Result<Unit>
}

package com.serviseyem.ui

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.NotificationEntity
import kotlinx.coroutines.launch
import java.util.UUID

fun MainViewModel.triggerNotification(
    title: String,
    message: String,
    targetType: String = "ALL",
    targetValue: String = "",
    context: Context? = null
) {
    val newNotif = NotificationEntity(
        id = UUID.randomUUID().toString(),
        title = title,
        message = message,
        targetType = targetType,
        targetValue = targetValue,
        timestamp = System.currentTimeMillis()
    )
    val currentList = _notifications.value.toMutableList()
    currentList.add(0, newNotif)
    _notifications.value = currentList
    try {
        viewModelScope.launch { notificationRepository.saveNotification(newNotif) }
    } catch (e: Exception) {
        android.util.Log.e("MainViewModel", "Error: ", e)
    }
    triggerNotification("$title: $message", context)
}

fun MainViewModel.triggerNotification(msg: String, context: Context? = null) {
    val now = System.currentTimeMillis()
    if (msg == lastNotifMsg && (now - lastNotifTime) < 3000L) {
        return
    }
    lastNotifMsg = msg
    lastNotifTime = now
    _toastMessage.value = msg
    val ctx = context ?: appContext
    if (ctx != null) {
        try {
            val channelId = "yemen_services_alerts"
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "إشعارات الخدمة والحجوزات والمحادثات",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "إشعارات التطبيق الفورية"
                    enableVibration(true)
                }
                nm?.createNotificationChannel(channel)
            }
            val intent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)?.apply {
                flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                ctx, (System.currentTimeMillis() % 1000).toInt(), intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val builder = androidx.core.app.NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("دليل خدمات اليمن 🔔")
                .setContentText(msg)
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(msg))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
            nm?.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Error: ", e)
        }
    }
}

fun MainViewModel.clearNotification() {
    _toastMessage.value = null
}

fun MainViewModel.addNotification(
    title: String,
    message: String,
    targetType: String = "ALL",
    targetValue: String = "",
    targetAudience: String = "ALL",
    targetRoles: List<String> = emptyList(),
    targetUserIds: List<String> = emptyList(),
    senderId: String = "SYSTEM",
    senderName: String = "النظام",
    dedupKey: String = "",
    expiryTimestamp: Long = 0L,
    scheduledTime: Long = 0L,
    customerPhone: String = "",
    customerName: String = "",
    notificationType: String = "NORMAL",
    channel: String = "IN_APP"
) {
    if (title.trim().isEmpty() || message.trim().isEmpty()) {
        return
    }
    val providerByPhone = _providers.value.find { it.phone.trim() == targetValue.trim() }
    val providerById = _providers.value.find { it.id == targetValue }
    val isNotifDisabled = (providerByPhone?.isNotificationsDisabled == true) || (providerById?.isNotificationsDisabled == true)
    if (isNotifDisabled) {
        triggerNotification("⚠️ تم حجب إرسال هذا الإشعار لأن الإدارة قامت بتعطيل إشعارات الفني: ${providerByPhone?.name ?: providerById?.name ?: ""}")
        return
    }
    val finalDedupKey = if (dedupKey.isNotBlank()) dedupKey else "${notificationType}_${targetValue}_${title}_${System.currentTimeMillis() / (30 * 1000L)}"
    val isDuplicate = _notifications.value.any { it.dedupKey == finalDedupKey || (it.title == title && it.targetValue == targetValue && Math.abs(it.timestamp - System.currentTimeMillis()) < 15000L) }
    if (isDuplicate) {
        return
    }
    val newNotif = NotificationEntity(
        id = "n_" + UUID.randomUUID().toString().take(8),
        title = title.trim(),
        message = message.trim(),
        targetType = targetType,
        targetValue = targetValue,
        targetAudience = targetAudience,
        targetRoles = targetRoles,
        targetUserIds = targetUserIds,
        senderId = senderId,
        senderName = senderName,
        dedupKey = finalDedupKey,
        timestamp = System.currentTimeMillis(),
        expiryTimestamp = expiryTimestamp,
        scheduledTime = scheduledTime,
        customerPhone = customerPhone,
        customerName = customerName,
        notificationType = notificationType,
        channel = channel
    )
    _notifications.value = listOf(newNotif) + _notifications.value.filter { it.id != newNotif.id }
    try {
        viewModelScope.launch { notificationRepository.saveNotification(newNotif) }
    } catch (e: Exception) {
        android.util.Log.e("MainViewModel", "Error: ", e)
    }
}

fun MainViewModel.markNotificationAsRead(context: Context, notifId: String) {
    val currentRead = _readNotificationIds.value.toMutableSet()
    val updated = preferenceHelper.markNotificationAsRead(context, notifId, currentRead)
    _readNotificationIds.value = updated
}

fun MainViewModel.loadReadNotifications(context: Context) {
    _readNotificationIds.value = preferenceHelper.getReadNotificationIds(context)
}

fun MainViewModel.markAllNotificationsAsRead(context: Context) {
    val allIds = _notifications.value.map { it.id }.toSet()
    preferenceHelper.markAllNotificationsAsRead(context, allIds)
    _readNotificationIds.value = allIds
}

fun MainViewModel.markNotificationsAsRead(context: Context, ids: Set<String>) {
    val current = _readNotificationIds.value.toMutableSet()
    if (current.addAll(ids)) {
        preferenceHelper.markAllNotificationsAsRead(context, current)
        _readNotificationIds.value = current
    }
}

fun MainViewModel.deleteNotification(notifId: String) {
    viewModelScope.launch { notificationRepository.deleteNotification(notifId) }
    _notifications.value = _notifications.value.filter { it.id != notifId }
    val currentRead = _readNotificationIds.value.toMutableSet()
    currentRead.remove(notifId)
    _readNotificationIds.value = currentRead
}

fun MainViewModel.deleteAllNotifications() {
    val allNotifs = _notifications.value
    _notifications.value = emptyList()
    _readNotificationIds.value = emptySet()
    allNotifs.forEach { notif ->
        viewModelScope.launch { notificationRepository.deleteNotification(notif.id) }
    }
}

fun MainViewModel.clearAllNotifications() {
    deleteAllNotifications()
}

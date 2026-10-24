package com.serviseyem.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build

/**
 * 🔔 Centralized Notification Channels Manager
 */
object NotificationChannels {
    
    // Core & Chat Channels
    const val CHAT_MESSAGES = "chat_messages"
    const val CHAT_VOICE = "chat_voice"
    const val CHAT_MEDIA = "chat_media"
    const val URGENT_ALERTS = "urgent_alerts"
    const val BOOKING_REMINDERS = "booking_reminders"
    const val GENERAL = "general_notifications"
    
    // Admin & Secondary Channels
    const val ADMIN_CRITICAL = "admin_critical"
    const val ADMIN_NORMAL = "admin_normal"
    const val USER_BOOKINGS = "user_bookings"
    const val USER_CHAT = "user_chat"
    const val USER_GENERAL = "user_general"
    
    // Legacy / Specific Provider Channels
    const val CHAT_MESSAGES_CHANNEL = "chat_messages_channel"
    const val CHAT_VOICE_CHANNEL = "chat_voice_channel"
    const val CHAT_MEDIA_CHANNEL = "chat_media_channel"
    const val URGENT_ALERTS_CHANNEL = "urgent_alerts_channel"
    const val BOOKING_REMINDER_CHANNEL = "booking_reminder_channel"
    
    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        
        val channels = listOf(
            NotificationChannel(CHAT_MESSAGES, "رسائل المحادثات", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "إشعارات الرسائل الفورية في المحادثات"
                enableLights(true)
                enableVibration(true)
            },
            NotificationChannel(CHAT_VOICE, "الرسائل الصوتية", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "إشعارات المكالمات والرسائل الصوتية"
                enableLights(true)
                enableVibration(true)
            },
            NotificationChannel(CHAT_MEDIA, "الوسائط والمستندات", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "إشعارات الصور والمستندات المرسلة"
            },
            NotificationChannel(URGENT_ALERTS, "الطلبات العاجلة", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "تنبيهات طلبات الخدمة الفورية والمزادات"
                enableLights(true)
                enableVibration(true)
            },
            NotificationChannel(BOOKING_REMINDERS, "تذكيرات الحجوزات", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "تذكيرات بمواعيد الحجوزات وتغيير الحالات"
                enableVibration(true)
            },
            NotificationChannel(GENERAL, "إشعارات عامة", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "إشعارات التحديثات والعروض العامة"
            },
            // Admin Critical
            NotificationChannel(ADMIN_CRITICAL, "إشعارات الأدمن الحرجة", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "طلبات استعادة كلمة المرور، البلاغات العاجلة، والمدفوعات"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                lightColor = Color.RED
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setBypassDnd(true)
                }
            },
            // Admin Normal
            NotificationChannel(ADMIN_NORMAL, "إشعارات الأدمن العادية", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "تحديثات، إحصائيات، تنبيهات عامة للإدارة"
                enableVibration(true)
                setShowBadge(true)
            },
            // User Bookings & Chat
            NotificationChannel(USER_BOOKINGS, "إشعارات الحجوزات", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "حجوزاتك وتحديثاتها أولاً بأول"
                enableVibration(true)
                setShowBadge(true)
            },
            NotificationChannel(USER_CHAT, "إشعارات المحادثات", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "رسائل المحادثات والدعم الفني الجديدة"
                enableVibration(true)
                setShowBadge(true)
            },
            NotificationChannel(USER_GENERAL, "إشعارات عامة", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "تنبيهات عامة وعروض المنصة"
            },
            // Legacy Specific aliases
            NotificationChannel(CHAT_MESSAGES_CHANNEL, "رسائل المحادثات المباشرة", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "إشعارات الرسائل النصية المباشرة"
                enableLights(true)
                enableVibration(true)
            },
            NotificationChannel(CHAT_VOICE_CHANNEL, "الرسائل الصوتية والمكالمات", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "إشعارات الرسائل الصوتية والاتصالات"
                enableLights(true)
                enableVibration(true)
            },
            NotificationChannel(CHAT_MEDIA_CHANNEL, "الوسائط والمستندات المرفقة", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "إشعارات الصور ومقاطع الفيديو والمستندات المرفقة"
            },
            NotificationChannel(URGENT_ALERTS_CHANNEL, "الطلبات العاجلة والطوارئ 🚨", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "إشعارات فورية فائقة الأولوية للطلبات العاجلة خلال 30 دقيقة"
                enableLights(true)
                enableVibration(true)
            },
            NotificationChannel(BOOKING_REMINDER_CHANNEL, "تنبيهات المواعيد والحجوزات", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "إشعارات تذكير المستخدمين بمواعيد الحجوزات القادمة"
                enableVibration(true)
            }
        )
        
        manager.createNotificationChannels(channels)
    }
}

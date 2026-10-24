package com.serviseyem.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.serviseyem.MainActivity

@Keep
data class ScheduledNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val scheduledTime: Long = 0L,
    val isActive: Boolean = true,
    val type: String = "REMINDER", // "REMINDER", "DAILY", "WEEKLY"
    val bookingId: String = ""
)

/**
 * ⏰ NotificationScheduler & Receiver
 * جدولة التنبيهات والإشعارات المستقبلية لمواعيد الحجوزات، تذكيرات الصيانة، والطلبات العاجلة.
 */
class ScheduledNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_NOTIF_TITLE") ?: "تذكير بالموعد 🔔"
        val message = intent.getStringExtra("EXTRA_NOTIF_MESSAGE") ?: "لديك حجز قادم قريباً."
        val notifId = intent.getIntExtra("EXTRA_NOTIF_ID", 1001)

        ChatNotificationHelper.createNotificationChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, ChatNotificationHelper.CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build())
        } catch (ignored: Exception) {}
    }
}

object NotificationScheduler {

    fun scheduleBookingReminder(
        context: Context,
        bookingId: String,
        serviceName: String,
        targetTimestamp: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val reminderTime = targetTimestamp - (2 * 60 * 60 * 1000L) // 2 hours before

        if (reminderTime <= System.currentTimeMillis()) return

        val intent = Intent(context, ScheduledNotificationReceiver::class.java).apply {
            putExtra("EXTRA_NOTIF_TITLE", "تذكير بموعد الخدمة: $serviceName")
            putExtra("EXTRA_NOTIF_MESSAGE", "موعد خدمتك يبدأ خلال ساعتين. يرجى التجهيز.")
            putExtra("EXTRA_NOTIF_ID", bookingId.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            bookingId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                } else {
                    alarmManager.setWindow(AlarmManager.RTC_WAKEUP, reminderTime, 10 * 60 * 1000L, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
            } catch (ignored: Exception) {}
        }
    }

    fun cancelReminder(context: Context, bookingId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ScheduledNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            bookingId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleReminder(context: Context, bookingId: String, hoursBefore: Int, appointmentTimeMillis: Long = System.currentTimeMillis() + (hoursBefore + 1) * 3600 * 1000L): Boolean {
        return try {
            val triggerTime = appointmentTimeMillis - (hoursBefore * 3600 * 1000L)
            if (triggerTime <= System.currentTimeMillis()) return false
            scheduleBookingReminder(context, bookingId, "طلب #$bookingId", appointmentTimeMillis)
            true
        } catch (e: Exception) {
            false
        }
    }
}

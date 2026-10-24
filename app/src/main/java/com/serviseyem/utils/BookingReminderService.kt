package com.serviseyem.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.serviseyem.MainActivity
import com.serviseyem.R
import com.serviseyem.data.BookingEntity
import java.util.Calendar

/**
 * ⏰ BookingReminderService & Receiver
 * نظام التنبيهات المسبقة للحجوزات والمواعيد:
 * - تنبيه قبل 24 ساعة من الموعد.
 * - تنبيه قبل ساعة واحدة من الموعد.
 * - يعمل محلياً بالكامل عبر AlarmManager بدون أي تكاليف على Firebase.
 */
class BookingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bookingId = intent.getStringExtra("booking_id") ?: ""
        val title = intent.getStringExtra("title") ?: "تذكير بموعد الخدمة ⏰"
        val message = intent.getStringExtra("message") ?: "لديك موعد خدمة مجدول قريباً."
        val notificationId = intent.getIntExtra("notif_id", 1001)

        BookingReminderService.showNotification(context, notificationId, title, message)
    }
}

object BookingReminderService {

    private const val CHANNEL_ID = "booking_reminder_channel"
    private const val CHANNEL_NAME = "تنبيهات المواعيد والحجوزات"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات تذكير المستخدمين بمواعيد الحجوزات القادمة"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * جدولة تذكير 24 ساعة وتذكير 1 ساعة قبل الموعد
     */
    fun scheduleBookingReminders(context: Context, booking: BookingEntity) {
        createNotificationChannel(context)

        val dateStr = if (booking.date.isNotBlank()) booking.date else booking.dateString
        val timeStr = if (booking.time.isNotBlank()) booking.time else booking.timeString

        val appointmentMillis = parseDateTimeToMillis(dateStr, timeStr) ?: return
        val currentMillis = System.currentTimeMillis()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // 1. Reminder 24 hours before
        val reminder24h = appointmentMillis - (24 * 60 * 60 * 1000)
        if (reminder24h > currentMillis) {
            val intent24h = Intent(context, BookingReminderReceiver::class.java).apply {
                putExtra("booking_id", booking.id)
                putExtra("title", "⏰ تذكير: موعدك غداً!")
                putExtra("message", "موعد خدمتك مع (${booking.providerName.ifEmpty { "الفني" }}) غداً في تمام الساعة $timeStr.")
                putExtra("notif_id", (booking.id.hashCode() + 24))
            }
            val pendingIntent24h = PendingIntent.getBroadcast(
                context,
                (booking.id.hashCode() + 24),
                intent24h,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder24h, pendingIntent24h)
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminder24h, pendingIntent24h)
            }
        }

        // 2. Reminder 1 hour before
        val reminder1h = appointmentMillis - (60 * 60 * 1000)
        if (reminder1h > currentMillis) {
            val intent1h = Intent(context, BookingReminderReceiver::class.java).apply {
                putExtra("booking_id", booking.id)
                putExtra("title", "🚨 اقترب الموعد: بعد ساعة واحدة!")
                putExtra("message", "تذكير: موعدك بعد ساعة واحدة في تمام الساعة $timeStr مع (${booking.providerName.ifEmpty { "الفني" }}).")
                putExtra("notif_id", (booking.id.hashCode() + 1))
            }
            val pendingIntent1h = PendingIntent.getBroadcast(
                context,
                (booking.id.hashCode() + 1),
                intent1h,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder1h, pendingIntent1h)
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminder1h, pendingIntent1h)
            }
        }
    }

    fun showNotification(context: Context, notifId: Int, title: String, message: String) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notification)
    }

    fun cancelBookingReminders(context: Context, bookingId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent24h = Intent(context, BookingReminderReceiver::class.java)
        val pendingIntent24h = PendingIntent.getBroadcast(
            context,
            (bookingId.hashCode() + 24),
            intent24h,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent24h)

        val intent1h = Intent(context, BookingReminderReceiver::class.java)
        val pendingIntent1h = PendingIntent.getBroadcast(
            context,
            (bookingId.hashCode() + 1),
            intent1h,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent1h)
    }

    private fun parseDateTimeToMillis(dateStr: String, timeStr: String): Long? {
        return try {
            // ✨ م2: استخدام تحليل التاريخ المباشر لتجنب SimpleDateFormat
            val parts = dateStr.split("-")
            if (parts.size != 3) return null
            val year = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull() ?: return null
            val day = parts[2].toIntOrNull() ?: return null
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
            }

            var hour = 9
            var minute = 0
            if (timeStr.contains(":")) {
                val parts = timeStr.split(":", " ")
                hour = parts[0].filter { it.isDigit() }.toIntOrNull() ?: 9
                minute = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
                if (timeStr.contains("م") || timeStr.contains("PM", ignoreCase = true)) {
                    if (hour < 12) hour += 12
                }
            }

            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.SECOND, 0)
            cal.timeInMillis
        } catch (e: Exception) {
            null
        }
    }
}

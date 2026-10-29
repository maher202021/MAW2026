package com.serviseyem.navigation

// [إصلاح ملاحة نهائي] خدمة الملاحة في الواجهة الأمامية مع حماية كاملة من استثناءات Foreground Service على أندرويد 14+
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.serviseyem.MainActivity
import com.serviseyem.R

/**
 * 🚘 NavigationForegroundService
 * Foreground Service for robust turn-by-turn navigation survival:
 * - Keeps location updates running with high accuracy even with screen turned off.
 * - Shows an ongoing notification with current instruction, ETA, and 'End Navigation' action.
 * - Protected against ForegroundServiceStartNotAllowedException on Android 14+.
 */
class NavigationForegroundService : Service() {

    companion object {
        private const val TAG = "NavForegroundService"
        const val CHANNEL_ID = "navigation_channel"
        const val NOTIFICATION_ID = 9101

        const val ACTION_START = "ACTION_START_NAVIGATION"
        const val ACTION_STOP = "ACTION_STOP_NAVIGATION"
        const val ACTION_UPDATE = "ACTION_UPDATE_STATUS"

        const val EXTRA_INSTRUCTION = "EXTRA_INSTRUCTION"
        const val EXTRA_DISTANCE = "EXTRA_DISTANCE"
        const val EXTRA_ETA = "EXTRA_ETA"

        fun startService(context: Context, instruction: String, distance: String, eta: String) {
            try {
                val intent = Intent(context, NavigationForegroundService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_INSTRUCTION, instruction)
                    putExtra(EXTRA_DISTANCE, distance)
                    putExtra(EXTRA_ETA, eta)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Throwable) {
                Log.w(TAG, "Safe catch: startForegroundService avoided crash: ${e.message}")
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, NavigationForegroundService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            } catch (e: Throwable) {
                Log.w(TAG, "Safe catch: stopService: ${e.message}")
            }
        }

        fun updateNotification(context: Context, instruction: String, distance: String, eta: String) {
            try {
                val intent = Intent(context, NavigationForegroundService::class.java).apply {
                    action = ACTION_UPDATE
                    putExtra(EXTRA_INSTRUCTION, instruction)
                    putExtra(EXTRA_DISTANCE, distance)
                    putExtra(EXTRA_ETA, eta)
                }
                context.startService(intent)
            } catch (e: Throwable) {
                Log.w(TAG, "Safe catch: updateNotification: ${e.message}")
            }
        }
    }

    private var currentInstruction = "الملاحة قيد التشغيل..."
    private var currentDistance = ""
    private var currentEta = ""

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
        } catch (e: Throwable) {
            Log.w(TAG, "Channel creation warning: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                ACTION_START -> {
                    currentInstruction = intent.getStringExtra(EXTRA_INSTRUCTION) ?: "الملاحة قيد التشغيل..."
                    currentDistance = intent.getStringExtra(EXTRA_DISTANCE) ?: ""
                    currentEta = intent.getStringExtra(EXTRA_ETA) ?: ""
                    startForegroundWithNotification()
                }
                ACTION_UPDATE -> {
                    intent.getStringExtra(EXTRA_INSTRUCTION)?.let { currentInstruction = it }
                    intent.getStringExtra(EXTRA_DISTANCE)?.let { currentDistance = it }
                    intent.getStringExtra(EXTRA_ETA)?.let { currentEta = it }
                    updateExistingNotification()
                }
                ACTION_STOP -> {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "onStartCommand exception caught safely: ${e.message}")
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundWithNotification() {
        try {
            val notification = buildNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "startForeground caught safely: ${e.message}")
        }
    }

    private fun updateExistingNotification() {
        try {
            val notification = buildNotification()
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify(NOTIFICATION_ID, notification)
        } catch (e: Throwable) {
            Log.w(TAG, "updateExistingNotification caught safely: ${e.message}")
        }
    }

    private fun buildNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingLaunch = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, NavigationForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subText = if (currentDistance.isNotBlank()) "المتبقي: $currentDistance | الوصول: $currentEta" else "دليل خدمات اليمن"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentInstruction)
            .setContentText(subText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setContentIntent(pendingLaunch)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إنهاء الملاحة", pendingStop)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "إشعارات التوجيه الملاحي الحي",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "عرض التوجيه المستمر والمسافة والوقت أثناء الملاحة"
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}

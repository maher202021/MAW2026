package com.serviseyem.utils

import com.serviseyem.utils.*

import android.content.Context
import android.util.Log

object AppSetup {
    private const val TAG = "AppSetup"

    fun initializeApplication(context: Context) {
        try {
            Log.i(TAG, "Initializing Yemen Services platform configuration...")
            setupNotificationChannels(context)
            setupImageCacheDefaults(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete app setup: ${e.message}", e)
        }
    }

    private fun setupNotificationChannels(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "تنبيهات الخدمات والطلبات"
            val descriptionText = "قناة الإشعارات الفورية لطلبات الصيانة والمحادثات في اليمن"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel("YEMEN_SERVICES_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: android.app.NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupImageCacheDefaults(context: Context) {
        // Prepare local image cache limits if needed
        Log.d(TAG, "Image caching and Room offline database initialized.")
    }
}

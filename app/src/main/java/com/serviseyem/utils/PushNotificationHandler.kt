package com.serviseyem.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import com.serviseyem.MainActivity
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Keep
data class PushNotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val message: String = "",
    val targetScreen: String = "home",
    val data: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

/**
 * 📲 PushNotificationHandler
 * استقبال وعرض الإشعارات الفورية والتوجيه وتخزين الإشعارات وإدارة حالتها
 */
class PushNotificationHandler(private val context: Context) {

    private val prefs = context.getSharedPreferences("push_notifications_store", Context.MODE_PRIVATE)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    companion object {
        private const val TAG = "PushNotificationHndlr"
        private const val CHANNEL_ID = "instant_services_push_channel"
        private const val CHANNEL_NAME = "إشعارات الطلبات والعروض الفورية"
        private const val KEY_SAVED_NOTIFS = "saved_push_notifications"
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة التنبيهات الفورية للعروض والطلبات"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun handlePushNotification(remoteMessage: RemoteMessage) {
        try {
            val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "إشعار جديد"
            val body = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: ""
            val targetScreen = remoteMessage.data["targetScreen"] ?: remoteMessage.data["screen"] ?: "requests"
            val id = remoteMessage.data["id"] ?: UUID.randomUUID().toString()

            val item = PushNotificationItem(
                id = id,
                title = title,
                message = body,
                targetScreen = targetScreen,
                data = remoteMessage.data,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            saveNotification(item)
            displayNotification(context, item)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling FCM message", e)
        }
    }

    fun displayNotification(ctx: Context, notification: PushNotificationItem) {
        try {
            val intent = Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("target_screen", notification.targetScreen)
                putExtra("notification_id", notification.id)
                for ((k, v) in notification.data) {
                    putExtra(k, v)
                }
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(ctx, notification.id.hashCode(), intent, flags)

            val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(notification.title)
                .setContentText(notification.message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            notificationManager?.notify(notification.id.hashCode(), builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying notification", e)
        }
    }

    fun saveNotification(notification: PushNotificationItem) {
        val current = getAllNotifications().toMutableList()
        val index = current.indexOfFirst { it.id == notification.id }
        if (index != -1) {
            current[index] = notification
        } else {
            current.add(0, notification)
        }
        saveAllNotifications(current)
    }

    fun getAllNotifications(): List<PushNotificationItem> {
        val jsonStr = prefs.getString(KEY_SAVED_NOTIFS, null) ?: return emptyList()
        val list = mutableListOf<PushNotificationItem>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val dataMap = mutableMapOf<String, String>()
                val dataObj = obj.optJSONObject("data")
                if (dataObj != null) {
                    val keys = dataObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        dataMap[k] = dataObj.optString(k)
                    }
                }
                list.add(
                    PushNotificationItem(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        message = obj.optString("message"),
                        targetScreen = obj.optString("targetScreen", "home"),
                        data = dataMap,
                        timestamp = obj.optLong("timestamp"),
                        isRead = obj.optBoolean("isRead", false)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing push notifications", e)
        }
        return list
    }

    fun markAsRead(notificationId: String) {
        val list = getAllNotifications().toMutableList()
        val index = list.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            list[index] = list[index].copy(isRead = true)
            saveAllNotifications(list)
        }
    }

    fun markAllAsRead() {
        val list = getAllNotifications().map { it.copy(isRead = true) }
        saveAllNotifications(list)
    }

    fun getUnreadCount(): Int {
        return getAllNotifications().count { !it.isRead }
    }

    fun deleteNotification(notificationId: String) {
        val list = getAllNotifications().filter { it.id != notificationId }
        saveAllNotifications(list)
    }

    fun navigateToScreen(ctx: Context, screen: String, data: Map<String, String>) {
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("target_screen", screen)
            for ((k, v) in data) {
                putExtra(k, v)
            }
        }
        ctx.startActivity(intent)
    }

    private fun saveAllNotifications(list: List<PushNotificationItem>) {
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("message", item.message)
            obj.put("targetScreen", item.targetScreen)
            val dataObj = JSONObject()
            for ((k, v) in item.data) {
                dataObj.put(k, v)
            }
            obj.put("data", dataObj)
            obj.put("timestamp", item.timestamp)
            obj.put("isRead", item.isRead)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_SAVED_NOTIFS, arr.toString()).apply()
    }
}

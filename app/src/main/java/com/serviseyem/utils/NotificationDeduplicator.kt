package com.serviseyem.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.serviseyem.data.NotificationEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * 🛡️ NotificationDeduplicator
 * منع تكرار الإشعارات وحذف الإشعارات القديمة وإدارة المعرفات الفريدة
 */
class NotificationDeduplicator(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("notification_dedup_prefs", Context.MODE_PRIVATE)

    init {
        cleanOldNotifications()
    }

    companion object {
        private const val TAG = "NotificationDedup"
        private const val KEY_SENT_IDS = "sent_notification_ids"
        private const val KEY_RECORD_TIMESTAMPS = "notification_timestamps"
    }

    fun generateUniqueId(notification: NotificationEntity): String {
        if (notification.relatedRequestId.isNotBlank() && notification.notificationType.isNotBlank()) {
            return "${notification.notificationType}_${notification.relatedRequestId}"
        }
        val target = if (notification.targetValue.isNotBlank()) notification.targetValue else notification.customerPhone
        val timeBucket = notification.timestamp / (60 * 1000L)
        return "${notification.notificationType}_${target}_${timeBucket}"
    }

    fun isDuplicate(notification: NotificationEntity): Boolean {
        if (notification.relatedRequestId.isNotBlank() && notification.notificationType.isNotBlank()) {
            val reqKey = "${notification.notificationType}_${notification.relatedRequestId}"
            if (getSentNotifications().contains(reqKey)) return true
        }
        val uniqueId = if (notification.id.isNotBlank()) notification.id else generateUniqueId(notification)
        val sentSet = getSentNotifications().toSet()
        val isDup = sentSet.contains(uniqueId)
        if (isDup) {
            Log.d(TAG, "Duplicate notification detected: $uniqueId")
        }
        return isDup
    }

    fun isJoinNotificationDuplicate(relatedRequestId: String, notificationType: String): Boolean {
        if (relatedRequestId.isBlank() || notificationType.isBlank()) return false
        val key = "${notificationType}_${relatedRequestId}"
        return getSentNotifications().contains(key)
    }

    fun markJoinNotificationSent(relatedRequestId: String, notificationType: String) {
        if (relatedRequestId.isBlank() || notificationType.isBlank()) return
        val key = "${notificationType}_${relatedRequestId}"
        val list = getSentNotifications().toMutableList()
        if (!list.contains(key)) {
            list.add(key)
            saveSentNotifications(list)

            val timestamps = getNotificationTimestamps().toMutableMap()
            timestamps[key] = System.currentTimeMillis()
            saveNotificationTimestamps(timestamps)
        }
    }

    fun markAsSent(notification: NotificationEntity) {
        val uniqueId = if (notification.id.isNotBlank()) notification.id else generateUniqueId(notification)
        val list = getSentNotifications().toMutableList()
        if (!list.contains(uniqueId)) {
            list.add(uniqueId)
            saveSentNotifications(list)

            val timestamps = getNotificationTimestamps().toMutableMap()
            timestamps[uniqueId] = System.currentTimeMillis()
            saveNotificationTimestamps(timestamps)
        }
    }

    fun removeDuplicates(notifications: List<NotificationEntity>): List<NotificationEntity> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<NotificationEntity>()
        for (notif in notifications) {
            val uid = if (notif.id.isNotBlank()) notif.id else generateUniqueId(notif)
            if (seen.add(uid)) {
                result.add(notif)
            }
        }
        return result
    }

    fun cleanOldNotifications(daysToKeep: Int = 30) {
        try {
            val cutoff = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
            val timestamps = getNotificationTimestamps().toMutableMap()
            val sentList = getSentNotifications().toMutableList()

            val toRemove = timestamps.filter { it.value < cutoff }.keys.toSet()
            for (key in toRemove) {
                timestamps.remove(key)
            }
            sentList.removeAll(toRemove)

            // Cap at 500 recent items to conserve SharedPreferences storage
            val finalSentList = if (sentList.size > 500) {
                sentList.takeLast(500)
            } else {
                sentList
            }

            saveNotificationTimestamps(timestamps)
            saveSentNotifications(finalSentList)
            Log.d(TAG, "Cleaned ${toRemove.size} old notifications older than $daysToKeep days")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning old notifications", e)
        }
    }

    fun getSentNotifications(): List<String> {
        val jsonStr = prefs.getString(KEY_SENT_IDS, null) ?: return emptyList()
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing sent notifications", e)
        }
        return list
    }

    private fun saveSentNotifications(list: List<String>) {
        val arr = JSONArray(list)
        prefs.edit().putString(KEY_SENT_IDS, arr.toString()).apply()
    }

    private fun getNotificationTimestamps(): Map<String, Long> {
        val jsonStr = prefs.getString(KEY_RECORD_TIMESTAMPS, null) ?: return emptyMap()
        val map = mutableMapOf<String, Long>()
        try {
            val obj = JSONObject(jsonStr)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.optLong(key)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing notification timestamps", e)
        }
        return map
    }

    private fun saveNotificationTimestamps(map: Map<String, Long>) {
        val obj = JSONObject()
        for ((k, v) in map) {
            obj.put(k, v)
        }
        prefs.edit().putString(KEY_RECORD_TIMESTAMPS, obj.toString()).apply()
    }
}

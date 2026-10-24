package com.serviseyem.data.repositories

import com.serviseyem.data.NotificationEntity
import com.serviseyem.utils.AppConstants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveNotification(notification: NotificationEntity) {
        val docId = notification.id.ifBlank { java.util.UUID.randomUUID().toString() }
        val finalNotif = if (notification.id.isBlank()) notification.copy(id = docId) else notification
        db.collection(AppConstants.COL_NOTIFICATIONS).document(docId).set(finalNotif).await()
    }

    suspend fun deleteNotification(notifId: String) {
        if (notifId.isBlank()) return
        db.collection(AppConstants.COL_NOTIFICATIONS).document(notifId).delete().await()
    }

    suspend fun cleanupOldNotifications(userPhone: String) {
        if (userPhone.isBlank()) return
        try {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val userNotifs = db.collection(AppConstants.COL_NOTIFICATIONS)
                .whereEqualTo("targetValue", userPhone)
                .get()
                .await()

            val expiredDocs = userNotifs.documents.filter { doc ->
                val ts = doc.getLong("createdAt") ?: doc.getLong("timestamp") ?: Long.MAX_VALUE
                ts < thirtyDaysAgo
            }
            if (expiredDocs.isNotEmpty()) {
                val batch = db.batch()
                for (doc in expiredDocs) {
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationRepository", "Error cleaning up old notifications: ${e.message}")
        }
    }
}

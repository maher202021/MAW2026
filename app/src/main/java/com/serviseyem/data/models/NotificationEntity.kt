package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class NotificationEntity(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val targetType: String = "ALL", // "ALL", "REGION", "CATEGORY", "USER", "PROVIDER", "SUPERVISOR", "ROLE", "SPECIFIC_USER"
    val targetValue: String = "",
    val targetAudience: String = "ADMIN_ONLY", // "ADMIN_ONLY", "SPECIFIC_ROLES", "SPECIFIC_USERS", "ALL_REGISTERED_USERS", "ALL"
    val targetRoles: List<String> = emptyList(), // "TECHNICIAN", "STORE", "MEDICAL", "RESTAURANT", "REAL_ESTATE", "ADMIN", "USER"
    val targetUserIds: List<String> = emptyList(),
    val senderId: String = "SYSTEM",
    val senderName: String = "النظام",
    val dedupKey: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val expiryTimestamp: Long = 0L,
    val scheduledTime: Long = 0L,
    val customerPhone: String = "",
    val customerName: String = "",
    val notificationType: String = "NORMAL", // "BOOKING", "MESSAGE", "SYSTEM", "ADMIN", "REGISTRATION_APPROVED", "SPECIAL_OFFER", "NORMAL", "JOIN_REQUEST", "JOIN_APPROVED", "JOIN_REJECTED"
    val channel: String = "IN_APP", // "IN_APP", "FCM", "WHATSAPP", "SMS", "TELEGRAM"
    val isRead: Boolean = false,
    val relatedRequestId: String = "",
    val fcmSent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Validation helper to reject dummy, incomplete or corrupted notifications
    fun isValid(): Boolean {
        return id.isNotBlank() &&
               title.trim().isNotEmpty() &&
               message.trim().isNotEmpty() &&
               senderId.trim().isNotEmpty() &&
               notificationType.trim().isNotEmpty()
    }

    fun getEffectiveTargetType(): String {
        return if (targetType.isNotBlank() && targetType != "ALL") targetType else targetAudience
    }

    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return expiryTimestamp > 0L && nowMillis > expiryTimestamp
    }
}

fun Map<String, Any?>.isValid(): Boolean {
    val id = this["id"] as? String
    val title = this["title"] as? String
    val message = this["message"] as? String
    return !id.isNullOrBlank() && !title.isNullOrBlank() && !message.isNullOrBlank()
}

package com.serviseyem.data.models

import androidx.annotation.Keep

/**
 * 📨 NotificationRequest
 * كائن بيانات موحد لتقليل معاملات إرسال الإشعارات وتسهيل تمريرها بين الطبقات.
 */
@Keep
data class NotificationRequest(
    val title: String,
    val message: String,
    val targetType: String = "ALL",
    val targetValue: String = "",
    val targetAudience: String = "ALL",
    val targetRoles: List<String> = emptyList(),
    val targetUserIds: List<String> = emptyList(),
    val notificationType: String = "NORMAL"
)

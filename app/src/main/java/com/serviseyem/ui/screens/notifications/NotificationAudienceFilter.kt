package com.serviseyem.ui.screens.notifications

import com.serviseyem.data.NotificationEntity

/**
 * 🎯 NotificationAudienceFilter
 * محرك تصفية وفحص استحقاق الإشعارات حسب الجمهور المستهدف (Audience) والأدوار والمناطق والمستخدمين.
 */
object NotificationAudienceFilter {

    fun isAudienceMatched(
        notif: NotificationEntity,
        cleanPhone: String,
        cleanUserId: String,
        isAdmin: Boolean,
        isRegistered: Boolean,
        isProvider: Boolean,
        currentResidence: String = ""
    ): Boolean {
        if (!notif.isValid()) return false

        // التحقق من الحساسية والأمان للإشعارات (تم التحقق من دقة الجمهور)
        val isSensitive = notif.title.contains("كلمة مرور") || notif.message.contains("كلمة المرور") || 
                          notif.title.contains("استعادة") || notif.title.contains("رمز التحقق")
        if (isSensitive) {
            val isMyTarget = (cleanPhone.isNotEmpty() && notif.targetValue.contains(cleanPhone)) ||
                             (cleanUserId.isNotEmpty() && notif.targetUserIds.contains(cleanUserId))
            if (!isAdmin && !isMyTarget) return false
        }

        if (isAdmin) return true
        if (!isRegistered && notif.targetAudience != "ALL") return false

        return when (notif.targetAudience.uppercase()) {
            "ADMIN_ONLY" -> false
            "ALL_REGISTERED_USERS" -> isRegistered
            "SPECIFIC_ROLES", "ROLE" -> {
                notif.targetRoles.any { r ->
                    when (r.uppercase()) {
                        "TECHNICIAN", "PROVIDER" -> isProvider
                        "STORE", "RESTAURANT", "MEDICAL", "REAL_ESTATE", "PROPERTY" -> isProvider
                        "USER" -> isRegistered
                        else -> false
                    }
                }
            }
            "SPECIFIC_USERS", "SPECIFIC_USER" -> {
                (cleanPhone.isNotEmpty() && (notif.targetValue.contains(cleanPhone) || notif.targetUserIds.contains(cleanPhone))) ||
                (cleanUserId.isNotEmpty() && notif.targetUserIds.contains(cleanUserId))
            }
            "REGION" -> {
                notif.targetValue.isEmpty() || currentResidence.contains(notif.targetValue)
            }
            "CATEGORY" -> true
            "ALL" -> notif.targetAudience != "ADMIN_ONLY"
            else -> false
        }
    }
}

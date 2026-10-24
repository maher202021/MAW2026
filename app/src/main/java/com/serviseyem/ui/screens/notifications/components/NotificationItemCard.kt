package com.serviseyem.ui.screens.notifications.components

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.NotificationEntity
import com.serviseyem.utils.NotificationDateFormatter
import com.serviseyem.utils.VisualThemePalette

/**
 * 🔔 NotificationItemCard
 * Reusable Card component for single notification display with actions, badges, and RTL formatting.
 */
@Composable
fun NotificationItemCard(
    notification: NotificationEntity,
    isUnread: Boolean,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val (iconText, badgeColor) = when {
        // Priority 1: Direct Type Match
        notification.notificationType == "BOOKING" -> "📅" to Color(0xFF3B82F6)
        notification.notificationType == "MESSAGE" -> "💬" to Color(0xFF06B6D4)
        notification.notificationType == "SPECIAL_OFFER" -> "🔥" to Color(0xFFF59E0B)
        notification.notificationType == "ADMIN" || notification.notificationType == "SYSTEM" -> "🛡️" to Color(0xFFFFD700)
        notification.notificationType == "JOIN_REQUEST" -> "👷" to Color(0xFF8B5CF6)
        notification.notificationType == "JOIN_APPROVED" || notification.notificationType == "REGISTRATION_APPROVED" -> "🎉" to Color(0xFF10B981)
        notification.notificationType == "JOIN_REJECTED" -> "❌" to Color(0xFFEF4444)

        // Priority 2: Title/Message Content Fallback (Heuristics)
        notification.title.contains("حجز") || notification.message.contains("حجز") -> "📅" to Color(0xFF3B82F6)
        notification.title.contains("دردشة") || notification.title.contains("رسالة") -> "💬" to Color(0xFF06B6D4)
        notification.title.contains("عرض") -> "🔥" to Color(0xFFF59E0B)
        notification.title.contains("طلب") || notification.title.contains("انضمام") -> "👷" to Color(0xFF8B5CF6)
        notification.title.contains("تفعيل") || notification.title.contains("قبول") -> "🎉" to Color(0xFF10B981)
        notification.title.contains("رفض") || notification.title.contains("إلغاء") -> "❌" to Color(0xFFEF4444)
        
        else -> "🔔" to themeColors.accent
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) Color(0xFF1E293B) else Color(0xFF0F172A).copy(alpha = 0.8f)
        ),
        border = BorderStroke(
            1.dp,
            if (isUnread) badgeColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 4.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("notification_item_${notification.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Badge
            Surface(
                shape = CircleShape,
                color = if (isUnread) badgeColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                border = BorderStroke(0.8.dp, if (isUnread) badgeColor else Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(iconText, fontSize = 18.sp)
                }
            }

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnread) Color.White else Color(0xFFE2E8F0),
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isUnread) {
                        Surface(
                            shape = CircleShape,
                            color = badgeColor,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = if (isUnread) Color(0xFFF1F5F9) else Color(0xFF94A3B8),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🕒 ${NotificationDateFormatter.format(notification.timestamp)}",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("delete_notif_btn_${notification.id}")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف الإشعار",
                            tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

package com.serviseyem.ui.screens.notifications.components

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 📭 NotificationEmptyState & Loading
 */
@Composable
fun NotificationEmptyState(
    activeTab: String,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val (emoji, message) = when (activeTab) {
        "READ" -> "📭" to "لا توجد إشعارات مقروءة حالياً"
        "UNREAD" -> "🎉" to "رائع! ليس لديك أي إشعارات غير مقروءة جديدة"
        "IMPORTANT" -> "⭐" to "لا توجد إشعارات هامة حالياً"
        else -> "🔔" to "لا توجد إشعارات نشطة حالياً"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(emoji, fontSize = 48.sp)
            Text(
                text = message,
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NotificationLoadingState(
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = themeColors.accent,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "جاري تحميل الإشعارات وتحديث الحالة...",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

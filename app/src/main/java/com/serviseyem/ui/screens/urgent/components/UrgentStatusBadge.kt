package com.serviseyem.ui.screens.urgent.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 🏷️ UrgentStatusBadge
 * شارة حالة الطلب العاجل ملونة وموحدة.
 */
@Composable
fun UrgentStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, textColor) = when (status) {
        "WAITING_FOR_OFFERS" -> Triple("في انتظار العروض ⏳", Color(0xFFD32F2F), Color.White)
        "REVIEWING_OFFERS" -> Triple("مراجعة العروض 📋", Color(0xFFF57C00), Color.White)
        "ACCEPTED" -> Triple("تم قبول العرض 🤝", Color(0xFF2E7D32), Color.White)
        "IN_PROGRESS" -> Triple("جاري التنفيذ 🛠️", Color(0xFF0284C7), Color.White)
        "COMPLETED" -> Triple("مكتمل ✅", Color(0xFF388E3C), Color.White)
        "CANCELLED" -> Triple("ملغي ❌", Color(0xFF757575), Color.White)
        "EXPIRED" -> Triple("منتهي المهلة ⌛", Color(0xFF616161), Color.White)
        else -> Triple(status, Color(0xFFD32F2F), Color.White)
    }

    Badge(
        containerColor = bgColor,
        contentColor = textColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

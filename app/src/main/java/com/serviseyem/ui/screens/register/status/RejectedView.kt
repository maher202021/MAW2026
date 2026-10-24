package com.serviseyem.ui.screens.register.status

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
 * ❌ RejectedView - عرض شاشة رفض طلب الانضمام مع سبب الرفض وإعادة التقديم
 */
@Composable
fun RejectedView(
    title: String = "❌ تم رفض طلب الانضمام",
    reason: String = "لم تستوفِ المستندات الشروط المطلوبة.",
    onReapply: () -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.5.dp, Color(0xFFEF4444))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "❌", fontSize = 28.sp)
            }

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444),
                textAlign = TextAlign.Center
            )

            Text(
                text = "سبب الرفض الإداري: $reason",
                fontSize = 11.5.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            Button(
                onClick = onReapply,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text("تعديل وإعادة تقديم الطلب 🔄", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

package com.serviseyem.ui.screens.dashboard

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.domain.entities.DashboardStatsEntity
import com.serviseyem.utils.VisualThemePalette

@Composable
fun TabStatisticsGrowth(
    stats: DashboardStatsEntity,
    themeColors: VisualThemePalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "📊 إحصائيات النشاط والنمو", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("الطلبات النشطة 📋", "${stats.activeBookingsCount} طلب", Color(0xFF3B82F6), themeColors, Modifier.weight(1f))
            StatCard("الطلبات المكتملة 🏁", "${stats.completedBookingsCount} طلب", Color(0xFF10B981), themeColors, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("متوسط التقييمات ⭐", "%.1f من 5".format(stats.averageRating), Color(0xFFFFA000), themeColors, Modifier.weight(1f))
            StatCard("مشاهدات الملف 👁️", "${stats.totalViews} مشاهدة", Color(0xFF8B5CF6), themeColors, Modifier.weight(1f))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "💰 الإيرادات التقديرية", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text(text = "${stats.totalRevenueYer} ريال يمني", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "يتم احتساب الإيرادات التقديرية بناءً على الطلبات المكتملة بنجاح.", fontSize = 11.sp, color = themeColors.textSecondary)
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    badgeColor: Color,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeColor)
            }
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.textPrimary)
        }
    }
}

package com.serviseyem.ui.screens.status

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.repositories.SystemStatusMetrics
import com.serviseyem.utils.VisualThemePalette

/**
 * 📊 StatusOverviewContent
 * Detailed system performance metrics layout and stats overview.
 */
@Composable
fun StatusOverviewContent(
    metrics: SystemStatusMetrics,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "📈 ملخص أداء النظام والبيانات الحية",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.textPrimary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusMetricCard(
                    title = "الفنيين والمهنيين",
                    value = "${metrics.providersCount}",
                    icon = "👨‍🔧",
                    color = Color(0xFF3B82F6),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
                StatusMetricCard(
                    title = "المتاجر والمراكز",
                    value = "${metrics.storesCount}",
                    icon = "🏬",
                    color = Color(0xFF10B981),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusMetricCard(
                    title = "العقارات والمعارض",
                    value = "${metrics.propertiesCount}",
                    icon = "🏠",
                    color = Color(0xFFF59E0B),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
                StatusMetricCard(
                    title = "الطلبات العاجلة",
                    value = "${metrics.instantRequestsCount}",
                    icon = "⚡",
                    color = Color(0xFFEF4444),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusMetricCard(
                    title = "الحجوزات المؤكدة",
                    value = "${metrics.bookingsCount}",
                    icon = "📋",
                    color = Color(0xFF8B5CF6),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
                StatusMetricCard(
                    title = "طلبات الانضمام المعلقة",
                    value = "${metrics.pendingJoinRequestsCount}",
                    icon = "📝",
                    color = Color(0xFFEC4899),
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

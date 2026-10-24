package com.serviseyem.ui.screens.notifications.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 🏷️ NotificationFilterTabs
 * Status tabs & dynamic category filter chips for Notifications
 */
@Composable
fun NotificationFilterTabs(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    tabCounts: List<Triple<String, String, Int>>,
    selectedTypeFilter: String,
    onTypeFilterSelected: (String) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Status Tabs (ALL, UNREAD, IMPORTANT, READ)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabCounts.forEach { t ->
                val isSel = activeTab == t.first
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) themeColors.accent else Color.Transparent)
                        .clickable { onTabSelected(t.first) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${t.second} (${t.third})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color(0xFF0F172A) else Color.White
                    )
                }
            }
        }

        // Quick Category Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filterChips = listOf(
                "ALL" to "جميع التنبيهات",
                "BOOKING" to "📅 الحجوزات",
                "MESSAGE" to "💬 المحادثات",
                "SPECIAL_OFFER" to "🔥 العروض والتخفيضات",
                "SYSTEM" to "⚙️ تنبيهات النظام"
            )
            filterChips.forEach { (typeKey, typeLabel) ->
                val isChipSelected = selectedTypeFilter == typeKey
                FilterChip(
                    selected = isChipSelected,
                    onClick = { onTypeFilterSelected(typeKey) },
                    label = {
                        Text(
                            typeLabel,
                            fontSize = 10.5.sp,
                            fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = themeColors.accent.copy(alpha = 0.25f),
                        selectedLabelColor = themeColors.accent,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFCBD5E1)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isChipSelected,
                        borderColor = if (isChipSelected) themeColors.accent else Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

package com.serviseyem.ui.screens.notifications.components

import androidx.compose.foundation.horizontalScroll
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 🏷️ NotificationsFilterBar
 * Horizontal scrollable filter chips for notification categories.
 */
@Composable
fun NotificationsFilterBar(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    countsMap: Map<String, Int> = emptyMap(),
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val filterItems = listOf(
        "ALL" to "الكل",
        "UNREAD" to "غير المقروءة",
        "BOOKING" to "الحجوزات",
        "MESSAGE" to "الرسائل",
        "SPECIAL_OFFER" to "العروض",
        "SYSTEM" to "النظام"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filterItems.forEach { (key, label) ->
            val isSelected = selectedCategory == key
            val count = countsMap[key] ?: 0

            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(key) },
                label = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(label, fontSize = 12.sp)
                        if (count > 0) {
                            Text(
                                text = "($count)",
                                fontSize = 11.sp,
                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00E5FF),
                    selectedLabelColor = Color(0xFF0F172A),
                    containerColor = Color(0xFF1E293B),
                    labelColor = Color(0xFF94A3B8)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155),
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

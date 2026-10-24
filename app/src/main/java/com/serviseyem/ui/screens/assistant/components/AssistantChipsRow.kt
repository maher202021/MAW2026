package com.serviseyem.ui.screens.assistant.components

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 🏷️ AssistantChipsRow
 * Row of quick action chips for service request, stores, restaurants, medical, and map.
 */
@Composable
fun AssistantChipsRow(
    themeColors: VisualThemePalette,
    onRequestQuickService: () -> Unit,
    onNavigateTechnicians: () -> Unit,
    onNavigateStores: () -> Unit,
    onNavigateRestaurants: () -> Unit,
    onNavigateMedical: () -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = themeColors.surface.copy(alpha = 0.8f),
        modifier = modifier.fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                AssistChip(
                    onClick = onRequestQuickService,
                    label = { Text("⚡ اطلب خدمتك الآن", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFEF4444)),
                    border = null,
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                AssistChip(
                    onClick = onNavigateTechnicians,
                    label = { Text("🔧 قائمة الفنيين والمهنيين", fontSize = 10.5.sp, color = themeColors.accent, fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                AssistChip(
                    onClick = onNavigateStores,
                    label = { Text("🏬 المتاجر والمراكز", fontSize = 10.5.sp, color = themeColors.textPrimary) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = themeColors.surface),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                AssistChip(
                    onClick = onNavigateRestaurants,
                    label = { Text("🍽️ المطاعم والكافيهات", fontSize = 10.5.sp, color = themeColors.textPrimary) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = themeColors.surface),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                AssistChip(
                    onClick = onNavigateMedical,
                    label = { Text("🏥 المراكز الطبية", fontSize = 10.5.sp, color = themeColors.textPrimary) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = themeColors.surface),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                AssistChip(
                    onClick = onNavigateToMap,
                    label = { Text("📍 خريطة الخدمات", fontSize = 10.5.sp, color = themeColors.textPrimary) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = themeColors.surface),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

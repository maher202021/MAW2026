package com.serviseyem.ui.screens.admin.subpanels

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class AdminTabItem(
    val title: String,
    val icon: String,
    val tag: String
)

/**
 * 🧭 Admin Horizontal Scrollable Tab Bar
 */
@Composable
fun AdminTabBar(
    tabs: List<AdminTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(tabs) { index, tab ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) themeColors.accent else Color(0xFF1E293B))
                    .border(
                        1.dp,
                        if (isSelected) themeColors.accent else Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(tab.icon, fontSize = 13.sp)
                    Text(
                        text = tab.title,
                        fontSize = 11.5.sp,
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

package com.serviseyem.ui.screens.register.components

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.domain.entities.CategoryOption

/**
 * 🏷️ RegistrationCategoryChips - شارات اختيار التصنيف الموحدة
 */
@Composable
fun RegistrationCategoryChips(
    title: String = "اختر القسم والتخصص",
    categories: List<CategoryOption>,
    selectedId: String,
    onCategorySelected: (String) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories, key = { it.id }) { item ->
                val isSelected = item.id == selectedId
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(item.id) },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = themeColors.accent,
                        containerColor = Color(0xFF1E293B)
                    )
                )
            }
        }
    }
}

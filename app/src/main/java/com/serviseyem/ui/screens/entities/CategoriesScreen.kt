package com.serviseyem.ui.screens.entities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.Category21Item
import com.serviseyem.ui.components.CategoryDataRepository
import com.serviseyem.ui.theme.*
import com.serviseyem.utils.VisualThemePalette

@Composable
fun CategoriesScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onCategoryClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val categoriesList = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            CategoryDataRepository.ALL_21_CATEGORIES
        } else {
            CategoryDataRepository.ALL_21_CATEGORIES.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkLuxuryBackground)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search Header Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث في الـ 21 قسم المتاحة...", color = TextSecondarySlate, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = GoldGradientStart) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardSurface,
                unfocusedContainerColor = CardSurface,
                focusedBorderColor = GoldGradientStart,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimaryWhite,
                unfocusedTextColor = TextPrimaryWhite
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📁 دليل الأقسام الـ21 الشامل باليمن",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryWhite
            )
            Text(
                text = "${categoriesList.size} قسم",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GoldGradientStart
            )
        }

        // 3-Column Responsive Grid for 21 Categories
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("categories_grid_21")
        ) {
            items(categoriesList, key = { it.id }) { cat ->
                LuxuryCategoryCard(
                    category = cat,
                    onClick = { onCategoryClick(cat.id) }
                )
            }
        }
    }
}

@Composable
fun LuxuryCategoryCard(
    category: Category21Item,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, category.color.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() }
            .testTag("category_card_${category.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(category.color.copy(alpha = 0.15f))
                    .border(1.dp, category.color.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.emoji,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = category.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryWhite,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${category.count} خدمة",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondarySlate,
                textAlign = TextAlign.Center
            )
        }
    }
}


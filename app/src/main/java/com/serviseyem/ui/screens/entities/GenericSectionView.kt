package com.serviseyem.ui.screens.entities

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import com.serviseyem.data.CityEntity
import com.serviseyem.ui.components.SkeletonCard
import com.serviseyem.utils.VisualThemePalette

/**
 * ♻️ GenericSectionView (Component 10/10 Architecture)
 * A generic and highly reusable component that handles layout, filtering, skeleton loading,
 * pagination (10 items per page with load more), and empty states for all entity screens.
 */
@Composable
fun <T : Any> GenericSectionView(
    themeColors: VisualThemePalette,
    items: List<T>,
    isLoading: Boolean,
    title: String,
    titleIcon: String,
    searchPlaceholder: String,
    categories: List<String>,
    cities: List<CityEntity>,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onCitySelected: (String) -> Unit,
    onMinRatingSelected: (Float) -> Unit,
    emptyMessage: String,
    extraHeaderContent: @Composable (() -> Unit)? = null,
    pageSize: Int = 10,
    itemContent: @Composable (T) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("الكل") }
    var selectedCityId by rememberSaveable { mutableStateOf("الكل") }
    var selectedMinRating by rememberSaveable { mutableStateOf(0.0f) }
    var visibleItemCount by rememberSaveable { mutableIntStateOf(pageSize) }

    // Reset pagination when items or filters change
    LaunchedEffect(items.size, selectedCategory, selectedCityId, selectedMinRating, searchQuery) {
        visibleItemCount = pageSize
    }

    val paginatedItems = remember(items, visibleItemCount) {
        items.take(visibleItemCount)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 🔍 Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearchQueryChanged(it)
            },
            placeholder = { Text(searchPlaceholder, fontSize = 11.sp, color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = themeColors.accent,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                focusedContainerColor = themeColors.surface,
                unfocusedContainerColor = themeColors.surface
            )
        )

        // 🏙️ City Filter
        Text("🏙️ اختر المدينة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCityId == "الكل",
                    onClick = { 
                        selectedCityId = "الكل" 
                        onCitySelected("الكل")
                    },
                    label = { Text("كل المدن 🇾🇪", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = themeColors.accent,
                        selectedLabelColor = Color.Black
                    )
                )
            }
            items(cities) { city ->
                FilterChip(
                    selected = selectedCityId == city.id,
                    onClick = { 
                        selectedCityId = city.id 
                        onCitySelected(city.id)
                    },
                    label = { Text("${city.icon} ${city.nameAr}", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = themeColors.accent,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        // ⭐ Rating Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("⭐ التقييم الأدنى:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
            listOf(0.0f, 3.0f, 4.0f, 4.5f).forEach { stars ->
                FilterChip(
                    selected = selectedMinRating == stars,
                    onClick = { 
                        selectedMinRating = stars 
                        onMinRatingSelected(stars)
                    },
                    label = { Text(if (stars == 0.0f) "الكل" else "⭐ $stars+", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = themeColors.accent,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        extraHeaderContent?.invoke()

        // 🏷️ Category Filter
        if (categories.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        color = if (isSelected) themeColors.accent else themeColors.surface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (isSelected) themeColors.accent else Color.Gray.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { 
                            selectedCategory = cat 
                            onCategorySelected(cat)
                        }
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$titleIcon $title:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Text("${paginatedItems.size} من ${items.size}", fontSize = 11.sp, color = Color.LightGray)
        }

        // 📄 Content Area
        if (isLoading) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(6) {
                    SkeletonCard(height = 140.dp)
                }
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(emptyMessage, color = Color.LightGray, fontSize = 14.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(paginatedItems) { item ->
                    itemContent(item)
                }

                if (visibleItemCount < items.size) {
                    item(span = { GridItemSpan(2) }) {
                        Button(
                            onClick = { visibleItemCount += pageSize },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, themeColors.accent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "تحميل المزيد (${items.size - visibleItemCount} متبقي) ⬇️",
                                color = themeColors.accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 🏠 [UnifiedEntitySectionView] - مكون عام لعرض قوائم الكيانات
 */
@Composable
fun <T : Any> UnifiedEntitySectionView(
    title: String,
    titleIcon: String,
    items: List<T>,
    isLoading: Boolean,
    themeColors: com.serviseyem.utils.VisualThemePalette,
    searchPlaceholder: String,
    categories: List<String> = emptyList(),
    cities: List<com.serviseyem.data.CityEntity> = emptyList(),
    onItemClick: (T) -> Unit,
    onAddClick: (() -> Unit)? = null,
    onSearchQueryChanged: (String) -> Unit = {},
    onCategorySelected: (String) -> Unit = {},
    onCitySelected: (String) -> Unit = {},
    emptyMessage: String = "لا توجد عناصر معروضة حالياً",
    itemContent: @Composable (T) -> Unit
) {
    GenericSectionView(
        themeColors = themeColors,
        items = items,
        isLoading = isLoading,
        title = title,
        titleIcon = titleIcon,
        searchPlaceholder = searchPlaceholder,
        categories = categories,
        cities = cities,
        onSearchQueryChanged = onSearchQueryChanged,
        onCategorySelected = onCategorySelected,
        onCitySelected = onCitySelected,
        onMinRatingSelected = { /* تنفيذ التصفية حسب التقييم */ },
        emptyMessage = emptyMessage,
        extraHeaderContent = {
            if (onAddClick != null) {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Text("➕ إضافة جديد", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        },
        itemContent = itemContent
    )
}

@Composable
fun RetryableErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    themeColors: com.serviseyem.utils.VisualThemePalette,
    maxRetries: Int = 3
) {
    var retryCount by remember { mutableStateOf(0) }
    var isAutoRetrying by remember { mutableStateOf(false) }
    
    LaunchedEffect(retryCount) {
        if (retryCount in 1..maxRetries) {
            kotlinx.coroutines.delay(3000L)
            isAutoRetrying = true
            onRetry()
            isAutoRetrying = false
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(56.dp)
            )
            
            Text(
                text = "⚠️ حدث خطأ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = errorMessage,
                fontSize = 13.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Button(
                onClick = {
                    if (retryCount < maxRetries) retryCount++ else retryCount = 1
                    onRetry()
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isAutoRetrying) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("جاري إعادة المحاولة...", color = Color.Black)
                } else {
                    Text("إعادة المحاولة 🔄", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            
            if (retryCount >= maxRetries) {
                Text(
                    text = "💡 تأكد من اتصالك بالإنترنت وحاول مرة أخرى",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}



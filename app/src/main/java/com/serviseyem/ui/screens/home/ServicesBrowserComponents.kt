package com.serviseyem.ui.screens.home

import android.content.Intent
import com.serviseyem.ui.*
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.CategoryEntity
import com.serviseyem.data.CityEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.ui.components.ProviderListSkeleton
import com.serviseyem.utils.VisualThemePalette

@Composable
fun ServicesSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isFilterActive: Boolean,
    onFilterClick: () -> Unit,
    isSpeechSearchEnabled: Boolean,
    onVoiceClick: () -> Unit,
    themeColors: VisualThemePalette
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isFilterActive) Color(0xFFD4AF37) else Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "بحث",
                tint = Color(0xFF0D5C46),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    maxLines = 1,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF0F172A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_input"),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "ابحث عن فني، متجر، خدمة، عقار...",
                                fontSize = 11.5.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                )
            }

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color.Gray, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(contentAlignment = Alignment.TopEnd) {
                Surface(
                    onClick = onFilterClick,
                    shape = RoundedCornerShape(8.dp),
                    color = if (isFilterActive) Color(0xFF0D5C46) else Color(0xFFF1F5F9),
                    border = BorderStroke(0.7.dp, if (isFilterActive) Color(0xFFD4AF37) else Color(0xFFE2E8F0)),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "فلترة وبحث متقدم",
                            tint = if (isFilterActive) Color.White else Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (isFilterActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                }
            }

            if (isSpeechSearchEnabled) {
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "بحث صوتي",
                        tint = Color(0xFF0D5C46),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesFilterBottomSheet(
    onDismissRequest: () -> Unit,
    phoneOrNameFilter: String,
    onPhoneOrNameFilterChange: (String) -> Unit,
    activeCityId: String?,
    onCityIdFilterChange: (String?) -> Unit,
    citiesList: List<CityEntity>,
    neighborFilter: String,
    onNeighborFilterChange: (String) -> Unit,
    radiusKm: Int,
    onRadiusKmChange: (Int) -> Unit,
    isVipOnly: Boolean,
    onVipOnlyChange: () -> Unit,
    isAvailableOnly: Boolean,
    onAvailableOnlyChange: () -> Unit,
    filterByCurrentCityOnly: Boolean,
    onFilterByCurrentCityOnlyChange: (Boolean) -> Unit,
    currentUserResidence: String,
    maxSearchRadiusKm: Int,
    onResetFilters: () -> Unit,
    themeColors: VisualThemePalette
) {
    var isCityDropdownOpen by remember { mutableStateOf(false) }
    var citySearchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = themeColors.accent, modifier = Modifier.size(18.dp))
                    Text("معايير التصفية والبحث المتقدم", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                IconButton(onClick = onDismissRequest, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "إغلاق", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

            OutlinedTextField(
                value = phoneOrNameFilter,
                onValueChange = onPhoneOrNameFilterChange,
                placeholder = { Text("البحث بالاسم أو رقم الهاتف...", fontSize = 11.sp, color = themeColors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = themeColors.accent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B)
                ),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("المدينة اليمنية:", fontSize = 10.sp, color = themeColors.textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val selectedCityName = citiesList.firstOrNull { it.id == activeCityId }?.nameAr ?: (if (activeCityId.isNullOrEmpty()) "كل المدن" else activeCityId ?: "كل المدن")
                        Box(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, themeColors.accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { isCityDropdownOpen = true }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = selectedCityName,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(Icons.Default.ArrowDropDown, null, tint = themeColors.accent, modifier = Modifier.size(18.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = isCityDropdownOpen,
                            onDismissRequest = { isCityDropdownOpen = false },
                            modifier = Modifier
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, themeColors.accent, RoundedCornerShape(8.dp))
                                    .width(220.dp)
                                    .heightIn(max = 280.dp)
                        ) {
                            OutlinedTextField(
                                value = citySearchQuery,
                                onValueChange = { 
                                    citySearchQuery = it
                                    if (it.isNotBlank()) {
                                        onCityIdFilterChange(it)
                                    }
                                },
                                placeholder = { Text("اكتب اسم المدينة...", fontSize = 10.sp, color = Color.Gray) },
                                singleLine = true,
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF1E293B),
                                    unfocusedContainerColor = Color(0xFF1E293B)
                                )
                            )

                            DropdownMenuItem(
                                text = { Text("🌍 كل المدن", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    onCityIdFilterChange(null)
                                    citySearchQuery = ""
                                    isCityDropdownOpen = false
                                }
                            )

                            val baseCities = listOf(
                                "صنعاء", "عدن", "تعز", "الحديدة", "إب", "حضرموت", "المكلا", "سيئون",
                                "مأرب", "ذمار", "حجة", "صعدة", "أبين", "لحج", "شبوة", "المهرة",
                                "عمران", "البيضاء", "الضالع", "ريمة", "المحويت", "سقطرى"
                            )
                            val dynamicCityNames = (citiesList.map { it.nameAr } + baseCities).distinct()
                            val filteredCityNames = if (citySearchQuery.isBlank()) dynamicCityNames else dynamicCityNames.filter { it.contains(citySearchQuery, ignoreCase = true) }

                            filteredCityNames.forEach { cityName ->
                                DropdownMenuItem(
                                    text = { Text("📍 $cityName", color = Color.White, fontSize = 11.sp) },
                                    onClick = {
                                        val matchingId = citiesList.firstOrNull { it.nameAr == cityName }?.id ?: cityName
                                        onCityIdFilterChange(matchingId)
                                        citySearchQuery = ""
                                        isCityDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("المنطقة / الحي:", fontSize = 10.sp, color = themeColors.textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = neighborFilter,
                        onValueChange = { if (it.length <= 50) onNeighborFilterChange(it) },
                        placeholder = { Text("مثال: حدة، الحصبة...", fontSize = 11.sp, color = themeColors.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = themeColors.accent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("نطاق البحث الجغرافي:", fontSize = 11.sp, color = Color.White)
                    Text("$radiusKm كم", fontSize = 11.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = radiusKm.toFloat(),
                    onValueChange = { onRadiusKmChange(it.toInt().coerceAtMost(maxSearchRadiusKm)) },
                    valueRange = 5f..50f,
                    steps = 5,
                    colors = SliderDefaults.colors(thumbColor = themeColors.accent, activeTrackColor = themeColors.accent)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isVipOnly,
                        onCheckedChange = { onVipOnlyChange() },
                        colors = CheckboxDefaults.colors(checkedColor = themeColors.accent)
                    )
                    Text("الذهبيون VIP", fontSize = 11.sp, color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isAvailableOnly,
                        onCheckedChange = { onAvailableOnlyChange() },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981))
                    )
                    Text("المتاحون الآن ⚡", fontSize = 11.sp, color = Color.White)
                }
            }

            Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = filterByCurrentCityOnly,
                    onCheckedChange = onFilterByCurrentCityOnlyChange,
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981))
                )
                Text(
                    text = if (currentUserResidence.isNotBlank()) "تصفية حسب مدينتي ($currentUserResidence)" else "تصفية عروض مدينتي القريبة",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onResetFilters,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("إعادة ضبط 🔄", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تطبيق الفلاتر ✅", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun ServicesCityTabs(
    activeCityId: String?,
    onCityClick: (String?) -> Unit,
    citiesList: List<CityEntity>,
    themeColors: VisualThemePalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "🌍 اختر المدينة لعرض الخدمات المحلية:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.textSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isAllSelected = activeCityId == null
            Surface(
                modifier = Modifier
                        .clickable { onCityClick(null) }
                        .testTag("city_tab_all"),
                shape = RoundedCornerShape(20.dp),
                color = if (isAllSelected) themeColors.accent else themeColors.surface,
                border = BorderStroke(1.dp, if (isAllSelected) Color.Transparent else themeColors.accent.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "🌍 كل المدن",
                    color = if (isAllSelected) Color.Black else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            citiesList.forEach { city ->
                val isSelected = activeCityId == city.id
                Surface(
                    modifier = Modifier
                            .clickable { onCityClick(city.id) }
                            .testTag("city_tab_${city.id}"),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) themeColors.accent else themeColors.surface,
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else themeColors.accent.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = city.nameAr,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ServicesCategoryChips(
    categories: List<CategoryEntity>,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit,
    themeColors: VisualThemePalette,
    onTabChange: (String) -> Unit,
    storesTabName: String,
    propertiesTabName: String
) {
    val mainCats = remember(categories) {
        categories.filter { it.isMainCategory || it.parentId.isNullOrEmpty() }
    }
    val displayCats = if (mainCats.isNotEmpty()) mainCats else categories

    val activeSubCategories = remember(selectedCategory, categories) {
        if (selectedCategory != null) {
            categories.filter { it.parentId == selectedCategory }
        } else {
            emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📂 الأقسام والتخصصات الرئيسية:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (selectedCategory != null) {
                Text(
                    text = "عرض الكل 🔄",
                    fontSize = 10.sp,
                    color = themeColors.accent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onCategorySelect(null) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        if (selectedCategory == null) {
            val chunkedCats = remember(displayCats) { displayCats.chunked(2) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                chunkedCats.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { cat ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (cat.id == "stores" || cat.parentId == "stores" || cat.id == "restaurants") {
                                            onTabChange(storesTabName)
                                            onCategorySelect(cat.id)
                                        } else if (cat.id == "realestate" || cat.parentId == "realestate" || cat.id == "jobs") {
                                            onTabChange(propertiesTabName)
                                            onCategorySelect(cat.id)
                                        } else {
                                            onTabChange("الرئيسية")
                                            onCategorySelect(cat.id)
                                        }
                                    }
                                    .testTag("category_grid_${cat.id}"),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF112211), // Elegant deep emerald background
                                border = BorderStroke(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700), // Gold
                                            Color(0xFF10B981)  // Emerald
                                        )
                                    )
                                ),
                                shadowElevation = 6.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // 3D-styled Icon Container
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF1A3322),
                                        border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                                        shadowElevation = 4.dp,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = cat.icon,
                                                fontSize = 20.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    Column(
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = cat.name,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        val subCount = categories.count { it.parentId == cat.id }
                                        Text(
                                            text = if (subCount > 0) "📌 $subCount أقسام" else "💼 دليل الخدمة",
                                            color = Color(0xFF10B981), // Emerald Accent
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        // Handle odd number of items by adding an empty spacer
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isAllSelected = selectedCategory == null
                Surface(
                    modifier = Modifier
                            .clickable { onCategorySelect(null) }
                            .testTag("category_chip_all"),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAllSelected) themeColors.accent else themeColors.surface,
                    border = BorderStroke(1.dp, if (isAllSelected) Color.Transparent else themeColors.accent.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "✨ الكل",
                        color = if (isAllSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                displayCats.forEach { cat ->
                    val isSelected = selectedCategory == cat.id
                    val hasSub = categories.any { it.parentId == cat.id }
                    Surface(
                        modifier = Modifier
                                .clickable {
                                    if (cat.id == "stores" || cat.parentId == "stores" || cat.id == "restaurants") {
                                        onTabChange(storesTabName)
                                        onCategorySelect(cat.id)
                                    } else if (cat.id == "realestate" || cat.parentId == "realestate" || cat.id == "jobs") {
                                        onTabChange(propertiesTabName)
                                        onCategorySelect(cat.id)
                                    } else {
                                        onTabChange("الرئيسية")
                                        if (isSelected) onCategorySelect(null)
                                        else onCategorySelect(cat.id)
                                    }
                                }
                                .testTag("category_chip_${cat.id}"),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) themeColors.accent else themeColors.surface,
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else themeColors.accent.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(cat.icon, fontSize = 12.sp)
                            Text(
                                text = cat.name,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (hasSub) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (activeSubCategories.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.7f),
                border = BorderStroke(0.6.dp, themeColors.accent.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "الخدمات الفرعية والتخصصات الدقيقة:",
                        fontSize = 10.sp,
                        color = themeColors.accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        activeSubCategories.forEach { subCat ->
                            Surface(
                                modifier = Modifier.clickable {
                                    onCategorySelect(subCat.id)
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selectedCategory == subCat.id) themeColors.accent else Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(0.6.dp, if (selectedCategory == subCat.id) Color.Transparent else Color.White.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(subCat.icon, fontSize = 10.sp)
                                    Text(
                                        text = subCat.name,
                                        color = if (selectedCategory == subCat.id) Color.Black else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

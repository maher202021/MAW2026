package com.serviseyem.ui.screens.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.utils.VisualThemePalette

/**
 * 🔍 MapFilterBar (مطابق للشاشة المرفقة)
 * شريط البحث والفلاتر المزدوج:
 * 1. شريط بحث أسود مع زر البرق البرتقالي للطلبات العاجلة
 * 2. شريط شرائح الفئات: كل الفئات، فنيين، متاجر ومحلات، مطاعم وكافيهات، مراكز طبية، عقارات
 * 3. شريط شرائح المحافظات: كل المحافظات، صنعاء، عدن، تعز، الحديدة، إب...
 */
@Composable
fun MapFilterBar(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val categories = remember {
        listOf(
            Triple("ALL", "كل الفئات 🌐", "🌐"),
            Triple("PROVIDERS", "فنيين 👷", "👷"),
            Triple("STORES", "متاجر ومحلات 🏪", "🏪"),
            Triple("RESTAURANTS", "مطاعم وكافيهات 🍔", "🍔"),
            Triple("MEDICAL", "مراكز طبية 🏥", "🏥"),
            Triple("PROPERTIES", "عقارات 🏠", "🏠")
        )
    }

    val governorates = remember {
        listOf("الكل", "صنعاء", "عدن", "تعز", "الحديدة", "إب", "حضرموت (المكلا)", "مأرب", "ذمار")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Search Bar with Quick Action Lightning Button
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "ابحث عن فني، متجر، مطعم، صيدلية، عقار...",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    // [تعديل] تحديد لون وحجم النص المكتوب صراحة ليظهر باللون الأبيض الناصع
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF00E5FF)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("map_search_input")
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                // [تعديل] حذف الزر الأحمر (البرق) نهائياً بناءً على طلب المستخدم
            }
        }

        // 2. Category Filter Chips (Row 1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { (catKey, catLabel, _) ->
                val isSelected = selectedCategory == catKey
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF60A5FA) else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("filter_chip_$catKey")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.text.ClickableText(
                            text = androidx.compose.ui.text.AnnotatedString(catLabel),
                            style = androidx.compose.ui.text.TextStyle(
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            onClick = { onCategorySelected(catKey) }
                        )
                    }
                }
            }
        }

        // 3. Governorate Selector Chips (Row 2 - كالصورة المرفقة)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            governorates.forEach { gov ->
                val isSelected = (gov == "الكل" && (selectedCity == "الكل" || selectedCity.isBlank())) ||
                        selectedCity == gov
                val label = if (gov == "الكل") "كل المحافظات 📍" else gov

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) Color(0xFF0F766E) else Color(0xFF1E293B),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF14B8A6) else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("city_chip_$gov")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.text.ClickableText(
                            text = androidx.compose.ui.text.AnnotatedString(label),
                            style = androidx.compose.ui.text.TextStyle(
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            onClick = { onCitySelected(gov) }
                        )
                    }
                }
            }
        }
    }
}

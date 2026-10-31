@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.serviseyem.ui.screens.map.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.serviseyem.utils.VisualThemePalette

/**
 * 🔍 MapFilterBar (Top Search Bar Capsule)
 * Glassmorphism capsule pinned at top center:
 * - Search icon + Voice assistant + Placeholder "ابحث عن شارع، متجر، جولة، أو تقاطع..." + Hidden Filter Icon
 * - Categories & Governorates appear only upon clicking the filter icon as a sleek popup menu that closes upon selection.
 * - Absolutely NOTHING rendered underneath the capsule to maintain 90% clean map view.
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
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onVoiceSearchClick: () -> Unit = {},
    onSearchExecute: (String) -> Unit = {}
) {
    var showFilterDialog by remember { mutableStateOf(false) }

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

    val isFilterActive = selectedCategory != "ALL" || selectedCity != "الكل"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Single Top Glassmorphism Capsule
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0B0F19).copy(alpha = 0.85f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Back Button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("map_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Search Icon
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "بحث",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )

                // Search Text Input
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "ابحث عن شارع، متجر، جولة، أو تقاطع...",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF10B981)
                    ),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = { onSearchExecute(searchQuery) }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("map_search_input")
                )

                // Clear button when query not empty
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Voice Assistant Button
                IconButton(
                    onClick = onVoiceSearchClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("map_voice_assistant_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "مساعد صوتي",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Filter Icon Button (Toggles hidden categories & cities menu)
                IconButton(
                    onClick = { showFilterDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("map_filter_toggle_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "تصفية الفئات والمحافظات",
                            tint = if (isFilterActive) Color(0xFF10B981) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        if (isFilterActive) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        }
    }

    // Hidden Filter Dropdown / Dialog (Appears only when tapping the filter icon)
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            containerColor = Color(0xFF0B0F19),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "تصفية الخريطة والخدمات",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(
                        onClick = { showFilterDialog = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Categories
                    Text(
                        "اختر الفئة:",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { (catKey, catLabel, _) ->
                            val isSelected = selectedCategory == catKey
                            Surface(
                                onClick = {
                                    onCategorySelected(catKey)
                                    showFilterDialog = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.8f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF10B981) else Color(0xFF334155)
                                )
                            ) {
                                Text(
                                    text = catLabel,
                                    color = if (isSelected) Color(0xFF10B981) else Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // 2. Governorates
                    Text(
                        "المحافظة أو المدينة:",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        governorates.forEach { gov ->
                            val isSelected = selectedCity == gov
                            Surface(
                                onClick = {
                                    onCitySelected(gov)
                                    showFilterDialog = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.8f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF10B981) else Color(0xFF334155)
                                )
                            ) {
                                Text(
                                    text = gov,
                                    color = if (isSelected) Color(0xFF10B981) else Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    if (isFilterActive) {
                        TextButton(
                            onClick = {
                                onCategorySelected("ALL")
                                onCitySelected("الكل")
                                showFilterDialog = false
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("إعادة ضبط الفلاتر", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

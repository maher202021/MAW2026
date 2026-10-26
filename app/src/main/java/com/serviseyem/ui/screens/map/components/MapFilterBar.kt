@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.serviseyem.ui.screens.map.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.MapMode
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.utils.CrossFuzzySearchEngine
import com.serviseyem.utils.VisualThemePalette
import java.util.Locale

@Composable
fun MapFilterBar(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onResultSelected: (CrossFuzzySearchEngine.SearchResult) -> Unit = {},
    providers: List<ProviderEntity> = emptyList(),
    stores: List<StoreEntity> = emptyList(),
    properties: List<PropertyEntity> = emptyList(),
    onBackClick: () -> Unit = {},
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    var showFiltersSheet by remember { mutableStateOf(false) }
    var isSearchFocused by remember { mutableStateOf(false) }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val smartResults = remember(searchQuery, providers, stores, properties) {
        if (searchQuery.isNotBlank()) {
            CrossFuzzySearchEngine.search(searchQuery, providers, stores, properties)
        } else emptyList()
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            data?.firstOrNull()?.let { onSearchQueryChange(it) }
        }
    }

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

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF1E293B).copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onBackClick() 
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "ابحث عن شارع، محل، فني، أو شخص...",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White, 
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Start
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFF10B981)
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { isSearchFocused = false }
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "مسح",
                                        tint = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("map_search_input")
                            .onFocusChanged { isSearchFocused = it.isFocused }
                    )

                    // Microphone Icon (Arabic Voice Search)
                    IconButton(onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن للبحث في اليمن...")
                        }
                        try { voiceLauncher.launch(intent) } catch (e: Exception) {}
                    }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "بحث صوتي",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(20.dp).padding(horizontal = 4.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    )

                    IconButton(onClick = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        showFiltersSheet = true 
                    }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "تصفية",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 🧠 Smart Autocomplete Results (Floating Layer)
        AnimatedVisibility(
            visible = (smartResults.isNotEmpty() || (searchQuery.length > 2 && isSearchFocused)),
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .heightIn(max = 400.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0B0F19).copy(alpha = 0.98f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                shadowElevation = 20.dp
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    if (smartResults.isEmpty() && searchQuery.length >= 2) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "لا توجد نتائج مطابقة لـ \"$searchQuery\"",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    items(smartResults) { res ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    onResultSelected(res)
                                    isSearchFocused = false
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val icon = when(res.type) {
                                    CrossFuzzySearchEngine.SearchType.CITY -> "🇾🇪"
                                    CrossFuzzySearchEngine.SearchType.AREA -> "📍"
                                    CrossFuzzySearchEngine.SearchType.STREET -> "🛣️"
                                    CrossFuzzySearchEngine.SearchType.PROVIDER -> "👷"
                                    CrossFuzzySearchEngine.SearchType.PERSON -> "👤"
                                    CrossFuzzySearchEngine.SearchType.STORE -> "🏪"
                                    CrossFuzzySearchEngine.SearchType.PROPERTY -> "🏠"
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.05f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(icon, fontSize = 18.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(14.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = res.title,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = res.subtitle,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.ArrowOutward,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981).copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.06f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showFiltersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFiltersSheet = false },
            containerColor = Color(0xFF0F172A),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "تصفية وبحث ذكي 🔍",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text("الفئات المتاحة", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categories.forEach { (catKey, catLabel, _) ->
                        val isSelected = selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onCategorySelected(catKey) 
                            },
                            label = { Text(catLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.05f),
                                selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                labelColor = Color.White.copy(alpha = 0.8f),
                                selectedLabelColor = Color(0xFF10B981)
                            )
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.1f))

                Text("الموقع الجغرافي (المحافظة)", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    governorates.forEach { gov ->
                        val isSelected = (gov == "الكل" && (selectedCity == "الكل" || selectedCity.isBlank())) ||
                                selectedCity == gov
                        val label = if (gov == "الكل") "كل اليمن 🇾🇪" else gov
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onCitySelected(gov) 
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.05f),
                                selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                labelColor = Color.White.copy(alpha = 0.8f),
                                selectedLabelColor = Color(0xFF10B981)
                            )
                        )
                    }
                }

                Button(
                    onClick = { showFiltersSheet = false },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("عرض النتائج المحسنة", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

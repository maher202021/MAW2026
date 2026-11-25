@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.serviseyem.ui.screens.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    userLat: Double = 0.0,
    userLng: Double = 0.0,
    onBackClick: () -> Unit = {},
    focusTick: Int = 0,
    onVoiceSearchClick: () -> Unit = {},
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showFiltersSheet by remember { mutableStateOf(false) }
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(focusTick) {
        if (focusTick > 0) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    var smartResults by remember { mutableStateOf<List<CrossFuzzySearchEngine.SearchResult>>(emptyList()) }
    var isSearchingOnline by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery, providers, stores, properties, userLat, userLng) {
        val q = searchQuery.trim()
        if (q.isBlank()) {
            smartResults = emptyList()
            isSearchingOnline = false
            return@LaunchedEffect
        }

        // المرحلة 1: نتائج فورية من محرك البحث الضبابي المحلي والمعالم اليمنية
        val localImmediate = CrossFuzzySearchEngine.searchEverythingAsync(
            query = q,
            providers = providers,
            stores = stores,
            properties = properties,
            userLat = userLat,
            userLng = userLng,
            context = context
        )
        smartResults = localImmediate

        if (q.length < 2) {
            isSearchingOnline = false
            return@LaunchedEffect
        }

        // المرحلة 2: مهلة قصيرة ثم بحث متوازٍ في قاعدة الأماكن وOSM (جولات، صرافات، مساجد، مدارس، شوارع)
        kotlinx.coroutines.delay(180)
        isSearchingOnline = true
        try {
            val outcome = com.serviseyem.utils.PlaceSearch.search(
                context = context,
                query = q,
                providers = providers,
                stores = stores,
                userLat = userLat,
                userLng = userLng
            )
            if (outcome is com.serviseyem.utils.PlaceSearchOutcome.Results) {
                val merged = LinkedHashMap<String, CrossFuzzySearchEngine.SearchResult>()
                localImmediate.forEach { res ->
                    val key = "${res.title.trim()}_${String.format(Locale.US, "%.3f_%.3f", res.latitude, res.longitude)}"
                    merged.putIfAbsent(key, res)
                }
                val hasUserGps = userLat != 0.0 && userLng != 0.0
                outcome.items.forEach { item ->
                    val key = "${item.shortName.trim()}_${String.format(Locale.US, "%.3f_%.3f", item.latitude, item.longitude)}"
                    if (!merged.containsKey(key)) {
                        val distKm = if (hasUserGps) {
                            CrossFuzzySearchEngine.computeHaversineKm(userLat, userLng, item.latitude, item.longitude)
                        } else null
                        val distSuffix = distKm?.let {
                            if (it < 1.0) " • ${(it * 1000).toInt()} م" else " • ${String.format(Locale.US, "%.1f", it)} كم"
                        } ?: ""
                        val searchType = when (item.category) {
                            "city" -> CrossFuzzySearchEngine.SearchType.CITY
                            "road", "street" -> CrossFuzzySearchEngine.SearchType.STREET
                            "area" -> CrossFuzzySearchEngine.SearchType.AREA
                            else -> CrossFuzzySearchEngine.SearchType.LANDMARK
                        }
                        val landmarkObj = com.serviseyem.ui.screens.map.utils.StreetOrLandmark(
                            name = item.shortName,
                            type = item.displayName,
                            city = selectedCity.takeIf { it != "الكل" } ?: "اليمن",
                            area = item.displayName,
                            lat = item.latitude,
                            lng = item.longitude
                        )
                        merged[key] = CrossFuzzySearchEngine.SearchResult(
                            id = "hybrid_${item.shortName}_${item.latitude}_${item.longitude}",
                            title = item.shortName,
                            subtitle = "${item.icon} ${item.displayName}$distSuffix",
                            type = searchType,
                            latitude = item.latitude,
                            longitude = item.longitude,
                            originalItem = landmarkObj,
                            score = 70,
                            distanceKm = distKm
                        )
                    }
                }
                smartResults = merged.values.take(30)
            }
        } catch (_: Exception) {
        } finally {
            isSearchingOnline = false
        }
    }

    val categories = remember {
        listOf(
            Triple("ALL", "كل الخدمات 🌐", "🌐"),
            Triple("STORES", "بقالة ودكاكين 🛒", "🛒"),
            Triple("RESTAURANTS", "بوفيه ومأكولات 🍳", "🍳"),
            Triple("PROVIDERS", "ميكانيكي وبنشر 🔧", "🔧"),
            Triple("MEDICAL", "صيدلية ومستوصف 💊", "💊"),
            Triple("PROPERTIES", "عقارات وبيوت 🏠", "🏠")
        )
    }

    val governorates = remember {
        listOf("الكل", "صنعاء", "عدن", "تعز", "الحديدة", "إب", "حضرموت (المكلا)", "مأرب", "ذمار")
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            // [نظام خرائط وملاحة عالمي - ملكنا]
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF0B1220).copy(alpha = 0.94f),
                border = BorderStroke(1.dp, Color(0xFF5B86FF).copy(alpha = 0.35f)),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onBackClick() 
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isSearchingOnline) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF00E5FF)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = Color(0xFF5B86FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "ابحث عن جولة، صرافة، جامع، شارع...",
                                fontSize = 12.5.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White, 
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Start
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFF00E5FF)
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { isSearchFocused = false }
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "مسح",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .testTag("map_search_input")
                            .onFocusChanged { isSearchFocused = it.isFocused }
                    )

                    // Microphone Icon (In-App Arabic Voice Search)
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onVoiceSearchClick()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("map_voice_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "بحث صوتي",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.height(20.dp).padding(horizontal = 2.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    )

                    IconButton(
                        onClick = { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            showFiltersSheet = true 
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "تصفية",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }



        // 🧠 Smart Autocomplete Results (Floating Layer)
        AnimatedVisibility(
            visible = (smartResults.isNotEmpty() || (searchQuery.length >= 2 && isSearchFocused)),
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
                                    if (isSearchingOnline) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 2.5.dp,
                                            color = Color(0xFF10B981)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "جاري البحث في خرائط اليمن وOSM...",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
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
                                    CrossFuzzySearchEngine.SearchType.LANDMARK -> "🏛️"
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
            containerColor = Color(0xFF0B1220),
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
                                selectedContainerColor = Color(0xFF3A6BFF).copy(alpha = 0.28f),
                                labelColor = Color.White.copy(alpha = 0.8f),
                                selectedLabelColor = Color(0xFF93C5FD)
                            )
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

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
                                selectedContainerColor = Color(0xFF3A6BFF).copy(alpha = 0.28f),
                                labelColor = Color.White.copy(alpha = 0.8f),
                                selectedLabelColor = Color(0xFF93C5FD)
                            )
                        )
                    }
                }

                Button(
                    onClick = { showFiltersSheet = false },
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp).padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A6BFF)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("عرض النتائج المحسنة", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

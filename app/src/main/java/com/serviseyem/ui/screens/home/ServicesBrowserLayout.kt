@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.home

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.components.AdminCustomBannerView
import com.serviseyem.ui.components.BannerSliderView
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.ui.theme.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import com.serviseyem.ui.components.*

import com.serviseyem.data.repositories.*
import com.serviseyem.ui.screens.home.sections.*
import com.serviseyem.*
import com.serviseyem.ui.dialogs.JobApplicationDialog
import com.serviseyem.VoiceManager

/**
 * 🏠 ServicesBrowserLayout - الشاشة الرئيسية لتصفح الخدمات والمتاجر باليمن
 * مفككة ومبنية وفق معمارية Clean Architecture و MVVM النظيفة (<250 سطر)
 */
@Composable
fun ServicesBrowserLayout(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    activeSectionIdForCreation: String,
    onActiveSectionIdForCreationChange: (String) -> Unit,
    preselectedRegistrationType: String,
    onPreselectedRegistrationTypeChange: (String) -> Unit,
    onChatOpen: (String) -> Unit
) {
    val context = LocalContext.current
    val appContext = context.applicationContext

    val categories by viewModel.categories.collectAsState()
    val allProviders by viewModel.providers.collectAsState()
    val filteredProviders by viewModel.filteredProviders.collectAsState()
    val isProvidersLoading by viewModel.isProvidersLoading.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val products by viewModel.products.collectAsState()
    val isVipOnly by viewModel.filterVipOnly.collectAsState()
    val isAvailableOnly by viewModel.filterAvailableOnly.collectAsState()
    val citiesList by viewModel.cities.collectAsState()
    val activeCityId by viewModel.filterCityId.collectAsState()
    val radiusKm by viewModel.maxKmRadius.collectAsState()
    val neighborFilter by viewModel.filterNeighborhoodName.collectAsState()
    val phoneOrNameFilter by viewModel.phoneOrNameFilter.collectAsState()
    val bannersList by viewModel.banners.collectAsState()
    val settingsState by viewModel.settings.collectAsState()
    val paymentWallets by viewModel.paymentWallets.collectAsState()

    val filteredStores by viewModel.filteredStores.collectAsState()
    val filteredProperties by viewModel.filteredProperties.collectAsState()

    var showFiltersPanel by remember { mutableStateOf(false) }
    var selectedStoreForDetails by remember { mutableStateOf<StoreEntity?>(null) }
    var selectedPropertyForDetails by remember { mutableStateOf<PropertyEntity?>(null) }
    var selectedJobForDetails by remember { mutableStateOf<JobEntity?>(null) }
    var showJobApplicationForJob by remember { mutableStateOf<JobEntity?>(null) }
    var providersLimit by remember { mutableStateOf(10) }
    val activeTabName by viewModel.activeBrowserTab.collectAsState()

    val activeTabs = remember(settingsState) {
        val list = mutableListOf("الرئيسية")
        if (settingsState.isStoresEnabled) list.add("المحلات والمتاجر")
        list.add("المطاعم والكافيهات")
        list.add("المراكز الطبية")
        if (settingsState.isPropertiesEnabled) list.add("العقارات")
        list.add("إعلانات الوظائف")
        list.add("المفضلة")
        list.toList()
    }

    val activeTabBg = AppBackground

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(activeTabBg),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (activeTabName == "الرئيسية") {
            // 1. Top Custom Banner
            if (settingsState.bannerEnabled && settingsState.bannerLocation == "TOP") {
                item {
                    AdminCustomBannerView(settingsState = settingsState, themeColors = themeColors)
                }
            }

            // 2. Banner Slider
            if (bannersList.isNotEmpty()) {
                item {
                    BannerSliderView(banners = bannersList, themeColors = themeColors) { catTarget ->
                        if (catTarget.isNotEmpty()) viewModel.selectCategory(catTarget)
                    }
                }
            }

            // 3. Search Bar - Floating Glassmorphic with Comprehensive Dropdown Results
            item {
                val isFilterActive = phoneOrNameFilter.isNotEmpty() || neighborFilter.isNotEmpty() || isVipOnly || isAvailableOnly
                val searchDropdownResults = remember(searchQuery, allProviders, stores, properties) {
                    if (searchQuery.trim().length >= 2) {
                        com.serviseyem.utils.CrossFuzzySearchEngine.searchEverything(
                            query = searchQuery,
                            providers = allProviders,
                            stores = stores,
                            properties = properties,
                            userLat = viewModel.userLatitude.value,
                            userLng = viewModel.userLongitude.value,
                            context = context
                        )
                    } else {
                        emptyList()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    ServicesSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { 
                            viewModel.updateSearchQuery(it)
                        },
                        isFilterActive = isFilterActive,
                        onFilterClick = { showFiltersPanel = true },
                        isSpeechSearchEnabled = settingsState.isSpeechSearchEnabled,
                        onVoiceClick = {
                            VoiceManager.onHear?.invoke { heardText ->
                                viewModel.updateSearchQuery(heardText)
                            }
                        },
                        themeColors = themeColors
                    )

                    if (searchDropdownResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "نتائج البحث المفلترة بالمسافة (${searchDropdownResults.size})",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "المسار والملاحة ↗",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                searchDropdownResults.take(8).forEach { item ->
                                    val userLat = viewModel.userLatitude.value
                                    val userLng = viewModel.userLongitude.value
                                    val distMeters = com.serviseyem.ui.screens.map.utils.MapDistanceCalculator.calculateDistanceMeters(
                                        userLat, userLng, item.latitude, item.longitude
                                    )
                                    val distFormatted = if (distMeters > 0) com.serviseyem.ui.screens.map.utils.MapDistanceCalculator.formatDistance(distMeters) else ""
                                    val etaFormatted = if (distMeters > 0) com.serviseyem.ui.screens.map.utils.MapDistanceCalculator.computeEta(distMeters) else ""

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.openNavigation(item.latitude, item.longitude, item.title)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val iconBadge = when (item.type) {
                                            com.serviseyem.utils.CrossFuzzySearchEngine.SearchType.STORE -> "🏪"
                                            com.serviseyem.utils.CrossFuzzySearchEngine.SearchType.PERSON -> "🛠️"
                                            com.serviseyem.utils.CrossFuzzySearchEngine.SearchType.STREET -> "📍"
                                            com.serviseyem.utils.CrossFuzzySearchEngine.SearchType.PROPERTY -> "🏠"
                                            com.serviseyem.utils.CrossFuzzySearchEngine.SearchType.CITY -> "🇾🇪"
                                            com.serviseyem.utils.CrossFuzzySearchEngine.SearchType.AREA -> "📍"
                                            else -> "📍"
                                        }
                                        Text(text = iconBadge, fontSize = 18.sp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                color = Color.White,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (distFormatted.isNotEmpty()) "${item.subtitle} • $distFormatted" else item.subtitle,
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 11.5.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (etaFormatted.isNotEmpty()) {
                                                Text(
                                                    text = etaFormatted,
                                                    color = Color(0xFF00E5FF),
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "انطلق 📍",
                                                color = Color(0xFF00E5FF),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 3.8 Custom Notification Card (تنبيه العروض الحصرية خلفية داكنة فخمة)
            item {
                val currentCategoryTitle = selectedCategory?.ifBlank { "جميع الأقسام" } ?: "جميع الأقسام"
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFFD4A017).copy(alpha = 0.6f)),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD4A017)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔔", fontSize = 16.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("تنبيه العروض الحصرية", color = Color(0xFFFFC93C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("•", color = Color(0xFF94A3B8))
                                Text(currentCategoryTitle, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "أحدث العروض والخصومات المتاحة الآن في فئة $currentCategoryTitle - استكشفها الآن!",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp,
                                maxLines = 2,
                                softWrap = true
                            )
                        }
                    }
                }
            }

            // 4. Quick Category Grid (Adaptive minSize = 80.dp cards - Dark Luxury Glassmorphic Cards)
            item {
                data class CategoryGridItem(
                    val title: String,
                    val emoji: String,
                    val gradient: androidx.compose.ui.graphics.Brush,
                    val onClick: () -> Unit
                )

                val allCategoryItems = remember {
                    listOf(
                        CategoryGridItem("المتاجر", "🏪", BespokePremiumSystem.StoresGradient) {
                            viewModel.setActiveBrowserTab("المحلات والمتاجر")
                        },
                        CategoryGridItem("المطاعم", "🍔", BespokePremiumSystem.FoodGradient) {
                            viewModel.setActiveBrowserTab("المطاعم والكافيهات")
                        },
                        CategoryGridItem("المراكز الطبية", "🏥", BespokePremiumSystem.MedicalGradient) {
                            viewModel.setActiveBrowserTab("المراكز الطبية")
                        },
                        CategoryGridItem("العقارات", "🏠", BespokePremiumSystem.EstateGradient) {
                            viewModel.setActiveBrowserTab("العقارات")
                        },
                        CategoryGridItem("الوظائف", "💼", BespokePremiumSystem.JobsGradient) {
                            viewModel.setActiveBrowserTab("إعلانات الوظائف")
                        },
                        CategoryGridItem("الفنيون", "🛠️", BespokePremiumSystem.ServicesGradient) {
                            viewModel.setActiveBrowserTab("الفنيون")
                        },
                        CategoryGridItem("خدمات منزلية", "🧹", androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF007A4D), Color(0xFF00838F)))) {
                            viewModel.setActiveBrowserTab("خدمات منزلية")
                        },
                        CategoryGridItem("نقل وتوصيل", "🚗", androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF007A4D), Color(0xFF0284C7)))) {
                            viewModel.setActiveBrowserTab("نقل وتوصيل")
                        }
                    )
                }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    val columns = (maxWidth / 80.dp).toInt().coerceIn(3, 8)
                    val chunkedRows = remember(columns, allCategoryItems) {
                        allCategoryItems.chunked(columns)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        chunkedRows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Surface(
                                        onClick = item.onClick,
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF1E293B),
                                        tonalElevation = 4.dp,
                                        shadowElevation = 3.dp,
                                        border = BorderStroke(1.dp, Color(0xFF334155)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 84.dp)
                                            .wrapContentHeight()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .shadow(3.dp, CircleShape)
                                                    .clip(CircleShape)
                                                    .background(item.gradient),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(item.emoji, fontSize = 18.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.title,
                                                fontSize = 11.sp,
                                                lineHeight = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 2,
                                                softWrap = true,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                if (rowItems.size < columns) {
                                    for (i in 0 until (columns - rowItems.size)) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Join As Provider Gold Banner
            item {
                com.serviseyem.ui.components.BespokePremiumSystem.JoinAsProviderBanner {
                    viewModel.navigateTo(AppScreens.REGISTER_FORM)
                }
            }

            // 8. Technicians & Providers Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🛠️", fontSize = 15.sp)
                        Text(
                            "الفنيون ومقدمو الخدمات المعتمدون",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = com.serviseyem.ui.components.BespokePremiumSystem.EmeraldAccent.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, com.serviseyem.ui.components.BespokePremiumSystem.EmeraldAccent)
                    ) {
                        Text(
                            text = "موثق ✓",
                            color = com.serviseyem.ui.components.BespokePremiumSystem.EmeraldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // 5. Main Tab Content
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 100.dp)) {
                when (activeTabName) {
                    "الفنيون", "قسم الفنيين" -> {
                        TechniciansSectionView(
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onChatOpen = onChatOpen
                        )
                    }
                    "خدمات منزلية" -> {
                        ComingSoonSectionView(
                            title = "الخدمات المنزلية",
                            subtitle = "نعمل على تجهيز باقة متكاملة من الخدمات المنزلية تشمل التنظيف الشامل، التعقيم، ورعاية الحدائق بأعلى معايير الجودة والأمان.",
                            iconEmoji = "🧹",
                            gradient = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF0D9488))),
                            onBackClick = { viewModel.setActiveBrowserTab("الرئيسية") },
                            onRegisterClick = {
                                viewModel.setTargetRegistrationType("home_services")
                                viewModel.navigateTo(AppScreens.REGISTER_FORM)
                            }
                        )
                    }
                    "نقل وتوصيل" -> {
                        ComingSoonSectionView(
                            title = "النقل والتوصيل",
                            subtitle = "نعمل على تجهيز أسطول النقل والتوصيل السريع لنقل البضائع، الطرود، والأثاث بين جميع المدن والمحافظات اليمنية.",
                            iconEmoji = "🚗",
                            gradient = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))),
                            onBackClick = { viewModel.setActiveBrowserTab("الرئيسية") },
                            onRegisterClick = {
                                viewModel.setTargetRegistrationType("delivery")
                                viewModel.navigateTo(AppScreens.REGISTER_FORM)
                            }
                        )
                    }
                    "المفضلة" -> {
                        FavoritesScreenLayout(
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onBackClick = { viewModel.setActiveBrowserTab("الرئيسية") },
                            onOpenProviderDetails = {
                                viewModel.selectedProvider = it
                                viewModel.navigateTo("DYNAMIC_PROFILE")
                            },
                            onOpenStoreDetails = { selectedStoreForDetails = it },
                            onOpenPropertyDetails = { selectedPropertyForDetails = it },
                            onOpenChat = onChatOpen
                        )
                    }
                    "المحلات والمتاجر", "المحلات", settingsState.storesTabName -> {
                        StoresSectionView(
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onStoreClick = { 
                                viewModel.selectedStore = it
                                viewModel.navigateTo("STORE_DETAILS")
                            },
                            onCreateStoreClick = {
                                viewModel.setTargetRegistrationType("store")
                                viewModel.navigateTo(AppScreens.REGISTER_FORM)
                            }
                        )
                    }
                    "المطاعم والكافيهات", "المطاعم" -> {
                        RestaurantsSectionView(
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onStoreClick = { 
                                viewModel.selectedStore = it
                                viewModel.navigateTo("STORE_DETAILS")
                            },
                            onCreateRestaurantClick = {
                                viewModel.setTargetRegistrationType("restaurant")
                                viewModel.navigateTo(AppScreens.REGISTER_FORM)
                            }
                        )
                    }
                    "المراكز الطبية", "المراكز" -> {
                        MedicalCentersSectionView(
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onStoreClick = { 
                                viewModel.selectedStore = it
                                viewModel.navigateTo("STORE_DETAILS")
                            },
                            onCreateMedicalClick = {
                                viewModel.setTargetRegistrationType("medical")
                                viewModel.navigateTo(AppScreens.REGISTER_FORM)
                            }
                        )
                    }
                    "العقارات", settingsState.propertiesTabName -> {
                        PropertiesSectionView(
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onPropertyClick = { selectedPropertyForDetails = it },
                            onCreatePropertyClick = {
                                viewModel.setTargetRegistrationType("property")
                                viewModel.navigateTo(AppScreens.REGISTER_FORM)
                            }
                        )
                    }
                    "إعلانات الوظائف", "الوظائف" -> {
                        JobsSectionView(
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onJobClick = { selectedJobForDetails = it },
                            onCreateJobClick = {
                                viewModel.setTargetRegistrationType("job")
                                viewModel.navigateTo(AppScreens.REGISTER_FORM)
                            }
                        )
                    }
                    else -> {
                        val effectiveDisplayProviders = if (searchQuery.isBlank() && 
                            (selectedCategory.isNullOrBlank() || selectedCategory == "ALL" || selectedCategory == "الكل")) {
                            allProviders
                        } else {
                            filteredProviders
                        }

                        ServicesBrowserMainContent(
                            viewModel = viewModel,
                            themeColors = themeColors,
                            displayProviders = effectiveDisplayProviders,
                            displayStores = filteredStores,
                            displayProperties = filteredProperties,
                            isProvidersLoading = isProvidersLoading,
                            categories = categories,
                            selectedCategoryId = selectedCategory,
                            onCategorySelected = { viewModel.selectCategory(it ?: "") },
                            providersLimit = providersLimit,
                            onLoadMore = { providersLimit += 10 },
                            onStoreClick = { 
                                viewModel.selectedStore = it
                                viewModel.navigateTo("STORE_DETAILS")
                            },
                            onPropertyClick = { selectedPropertyForDetails = it },
                            onChatOpen = onChatOpen
                        )
                    }
                }
            }
        }

    }


    // Dialogs & Payment Sheets
    if (showFiltersPanel) {
        FilterPanelDialog(
            cities = citiesList,
            selectedCityId = activeCityId ?: "",
            onSelectCity = { viewModel.setCityFilter(it) },
            categories = categories,
            selectedCategoryId = selectedCategory,
            onSelectCategory = { viewModel.selectCategory(it ?: "") },
            isVipOnly = isVipOnly,
            onToggleVip = { viewModel.toggleVipFilter() },
            isAvailableOnly = isAvailableOnly,
            onToggleAvailable = { viewModel.toggleAvailableFilter() },
            radiusKm = radiusKm,
            onRadiusChange = { viewModel.setRadiusKm(it) },
            neighborhood = neighborFilter,
            onNeighborhoodChange = { viewModel.setNeighborhoodFilter(it) },
            themeColors = themeColors,
            onDismiss = { showFiltersPanel = false }
        )
    }

    selectedStoreForDetails?.let { store ->
        StoreQuickDetailsDialog(
            store = store,
            context = context,
            themeColors = themeColors,
            onDismiss = { selectedStoreForDetails = null },
            onOpenDetails = {
                val storeToOpen = store
                selectedStoreForDetails = null
                viewModel.selectedStore = storeToOpen
                viewModel.navigateTo("STORE_DETAILS")
            }
        )
    }

    selectedPropertyForDetails?.let { prop ->
        PropertyQuickDetailsDialog(
            property = prop,
            context = context,
            themeColors = themeColors,
            onDismiss = { selectedPropertyForDetails = null }
        )
    }

    selectedJobForDetails?.let { job ->
        JobQuickDetailsDialog(
            job = job,
            context = context,
            themeColors = themeColors,
            onDismiss = { selectedJobForDetails = null },
            onApply = {
                val targetJob = job
                selectedJobForDetails = null
                showJobApplicationForJob = targetJob
            }
        )
    }

    showJobApplicationForJob?.let { job ->
        JobApplicationDialog(
            viewModel = viewModel,
            themeColors = themeColors,
            jobTitle = job.title,
            onDismiss = { showJobApplicationForJob = null }
        )
    }
}

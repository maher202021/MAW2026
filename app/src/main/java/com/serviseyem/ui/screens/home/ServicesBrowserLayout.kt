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

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(com.serviseyem.ui.components.BespokePremiumSystem.DeepDark),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
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

            // 3. Search Bar - Floating Glassmorphic
            item {
                val isFilterActive = phoneOrNameFilter.isNotEmpty() || neighborFilter.isNotEmpty() || isVipOnly || isAvailableOnly
                Box(
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
                }
            }
            
            // 4. Quick Category Grid (4x2 Bespoke Categories - Unified Dimensions & High Contrast)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val gridRow1 = listOf(
                        Triple("المتاجر", "🏪", com.serviseyem.ui.components.BespokePremiumSystem.StoresGradient) to {
                            viewModel.setActiveBrowserTab("المحلات والمتاجر")
                        },
                        Triple("المطاعم", "🍔", com.serviseyem.ui.components.BespokePremiumSystem.FoodGradient) to {
                            viewModel.setActiveBrowserTab("المطاعم والكافيهات")
                        },
                        Triple("المراكز الطبية", "🏥", com.serviseyem.ui.components.BespokePremiumSystem.MedicalGradient) to {
                            viewModel.setActiveBrowserTab("المراكز الطبية")
                        },
                        Triple("العقارات", "🏠", com.serviseyem.ui.components.BespokePremiumSystem.EstateGradient) to {
                            viewModel.setActiveBrowserTab("العقارات")
                        }
                    )

                    val gridRow2 = listOf(
                        Triple("الوظائف", "💼", com.serviseyem.ui.components.BespokePremiumSystem.JobsGradient) to {
                            viewModel.setActiveBrowserTab("إعلانات الوظائف")
                        },
                        Triple("الفنيون", "🛠️", com.serviseyem.ui.components.BespokePremiumSystem.ServicesGradient) to {
                            viewModel.setActiveBrowserTab("الفنيون")
                        },
                        Triple("خدمات منزلية", "🧹", androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF0D9488)))) to {
                            viewModel.setActiveBrowserTab("خدمات منزلية")
                        },
                        Triple("نقل وتوصيل", "🚗", androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))) to {
                            viewModel.setActiveBrowserTab("نقل وتوصيل")
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gridRow1.forEach { (cat, onClick) ->
                            Surface(
                                onClick = onClick,
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF131D33),
                                border = BorderStroke(1.dp, Color(0xFF1E2E4E)),
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(76.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(cat.third),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(cat.second, fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cat.first,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gridRow2.forEach { (cat, onClick) ->
                            Surface(
                                onClick = onClick,
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF131D33),
                                border = BorderStroke(1.dp, Color(0xFF1E2E4E)),
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(76.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(cat.third),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(cat.second, fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cat.first,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        softWrap = false
                                    )
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

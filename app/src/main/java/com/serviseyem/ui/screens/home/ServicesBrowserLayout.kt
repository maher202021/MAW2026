@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.home

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        modifier = Modifier.fillMaxSize(),
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

            // 3. Search Bar
            item {
                val isFilterActive = phoneOrNameFilter.isNotEmpty() || neighborFilter.isNotEmpty() || isVipOnly || isAvailableOnly
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateToScreen(AppScreens.UNIFIED_SEARCH) }
                ) {
                    ServicesSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { 
                            viewModel.updateSearchQuery(it)
                            viewModel.navigateToScreen(AppScreens.UNIFIED_SEARCH)
                        },
                        isFilterActive = isFilterActive,
                        onFilterClick = { showFiltersPanel = true },
                        isSpeechSearchEnabled = settingsState.isSpeechSearchEnabled,
                        onVoiceClick = {
                            VoiceManager.onHear?.invoke { heardText ->
                                viewModel.updateSearchQuery(heardText)
                                viewModel.navigateToScreen(AppScreens.UNIFIED_SEARCH)
                            }
                        },
                        themeColors = themeColors
                    )
                }
            }
        }

        // 4. Tab Navigation Chips (Modern luxury light aesthetic with active glowing border)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeTabs.forEach { tabName ->
                    val isSelected = activeTabName == tabName
                    val tabIcon = when (tabName) {
                        "الرئيسية" -> androidx.compose.material.icons.Icons.Default.Home
                        "المحلات والمتاجر", "المحلات" -> androidx.compose.material.icons.Icons.Default.Storefront
                        "المطاعم والكافيهات", "المطاعم" -> androidx.compose.material.icons.Icons.Default.Restaurant
                        "المراكز الطبية", "المراكز" -> androidx.compose.material.icons.Icons.Default.LocalHospital
                        "العقارات" -> androidx.compose.material.icons.Icons.Default.Apartment
                        "إعلانات الوظائف", "الوظائف" -> androidx.compose.material.icons.Icons.Default.Work
                        "المفضلة" -> androidx.compose.material.icons.Icons.Default.Favorite
                        else -> androidx.compose.material.icons.Icons.Default.Category
                    }

                    Surface(
                        onClick = { viewModel.setActiveBrowserTab(tabName) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color.White else Color(0xFF131E33),
                        shadowElevation = if (isSelected) 4.dp else 1.dp,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color.White else Color(0xFF1F2F4E)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = tabIcon,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = tabName,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelected) Color(0xFF0F172A) else Color.White
                            )
                        }
                    }
                }
            }
        }

        // 5. Main Tab Content
        item {
            when (activeTabName) {
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

    // Dialogs & Payment Sheets
    if (showFiltersPanel) {
        FilterPanelDialog(
            cities = citiesList,
            selectedCityId = activeCityId ?: "",
            onSelectCity = { viewModel.setCityFilter(it) },
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

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

    val activeTabBg = remember(activeTabName) {
        when (activeTabName) {
            "المطاعم والكافيهات", "المطاعم" -> com.serviseyem.ui.theme.FoodScreenBg
            "المحلات والمتاجر", "المحلات" -> com.serviseyem.ui.theme.StoreScreenBg
            "المراكز الطبية", "المراكز الطبية والعيادات" -> com.serviseyem.ui.theme.MedicalScreenBg
            "العقارات" -> com.serviseyem.ui.theme.EstateScreenBg
            "إعلانات الوظائف", "الوظائف" -> com.serviseyem.ui.theme.JobsScreenBg
            else -> com.serviseyem.ui.theme.AppBackground
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(activeTabBg),
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
            
            // 3.8 Custom Notification Card (تنبيه العروض الحصرية بلون ذهبي هادئ فوق القسم الرئيسي)
            item {
                val currentCategoryTitle = selectedCategory?.ifBlank { "جميع الأقسام" } ?: "جميع الأقسام"
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = LightCardSurface,
                    border = BorderStroke(1.dp, SoftGold.copy(alpha = 0.5f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFE6F4EA))))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SoftGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔔", fontSize = 16.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("تنبيه العروض الحصرية", color = Color(0xFFB45309), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("•", color = TextMutedSage)
                                Text(currentCategoryTitle, color = TextMutedSage, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "أحدث العروض والخصومات المتاحة الآن في فئة $currentCategoryTitle - استكشفها الآن!",
                                color = TextDarkGreen,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 4. Quick Category Grid (4x2 Bespoke Categories - HD 3D Style with depth)
            item {
                data class CategoryGridItem(
                    val title: String,
                    val emoji: String,
                    val gradient: androidx.compose.ui.graphics.Brush,
                    val onClick: () -> Unit
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val gridRow1 = listOf(
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
                        }
                    )

                    val gridRow2 = listOf(
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        gridRow1.forEach { item ->
                            Surface(
                                onClick = item.onClick,
                                shape = RoundedCornerShape(16.dp),
                                color = LightCardSurface,
                                tonalElevation = 3.dp,
                                shadowElevation = 3.dp,
                                border = BorderStroke(1.dp, LightCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(78.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .shadow(4.dp, RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(item.gradient),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.emoji, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDarkGreen,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        gridRow2.forEach { item ->
                            Surface(
                                onClick = item.onClick,
                                shape = RoundedCornerShape(16.dp),
                                color = LightCardSurface,
                                tonalElevation = 3.dp,
                                shadowElevation = 3.dp,
                                border = BorderStroke(1.dp, LightCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(78.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .shadow(4.dp, RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(item.gradient),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.emoji, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDarkGreen,
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

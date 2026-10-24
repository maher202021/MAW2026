package com.serviseyem.ui

import androidx.activity.result.ActivityResultLauncher
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.*
import com.serviseyem.ui.CreateBookingScreen
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.*
import com.serviseyem.ui.dialogs.*
import com.serviseyem.ui.screens.admin.AdminPanelLayout
import com.serviseyem.ui.screens.assistant.SmartAssistantDialogView
import com.serviseyem.ui.screens.bookings.BookingsScreenLayout
import com.serviseyem.ui.screens.bookings.InstantRequestsScreen
import com.serviseyem.ui.screens.bookings.OrdersScreenLayout
import com.serviseyem.ui.screens.chat.ChatListScreen
import com.serviseyem.ui.screens.chat.ChatScreen
import com.serviseyem.ui.screens.entities.*
import com.serviseyem.ui.screens.home.FavoritesScreenLayout
import com.serviseyem.ui.screens.home.ServicesBrowserLayout
import com.serviseyem.ui.screens.home.UnifiedGlobalSearchScreen
import com.serviseyem.ui.screens.map.MapScreenLayout
import com.serviseyem.ui.screens.notifications.UserNotificationsDialogView
import com.serviseyem.ui.screens.owner.OwnerDashboardScreen
import com.serviseyem.ui.screens.register.RegisterScreen
import com.serviseyem.ui.screens.status.StatusScreen
import com.serviseyem.ui.screens.urgent.UrgentOffersList
import com.serviseyem.ui.screens.urgent.UrgentRequestDetailsScreen
import com.serviseyem.utils.VisualThemePalette

@Composable
fun AppNavigator(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    permissionLauncher: ActivityResultLauncher<Array<String>>? = null,
    locationPermissions: Array<String> = emptyArray()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val settingsState by viewModel.settings.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()

    val showNotificationsDialog by viewModel.showNotificationsDialog.collectAsState()
    val showRestoreAccountDialog by viewModel.showRestoreAccountDialog.collectAsState()

    val providers by viewModel.providers.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategoryId.collectAsState()
    val requestOffers by viewModel.requestOffers.collectAsState()

    if (settingsState.isMaintenanceActive && adminRole == "GUEST") {
        MaintenanceSplashView(settingsState = settingsState, themeColors = themeColors, viewModel = viewModel)
        return
    }

    Scaffold(
        topBar = {
            if (ScreenRoutes.showTopBar(currentScreen, adminRole)) {
                AppHeaderBar(
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onNotificationsClick = { viewModel.navigateToScreen(AppScreens.NOTIFICATIONS_VIEW) },
                    onChatsClick = { viewModel.navigateToScreen(AppScreens.CHAT_LIST) },
                    onMenuClick = { viewModel.toggleRestoreAccountDialog(true) }
                )
            }
        },
        bottomBar = {
            if (ScreenRoutes.showBottomBar(currentScreen, adminRole)) {
                AppFooterBar(
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onInfoClick = { viewModel.navigateToScreen(AppScreens.ABOUT_APP) }
                )
            }
        },
        contentWindowInsets = if (currentScreen == AppScreens.CHAT_DIRECT || currentScreen == AppScreens.MAP_VIEW || currentScreen == AppScreens.SMART_ASSISTANT) {
            WindowInsets(0, 0, 0, 0)
        } else {
            ScaffoldDefaults.contentWindowInsets
        },
        containerColor = themeColors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (currentScreen == AppScreens.CHAT_DIRECT || currentScreen == AppScreens.MAP_VIEW || currentScreen == AppScreens.SMART_ASSISTANT) PaddingValues(0.dp) else innerPadding)
        ) {
            when (currentScreen) {
                AppScreens.USER_BROWSE, AppScreens.HOME -> {
                    var activeSectionIdForCreation by remember { mutableStateOf("") }
                    var preselectedRegistrationType by remember { mutableStateOf("") }

                    ServicesBrowserLayout(
                        viewModel = viewModel,
                        themeColors = themeColors,
                        activeSectionIdForCreation = activeSectionIdForCreation,
                        onActiveSectionIdForCreationChange = { activeSectionIdForCreation = it },
                        preselectedRegistrationType = preselectedRegistrationType,
                        onPreselectedRegistrationTypeChange = { preselectedRegistrationType = it },
                        onChatOpen = { targetId ->
                            val foundProvider = viewModel.providers.value.find { it.id == targetId || it.phone == targetId }
                            if (foundProvider != null) {
                                viewModel.openDirectChat(
                                    targetUserId = foundProvider.id.ifBlank { foundProvider.phone },
                                    targetUserName = foundProvider.name,
                                    targetUserPhoto = foundProvider.profileImage,
                                    relatedEntityId = foundProvider.id,
                                    relatedEntityType = "PROVIDER"
                                )
                            } else {
                                val foundStore = viewModel.stores.value.find { it.id == targetId }
                                if (foundStore != null) {
                                    val isMedical = foundStore.sectionId.contains("medical") || foundStore.categoryId.contains("medical") || foundStore.name.contains("طبي") || foundStore.name.contains("صيدلية")
                                    val isRestaurant = !isMedical && (foundStore.sectionId.contains("restaurant") || foundStore.categoryId.contains("restaurant") || foundStore.name.contains("مطعم") || foundStore.name.contains("كافيه"))
                                    val entityType = when {
                                        isMedical -> "MEDICAL"
                                        isRestaurant -> "RESTAURANT"
                                        else -> "STORE"
                                    }
                                    viewModel.openDirectChat(
                                        targetUserId = foundStore.id,
                                        targetUserName = foundStore.name,
                                        targetUserPhoto = foundStore.logoImage.ifBlank { foundStore.coverImage },
                                        relatedEntityId = foundStore.id,
                                        relatedEntityType = entityType
                                    )
                                } else {
                                    val foundProp = viewModel.properties.value.find { it.id == targetId }
                                    if (foundProp != null) {
                                        viewModel.openDirectChat(
                                            targetUserId = foundProp.id.ifBlank { foundProp.phone },
                                            targetUserName = foundProp.title.ifBlank { foundProp.ownerName },
                                            targetUserPhoto = foundProp.images.firstOrNull() ?: "",
                                            relatedEntityId = foundProp.id,
                                            relatedEntityType = "PROPERTY"
                                        )
                                    } else if (targetId == "support_channel_id" || targetId == "admin_support") {
                                        viewModel.openSupportChat()
                                    } else {
                                        viewModel.openDirectChat(
                                            targetUserId = targetId,
                                            targetUserName = "مقدم الخدمة",
                                            relatedEntityId = targetId,
                                            relatedEntityType = "PROVIDER"
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
                AppScreens.ADMIN_PANEL -> AdminPanelLayout(viewModel = viewModel, themeColors = themeColors)
                AppScreens.OWNER_PANEL, AppScreens.PRODUCTS_MGMT_VIEW, AppScreens.PRICE_MGMT_VIEW, AppScreens.OFFERS_MGMT_VIEW, AppScreens.GALLERY_MGMT_VIEW -> OwnerDashboardScreen(
                    account = UnifiedBusinessAccount(id = "owner_1", name = "المالك", description = "منصة اليمن"),
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onNavigateTab = {},
                    onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                )
                AppScreens.STATUS_VIEW -> StatusScreen(viewModel = viewModel, themeColors = themeColors)
                AppScreens.CATEGORIES_VIEW -> CategoriesScreen(viewModel = viewModel, themeColors = themeColors, onCategoryClick = { cat -> viewModel.selectCategory(cat); viewModel.navigateToScreen(AppScreens.USER_BROWSE) })
                AppScreens.STORES_VIEW -> StoresScreen(
                    viewModel = viewModel, 
                    themeColors = themeColors, 
                    onStoreClick = {}, 
                    onChatClick = { store ->
                        viewModel.openDirectChat(
                            targetUserId = store.id,
                            targetUserName = store.name,
                            targetUserPhoto = store.logoImage.ifBlank { store.coverImage },
                            relatedEntityId = store.id,
                            relatedEntityType = "STORE"
                        )
                    }, 
                    onRequestServiceClick = { viewModel.navigateToScreen(AppScreens.QUICK_SERVICE_REQUEST) }
                )
                AppScreens.MEDICAL_VIEW -> MedicalCentersScreen(
                    viewModel = viewModel, 
                    themeColors = themeColors, 
                    onMedicalCenterClick = {}, 
                    onChatClick = { med ->
                        viewModel.openDirectChat(
                            targetUserId = med.id,
                            targetUserName = med.name,
                            targetUserPhoto = med.profileImage,
                            relatedEntityId = med.id,
                            relatedEntityType = "MEDICAL"
                        )
                    }, 
                    onBookAppointmentClick = { viewModel.navigateToScreen(AppScreens.QUICK_SERVICE_REQUEST) }
                )
                AppScreens.RESTAURANTS_VIEW -> RestaurantsScreen(
                    viewModel = viewModel, 
                    themeColors = themeColors, 
                    onRestaurantClick = {}, 
                    onChatClick = { rest ->
                        viewModel.openDirectChat(
                            targetUserId = rest.id,
                            targetUserName = rest.name,
                            targetUserPhoto = rest.logoImage.ifBlank { rest.coverImage },
                            relatedEntityId = rest.id,
                            relatedEntityType = "RESTAURANT"
                        )
                    }, 
                    onOrderMealClick = { viewModel.navigateToScreen(AppScreens.QUICK_SERVICE_REQUEST) }
                )
                AppScreens.PROPERTIES_VIEW -> PropertiesScreen(
                    viewModel = viewModel, 
                    themeColors = themeColors, 
                    onPropertyClick = {}, 
                    onChatClick = { prop ->
                        viewModel.openDirectChat(
                            targetUserId = prop.id.ifBlank { prop.phone },
                            targetUserName = prop.title.ifBlank { prop.ownerName },
                            targetUserPhoto = prop.images.firstOrNull() ?: "",
                            relatedEntityId = prop.id,
                            relatedEntityType = "PROPERTY"
                        )
                    }, 
                    onRequestInspectionClick = { viewModel.navigateToScreen(AppScreens.QUICK_SERVICE_REQUEST) }
                )
                AppScreens.CHAT_LIST -> ChatListScreen(
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    themeColors = themeColors,
                    onChannelClick = { ch ->
                        viewModel.targetChatChannelId = ch.id
                        viewModel.navigateToScreen(AppScreens.CHAT_DIRECT)
                    },
                    onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) },
                    onStartSupportChat = { viewModel.openSupportChat() }
                )
                AppScreens.CHAT_DIRECT -> ChatScreen(
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    themeColors = themeColors,
                    channelId = viewModel.targetChatChannelId,
                    targetUserId = viewModel.targetChatUserId,
                    targetUserName = viewModel.targetChatUserName,
                    targetUserPhoto = viewModel.targetChatUserPhoto,
                    relatedEntityId = viewModel.targetChatRelatedEntityId,
                    relatedEntityType = viewModel.targetChatRelatedEntityType,
                    showClientIdentityToProviders = viewModel.settings.value.showClientIdentityToProviders,
                    onBackClick = {
                        viewModel.targetChatChannelId = null
                        viewModel.targetChatUserId = null
                        viewModel.targetChatUserName = null
                        viewModel.targetChatUserPhoto = null
                        viewModel.targetChatRelatedEntityId = null
                        viewModel.targetChatRelatedEntityType = null
                        viewModel.navigateToScreen(AppScreens.USER_BROWSE)
                    }
                )
                AppScreens.CREATE_BOOKING -> {
                    val pId = viewModel.selectedProvider?.id
                        ?: viewModel.selectedStore?.id
                        ?: viewModel.selectedProperty?.id
                        ?: ""
                    val pName = viewModel.selectedProvider?.name
                        ?: viewModel.selectedStore?.name
                        ?: viewModel.selectedProperty?.title
                        ?: ""
                    val pService = viewModel.selectedProvider?.specialization?.ifBlank { viewModel.selectedProvider?.profession }
                        ?: viewModel.selectedStore?.categoryId
                        ?: viewModel.selectedProperty?.title
                        ?: ""
                    CreateBookingScreen(
                        providerId = pId,
                        providerName = pName,
                        preselectedService = pService,
                        onBack = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) },
                        onBookingCreated = { booking ->
                            viewModel.createBooking(booking)
                            viewModel.navigateToScreen(AppScreens.BOOKINGS_VIEW)
                        }
                    )
                }
                AppScreens.DYNAMIC_PROFILE, AppScreens.OWNER_PROFILE_VIEW, AppScreens.PROVIDER_DETAILS, AppScreens.STORE_DETAILS, AppScreens.PROPERTY_DETAILS -> DynamicPolymorphicProfileScreen(
                    provider = viewModel.selectedProvider,
                    store = viewModel.selectedStore,
                    property = viewModel.selectedProperty,
                    job = viewModel.selectedJob,
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                )
                AppScreens.BOOKINGS_VIEW -> BookingsScreenLayout(viewModel = viewModel, themeColors = themeColors)
                AppScreens.INSTANT_REQUESTS_VIEW -> InstantRequestsScreen(viewModel = viewModel, themeColors = themeColors)
                AppScreens.ORDERS_VIEW -> OrdersScreenLayout(viewModel = viewModel, themeColors = themeColors, onRequestQuickService = { viewModel.navigateToScreen(AppScreens.QUICK_SERVICE_REQUEST) })
                AppScreens.MAP_VIEW -> MapScreenLayout(
                    viewModel = viewModel,
                    onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) },
                    onOpenProviderDetails = { provider ->
                        viewModel.selectedProvider = provider
                        viewModel.selectedStore = null
                        viewModel.selectedProperty = null
                        viewModel.selectedJob = null
                        viewModel.navigateToScreen(AppScreens.PROVIDER_DETAILS)
                    },
                    onOpenStoreDetails = { store ->
                        viewModel.selectedStore = store
                        viewModel.selectedProvider = null
                        viewModel.selectedProperty = null
                        viewModel.selectedJob = null
                        viewModel.navigateToScreen(AppScreens.STORE_DETAILS)
                    },
                    onOpenPropertyDetails = { prop ->
                        viewModel.selectedProperty = prop
                        viewModel.selectedProvider = null
                        viewModel.selectedStore = null
                        viewModel.selectedJob = null
                        viewModel.navigateToScreen(AppScreens.PROPERTY_DETAILS)
                    },
                    onRequestBooking = { entity ->
                        when (entity) {
                            is ProviderEntity -> {
                                viewModel.selectedProvider = entity
                                viewModel.selectedStore = null
                                viewModel.selectedProperty = null
                                viewModel.selectedJob = null
                                viewModel.navigateToScreen(AppScreens.CREATE_BOOKING)
                            }
                            is StoreEntity -> {
                                viewModel.selectedStore = entity
                                viewModel.selectedProvider = null
                                viewModel.selectedProperty = null
                                viewModel.selectedJob = null
                                viewModel.navigateToScreen(AppScreens.CREATE_BOOKING)
                            }
                            is PropertyEntity -> {
                                viewModel.selectedProperty = entity
                                viewModel.selectedProvider = null
                                viewModel.selectedStore = null
                                viewModel.selectedJob = null
                                viewModel.navigateToScreen(AppScreens.CREATE_BOOKING)
                            }
                        }
                    }
                )
                AppScreens.FAVORITES_VIEW -> FavoritesScreenLayout(
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                )
                AppScreens.URGENT_REQUEST_DETAILS -> UrgentRequestDetailsScreen(
                    requestId = viewModel.selectedRequestId,
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onNavigateBack = { viewModel.goBack() },
                    onNavigateToChat = { phoneOrId, name ->
                        viewModel.openDirectChat(
                            targetUserId = phoneOrId,
                            targetUserName = name,
                            relatedEntityId = viewModel.selectedRequestId,
                            relatedEntityType = "URGENT_PROVIDER"
                        )
                    }
                )
                AppScreens.OFFERS_LIST -> UrgentOffersList(
                    offers = requestOffers,
                    isOwner = viewModel.isProviderUser,
                    onAcceptOffer = { _ -> },
                    onContactProvider = { offer ->
                        viewModel.openOrCreateChatChannel(
                            targetId = offer.technicianId.ifBlank { offer.technicianPhone },
                            targetType = "URGENT_PROVIDER",
                            targetName = offer.technicianName,
                            targetPhone = offer.technicianPhone,
                            relatedEntityId = offer.requestId,
                            relatedEntityType = "URGENT_PROVIDER"
                        ) {
                            viewModel.navigateToScreen(AppScreens.CHAT_DIRECT)
                        }
                    }
                )
                AppScreens.REGISTER_FORM, AppScreens.JOIN_REQUEST_STATUS, "TECHNICIAN_DASHBOARD", "STORE_DASHBOARD", "RESTAURANT_DASHBOARD", "MEDICAL_DASHBOARD", "PROPERTY_DASHBOARD", "JOB_POSTER_DASHBOARD", "CLIENT_DASHBOARD", "DASHBOARD" -> {
                    RegisterScreen(
                        viewModel = viewModel,
                        themeColors = themeColors,
                        onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                    )
                }
                AppScreens.PASSWORD_RESET_WAITING -> {
                    com.serviseyem.ui.screens.register.PasswordResetWaitingScreen(
                        viewModel = viewModel,
                        themeColors = themeColors,
                        onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                    )
                }
                AppScreens.ABOUT_APP -> {
                    com.serviseyem.ui.screens.about.AboutAppScreenContent(
                        viewModel = viewModel,
                        themeColors = themeColors
                    )
                }
                AppScreens.NOTIFICATIONS_VIEW -> {
                    com.serviseyem.ui.screens.notifications.UserNotificationsScreen(
                        viewModel = viewModel,
                        themeColors = themeColors,
                        onBack = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                    )
                }
                AppScreens.SMART_ASSISTANT -> {
                    SmartAssistantDialogView(
                        viewModel = viewModel,
                        settings = settingsState,
                        themeColors = themeColors,
                        onDismiss = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) },
                        onChatOpen = { viewModel.openSupportChat() },
                        onRequestQuickService = {
                            viewModel.navigateToScreen(AppScreens.QUICK_SERVICE_REQUEST)
                        },
                        onNavigateToMap = {
                            viewModel.navigateToScreen(AppScreens.MAP_VIEW)
                        }
                    )
                }
                AppScreens.QUICK_SERVICE_REQUEST -> {
                    QuickServiceRequestDialog(
                        viewModel = viewModel,
                        themeColors = themeColors,
                        onDismiss = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) },
                        onRequestCreated = { viewModel.navigateToScreen(AppScreens.ORDERS_VIEW) }
                    )
                }
                AppScreens.UNIFIED_SEARCH -> {
                    UnifiedGlobalSearchScreen(
                        viewModel = viewModel,
                        themeColors = themeColors,
                        onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("الشاشة المعروضة: $currentScreen", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            // ⚠️ SECURITY NOTICE: Backdoor dialog for debugging only.
            // Must be disabled in production builds via BuildConfig.DEBUG check.
            val showBackdoorDialog by viewModel.showBackdoorDialog.collectAsState()
            if (showBackdoorDialog) {
                BackdoorLoginDialog(
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onDismiss = { viewModel.dismissBackdoorDialog() }
                )
            }

            if (currentScreen == AppScreens.USER_BROWSE || currentScreen == AppScreens.HOME || currentScreen == AppScreens.FAVORITES_VIEW) {
                FloatingIconsOverlay(
                    settings = settingsState,
                    themeColors = themeColors,
                    onAssistantClick = { viewModel.navigateToScreen(AppScreens.SMART_ASSISTANT) },
                    onRequestServiceClick = { viewModel.navigateToScreen(AppScreens.QUICK_SERVICE_REQUEST) }
                )
            }

            if (showRestoreAccountDialog) {
                RestoreAccountDialog(viewModel = viewModel, themeColors = themeColors, onDismiss = { viewModel.toggleRestoreAccountDialog(false) })
            }
            if (showNotificationsDialog) {
                UserNotificationsDialogView(viewModel = viewModel, themeColors = themeColors, onDismiss = { viewModel.toggleNotificationsDialog(false) })
            }
        }
    }
}

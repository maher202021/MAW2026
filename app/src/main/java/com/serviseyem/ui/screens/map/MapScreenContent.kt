@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.serviseyem.ui.screens.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.map.components.ContextualSmartSuggestionsCard
import com.serviseyem.ui.screens.map.components.MapFilterBar
import com.serviseyem.ui.screens.map.components.SmartNavigationPanel
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.ui.screens.map.utils.StreetOrLandmark
import com.serviseyem.utils.GnssDiagnosticsManager
import com.serviseyem.utils.GpsSensorFusionFilter
import com.serviseyem.utils.LandmarkAddressStore
import com.serviseyem.utils.RecentDestinations
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.VoiceSearchHelper
import com.serviseyem.utils.VoiceState
import com.serviseyem.utils.VoiceStatus
import com.serviseyem.utils.resolveThemePalette
import kotlinx.coroutines.launch

/**
 * 🗺️ MapScreenContent
 * Primary presentation composable for the Map & Radar screen (Radar-First 100%).
 */
@Composable
fun MapScreenContent(
    viewModel: MainViewModel,
    state: MapScreenState = rememberMapScreenState(),
    onBackClick: () -> Unit = {},
    onOpenProviderDetails: (ProviderEntity) -> Unit = {},
    onOpenStoreDetails: (StoreEntity) -> Unit = {},
    onOpenPropertyDetails: (PropertyEntity) -> Unit = {},
    onRequestBooking: (Any) -> Unit = {},
    themeColors: VisualThemePalette = resolveThemePalette(viewModel.settings.collectAsState().value)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
            }
        }
    }

    var showMapErrorOverlay by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<android.webkit.WebView?>(null) }
    var showValhallaDialog by remember { mutableStateOf(false) }
    var showPlacesDownload by remember { mutableStateOf(false) }
    var showRegionsDialog by remember { mutableStateOf(false) }
    var showDestChooser by remember { mutableStateOf(false) }
    var pickingDestination by remember { mutableStateOf(false) }
    var pickedPoint by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var offlinePackVersion by remember { mutableIntStateOf(0) }
    val addressStore = remember { LandmarkAddressStore(context) }
    var savedAddresses by remember { mutableStateOf(addressStore.all()) }

    var voiceState by remember { mutableStateOf(VoiceState()) }
    var searchFocusTick by remember { mutableIntStateOf(0) }
    val voiceHelper = remember {
        VoiceSearchHelper(
            context = context,
            onFinal = { text -> state.searchQuery = text },
            onState = { newState -> voiceState = newState }
        )
    }
    DisposableEffect(Unit) {
        onDispose { voiceHelper.destroy() }
    }
    val micPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceHelper.start()
        } else {
            voiceState = VoiceState(
                status = VoiceStatus.ERROR,
                message = "صلاحية الميكروفون مطلوبة للبحث الصوتي.",
                canOpenSettings = true,
                canUseKeyboard = true
            )
        }
    }

    BackHandler(enabled = true) {
        when {
            pickingDestination -> {
                pickingDestination = false
                pickedPoint = null
            }
            state.selectedEntity != null -> state.resetSelection()
            else -> onBackClick()
        }
    }

    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()

    val userLatState by viewModel.userLatitude.collectAsState()
    val userLngState by viewModel.userLongitude.collectAsState()
    val hasRealLocation = userLatState != 0.0 && !userLatState.isNaN() && userLngState != 0.0 && !userLngState.isNaN()

    val fallbackCityCoords = remember(state.selectedCity) {
        OfflineMapManager.getCityCoordinates(state.selectedCity)
    }
    val safeUserLat = if (hasRealLocation) userLatState else fallbackCityCoords.latitude
    val safeUserLng = if (hasRealLocation) userLngState else fallbackCityCoords.longitude

    LaunchedEffect(pickingDestination) {
        if (pickingDestination) {
            snackbarHostState.showSnackbar("اضغط على الخريطة لتحديد الوجهة")
        }
    }

    fun startDestinationNavigation(destName: String, la: Double, lo: Double) {
        RecentDestinations(context).add(destName, la, lo)
        state.pendingNavDestination = StreetOrLandmark(
            name = destName,
            type = "وجهة مختارة",
            city = state.selectedCity,
            area = "",
            lat = la,
            lng = lo
        )
        state.isRadarMode = false
        state.currentMapMode = MapMode.NAVIGATION
        state.currentMapLayer = "navigation"
        webViewRef?.post {
            webViewRef?.evaluateJavascript(
                "if (window.switchLayer) { window.switchLayer('navigation'); } if (window.flyToLocation) { window.flyToLocation($la, $lo, 16); }",
                null
            )
        }
        if (!hasRealLocation) {
            viewModel.startLocationUpdates()
            coroutineScope.launch {
                snackbarHostState.showSnackbar("تم اختيار «$destName» — فعّل GPS لتحديث نقطة انطلاقك")
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.startLocationUpdates()
            coroutineScope.launch {
                snackbarHostState.showSnackbar("تم تفعيل نظام تحديد المواقع بنجاح ✓")
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            viewModel.startLocationUpdates()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { state.clearOffsets() }
    }

    val filteredProviders = remember(providers, state.selectedCategory, state.selectedCity, state.searchQuery) {
        MapScreenFilters.filterProviders(providers, state.selectedCategory, state.selectedCity, state.searchQuery)
    }
    val filteredStores = remember(stores, state.selectedCategory, state.selectedCity, state.searchQuery) {
        MapScreenFilters.filterStores(stores, state.selectedCategory, state.selectedCity, state.searchQuery)
    }
    val filteredProperties = remember(properties, state.selectedCategory, state.selectedCity, state.searchQuery) {
        MapScreenFilters.filterProperties(properties, state.selectedCategory, state.selectedCity, state.searchQuery)
    }
    val radarPoints = remember(filteredProviders, filteredStores, filteredProperties, safeUserLat, safeUserLng) {
        MapScreenFilters.buildRadarPoints(
            filteredProviders = filteredProviders,
            filteredStores = filteredStores,
            filteredProperties = filteredProperties,
            safeUserLat = safeUserLat,
            safeUserLng = safeUserLng
        )
    }

    LaunchedEffect(state.selectedCity) {
        state.panOffset = Offset.Zero
        state.zoomScale = 1.0f
    }

    val sensorManager = remember { context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager }
    val gpsFusionFilter = remember { GpsSensorFusionFilter(sensorManager) }
    val gnssDiagnosticsManager = remember { GnssDiagnosticsManager(context) }
    val satelliteStatus by gnssDiagnosticsManager.gnssStatusFlow.collectAsState()

    LaunchedEffect(Unit) { gnssDiagnosticsManager.startMonitoring() }
    LaunchedEffect(Unit) { gpsFusionFilter.start() }
    DisposableEffect(Unit) {
        onDispose {
            gpsFusionFilter.stop()
            gnssDiagnosticsManager.stopMonitoring()
        }
    }
    LaunchedEffect(userLatState, userLngState) {
        val loc = android.location.Location("fusion").apply {
            latitude = userLatState
            longitude = userLngState
            accuracy = 10f
        }
        gpsFusionFilter.onLocationUpdate(loc)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color(0xFF0B0F19),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White,
                    actionColor = Color(0xFF10B981),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(com.serviseyem.ui.theme.AppBackground)
                .padding(paddingValues)
        ) {
            MapMainViewport(
                state = state,
                radarPoints = radarPoints,
                safeUserLat = safeUserLat,
                safeUserLng = safeUserLng,
                filteredProviders = filteredProviders,
                filteredStores = filteredStores,
                filteredProperties = filteredProperties,
                pickingDestination = pickingDestination,
                pickedPoint = pickedPoint,
                onPickedPointChange = { pickedPoint = it },
                onShowMapErrorChange = { showMapErrorOverlay = it },
                onWebViewCreated = { webViewRef = it },
                webViewRef = webViewRef,
                context = context,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState,
                haptic = haptic,
                themeColors = themeColors
            )

            BackHandler(enabled = state.currentMapMode == MapMode.AR_VPS) {
                state.currentMapMode = MapMode.VELA_3D
            }

            MapFilterBar(
                selectedCategory = state.selectedCategory,
                onCategorySelected = { state.selectedCategory = it },
                selectedCity = state.selectedCity,
                onCitySelected = { city ->
                    state.selectedCity = city
                    if (city != "الكل") {
                        val coords = OfflineMapManager.getCityCoordinates(city)
                        viewModel.updateUserLocation(coords.latitude, coords.longitude)
                    }
                },
                searchQuery = state.searchQuery,
                onSearchQueryChange = { state.searchQuery = it },
                onResultSelected = { result ->
                    state.searchQuery = result.title
                    state.panOffset = Offset.Zero
                    state.zoomScale = 1.8f
                    val script = "if (window.flyToLocation) { window.flyToLocation(${result.latitude}, ${result.longitude}, 18); }"
                    webViewRef?.evaluateJavascript(script, null)
                    when (val item = result.originalItem) {
                        is ProviderEntity -> state.selectedEntity = item
                        is StoreEntity -> state.selectedEntity = item
                        is PropertyEntity -> state.selectedEntity = item
                        is StreetOrLandmark -> {
                            state.pendingNavDestination = item
                            state.currentMapMode = MapMode.NAVIGATION
                        }
                    }
                },
                providers = providers,
                stores = stores,
                properties = properties,
                userLat = safeUserLat,
                userLng = safeUserLng,
                onBackClick = onBackClick,
                focusTick = searchFocusTick,
                onVoiceSearchClick = {
                    if (voiceHelper.hasMicPermission()) voiceHelper.start()
                    else micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                themeColors = themeColors,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            )

            if (state.isContextualSuggestionVisible) {
                ContextualSmartSuggestionsCard(
                    isVisible = state.isContextualSuggestionVisible,
                    currentCity = state.selectedCity,
                    onApplySuggestion = { cat ->
                        state.selectedCategory = cat
                        state.isContextualSuggestionVisible = false
                    },
                    onDismiss = { state.isContextualSuggestionVisible = false },
                    themeColors = themeColors,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                )
            }

            MapGnssBadge(
                satelliteStatus = satelliteStatus,
                onClick = { state.isGnssPanelOpen = true },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(top = 70.dp, start = 12.dp)
            )

            MapControlsDock(
                state = state,
                viewModel = viewModel,
                context = context,
                providers = providers,
                filteredProviders = filteredProviders,
                safeUserLat = safeUserLat,
                safeUserLng = safeUserLng,
                webViewRef = webViewRef,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState,
                themeColors = themeColors,
                onHideMapError = { showMapErrorOverlay = false },
                onOpenDestChooser = { showDestChooser = true },
                onOpenValhallaDialog = { showValhallaDialog = true },
                onOpenRegionsDialog = { showRegionsDialog = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            )

            BackHandler(enabled = state.currentMapMode == MapMode.NAVIGATION) {
                state.currentMapMode = MapMode.VELA_3D
                webViewRef?.post { webViewRef?.evaluateJavascript("if (window.clearRoute) { window.clearRoute(); }", null) }
            }

            if (pickingDestination) {
                MapPickDestinationBar(
                    context = context,
                    pickedPoint = pickedPoint,
                    onCancel = {
                        pickingDestination = false
                        pickedPoint = null
                    },
                    onConfirmDestination = { label, la, lo ->
                        pickingDestination = false
                        pickedPoint = null
                        startDestinationNavigation(label, la, lo)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }

            if (state.currentMapMode == MapMode.NAVIGATION && !pickingDestination) {
                SmartNavigationPanel(
                    userLat = safeUserLat,
                    userLng = safeUserLng,
                    themeColors = themeColors,
                    initialDestination = state.pendingNavDestination,
                    onDrawRouteOnMap = { lat1, lng1, lat2, lng2, destName ->
                        val escapedName = destName.replace("'", "\\'")
                        val script = "if (window.drawRoute) { window.drawRoute($lat1, $lng1, $lat2, $lng2, '#00E5FF', '$escapedName'); }"
                        webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    },
                    onClearRoute = {
                        webViewRef?.post { webViewRef?.evaluateJavascript("if (window.clearRoute) { window.clearRoute(); }", null) }
                    },
                    onCloseNavigation = {
                        state.currentMapMode = MapMode.VELA_3D
                        state.currentMapLayer = "google_satellite"
                        state.pendingNavDestination = null
                        val script = "if (window.clearRoute) { window.clearRoute(); } if (window.switchLayer) { window.switchLayer('google_satellite'); }"
                        webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    },
                    onPromptDownloadOffline = { showValhallaDialog = true },
                    onOpenDestinationChooser = { showDestChooser = true },
                    onOpenPlacesDownload = { showPlacesDownload = true },
                    onLaunchTurnByTurn = { la, lo, destTitle -> viewModel.openNavigation(la, lo, destTitle) },
                    offlineStateKey = offlinePackVersion,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }

            state.selectedEntity?.let { entity ->
                MapEntityBottomSheetHost(
                    entity = entity,
                    state = state,
                    viewModel = viewModel,
                    safeUserLat = safeUserLat,
                    safeUserLng = safeUserLng,
                    onOpenProviderDetails = onOpenProviderDetails,
                    onOpenStoreDetails = onOpenStoreDetails,
                    onOpenPropertyDetails = onOpenPropertyDetails,
                    onRequestBooking = onRequestBooking,
                    themeColors = themeColors,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .imePadding()
                )
            }
        }
    }

    MapScreenDialogsHost(
        state = state,
        viewModel = viewModel,
        context = context,
        providers = providers,
        stores = stores,
        safeUserLat = safeUserLat,
        safeUserLng = safeUserLng,
        hasRealLocation = hasRealLocation,
        savedAddresses = savedAddresses,
        addressStore = addressStore,
        onSavedAddressesUpdated = { savedAddresses = it },
        showDestChooser = showDestChooser,
        onDismissDestChooser = { showDestChooser = false },
        onStartDestinationNavigation = ::startDestinationNavigation,
        onStartPickFromMap = {
            showDestChooser = false
            state.isRadarMode = false
            pickedPoint = null
            pickingDestination = true
        },
        showValhallaDialog = showValhallaDialog,
        onDismissValhallaDialog = { showValhallaDialog = false },
        onOpenDestChooser = { showDestChooser = true },
        showPlacesDownload = showPlacesDownload,
        onOpenPlacesDownload = { showPlacesDownload = true },
        onDismissPlacesDownload = { showPlacesDownload = false },
        showRegionsDialog = showRegionsDialog,
        onOpenRegionsDialog = { showRegionsDialog = true },
        onDismissRegionsDialog = { showRegionsDialog = false },
        onBumpOfflinePackVersion = { offlinePackVersion++ },
        voiceState = voiceState,
        onResetVoiceState = { voiceState = VoiceState(status = VoiceStatus.IDLE) },
        voiceHelper = voiceHelper,
        micPermLauncher = micPermLauncher,
        onBumpSearchFocus = { searchFocusTick++ },
        satelliteStatus = satelliteStatus,
        webViewRef = webViewRef,
        coroutineScope = coroutineScope,
        snackbarHostState = snackbarHostState,
        onRequestBooking = onRequestBooking,
        themeColors = themeColors
    )
}

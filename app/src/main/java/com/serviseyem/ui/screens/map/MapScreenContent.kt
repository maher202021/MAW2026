@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.serviseyem.ui.screens.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
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
import com.serviseyem.ui.screens.map.components.MapFilterBar
import com.serviseyem.ui.screens.map.components.SmartNavigationPanel
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.ui.screens.map.utils.StreetOrLandmark
import com.serviseyem.utils.GnssDiagnosticsManager
import com.serviseyem.utils.GpsSensorFusionFilter
import com.serviseyem.utils.LandmarkAddressStore
import com.serviseyem.utils.LocationHealth
import com.serviseyem.utils.LocationStatus
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
    // [نظام خرائط وملاحة عالمي - ملكنا]
    var longPressDraftPoint by remember { mutableStateOf<Pair<Double, Double>?>(null) }
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
            longPressDraftPoint != null -> {
                longPressDraftPoint = null
            }
            state.selectedEntity != null -> state.resetSelection()
            else -> onBackClick()
        }
    }

    val settingsState by viewModel.settings.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val isPrivilegedAdmin = adminRole in setOf("OWNER", "MAIN_ADMIN", "SUPER_ADMIN", "ADMIN", "SUPERVISOR")

    val mapBlockedByAdmin = !settingsState.isMapFeatureEnabled && !isPrivilegedAdmin
    val mapBlockedForGuest = !settingsState.showMapToVisitors && currentUserId == "guest" && !isPrivilegedAdmin

    if (mapBlockedByAdmin || mapBlockedForGuest) {
        Scaffold(
            containerColor = themeColors.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { pad ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = themeColors.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (mapBlockedByAdmin) "🗺️ الخرائط متوقفة مؤقتاً" else "🔒 الخريطة مخصصة للأعضاء المسجلين",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (mapBlockedByAdmin) {
                                "تم إيقاف عرض شاشة الخرائط والرادار مؤقتاً من قبل إدارة المنصة."
                            } else {
                                "يرجى تسجيل الدخول أو إنشاء حساب لعرض الخريطة التفاعلية والخدمات القريبة."
                            },
                            color = themeColors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("العودة", color = Color.Black)
                        }
                    }
                }
            }
        }
        return
    }

    LaunchedEffect(settingsState.defaultMapBaseLayer) {
        val defaultLayer = settingsState.defaultMapBaseLayer.ifBlank { "esri_world_imagery" }
        if (state.currentMapMode != MapMode.NAVIGATION) {
            state.currentMapLayer = defaultLayer
        }
    }

    LaunchedEffect(settingsState.mapMaxDistanceKm) {
        if (settingsState.mapMaxDistanceKm in 5..150) {
            state.maxRangeKm = settingsState.mapMaxDistanceKm.toFloat()
        }
    }

    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()

    val userLatState by viewModel.userLatitude.collectAsState()
    val userLngState by viewModel.userLongitude.collectAsState()
    val userAccuracyMeters by viewModel.userAccuracyMeters.collectAsState()
    val lastLocationTimestampMs by viewModel.lastLocationTimestampMs.collectAsState()
    val hasRealLocation = userLatState != 0.0 && !userLatState.isNaN() && userLngState != 0.0 && !userLngState.isNaN()
    // [نظام خرائط وملاحة عالمي - ملكنا]
    val locationStatus = remember(userLatState, userLngState, userAccuracyMeters, lastLocationTimestampMs) {
        LocationHealth.evaluate(
            context = context,
            lat = userLatState,
            lng = userLngState,
            accuracyM = userAccuracyMeters,
            timestampMs = lastLocationTimestampMs
        )
    }

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

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("map_ux_hints", android.content.Context.MODE_PRIVATE)
        if (!prefs.getBoolean("shown_long_press_hint", false)) {
            prefs.edit().putBoolean("shown_long_press_hint", true).apply()
            kotlinx.coroutines.delay(1400L)
            snackbarHostState.showSnackbar("اضغط مطولاً على الخريطة لتحديد نقطة")
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

    val adminVisibleProviders = remember(providers, settingsState.showProvidersOnMap) {
        if (settingsState.showProvidersOnMap) providers else emptyList()
    }
    val adminVisibleStores = remember(
        stores,
        settingsState.showStoresOnMap,
        settingsState.showRestaurantsOnMap,
        settingsState.showMedicalOnMap
    ) {
        stores.filter { store ->
            val sec = store.sectionId.lowercase()
            val cat = store.categoryId.lowercase()
            val nm = store.name.lowercase()
            val isMedical = sec.contains("medical") || cat.contains("medical") || nm.contains("طبي") || nm.contains("عياد") || nm.contains("مستشفى") || nm.contains("صيدلية")
            val isRestaurant = !isMedical && (sec.contains("restaurant") || cat.contains("restaurant") || nm.contains("مطعم") || nm.contains("كافيه") || nm.contains("وجبات"))
            when {
                isMedical -> settingsState.showMedicalOnMap
                isRestaurant -> settingsState.showRestaurantsOnMap
                else -> settingsState.showStoresOnMap
            }
        }
    }
    val adminVisibleProperties = remember(properties, settingsState.showPropertiesOnMap) {
        if (settingsState.showPropertiesOnMap) properties else emptyList()
    }

    val filteredProviders = remember(adminVisibleProviders, state.selectedCategory, state.selectedCity, state.searchQuery) {
        MapScreenFilters.filterProviders(adminVisibleProviders, state.selectedCategory, state.selectedCity, state.searchQuery)
    }
    val filteredStores = remember(adminVisibleStores, state.selectedCategory, state.selectedCity, state.searchQuery) {
        MapScreenFilters.filterStores(adminVisibleStores, state.selectedCategory, state.selectedCity, state.searchQuery)
    }
    val filteredProperties = remember(adminVisibleProperties, state.selectedCategory, state.selectedCity, state.searchQuery) {
        MapScreenFilters.filterProperties(adminVisibleProperties, state.selectedCategory, state.selectedCity, state.searchQuery)
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

    val pendingFocus by viewModel.pendingMapFocus.collectAsState()
    LaunchedEffect(pendingFocus, webViewRef) {
        val target = pendingFocus ?: return@LaunchedEffect
        if (webViewRef != null && target.lat != 0.0 && target.lng != 0.0) {
            state.isRadarMode = false
            val markerArr = if (target.label.isNotBlank()) {
                org.json.JSONArray().put(
                    org.json.JSONObject()
                        .put("name", target.label)
                        .put("lat", target.lat)
                        .put("lng", target.lng)
                ).toString()
            } else null
            val markerScript = markerArr?.let { "if (window.setWaypointMarkers) { window.setWaypointMarkers($it); }" }.orEmpty()
            webViewRef?.post {
                webViewRef?.evaluateJavascript(
                    "$markerScript if (window.flyToLocation) { window.flyToLocation(${target.lat}, ${target.lng}, 17); }",
                    null
                )
            }
            viewModel.consumePendingMapFocus()
        }
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
            val disabledLayers = remember(settingsState.disabledMapLayers) {
                settingsState.disabledMapLayers.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
            }
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
                longPressDraftPoint = longPressDraftPoint,
                onPickedPointChange = { pickedPoint = it },
                onMapLongPress = { la, lo ->
                    state.selectedEntity = null
                    longPressDraftPoint = la to lo
                },
                onMapTap = {
                    state.selectedEntity = null
                    longPressDraftPoint = null
                },
                onShowMapErrorChange = { showMapErrorOverlay = it },
                onWebViewCreated = { webViewRef = it },
                webViewRef = webViewRef,
                context = context,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState,
                haptic = haptic,
                maxZoom = settingsState.mapMaxZoom.coerceIn(10, 22),
                showSatMenu = settingsState.showSatMenuOnMap,
                showMeasureTool = settingsState.showMeasureToolOnMap && ("MEASURE_RULER" !in disabledLayers),
                themeColors = themeColors
            )

            BackHandler(enabled = state.currentMapMode == MapMode.AR_VPS) {
                state.currentMapMode = MapMode.VELA_3D
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MapFilterBar(
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { state.selectedCategory = it },
                    selectedCity = state.selectedCity,
                    onCitySelected = { city ->
                        state.selectedCity = city
                        val coords = OfflineMapManager.getCityCoordinates(city)
                        webViewRef?.post {
                            webViewRef?.evaluateJavascript(
                                "if (window.flyToLocation) { window.flyToLocation(${coords.latitude}, ${coords.longitude}, ${coords.defaultZoom}); }",
                                null
                            )
                        }
                    },
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = { state.searchQuery = it },
                    onResultSelected = { result ->
                        state.panOffset = Offset.Zero
                        state.zoomScale = 1.8f
                        webViewRef?.post {
                            webViewRef?.evaluateJavascript(
                                "if (window.flyToLocation) { window.flyToLocation(${result.latitude}, ${result.longitude}, 17); }",
                                null
                            )
                        }
                        when (val item = result.originalItem) {
                            is ProviderEntity -> {
                                state.searchQuery = result.title
                                state.selectedEntity = item
                            }
                            is StoreEntity -> {
                                state.searchQuery = result.title
                                state.selectedEntity = item
                            }
                            is PropertyEntity -> {
                                state.searchQuery = result.title
                                state.selectedEntity = item
                            }
                            is StreetOrLandmark -> {
                                state.searchQuery = ""
                                val markerArr = org.json.JSONArray().put(
                                    org.json.JSONObject()
                                        .put("name", item.name)
                                        .put("lat", result.latitude)
                                        .put("lng", result.longitude)
                                )
                                webViewRef?.post {
                                    webViewRef?.evaluateJavascript(
                                        "if (window.setWaypointMarkers) { window.setWaypointMarkers($markerArr); }",
                                        null
                                    )
                                }
                                state.pendingNavDestination = item
                                state.currentMapMode = MapMode.NAVIGATION
                            }
                            else -> {
                                state.searchQuery = ""
                                val landmark = StreetOrLandmark(
                                    name = result.title,
                                    type = result.subtitle,
                                    city = state.selectedCity,
                                    area = result.subtitle,
                                    lat = result.latitude,
                                    lng = result.longitude
                                )
                                state.pendingNavDestination = landmark
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
                    themeColors = themeColors
                )

                // [نظام خرائط وملاحة عالمي - ملكنا]
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    LocationStatusPill(
                        status = locationStatus,
                        onActionClick = {
                            when (locationStatus) {
                                is LocationStatus.NoPermission -> {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                                is LocationStatus.ProviderOff -> {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }
                                }
                                else -> viewModel.startLocationUpdates()
                            }
                        }
                    )
                }
            }

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
                modifier = Modifier.fillMaxSize()
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
            } else if (longPressDraftPoint != null) {
                MapPickDestinationBar(
                    context = context,
                    pickedPoint = longPressDraftPoint,
                    onCancel = { longPressDraftPoint = null },
                    onConfirmDestination = { label, la, lo ->
                        longPressDraftPoint = null
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
                        val quotedName = org.json.JSONObject.quote(destName)
                        val script = "if (window.drawRoute) { window.drawRoute($lat1, $lng1, $lat2, $lng2, '#00E5FF', $quotedName); }"
                        webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    },
                    onClearRoute = {
                        webViewRef?.post { webViewRef?.evaluateJavascript("if (window.clearRoute) { window.clearRoute(); }", null) }
                    },
                    onCloseNavigation = {
                        val fallbackLayer = settingsState.defaultMapBaseLayer.ifBlank { "esri_world_imagery" }
                        state.currentMapMode = MapMode.VELA_3D
                        state.currentMapLayer = fallbackLayer
                        state.pendingNavDestination = null
                        val quotedFallback = org.json.JSONObject.quote(fallbackLayer)
                        val script = "if (window.clearRoute) { window.clearRoute(); } if (window.switchLayer) { window.switchLayer($quotedFallback); }"
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

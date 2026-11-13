@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.map
import com.serviseyem.ui.*

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.dialogs.BookingDialog
import com.serviseyem.ui.screens.map.components.*
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.utils.GnssDiagnosticsManager
import com.serviseyem.utils.GpxManager
import com.serviseyem.utils.VisualThemePalette
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
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    // ضبط أيقونات شريط الحالة العلوي للهاتف لتكون واضحة وناصعة البياض على الخلفية الداكنة
    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
            }
        }
    }

    var showMapErrorOverlay by remember { mutableStateOf(false) }
    var mapRetryCount by remember { mutableIntStateOf(0) }
    var webViewRef by remember { mutableStateOf<android.webkit.WebView?>(null) }
    var showValhallaDialog by remember { mutableStateOf(false) }
    var showPlacesDownload by remember { mutableStateOf(false) }
    var showDestChooser by remember { mutableStateOf(false) }
    var pickingDestination by remember { mutableStateOf(false) }
    var pickedPoint by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var offlinePackVersion by remember { mutableIntStateOf(0) }
    val addressStore = remember { com.serviseyem.utils.LandmarkAddressStore(context) }
    var savedAddresses by remember { mutableStateOf(addressStore.all()) }

    androidx.activity.compose.BackHandler(enabled = true) {
        when {
            pickingDestination -> {
                pickingDestination = false
                pickedPoint = null
            }
            state.selectedEntity != null -> state.resetSelection()
            else -> onBackClick()
        }
    }

    // Data streams from ViewModel
    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()

    val userLatState by viewModel.userLatitude.collectAsState()
    val userLngState by viewModel.userLongitude.collectAsState()

    val hasRealLocation = userLatState != 0.0 && !userLatState.isNaN() && userLngState != 0.0 && !userLngState.isNaN()

    // إصلاح 1.1: عند عدم توفر GPS حقيقي نستخدم مركز المدينة المحددة حالياً بدلاً من إحداثيات ثابتة قديمة
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
        com.serviseyem.utils.RecentDestinations(context).add(destName, la, lo)
        state.pendingNavDestination = com.serviseyem.ui.screens.map.utils.StreetOrLandmark(
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

    // Location Permission launcher
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

    // Auto-request location updates on screen launch to acquire user's real GPS immediately
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
        onDispose {
            state.clearOffsets()
        }
    }

    // Filtered lists using MapScreenFilters module
    val filteredProviders = remember(providers, state.selectedCategory, state.selectedCity, state.searchQuery) {
        MapScreenFilters.filterProviders(providers, state.selectedCategory, state.selectedCity, state.searchQuery)
    }

    val filteredStores = remember(stores, state.selectedCategory, state.selectedCity, state.searchQuery) {
        MapScreenFilters.filterStores(stores, state.selectedCategory, state.selectedCity, state.searchQuery)
    }

    val filteredProperties = remember(properties, state.selectedCategory, state.selectedCity, state.searchQuery) {
        MapScreenFilters.filterProperties(properties, state.selectedCategory, state.selectedCity, state.searchQuery)
    }

    // Radar Points calculated via MapScreenFilters
    val radarPoints = remember(filteredProviders, filteredStores, filteredProperties, safeUserLat, safeUserLng) {
        MapScreenFilters.buildRadarPoints(
            filteredProviders = filteredProviders,
            filteredStores = filteredStores,
            filteredProperties = filteredProperties,
            safeUserLat = safeUserLat,
            safeUserLng = safeUserLng
        )
    }

    // Reset map pan & zoom when governorate changes
    LaunchedEffect(state.selectedCity) {
        state.panOffset = Offset.Zero
        state.zoomScale = 1.0f
    }

    val sensorManager = remember { context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager }
    val gpsFusionFilter = remember { com.serviseyem.utils.GpsSensorFusionFilter(sensorManager) }

    val gnssDiagnosticsManager = remember { GnssDiagnosticsManager(context) }
    val satelliteStatus by gnssDiagnosticsManager.gnssStatusFlow.collectAsState()

    LaunchedEffect(Unit) {
        gnssDiagnosticsManager.startMonitoring()
    }

    LaunchedEffect(Unit) {
        gpsFusionFilter.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            gpsFusionFilter.stop()
            gnssDiagnosticsManager.stopMonitoring()
        }
    }

    // Update fusion filter when raw location changes
    LaunchedEffect(userLatState, userLngState) {
        val loc = android.location.Location("fusion").apply {
            latitude = userLatState
            longitude = userLngState
            accuracy = 10f // Assuming 10m accuracy for raw GPS
        }
        gpsFusionFilter.onLocationUpdate(loc)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color(0xFF0B0F19), // Requested background
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
            // Main Map View (Radar-First 100% by default; WebView only on explicit user toggle)
            if (state.isRadarMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    RadarRenderer(
                        items = radarPoints,
                        selectedItemId = when (val ent = state.selectedEntity) {
                            is ProviderEntity -> ent.id
                            is StoreEntity -> ent.id
                            is PropertyEntity -> ent.id
                            else -> null
                        },
                        onItemSelected = { item ->
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            state.selectedEntity = item.originalItem
                        },
                        isHeatmapActive = state.isHeatmapActive,
                        maxRangeKm = state.maxRangeKm,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else if (state.currentMapMode == MapMode.AR_VPS) {
                // 📷 الوضع 3: كاميرا الواقع المعزز والرؤية الحاسوبية (VPS AR Mode)
                VpsArCameraView(
                    userLat = safeUserLat,
                    userLng = safeUserLng,
                    nearbyProviders = filteredProviders,
                    nearbyStores = filteredStores,
                    nearbyProperties = filteredProperties,
                    onEntitySelected = { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        state.selectedEntity = it 
                    },
                    onCloseAR = { state.currentMapMode = MapMode.VELA_3D },
                    themeColors = themeColors,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    RealLeafletMapView(
                        userCoords = Pair(safeUserLat, safeUserLng),
                        nearbyProviders = filteredProviders,
                        nearbyStores = filteredStores,
                        nearbyProperties = filteredProperties,
                        dynamicOffsets = state.dynamicOffsets,
                        selectedCity = state.selectedCity,
                        zoomScale = state.zoomScale,
                        onZoomScaleChange = { state.zoomScale = it },
                        panOffset = state.panOffset,
                        onPanOffsetChange = { state.panOffset = it },
                        selectedEntity = state.selectedEntity,
                        mapMode = state.currentMapMode,
                        currentMapLayer = state.currentMapLayer,
                        tiltAngle = state.tiltAngle,
                        onProviderSelected = { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            state.selectedEntity = it 
                        },
                        onStoreSelected = { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            state.selectedEntity = it 
                        },
                        onPropertySelected = { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            state.selectedEntity = it 
                        },
                        onDeselect = { state.selectedEntity = null },
                        onSwitchToRadar = {
                            showMapErrorOverlay = false
                            state.isRadarMode = true
                        },
                        onWebViewCreated = { webViewRef = it },
                        isEarthquakeLayerActive = state.isEarthquakeLayerActive,
                        earthquakeUpdateTrigger = state.earthquakeUpdateTrigger,
                        onEarthquakeDataLoaded = { timestamp, count ->
                            state.lastEarthquakeUpdateTime = timestamp
                            state.earthquakeCount = count
                            state.isEarthquakeLoading = false
                        },
                        onEarthquakeDataError = { _ ->
                            state.isEarthquakeLoading = false
                        },
                        pickMode = pickingDestination,
                        onMapPicked = { la, lo ->
                            if (pickingDestination) pickedPoint = la to lo
                        },
                        waypointMarkers = pickedPoint?.takeIf { pickingDestination }?.let {
                            listOf(Triple("الوجهة المختارة", it.first, it.second))
                        }.orEmpty(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            androidx.activity.compose.BackHandler(enabled = state.currentMapMode == MapMode.AR_VPS) {
                state.currentMapMode = MapMode.VELA_3D
            }

            // --- 1. Minimalist Search Bar (Top) ---
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
                    
                    // التوجه السينمائي إلى الموقع المختار
                    val script = "if (window.flyToLocation) { window.flyToLocation(${result.latitude}, ${result.longitude}, 18); }"
                    webViewRef?.evaluateJavascript(script, null)
                    
                    // عرض تفاصيل العنصر فوراً أو الانتقال للملاحة للشوارع والمعالم
                    when (val item = result.originalItem) {
                        is ProviderEntity -> state.selectedEntity = item
                        is StoreEntity -> state.selectedEntity = item
                        is PropertyEntity -> state.selectedEntity = item
                        is com.serviseyem.ui.screens.map.utils.StreetOrLandmark -> {
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
                themeColors = themeColors,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            )

            // --- 1.1 Contextual Smart Suggestion Card ---
            if (state.isContextualSuggestionVisible) {
                ContextualSmartSuggestionsCard(
                    isVisible = state.isContextualSuggestionVisible,
                    currentCity = state.selectedCity,
                    onApplySuggestion = { cat ->
                        state.selectedCategory = cat
                        state.isContextualSuggestionVisible = false
                    },
                    onDismiss = {
                        state.isContextualSuggestionVisible = false
                    },
                    themeColors = themeColors,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                )
            }

            // 🛰️ ثالثاً: مكوّن تشخيص GNSS الحي المدمج في زاوية الشاشة (تحديث محلي 100% بصفر استهلاك لـ Firebase)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.90f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(top = 70.dp, start = 12.dp)
                    .clickable { state.isGnssPanelOpen = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.SatelliteAlt,
                        contentDescription = "أقمار صناعية",
                        tint = if (satelliteStatus.usedSatellites > 0) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "🛰️ ${satelliteStatus.usedSatellites}/${satelliteStatus.totalSatellites}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "• ${satelliteStatus.qualityRating.take(6)}",
                        fontSize = 9.sp,
                        color = Color(0xFFA7F3D0)
                    )
                }
            }

            // --- 2. Unified Floating Control Dock (Right Side) ---
            MapControls(
                isRadarMode = state.isRadarMode,
                onToggleRadarMode = {
                    showMapErrorOverlay = false
                    state.isRadarMode = !state.isRadarMode
                },
                isHeatmapActive = state.isHeatmapActive,
                onToggleHeatmap = {
                    state.isHeatmapActive = !state.isHeatmapActive
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            if (state.isHeatmapActive) "تم تفعيل الخريطة الحرارية 🔥" else "تم إيقاف الخريطة الحرارية"
                        )
                    }
                },
                currentLayer = state.currentMapLayer,
                onSwitchLayer = { layerKey ->
                    if (layerKey == "navigation") {
                        if (state.pendingNavDestination == null) {
                            showDestChooser = true
                        } else {
                            state.currentMapLayer = layerKey
                            state.currentMapMode = MapMode.NAVIGATION
                            val script = "if (window.switchLayer) { window.switchLayer('$layerKey'); }"
                            webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                        }
                    } else {
                        state.currentMapLayer = layerKey
                        if (state.currentMapMode == MapMode.NAVIGATION) {
                            state.currentMapMode = MapMode.VELA_3D
                            webViewRef?.post { webViewRef?.evaluateJavascript("if (window.clearRoute) { window.clearRoute(); }", null) }
                        }
                        val script = "if (window.switchLayer) { window.switchLayer('$layerKey'); }"
                        webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    }
                },
                onZoomIn = {
                    state.zoomScale = (state.zoomScale * 1.3f).coerceIn(0.25f, 8.0f)
                    webViewRef?.post { webViewRef?.evaluateJavascript("if (window.zoomIn) { window.zoomIn(); }", null) }
                },
                onZoomOut = {
                    state.zoomScale = (state.zoomScale / 1.3f).coerceIn(0.25f, 8.0f)
                    webViewRef?.post { webViewRef?.evaluateJavascript("if (window.zoomOut) { window.zoomOut(); }", null) }
                },
                onRecenterLocation = {
                    state.panOffset = Offset.Zero
                    state.zoomScale = 1.0f
                    viewModel.startLocationUpdates()
                },
                isGpsActive = viewModel.isGpsTrackingActive.collectAsState().value,
                onDownloadValhallaClick = {
                    showValhallaDialog = true
                },
                // Vela Modes
                currentMode = state.currentMapMode,
                onModeChange = { mode ->
                    if (mode == MapMode.NAVIGATION && state.pendingNavDestination == null) {
                        showDestChooser = true
                    } else {
                        state.currentMapMode = mode
                        if (mode == MapMode.NAVIGATION) {
                            state.currentMapLayer = "navigation"
                            webViewRef?.post { webViewRef?.evaluateJavascript("if (window.switchLayer) { window.switchLayer('navigation'); }", null) }
                        }
                    }
                },
                tiltAngle = state.tiltAngle,
                onToggleTilt = { state.tiltAngle = if (state.tiltAngle > 0) 0f else 32f },
                onOpenAleph = {
                    state.alephSelectedEntity = state.selectedEntity
                    state.isAlephDialogOpen = true
                },
                // Advanced Local Map Tools
                isDeadZoneActive = state.isDeadZoneOverlayActive,
                onToggleDeadZoneOverlay = {
                    state.isDeadZoneOverlayActive = !state.isDeadZoneOverlayActive
                    val active = state.isDeadZoneOverlayActive
                    val script = "if (window.setDeadZones) { window.setDeadZones($active); }"
                    webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(if (active) "تم عرض مناطق التغطية الضعيفة 🔴" else "تم إخفاء التغطية الضعيفة")
                    }
                },
                isReliabilityActive = state.isReliabilityLayerActive,
                onToggleReliability = {
                    state.isReliabilityLayerActive = !state.isReliabilityLayerActive
                    val active = state.isReliabilityLayerActive
                    if (active) {
                        // بناء طبقة الموثوقية: أخضر (فنيون متاحون)، أصفر (منشغلون)، أحمر (غير مغطى)
                        val zones = providers.groupBy { it.area.ifBlank { it.cityId } }.map { (area, areaProviders) ->
                            val hasAvailable = areaProviders.any { it.isAvailable }
                            val isBusy = areaProviders.isNotEmpty() && !hasAvailable
                            val status = if (hasAvailable) "AVAILABLE" else (if (isBusy) "BUSY" else "UNCOVERED")
                            val firstProv = areaProviders.firstOrNull()
                            mapOf(
                                "lat" to (firstProv?.latitude ?: safeUserLat),
                                "lng" to (firstProv?.longitude ?: safeUserLng),
                                "areaName" to (if (area.isNotBlank()) area else "المنطقة الحالية"),
                                "status" to status,
                                "details" to "عدد الفنيين: ${areaProviders.size} • المتاحين: ${areaProviders.count { it.isAvailable }}",
                                "radius" to 1100
                            )
                        }
                        val jsonStr = org.json.JSONArray(zones).toString()
                        webViewRef?.post { webViewRef?.evaluateJavascript("if (window.setReliabilityZones) { window.setReliabilityZones($jsonStr); }", null) }
                    } else {
                        webViewRef?.post { webViewRef?.evaluateJavascript("if (window.setReliabilityZones) { window.setReliabilityZones([]); }", null) }
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(if (active) "تم تفعيل خريطة الموثوقية وتوافر الفنيين (أخضر/أصفر/أحمر) 🛡️" else "تم إيقاف خريطة الموثوقية")
                    }
                },
                onOpenRoutePlanner = {
                    state.isRoutePlannerOpen = true
                },
                isRoadSafetyActive = state.isRoadSafetyActive,
                onToggleRoadSafety = {
                    state.isRoadSafetyActive = !state.isRoadSafetyActive
                    val active = state.isRoadSafetyActive
                    if (active) {
                        // جمع المخاطر والصعوبات من مزودي الخدمات
                        val hazardsJson = filteredProviders.filter { it.roadDifficulty >= 2 }.map {
                            mapOf(
                                "lat" to it.latitude,
                                "lng" to it.longitude,
                                "difficulty" to it.roadDifficulty,
                                "title" to (if (it.roadHazardDescription.isNotBlank()) it.roadHazardDescription else "طريق جبلي / وعر"),
                                "desc" to "فني المنطقة: ${it.name} • صعوبة: ${it.roadDifficulty}/5",
                                "radius" to 600
                            )
                        }
                        val jsonStr = org.json.JSONArray(hazardsJson).toString()
                        webViewRef?.post { webViewRef?.evaluateJavascript("if (window.setRoadSafetyHazards) { window.setRoadSafetyHazards($jsonStr); }", null) }
                    } else {
                        webViewRef?.post { webViewRef?.evaluateJavascript("if (window.setRoadSafetyHazards) { window.setRoadSafetyHazards([]); }", null) }
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(if (active) "تم تفعيل طبقة سلامة الطريق وصعوبات الطرق اليمنية ⚠️" else "تم إخفاء طبقة سلامة الطرق")
                    }
                },
                onOpenAddOsmLandmark = {
                    state.isAddOsmDialogOpen = true
                },
                onTriggerRuralSos = {
                    val packet = com.serviseyem.utils.RuralEmergencySosManager.EmergencyPacket(
                        latitude = safeUserLat,
                        longitude = safeUserLng,
                        nearestTown = state.selectedCity
                    )
                    com.serviseyem.utils.RuralEmergencySosManager.sendEmergencySms(context, "199", packet) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("تم تجهيز رسالة النجدة الريفية عبر SMS 🚨")
                        }
                    }
                },
                isNightMode = state.isNightModeActive,
                onToggleNightMode = {
                    state.isNightModeActive = !state.isNightModeActive
                    val active = state.isNightModeActive
                    val script = "if (window.toggleNightMode) { window.toggleNightMode($active); }"
                    webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(if (active) "تم تفعيل الوضع الليلي للخريطة 🌙" else "تم إيقاف الوضع الليلي والعودة للوضع النهاري ☀️")
                    }
                },
                isNasaGibsActive = state.isNasaGibsActive,
                onToggleNasaGibs = {
                    state.isNasaGibsActive = !state.isNasaGibsActive
                    val active = state.isNasaGibsActive
                    if (active) {
                        state.isEoxCloudlessActive = false
                        state.isEsriImageryActive = false
                    }
                    val script = "if (window.toggleNasaGibsLayer) { window.toggleNasaGibsLayer($active); } if (window.toggleEoxCloudlessLayer) { window.toggleEoxCloudlessLayer(false); } if (window.toggleEsriWorldImageryLayer) { window.toggleEsriWorldImageryLayer(false); }"
                    webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            if (active) "تم تفعيل طبقة أقمار NASA GIBS الحية (شبه فورية) 🛰️"
                            else "تم إيقاف طبقة NASA GIBS والعودة للطبقة الافتراضية"
                        )
                    }
                },
                isEoxCloudlessActive = state.isEoxCloudlessActive,
                onToggleEoxCloudless = {
                    state.isEoxCloudlessActive = !state.isEoxCloudlessActive
                    val active = state.isEoxCloudlessActive
                    if (active) {
                        state.isNasaGibsActive = false
                        state.isEsriImageryActive = false
                    }
                    val script = "if (window.toggleEoxCloudlessLayer) { window.toggleEoxCloudlessLayer($active); } if (window.toggleNasaGibsLayer) { window.toggleNasaGibsLayer(false); } if (window.toggleEsriWorldImageryLayer) { window.toggleEsriWorldImageryLayer(false); }"
                    webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            if (active) "تم تفعيل طبقة EOX Cloudless عالية الدقة (10 أمتار) 🛰️"
                            else "تم إيقاف طبقة EOX Cloudless والعودة للطبقة الافتراضية"
                        )
                    }
                },
                isEsriImageryActive = state.isEsriImageryActive,
                onToggleEsriImagery = {
                    state.isEsriImageryActive = !state.isEsriImageryActive
                    val active = state.isEsriImageryActive
                    if (active) {
                        state.isNasaGibsActive = false
                        state.isEoxCloudlessActive = false
                    }
                    val script = "if (window.toggleEsriWorldImageryLayer) { window.toggleEsriWorldImageryLayer($active); } if (window.toggleNasaGibsLayer) { window.toggleNasaGibsLayer(false); } if (window.toggleEoxCloudlessLayer) { window.toggleEoxCloudlessLayer(false); }"
                    webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            if (active) "تم تفعيل طبقة Esri عالية الدقة والوضوح (مجانية 100%) 🛰️"
                            else "تم إيقاف طبقة Esri والعودة للطبقة الافتراضية"
                        )
                    }
                },
                isEarthquakeActive = state.isEarthquakeLayerActive,
                onToggleEarthquake = {
                    state.isEarthquakeBottomSheetOpen = true
                },
                isGroupActive = state.isGroupTrackingActive,
                onToggleGroupTracking = {
                    state.isGroupTrackingActive = !state.isGroupTrackingActive
                },
                onOpenGnssDiagnostics = {
                    state.isGnssPanelOpen = true
                },
                onExportGpx = {
                    val defaultRoute = listOf(
                        Pair(safeUserLat, safeUserLng),
                        Pair(safeUserLat + 0.01, safeUserLng + 0.01)
                    )
                    val result = GpxManager.exportRouteToGpx(context, "مسار_الخدمات_${state.selectedCity}", defaultRoute)
                    result.onSuccess { gpxFile ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("تم تصدير مسار GPX بنجاح: ${gpxFile.name} 🛰️")
                        }
                    }.onFailure {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("فشل تصدير مسار GPX")
                        }
                    }
                },
                onPrintMapPdf = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("جاري تحضير ملخص الخريطة المطبوع 📄")
                    }
                },
                themeColors = themeColors,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            )

            // BackHandler for Navigation mode
            androidx.activity.compose.BackHandler(enabled = state.currentMapMode == MapMode.NAVIGATION) {
                state.currentMapMode = MapMode.VELA_3D
                val script = "if (window.clearRoute) { window.clearRoute(); }"
                webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
            }

            // 🎯 شريط تأكيد اختيار نقطة الوجهة من الخريطة مباشرة
            if (pickingDestination) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xF50F172A),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth()
                        .testTag("map_pick_confirm_card")
                ) {
                    val pt = pickedPoint
                    if (pt == null) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "اضغط على أي موقع على الخريطة لتحديده كوجهة 🎯",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { pickingDestination = false },
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("إلغاء", color = Color(0xFFEF4444))
                            }
                        }
                    } else {
                        val (la, lo) = pt
                        val nearest = remember(la, lo) { com.serviseyem.utils.OfflinePlacesDb.nearest(context, la, lo) }
                        val label = nearest?.let { "قرب $it" } ?: "نقطة على الخريطة"
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "الوجهة: $label",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${"%.4f".format(java.util.Locale.US, la)}, ${"%.4f".format(java.util.Locale.US, lo)}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        pickingDestination = false
                                        pickedPoint = null
                                        startDestinationNavigation(label, la, lo)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("map_pick_start_nav_button")
                                ) {
                                    Text("ابدأ الملاحة", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        pickingDestination = false
                                        pickedPoint = null
                                    },
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("map_pick_cancel_button")
                                ) {
                                    Text("إلغاء", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // 🧭 Dedicated Smart Navigation Panel (Dual: Inter-City Highways & Urban Street Precision)
            if (state.currentMapMode == MapMode.NAVIGATION && !pickingDestination) {
                com.serviseyem.ui.screens.map.components.SmartNavigationPanel(
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
                        val script = "if (window.clearRoute) { window.clearRoute(); }"
                        webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    },
                    onCloseNavigation = {
                        state.currentMapMode = MapMode.VELA_3D
                        state.currentMapLayer = "google_satellite"
                        state.pendingNavDestination = null
                        val script = "if (window.clearRoute) { window.clearRoute(); } if (window.switchLayer) { window.switchLayer('google_satellite'); }"
                        webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    },
                    onPromptDownloadOffline = {
                        showValhallaDialog = true
                    },
                    onOpenDestinationChooser = {
                        showDestChooser = true
                    },
                    onOpenPlacesDownload = {
                        showPlacesDownload = true
                    },
                    onLaunchTurnByTurn = { la, lo, destTitle ->
                        viewModel.openNavigation(la, lo, destTitle)
                    },
                    offlineStateKey = offlinePackVersion,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }

            // Bottom Sheet for selected item
            state.selectedEntity?.let { entity ->
                MapBottomSheet(
                    entity = entity,
                    userLat = safeUserLat,
                    userLng = safeUserLng,
                    onDismiss = { state.selectedEntity = null },
                    onRequestBooking = { target ->
                        state.selectedEntity = null
                        when (target) {
                            is ProviderEntity -> {
                                state.bookingProviderTarget = target
                            }
                            is StoreEntity -> {
                                onRequestBooking(target)
                            }
                            is PropertyEntity -> {
                                onRequestBooking(target)
                            }
                            else -> onRequestBooking(target)
                        }
                    },
                    onOpenDetails = { ent ->
                        state.selectedEntity = null
                        when (ent) {
                            is ProviderEntity -> onOpenProviderDetails(ent)
                            is StoreEntity -> onOpenStoreDetails(ent)
                            is PropertyEntity -> onOpenPropertyDetails(ent)
                        }
                    },
                    onOpenChat = { ent ->
                        state.selectedEntity = null
                        when (ent) {
                            is ProviderEntity -> {
                                viewModel.openDirectChat(
                                    targetUserId = ent.id.ifBlank { ent.phone },
                                    targetUserName = ent.name,
                                    targetUserPhoto = ent.profileImage,
                                    relatedEntityId = ent.id,
                                    relatedEntityType = "PROVIDER"
                                )
                            }
                            is StoreEntity -> {
                                val isMedical = ent.sectionId.contains("medical") || ent.categoryId.contains("medical") || ent.name.contains("طبي") || ent.name.contains("صيدلية")
                                val isRestaurant = !isMedical && (ent.sectionId.contains("restaurant") || ent.categoryId.contains("restaurant") || ent.name.contains("مطعم") || ent.name.contains("كافيه"))
                                val entityType = when {
                                    isMedical -> "MEDICAL"
                                    isRestaurant -> "RESTAURANT"
                                    else -> "STORE"
                                }
                                viewModel.openDirectChat(
                                    targetUserId = ent.id,
                                    targetUserName = ent.name,
                                    targetUserPhoto = ent.logoImage.ifBlank { ent.coverImage },
                                    relatedEntityId = ent.id,
                                    relatedEntityType = entityType
                                )
                            }
                            is PropertyEntity -> {
                                viewModel.openDirectChat(
                                    targetUserId = ent.id,
                                    targetUserName = ent.title,
                                    targetUserPhoto = ent.images.firstOrNull() ?: "",
                                    relatedEntityId = ent.id,
                                    relatedEntityType = "PROPERTY"
                                )
                            }
                        }
                    },
                    onOpenAleph = { ent ->
                        state.alephSelectedEntity = ent
                        state.isAlephDialogOpen = true
                    },
                    onStartNavigation = { targetLat, targetLng, destTitle ->
                        viewModel.openNavigation(targetLat, targetLng, destTitle)
                    },
                    themeColors = themeColors,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .imePadding()
                )
            }
        }
    }

    // 👁️ المساعد البصري الذكي Aleph Dialog
    if (state.isAlephDialogOpen) {
        AlephVisualAssistantDialog(
            targetEntity = state.alephSelectedEntity ?: state.selectedEntity,
            userLat = safeUserLat,
            userLng = safeUserLng,
            onDismiss = { state.isAlephDialogOpen = false },
            onOpenBooking = { target ->
                state.isAlephDialogOpen = false
                target?.let { onRequestBooking(it) }
            },
            themeColors = themeColors
        )
    }

    // Unified Direct Booking Dialog
    state.bookingProviderTarget?.let { provider ->
        BookingDialog(
            provider = provider,
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { state.bookingProviderTarget = null }
        )
    }

    // 🎯 لوحة اختيار الوجهة الثلاثية (بحث، خريطة، مدن، عناوين، أخيرة، إحداثيات)
    if (showDestChooser) {
        DestinationChooserSheet(
            userLat = safeUserLat,
            userLng = safeUserLng,
            hasRealLocation = hasRealLocation,
            providers = providers,
            stores = stores,
            savedAddresses = savedAddresses,
            onPick = { name, la, lo ->
                showDestChooser = false
                startDestinationNavigation(name, la, lo)
            },
            onPickFromMap = {
                showDestChooser = false
                state.isRadarMode = false
                pickedPoint = null
                pickingDestination = true
            },
            onDownloadPlaces = {
                showDestChooser = false
                showPlacesDownload = true
            },
            onDismiss = { showDestChooser = false },
            selectedCity = state.selectedCity,
            onAddressSaved = {
                savedAddresses = addressStore.all()
            }
        )
    }

    // 📥 Valhalla Offline Navigation Download & Storage Import Dialog
    if (showValhallaDialog) {
        ValhallaOfflineDialog(
            onDismiss = { showValhallaDialog = false },
            onStartNavigation = {
                offlinePackVersion++
                if (state.pendingNavDestination == null) {
                    showDestChooser = true
                } else {
                    state.currentMapMode = MapMode.NAVIGATION
                    state.currentMapLayer = "navigation"
                    webViewRef?.post { webViewRef?.evaluateJavascript("if (window.switchLayer) { window.switchLayer('navigation'); }", null) }
                }
            },
            onOpenPlacesDownload = {
                showPlacesDownload = true
            },
            onStatusMessage = { msg ->
                offlinePackVersion++
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        )
    }

    // 📍 حوار تحميل/استيراد قاعدة الأماكن المحلية للبحث بلا إنترنت
    if (showPlacesDownload) {
        PlacesDownloadDialog(
            onDismiss = { showPlacesDownload = false },
            onCompleted = { msg ->
                offlinePackVersion++
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        )
    }

    // 🛰️ GNSS Live Satellite Diagnostics Overlay Modal
    if (state.isGnssPanelOpen) {
        GnssSignalPanel(
            statusSummary = satelliteStatus,
            themeColors = themeColors,
            onDismiss = { state.isGnssPanelOpen = false }
        )
    }

    // 👥 Group Tracking Modal
    if (state.isGroupTrackingActive) {
        GroupTrackingDialog(
            isGroupActive = state.currentGroupCode.isNotBlank(),
            currentGroupCode = state.currentGroupCode,
            onStartGroupSession = { code ->
                state.currentGroupCode = code
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تفعيل تتبع المجموعة: $code 👥")
                }
            },
            onStopGroupSession = {
                state.currentGroupCode = ""
                state.isGroupTrackingActive = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم إيقاف تتبع المجموعة 🔴")
                }
            },
            themeColors = themeColors,
            onDismiss = { state.isGroupTrackingActive = false }
        )
    }

    // 🏅 الفكرة الرابعة: نافذة إضافة معلم يمني لتوأمة OpenStreetMap
    if (state.isAddOsmDialogOpen) {
        AddOsmLandmarkDialog(
            userLat = safeUserLat,
            userLng = safeUserLng,
            currentCity = state.selectedCity,
            themeColors = themeColors,
            onSaveLandmark = { node ->
                com.serviseyem.utils.OsmContributionManager.savePendingLandmark(context, node)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم حفظ المعلم [${node.name}] بنجاح ومشاركته في خريطة اليمن 🌟")
                }
            },
            onDismiss = { state.isAddOsmDialogOpen = false }
        )
    }

    // 🎖️ الفكرة الخامسة: نافذة تخطيط المسارات متعددة المحطات (Route Planner)
    if (state.isRoutePlannerOpen) {
        RoutePlannerDialog(
            userLat = safeUserLat,
            userLng = safeUserLng,
            currentCity = state.selectedCity,
            themeColors = themeColors,
            onCalculatePlannedRoute = { start, stops, dest ->
                state.currentMapMode = MapMode.NAVIGATION
                state.currentMapLayer = "navigation"
                val startCoords = OfflineMapManager.getCityCoordinates(start)
                val destCoords = OfflineMapManager.getCityCoordinates(dest)
                val script = "if (window.drawRoute) { window.drawRoute(${startCoords.latitude}, ${startCoords.longitude}, ${destCoords.latitude}, ${destCoords.longitude}, '#00E5FF', 'مسار $start إلى $dest'); }"
                webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تخطيط ورسم المسار من $start عبر ${stops.joinToString()} إلى $dest 🛣️")
                }
            },
            onDismiss = { state.isRoutePlannerOpen = false }
        )
    }

    // 🌋 نافذة التحكم بطبقة الزلازل الحية
    if (state.isEarthquakeBottomSheetOpen) {
        EarthquakeBottomSheet(
            isActive = state.isEarthquakeLayerActive,
            onToggleActive = { active ->
                state.isEarthquakeLayerActive = active
                if (active) {
                    state.isEarthquakeLoading = true
                    state.earthquakeUpdateTrigger++
                }
            },
            lastUpdateTime = state.lastEarthquakeUpdateTime,
            earthquakeCount = state.earthquakeCount,
            isLoading = state.isEarthquakeLoading,
            onRefreshNow = {
                state.isEarthquakeLoading = true
                state.isEarthquakeLayerActive = true
                state.earthquakeUpdateTrigger++
            },
            themeColors = themeColors,
            onDismiss = { state.isEarthquakeBottomSheetOpen = false }
        )
    }
}

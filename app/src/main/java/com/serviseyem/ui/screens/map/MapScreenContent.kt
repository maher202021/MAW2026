@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.map
import com.serviseyem.ui.*

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
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
    var isDownloadingValhalla by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadStatusText by remember { mutableStateOf("") }

    androidx.activity.compose.BackHandler(enabled = true) {
        if (state.selectedEntity != null) {
            state.resetSelection()
        } else {
            onBackClick()
        }
    }

    // Data streams from ViewModel
    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()

    val userLatState by viewModel.userLatitude.collectAsState()
    val userLngState by viewModel.userLongitude.collectAsState()

    // إصلاح 1.1: عند عدم توفر GPS حقيقي نستخدم مركز المدينة المحددة حالياً بدلاً من إحداثيات ثابتة قديمة
    val fallbackCityCoords = remember(state.selectedCity) {
        OfflineMapManager.getCityCoordinates(state.selectedCity)
    }
    val safeUserLat = if (userLatState != 0.0 && !userLatState.isNaN()) userLatState else fallbackCityCoords.latitude
    val safeUserLng = if (userLngState != 0.0 && !userLngState.isNaN()) userLngState else fallbackCityCoords.longitude

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

    LaunchedEffect(Unit) {
        gpsFusionFilter.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            gpsFusionFilter.stop()
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
                .background(Color(0xFF0B0F19))
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
                    
                    // عرض تفاصيل العنصر فوراً إذا كان كياناً
                    when (val item = result.originalItem) {
                        is ProviderEntity -> state.selectedEntity = item
                        is StoreEntity -> state.selectedEntity = item
                        is PropertyEntity -> state.selectedEntity = item
                    }
                },
                providers = providers,
                stores = stores,
                properties = properties,
                onBackClick = onBackClick,
                themeColors = themeColors,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            )

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
                    state.currentMapLayer = layerKey
                    if (layerKey == "vela_3d") {
                        state.currentMapMode = MapMode.VELA_3D
                        state.tiltAngle = 42f
                    } else {
                        state.currentMapMode = MapMode.AERIAL_OAM
                        state.tiltAngle = 0f
                    }
                    if (layerKey == "navigation") state.currentMapMode = MapMode.NAVIGATION
                    val script = """
                        if (window.switchLayer) { window.switchLayer('$layerKey'); }
                        if (window.setTilt) { window.setTilt(${state.tiltAngle}); }
                        if (window.setVelaMode) { window.setVelaMode('${state.currentMapMode.name}'); }
                    """.trimIndent()
                    webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
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
                    state.currentMapMode = MapMode.NAVIGATION
                    state.currentMapLayer = "navigation"
                    showValhallaDialog = true
                },
                // Vela Modes
                currentMode = state.currentMapMode,
                onModeChange = { mode -> 
                    state.currentMapMode = mode
                    if (mode == MapMode.VELA_3D) {
                        state.tiltAngle = 42f
                        webViewRef?.post { webViewRef?.evaluateJavascript("if (window.setTilt) { window.setTilt(42); }", null) }
                    } else {
                        state.tiltAngle = 0f
                        webViewRef?.post { webViewRef?.evaluateJavascript("if (window.setTilt) { window.setTilt(0); }", null) }
                    }
                },
                tiltAngle = state.tiltAngle,
                onToggleTilt = {
                    val next = if (state.tiltAngle > 0) 0f else 42f
                    state.tiltAngle = next
                    webViewRef?.post { webViewRef?.evaluateJavascript("if (window.setTilt) { window.setTilt($next); }", null) }
                },
                onCycleTiltAngle = {
                    val next = when (state.tiltAngle) {
                        0f -> 30f
                        30f -> 45f
                        45f -> 60f
                        else -> 0f
                    }
                    state.tiltAngle = next
                    webViewRef?.post { webViewRef?.evaluateJavascript("if (window.setTilt) { window.setTilt($next); }", null) }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("زاوية الرؤية ثلاثية الأبعاد: ${next.toInt()}° 📐")
                    }
                },
                onResetNorth = {
                    state.panOffset = Offset.Zero
                    webViewRef?.post { webViewRef?.evaluateJavascript("if (window.recenterUser) { window.recenterUser(); }", null) }
                },
                onToggleTraffic = {
                    val target = if (state.currentMapLayer == "hot") "satellite" else "hot"
                    state.currentMapLayer = target
                    webViewRef?.post { webViewRef?.evaluateJavascript("if (window.switchLayer) { window.switchLayer('$target'); }", null) }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(if (target == "hot") "تم تفعيل خريطة الشوارع الحية 🗺️" else "تم العودة للأقمار الصناعية 🛰️")
                    }
                },
                onOpenStreetView = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("عرض بانورامي تفاعلي 360° للموقع 🌐")
                    }
                },
                onOpenAleph = {
                    state.alephSelectedEntity = state.selectedEntity
                    state.isAlephDialogOpen = true
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

            // 🧭 Dedicated Smart Navigation Panel (Dual: Inter-City Highways & Urban Street Precision)
            if (state.currentMapMode == MapMode.NAVIGATION) {
                com.serviseyem.ui.screens.map.components.SmartNavigationPanel(
                    userLat = safeUserLat,
                    userLng = safeUserLng,
                    themeColors = themeColors,
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
                        val script = "if (window.clearRoute) { window.clearRoute(); } if (window.switchLayer) { window.switchLayer('google_satellite'); }"
                        webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                    },
                    onPromptDownloadOffline = {
                        showValhallaDialog = true
                    },
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

    // 📥 Valhalla Offline Navigation Download Dialog
    if (showValhallaDialog) {
        val isAlreadyDownloaded = remember(showValhallaDialog) {
            com.serviseyem.utils.ValhallaDownloadManager.isDownloaded(context)
        }

        AlertDialog(
            onDismissRequest = { if (!isDownloadingValhalla) showValhallaDialog = false },
            title = {
                Text(
                    text = if (isAlreadyDownloaded) "🧭 الملاحة الدقيقة (أوفلاين) جاهزة" else "📥 تحميل الملاحة الدقيقة الكاملة (أوفلاين)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isAlreadyDownloaded && !isDownloadingValhalla) {
                        Text(
                            text = "ملف خرائط ومسارات الملاحة الدقيقة (25 ميجابايت) محفوظ مسبقاً على هذا الهاتف وجاهز للاستخدام أوفلاين دون إنترنت.\n\nيمكنك بدء الملاحة الآن فوراً، أو إعادة تنزيل أحدث ملف خرائط.",
                            fontSize = 12.sp,
                            color = Color(0xFFA7F3D0)
                        )
                    } else {
                        Text(
                            text = "لتفعيل الملاحة الدقيقة والمسارات التفصيلية داخل شوارع اليمن، يحتاج التطبيق إلى تحميل ملف خرائط أوفلاين (حوالي 25 ميجابايت). يُنصح باستخدام شبكة Wi-Fi.",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                    
                    if (isDownloadingValhalla) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Color(0xFF00E5FF),
                            trackColor = Color(0xFF1E293B)
                        )
                        Text(downloadStatusText, fontSize = 11.sp, color = Color(0xFF00E5FF))
                    }
                }
            },
            confirmButton = {
                if (!isDownloadingValhalla) {
                    if (isAlreadyDownloaded) {
                        Button(
                            onClick = {
                                state.currentMapMode = MapMode.NAVIGATION
                                state.currentMapLayer = "navigation"
                                val script = "if (window.switchLayer) { window.switchLayer('navigation'); }"
                                webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                                showValhallaDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("🧭 تم فتح شاشة الملاحة الدقيقة أوفلاين بنجاح")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("بدء الملاحة الآن 🧭", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                isDownloadingValhalla = true
                                downloadStatusText = "جاري بدء التحميل..."
                                coroutineScope.launch {
                                    val prefs = context.getSharedPreferences("valhalla_prefs", android.content.Context.MODE_PRIVATE)
                                    val adminUrl = prefs.getString("valhalla_url", "") ?: ""
                                    val success = com.serviseyem.utils.ValhallaDownloadManager.downloadAndExtractOfflineTiles(context, adminUrl) { prog, msg ->
                                        downloadProgress = prog
                                        downloadStatusText = msg
                                    }
                                    isDownloadingValhalla = false
                                    showValhallaDialog = false
                                    if (success) {
                                        state.currentMapMode = MapMode.NAVIGATION
                                        state.currentMapLayer = "navigation"
                                        val script = "if (window.switchLayer) { window.switchLayer('navigation'); }"
                                        webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                                        snackbarHostState.showSnackbar("تم تحميل وحفظ الملاحة بنجاح ✓ تم فتح شاشة الملاحة")
                                    } else {
                                        snackbarHostState.showSnackbar("فشل التحميل، يرجى التحقق من الاتصال")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("تحميل الآن", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            dismissButton = {
                if (!isDownloadingValhalla) {
                    if (isAlreadyDownloaded) {
                        TextButton(
                            onClick = {
                                isDownloadingValhalla = true
                                downloadStatusText = "جاري إعادة التحميل..."
                                coroutineScope.launch {
                                    com.serviseyem.utils.ValhallaDownloadManager.clearOfflineData(context)
                                    val prefs = context.getSharedPreferences("valhalla_prefs", android.content.Context.MODE_PRIVATE)
                                    val adminUrl = prefs.getString("valhalla_url", "") ?: ""
                                    val success = com.serviseyem.utils.ValhallaDownloadManager.downloadAndExtractOfflineTiles(context, adminUrl) { prog, msg ->
                                        downloadProgress = prog
                                        downloadStatusText = msg
                                    }
                                    isDownloadingValhalla = false
                                    showValhallaDialog = false
                                    if (success) {
                                        state.currentMapMode = MapMode.NAVIGATION
                                        state.currentMapLayer = "navigation"
                                        val script = "if (window.switchLayer) { window.switchLayer('navigation'); }"
                                        webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                                        snackbarHostState.showSnackbar("تم تحديث وحفظ خرائط الملاحة بنجاح ✓")
                                    }
                                }
                            }
                        ) {
                            Text("إعادة التنزيل 🔄", color = Color(0xFF00E5FF), fontSize = 12.sp)
                        }
                    } else {
                        TextButton(onClick = { showValhallaDialog = false }) {
                            Text("لاحقاً", color = Color.Gray)
                        }
                    }
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

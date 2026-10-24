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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color(0xFF0F172A),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White,
                    actionColor = Color(0xFF00E5FF),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
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
                            state.selectedEntity = item.originalItem
                        },
                        isHeatmapActive = state.isHeatmapActive,
                        maxRangeKm = state.maxRangeKm,
                        modifier = Modifier.fillMaxSize()
                    )

                    // زر التبديل الاختياري إلى الخريطة الكاملة في الأسفل عند عدم وجود عنصر محدد
                    if (state.selectedEntity == null) {
                        Button(
                            onClick = {
                                showMapErrorOverlay = false
                                state.isRadarMode = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 24.dp)
                        ) {
                            Text("🗺️ تبديل إلى خريطة اليمن الكاملة الحية", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            } else if (state.currentMapMode == MapMode.AR_VPS) {
                // 📷 الوضع 3: كاميرا الواقع المعزز والرؤية الحاسوبية (VPS AR Mode)
                VpsArCameraView(
                    userLat = safeUserLat,
                    userLng = safeUserLng,
                    nearbyProviders = filteredProviders,
                    nearbyStores = filteredStores,
                    nearbyProperties = filteredProperties,
                    onEntitySelected = { state.selectedEntity = it },
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
                        onProviderSelected = { state.selectedEntity = it },
                        onStoreSelected = { state.selectedEntity = it },
                        onPropertySelected = { state.selectedEntity = it },
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

            // [تعديل] شارة مصدر الخريطة والنقاط المكتشفة كالصورة المرفقة تماماً
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, bottom = if (state.selectedEntity != null) 220.dp else 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), androidx.compose.foundation.shape.CircleShape)
                    )
                    Text(
                        text = if (state.isRadarMode) "رادار كشف الخدمات · ${radarPoints.size} نقطة مكتشفة" else "OpenStreetMap · ${radarPoints.size} نقطة مكتشفة",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }


            // Top Header & Filter Bar
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .statusBarsPadding()
            ) {
                // Top Bar with Back Button
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier.testTag("map_back_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                            }
                            Text(
                                if (state.isRadarMode) "🛰️ رادار الخدمات المحلي (أوفلاين)" else "🗺️ خريطة دليل اليمن المباشرة",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Entity Count Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${radarPoints.size} متوفر",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Filter Bar
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
                    themeColors = themeColors
                )

            }

            // 🎛️ مركز التحكم الموحد - عمودي على الجانب (Vela 3D + OAM + AR VPS + Aleph Vision)
            if (state.currentMapMode != MapMode.AR_VPS) {
                MapControlCenter(
                    currentMode = state.currentMapMode,
                    onModeChange = { mode ->
                        state.currentMapMode = mode
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("تم تفعيل ${mode.label} (${mode.icon})")
                        }
                    },
                    tiltAngle = state.tiltAngle,
                    onToggleTilt = {
                        state.tiltAngle = if (state.tiltAngle > 0) 0f else 32f
                    },
                    onOpenAleph = {
                        state.alephSelectedEntity = state.selectedEntity
                        state.isAlephDialogOpen = true
                    },
                    themeColors = themeColors,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 230.dp, start = 12.dp)
                )
            }

            // Map Controls (Right / Floating)
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
                            if (state.isHeatmapActive) "تم تفعيل الخريطة الحرارية لكثافة الخدمات 🔥" else "تم إيقاف الخريطة الحرارية"
                        )
                    }
                },
                currentLayer = state.currentMapLayer,
                onSwitchLayer = { layerKey ->
                    state.currentMapLayer = layerKey
                    val script = "if (window.switchLayer) { window.switchLayer('$layerKey'); }"
                    webViewRef?.post {
                        webViewRef?.evaluateJavascript(script, null)
                    }
                    coroutineScope.launch {
                        val layerName = when(layerKey) {
                            "google_satellite" -> "الأقمار الصناعية"
                            "osm" -> "خريطة الشوارع العادية"
                            "navigation" -> "طبقة الملاحة والمسارات 🧭"
                            "streets" -> "خرائط الطرق الرئيسية"
                            else -> layerKey
                        }
                        snackbarHostState.showSnackbar("تم التبديل إلى: $layerName")
                    }
                },
                onZoomIn = {
                    state.zoomScale = (state.zoomScale * 1.30f).coerceIn(0.25f, 8.0f)
                },
                onZoomOut = {
                    state.zoomScale = (state.zoomScale / 1.30f).coerceIn(0.25f, 8.0f)
                },
                onRecenterLocation = {
                    state.panOffset = Offset.Zero
                    state.zoomScale = 1.0f
                    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (hasFine || hasCoarse) {
                        viewModel.startLocationUpdates()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("تمت إعادة التمركز إلى موقعك الفعلي 📍")
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
                },
                isGpsActive = viewModel.isGpsTrackingActive.collectAsState().value,
                onDownloadValhallaClick = { showValhallaDialog = true },
                themeColors = themeColors,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .navigationBarsPadding()
                    .padding(top = 230.dp, end = 12.dp, bottom = if (state.selectedEntity != null) 200.dp else 20.dp)
            )

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
        AlertDialog(
            onDismissRequest = { if (!isDownloadingValhalla) showValhallaDialog = false },
            title = { Text("📥 تحميل الملاحة الدقيقة الكاملة (أوفلاين)", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("لتفعيل الملاحة الدقيقة والمسارات التفصيلية داخل شوارع اليمن، يحتاج التطبيق إلى تحميل ملف خرائط أوفلاين (حوالي 25 ميجابايت). يُنصح باستخدام شبكة Wi-Fi.", fontSize = 12.sp, color = Color.LightGray)
                    
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
                                snackbarHostState.showSnackbar(if (success) "تم تحميل وتفعيل الملاحة الدقيقة بنجاح ✓" else "فشل التحميل، يرجى التحقق من الاتصال")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("تحميل الآن", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isDownloadingValhalla) {
                    TextButton(onClick = { showValhallaDialog = false }) {
                        Text("لاحقاً", color = Color.Gray)
                    }
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

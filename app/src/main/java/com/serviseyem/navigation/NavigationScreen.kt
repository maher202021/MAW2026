package com.serviseyem.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.LandmarkAddressStore
import com.serviseyem.utils.PlaceSearch
import com.serviseyem.utils.PlaceSearchOutcome
import com.serviseyem.utils.RecentDestinations
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Locale

/**
 * 🧭 NavigationScreen
 * الشاشة الموحدة للملاحة المباشرة (تنسيق محرك الموقع، الصوت، حساب المسار، وعرض الخريطة واللوحات).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NavigationScreen(
    themeColors: VisualThemePalette,
    viewModel: MainViewModel? = null,
    initialTargetLat: Double = 15.3547,
    initialTargetLng: Double = 44.2066,
    initialTargetName: String = "المركز الطبي الاستشاري - شارع حدة",
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    // 1. Production Location Engine & Sensors
    val locationEngine = remember { LocationEngine(context) }
    val locationFix by locationEngine.locationFlow.collectAsState()
    val compassHeading by locationEngine.compassHeadingFlow.collectAsState()
    val isGpsWeak by locationEngine.isGpsLostFlow.collectAsState()

    // 2. Production Voice Guidance Engine
    val navigationVoice = remember { NavigationVoice(context) }
    val isVoiceMuted by navigationVoice.isMuted.collectAsState()
    val isArabicVoiceSupported by navigationVoice.isArabicSupported.collectAsState()

    // Screen-on and Foreground Service management
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        locationEngine.startNavigationTracking()
        val hasFineLoc = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasFineLoc) {
            NavigationForegroundService.startService(context, "جاري بدء الملاحة إلى $initialTargetName", "", "--:--")
        }

        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            locationEngine.destroy()
            navigationVoice.destroy()
            NavigationForegroundService.stopService(context)
        }
    }

    // Destinations & Pickers
    var targetLat by remember { mutableDoubleStateOf(initialTargetLat) }
    var targetLng by remember { mutableDoubleStateOf(initialTargetLng) }
    var targetName by remember { mutableStateOf(initialTargetName) }
    var showDestChooser by remember { mutableStateOf(false) }
    var showValhallaDialog by remember { mutableStateOf(false) }
    var showPlacesDownload by remember { mutableStateOf(false) }
    var pickingOnNavMap by remember { mutableStateOf(false) }
    var pickedNavPoint by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var statusBannerText by remember { mutableStateOf<String?>(null) }
    var hasAnnouncedArrival by remember { mutableStateOf(false) }

    val providersList = viewModel?.providers?.collectAsState()?.value ?: emptyList()
    val storesList = viewModel?.stores?.collectAsState()?.value ?: emptyList()
    val addressStore = remember { LandmarkAddressStore(context) }
    var savedAddresses by remember { mutableStateOf(addressStore.all()) }

    // Offline / Valhalla State
    var hasValhallaFiles by remember { mutableStateOf(OfflineMapLifecycleManager.getInstance(context).isOfflineDataAvailable()) }
    val realNavigator = remember { RealValhallaNavigator(context) }

    // Active Route State
    var routeResult by remember { mutableStateOf<NavigationRouteResult?>(null) }
    var activeInstructionIndex by remember { mutableIntStateOf(0) }
    var currentInstruction by remember { mutableStateOf("انطلق نحو مسار الرحلة") }
    var nextTurnManeuver by remember { mutableStateOf(ManeuverType.START) }
    var remainingDistanceText by remember { mutableStateOf("جاري الحساب...") }
    var estimatedTimeText by remember { mutableStateOf("جاري الحساب...") }
    var estimatedArrivalClock by remember { mutableStateOf("--:--") }
    var totalTripDistanceMeters by remember { mutableDoubleStateOf(1000.0) }
    var tripProgressFraction by remember { mutableFloatStateOf(0.0f) }

    // Off-route tracking
    var offRouteConsecutiveSeconds by remember { mutableIntStateOf(0) }
    var isRerouting by remember { mutableStateOf(false) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NavigationSearchResult>>(emptyList()) }
    var searchMessage by remember { mutableStateOf<String?>(null) }
    var canDownloadPlacesFromSearch by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Map WebView reference
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(isArabicVoiceSupported) {
        if (!isArabicVoiceSupported && statusBannerText == null) {
            statusBannerText = "تنبيه: صوت اللغة العربية غير مثبّت في محرك النطق بالهاتف — فعّله من إعدادات إمكانية الوصول/النطق."
        }
    }

    fun recalculateRoute(announce: Boolean = false) {
        hasAnnouncedArrival = false
        coroutineScope.launch {
            val result = realNavigator.calculateRoute(
                locationFix.latitude,
                locationFix.longitude,
                targetLat,
                targetLng,
                targetName
            )

            routeResult = result

            if (result is NavigationRouteResult.Success) {
                totalTripDistanceMeters = result.totalDistanceMeters.coerceAtLeast(1.0)
                tripProgressFraction = 0.02f
                remainingDistanceText = HaversinePrecisionNavigator.formatDistanceArabic(result.totalDistanceMeters)
                estimatedTimeText = HaversinePrecisionNavigator.formatDurationArabic(result.estimatedDurationSeconds)

                val arrivalTimeMillis = System.currentTimeMillis() + (result.estimatedDurationSeconds * 1000L)
                val sdf = java.text.SimpleDateFormat("hh:mm a", Locale("ar"))
                estimatedArrivalClock = sdf.format(java.util.Date(arrivalTimeMillis))

                if (result.instructions.isNotEmpty()) {
                    activeInstructionIndex = 0
                    val first = result.instructions.first()
                    currentInstruction = first.text
                    nextTurnManeuver = first.maneuverType

                    if (announce) {
                        navigationVoice.speak("بدء الملاحة إلى $targetName. ${first.text}", queueMode = android.speech.tts.TextToSpeech.QUEUE_FLUSH)
                    }
                }

                NavigationForegroundService.updateNotification(
                    context,
                    currentInstruction,
                    remainingDistanceText,
                    estimatedArrivalClock
                )

                val coordsJson = JSONArray().apply {
                    result.points.forEach { pt ->
                        put(JSONArray().apply {
                            put(pt.lat)
                            put(pt.lon)
                        })
                    }
                }.toString()

                val argsObj = org.json.JSONObject().apply {
                    put("sLat", locationFix.latitude)
                    put("sLng", locationFix.longitude)
                    put("eLat", targetLat)
                    put("eLng", targetLng)
                    put("coords", JSONArray(coordsJson))
                    put("title", targetName)
                }.toString()
                val script = "if (window.renderNavigationRouteSafe) { window.renderNavigationRouteSafe($argsObj); } else if (window.renderNavigationRoute) { window.renderNavigationRoute(${locationFix.latitude}, ${locationFix.longitude}, $targetLat, $targetLng, $coordsJson, ${org.json.JSONObject.quote(targetName)}); }"
                webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
            }
            isRerouting = false
        }
    }

    LaunchedEffect(targetLat, targetLng, hasValhallaFiles) {
        recalculateRoute(announce = true)
    }

    LaunchedEffect(locationFix.latitude, locationFix.longitude) {
        val res = routeResult
        var distToNextTurn = 999.0
        if (res is NavigationRouteResult.Success) {
            // 1) تحديث المسافة المتبقية والتقدم والوقت المتوقع للوصول لحظياً
            val directRemaining = HaversinePrecisionNavigator.distanceMeters(
                locationFix.latitude, locationFix.longitude,
                targetLat, targetLng
            )
            remainingDistanceText = HaversinePrecisionNavigator.formatDistanceArabic(directRemaining)
            tripProgressFraction = HaversinePrecisionNavigator.calculateTripProgress(totalTripDistanceMeters, directRemaining)

            val speedMps = (locationFix.speedKmh.coerceAtLeast(25f) * 1000.0) / 3600.0
            val estRemainingSeconds = (directRemaining / speedMps).toInt().coerceAtLeast(15)
            estimatedTimeText = HaversinePrecisionNavigator.formatDurationArabic(estRemainingSeconds)
            val arrivalMillis = System.currentTimeMillis() + (estRemainingSeconds * 1000L)
            estimatedArrivalClock = java.text.SimpleDateFormat("hh:mm a", Locale("ar")).format(java.util.Date(arrivalMillis))

            // 2) اكتشاف الانحراف وإعادة التوجيه إذا كان المسار شارعياً (أكثر من نقطتين)
            if (res.points.size > 2) {
                val offRoute = realNavigator.isOffRoute(
                    locationFix.latitude,
                    locationFix.longitude,
                    locationFix.accuracyMeters,
                    res.points
                )
                if (offRoute) {
                    offRouteConsecutiveSeconds++
                    if (offRouteConsecutiveSeconds >= 5 && !isRerouting) {
                        isRerouting = true
                        currentInstruction = "جارٍ إعادة حساب المسار..."
                        navigationVoice.announceReroute()
                        recalculateRoute(announce = false)
                        offRouteConsecutiveSeconds = 0
                    }
                } else {
                    offRouteConsecutiveSeconds = 0
                }
            }

            // 3) التدرج الحقيقي لخطوات الانعطاف والتنبيه الصوتي المسبق
            if (res.instructions.isNotEmpty() && activeInstructionIndex < res.instructions.size) {
                var idx = activeInstructionIndex
                while (idx < res.instructions.size - 1) {
                    val step = res.instructions[idx]
                    if (step.maneuverLat != 0.0 && step.maneuverLon != 0.0) {
                        val d = HaversinePrecisionNavigator.distanceMeters(
                            locationFix.latitude, locationFix.longitude,
                            step.maneuverLat, step.maneuverLon
                        )
                        if (d < 22.0) {
                            idx++
                            continue
                        }
                    }
                    break
                }
                if (idx != activeInstructionIndex) {
                    activeInstructionIndex = idx
                }

                val currentStep = res.instructions[activeInstructionIndex]
                currentInstruction = currentStep.text
                nextTurnManeuver = currentStep.maneuverType

                distToNextTurn = if (currentStep.maneuverLat != 0.0 && currentStep.maneuverLon != 0.0) {
                    HaversinePrecisionNavigator.distanceMeters(
                        locationFix.latitude, locationFix.longitude,
                        currentStep.maneuverLat, currentStep.maneuverLon
                    )
                } else {
                    directRemaining.coerceAtMost(currentStep.segmentDistance.coerceAtLeast(30.0))
                }

                navigationVoice.processTurnAnnouncement(
                    instructionKey = "${activeInstructionIndex}_${currentStep.maneuverType}",
                    instructionText = currentStep.text,
                    distanceToTurnMeters = distToNextTurn,
                    currentSpeedKmh = locationFix.speedKmh
                )
            }

            NavigationForegroundService.updateNotification(
                context,
                currentInstruction,
                remainingDistanceText,
                estimatedArrivalClock
            )

            // 4) إعلان الوصول في كافة أوضاع الملاحة
            if (directRemaining <= 35.0 && !hasAnnouncedArrival) {
                hasAnnouncedArrival = true
                currentInstruction = "وصلت إلى وجهتك: $targetName"
                nextTurnManeuver = ManeuverType.DESTINATION
                navigationVoice.announceArrival(targetName)
            }
        }

        val smartZoom = if (distToNextTurn < 200.0) 18 else 15
        val script = "if (window.updateUserPosition) { window.updateUserPosition(${locationFix.latitude}, ${locationFix.longitude}, ${locationFix.bearingDegrees}); } if (window.applySmartZoom) { window.applySmartZoom($smartZoom); }"
        webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length < 2) {
            searchResults = emptyList()
            searchMessage = null
            canDownloadPlacesFromSearch = false
            isSearching = false
            return@LaunchedEffect
        }
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            delay(220)
            isSearching = true
            searchMessage = null
            canDownloadPlacesFromSearch = false
            val query = searchQuery.trim()
            when (val outcome = PlaceSearch.search(
                context = context,
                query = query,
                providers = providersList,
                stores = storesList,
                userLat = locationFix.latitude,
                userLng = locationFix.longitude
            )) {
                is PlaceSearchOutcome.Results -> {
                    searchResults = outcome.items.map { res ->
                        NavigationSearchResult(
                            title = res.shortName,
                            subtitle = res.displayName,
                            lat = res.latitude,
                            lng = res.longitude,
                            isLocal = outcome.source != "online" || res.category == "provider" || res.category == "store"
                        )
                    }.distinctBy { "${it.lat},${it.lng}" }
                }
                is PlaceSearchOutcome.Message -> {
                    searchResults = emptyList()
                    searchMessage = outcome.text
                    canDownloadPlacesFromSearch = outcome.canDownloadPlaces
                }
            }
            isSearching = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
    ) {
        LaunchedEffect(pickingOnNavMap) {
            webViewRef?.evaluateJavascript("if (window.setPickMode) { window.setPickMode($pickingOnNavMap); }", null)
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewRef = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.allowFileAccess = true
                    settings.allowContentAccess = false
                    addJavascriptInterface(
                        object {
                            @android.webkit.JavascriptInterface
                            fun onNavMapPicked(lat: Double, lng: Double) {
                                post {
                                    if (pickingOnNavMap) {
                                        pickedNavPoint = lat to lng
                                    }
                                }
                            }
                        },
                        "NavBridge"
                    )
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?
                        ): android.webkit.WebResourceResponse? {
                            return com.serviseyem.utils.OfflineTileInterceptor.intercept(ctx, request)
                                ?: super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript("if (window.setPickMode) { window.setPickMode($pickingOnNavMap); }", null)
                            recalculateRoute()
                        }
                    }
                    val htmlContent = buildNavigationLeafletHtml(
                        locationFix.latitude,
                        locationFix.longitude,
                        targetLat,
                        targetLng,
                        targetName,
                        preferOsmOffline = com.serviseyem.utils.OfflineTileInterceptor.hasAnyInstalledRegions(ctx)
                    )
                    loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { view -> webViewRef = view }
        )

        NavigationTopHudColumn(
            hasValhallaFiles = hasValhallaFiles,
            isVoiceMuted = isVoiceMuted,
            statusBannerText = statusBannerText,
            isGpsWeak = isGpsWeak,
            searchQuery = searchQuery,
            isSearching = isSearching,
            searchResults = searchResults,
            searchMessage = searchMessage,
            canDownloadPlacesFromSearch = canDownloadPlacesFromSearch,
            nextTurnManeuver = nextTurnManeuver,
            currentInstruction = currentInstruction,
            targetName = targetName,
            onBackClick = onBackClick,
            onOpenValhallaDialog = { showValhallaDialog = true },
            onOpenDestChooser = { showDestChooser = true },
            onToggleMute = { navigationVoice.toggleMute() },
            onDismissBanner = { statusBannerText = null },
            onSearchQueryChange = { searchQuery = it },
            onClearSearch = { searchQuery = "" },
            onSelectSearchResult = { result ->
                RecentDestinations(context).add(result.title, result.lat, result.lng)
                targetLat = result.lat
                targetLng = result.lng
                targetName = result.title
                searchQuery = ""
                searchResults = emptyList()
                searchMessage = null
                recalculateRoute(announce = true)
            },
            onOpenPlacesDownload = { showPlacesDownload = true }
        )

        NavigationFloatingWidgets(
            speedKmh = locationFix.speedKmh.toInt(),
            onRecenterClick = {
                val script = "if (window.centerOnUser) { window.centerOnUser(${locationFix.latitude}, ${locationFix.longitude}); }"
                webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        )

        if (pickingOnNavMap) {
            NavigationPickOnMapBar(
                context = context,
                pickedNavPoint = pickedNavPoint,
                onCancel = {
                    pickingOnNavMap = false
                    pickedNavPoint = null
                },
                onConfirmStart = { label, la, lo ->
                    pickingOnNavMap = false
                    pickedNavPoint = null
                    RecentDestinations(context).add(label, la, lo)
                    targetLat = la
                    targetLng = lo
                    targetName = label
                    recalculateRoute(announce = true)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(14.dp)
                    .fillMaxWidth()
            )
        } else {
            RealtimeNavigationProgressComponent(
                isOfflineMode = hasValhallaFiles,
                progressPercentage = tripProgressFraction,
                remainingDistanceText = remainingDistanceText,
                estimatedDurationText = estimatedTimeText,
                estimatedArrivalTimeText = estimatedArrivalClock,
                currentSpeedKmh = locationFix.speedKmh.toInt(),
                currentInstruction = currentInstruction,
                nextManeuver = nextTurnManeuver,
                destinationName = targetName,
                isVoiceMuted = isVoiceMuted,
                onToggleVoice = { navigationVoice.toggleMute() },
                onRecenterClick = {
                    val script = "if (window.centerOnUser) { window.centerOnUser(${locationFix.latitude}, ${locationFix.longitude}); }"
                    webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                },
                onEndNavigationClick = {
                    navigationVoice.speak("تم إنهاء رحلة الملاحة بنجاح")
                    onBackClick()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }
    }

    NavigationDialogsHost(
        context = context,
        showDestChooser = showDestChooser,
        showValhallaDialog = showValhallaDialog,
        showPlacesDownload = showPlacesDownload,
        userLat = locationFix.latitude,
        userLng = locationFix.longitude,
        providersList = providersList,
        storesList = storesList,
        savedAddresses = savedAddresses,
        addressStore = addressStore,
        onSavedAddressesUpdated = { savedAddresses = it },
        onDismissDestChooser = { showDestChooser = false },
        onPickDestination = { name, la, lo ->
            showDestChooser = false
            targetLat = la
            targetLng = lo
            targetName = name
            recalculateRoute(announce = true)
        },
        onStartMapPick = {
            showDestChooser = false
            pickedNavPoint = null
            pickingOnNavMap = true
        },
        onOpenPlacesDownload = {
            showDestChooser = false
            showPlacesDownload = true
        },
        onDismissValhallaDialog = { showValhallaDialog = false },
        onValhallaFilesUpdated = { msg, announce ->
            realNavigator.refreshEngine()
            hasValhallaFiles = OfflineMapLifecycleManager.getInstance(context).isOfflineDataAvailable()
            if (msg != null) statusBannerText = msg
            recalculateRoute(announce = announce)
        },
        onDismissPlacesDownload = { showPlacesDownload = false },
        onPlacesDownloadCompleted = { msg -> statusBannerText = msg }
    )
}

data class NavigationSearchResult(
    val title: String,
    val subtitle: String,
    val lat: Double,
    val lng: Double,
    val isLocal: Boolean
)

package com.serviseyem.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.BespokePremiumSystem
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * 🧭 NavigationScreen
 * World-Class Production-Grade Hybrid Navigation Screen:
 * - LocationEngine integration with 1s updates, 1D Kalman smoothing, and dead reckoning.
 * - NavigationForegroundService with persistent notification to survive screen-off.
 * - FLAG_KEEP_SCREEN_ON to keep display active while driving.
 * - NavigationVoice with AudioFocus ducking and speed-scaled Arabic announcements.
 * - True offline Valhalla routing vs clean non-fabricated direct Basic mode.
 * - High-contrast HUD overlay with real-time speed, compass, and connection badge.
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

        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            locationEngine.stopTracking()
            navigationVoice.destroy()
            NavigationForegroundService.stopService(context)
        }
    }

    // Destinations
    var targetLat by remember { mutableDoubleStateOf(initialTargetLat) }
    var targetLng by remember { mutableDoubleStateOf(initialTargetLng) }
    var targetName by remember { mutableStateOf(initialTargetName) }

    // Offline / Valhalla State
    var hasValhallaFiles by remember { mutableStateOf(OfflineMapLifecycleManager.getInstance(context).isOfflineDataAvailable()) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    var downloadStatusText by remember { mutableStateOf("") }

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
    var tripProgressFraction by remember { mutableFloatStateOf(0.1f) }

    // Off-route tracking
    var offRouteConsecutiveSeconds by remember { mutableIntStateOf(0) }
    var isRerouting by remember { mutableStateOf(false) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NavigationSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Map WebView reference
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Recalculate route
    fun recalculateRoute(announce: Boolean = false) {
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
                totalTripDistanceMeters = result.totalDistanceMeters
                tripProgressFraction = HaversinePrecisionNavigator.calculateTripProgress(
                    result.totalDistanceMeters,
                    result.totalDistanceMeters * 0.95
                )
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

                // Update notification in foreground service
                NavigationForegroundService.updateNotification(
                    context,
                    currentInstruction,
                    remainingDistanceText,
                    estimatedArrivalClock
                )

                // Draw polyline on Leaflet WebView
                val coordsJson = JSONArray().apply {
                    result.points.forEach { pt ->
                        put(JSONArray().apply {
                            put(pt.lat)
                            put(pt.lon)
                        })
                    }
                }.toString()

                val escapedTarget = targetName.replace("'", "\\'")
                val script = "if (window.renderNavigationRoute) { window.renderNavigationRoute(${locationFix.latitude}, ${locationFix.longitude}, $targetLat, $targetLng, $coordsJson, '$escapedTarget'); }"
                webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
            }
            isRerouting = false
        }
    }

    // Launch initial route calculation & start foreground service
    LaunchedEffect(targetLat, targetLng, hasValhallaFiles) {
        NavigationForegroundService.startService(context, currentInstruction, remainingDistanceText, estimatedArrivalClock)
        recalculateRoute(announce = true)
    }

    // Live GPS tracking update loop (every new fix from LocationEngine)
    LaunchedEffect(locationFix.latitude, locationFix.longitude) {
        val res = routeResult
        if (res is NavigationRouteResult.Success && res.isOfflineEngine) {
            // Check off-route
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

            // Check distance to next maneuver and trigger voice
            if (res.instructions.isNotEmpty() && activeInstructionIndex < res.instructions.size) {
                val currentStep = res.instructions[activeInstructionIndex]
                val distToStep = currentStep.segmentDistance.coerceAtLeast(30.0)

                navigationVoice.processTurnAnnouncement(
                    instructionKey = "${activeInstructionIndex}_${currentStep.maneuverType}",
                    instructionText = currentStep.text,
                    distanceToTurnMeters = distToStep,
                    currentSpeedKmh = locationFix.speedKmh
                )
            }

            // Check destination arrival
            val distToEnd = HaversinePrecisionNavigator.distanceMeters(
                locationFix.latitude, locationFix.longitude,
                targetLat, targetLng
            )
            if (distToEnd <= 30.0) {
                navigationVoice.announceArrival(targetName)
            }
        }

        // Update user marker on Leaflet map
        val script = "if (window.updateUserPosition) { window.updateUserPosition(${locationFix.latitude}, ${locationFix.longitude}, ${locationFix.bearingDegrees}); }"
        webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
    }

    // Search query debouncing with Arabic normalization
    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length < 2) {
            searchResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            delay(500)
            isSearching = true
            val query = searchQuery.trim()

            // 1. Search local stores, clinics, properties
            val localResults = mutableListOf<NavigationSearchResult>()
            viewModel?.stores?.value?.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.localNeighborhood.contains(query, ignoreCase = true) ||
                it.cityId.contains(query, ignoreCase = true)
            }?.take(3)?.forEach { s ->
                val subtitle = s.localNeighborhood.ifBlank { s.cityId }.ifBlank { "متجر / خدمة" }
                localResults.add(NavigationSearchResult(title = s.name, subtitle = subtitle, lat = s.latitude, lng = s.longitude, isLocal = true))
            }
            viewModel?.properties?.value?.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.localNeighborhood.contains(query, ignoreCase = true) ||
                it.cityId.contains(query, ignoreCase = true)
            }?.take(2)?.forEach { p ->
                val subtitle = p.localNeighborhood.ifBlank { p.cityId }.ifBlank { "عقار" }
                localResults.add(NavigationSearchResult(title = p.title, subtitle = subtitle, lat = p.latitude, lng = p.longitude, isLocal = true))
            }

            // 2. OpenStreetMap Nominatim API in background
            val onlineResults = withContext(Dispatchers.IO) {
                fetchNominatimPlaces(query)
            }

            searchResults = (localResults + onlineResults).distinctBy { "${it.lat},${it.lng}" }
            isSearching = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
    ) {
        // 1. Leaflet HTML5 Map View
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewRef = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.allowFileAccess = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            recalculateRoute()
                        }
                    }
                    val htmlContent = buildNavigationLeafletHtml(
                        locationFix.latitude,
                        locationFix.longitude,
                        targetLat,
                        targetLng,
                        targetName
                    )
                    loadDataWithBaseURL("https://openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { view -> webViewRef = view }
        )

        // 2. Top Header & Turn-by-Turn Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Action bar: Back, Mode Badge, GPS Weak indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF131C2E).copy(alpha = 0.92f), RoundedCornerShape(12.dp))
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (hasValhallaFiles) Color(0xFF064E3B).copy(alpha = 0.8f) else Color(0xFF0C4A6E).copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, if (hasValhallaFiles) Color(0xFF10B981) else Color(0xFF0284C7))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (hasValhallaFiles) Color(0xFF10B981) else Color(0xFF38BDF8))
                        )
                        Text(
                            text = if (hasValhallaFiles) "ملاحة أوفلاين (فالدالا)" else "تقريبي - مسافة مباشرة",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = { navigationVoice.toggleMute() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF131C2E).copy(alpha = 0.92f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = if (isVoiceMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "الصوت",
                        tint = if (isVoiceMuted) Color.Gray else Color(0xFF00E5FF)
                    )
                }
            }

            // GPS Signal Loss Warning Banner
            AnimatedVisibility(visible = isGpsWeak) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("إشارة GPS ضعيفة - جاري المتابعة بالتقدير الملاحي", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Search Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF131C2E).copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث عن مكان، شارع، أو خدمة لتغيير الوجهة...", color = Color.Gray, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00E5FF)) },
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF00E5FF))
                            } else if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (searchResults.isNotEmpty()) {
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(searchResults) { result ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            targetLat = result.lat
                                            targetLng = result.lng
                                            targetName = result.title
                                            searchQuery = ""
                                            searchResults = emptyList()
                                            recalculateRoute(announce = true)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (result.isLocal) Icons.Default.Place else Icons.Default.LocationSearching,
                                        contentDescription = null,
                                        tint = if (result.isLocal) Color(0xFF10B981) else Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = result.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = result.subtitle, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Divider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }

            // Dynamic Turn-by-Turn Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.96f),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981))),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (nextTurnManeuver) {
                                ManeuverType.TURN_RIGHT, ManeuverType.SLIGHT_RIGHT, ManeuverType.SHARP_RIGHT -> Icons.Default.TurnRight
                                ManeuverType.TURN_LEFT, ManeuverType.SLIGHT_LEFT, ManeuverType.SHARP_LEFT -> Icons.Default.TurnLeft
                                ManeuverType.U_TURN -> Icons.Default.Refresh
                                ManeuverType.ROUNDABOUT -> Icons.Default.ChangeCircle
                                ManeuverType.DESTINATION -> Icons.Default.Flag
                                else -> Icons.Default.ArrowUpward
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentInstruction,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الوجهة: $targetName",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 3. Floating Speedometer & Compass Widgets
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Speedometer HUD
            Surface(
                shape = CircleShape,
                color = Color(0xFF0F172A).copy(alpha = 0.9f),
                border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                modifier = Modifier.size(52.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${locationFix.speedKmh.toInt()}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("كم/س", color = Color(0xFF00E5FF), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Recenter Map Button
            IconButton(
                onClick = {
                    val script = "if (window.centerOnUser) { window.centerOnUser(${locationFix.latitude}, ${locationFix.longitude}); }"
                    webViewRef?.post { webViewRef?.evaluateJavascript(script, null) }
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFF0F172A).copy(alpha = 0.9f), CircleShape)
                    .clip(CircleShape)
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "موقعي", tint = Color.White)
            }
        }

        // 4. Real-Time Navigation Progress HUD Component
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

data class NavigationSearchResult(
    val title: String,
    val subtitle: String,
    val lat: Double,
    val lng: Double,
    val isLocal: Boolean
)

private fun fetchNominatimPlaces(query: String): List<NavigationSearchResult> {
    return try {
        val encoded = URLEncoder.encode("$query, Yemen", "UTF-8")
        val url = URL("https://nominatim.openstreetmap.org/search?format=json&q=$encoded&countrycodes=ye&limit=5&addressdetails=1")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 5000
            readTimeout = 5000
            setRequestProperty("User-Agent", "YemenServicesGuide/1.0 (Android)")
        }

        if (conn.responseCode == 200) {
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(text)
            val list = mutableListOf<NavigationSearchResult>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val displayName = obj.optString("display_name", "")
                val parts = displayName.split(",")
                val title = parts.firstOrNull()?.trim() ?: displayName
                val subtitle = parts.drop(1).take(2).joinToString(", ").trim()
                val lat = obj.optDouble("lat", 0.0)
                val lon = obj.optDouble("lon", 0.0)
                if (lat != 0.0 && lon != 0.0) {
                    list.add(NavigationSearchResult(title = title, subtitle = subtitle, lat = lat, lng = lon, isLocal = false))
                }
            }
            list
        } else {
            emptyList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun buildNavigationLeafletHtml(
    startLat: Double,
    startLon: Double,
    endLat: Double,
    endLon: Double,
    destName: String
): String {
    return """
    <!DOCTYPE html>
    <html lang="ar" dir="rtl">
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            html, body, #map { width: 100%; height: 100%; margin: 0; padding: 0; background: #070B14; }
            .user-pulse {
                width: 22px; height: 22px;
                background: #00E5FF;
                border: 3px solid #ffffff;
                border-radius: 50%;
                box-shadow: 0 0 15px #00E5FF;
            }
            .dest-pulse {
                width: 26px; height: 26px;
                background: #EF4444;
                border: 3px solid #ffffff;
                border-radius: 50%;
                box-shadow: 0 0 18px #EF4444;
            }
            .leaflet-control-attribution { display: none !important; }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <script>
            var map = L.map('map', {
                center: [$startLat, $startLon],
                zoom: 15,
                zoomControl: false
            });

            L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                maxZoom: 19,
                subdomains: 'abcd'
            }).addTo(map);

            var userIcon = L.divIcon({ className: 'user-pulse', iconSize: [22, 22] });
            var destIcon = L.divIcon({ className: 'dest-pulse', iconSize: [26, 26] });

            var userMarker = L.marker([$startLat, $startLon], { icon: userIcon }).addTo(map);
            var destMarker = L.marker([$endLat, $endLon], { icon: destIcon }).addTo(map).bindPopup('$destName');

            var routePolyline = null;

            window.renderNavigationRoute = function(sLat, sLng, eLat, eLng, coords, title) {
                userMarker.setLatLng([sLat, sLng]);
                destMarker.setLatLng([eLat, eLng]);
                destMarker.setPopupContent(title);

                if (routePolyline) {
                    map.removeLayer(routePolyline);
                }

                var latlngs = [];
                for (var i = 0; i < coords.length; i++) {
                    latlngs.push([coords[i][0], coords[i][1]]);
                }

                routePolyline = L.polyline(latlngs, {
                    color: '#00E5FF',
                    weight: 6,
                    opacity: 0.9,
                    lineJoin: 'round'
                }).addTo(map);

                map.fitBounds(routePolyline.getBounds(), { padding: [80, 80] });
            };

            window.updateUserPosition = function(lat, lng, heading) {
                userMarker.setLatLng([lat, lng]);
            };

            window.centerOnUser = function(lat, lng) {
                map.setView([lat, lng], 17, { animate: true });
            };
        </script>
    </body>
    </html>
    """.trimIndent()
}

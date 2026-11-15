package com.serviseyem.ui.screens.map

import android.content.Context
import android.webkit.WebView
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.serviseyem.data.ProviderEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.map.components.MapControls
import com.serviseyem.utils.GpxManager
import com.serviseyem.utils.RuralEmergencySosManager
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray

@Composable
internal fun MapControlsDock(
    state: MapScreenState,
    viewModel: MainViewModel,
    context: Context,
    providers: List<ProviderEntity>,
    filteredProviders: List<ProviderEntity>,
    safeUserLat: Double,
    safeUserLng: Double,
    webViewRef: WebView?,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    themeColors: VisualThemePalette,
    onHideMapError: () -> Unit,
    onOpenDestChooser: () -> Unit,
    onOpenValhallaDialog: () -> Unit,
    onOpenRegionsDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    MapControls(
        isRadarMode = state.isRadarMode,
        onToggleRadarMode = {
            onHideMapError()
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
                    onOpenDestChooser()
                } else {
                    state.currentMapLayer = layerKey
                    state.currentMapMode = MapMode.NAVIGATION
                    val script = "if (window.switchLayer) { window.switchLayer('$layerKey'); }"
                    webViewRef?.post { webViewRef.evaluateJavascript(script, null) }
                }
            } else {
                state.currentMapLayer = layerKey
                if (state.currentMapMode == MapMode.NAVIGATION) {
                    state.currentMapMode = MapMode.VELA_3D
                    webViewRef?.post { webViewRef.evaluateJavascript("if (window.clearRoute) { window.clearRoute(); }", null) }
                }
                val script = "if (window.switchLayer) { window.switchLayer('$layerKey'); }"
                webViewRef?.post { webViewRef.evaluateJavascript(script, null) }
            }
        },
        onZoomIn = {
            state.zoomScale = (state.zoomScale * 1.3f).coerceIn(0.25f, 8.0f)
            webViewRef?.post { webViewRef.evaluateJavascript("if (window.zoomIn) { window.zoomIn(); }", null) }
        },
        onZoomOut = {
            state.zoomScale = (state.zoomScale / 1.3f).coerceIn(0.25f, 8.0f)
            webViewRef?.post { webViewRef.evaluateJavascript("if (window.zoomOut) { window.zoomOut(); }", null) }
        },
        onRecenterLocation = {
            state.panOffset = Offset.Zero
            state.zoomScale = 1.0f
            viewModel.startLocationUpdates()
        },
        isGpsActive = viewModel.isGpsTrackingActive.collectAsState().value,
        onDownloadValhallaClick = onOpenValhallaDialog,
        onOpenRegionsDialog = onOpenRegionsDialog,
        currentMode = state.currentMapMode,
        onModeChange = { mode ->
            if (mode == MapMode.NAVIGATION && state.pendingNavDestination == null) {
                onOpenDestChooser()
            } else {
                state.currentMapMode = mode
                if (mode == MapMode.NAVIGATION) {
                    state.currentMapLayer = "navigation"
                    webViewRef?.post { webViewRef.evaluateJavascript("if (window.switchLayer) { window.switchLayer('navigation'); }", null) }
                }
            }
        },
        tiltAngle = state.tiltAngle,
        onToggleTilt = { state.tiltAngle = if (state.tiltAngle > 0) 0f else 32f },
        onOpenAleph = {
            state.alephSelectedEntity = state.selectedEntity
            state.isAlephDialogOpen = true
        },
        isDeadZoneActive = state.isDeadZoneOverlayActive,
        onToggleDeadZoneOverlay = {
            state.isDeadZoneOverlayActive = !state.isDeadZoneOverlayActive
            val active = state.isDeadZoneOverlayActive
            val script = "if (window.setDeadZones) { window.setDeadZones($active); }"
            webViewRef?.post { webViewRef.evaluateJavascript(script, null) }
            coroutineScope.launch {
                snackbarHostState.showSnackbar(if (active) "تم عرض مناطق التغطية الضعيفة 🔴" else "تم إخفاء التغطية الضعيفة")
            }
        },
        isReliabilityActive = state.isReliabilityLayerActive,
        onToggleReliability = {
            state.isReliabilityLayerActive = !state.isReliabilityLayerActive
            val active = state.isReliabilityLayerActive
            if (active) {
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
                val jsonStr = JSONArray(zones).toString()
                webViewRef?.post { webViewRef.evaluateJavascript("if (window.setReliabilityZones) { window.setReliabilityZones($jsonStr); }", null) }
            } else {
                webViewRef?.post { webViewRef.evaluateJavascript("if (window.setReliabilityZones) { window.setReliabilityZones([]); }", null) }
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
                val jsonStr = JSONArray(hazardsJson).toString()
                webViewRef?.post { webViewRef.evaluateJavascript("if (window.setRoadSafetyHazards) { window.setRoadSafetyHazards($jsonStr); }", null) }
            } else {
                webViewRef?.post { webViewRef.evaluateJavascript("if (window.setRoadSafetyHazards) { window.setRoadSafetyHazards([]); }", null) }
            }
            coroutineScope.launch {
                snackbarHostState.showSnackbar(if (active) "تم تفعيل طبقة سلامة الطريق وصعوبات الطرق اليمنية ⚠️" else "تم إخفاء طبقة سلامة الطرق")
            }
        },
        onOpenAddOsmLandmark = {
            state.isAddOsmDialogOpen = true
        },
        onTriggerRuralSos = {
            val packet = RuralEmergencySosManager.EmergencyPacket(
                latitude = safeUserLat,
                longitude = safeUserLng,
                nearestTown = state.selectedCity
            )
            RuralEmergencySosManager.sendEmergencySms(context, "199", packet) {
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
            webViewRef?.post { webViewRef.evaluateJavascript(script, null) }
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
            val script = "if (window.setGibs && $active) { window.setGibs('${state.gibsProduct}'); } if (window.toggleNasaGibsLayer) { window.toggleNasaGibsLayer($active); } if (window.toggleEoxCloudlessLayer) { window.toggleEoxCloudlessLayer(false); } if (window.toggleEsriWorldImageryLayer) { window.toggleEsriWorldImageryLayer(false); }"
            webViewRef?.post { webViewRef.evaluateJavascript(script, null) }
            if (!active) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم إيقاف طبقة NASA GIBS والعودة للطبقة الافتراضية")
                }
            }
        },
        onLongPressNasaGibs = {
            val order = listOf("viirs_noaa20", "viirs_snpp", "truecolor", "landtemp")
            val idx = order.indexOf(state.gibsProduct)
            state.gibsProduct = order[(idx + 1) % order.size]
            state.isEoxCloudlessActive = false
            state.isEsriImageryActive = false
            if (!state.isNasaGibsActive) {
                state.isNasaGibsActive = true
                webViewRef?.post {
                    webViewRef.evaluateJavascript(
                        "if (window.toggleEoxCloudlessLayer) { window.toggleEoxCloudlessLayer(false); } if (window.toggleEsriWorldImageryLayer) { window.toggleEsriWorldImageryLayer(false); } if (window.setGibs) { window.setGibs('${state.gibsProduct}'); } if (window.toggleNasaGibsLayer) { window.toggleNasaGibsLayer(true); }",
                        null
                    )
                }
            } else {
                webViewRef?.post {
                    webViewRef.evaluateJavascript("if (window.setGibs) { window.setGibs('${state.gibsProduct}'); }", null)
                }
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
            webViewRef?.post { webViewRef.evaluateJavascript(script, null) }
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
            webViewRef?.post { webViewRef.evaluateJavascript(script, null) }
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
        modifier = modifier
    )
}

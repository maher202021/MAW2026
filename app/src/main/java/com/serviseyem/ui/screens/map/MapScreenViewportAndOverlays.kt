@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.serviseyem.ui.screens.map

import android.content.Context
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.map.components.MapBottomSheet
import com.serviseyem.ui.screens.map.components.MarkerRenderer
import com.serviseyem.ui.screens.map.components.RadarRenderer
import com.serviseyem.ui.screens.map.components.VpsArCameraView
import com.serviseyem.utils.GnssStatusSummary
import com.serviseyem.utils.OfflinePlacesDb
import com.serviseyem.utils.OfflineTileInterceptor
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun MapMainViewport(
    state: MapScreenState,
    radarPoints: List<MarkerRenderer.MapItemPoint>,
    safeUserLat: Double,
    safeUserLng: Double,
    filteredProviders: List<ProviderEntity>,
    filteredStores: List<StoreEntity>,
    filteredProperties: List<PropertyEntity>,
    pickingDestination: Boolean,
    pickedPoint: Pair<Double, Double>?,
    onPickedPointChange: (Pair<Double, Double>) -> Unit,
    onShowMapErrorChange: (Boolean) -> Unit,
    onWebViewCreated: (WebView?) -> Unit,
    webViewRef: WebView?,
    context: Context,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    haptic: HapticFeedback,
    maxZoom: Int = 22,
    showSatMenu: Boolean = true,
    showMeasureTool: Boolean = true,
    themeColors: VisualThemePalette
) {
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
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    state.selectedEntity = item.originalItem
                },
                isHeatmapActive = state.isHeatmapActive,
                maxRangeKm = state.maxRangeKm,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else if (state.currentMapMode == MapMode.AR_VPS) {
        VpsArCameraView(
            userLat = safeUserLat,
            userLng = safeUserLng,
            nearbyProviders = filteredProviders,
            nearbyStores = filteredStores,
            nearbyProperties = filteredProperties,
            onEntitySelected = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                maxZoom = maxZoom,
                showSatMenu = showSatMenu,
                showMeasureTool = showMeasureTool,
                tiltAngle = state.tiltAngle,
                onProviderSelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    state.selectedEntity = it
                },
                onStoreSelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    state.selectedEntity = it
                },
                onPropertySelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    state.selectedEntity = it
                },
                onDeselect = { state.selectedEntity = null },
                onSwitchToRadar = {
                    onShowMapErrorChange(false)
                    state.isRadarMode = true
                },
                onWebViewCreated = onWebViewCreated,
                onTilesFailedOffline = { layerKey ->
                    if (layerKey != "osm_ar" && layerKey != "osm" && OfflineTileInterceptor.hasAnyInstalledRegions(context)) {
                        state.currentMapLayer = "osm"
                        webViewRef?.post {
                            webViewRef.evaluateJavascript("if (window.switchLayer) { window.switchLayer('osm'); }", null)
                        }
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("تم التبديل للخريطة المحفوظة بلا إنترنت © مساهمو OpenStreetMap")
                        }
                    }
                },
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
                isNasaGibsActive = state.isNasaGibsActive,
                gibsProduct = state.gibsProduct,
                onGibsInfo = { product, date ->
                    val title = when (product) {
                        "viirs_noaa20" -> "VIIRS NOAA-20"
                        "viirs_snpp" -> "VIIRS Suomi NPP"
                        "truecolor" -> "MODIS Terra"
                        "landtemp" -> "حرارة السطح MODIS"
                        else -> "NASA GIBS"
                    }
                    val resLabel = if (product == "landtemp") "دقة 1 كم" else "دقة 250 م"
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("ناسا ($title): صورة $date • تأخير نحو 3 ساعات • $resLabel")
                    }
                },
                isEoxCloudlessActive = state.isEoxCloudlessActive,
                isEsriImageryActive = state.isEsriImageryActive,
                eumetEnabled = state.eumetEnabled,
                eumetLayer = state.eumetLayerName,
                onOverlayStatus = { overlay, ok ->
                    if (overlay == "eumet" && !ok) {
                        state.eumetEnabled = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("تعذر تحميل صور Meteosat (تحقق من اتصال الإنترنت)")
                        }
                    }
                },
                pickMode = pickingDestination,
                onMapPicked = { la, lo ->
                    if (pickingDestination) onPickedPointChange(la to lo)
                },
                waypointMarkers = pickedPoint?.takeIf { pickingDestination }?.let {
                    listOf(Triple("الوجهة المختارة", it.first, it.second))
                }.orEmpty(),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
internal fun MapGnssBadge(
    satelliteStatus: GnssStatusSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
        shadowElevation = 8.dp,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SatelliteAlt,
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
}

@Composable
internal fun MapPickDestinationBar(
    context: Context,
    pickedPoint: Pair<Double, Double>?,
    onCancel: () -> Unit,
    onConfirmDestination: (label: String, lat: Double, lng: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xF50F172A),
        border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
        shadowElevation = 10.dp,
        modifier = modifier.testTag("map_pick_confirm_card")
    ) {
        if (pickedPoint == null) {
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
                    onClick = onCancel,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("إلغاء", color = Color(0xFFEF4444))
                }
            }
        } else {
            val (la, lo) = pickedPoint
            val nearest by androidx.compose.runtime.produceState<String?>(initialValue = null, key1 = la, key2 = lo) {
                value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    OfflinePlacesDb.nearest(context, la, lo)
                }
            }
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
                        onClick = { onConfirmDestination(label, la, lo) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("map_pick_start_nav_button")
                    ) {
                        Text("ابدأ الملاحة", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onCancel,
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

@Composable
internal fun MapEntityBottomSheetHost(
    entity: Any,
    state: MapScreenState,
    viewModel: MainViewModel,
    safeUserLat: Double,
    safeUserLng: Double,
    onOpenProviderDetails: (ProviderEntity) -> Unit,
    onOpenStoreDetails: (StoreEntity) -> Unit,
    onOpenPropertyDetails: (PropertyEntity) -> Unit,
    onRequestBooking: (Any) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    MapBottomSheet(
        entity = entity,
        userLat = safeUserLat,
        userLng = safeUserLng,
        onDismiss = { state.selectedEntity = null },
        onRequestBooking = { target ->
            state.selectedEntity = null
            when (target) {
                is ProviderEntity -> state.bookingProviderTarget = target
                is StoreEntity -> onRequestBooking(target)
                is PropertyEntity -> onRequestBooking(target)
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
        modifier = modifier
    )
}

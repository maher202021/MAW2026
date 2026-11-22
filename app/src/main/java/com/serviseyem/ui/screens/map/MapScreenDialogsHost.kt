package com.serviseyem.ui.screens.map

import android.Manifest
import android.content.Context
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.dialogs.BookingDialog
import com.serviseyem.ui.screens.map.components.AddOsmLandmarkDialog
import com.serviseyem.ui.screens.map.components.AlephVisualAssistantDialog
import com.serviseyem.ui.screens.map.components.EarthquakeBottomSheet
import com.serviseyem.ui.screens.map.components.GnssSignalPanel
import com.serviseyem.ui.screens.map.components.GroupTrackingDialog
import com.serviseyem.ui.screens.map.components.RoutePlannerDialog
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.utils.GnssStatusSummary
import com.serviseyem.utils.LandmarkAddress
import com.serviseyem.utils.LandmarkAddressStore
import com.serviseyem.utils.OsmContributionManager
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.VoiceSearchHelper
import com.serviseyem.utils.VoiceState
import com.serviseyem.utils.VoiceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun MapScreenDialogsHost(
    state: MapScreenState,
    viewModel: MainViewModel,
    context: Context,
    providers: List<ProviderEntity>,
    stores: List<StoreEntity>,
    safeUserLat: Double,
    safeUserLng: Double,
    hasRealLocation: Boolean,
    savedAddresses: List<LandmarkAddress>,
    addressStore: LandmarkAddressStore,
    onSavedAddressesUpdated: (List<LandmarkAddress>) -> Unit,
    showDestChooser: Boolean,
    onDismissDestChooser: () -> Unit,
    onStartDestinationNavigation: (String, Double, Double) -> Unit,
    onStartPickFromMap: () -> Unit,
    showValhallaDialog: Boolean,
    onDismissValhallaDialog: () -> Unit,
    onOpenDestChooser: () -> Unit,
    showPlacesDownload: Boolean,
    onOpenPlacesDownload: () -> Unit,
    onDismissPlacesDownload: () -> Unit,
    showRegionsDialog: Boolean,
    onOpenRegionsDialog: () -> Unit,
    onDismissRegionsDialog: () -> Unit,
    onBumpOfflinePackVersion: () -> Unit,
    voiceState: VoiceState,
    onResetVoiceState: () -> Unit,
    voiceHelper: VoiceSearchHelper,
    micPermLauncher: ActivityResultLauncher<String>,
    onBumpSearchFocus: () -> Unit,
    satelliteStatus: GnssStatusSummary,
    webViewRef: WebView?,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onRequestBooking: (Any) -> Unit,
    themeColors: VisualThemePalette
) {
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

    state.bookingProviderTarget?.let { provider ->
        BookingDialog(
            provider = provider,
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { state.bookingProviderTarget = null }
        )
    }

    if (showDestChooser) {
        DestinationChooserSheet(
            userLat = safeUserLat,
            userLng = safeUserLng,
            hasRealLocation = hasRealLocation,
            providers = providers,
            stores = stores,
            savedAddresses = savedAddresses,
            onPick = { name, la, lo ->
                onDismissDestChooser()
                onStartDestinationNavigation(name, la, lo)
            },
            onPickFromMap = onStartPickFromMap,
            onDownloadPlaces = {
                onDismissDestChooser()
                onOpenPlacesDownload()
            },
            onDismiss = onDismissDestChooser,
            selectedCity = state.selectedCity,
            onAddressSaved = {
                onSavedAddressesUpdated(addressStore.all())
            }
        )
    }

    if (showValhallaDialog) {
        ValhallaOfflineDialog(
            onDismiss = onDismissValhallaDialog,
            onStartNavigation = {
                onBumpOfflinePackVersion()
                if (state.pendingNavDestination == null) {
                    onOpenDestChooser()
                } else {
                    state.currentMapMode = MapMode.NAVIGATION
                    state.currentMapLayer = "navigation"
                    webViewRef?.post { webViewRef.evaluateJavascript("if (window.switchLayer) { window.switchLayer('navigation'); }", null) }
                }
            },
            onOpenPlacesDownload = onOpenPlacesDownload,
            onOpenRegionsDownload = onOpenRegionsDialog,
            onStatusMessage = { msg ->
                onBumpOfflinePackVersion()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        )
    }

    if (showPlacesDownload) {
        PlacesDownloadDialog(
            onDismiss = onDismissPlacesDownload,
            onCompleted = { msg ->
                onBumpOfflinePackVersion()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        )
    }

    if (showRegionsDialog) {
        RegionsDialog(
            onDismiss = onDismissRegionsDialog,
            onRegionsChanged = {
                onBumpOfflinePackVersion()
                webViewRef?.post { webViewRef.reload() }
            }
        )
    }

    if (voiceState.status != VoiceStatus.IDLE) {
        VoiceSearchDialog(
            state = voiceState,
            onRetry = {
                if (voiceHelper.hasMicPermission()) {
                    voiceHelper.start()
                } else {
                    micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onDismiss = {
                voiceHelper.cancel()
                onResetVoiceState()
            },
            onUseKeyboardMic = {
                voiceHelper.cancel()
                onResetVoiceState()
                onBumpSearchFocus()
            },
            onOpenSettings = {
                VoiceSearchHelper.openVoiceSettings(context)
            }
        )
    }

    if (state.isGnssPanelOpen) {
        GnssSignalPanel(
            statusSummary = satelliteStatus,
            themeColors = themeColors,
            onDismiss = { state.isGnssPanelOpen = false }
        )
    }

    if (state.isGroupTrackingActive) {
        GroupTrackingDialog(
            isGroupActive = state.currentGroupCode.isNotBlank(),
            currentGroupCode = state.currentGroupCode,
            userLat = safeUserLat,
            userLng = safeUserLng,
            onStartGroupSession = { code ->
                state.currentGroupCode = code
                if (safeUserLat != 0.0 || safeUserLng != 0.0) {
                    val memberJson = org.json.JSONArray().apply {
                        put(org.json.JSONObject().apply {
                            put("name", "موقعي ($code)")
                            put("lat", safeUserLat)
                            put("lng", safeUserLng)
                        })
                    }.toString()
                    val quoted = org.json.JSONObject.quote(memberJson)
                    webViewRef?.post { webViewRef.evaluateJavascript("if (window.setGroupMembers) { window.setGroupMembers($quoted); }", null) }
                }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تفعيل تتبع المجموعة: $code 👥")
                }
            },
            onStopGroupSession = {
                state.currentGroupCode = ""
                state.isGroupTrackingActive = false
                webViewRef?.post { webViewRef.evaluateJavascript("if (window.setGroupMembers) { window.setGroupMembers([]); }", null) }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم إيقاف تتبع المجموعة 🔴")
                }
            },
            themeColors = themeColors,
            onDismiss = { state.isGroupTrackingActive = false }
        )
    }

    if (state.isAddOsmDialogOpen) {
        AddOsmLandmarkDialog(
            userLat = safeUserLat,
            userLng = safeUserLng,
            currentCity = state.selectedCity,
            themeColors = themeColors,
            onSaveLandmark = { node ->
                OsmContributionManager.savePendingLandmark(context, node)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم حفظ المعلم [${node.name}] بنجاح ومشاركته في خريطة اليمن 🌟")
                }
            },
            onDismiss = { state.isAddOsmDialogOpen = false }
        )
    }

    if (state.isRoutePlannerOpen) {
        RoutePlannerDialog(
            userLat = safeUserLat,
            userLng = safeUserLng,
            currentCity = state.selectedCity,
            themeColors = themeColors,
            onCalculatePlannedRoute = { start, stops, dest ->
                state.currentMapMode = MapMode.NAVIGATION
                state.currentMapLayer = "navigation"
                val points = buildList {
                    val sc = OfflineMapManager.getCityCoordinates(start)
                    add(Triple(start, sc.latitude, sc.longitude))
                    stops.forEach { stopName ->
                        val stc = OfflineMapManager.getCityCoordinates(stopName)
                        add(Triple(stopName, stc.latitude, stc.longitude))
                    }
                    val dc = OfflineMapManager.getCityCoordinates(dest)
                    add(Triple(dest, dc.latitude, dc.longitude))
                }
                val wpJson = org.json.JSONArray().apply {
                    points.forEach { (name, la, lo) ->
                        put(org.json.JSONObject().apply {
                            put("name", name)
                            put("lat", la)
                            put("lng", lo)
                        })
                    }
                }.toString()
                val quotedWp = org.json.JSONObject.quote(wpJson)
                val script = "if (window.drawMultiStopRoute) { window.drawMultiStopRoute($quotedWp, '#00E5FF'); }"
                webViewRef?.post { webViewRef.evaluateJavascript(script, null) }
                val viaText = if (stops.isNotEmpty()) " عبر ${stops.joinToString(" ← ")}" else ""
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تخطيط ورسم المسار من $start$viaText إلى $dest 🛣️")
                }
            },
            onDismiss = { state.isRoutePlannerOpen = false }
        )
    }

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

    if (state.isMoreLayersSheetOpen) {
        val settingsVal = viewModel.settings.collectAsState().value
        val disabledSet = settingsVal.disabledMapLayers.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        com.serviseyem.ui.screens.map.components.MoreLayersSheet(
            state = state,
            onDismiss = { state.isMoreLayersSheetOpen = false },
            disabledLayerIds = disabledSet,
            themeColors = themeColors
        )
    }
}

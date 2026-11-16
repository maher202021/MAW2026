package com.serviseyem.ui.screens.map

import android.Manifest
import android.content.Context
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
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
                val startCoords = OfflineMapManager.getCityCoordinates(start)
                val destCoords = OfflineMapManager.getCityCoordinates(dest)
                val script = "if (window.drawRoute) { window.drawRoute(${startCoords.latitude}, ${startCoords.longitude}, ${destCoords.latitude}, ${destCoords.longitude}, '#00E5FF', 'مسار $start إلى $dest'); }"
                webViewRef?.post { webViewRef.evaluateJavascript(script, null) }
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تخطيط ورسم المسار من $start عبر ${stops.joinToString()} إلى $dest 🛣️")
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
        com.serviseyem.ui.screens.map.components.MoreLayersSheet(
            state = state,
            onDismiss = { state.isMoreLayersSheetOpen = false },
            themeColors = themeColors
        )
    }
}

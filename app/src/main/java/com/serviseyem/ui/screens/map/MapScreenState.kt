package com.serviseyem.ui.screens.map

import androidx.compose.runtime.*
import com.serviseyem.ui.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity

enum class MapMode(val label: String, val icon: String, val hint: String) {
    VELA_3D("Vela 3D", "🏙️", "خريطة شوارع ومباني ثلاثية الأبعاد تفاعلية"),
    AERIAL_OAM("الصور الجوية OAM", "🛰️", "صور جوية وأقمار صناعية فائقة الدقة"),
    AR_VPS("واقع معزز VPS", "📷", "الرؤية الحاسوبية المباشرة عبر الكاميرا"),
    NAVIGATION("الملاحة الذكية", "🧭", "ملاحة دقيقة خطوة بخطوة ومسارات الشوارع"),
    ALEPH_AI("المساعد البصري Aleph", "👁️", "استكشاف وتحليل بصري ذكي للمكان")
}

/**
 * 🗺️ MapScreenState
 * Encapsulates the UI and interaction states for Map, 3D, Satellite, AR VPS, and Radar views.
 */
@Stable
class MapScreenState(
    isRadarMode: Boolean = false,
    isHeatmapActive: Boolean = false,
    selectedCategory: String = "ALL",
    selectedCity: String = "الكل",
    searchQuery: String = "",
    maxRangeKm: Float = 25.0f,
    initialMode: MapMode = MapMode.VELA_3D
) {
    var currentMapMode by mutableStateOf(initialMode)
    var isRadarMode by mutableStateOf(isRadarMode)
    var isSatelliteMode by mutableStateOf(true)
    var currentMapLayer by mutableStateOf("esri_world_imagery")
    var isHeatmapActive by mutableStateOf(isHeatmapActive)
    var selectedCategory by mutableStateOf(selectedCategory)
    var selectedCity by mutableStateOf(selectedCity)
    var searchQuery by mutableStateOf(searchQuery)
    var maxRangeKm by mutableFloatStateOf(maxRangeKm)

    var zoomScale by mutableFloatStateOf(1.0f)
    var panOffset by mutableStateOf(androidx.compose.ui.geometry.Offset.Zero)
    var tiltAngle by mutableFloatStateOf(30f)
    var is3DBuildingsActive by mutableStateOf(true)

    // New Local Map Feature States
    var isGnssPanelOpen by mutableStateOf(false)
    var isDeadZoneOverlayActive by mutableStateOf(false)
    var isReliabilityLayerActive by mutableStateOf(false)
    var isRoadSafetyActive by mutableStateOf(false)
    var isAddOsmDialogOpen by mutableStateOf(false)
    var isRoutePlannerOpen by mutableStateOf(false)
    var isNightModeActive by mutableStateOf(false)
    var isNasaGibsActive by mutableStateOf(false)
    var gibsProduct by mutableStateOf("viirs_noaa20")
    var isEoxCloudlessActive by mutableStateOf(false)
    var isEsriImageryActive by mutableStateOf(false)
    var eumetEnabled by mutableStateOf(false)
    var eumetLayerName by mutableStateOf("")
    var isMoreLayersSheetOpen by mutableStateOf(false)
    var isEarthquakeLayerActive by mutableStateOf(false)
    var lastEarthquakeUpdateTime by mutableStateOf<Long?>(null)
    var earthquakeCount by mutableIntStateOf(0)
    var isEarthquakeBottomSheetOpen by mutableStateOf(false)
    var earthquakeUpdateTrigger by mutableIntStateOf(0)
    var isEarthquakeLoading by mutableStateOf(false)
    var isGroupTrackingActive by mutableStateOf(false)
    var currentGroupCode by mutableStateOf("")
    var isContextualSuggestionVisible by mutableStateOf(true)

    var selectedEntity by mutableStateOf<Any?>(null)
    var bookingProviderTarget by mutableStateOf<ProviderEntity?>(null)
    var pendingNavDestination by mutableStateOf<com.serviseyem.ui.screens.map.utils.StreetOrLandmark?>(null)
    var isAlephDialogOpen by mutableStateOf(false)
    var alephSelectedEntity by mutableStateOf<Any?>(null)

    val dynamicOffsets = mutableStateMapOf<String, Pair<Double, Double>>()

    fun resetSelection() {
        selectedEntity = null
        bookingProviderTarget = null
    }

    fun clearOffsets() {
        dynamicOffsets.clear()
    }

    companion object {
        val Saver: Saver<MapScreenState, *> = listSaver(
            save = {
                listOf(
                    it.isRadarMode,
                    it.isHeatmapActive,
                    it.selectedCategory,
                    it.selectedCity,
                    it.searchQuery,
                    it.maxRangeKm,
                    it.currentMapMode.name,
                    it.currentMapLayer,
                    it.tiltAngle,
                    it.is3DBuildingsActive,
                    it.isNightModeActive,
                    it.isNasaGibsActive,
                    it.gibsProduct,
                    it.isEoxCloudlessActive,
                    it.isEsriImageryActive,
                    it.eumetEnabled,
                    it.eumetLayerName,
                    it.isEarthquakeLayerActive,
                    it.isDeadZoneOverlayActive,
                    it.isReliabilityLayerActive,
                    it.isRoadSafetyActive,
                    it.isContextualSuggestionVisible
                )
            },
            restore = { saved ->
                val mode = try { MapMode.valueOf(saved.getOrNull(6) as? String ?: "VELA_3D") } catch (_: Exception) { MapMode.VELA_3D }
                MapScreenState(
                    isRadarMode = saved[0] as Boolean,
                    isHeatmapActive = saved[1] as Boolean,
                    selectedCategory = saved[2] as String,
                    selectedCity = saved[3] as String,
                    searchQuery = saved[4] as String,
                    maxRangeKm = (saved[5] as Number).toFloat(),
                    initialMode = mode
                ).apply {
                    currentMapLayer = (saved.getOrNull(7) as? String) ?: "esri_world_imagery"
                    tiltAngle = (saved.getOrNull(8) as? Number)?.toFloat() ?: 30f
                    is3DBuildingsActive = (saved.getOrNull(9) as? Boolean) ?: true
                    isNightModeActive = (saved.getOrNull(10) as? Boolean) ?: false
                    isNasaGibsActive = (saved.getOrNull(11) as? Boolean) ?: false
                    gibsProduct = (saved.getOrNull(12) as? String) ?: "viirs_noaa20"
                    isEoxCloudlessActive = (saved.getOrNull(13) as? Boolean) ?: false
                    isEsriImageryActive = (saved.getOrNull(14) as? Boolean) ?: false
                    eumetEnabled = (saved.getOrNull(15) as? Boolean) ?: false
                    eumetLayerName = (saved.getOrNull(16) as? String) ?: ""
                    isEarthquakeLayerActive = (saved.getOrNull(17) as? Boolean) ?: false
                    isDeadZoneOverlayActive = (saved.getOrNull(18) as? Boolean) ?: false
                    isReliabilityLayerActive = (saved.getOrNull(19) as? Boolean) ?: false
                    isRoadSafetyActive = (saved.getOrNull(20) as? Boolean) ?: false
                    isContextualSuggestionVisible = (saved.getOrNull(21) as? Boolean) ?: true
                }
            }
        )
    }
}

@Composable
fun rememberMapScreenState(): MapScreenState {
    return rememberSaveable(saver = MapScreenState.Saver) {
        MapScreenState()
    }
}


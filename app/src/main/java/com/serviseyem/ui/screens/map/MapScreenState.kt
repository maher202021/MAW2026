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
    var currentMapLayer by mutableStateOf("google_satellite")
    var isHeatmapActive by mutableStateOf(isHeatmapActive)
    var selectedCategory by mutableStateOf(selectedCategory)
    var selectedCity by mutableStateOf(selectedCity)
    var searchQuery by mutableStateOf(searchQuery)
    var maxRangeKm by mutableFloatStateOf(maxRangeKm)

    var zoomScale by mutableFloatStateOf(1.0f)
    var panOffset by mutableStateOf(androidx.compose.ui.geometry.Offset.Zero)
    var tiltAngle by mutableFloatStateOf(30f)
    var is3DBuildingsActive by mutableStateOf(true)

    var selectedEntity by mutableStateOf<Any?>(null)
    var bookingProviderTarget by mutableStateOf<ProviderEntity?>(null)
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
            save = { listOf(it.isRadarMode, it.isHeatmapActive, it.selectedCategory, it.selectedCity, it.searchQuery, it.maxRangeKm, it.currentMapMode.name) },
            restore = {
                val mode = try { MapMode.valueOf(it.getOrNull(6) as? String ?: "VELA_3D") } catch (e: Exception) { MapMode.VELA_3D }
                MapScreenState(
                    isRadarMode = it[0] as Boolean,
                    isHeatmapActive = it[1] as Boolean,
                    selectedCategory = it[2] as String,
                    selectedCity = it[3] as String,
                    searchQuery = it[4] as String,
                    maxRangeKm = (it[5] as Number).toFloat(),
                    initialMode = mode
                )
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


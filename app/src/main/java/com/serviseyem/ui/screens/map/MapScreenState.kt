package com.serviseyem.ui.screens.map

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.navigation.NavWaypoint

enum class MapMode { OSM, HOT, TOPO, SATELLITE, VELA_3D, AR_VPS, ALEPH_ASSISTANT }

@Stable
class MapScreenState(
    isRadarMode: Boolean = false,
    isHeatmapActive: Boolean = false,
    selectedCategory: String = "ALL",
    selectedCity: String = "الكل",
    searchQuery: String = "",
    maxRangeKm: Float = 25.0f,
    initialMapMode: MapMode = MapMode.SATELLITE
) {
    var isRadarMode by mutableStateOf(isRadarMode)
    var isHeatmapActive by mutableStateOf(isHeatmapActive)
    var isOfflineCacheActive by mutableStateOf(true)
    var showStreetAlertDialog by mutableStateOf(false)
    var currentMapMode by mutableStateOf(initialMapMode)
    var isAlephAssistantOpen by mutableStateOf(false)
    var showLayerDialog by mutableStateOf(false)
    var selectedCategory by mutableStateOf(selectedCategory)
    var selectedCity by mutableStateOf(selectedCity)
    var searchQuery by mutableStateOf(searchQuery)
    var maxRangeKm by mutableFloatStateOf(maxRangeKm)
    var zoomScale by mutableFloatStateOf(1.0f)
    var panOffset by mutableStateOf(androidx.compose.ui.geometry.Offset.Zero)
    var selectedEntity by mutableStateOf<Any?>(null)
    var bookingProviderTarget by mutableStateOf<ProviderEntity?>(null)
    var isNavigating by mutableStateOf(false)
    val dynamicOffsets = mutableStateMapOf<String, Pair<Double, Double>>()
    val selectedWaypoints = mutableStateListOf<NavWaypoint>()

    /** القائمة النهائية التي تُمرَّر إلى NavigationScreen (تُثبَّت لحظة بدء الملاحة). */
    var navWaypoints by mutableStateOf<List<NavWaypoint>>(emptyList())

    /** يحوّل كياناً (مزود/متجر/عقار/نقطة) إلى محطة، أو null إن كانت الإحداثيات غير صالحة. */
    fun entityToWaypoint(entity: Any?): NavWaypoint? {
        val triple = when (entity) {
            is NavWaypoint -> return if (isValid(entity.latitude, entity.longitude)) entity else null
            is ProviderEntity -> Triple(entity.name, entity.latitude, entity.longitude)
            is StoreEntity -> Triple(entity.name, entity.latitude, entity.longitude)
            is PropertyEntity -> Triple(entity.title, entity.latitude, entity.longitude)
            else -> return null
        }
        val (name, lat, lng) = triple
        return if (isValid(lat, lng)) NavWaypoint(name = name.ifBlank { "وجهة" }, latitude = lat, longitude = lng) else null
    }

    private fun isValid(lat: Double, lng: Double) =
        !lat.isNaN() && !lng.isNaN() && lat in -90.0..90.0 && lng in -180.0..180.0 && !(lat == 0.0 && lng == 0.0)

    private fun same(a: NavWaypoint, b: NavWaypoint) =
        kotlin.math.abs(a.latitude - b.latitude) < 1e-5 && kotlin.math.abs(a.longitude - b.longitude) < 1e-5

    fun addWaypoint(entity: Any) {
        val wp = entityToWaypoint(entity) ?: return
        if (selectedWaypoints.none { same(it, wp) }) selectedWaypoints.add(wp)
    }

    fun removeWaypoint(waypoint: NavWaypoint) { selectedWaypoints.remove(waypoint) }
    fun clearWaypoints() { selectedWaypoints.clear() }

    /**
     * يبدأ الملاحة: المحطات المختارة + الوجهة (target) كمحطة أخيرة.
     * يعيد false إن لم توجد أي وجهة صالحة (فلا تبدأ ملاحة وهمية).
     */
    fun startNavigation(target: Any?): Boolean {
        val list = selectedWaypoints.toMutableList()
        entityToWaypoint(target)?.let { dest -> if (list.none { same(it, dest) }) list.add(dest) }
        if (list.isEmpty()) return false
        navWaypoints = list
        isNavigating = true
        return true
    }

    fun stopNavigation() {
        isNavigating = false
        navWaypoints = emptyList()
        selectedWaypoints.clear()
    }

    /** لا تمسح المحطات المختارة (كان خطأ: إغلاق النافذة السفلية يمحو القائمة). */
    fun resetSelection() {
        selectedEntity = null
        bookingProviderTarget = null
    }

    fun clearOffsets() { dynamicOffsets.clear() }

    companion object {
        private fun encode(list: List<NavWaypoint>): String =
            list.joinToString(";;") { "${it.name.replace(";", " ").replace("|", " ")}|${it.latitude}|${it.longitude}" }

        private fun decode(s: String): List<NavWaypoint> =
            s.split(";;").filter { it.isNotBlank() }.mapNotNull { part ->
                val p = part.split("|")
                if (p.size != 3) return@mapNotNull null
                val lat = p[1].toDoubleOrNull() ?: return@mapNotNull null
                val lng = p[2].toDoubleOrNull() ?: return@mapNotNull null
                NavWaypoint(name = p[0], latitude = lat, longitude = lng)
            }

        val Saver: Saver<MapScreenState, *> = listSaver(
            save = {
                listOf(
                    it.isRadarMode, it.isHeatmapActive, it.selectedCategory, it.selectedCity, it.searchQuery,
                    it.maxRangeKm, it.currentMapMode.name, it.isNavigating, encode(it.navWaypoints)
                )
            },
            restore = {
                val mode = try { MapMode.valueOf(it.getOrNull(6) as? String ?: "SATELLITE") } catch (e: Exception) { MapMode.SATELLITE }
                MapScreenState(
                    isRadarMode = it[0] as Boolean,
                    isHeatmapActive = it[1] as Boolean,
                    selectedCategory = it[2] as String,
                    selectedCity = it[3] as String,
                    searchQuery = it[4] as String,
                    maxRangeKm = (it[5] as Number).toFloat(),
                    initialMapMode = mode
                ).also { st ->
                    val wps = decode(it.getOrNull(8) as? String ?: "")
                    if ((it.getOrNull(7) as? Boolean) == true && wps.isNotEmpty()) {
                        st.navWaypoints = wps
                        st.isNavigating = true
                    }
                }
            }
        )
    }
}

@Composable
fun rememberMapScreenState(): MapScreenState = rememberSaveable(saver = MapScreenState.Saver) { MapScreenState() }

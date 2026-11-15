package com.serviseyem.navigation

import android.content.Context
import android.util.Log
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.abs
import kotlin.math.max

/**
 * 🗺️ RealValhallaNavigator
 * محرك الملاحة الحقيقي المعتمد على C++ و Valhalla Mobile:
 * - يقرأ حزم شوارع OpenStreetMap الحقيقية من ملف valhalla_tiles.tar.
 * - يفوض حساب المسار وفك تشفير Polyline6 والبحث لمكونات متخصصة.
 * - يعود للمسار المباشر الصريح فقط عند عدم تنزيل حزم الأوفلاين بعد، مع تنبيه واضح.
 */
class RealValhallaNavigator(private val context: Context) {

    data class SearchAddressResult(
        val displayName: String,
        val shortName: String,
        val latitude: Double,
        val longitude: Double,
        val category: String = "place",
        val icon: String = "📍"
    )

    companion object {
        @Volatile
        var lastSearchError: String? = null

        suspend fun searchAddressAndPlaces(
            query: String,
            providers: List<ProviderEntity> = emptyList(),
            stores: List<StoreEntity> = emptyList(),
            userLat: Double = 0.0,
            userLng: Double = 0.0
        ): List<SearchAddressResult> =
            ValhallaPlaceSearchHelper.searchAddressAndPlaces(
                query = query,
                providers = providers,
                stores = stores,
                userLat = userLat,
                userLng = userLng,
                onError = { lastSearchError = it }
            )
    }

    private val TAG = "RealValhallaNavigator"
    private val mutex = Mutex()
    private var isEngineInitialized = false
    private var valhallaBridge: ValhallaNativeBridge? = null
    private var currentConfigPath: String? = null

    init {
        initializeEngine()
    }

    fun initializeEngine() {
        try {
            val valhallaDir = File(context.filesDir, OfflineMapLifecycleManager.VALHALLA_DIR)
            val marker = File(valhallaDir, OfflineMapLifecycleManager.MARKER_FILE)
            val archive = File(valhallaDir, OfflineMapLifecycleManager.TILES_ARCHIVE)

            if (marker.exists() && archive.exists() && archive.length() >= 10_000_000L) {
                val configFile = ensureConfigFile(valhallaDir, archive)
                currentConfigPath = configFile.absolutePath

                try {
                    valhallaBridge?.close()
                    valhallaBridge = ValhallaNativeBridge(configFile.absolutePath)
                    isEngineInitialized = true
                    Log.d(TAG, "Valhalla C++ Native Engine successfully initialized with archive: ${archive.absolutePath}")
                } catch (e: Throwable) {
                    Log.e(TAG, "Failed to instantiate ValhallaNativeBridge: ${e.message}")
                    isEngineInitialized = false
                }
            } else {
                Log.i(TAG, "Valhalla tiles not found or smaller than 10MB. Offline navigation pending download.")
                isEngineInitialized = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Valhalla engine initialization error: ${e.message}")
            isEngineInitialized = false
        }
    }

    private fun ensureConfigFile(valhallaDir: File, archiveFile: File): File {
        val configFile = File(valhallaDir, "valhalla.json")
        val configJson = JSONObject().apply {
            put("mjolnir", JSONObject().apply {
                put("tile_dir", valhallaDir.absolutePath)
                put("tile_extract", archiveFile.absolutePath)
                val adminDb = File(valhallaDir, "admin.sqlite")
                if (adminDb.exists()) put("admin", adminDb.absolutePath)
                val tzDb = File(valhallaDir, "tz.sqlite")
                if (tzDb.exists()) put("timezone", tzDb.absolutePath)
            })
            put("loki", JSONObject().apply {
                put("actions", JSONArray(listOf("route", "locate", "sources_to_targets")))
            })
            put("thor", JSONObject().apply {
                put("source_to_target_algorithm", "select_optimal")
            })
            put("meili", JSONObject().apply {
                put("customizable", JSONArray(listOf("mode")))
            })
        }
        configFile.writeText(configJson.toString(2))
        return configFile
    }

    fun isReady(): Boolean = isEngineInitialized

    fun refreshEngine() {
        initializeEngine()
    }

    suspend fun calculateRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        destinationName: String = "الوجهة"
    ): NavigationRouteResult = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!isEngineInitialized || valhallaBridge == null) {
                initializeEngine()
            }

            val bridge = valhallaBridge
            if (!isEngineInitialized || bridge == null) {
                return@withContext ValhallaRoutingEngine.calculateBasicDirectRoute(
                    startLat, startLon, endLat, endLon, destinationName
                )
            }

            try {
                ValhallaRoutingEngine.calculateValhallaOfflineRoute(
                    bridge, startLat, startLon, endLat, endLon, destinationName
                )
            } catch (e: Exception) {
                Log.e(TAG, "Valhalla native route error, falling back to direct line: ${e.message}")
                ValhallaRoutingEngine.calculateBasicDirectRoute(
                    startLat, startLon, endLat, endLon, destinationName
                )
            }
        }
    }

    fun isOffRoute(
        currentLat: Double,
        currentLon: Double,
        accuracyMeters: Float,
        routePoints: List<RoutePoint>
    ): Boolean {
        if (routePoints.size < 2) return false

        val threshold = max(35.0, (2.0 * accuracyMeters).toDouble())
        var minCrossTrack = Double.MAX_VALUE

        for (i in 0 until routePoints.size - 1) {
            val dist = abs(
                HaversinePrecisionNavigator.crossTrackDistance(
                    currentLat, currentLon,
                    routePoints[i].lat, routePoints[i].lon,
                    routePoints[i + 1].lat, routePoints[i + 1].lon
                )
            )
            if (dist < minCrossTrack) {
                minCrossTrack = dist
            }
        }

        return minCrossTrack > threshold
    }

    fun close() {
        try {
            valhallaBridge?.close()
            valhallaBridge = null
        } catch (_: Exception) {}
        isEngineInitialized = false
    }
}

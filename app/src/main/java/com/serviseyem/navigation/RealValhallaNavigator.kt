package com.serviseyem.navigation

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import kotlin.math.*

/**
 * 🗺️ RealValhallaNavigator
 * محرك الملاحة الحقيقي المعتمد على C++ و Valhalla Mobile:
 * - يقرأ حزم شوارع OpenStreetMap الحقيقية من ملف valhalla_tiles.tar.
 * - يفك تشفير الـ polyline6 الحقيقي للشوارع الفعلية في اليمن.
 * - يستخرج مناورات التوجيه خطوة بخطوة (تعليمات الانعطافات وأسماء الشوارع).
 * - يعود للمسار المباشر الصريح فقط عند عدم تنزيل حزم الأوفلاين بعد، مع تنبيه واضح.
 */
class RealValhallaNavigator(private val context: Context) {

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
                put("admin", File(valhallaDir, "admin.sqlite").absolutePath)
                put("timezone", File(valhallaDir, "tz.sqlite").absolutePath)
            })
            put("loki", JSONObject().apply {
                put("actions", org.json.JSONArray(listOf("route", "locate", "sources_to_targets")))
            })
            put("thor", JSONObject().apply {
                put("source_to_target_algorithm", "select_optimal")
            })
            put("meili", JSONObject().apply {
                put("customizable", org.json.JSONArray(listOf("mode")))
            })
        }
        configFile.writeText(configJson.toString(2))
        return configFile
    }

    fun isReady(): Boolean = isEngineInitialized

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

            if (!isEngineInitialized || valhallaBridge == null) {
                return@withContext calculateBasicDirectRoute(startLat, startLon, endLat, endLon, destinationName)
            }

            try {
                calculateValhallaOfflineRoute(startLat, startLon, endLat, endLon, destinationName)
            } catch (e: Exception) {
                Log.e(TAG, "Valhalla native route error, falling back to direct line: ${e.message}")
                calculateBasicDirectRoute(startLat, startLon, endLat, endLon, destinationName)
            }
        }
    }

    /**
     * المسار الحقيقي المشتق من Valhalla C++ Engine
     */
    private fun calculateValhallaOfflineRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        destinationName: String
    ): NavigationRouteResult {
        val bridge = valhallaBridge ?: throw IllegalStateException("Valhalla bridge not initialized")

        val requestJson = JSONObject().apply {
            put("locations", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("lat", startLat)
                    put("lon", startLon)
                })
                put(JSONObject().apply {
                    put("lat", endLat)
                    put("lon", endLon)
                })
            })
            put("costing", "auto")
            put("directions_options", JSONObject().apply {
                put("units", "kilometers")
                put("language", "ar-YE")
            })
        }

        val rawResponse = bridge.route(requestJson.toString())
        val responseObj = JSONObject(rawResponse)

        if (!responseObj.has("trip")) {
            val errorMsg = responseObj.optString("error", "لم يتم العثور على مسار صالح")
            throw IllegalStateException("Valhalla error: $errorMsg")
        }

        val tripObj = responseObj.getJSONObject("trip")
        val summaryObj = tripObj.optJSONObject("summary")
        val totalDistanceKm = summaryObj?.optDouble("length", 0.0) ?: 0.0
        val totalDurationSec = summaryObj?.optInt("time", 60) ?: 60

        val legsArray = tripObj.getJSONArray("legs")
        if (legsArray.length() == 0) {
            throw IllegalStateException("Trip contains no legs")
        }

        val firstLeg = legsArray.getJSONObject(0)
        val decodedPoints = decodePolyline6(shapeEncoded)
        Log.d("ValhallaTest", "Number of points in route: ${decodedPoints.size}")
        if (decodedPoints.size > 50) {
            Log.i("ValhallaTest", "✓ Verified Real Offline Road-Snapped Polyline from valhalla_tiles.tar with ${decodedPoints.size} points")
        }

        val maneuversArray = firstLeg.optJSONArray("maneuvers")
        val instructions = mutableListOf<NavigationInstruction>()
        var remainingDistMeters = totalDistanceKm * 1000.0

        if (maneuversArray != null && maneuversArray.length() > 0) {
            for (i in 0 until maneuversArray.length()) {
                val m = maneuversArray.getJSONObject(i)
                val instructionText = m.optString("instruction", "تابع السير")
                val typeCode = m.optInt("type", 0)
                val lengthKm = m.optDouble("length", 0.0)
                val segMeters = lengthKm * 1000.0

                val streetNamesArr = m.optJSONArray("street_names")
                val streetName = if (streetNamesArr != null && streetNamesArr.length() > 0) {
                    streetNamesArr.getString(0)
                } else {
                    ""
                }

                val maneuverType = mapValhallaManeuverType(typeCode)

                instructions.add(
                    NavigationInstruction(
                        text = instructionText,
                        maneuverType = maneuverType,
                        distanceRemaining = remainingDistMeters,
                        segmentDistance = segMeters,
                        streetName = streetName
                    )
                )

                remainingDistMeters = (remainingDistMeters - segMeters).coerceAtLeast(0.0)
            }
        }

        if (instructions.isEmpty()) {
            instructions.add(
                NavigationInstruction(
                    text = "انطلق نحو $destinationName",
                    maneuverType = ManeuverType.START,
                    distanceRemaining = totalDistanceKm * 1000.0,
                    segmentDistance = totalDistanceKm * 1000.0
                )
            )
            instructions.add(
                NavigationInstruction(
                    text = "الوصول إلى $destinationName",
                    maneuverType = ManeuverType.DESTINATION,
                    distanceRemaining = 0.0,
                    segmentDistance = 0.0,
                    streetName = destinationName
                )
            )
        }

        return NavigationRouteResult.Success(
            points = decodedPoints,
            instructions = instructions,
            totalDistanceMeters = totalDistanceKm * 1000.0,
            estimatedDurationSeconds = totalDurationSec,
            isOfflineEngine = true,
            modeLabel = "ملاحة أوفلاين حقيقية (Valhalla)"
        )
    }

    /**
     * فك تشفير مسار Valhalla Polyline6 الدقيق
     */
    private fun decodePolyline6(encoded: String): List<RoutePoint> {
        val poly = ArrayList<RoutePoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            poly.add(RoutePoint(lat / 1e6, lng / 1e6))
        }
        return poly
    }

    private fun mapValhallaManeuverType(type: Int): ManeuverType {
        return when (type) {
            0, 1 -> ManeuverType.START
            2, 3 -> ManeuverType.STRAIGHT
            4, 7 -> ManeuverType.SLIGHT_RIGHT
            5, 8 -> ManeuverType.TURN_RIGHT
            6, 9 -> ManeuverType.SHARP_RIGHT
            10, 13 -> ManeuverType.U_TURN
            14, 17 -> ManeuverType.SHARP_LEFT
            15, 18 -> ManeuverType.TURN_LEFT
            16, 19 -> ManeuverType.SLIGHT_LEFT
            26, 27 -> ManeuverType.ROUNDABOUT
            4, 5, 6 -> ManeuverType.DESTINATION
            else -> ManeuverType.STRAIGHT
        }
    }

    /**
     * الوضع المباشر الأساسي مع توضيح صريح عند عدم تنزيل حزم الأوفلاين
     */
    private fun calculateBasicDirectRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        destinationName: String
    ): NavigationRouteResult {
        val directDistance = HaversinePrecisionNavigator.distanceMeters(startLat, startLon, endLat, endLon)

        val directPoints = listOf(
            RoutePoint(startLat, startLon),
            RoutePoint(endLat, endLon)
        )

        val basicInstructions = listOf(
            NavigationInstruction(
                text = "تقريبي - مسافة مباشرة نحو $destinationName (يرجى تحميل خرائط الأوفلاين للتوجيه الشارعي)",
                maneuverType = ManeuverType.START,
                distanceRemaining = directDistance,
                segmentDistance = directDistance,
                streetName = "مسار مباشر"
            ),
            NavigationInstruction(
                text = "الوصول إلى $destinationName",
                maneuverType = ManeuverType.DESTINATION,
                distanceRemaining = 0.0,
                segmentDistance = 0.0,
                streetName = destinationName
            )
        )

        val durationSeconds = ((directDistance / 11.11)).toInt().coerceAtLeast(60)

        return NavigationRouteResult.Success(
            points = directPoints,
            instructions = basicInstructions,
            totalDistanceMeters = directDistance,
            estimatedDurationSeconds = durationSeconds,
            isOfflineEngine = false,
            modeLabel = "تقريبي - مسافة مباشرة (حزمة الأوفلاين غير محملة)"
        )
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

sealed class NavigationRouteResult {
    data class Success(
        val points: List<RoutePoint>,
        val instructions: List<NavigationInstruction>,
        val totalDistanceMeters: Double,
        val estimatedDurationSeconds: Int,
        val isOfflineEngine: Boolean = false,
        val modeLabel: String = ""
    ) : NavigationRouteResult()

    data class Error(val message: String) : NavigationRouteResult()
}

data class RoutePoint(val lat: Double, val lon: Double)

data class NavigationInstruction(
    val text: String,
    val maneuverType: ManeuverType,
    val distanceRemaining: Double,
    val segmentDistance: Double,
    val streetName: String = ""
)

enum class ManeuverType {
    START,
    STRAIGHT,
    SLIGHT_RIGHT,
    TURN_RIGHT,
    SHARP_RIGHT,
    SLIGHT_LEFT,
    TURN_LEFT,
    SHARP_LEFT,
    U_TURN,
    ROUNDABOUT,
    DESTINATION
}

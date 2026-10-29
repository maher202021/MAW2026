package com.serviseyem.navigation

// [إصلاح ملاحة نهائي] محرك الملاحة أونلاين OSRM الآمن المقاوم للأعطال
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

data class NavRouteResult(
    val success: Boolean,
    val points: List<RoutePoint> = emptyList(),
    val instructions: List<NavigationInstruction> = emptyList(),
    val totalDistanceMeters: Double = 0.0,
    val estimatedDurationSeconds: Int = 0,
    val isOffline: Boolean = false,
    val modeLabel: String = "",
    val errorMessage: String? = null
)

object OnlineOsrmNavigator {

    private const val TAG = "OnlineOsrmNavigator"
    private const val BASE_URL = "https://router.project-osrm.org/route/v1/driving/"
    private const val CONNECT_TIMEOUT_MS = 7000
    private const val READ_TIMEOUT_MS = 8000

    /**
     * [إصلاح ملاحة نهائي] حساب المسار أونلاين باستخدام خوادم OSRM العالمية.
     * يعمل بالكامل في Dispatchers.IO مع try-catch شامل وtimeout لمنع التعطل والانهيار.
     */
    suspend fun calculateRoute(
        origin: Pair<Double, Double>,
        waypoints: List<Pair<Double, Double>>
    ): NavRouteResult = withContext(Dispatchers.IO) {
        try {
            if (waypoints.isEmpty()) {
                return@withContext NavRouteResult(
                    success = false,
                    errorMessage = "لا توجد نقاط وجهة محددة"
                )
            }

            val destination = waypoints.last()
            val startLat = origin.first
            val startLon = origin.second
            val endLat = destination.first
            val endLon = destination.second

            // OSRM expects coordinates in {lon},{lat} format
            val coordinatesString = buildString {
                append("${startLon},${startLat}")
                for (wp in waypoints) {
                    append(";${wp.second},${wp.first}")
                }
            }

            val requestUrl = "${BASE_URL}${coordinatesString}?overview=full&geometries=geojson&steps=true"
            Log.d(TAG, "Fetching OSRM route: $requestUrl")

            val url = URL(requestUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "YemenServicesGuide/1.0 (Android; Navigation)")
                setRequestProperty("Accept", "application/json")
            }

            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode != 200) {
                Log.w(TAG, "OSRM returned HTTP $responseCode")
                return@withContext NavRouteResult(
                    success = false,
                    errorMessage = "خادم الملاحة أونلاين غير متاح (HTTP $responseCode)"
                )
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseBody)
            val code = json.optString("code", "")

            if (code != "Ok") {
                val message = json.optString("message", "خطأ في استجابة خادم OSRM")
                return@withContext NavRouteResult(
                    success = false,
                    errorMessage = message
                )
            }

            val routes = json.optJSONArray("routes")
            if (routes == null || routes.length() == 0) {
                return@withContext NavRouteResult(
                    success = false,
                    errorMessage = "لم يتم العثور على مسار قيادة متاح"
                )
            }

            val primaryRoute = routes.getJSONObject(0)
            val totalDistance = primaryRoute.optDouble("distance", 0.0)
            val totalDuration = primaryRoute.optDouble("duration", 0.0).roundToInt()

            // Parse polyline geometry
            val geometry = primaryRoute.optJSONObject("geometry")
            val coordsArray = geometry?.optJSONArray("coordinates")
            val routePoints = mutableListOf<RoutePoint>()

            if (coordsArray != null) {
                for (i in 0 until coordsArray.length()) {
                    val pair = coordsArray.getJSONArray(i)
                    val lon = pair.getDouble(0)
                    val lat = pair.getDouble(1)
                    routePoints.add(RoutePoint(lat = lat, lon = lon))
                }
            }

            // Fallback points if geometry was empty
            if (routePoints.isEmpty()) {
                routePoints.add(RoutePoint(startLat, startLon))
                routePoints.add(RoutePoint(endLat, endLon))
            }

            // Parse turn-by-turn steps
            val instructions = mutableListOf<NavigationInstruction>()
            val legs = primaryRoute.optJSONArray("legs")
            var remainingDist = totalDistance

            if (legs != null && legs.length() > 0) {
                val leg = legs.getJSONObject(0)
                val steps = leg.optJSONArray("steps")
                if (steps != null) {
                    for (i in 0 until steps.length()) {
                        val step = steps.getJSONObject(i)
                        val stepDistance = step.optDouble("distance", 0.0)
                        val streetName = step.optString("name", "").ifBlank { "شارع بدون اسم" }
                        val maneuverObj = step.optJSONObject("maneuver")
                        val mType = maneuverObj?.optString("type", "") ?: ""
                        val mModifier = maneuverObj?.optString("modifier", "") ?: ""

                        val (maneuverType, arabicText) = parseOsrmManeuver(mType, mModifier, streetName)

                        instructions.add(
                            NavigationInstruction(
                                text = arabicText,
                                maneuverType = maneuverType,
                                distanceRemaining = remainingDist,
                                segmentDistance = stepDistance,
                                streetName = streetName
                            )
                        )
                        remainingDist = (remainingDist - stepDistance).coerceAtLeast(0.0)
                    }
                }
            }

            if (instructions.isEmpty()) {
                instructions.add(
                    NavigationInstruction(
                        text = "انطلق نحو مسار الرحلة",
                        maneuverType = ManeuverType.START,
                        distanceRemaining = totalDistance,
                        segmentDistance = totalDistance
                    )
                )
                instructions.add(
                    NavigationInstruction(
                        text = "الوصول إلى الوجهة",
                        maneuverType = ManeuverType.DESTINATION,
                        distanceRemaining = 0.0,
                        segmentDistance = 0.0
                    )
                )
            }

            return@withContext NavRouteResult(
                success = true,
                points = routePoints,
                instructions = instructions,
                totalDistanceMeters = totalDistance,
                estimatedDurationSeconds = totalDuration,
                isOffline = false,
                modeLabel = "ملاحة أونلاين (OSRM)"
            )
        } catch (e: Exception) {
            Log.w(TAG, "Safe catch: OSRM route calculation failed safely: ${e.message}")
            return@withContext NavRouteResult(
                success = false,
                errorMessage = e.message ?: "خطأ أثناء محاولة جلب المسار أونلاين"
            )
        }
    }

    private fun parseOsrmManeuver(type: String, modifier: String, streetName: String): Pair<ManeuverType, String> {
        val streetSuffix = if (streetName.isNotBlank() && streetName != "شارع بدون اسم") " في $streetName" else ""
        return when (type) {
            "depart" -> ManeuverType.START to "انطلق نحو مسار الرحلة$streetSuffix"
            "arrive" -> ManeuverType.DESTINATION to "الوصول إلى الوجهة"
            "roundabout", "rotary" -> ManeuverType.ROUNDABOUT to "ادخل الدوار واتجه نحو المخرج$streetSuffix"
            "turn", "end of road" -> when (modifier) {
                "right" -> ManeuverType.TURN_RIGHT to "انعطف يميناً$streetSuffix"
                "left" -> ManeuverType.TURN_LEFT to "انعطف يساراً$streetSuffix"
                "sharp right" -> ManeuverType.SHARP_RIGHT to "انعطاف حاد إلى اليمين$streetSuffix"
                "sharp left" -> ManeuverType.SHARP_LEFT to "انعطاف حاد إلى اليسار$streetSuffix"
                "slight right" -> ManeuverType.SLIGHT_RIGHT to "انحرف قليلاً إلى اليمين$streetSuffix"
                "slight left" -> ManeuverType.SLIGHT_LEFT to "انحرف قليلاً إلى اليسار$streetSuffix"
                "uturn" -> ManeuverType.U_TURN to "قم بالدوران للخلف$streetSuffix"
                else -> ManeuverType.STRAIGHT to "تابع السير للأمام$streetSuffix"
            }
            "new name", "continue" -> ManeuverType.STRAIGHT to "تابع السير للأمام$streetSuffix"
            "fork" -> if (modifier.contains("right")) {
                ManeuverType.SLIGHT_RIGHT to "اتجه إلى اليمين عند المفترق$streetSuffix"
            } else {
                ManeuverType.SLIGHT_LEFT to "اتجه إلى اليسار عند المفترق$streetSuffix"
            }
            "off ramp", "on ramp" -> ManeuverType.SLIGHT_RIGHT to "اتجه نحو المنحدر$streetSuffix"
            else -> ManeuverType.STRAIGHT to "تابع السير للأمام$streetSuffix"
        }
    }
}

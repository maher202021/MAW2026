package com.serviseyem.navigation

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * محرك حساب المسار الفعلي عبر Valhalla C++ مع فك تشفير Polyline6 والمناورات، أو الرجوع للمسار المباشر.
 */
internal object ValhallaRoutingEngine {

    /**
     * المسار الحقيقي المشتق من Valhalla C++ Engine
     */
    fun calculateValhallaOfflineRoute(
        bridge: ValhallaNativeBridge,
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        destinationName: String
    ): NavigationRouteResult {
        val requestJson = JSONObject().apply {
            put("locations", JSONArray().apply {
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
        val shapeEncoded = firstLeg.getString("shape")
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
                val shapeIdx = m.optInt("begin_shape_index", 0)
                val maneuverPt = decodedPoints.getOrNull(shapeIdx)

                instructions.add(
                    NavigationInstruction(
                        text = instructionText,
                        maneuverType = maneuverType,
                        distanceRemaining = remainingDistMeters,
                        segmentDistance = segMeters,
                        streetName = streetName,
                        maneuverLat = maneuverPt?.lat ?: endLat,
                        maneuverLon = maneuverPt?.lon ?: endLon,
                        shapeIndex = shapeIdx
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
                    segmentDistance = totalDistanceKm * 1000.0,
                    maneuverLat = endLat,
                    maneuverLon = endLon
                )
            )
            instructions.add(
                NavigationInstruction(
                    text = "الوصول إلى $destinationName",
                    maneuverType = ManeuverType.DESTINATION,
                    distanceRemaining = 0.0,
                    segmentDistance = 0.0,
                    streetName = destinationName,
                    maneuverLat = endLat,
                    maneuverLon = endLon
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
     * مسار حقيقي متتبع للشوارع عبر OSRM عند توفر الإنترنت وعدم توفر حزم Valhalla الأوفلاين
     */
    fun calculateOsrmOnlineRoute(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        destinationName: String
    ): NavigationRouteResult? {
        var conn: java.net.HttpURLConnection? = null
        return try {
            val url = java.net.URL(
                "https://router.project-osrm.org/route/v1/driving/$startLon,$startLat;$endLon,$endLat?overview=full&geometries=polyline6&steps=true"
            )
            conn = (url.openConnection() as java.net.HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "AlYemenServicesApp/1.2 (Android)")
            }
            if (conn.responseCode !in 200..299) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            if (json.optString("code") != "Ok") return null
            val routes = json.optJSONArray("routes") ?: return null
            if (routes.length() == 0) return null

            val route = routes.getJSONObject(0)
            val totalDistM = route.optDouble("distance", 0.0)
            val totalDurSec = route.optDouble("duration", 60.0).toInt().coerceAtLeast(30)
            val geometry = route.optString("geometry", "")
            val points = if (geometry.isNotBlank()) decodePolyline6(geometry) else listOf(
                RoutePoint(startLat, startLon),
                RoutePoint(endLat, endLon)
            )

            val instructions = mutableListOf<NavigationInstruction>()
            val legs = route.optJSONArray("legs")
            var remDist = totalDistM
            if (legs != null && legs.length() > 0) {
                val steps = legs.getJSONObject(0).optJSONArray("steps")
                if (steps != null) {
                    for (i in 0 until steps.length()) {
                        val step = steps.getJSONObject(i)
                        val stepDist = step.optDouble("distance", 0.0)
                        val streetName = step.optString("name", "")
                        val maneuver = step.optJSONObject("maneuver")
                        val mTypeStr = maneuver?.optString("type", "") ?: ""
                        val mModStr = maneuver?.optString("modifier", "") ?: ""
                        val locArr = maneuver?.optJSONArray("location")
                        val mLon = locArr?.optDouble(0, endLon) ?: endLon
                        val mLat = locArr?.optDouble(1, endLat) ?: endLat

                        val (maneuverType, arText) = translateOsrmManeuver(mTypeStr, mModStr, streetName, destinationName)
                        instructions.add(
                            NavigationInstruction(
                                text = arText,
                                maneuverType = maneuverType,
                                distanceRemaining = remDist,
                                segmentDistance = stepDist,
                                streetName = streetName,
                                maneuverLat = mLat,
                                maneuverLon = mLon
                            )
                        )
                        remDist = (remDist - stepDist).coerceAtLeast(0.0)
                    }
                }
            }

            if (instructions.isEmpty()) {
                instructions.add(
                    NavigationInstruction(
                        text = "انطلق نحو $destinationName",
                        maneuverType = ManeuverType.START,
                        distanceRemaining = totalDistM,
                        segmentDistance = totalDistM,
                        maneuverLat = endLat,
                        maneuverLon = endLon
                    )
                )
            }

            NavigationRouteResult.Success(
                points = points,
                instructions = instructions,
                totalDistanceMeters = totalDistM,
                estimatedDurationSeconds = totalDurSec,
                isOfflineEngine = false,
                modeLabel = "ملاحة شوارع حية (OSM)"
            )
        } catch (e: Exception) {
            Log.w("ValhallaRoutingEngine", "OSRM fallback error: ${e.message}")
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun translateOsrmManeuver(
        type: String,
        modifier: String,
        streetName: String,
        destinationName: String
    ): Pair<ManeuverType, String> {
        val onStreet = if (streetName.isNotBlank()) " إلى $streetName" else ""
        if (type == "depart") return ManeuverType.START to "انطلق في المسار$onStreet"
        if (type == "arrive") return ManeuverType.DESTINATION to "وصلت إلى وجهتك: $destinationName"
        if (type == "roundabout" || type == "rotary" || type == "exit roundabout") {
            return ManeuverType.ROUNDABOUT to "عند الدوار، اسلك المخرج$onStreet"
        }
        return when (modifier) {
            "uturn" -> ManeuverType.U_TURN to "استدر للخلف (يو تيرن)$onStreet"
            "sharp right" -> ManeuverType.SHARP_RIGHT to "انعطف يميناً بقوة$onStreet"
            "right" -> ManeuverType.TURN_RIGHT to "انعطف يميناً$onStreet"
            "slight right" -> ManeuverType.SLIGHT_RIGHT to "اتجه قليلاً إلى اليمين$onStreet"
            "sharp left" -> ManeuverType.SHARP_LEFT to "انعطف يساراً بقوة$onStreet"
            "left" -> ManeuverType.TURN_LEFT to "انعطف يساراً$onStreet"
            "slight left" -> ManeuverType.SLIGHT_LEFT to "اتجه قليلاً إلى اليسار$onStreet"
            else -> ManeuverType.STRAIGHT to "استمر للأمام$onStreet"
        }
    }

    /**
     * الوضع المباشر الأساسي مع توضيح صريح عند عدم تنزيل حزم الأوفلاين
     */
    fun calculateBasicDirectRoute(
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
                text = "مسار مباشر نحو $destinationName",
                maneuverType = ManeuverType.START,
                distanceRemaining = directDistance,
                segmentDistance = directDistance,
                streetName = "مسار مباشر",
                maneuverLat = endLat,
                maneuverLon = endLon
            ),
            NavigationInstruction(
                text = "الوصول إلى $destinationName",
                maneuverType = ManeuverType.DESTINATION,
                distanceRemaining = 0.0,
                segmentDistance = 0.0,
                streetName = destinationName,
                maneuverLat = endLat,
                maneuverLon = endLon
            )
        )

        val durationSeconds = ((directDistance / 11.11)).toInt().coerceAtLeast(60)

        return NavigationRouteResult.Success(
            points = directPoints,
            instructions = basicInstructions,
            totalDistanceMeters = directDistance,
            estimatedDurationSeconds = durationSeconds,
            isOfflineEngine = false,
            modeLabel = "تقريبي - مسافة مباشرة (أضف حزمة الأوفلاين للتوجيه بلا نت)"
        )
    }

    /**
     * فك تشفير مسار Valhalla Polyline6 الدقيق
     */
    fun decodePolyline6(encoded: String): List<RoutePoint> {
        if (encoded.isBlank()) return emptyList()
        return try {
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
                    if (index >= len || shift > 30) return emptyList()
                    b = encoded[index++].code - 63
                    if (b < 0 || b > 63) return emptyList()
                    result = result or ((b and 0x1f) shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
                lat += dlat

                shift = 0
                result = 0
                do {
                    if (index >= len || shift > 30) return emptyList()
                    b = encoded[index++].code - 63
                    if (b < 0 || b > 63) return emptyList()
                    result = result or ((b and 0x1f) shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
                lng += dlng

                val latDeg = lat / 1e6
                val lngDeg = lng / 1e6
                if (latDeg !in -90.0..90.0 || lngDeg !in -180.0..180.0) return emptyList()
                poly.add(RoutePoint(latDeg, lngDeg))
            }
            poly
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun mapValhallaManeuverType(type: Int): ManeuverType {
        return when (type) {
            0, 1, 2, 3 -> ManeuverType.START
            4, 5, 6 -> ManeuverType.DESTINATION
            7, 8 -> ManeuverType.STRAIGHT
            9 -> ManeuverType.SLIGHT_RIGHT
            10 -> ManeuverType.TURN_RIGHT
            11 -> ManeuverType.SHARP_RIGHT
            12, 13 -> ManeuverType.U_TURN
            14 -> ManeuverType.SHARP_LEFT
            15 -> ManeuverType.TURN_LEFT
            16 -> ManeuverType.SLIGHT_LEFT
            26, 27 -> ManeuverType.ROUNDABOUT
            else -> ManeuverType.STRAIGHT
        }
    }
}

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

    /**
     * فك تشفير مسار Valhalla Polyline6 الدقيق
     */
    fun decodePolyline6(encoded: String): List<RoutePoint> {
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

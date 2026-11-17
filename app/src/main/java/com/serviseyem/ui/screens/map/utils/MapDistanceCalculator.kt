package com.serviseyem.ui.screens.map.utils

import kotlin.math.*

/**
 * 🚲 TransportMode - وسيلة التنقل لحساب وقت الوصول المتوقع
 */
enum class TransportMode(val speedKmH: Double, val icon: String, val labelArabic: String) {
    WALKING(3.5, "⏱️", "سيراً"),
    BICYCLE(15.0, "🚲", "بالدراجة"),
    CAR(35.0, "🚘", "بالسيارة")
}

/**
 * 🧭 MapDistanceCalculator
 * Precise Haversine distance calculator and ETA estimation (walking: 3.5 km/h, bicycle: 15 km/h, driving: 35 km/h)
 */
object MapDistanceCalculator {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Compute Haversine distance in meters between two lat/lng points
     */
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 && lon1 == 0.0) return 0.0
        if (lat2 == 0.0 && lon2 == 0.0) return 0.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Format distance in meters to a clean Arabic string ("500 م" or "2.4 كم")
     */
    fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.roundToInt()} م"
        } else {
            String.format(java.util.Locale.US, "%.1f كم", meters / 1000.0)
        }
    }

    /**
     * Compute Estimated Time of Arrival (ETA) text with specified or auto transport mode:
     * Walking speed: 3.5 km/h
     */
     fun computeEta(distanceMeters: Double, mode: TransportMode? = null): String {
         val distKm = distanceMeters / 1000.0
         val selectedMode = mode ?: if (distKm < 1.0) TransportMode.WALKING else TransportMode.CAR
         val minutes = max(1, (distKm / selectedMode.speedKmH * 60).roundToInt())
         val distFormatted = if (distKm < 1.0) "${distanceMeters.roundToInt()} م" else "${String.format(java.util.Locale.US, "%.1f", distKm)} كم"
         return "${selectedMode.icon} ~ $minutes دقيقة ${selectedMode.labelArabic} ($distFormatted)"
     }

    /**
     * Bearing in degrees (0..360) from (lat1, lon1) to (lat2, lon2).
     */
    fun calculateBearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dLon = Math.toRadians(lon2 - lon1)
        val y = sin(dLon) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLon)
        val deg = Math.toDegrees(atan2(y, x)).toFloat()
        return ((deg % 360f) + 360f) % 360f
    }

    fun cardinalDirectionAr(bearingDeg: Float): String {
        val d = ((bearingDeg % 360f) + 360f) % 360f
        return when {
            d < 22.5f || d >= 337.5f -> "شمال"
            d < 67.5f -> "شمال شرق"
            d < 112.5f -> "شرق"
            d < 157.5f -> "جنوب شرق"
            d < 202.5f -> "جنوب"
            d < 247.5f -> "جنوب غرب"
            d < 292.5f -> "غرب"
            else -> "شمال غرب"
        }
    }

    /**
     * 3. خوارزمية تحسين ترتيب محطات التوقف (أقصر مسار يمر بالمحطات وصولاً إلى الوجهة النهائية)
     */
    data class Waypoint(val name: String, val lat: Double, val lng: Double)

    fun optimizeWaypoints(
        startLat: Double,
        startLng: Double,
        waypoints: List<Waypoint>,
        destLat: Double,
        destLng: Double
    ): List<Waypoint> {
        if (waypoints.size <= 1) return waypoints

        fun tourDistance(order: List<Waypoint>): Double {
            var total = 0.0
            var curLat = startLat
            var curLng = startLng
            for (wp in order) {
                total += calculateDistanceMeters(curLat, curLng, wp.lat, wp.lng)
                curLat = wp.lat
                curLng = wp.lng
            }
            if (destLat != 0.0 || destLng != 0.0) {
                total += calculateDistanceMeters(curLat, curLng, destLat, destLng)
            }
            return total
        }

        // Exact shortest path across all permutations for up to 6 intermediate stops (max 720 perms)
        if (waypoints.size <= 6) {
            var bestOrder = waypoints
            var bestDist = tourDistance(waypoints)
            fun permute(current: MutableList<Waypoint>, remaining: MutableList<Waypoint>) {
                if (remaining.isEmpty()) {
                    val d = tourDistance(current)
                    if (d < bestDist) {
                        bestDist = d
                        bestOrder = current.toList()
                    }
                    return
                }
                for (i in remaining.indices) {
                    val next = remaining.removeAt(i)
                    current.add(next)
                    permute(current, remaining)
                    current.removeAt(current.lastIndex)
                    remaining.add(i, next)
                }
            }
            permute(mutableListOf(), waypoints.toMutableList())
            return bestOrder
        }

        val unvisited = waypoints.toMutableList()
        val optimized = mutableListOf<Waypoint>()
        var currentLat = startLat
        var currentLng = startLng

        while (unvisited.isNotEmpty()) {
            val next = unvisited.minByOrNull { wp ->
                val leg = calculateDistanceMeters(currentLat, currentLng, wp.lat, wp.lng)
                val destBias = if (unvisited.size == 1 && (destLat != 0.0 || destLng != 0.0)) {
                    calculateDistanceMeters(wp.lat, wp.lng, destLat, destLng)
                } else 0.0
                leg + destBias
            } ?: break
            optimized.add(next)
            currentLat = next.lat
            currentLng = next.lng
            unvisited.remove(next)
        }
        return optimized
    }
}

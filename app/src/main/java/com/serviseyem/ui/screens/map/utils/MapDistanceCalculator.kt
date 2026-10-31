package com.serviseyem.ui.screens.map.utils

import kotlin.math.*
import com.serviseyem.ui.*

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
}

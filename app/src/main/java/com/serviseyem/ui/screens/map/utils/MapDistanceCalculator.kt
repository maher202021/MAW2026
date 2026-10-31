package com.serviseyem.ui.screens.map.utils

import com.serviseyem.navigation.HaversinePrecisionNavigator
import kotlin.math.max
import kotlin.math.roundToInt

enum class TransportMode(val speedKmH: Double, val icon: String, val labelArabic: String) {
    WALKING(3.5, "⏱️", "سيراً"),
    BICYCLE(15.0, "🚲", "بالدراجة"),
    CAR(35.0, "🚘", "بالسيارة")
}

object MapDistanceCalculator {

    /** مسافة جيوديسية مباشرة (ليست مسافة طريق). تعيد 0 للإحداثيات غير الصالحة. */
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1.isNaN() || lon1.isNaN() || lat2.isNaN() || lon2.isNaN()) return 0.0
        if (lat1 == 0.0 && lon1 == 0.0) return 0.0
        if (lat2 == 0.0 && lon2 == 0.0) return 0.0
        return HaversinePrecisionNavigator.distanceMeters(lat1, lon1, lat2, lon2)
    }

    fun formatDistance(meters: Double): String {
        if (meters.isNaN() || meters < 0) return "—"
        return if (meters < 1000) "${meters.roundToInt()} م"
        else String.format(java.util.Locale.US, "%.1f كم", meters / 1000.0)
    }

    /** للنطق: يقرّب إلى 10 م (<100) أو 50 م، وللكيلومترات رقم عشري واحد. */
    fun formatDistanceSpoken(meters: Double): String {
        val m = max(0.0, if (meters.isNaN()) 0.0 else meters)
        return if (m < 1000) {
            val r = if (m < 100) (m / 10).roundToInt() * 10 else (m / 50).roundToInt() * 50
            "${max(r, 10)} متراً"
        } else {
            val km = m / 1000.0
            val txt = if (km == km.toInt().toDouble()) km.toInt().toString() else String.format(java.util.Locale.US, "%.1f", km)
            "$txt كيلومتر"
        }
    }

    /** مدة مختصرة للواجهة: «25 دقيقة» أو «1 س 20 د». */
    fun formatDuration(seconds: Double): String {
        if (seconds.isNaN() || seconds < 0) return "—"
        val totalMin = (seconds / 60.0).roundToInt()
        if (totalMin < 1) return "أقل من دقيقة"
        if (totalMin < 60) return "$totalMin دقيقة"
        val h = totalMin / 60
        val m = totalMin % 60
        return if (m == 0) "$h س" else "$h س $m د"
    }

    /** تقدير تقريبي من مسافة مباشرة. موسوم دائماً «تقريبي» وبدون تكرار المسافة. */
    fun computeEta(distanceMeters: Double, mode: TransportMode? = null): String {
        val selected = mode ?: if (distanceMeters < 1000.0) TransportMode.WALKING else TransportMode.CAR
        val minutes = max(1, (distanceMeters / 1000.0 / selected.speedKmH * 60).roundToInt())
        return "${selected.icon} ~ $minutes دقيقة ${selected.labelArabic} (تقريبي)"
    }
}

package com.serviseyem.ui.screens.map.utils

import com.serviseyem.ui.screens.map.visualaddress.LandmarkOffsetCalculator
import kotlin.math.*

/**
 * 📍 FusedLocation - الموقع المصفى بعد دمج الحساسات وإزالة التشويش
 */
data class FusedLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val isDeadReckoning: Boolean,
    val confidence: Float, // 0.0 to 1.0
    val bearingDeg: Float
)

/**
 * 🛰️ GpsFusionFilter
 * مرشح متقدم لتقليل تشويش الـ GPS في اليمن وتجاهل القفزات المفاجئة
 * يطبق خوارزمية Dead Reckoning والتنعيم الحركي (Low-Pass Filter)
 */
class GpsFusionFilter {

    private var lastValidLat: Double = 0.0
    private var lastValidLng: Double = 0.0
    private var lastTimestamp: Long = 0L
    private var lastBearing: Float = 0f
    private var lastEstimatedSpeedMps: Float = 0f

    // أقصى سرعة منطقية في شوارع ومدن اليمن (140 كم/س = ~38 م/ث)
    private val maxSpeedMps = 38.8f

    /**
     * معالجة وتصفية قراءة GPS الواردة
     */
    fun processLocation(
        rawLat: Double,
        rawLng: Double,
        accuracyMeters: Float,
        speedMps: Float = 0f,
        bearingDeg: Float = 0f,
        timestamp: Long = System.currentTimeMillis()
    ): FusedLocation {
        // التحقق الأولي من حدود اليمن
        if (!MapCoordinateResolver.isWithinYemen(rawLat, rawLng)) {
            val fallbackLat = if (lastValidLat != 0.0) lastValidLat else MapCoordinateResolver.DEFAULT_YEMEN_LAT
            val fallbackLng = if (lastValidLng != 0.0) lastValidLng else MapCoordinateResolver.DEFAULT_YEMEN_LNG
            return FusedLocation(fallbackLat, fallbackLng, 100f, true, 0.3f, lastBearing)
        }

        // أول قراءة
        if (lastValidLat == 0.0 && lastValidLng == 0.0) {
            lastValidLat = rawLat
            lastValidLng = rawLng
            lastTimestamp = timestamp
            lastBearing = bearingDeg
            return FusedLocation(rawLat, rawLng, accuracyMeters, false, 0.9f, bearingDeg)
        }

        val dtSeconds = max(0.2, (timestamp - lastTimestamp) / 1000.0)
        val distance = MapDistanceCalculator.calculateDistanceMeters(lastValidLat, lastValidLng, rawLat, rawLng)
        val apparentSpeed = (distance / dtSeconds).toFloat()

        // 1. كشف القفزات غير المنطقية (GPS Spikes) الناتجة عن التشويش
        val isSpike = apparentSpeed > maxSpeedMps && distance > 50.0

        if (isSpike) {
            // تجاهل القفزة وتطبيق تقدير الحركة الميتة (Dead Reckoning)
            val projectedDist = lastEstimatedSpeedMps * dtSeconds
            val projected = LandmarkOffsetCalculator.calculateDestinationCoordinates(
                lastValidLat,
                lastValidLng,
                projectedDist,
                bearingToOffsetDirection(lastBearing)
            )

            lastTimestamp = timestamp
            return FusedLocation(
                latitude = projected.first,
                longitude = projected.second,
                accuracyMeters = max(accuracyMeters, 60f),
                isDeadReckoning = true,
                confidence = 0.5f,
                bearingDeg = lastBearing
            )
        }

        // 2. تطبيق التنعيم الحركي (Exponential Smoothing / Kalman Weight)
        // إذا كانت الدقة ضعيفة (> 50 م)، نعتمد أكثر على الموقع السابق
        val alpha = when {
            accuracyMeters < 15f -> 0.75f
            accuracyMeters < 40f -> 0.45f
            else -> 0.25f
        }

        val smoothedLat = (alpha * rawLat) + ((1f - alpha) * lastValidLat)
        val smoothedLng = (alpha * rawLng) + ((1f - alpha) * lastValidLng)

        lastValidLat = smoothedLat
        lastValidLng = smoothedLng
        lastTimestamp = timestamp
        lastEstimatedSpeedMps = if (speedMps > 0f) speedMps else apparentSpeed
        if (bearingDeg != 0f) lastBearing = bearingDeg

        val confidence = (1f - (accuracyMeters / 150f)).coerceIn(0.2f, 0.98f)

        return FusedLocation(
            latitude = smoothedLat,
            longitude = smoothedLng,
            accuracyMeters = accuracyMeters,
            isDeadReckoning = false,
            confidence = confidence,
            bearingDeg = lastBearing
        )
    }

    private fun bearingToOffsetDirection(bearing: Float): com.serviseyem.ui.screens.map.visualaddress.OffsetDirection {
        val b = (bearing + 360) % 360
        return when {
            b in 22.5..67.5 -> com.serviseyem.ui.screens.map.visualaddress.OffsetDirection.NORTH_EAST
            b in 67.5..112.5 -> com.serviseyem.ui.screens.map.visualaddress.OffsetDirection.EAST
            b in 112.5..157.5 -> com.serviseyem.ui.screens.map.visualaddress.OffsetDirection.SOUTH_EAST
            b in 157.5..202.5 -> com.serviseyem.ui.screens.map.visualaddress.OffsetDirection.SOUTH
            b in 202.5..247.5 -> com.serviseyem.ui.screens.map.visualaddress.OffsetDirection.SOUTH_WEST
            b in 247.5..292.5 -> com.serviseyem.ui.screens.map.visualaddress.OffsetDirection.WEST
            b in 292.5..337.5 -> com.serviseyem.ui.screens.map.visualaddress.OffsetDirection.NORTH_WEST
            else -> com.serviseyem.ui.screens.map.visualaddress.OffsetDirection.NORTH
        }
    }
}

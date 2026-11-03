package com.serviseyem.ui.screens.map.visualaddress

import kotlin.math.*

/**
 * 📐 LandmarkOffsetCalculator
 * يحسب الإحداثيات التقريبية بناءً على المعلم المختار والإزاحة بالمتر والاتجاه الجغرافي
 */
object LandmarkOffsetCalculator {

    private const val EARTH_RADIUS = 6371000.0 // meters

    /**
     * حساب إحداثيات الوجهة التقريبية من المعلم والاتجاه والمسافة
     */
    fun calculateDestinationCoordinates(
        landmarkLat: Double,
        landmarkLng: Double,
        distanceMeters: Double,
        direction: OffsetDirection
    ): Pair<Double, Double> {
        val bearingRad = Math.toRadians(direction.bearingDeg)
        val latRad = Math.toRadians(landmarkLat)
        val lngRad = Math.toRadians(landmarkLng)
        val angularDist = distanceMeters / EARTH_RADIUS

        val targetLatRad = asin(
            sin(latRad) * cos(angularDist) +
            cos(latRad) * sin(angularDist) * cos(bearingRad)
        )

        val targetLngRad = lngRad + atan2(
            sin(bearingRad) * sin(angularDist) * cos(latRad),
            cos(angularDist) - sin(latRad) * sin(targetLatRad)
        )

        return Pair(Math.toDegrees(targetLatRad), Math.toDegrees(targetLngRad))
    }

    /**
     * إيجاد أقرب معلم مسجل لموقع معين وحساب الإزاحة إليه تلقائياً
     */
    fun findNearestLandmarkAndComputeOffset(
        targetLat: Double,
        targetLng: Double,
        city: String
    ): VisualAddress {
        val landmarks = LandmarkRepository.getLandmarksForCity(city)
        if (landmarks.isEmpty()) {
            return VisualAddress(
                landmarkId = "sn_saleh",
                landmarkName = "جامع الصالح",
                cityId = "صنعاء",
                direction = OffsetDirection.NORTH,
                approximateDistanceMeters = 300,
                visualNotes = "",
                calculatedLatitude = targetLat,
                calculatedLongitude = targetLng
            )
        }

        var closest = landmarks.first()
        var minDistance = Double.MAX_VALUE

        landmarks.forEach { lm ->
            val dist = calculateDistance(targetLat, targetLng, lm.latitude, lm.longitude)
            if (dist < minDistance) {
                minDistance = dist
                closest = lm
            }
        }

        val bearing = calculateBearing(closest.latitude, closest.longitude, targetLat, targetLng)
        val direction = bearingToDirection(bearing)
        val roundedDistance = (Math.round(minDistance / 50.0) * 50).toInt().coerceAtLeast(50)

        return VisualAddress(
            landmarkId = closest.id,
            landmarkName = closest.name,
            cityId = closest.cityId,
            direction = direction,
            approximateDistanceMeters = roundedDistance,
            visualNotes = "",
            calculatedLatitude = targetLat,
            calculatedLongitude = targetLng
        )
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS * c
    }

    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)
        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }

    private fun bearingToDirection(bearing: Double): OffsetDirection {
        return when (bearing) {
            in 22.5..67.5 -> OffsetDirection.NORTH_EAST
            in 67.5..112.5 -> OffsetDirection.EAST
            in 112.5..157.5 -> OffsetDirection.SOUTH_EAST
            in 157.5..202.5 -> OffsetDirection.SOUTH
            in 202.5..247.5 -> OffsetDirection.SOUTH_WEST
            in 247.5..292.5 -> OffsetDirection.WEST
            in 292.5..337.5 -> OffsetDirection.NORTH_WEST
            else -> OffsetDirection.NORTH
        }
    }
}

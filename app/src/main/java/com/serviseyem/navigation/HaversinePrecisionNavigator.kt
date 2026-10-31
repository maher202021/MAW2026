package com.serviseyem.navigation

import java.util.Locale
import kotlin.math.*

/**
 * 📐 HaversinePrecisionNavigator
 * High-precision geodesic and spherical geometry navigation utilities.
 *
 * Earth Radius: R = 6,371,008.8 m based on the International Union of Geodesy
 * and Geophysics (IUGG) mean radius for optimal spherical navigation calculations.
 */
object HaversinePrecisionNavigator {

    /**
     * IUGG mean Earth radius in meters.
     */
    const val EARTH_RADIUS_METERS: Double = 6371008.8

    enum class TransportationMode(val speedKmh: Double, val labelAr: String) {
        WALKING(4.5, "مشياً على الأقدام"),
        CITY_DRIVING(36.0, "قيادة داخل المدينة"),
        BOULEVARD_DRIVING(55.0, "شارع رئيسي"),
        HIGHWAY_DRIVING(85.0, "طريق سريع")
    }

    /**
     * Calculates great-circle distance between two coordinates in meters using the Haversine formula.
     */
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == lat2 && lon1 == lon2) return 0.0

        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val sinDPhi = sin(deltaPhi / 2.0)
        val sinDLambda = sin(deltaLambda / 2.0)

        val a = sinDPhi * sinDPhi + cos(phi1) * cos(phi2) * sinDLambda * sinDLambda
        val clampedA = a.coerceIn(0.0, 1.0)
        val c = 2.0 * atan2(sqrt(clampedA), sqrt(1.0 - clampedA))

        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates the initial bearing (forward azimuth) from point 1 to point 2 in degrees in range [0, 360).
     */
    fun initialBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)

        return (Math.toDegrees(theta) + 360.0) % 360.0
    }

    /**
     * Calculates destination coordinates given start point, initial bearing (degrees), and distance (meters).
     */
    fun destinationPoint(lat: Double, lon: Double, bearingDeg: Double, distanceM: Double): Pair<Double, Double> {
        val angularDistance = distanceM / EARTH_RADIUS_METERS
        val bearingRad = Math.toRadians(bearingDeg)
        val phi1 = Math.toRadians(lat)
        val lambda1 = Math.toRadians(lon)

        val sinPhi1 = sin(phi1)
        val cosPhi1 = cos(phi1)
        val sinDist = sin(angularDistance)
        val cosDist = cos(angularDistance)

        val phi2 = asin(sinPhi1 * cosDist + cosPhi1 * sinDist * cos(bearingRad))
        val lambda2 = lambda1 + atan2(
            sin(bearingRad) * sinDist * cosPhi1,
            cosDist - sinPhi1 * sin(phi2)
        )

        val normalizedLat = Math.toDegrees(phi2)
        val normalizedLon = (Math.toDegrees(lambda2) + 540.0) % 360.0 - 180.0

        return Pair(normalizedLat, normalizedLon)
    }

    /**
     * Calculates cross-track distance in meters: signed perpendicular distance of a point (latP, lonP)
     * from a great-circle path running from (lat1, lon1) to (lat2, lon2).
     * Positive = right of path, Negative = left of path.
     */
    fun crossTrackDistance(
        latP: Double, lonP: Double,
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dist1P = distanceMeters(lat1, lon1, latP, lonP) / EARTH_RADIUS_METERS
        val bearing1P = Math.toRadians(initialBearing(lat1, lon1, latP, lonP))
        val bearing12 = Math.toRadians(initialBearing(lat1, lon1, lat2, lon2))

        val dXt = asin(sin(dist1P) * sin(bearing1P - bearing12))
        return dXt * EARTH_RADIUS_METERS
    }

    /**
     * Calculates along-track distance in meters: distance from path start (lat1, lon1)
     * to the point projected perpendicularly on the path.
     */
    fun alongTrackDistance(
        latP: Double, lonP: Double,
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dist1P = distanceMeters(lat1, lon1, latP, lonP) / EARTH_RADIUS_METERS
        val xtDist = crossTrackDistance(latP, lonP, lat1, lon1, lat2, lon2) / EARTH_RADIUS_METERS

        val cosDist1P = cos(dist1P)
        val cosXt = cos(xtDist)
        if (abs(cosXt) < 1e-12) return 0.0

        val dAt = acos((cosDist1P / cosXt).coerceIn(-1.0, 1.0))
        val bearing1P = initialBearing(lat1, lon1, latP, lonP)
        val bearing12 = initialBearing(lat1, lon1, lat2, lon2)
        val angleDiff = abs(angleDelta(bearing12, bearing1P))

        val signedDistance = if (angleDiff > 90.0) -dAt * EARTH_RADIUS_METERS else dAt * EARTH_RADIUS_METERS
        return signedDistance
    }

    /**
     * Normalizes an angle in degrees to range [0, 360).
     */
    fun normalizeAngle(angleDeg: Double): Double {
        var b = angleDeg % 360.0
        if (b < 0.0) b += 360.0
        return b
    }

    /**
     * Calculates the minimal signed difference between two bearings in degrees, range [-180, 180].
     * Positive = turning clockwise (right), Negative = turning counter-clockwise (left).
     */
    fun angleDelta(fromBearingDeg: Double, toBearingDeg: Double): Double {
        val diff = (toBearingDeg - fromBearingDeg + 540.0) % 360.0 - 180.0
        return diff
    }

    /**
     * Pure turn classifier from bearing delta.
     */
    fun classifyTurn(deltaDeg: Double): ManeuverType {
        return when {
            deltaDeg in -15.0..15.0 -> ManeuverType.STRAIGHT
            deltaDeg in 15.0..45.0 -> ManeuverType.SLIGHT_RIGHT
            deltaDeg in 45.0..120.0 -> ManeuverType.TURN_RIGHT
            deltaDeg in 120.0..165.0 -> ManeuverType.SHARP_RIGHT
            deltaDeg in -45.0..-15.0 -> ManeuverType.SLIGHT_LEFT
            deltaDeg in -120.0..-45.0 -> ManeuverType.TURN_LEFT
            deltaDeg in -165.0..-120.0 -> ManeuverType.SHARP_LEFT
            else -> ManeuverType.U_TURN
        }
    }

    /**
     * Formats distance in Arabic according to grammar and standard rounding rules:
     * - Under 1000m: rounded to nearest 10m/50m with appropriate Arabic phrasing.
     * - Above 1000m: in km with 1 decimal.
     */
    fun formatDistanceArabic(meters: Double): String {
        val m = meters.coerceAtLeast(0.0)
        return if (m < 1000.0) {
            val roundedM = if (m < 100.0) {
                ((m / 10.0).roundToInt() * 10).coerceAtLeast(10)
            } else {
                ((m / 50.0).roundToInt() * 50)
            }
            when {
                roundedM == 1 -> "متر واحد"
                roundedM == 2 -> "متران"
                roundedM in 3..10 -> "$roundedM أمتار"
                else -> "$roundedM متراً"
            }
        } else {
            val km = m / 1000.0
            String.format(Locale.US, "%.1f كم", km)
        }
    }

    /**
     * Formats duration in Arabic with hours and minutes.
     */
    fun formatDurationArabic(seconds: Int): String {
        val s = seconds.coerceAtLeast(0)
        val minutes = s / 60
        val hours = minutes / 60
        val remMin = minutes % 60

        return when {
            hours == 1 && remMin == 0 -> "ساعة واحدة"
            hours == 2 && remMin == 0 -> "ساعتان"
            hours in 3..10 && remMin == 0 -> "$hours ساعات"
            hours > 0 && remMin > 0 -> "$hours س و $remMin د"
            minutes == 1 -> "دقيقة واحدة"
            minutes == 2 -> "دقيقتان"
            minutes in 3..10 -> "$minutes دقائق"
            minutes > 10 -> "$minutes دقيقة"
            else -> "أقل من دقيقة"
        }
    }

    /**
     * Calculates trip completion fraction along a route polyline in range [0.0f, 1.0f].
     */
    fun calculateTripProgress(totalDistanceMeters: Double, remainingDistanceMeters: Double): Float {
        if (totalDistanceMeters <= 0.0) return 1.0f
        val completed = totalDistanceMeters - remainingDistanceMeters
        return (completed / totalDistanceMeters).toFloat().coerceIn(0.0f, 1.0f)
    }
}

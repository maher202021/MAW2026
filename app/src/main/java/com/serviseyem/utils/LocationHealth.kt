package com.serviseyem.utils

import android.content.Context
import android.location.LocationManager
import android.os.Build

// [نظام خرائط وملاحة عالمي - ملكنا]
sealed class LocationStatus {
    data object NoPermission : LocationStatus()
    data object ProviderOff : LocationStatus()
    data object Searching : LocationStatus()
    data class Weak(val accuracyM: Float) : LocationStatus()
    data class Good(val accuracyM: Float) : LocationStatus()
}

// [نظام خرائط وملاحة عالمي - ملكنا]
object LocationHealth {
    private const val STALE_THRESHOLD_MS = 180_000L
    private const val WEAK_ACCURACY_THRESHOLD_M = 100f

    fun isLocationServiceEnabled(context: Context): Boolean {
        return try {
            val lm = context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                lm.isLocationEnabled
            } else {
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }
        } catch (_: Exception) {
            false
        }
    }

    fun evaluate(
        context: Context,
        lat: Double,
        lng: Double,
        accuracyM: Float,
        timestampMs: Long,
        nowMs: Long = System.currentTimeMillis()
    ): LocationStatus {
        if (!LocationCapture.hasLocationPermission(context)) {
            return LocationStatus.NoPermission
        }
        if (!isLocationServiceEnabled(context)) {
            return LocationStatus.ProviderOff
        }
        val validCoords = lat != 0.0 && lng != 0.0 && !lat.isNaN() && !lng.isNaN() &&
            lat in -90.0..90.0 && lng in -180.0..180.0
        if (!validCoords || timestampMs <= 0L) {
            return LocationStatus.Searching
        }
        val ageMs = nowMs - timestampMs
        if (ageMs > STALE_THRESHOLD_MS) {
            return LocationStatus.Searching
        }
        if (accuracyM.isNaN() || accuracyM <= 0f || accuracyM > WEAK_ACCURACY_THRESHOLD_M) {
            return LocationStatus.Weak(if (accuracyM.isFinite() && accuracyM > 0f) accuracyM else 150f)
        }
        return LocationStatus.Good(accuracyM)
    }
}

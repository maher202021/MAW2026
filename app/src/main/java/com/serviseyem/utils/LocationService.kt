package com.serviseyem.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.*

/**
 * 📍 LocationCoordinates - Data model holding geo coordinates with metadata
 */
data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val isMock: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 🛰️ LocationPermissionState - Permission statuses for GPS
 */
enum class LocationPermissionState {
    GRANTED_FINE,
    GRANTED_COARSE,
    DENIED,
    GPS_DISABLED
}

/**
 * 🌐 LocationResult - Sealed class representing location acquisition result
 */
sealed class LocationResult {
    data class Success(val coordinates: LocationCoordinates) : LocationResult()
    data class Error(val message: String, val cause: Throwable? = null) : LocationResult()
    object PermissionDenied : LocationResult()
    object GpsDisabled : LocationResult()
}

/**
 * 🧭 LocationService
 * Professional location provider service handling GPS coordinates, permission checks,
 * fallback mechanism (Default Sana'a / Yemen coordinates), and distance calculations.
 */
class LocationService(
    private val context: Context,
    private val fusedClient: FusedLocationProviderClient? = null,
    private val customLocationManager: LocationManager? = null
) {
    private val locationManager: LocationManager by lazy {
        customLocationManager ?: (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
    }

    private val client: FusedLocationProviderClient by lazy {
        fusedClient ?: LocationServices.getFusedLocationProviderClient(context)
    }

    companion object {
        // Default Yemen / Sana'a coordinates fallback
        const val DEFAULT_YEMEN_LAT = 15.3694
        const val DEFAULT_YEMEN_LNG = 44.1910
        const val EARTH_RADIUS_KM = 6371.0
    }

    /**
     * Check current GPS permission status
     */
    fun checkPermissionState(): LocationPermissionState {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return LocationPermissionState.DENIED
        }

        if (!isGpsEnabled()) {
            return LocationPermissionState.GPS_DISABLED
        }

        return if (hasFine) LocationPermissionState.GRANTED_FINE else LocationPermissionState.GRANTED_COARSE
    }

    /**
     * Checks if GPS provider or Network provider is active
     */
    fun isGpsEnabled(): Boolean {
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Fetch current device coordinates with permission awareness and graceful fallbacks
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoordinates(useHighAccuracy: Boolean = true): LocationResult {
        val permissionState = checkPermissionState()
        if (permissionState == LocationPermissionState.DENIED) {
            return LocationResult.PermissionDenied
        }
        if (permissionState == LocationPermissionState.GPS_DISABLED) {
            return LocationResult.GpsDisabled
        }

        return try {
            val priority = if (useHighAccuracy && permissionState == LocationPermissionState.GRANTED_FINE) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

            val cts = CancellationTokenSource()
            val location: Location? = suspendCancellableCoroutine { continuation ->
                try {
                    client.getCurrentLocation(priority, cts.token)
                        .addOnSuccessListener { loc ->
                            continuation.resume(loc)
                        }
                        .addOnFailureListener { exc ->
                            continuation.resume(null)
                        }
                } catch (e: SecurityException) {
                    continuation.resume(null)
                }
            }

            if (location != null) {
                LocationResult.Success(
                    LocationCoordinates(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracyMeters = location.accuracy,
                        timestamp = location.time
                    )
                )
            } else {
                // Fallback to last known location from LocationManager if available
                val lastKnown = getLastKnownLocationFromManager()
                if (lastKnown != null) {
                    LocationResult.Success(
                        LocationCoordinates(
                            latitude = lastKnown.latitude,
                            longitude = lastKnown.longitude,
                            accuracyMeters = lastKnown.accuracy,
                            timestamp = lastKnown.time
                        )
                    )
                } else {
                    // Fallback to default Yemen location
                    LocationResult.Success(
                        LocationCoordinates(
                            latitude = DEFAULT_YEMEN_LAT,
                            longitude = DEFAULT_YEMEN_LNG,
                            accuracyMeters = 5000f,
                            isMock = true
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            LocationResult.PermissionDenied
        } catch (e: Exception) {
            LocationResult.Error(e.message ?: "Failed to retrieve location", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocationFromManager(): Location? {
        return try {
            val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            when {
                gpsLoc != null && netLoc != null -> if (gpsLoc.time > netLoc.time) gpsLoc else netLoc
                gpsLoc != null -> gpsLoc
                else -> netLoc
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate precise Haversine distance in kilometers between two lat/lng coordinates
     */
    fun calculateHaversineDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        if (lat1 == 0.0 && lon1 == 0.0) return 0.0
        if (lat2 == 0.0 && lon2 == 0.0) return 0.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }
}

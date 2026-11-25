package com.serviseyem.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * LocationCapture
 * يلتقط موقعاً حياً ودقيقاً من GPS / FusedLocation عند وجود إذن الموقع،
 * أو يعيد null عند التعذر دون اختلاق إحداثيات صنعاء.
 */
object LocationCapture {

    fun hasLocationPermission(context: Context): Boolean {
        val appCtx = context.applicationContext
        val fine = ContextCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(appCtx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    // [نظام خرائط وملاحة عالمي - ملكنا]
    @SuppressLint("MissingPermission")
    suspend fun fresh(
        context: Context,
        timeoutMs: Long = 8_000L,
        maxAccuracyM: Float = 500f
    ): Location? {
        val appCtx = context.applicationContext
        if (!hasLocationPermission(appCtx)) return null

        val fusedLoc = try {
            val client = LocationServices.getFusedLocationProviderClient(appCtx)
            val cts = CancellationTokenSource()
            val loc = withTimeoutOrNull(timeoutMs) {
                suspendCancellableCoroutine<Location?> { cont ->
                    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                        .addOnSuccessListener { if (cont.isActive) cont.resume(it) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                    cont.invokeOnCancellation { cts.cancel() }
                }
            }
            cts.cancel()
            loc
        } catch (_: Throwable) {
            null
        }

        val candidate = fusedLoc ?: try {
            val lm = appCtx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gps = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val net = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            listOfNotNull(gps, net).minByOrNull { if (it.hasAccuracy()) it.accuracy else Float.MAX_VALUE }
        } catch (_: Throwable) {
            null
        }

        return candidate?.takeIf {
            LandmarkAddress.isValid(it.latitude, it.longitude) &&
                (!it.hasAccuracy() || it.accuracy <= maxAccuracyM)
        }
    }
}

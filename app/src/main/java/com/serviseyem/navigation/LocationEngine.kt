package com.serviseyem.navigation

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import android.os.SystemClock
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 🛰️ LocationEngine
 * Production-grade fused location engine with:
 * - 1s high-accuracy FusedLocationProvider updates.
 * - Outlier rejection (accuracy > 50m, impossible speed jumps > 70 m/s).
 * - 1D Kalman filter for coordinates smoothing and deadband speed conversion.
 * - Sensor-fused compass heading (GPS bearing if speed > 2 m/s, otherwise rotation-vector + geomagnetic declination).
 * - Dead-reckoning fallback up to 10 seconds when entering tunnels or during GPS loss.
 */
class LocationEngine(private val context: Context) : SensorEventListener {

    data class LocationFix(
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float,
        val speedKmh: Float,
        val bearingDegrees: Float,
        val timestamp: Long,
        val isDeadReckoning: Boolean = false,
        val isGpsWeak: Boolean = false
    )

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _locationFlow = MutableStateFlow(
        LocationFix(
            latitude = 15.3694,
            longitude = 44.1910,
            accuracyMeters = 15f,
            speedKmh = 0f,
            bearingDegrees = 0f,
            timestamp = System.currentTimeMillis()
        )
    )
    val locationFlow: StateFlow<LocationFix> = _locationFlow.asStateFlow()

    private val _compassHeadingFlow = MutableStateFlow(0f)
    val compassHeadingFlow: StateFlow<Float> = _compassHeadingFlow.asStateFlow()

    private val _isGpsLostFlow = MutableStateFlow(false)
    val isGpsLostFlow: StateFlow<Boolean> = _isGpsLostFlow.asStateFlow()

    // Configuration
    private val maxAcceptableAccuracy = 50f // meters
    private val maxSpeedMetersPerSec = 70f // ~252 km/h
    private val speedDeadbandKmh = 1.5f

    // Kalman Filter variables
    private var kalmanLat = 0.0
    private var kalmanLon = 0.0
    private var kalmanVariance = -1.0 // Uninitialized
    private val kalmanQ = 3.0 // Process noise (meters per second)

    private var lastRawLocation: Location? = null
    private var lastFixElapsedTimeNanos: Long = 0
    private var isTracking = false

    // Sensor Fusion Heading
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var declination = 0f

    // Dead reckoning job
    private var deadReckoningJob: Job? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            processNewLocation(location)
        }
    }

    /**
     * Start high-accuracy updates for active navigation (1 second interval).
     */
    @SuppressLint("MissingPermission")
    fun startNavigationTracking() {
        if (isTracking) return
        isTracking = true

        registerSensors()

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(800L)
            .setMaxUpdateDelayMillis(1500L)
            .setMinUpdateDistanceMeters(0f)
            .build()

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
            startGpsWatchdog()
        } catch (_: SecurityException) {
            isTracking = false
        }
    }

    /**
     * Start low-power updates when navigation is idle.
     */
    @SuppressLint("MissingPermission")
    fun startIdleTracking() {
        stopTracking()
        isTracking = true

        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(5000L)
            .build()

        try {
            fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (_: SecurityException) {
            isTracking = false
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        deadReckoningJob?.cancel()
        try {
            fusedClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
        unregisterSensors()
    }

    private fun processNewLocation(raw: Location) {
        // 1. Accuracy filtering
        if (raw.accuracy <= 0f || raw.accuracy > maxAcceptableAccuracy) return

        // 2. Unrealistic jump filtering (impossible speed)
        val nowElapsed = SystemClock.elapsedRealtimeNanos()
        val prev = lastRawLocation
        if (prev != null && lastFixElapsedTimeNanos > 0) {
            val dtSeconds = (nowElapsed - lastFixElapsedTimeNanos) / 1_000_000_000.0
            if (dtSeconds > 0.05) {
                val distanceM = HaversinePrecisionNavigator.distanceMeters(
                    prev.latitude, prev.longitude,
                    raw.latitude, raw.longitude
                )
                val calculatedSpeed = distanceM / dtSeconds
                if (calculatedSpeed > maxSpeedMetersPerSec) {
                    return // Outlier jump discarded
                }
            }
        }

        lastRawLocation = raw
        lastFixElapsedTimeNanos = nowElapsed
        _isGpsLostFlow.value = false

        // Update geomagnetic declination for compass heading correction
        val geoField = GeomagneticField(
            raw.latitude.toFloat(),
            raw.longitude.toFloat(),
            raw.altitude.toFloat(),
            raw.time
        )
        declination = geoField.declination

        // 3. 1D Coordinates Smoothing via Kalman formulation
        smoothCoordinates(raw.latitude, raw.longitude, raw.accuracy)

        // 4. Calculate smoothed speed with deadband
        val rawSpeedKmh = (raw.speed * 3.6f)
        val filteredSpeedKmh = if (rawSpeedKmh < speedDeadbandKmh) 0f else rawSpeedKmh

        // 5. Sensor / GPS bearing arbitration
        val heading = if (raw.speed > 2.0f && raw.hasBearing()) {
            raw.bearing
        } else {
            _compassHeadingFlow.value
        }

        _locationFlow.value = LocationFix(
            latitude = kalmanLat,
            longitude = kalmanLon,
            accuracyMeters = raw.accuracy,
            speedKmh = filteredSpeedKmh,
            bearingDegrees = heading,
            timestamp = raw.time,
            isDeadReckoning = false,
            isGpsWeak = false
        )
    }

    private fun smoothCoordinates(lat: Double, lon: Double, accuracy: Float) {
        val variance = (accuracy * accuracy).toDouble()
        if (kalmanVariance < 0.0) {
            kalmanLat = lat
            kalmanLon = lon
            kalmanVariance = variance
        } else {
            // Apply time prediction variance
            kalmanVariance += kalmanQ * kalmanQ
            // Kalman gain
            val k = kalmanVariance / (kalmanVariance + variance)
            kalmanLat += k * (lat - kalmanLat)
            kalmanLon += k * (lon - kalmanLon)
            kalmanVariance = (1.0 - k) * kalmanVariance
        }
    }

    private fun startGpsWatchdog() {
        deadReckoningJob?.cancel()
        deadReckoningJob = engineScope.launch {
            var deadReckoningSeconds = 0
            while (isActive && isTracking) {
                delay(1000L)
                val elapsedSinceFixMs = (SystemClock.elapsedRealtimeNanos() - lastFixElapsedTimeNanos) / 1_000_000L
                if (elapsedSinceFixMs > 3500L) {
                    _isGpsLostFlow.value = true
                    if (deadReckoningSeconds < 10) {
                        deadReckoningSeconds++
                        advanceDeadReckoning()
                    }
                } else {
                    deadReckoningSeconds = 0
                    _isGpsLostFlow.value = false
                }
            }
        }
    }

    /**
     * Advances vehicle position along current speed and bearing vector during short GPS outages.
     */
    private fun advanceDeadReckoning() {
        val current = _locationFlow.value
        if (current.speedKmh <= 0.5f) return

        val distanceMetersInOneSec = (current.speedKmh * 1000.0) / 3600.0
        val nextPoint = HaversinePrecisionNavigator.destinationPoint(
            current.latitude, current.longitude,
            current.bearingDegrees.toDouble(),
            distanceMetersInOneSec
        )

        kalmanLat = nextPoint.first
        kalmanLon = nextPoint.second

        _locationFlow.value = current.copy(
            latitude = nextPoint.first,
            longitude = nextPoint.second,
            isDeadReckoning = true,
            isGpsWeak = true,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun registerSensors() {
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun unregisterSensors() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            // Azimuth in degrees [0, 360)
            var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            azimuth = (azimuth + declination + 360f) % 360f
            _compassHeadingFlow.value = azimuth
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

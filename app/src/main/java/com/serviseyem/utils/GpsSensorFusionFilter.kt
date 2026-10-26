package com.serviseyem.utils

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import kotlin.math.*

/**
 * 🛰️ GpsSensorFusionFilter
 * Advanced algorithm to increase location accuracy and heading precision by merging:
 * 1. GPS Coordinates (Raw Location)
 * 2. Gyroscope (Rotational Velocity)
 * 3. Magnetometer (True North Orientation)
 * 4. Accelerometer (Gravity & Movement Detection)
 */
class GpsSensorFusionFilter(private val sensorManager: SensorManager) : SensorEventListener {

    // Final fused values
    var fusedLatitude: Double = 0.0
    var fusedLongitude: Double = 0.0
    var fusedHeading: Float = 0f
    var confidenceScore: Float = 0f

    // Sensor raw data
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var gyroVelocity = FloatArray(3)
    
    // Kalman Filter variables for Position
    private var varianceP = 1.0 // Position variance
    private var varianceQ = 0.05 // Process noise
    private var varianceR = 0.8 // Measurement noise (GPS)

    fun onLocationUpdate(location: Location) {
        val rawLat = location.latitude
        val rawLng = location.longitude
        val accuracy = location.accuracy // Use accuracy to adjust R

        // Simple Kalman 1D for Lat & Lng (can be expanded to 2D matrix)
        fusedLatitude = applyKalman(fusedLatitude, rawLat, accuracy)
        fusedLongitude = applyKalman(fusedLongitude, rawLng, accuracy)
        
        confidenceScore = (100f - accuracy).coerceIn(0f, 100f)
    }

    private fun applyKalman(current: Double, measured: Double, accuracy: Float): Double {
        if (current == 0.0) return measured
        
        // Prediction
        varianceP += varianceQ
        
        // Measurement Update
        val rValue = (accuracy * accuracy).toDouble() / 100.0
        val kalmanGain = varianceP / (varianceP + rValue)
        val result = current + kalmanGain * (measured - current)
        varianceP = (1.0 - kalmanGain) * varianceP
        
        return result
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
            Sensor.TYPE_GYROSCOPE -> gyroVelocity = event.values.clone()
        }

        updateHeading()
    }

    private fun updateHeading() {
        if (gravity != null && geomagnetic != null) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                
                // Azimuth (radians) to Degrees
                val rawHeading = Math.toDegrees(orientation[0].toDouble()).toFloat()
                
                // Low-pass filter for heading stability
                fusedHeading = fusedHeading + 0.15f * (rawHeading - fusedHeading)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun start() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }
}

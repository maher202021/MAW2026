package com.serviseyem.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 🛰️ GnssSatelliteInfo - Single satellite signal metric
 */
data class GnssSatelliteInfo(
    val svid: Int,
    val constellationName: String,
    val constellationType: Int,
    val cn0DbHz: Float,
    val usedInFix: Boolean,
    val elevationDegrees: Float,
    val azimuthDegrees: Float
)

/**
 * 🛰️ GnssStatusSummary - Complete live diagnostic data packet
 */
data class GnssStatusSummary(
    val totalSatellites: Int = 0,
    val usedSatellites: Int = 0,
    val satellites: List<GnssSatelliteInfo> = emptyList(),
    val isFixAcquired: Boolean = false,
    val ttffMs: Int = 0,
    val isMonitoringActive: Boolean = false,
    val averageCn0: Float = 0f,
    val qualityRating: String = "غير متصل ⚪" // "ممتازة 🟢", "جيدة 🟡", "ضعيفة 🔴", "غير متصل ⚪"
)

/**
 * 🛠️ GnssDiagnosticsManager
 * Real-time native Android GNSS Satellite Signal Diagnostic Engine (100% Free, On-Device, No API Keys required).
 */
class GnssDiagnosticsManager(private val context: Context) {

    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val _gnssStatusFlow = MutableStateFlow(GnssStatusSummary())
    val gnssStatusFlow: StateFlow<GnssStatusSummary> = _gnssStatusFlow.asStateFlow()

    private var gnssCallback: GnssStatus.Callback? = null
    private var isListening = false

    @SuppressLint("MissingPermission")
    fun startMonitoring() {
        if (isListening) return

        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine) {
            _gnssStatusFlow.value = _gnssStatusFlow.value.copy(
                qualityRating = "بحاجة لإذن الموقع ⚠️"
            )
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val callback = object : GnssStatus.Callback() {
                    override fun onStarted() {
                        _gnssStatusFlow.value = _gnssStatusFlow.value.copy(isMonitoringActive = true)
                    }

                    override fun onStopped() {
                        _gnssStatusFlow.value = _gnssStatusFlow.value.copy(isMonitoringActive = false)
                    }

                    override fun onFirstFix(ttffMillis: Int) {
                        _gnssStatusFlow.value = _gnssStatusFlow.value.copy(
                            isFixAcquired = true,
                            ttffMs = ttffMillis
                        )
                    }

                    override fun onSatelliteStatusChanged(status: GnssStatus) {
                        val count = status.satelliteCount
                        val satList = ArrayList<GnssSatelliteInfo>(count)
                        var usedCount = 0
                        var totalCn0 = 0f

                        for (i in 0 until count) {
                            val svid = status.getSvid(i)
                            val constType = status.getConstellationType(i)
                            val cn0 = status.getCn0DbHz(i)
                            val used = status.usedInFix(i)
                            val elev = status.getElevationDegrees(i)
                            val azim = status.getAzimuthDegrees(i)

                            if (used) usedCount++
                            totalCn0 += cn0

                            val constName = when (constType) {
                                GnssStatus.CONSTELLATION_GPS -> "GPS 🇺🇸"
                                GnssStatus.CONSTELLATION_GLONASS -> "GLONASS 🇷🇺"
                                GnssStatus.CONSTELLATION_BEIDOU -> "BEIDOU 🇨🇳"
                                GnssStatus.CONSTELLATION_GALILEO -> "GALILEO 🇪🇺"
                                GnssStatus.CONSTELLATION_QZSS -> "QZSS 🇯🇵"
                                GnssStatus.CONSTELLATION_IRNSS -> "NavIC 🇮🇳"
                                else -> "قمر صناعي 🛰️"
                            }

                            satList.add(
                                GnssSatelliteInfo(
                                    svid = svid,
                                    constellationName = constName,
                                    constellationType = constType,
                                    cn0DbHz = cn0,
                                    usedInFix = used,
                                    elevationDegrees = elev,
                                    azimuthDegrees = azim
                                )
                            )
                        }

                        val avgCn0 = if (count > 0) totalCn0 / count else 0f
                        val rating = when {
                            usedCount >= 8 && avgCn0 >= 28f -> "إشارة جبارة وممتازة 🟢"
                            usedCount >= 4 && avgCn0 >= 20f -> "إشارة جيدة جداً 🟡"
                            usedCount > 0 -> "إشارة ضعيفة / مباني مرتفعة 🔴"
                            else -> "جاري البحث عن أقمار 🛰️"
                        }

                        _gnssStatusFlow.value = GnssStatusSummary(
                            totalSatellites = count,
                            usedSatellites = usedCount,
                            satellites = satList.sortedByDescending { it.cn0DbHz },
                            isFixAcquired = usedCount >= 3,
                            ttffMs = _gnssStatusFlow.value.ttffMs,
                            isMonitoringActive = true,
                            averageCn0 = avgCn0,
                            qualityRating = rating
                        )
                    }
                }

                gnssCallback = callback
                locationManager.registerGnssStatusCallback(callback, Handler(Looper.getMainLooper()))
                isListening = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopMonitoring() {
        if (!isListening) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                gnssCallback?.let {
                    locationManager.unregisterGnssStatusCallback(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            gnssCallback = null
            isListening = false
            _gnssStatusFlow.value = _gnssStatusFlow.value.copy(isMonitoringActive = false)
        }
    }
}

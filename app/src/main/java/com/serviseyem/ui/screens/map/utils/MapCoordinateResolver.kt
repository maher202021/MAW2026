package com.serviseyem.ui.screens.map.utils

import androidx.compose.ui.geometry.Offset
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.utils.getCityCenterCoords
import kotlin.math.*

/**
 * 📍 ResolvedCoordinate - الإحداثي الموحد مع حالة الدقة
 */
data class ResolvedCoordinate(
    val latitude: Double,
    val longitude: Double,
    val isExact: Boolean,
    val source: String, // "GPS", "LANDMARK_OFFSET", "CITY_FALLBACK", "MANUAL"
    val cityName: String,
    val neighborhood: String = ""
)

/**
 * 🎯 MapCoordinateResolver
 * المصدر الوحيد والنهائي للحقيقة (Single Source of Truth) للإحداثيات داخل التطبيق
 * يوحد الحسابات بين RealLeafletMapView و OfflineInteractiveMap و RadarRenderer
 */
object MapCoordinateResolver {

    // حدود الجمهورية اليمنية الجغرافية
    const val YEMEN_MIN_LAT = 11.5
    const val YEMEN_MAX_LAT = 19.5
    const val YEMEN_MIN_LNG = 41.5
    const val YEMEN_MAX_LNG = 54.5

    // صنعاء كنقطة ارتكاز افتراضية
    const val DEFAULT_YEMEN_LAT = 15.3694
    const val DEFAULT_YEMEN_LNG = 44.1910

    /**
     * التحقق مما إذا كانت الإحداثيات تقع داخل حدود اليمن
     */
    fun isWithinYemen(lat: Double, lng: Double): Boolean {
        return lat in YEMEN_MIN_LAT..YEMEN_MAX_LAT && lng in YEMEN_MIN_LNG..YEMEN_MAX_LNG
    }

    /**
     * حل وتوحيد إحداثيات مزود الخدمة (فني / مهني)
     */
    fun resolveProvider(provider: ProviderEntity): ResolvedCoordinate {
        val hasExactGps = provider.latitude != 0.0 && provider.longitude != 0.0 && isWithinYemen(provider.latitude, provider.longitude)
        if (hasExactGps) {
            return ResolvedCoordinate(
                latitude = provider.latitude,
                longitude = provider.longitude,
                isExact = true,
                source = "GPS",
                cityName = provider.cityId,
                neighborhood = provider.localNeighborhood
            )
        }

        // استخدام مركز المدينة مع إزاحة حتمية (Deterministic Offset) لمنع تراكب الدبابيس
        val cityCoords = getCityCenterCoords(provider.cityId)
        val offset = getDeterministicOffset(provider.id.ifBlank { provider.phone })
        return ResolvedCoordinate(
            latitude = cityCoords.first + offset.first,
            longitude = cityCoords.second + offset.second,
            isExact = false,
            source = "CITY_FALLBACK",
            cityName = provider.cityId,
            neighborhood = provider.localNeighborhood
        )
    }

    /**
     * حل وتوحيد إحداثيات المتجر أو المطعم أو المركز الطبي
     */
    fun resolveStore(store: StoreEntity): ResolvedCoordinate {
        val hasExactGps = store.latitude != 0.0 && store.longitude != 0.0 && isWithinYemen(store.latitude, store.longitude)
        if (hasExactGps) {
            return ResolvedCoordinate(
                latitude = store.latitude,
                longitude = store.longitude,
                isExact = true,
                source = "GPS",
                cityName = store.cityId,
                neighborhood = store.localNeighborhood
            )
        }

        val cityCoords = getCityCenterCoords(store.cityId)
        val offset = getDeterministicOffset(store.id)
        return ResolvedCoordinate(
            latitude = cityCoords.first + offset.first,
            longitude = cityCoords.second + offset.second,
            isExact = false,
            source = "CITY_FALLBACK",
            cityName = store.cityId,
            neighborhood = store.localNeighborhood
        )
    }

    /**
     * حل وتوحيد إحداثيات العقار
     */
    fun resolveProperty(property: PropertyEntity): ResolvedCoordinate {
        val hasExactGps = property.latitude != 0.0 && property.longitude != 0.0 && isWithinYemen(property.latitude, property.longitude)
        if (hasExactGps) {
            return ResolvedCoordinate(
                latitude = property.latitude,
                longitude = property.longitude,
                isExact = true,
                source = "GPS",
                cityName = property.cityId,
                neighborhood = property.localNeighborhood
            )
        }

        val cityCoords = getCityCenterCoords(property.cityId)
        val offset = getDeterministicOffset(property.id)
        return ResolvedCoordinate(
            latitude = cityCoords.first + offset.first,
            longitude = cityCoords.second + offset.second,
            isExact = false,
            source = "CITY_FALLBACK",
            cityName = property.cityId,
            neighborhood = property.localNeighborhood
        )
    }

    /**
     * توليد إزاحة حتمية ثابتة مستندة إلى معرف العنصر لمنع تراكم النقاط في نفس النقطة المركزية
     */
    fun getDeterministicOffset(entityId: String, maxOffsetMeters: Double = 1200.0): Pair<Double, Double> {
        val hash = abs(entityId.hashCode())
        val angleRad = (hash % 360) * (PI / 180.0)
        val distance = 150.0 + (hash % maxOffsetMeters.toInt())

        // 1 درجة عرض = ~111,000 متر
        // 1 درجة طول = ~111,000 * cos(lat)
        val latOffset = (distance * cos(angleRad)) / 111320.0
        val lngOffset = (distance * sin(angleRad)) / (111320.0 * cos(Math.toRadians(DEFAULT_YEMEN_LAT)))

        return Pair(latOffset, lngOffset)
    }

    /**
     * تحويل الإحداثيات الجغرافية (Lat/Lng) إلى إحداثيات شاشة (Offset) لرسام الكانفاس Native Canvas / RadarRenderer
     */
    fun latLngToScreen(
        targetLat: Double,
        targetLng: Double,
        centerLat: Double,
        centerLng: Double,
        zoomScale: Float,
        screenWidth: Float,
        screenHeight: Float
    ): Offset {
        val metersPerDegreeLat = 111320.0
        val metersPerDegreeLng = 111320.0 * cos(Math.toRadians(centerLat))

        val deltaX = (targetLng - centerLng) * metersPerDegreeLng
        val deltaY = (centerLat - targetLat) * metersPerDegreeLat // Y موجب نحو الأسفل في الشاشة

        val screenX = (screenWidth / 2f) + (deltaX.toFloat() * zoomScale)
        val screenY = (screenHeight / 2f) + (deltaY.toFloat() * zoomScale)

        return Offset(screenX, screenY)
    }

    /**
     * حساب الزاوية (Bearing) بالدرجات بين نقطتين لتوجيه الرادار والبوصلة
     */
    fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)

        val bearingDeg = Math.toDegrees(theta)
        return ((bearingDeg + 360) % 360).toFloat()
    }
}

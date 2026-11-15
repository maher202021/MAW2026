package com.serviseyem.ui.screens.map.components

import androidx.compose.ui.graphics.Color
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

internal fun buildOfflineMapPoints(
    nearbyProviders: List<ProviderEntity>,
    nearbyStores: List<StoreEntity>,
    nearbyProperties: List<PropertyEntity>,
    selectedCity: String,
    originLat: Double,
    originLng: Double,
    metersPerDegreeLat: Double,
    metersPerDegreeLng: Double
): List<InteractiveMapPoint> {
    val points = mutableListOf<InteractiveMapPoint>()

    nearbyProviders.forEach { p ->
        val (xMeters, yMeters) = projectEntityMeters(
            id = p.id,
            lat = p.latitude,
            lng = p.longitude,
            cityId = p.cityId,
            selectedCity = selectedCity,
            originLat = originLat,
            originLng = originLng,
            metersPerDegreeLat = metersPerDegreeLat,
            metersPerDegreeLng = metersPerDegreeLng
        )
        points.add(
            InteractiveMapPoint(
                id = p.id,
                name = p.name.ifBlank { "فني متخصص" },
                category = p.profession.ifBlank { "خدمات صيانة" },
                type = "PROVIDER",
                emoji = "👷",
                color = Color(0xFF00E5FF),
                xMeters = xMeters,
                yMeters = yMeters,
                rating = p.rating.toDouble().coerceAtLeast(1.0),
                phone = p.phone,
                originalEntity = p
            )
        )
    }

    nearbyStores.forEach { s ->
        val (xMeters, yMeters) = projectEntityMeters(
            id = s.id,
            lat = s.latitude,
            lng = s.longitude,
            cityId = s.cityId,
            selectedCity = selectedCity,
            originLat = originLat,
            originLng = originLng,
            metersPerDegreeLat = metersPerDegreeLat,
            metersPerDegreeLng = metersPerDegreeLng
        )

        val isMedical = s.sectionId.contains("medical") || s.categoryId.contains("medical") || s.name.contains("طبي") || s.name.contains("صيدلية")
        val isRestaurant = !isMedical && (s.sectionId.contains("restaurant") || s.categoryId.contains("restaurant") || s.name.contains("مطعم") || s.name.contains("كافيه"))

        val pinColor = when {
            isMedical -> Color(0xFFEC4899)
            isRestaurant -> Color(0xFFF59E0B)
            else -> Color(0xFF10B981)
        }
        val pinEmoji = when {
            isMedical -> "🏥"
            isRestaurant -> "🍔"
            else -> "🏪"
        }

        points.add(
            InteractiveMapPoint(
                id = s.id,
                name = s.name.ifBlank { "متجر" },
                category = s.description.ifBlank { if (isMedical) "مركز طبي" else if (isRestaurant) "مطعم" else "متجر" },
                type = if (isMedical) "MEDICAL" else if (isRestaurant) "RESTAURANT" else "STORE",
                emoji = pinEmoji,
                color = pinColor,
                xMeters = xMeters,
                yMeters = yMeters,
                rating = s.rating.toDouble().coerceAtLeast(1.0),
                phone = s.phone,
                originalEntity = s
            )
        )
    }

    nearbyProperties.forEach { prop ->
        val (xMeters, yMeters) = projectEntityMeters(
            id = prop.id,
            lat = prop.latitude,
            lng = prop.longitude,
            cityId = prop.cityId,
            selectedCity = selectedCity,
            originLat = originLat,
            originLng = originLng,
            metersPerDegreeLat = metersPerDegreeLat,
            metersPerDegreeLng = metersPerDegreeLng
        )
        points.add(
            InteractiveMapPoint(
                id = prop.id,
                name = prop.title.ifBlank { "عقار معروض" },
                category = "عقار",
                type = "PROPERTY",
                emoji = "🏠",
                color = Color(0xFF8B5CF6),
                xMeters = xMeters,
                yMeters = yMeters,
                rating = 5.0,
                phone = prop.phone,
                originalEntity = prop
            )
        )
    }

    return points
}

private fun projectEntityMeters(
    id: String,
    lat: Double,
    lng: Double,
    cityId: String,
    selectedCity: String,
    originLat: Double,
    originLng: Double,
    metersPerDegreeLat: Double,
    metersPerDegreeLng: Double
): Pair<Float, Float> {
    val hasValidCoords = lat != 0.0 && lng != 0.0 &&
        !lat.isNaN() && !lng.isNaN() &&
        lat in -90.0..90.0 && lng in -180.0..180.0

    return if (hasValidCoords) {
        val dLng = lng - originLng
        val dLat = lat - originLat
        Pair((dLng * metersPerDegreeLng).toFloat(), (-dLat * metersPerDegreeLat).toFloat())
    } else {
        val targetCity = if (cityId.isNotBlank()) cityId else selectedCity
        val cityCoords = OfflineMapManager.getCityCoordinates(targetCity)
        val dLng = cityCoords.longitude - originLng
        val dLat = cityCoords.latitude - originLat
        val baseXMeters = (dLng * metersPerDegreeLng).toFloat()
        val baseYMeters = (-dLat * metersPerDegreeLat).toFloat()

        val idHash = id.hashCode().toLong()
        val hashAngle = (abs(idHash) % 360).toDouble() * (Math.PI / 180.0)
        val hashOffsetMeters = 40.0 + (abs(idHash ushr 16) % 120)
        val offsetX = (hashOffsetMeters * cos(hashAngle)).toFloat()
        val offsetY = (hashOffsetMeters * sin(hashAngle)).toFloat()
        Pair(baseXMeters + offsetX, baseYMeters + offsetY)
    }
}

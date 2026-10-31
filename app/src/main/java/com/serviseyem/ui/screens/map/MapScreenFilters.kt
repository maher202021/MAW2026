package com.serviseyem.ui.screens.map

import com.serviseyem.data.PropertyEntity
import com.serviseyem.ui.*
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.components.MarkerRenderer
import com.serviseyem.utils.getPropertyCoords
import com.serviseyem.utils.getProviderCoords
import com.serviseyem.utils.getStoreCoords
import com.serviseyem.ui.screens.map.utils.CityAliases
import com.serviseyem.ui.screens.map.utils.MapDistanceCalculator
import kotlin.math.cos
import kotlin.math.sin

/**
 * 🗺️ MapScreenFilters
 * Resilient filtering engine for Yemeni cities, categories, search queries, and Radar coordinates conversion.
 */
object MapScreenFilters {

    fun matchesCityFilter(
        cityFilter: String,
        entityCityId: String,
        entityArea: String,
        entityNeighborhood: String
    ): Boolean {
        if (cityFilter.isEmpty() || cityFilter == "الكل" || cityFilter == "جميع المدن والمحافظات") return true
        val normFilter = cityFilter.trim().lowercase()
        val combined = "$entityCityId $entityArea $entityNeighborhood".lowercase()

        val matchingKey = CityAliases.ALIASES.keys.find { normFilter.contains(it.lowercase()) }
        val aliases = if (matchingKey != null) CityAliases.ALIASES[matchingKey] ?: listOf(normFilter) else listOf(normFilter)

        return aliases.any { alias -> combined.contains(alias.lowercase()) }
    }

    fun filterProviders(
        providers: List<ProviderEntity>,
        selectedCategory: String,
        selectedCity: String,
        searchQuery: String
    ): List<ProviderEntity> {
        val cleanQuery = searchQuery.trim().lowercase()
        return providers.filter { p ->
            val matchesCat = selectedCategory == "ALL" || selectedCategory == "PROVIDERS"
            val matchesCity = matchesCityFilter(selectedCity, p.cityId, p.area, p.localNeighborhood)
            val matchesQuery = cleanQuery.isEmpty() ||
                    p.name.lowercase().contains(cleanQuery) ||
                    p.profession.lowercase().contains(cleanQuery) ||
                    p.customCategoryName.lowercase().contains(cleanQuery) ||
                    p.specialization.lowercase().contains(cleanQuery) ||
                    p.area.lowercase().contains(cleanQuery) ||
                    p.localNeighborhood.lowercase().contains(cleanQuery)
            matchesCat && matchesCity && matchesQuery
        }
    }

    fun filterStores(
        stores: List<StoreEntity>,
        selectedCategory: String,
        selectedCity: String,
        searchQuery: String
    ): List<StoreEntity> {
        val cleanQuery = searchQuery.trim().lowercase()
        return stores.filter { s ->
            val isMedical = s.sectionId.contains("medical") || s.categoryId.contains("medical") || s.name.contains("طبي") || s.name.contains("صيدلية") || s.name.contains("مستشفى") || s.name.contains("عيادة")
            val isRestaurant = !isMedical && (s.sectionId.contains("restaurant") || s.categoryId.contains("restaurant") || s.name.contains("مطعم") || s.name.contains("كافيه") || s.name.contains("مأكولات") || s.name.contains("شاورما"))

            val matchesCat = when (selectedCategory) {
                "ALL" -> true
                "STORES" -> !isMedical && !isRestaurant
                "RESTAURANTS" -> isRestaurant
                "MEDICAL" -> isMedical
                else -> false
            }
            val matchesCity = matchesCityFilter(selectedCity, s.cityId, "", s.localNeighborhood)
            val matchesQuery = cleanQuery.isEmpty() ||
                    s.name.lowercase().contains(cleanQuery) ||
                    s.description.lowercase().contains(cleanQuery) ||
                    s.localNeighborhood.lowercase().contains(cleanQuery) ||
                    s.sectionId.lowercase().contains(cleanQuery) ||
                    s.categoryId.lowercase().contains(cleanQuery)
            matchesCat && matchesCity && matchesQuery
        }
    }

    fun filterProperties(
        properties: List<PropertyEntity>,
        selectedCategory: String,
        selectedCity: String,
        searchQuery: String
    ): List<PropertyEntity> {
        val cleanQuery = searchQuery.trim().lowercase()
        return properties.filter { pr ->
            val matchesCat = selectedCategory == "ALL" || selectedCategory == "PROPERTIES"
            val matchesCity = matchesCityFilter(selectedCity, pr.cityId, "", pr.localNeighborhood)
            val matchesQuery = cleanQuery.isEmpty() ||
                    pr.title.lowercase().contains(cleanQuery) ||
                    pr.description.lowercase().contains(cleanQuery) ||
                    pr.localNeighborhood.lowercase().contains(cleanQuery)
            matchesCat && matchesCity && matchesQuery
        }
    }

    fun buildRadarPoints(
        filteredProviders: List<ProviderEntity>,
        filteredStores: List<StoreEntity>,
        filteredProperties: List<PropertyEntity>,
        safeUserLat: Double,
        safeUserLng: Double
    ): List<MarkerRenderer.MapItemPoint> {
        val list = mutableListOf<MarkerRenderer.MapItemPoint>()

        fun addPoints(items: List<Any>, typeName: String) {
            items.forEach { item ->
                val lat: Double
                val lng: Double
                val title: String
                val rating: Double
                val isAvailable: Boolean
                val id: String

                when (item) {
                    is ProviderEntity -> {
                        id = item.id
                        lat = item.latitude
                        lng = item.longitude
                        title = item.name
                        rating = item.rating.toDouble()
                        isAvailable = item.isAvailable
                    }
                    is StoreEntity -> {
                        id = item.id
                        lat = item.latitude
                        lng = item.longitude
                        title = item.name
                        rating = item.rating.toDouble()
                        isAvailable = item.isActive
                    }
                    is PropertyEntity -> {
                        id = item.id
                        lat = item.latitude
                        lng = item.longitude
                        title = item.title
                        rating = 5.0
                        isAvailable = item.isActive
                    }
                    else -> return@forEach
                }

                val d = MapDistanceCalculator.calculateDistanceMeters(safeUserLat, safeUserLng, lat, lng)
                val r = (d / 1000.0).coerceIn(0.5, 30.0).toFloat() * 10f
                
                val dLng = Math.toRadians(lng - safeUserLng)
                val lat1 = Math.toRadians(safeUserLat)
                val lat2 = Math.toRadians(lat)
                val y = sin(dLng) * cos(lat2)
                val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
                val azimuth = Math.atan2(y, x)

                val derivedType = when (item) {
                    is StoreEntity -> {
                        val isMedical = item.sectionId.contains("medical") || item.categoryId.contains("medical") || item.name.contains("طبي") || item.name.contains("صيدلية")
                        val isRestaurant = !isMedical && (item.sectionId.contains("restaurant") || item.categoryId.contains("restaurant") || item.name.contains("مطعم") || item.name.contains("كافيه"))
                        if (isMedical) "MEDICAL" else if (isRestaurant) "RESTAURANT" else "STORE"
                    }
                    else -> typeName
                }

                list.add(
                    MarkerRenderer.MapItemPoint(
                        id = id,
                        title = title,
                        type = derivedType,
                        x = (r * sin(azimuth)).toFloat(),
                        y = (r * -cos(azimuth)).toFloat(),
                        rating = rating,
                        isAvailable = isAvailable,
                        originalItem = item
                    )
                )
            }
        }

        addPoints(filteredProviders, "PROVIDER")
        addPoints(filteredStores, "STORE")
        addPoints(filteredProperties, "PROPERTY")

        return list
    }
}

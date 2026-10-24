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
import com.serviseyem.ui.screens.home.extensions.*
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
            val isMedical = s.isMedicalCenter()
            val isRestaurant = s.isRestaurantOrCafe()

            val matchesCat = when (selectedCategory) {
                "ALL" -> true
                "PROVIDERS" -> false
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
        var angle = 0.0

        filteredProviders.forEach { p ->
            val coords = getProviderCoords(p)
            val d = MapDistanceCalculator.calculateDistanceMeters(safeUserLat, safeUserLng, coords.first, coords.second)
            val r = (d / 1000.0).coerceIn(1.0, 30.0).toFloat() * 10f
            val rad = Math.toRadians(angle)
            list.add(
                MarkerRenderer.MapItemPoint(
                    id = p.id,
                    title = p.name,
                    type = "PROVIDER",
                    x = (r * cos(rad)).toFloat(),
                    y = (r * sin(rad)).toFloat(),
                    rating = p.rating.toDouble(),
                    isAvailable = p.isAvailable,
                    originalItem = p
                )
            )
            angle += 40.0
        }

        filteredStores.forEach { s ->
            val coords = getStoreCoords(s)
            val isMedical = s.isMedicalCenter()
            val isRestaurant = s.isRestaurantOrCafe()
            val typeStr = if (isMedical) "MEDICAL" else if (isRestaurant) "RESTAURANT" else "STORE"
            val d = MapDistanceCalculator.calculateDistanceMeters(safeUserLat, safeUserLng, coords.first, coords.second)
            val r = (d / 1000.0).coerceIn(1.0, 30.0).toFloat() * 10f
            val rad = Math.toRadians(angle)
            list.add(
                MarkerRenderer.MapItemPoint(
                    id = s.id,
                    title = s.name,
                    type = typeStr,
                    x = (r * cos(rad)).toFloat(),
                    y = (r * sin(rad)).toFloat(),
                    rating = s.rating.toDouble(),
                    isAvailable = s.isActive,
                    originalItem = s
                )
            )
            angle += 35.0
        }

        filteredProperties.forEach { pr ->
            val coords = getPropertyCoords(pr)
            val d = MapDistanceCalculator.calculateDistanceMeters(safeUserLat, safeUserLng, coords.first, coords.second)
            val r = (d / 1000.0).coerceIn(1.0, 30.0).toFloat() * 10f
            val rad = Math.toRadians(angle)
            list.add(
                MarkerRenderer.MapItemPoint(
                    id = pr.id,
                    title = pr.title,
                    type = "PROPERTY",
                    x = (r * cos(rad)).toFloat(),
                    y = (r * sin(rad)).toFloat(),
                    rating = 5.0,
                    isAvailable = pr.isActive,
                    originalItem = pr
                )
            )
            angle += 45.0
        }
        return list
    }
}

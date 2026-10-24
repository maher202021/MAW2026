package com.serviseyem.ui.viewmodels
import com.serviseyem.ui.helpers.AppState

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.serviseyem.ui.*
import com.serviseyem.data.BannerEntity
import com.serviseyem.data.CategoryEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.data.PropertyEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID


import javax.inject.Inject


open class HomeViewModel @Inject constructor(
    val appState: AppState
) : BaseViewModel() {

    internal val _categories get() = appState._categories
    val categories: StateFlow<List<CategoryEntity>> = _categories.asStateFlow()

    internal val _providers get() = appState._providers
    val providers: StateFlow<List<ProviderEntity>> = _providers.asStateFlow()

    internal val _filteredProviders = MutableStateFlow<List<ProviderEntity>>(emptyList())
    val filteredProviders: StateFlow<List<ProviderEntity>> = _filteredProviders.asStateFlow()

    internal val _filteredStores = MutableStateFlow<List<StoreEntity>>(emptyList())
    val filteredStores: StateFlow<List<StoreEntity>> = _filteredStores.asStateFlow()

    internal val _filteredProperties = MutableStateFlow<List<PropertyEntity>>(emptyList())
    val filteredProperties: StateFlow<List<PropertyEntity>> = _filteredProperties.asStateFlow()

    internal val _banners get() = appState._banners
    val banners: StateFlow<List<BannerEntity>> = _banners.asStateFlow()

    internal val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    internal val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    internal val _filterVipOnly = MutableStateFlow(false)
    val filterVipOnly: StateFlow<Boolean> = _filterVipOnly.asStateFlow()

    internal val _filterAvailableOnly = MutableStateFlow(false)
    val filterAvailableOnly: StateFlow<Boolean> = _filterAvailableOnly.asStateFlow()

    internal val _filterByCurrentCityOnly = MutableStateFlow(false)
    val filterByCurrentCityOnly: StateFlow<Boolean> = _filterByCurrentCityOnly.asStateFlow()

    internal val _filterCityId = MutableStateFlow<String?>(null)
    val filterCityId: StateFlow<String?> = _filterCityId.asStateFlow()

    internal val _filterNeighborhoodName = MutableStateFlow("")
    val filterNeighborhoodName: StateFlow<String> = _filterNeighborhoodName.asStateFlow()

    internal val _phoneOrNameFilter = MutableStateFlow("")
    val phoneOrNameFilter: StateFlow<String> = _phoneOrNameFilter.asStateFlow()

    internal val _activeBrowserTab = MutableStateFlow("الرئيسية")
    val activeBrowserTab: StateFlow<String> = _activeBrowserTab.asStateFlow()

    internal val _maxKmRadius get() = appState._maxKmRadius
    val maxKmRadius: StateFlow<Int> = _maxKmRadius.asStateFlow()

    // ==========================================
    // 🗺️ Map & Location Utilities (Transferred from MapViewModel)
    // ==========================================
    internal val _userLatitude = MutableStateFlow(15.3694) // Default Sana'a
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()

    internal val _userLongitude = MutableStateFlow(44.1910)
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    internal val _selectedCityName = MutableStateFlow("صنعاء")
    val selectedCityName: StateFlow<String> = _selectedCityName.asStateFlow()

    internal val _isManualLocationMode = MutableStateFlow(false)
    val isManualLocationMode: StateFlow<Boolean> = _isManualLocationMode.asStateFlow()

    private val cachedFilteredResults = mutableMapOf<String, List<ProviderEntity>>()

    init {
        viewModelScope.launch {
            _providers.collect {
                clearCache()
                applyFilters()
            }
        }
    }

    fun clearCache() {
        cachedFilteredResults.clear()
    }

    private fun generateCacheKey(vararg params: Any?): String {
        return params.joinToString("_")
    }

    fun applyFilters(userResidence: String = "") {
        val allProviders = _providers.value
        val selectedCat = _selectedCategoryId.value
        val query = _searchQuery.value.trim().lowercase()
        val vipOnly = _filterVipOnly.value
        val availOnly = _filterAvailableOnly.value
        val cityId = _filterCityId.value
        val neighborhood = _filterNeighborhoodName.value.trim().lowercase()
        val phoneName = _phoneOrNameFilter.value.trim().lowercase()
        val currentCityOnly = _filterByCurrentCityOnly.value

        val radiusKm = _maxKmRadius.value
        val userLat = _userLatitude.value
        val userLng = _userLongitude.value

        val cacheKey = generateCacheKey(
            allProviders.hashCode(),
            selectedCat,
            query,
            vipOnly,
            availOnly,
            cityId,
            neighborhood,
            phoneName,
            currentCityOnly,
            userResidence,
            radiusKm,
            userLat,
            userLng
        )

        cachedFilteredResults[cacheKey]?.let {
            _filteredProviders.value = it
            return
        }

        var filtered = allProviders

        if (!selectedCat.isNullOrBlank() && selectedCat != "ALL" && selectedCat != "الكل") {
            filtered = filtered.filter { it.categoryId == selectedCat }
        }
        if (query.isNotEmpty()) {
            filtered = filtered.filter { 
                it.name.lowercase().contains(query) || 
                it.profession.lowercase().contains(query) ||
                it.specialization.lowercase().contains(query) ||
                it.customCategoryName.lowercase().contains(query) ||
                it.area.lowercase().contains(query) || 
                it.localNeighborhood.lowercase().contains(query) ||
                it.phone.contains(query)
            }
        }
        _filteredProviders.value = filtered.take(100)

        // Store and Property filtering
        val allStores = appState._stores.value
        val allProperties = appState._properties.value
        val allProducts = appState._products.value

        val matchedStoreIds = if (query.isEmpty()) emptySet() else allProducts.filter { 
            it.name.lowercase().contains(query) || it.description.lowercase().contains(query) 
        }.map { it.storeId }.toSet()

        _filteredStores.value = allStores.filter { s ->
            query.isEmpty() ||
            s.name.lowercase().contains(query) ||
            s.phone.contains(query) ||
            s.categoryId.lowercase().contains(query) ||
            matchedStoreIds.contains(s.id)
        }.take(50)

        _filteredProperties.value = allProperties.filter { p ->
            query.isEmpty() ||
            p.title.lowercase().contains(query) ||
            p.phone.contains(query) ||
            p.cityId.lowercase().contains(query) ||
            p.localNeighborhood.lowercase().contains(query) ||
            p.propertyType.lowercase().contains(query)
        }.take(50)

        if (vipOnly) {
            filtered = filtered.filter { it.isVip || it.subscriptionStatus == "APPROVED" || it.subscriptionStatus == "ACCEPTED" }
        }
        if (availOnly) {
            filtered = filtered.filter { it.isAvailable }
        }
        if (cityId != null) {
            filtered = filtered.filter { it.cityId == cityId }
        }
        val cleanResidence = userResidence.trim().lowercase()
        if (_filterByCurrentCityOnly.value && cleanResidence.isNotEmpty() && cleanResidence != "الكل" && cleanResidence != "اليمن") {
            filtered = filtered.filter { p ->
                val pArea = p.area.trim().lowercase()
                val pCity = p.cityId.trim().lowercase()
                val pNeigh = p.localNeighborhood.trim().lowercase()
                (pArea.isNotEmpty() && (pArea.contains(cleanResidence) || cleanResidence.contains(pArea))) ||
                (pCity.isNotEmpty() && pCity.contains(cleanResidence)) ||
                (pNeigh.isNotEmpty() && pNeigh.contains(cleanResidence))
            }
        }
        if (neighborhood.isNotEmpty()) {
            filtered = filtered.filter { it.localNeighborhood.lowercase().contains(neighborhood) }
        }
        if (phoneName.isNotEmpty()) {
            filtered = filtered.filter { 
                it.name.lowercase().contains(phoneName) || 
                it.phone.contains(phoneName) 
            }
        }
        if (radiusKm in 1..499 && userLat != 0.0 && userLng != 0.0) {
            filtered = filtered.filter { p ->
                if (p.latitude == 0.0 && p.longitude == 0.0) {
                    true
                } else {
                    val distMeters = com.serviseyem.utils.calculateDistanceInMeters(userLat, userLng, p.latitude, p.longitude)
                    (distMeters / 1000.0) <= radiusKm.toDouble()
                }
            }
        }

        val finalResult = filtered.take(100)
        cachedFilteredResults[cacheKey] = finalResult
        _filteredProviders.value = finalResult
    }

    fun setActiveBrowserTab(tabName: String) {
        _activeBrowserTab.value = tabName
    }

    fun openBrowserTab(tabName: String, categoryId: String? = null, query: String = "") {
        _activeBrowserTab.value = tabName
        _selectedCategoryId.value = categoryId
        _searchQuery.value = query
        clearCache()
        applyFilters()
    }

    fun selectCategory(categoryId: String?, userResidence: String = "") {
        _selectedCategoryId.value = categoryId
        applyFilters(userResidence)
        
        try {
            val bundle = android.os.Bundle().apply {
                putString("category_id", categoryId ?: "all")
            }
            com.serviseyem.MyApplication.logFirebaseEvent("select_category", bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateSearchQuery(query: String, userResidence: String = "") {
        _searchQuery.value = query
        applyFilters(userResidence)

        try {
            val bundle = android.os.Bundle().apply {
                putString("search_query", query)
            }
            com.serviseyem.MyApplication.logFirebaseEvent("search_query", bundle)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleVipFilter(userResidence: String = "") {
        _filterVipOnly.value = !_filterVipOnly.value
        applyFilters(userResidence)
    }

    fun toggleAvailableFilter(userResidence: String = "") {
        _filterAvailableOnly.value = !_filterAvailableOnly.value
        applyFilters(userResidence)
    }

    fun toggleFilterByCurrentCityOnly(enabled: Boolean? = null, userResidence: String = "") {
        _filterByCurrentCityOnly.value = enabled ?: !_filterByCurrentCityOnly.value
        applyFilters(userResidence)
    }

    fun setCityFilter(cityId: String?, userResidence: String = "") {
        _filterCityId.value = cityId
        applyFilters(userResidence)
    }

    fun setNeighborhoodFilter(neighborhood: String, userResidence: String = "") {
        _filterNeighborhoodName.value = neighborhood
        applyFilters(userResidence)
    }

    fun setPhoneOrNameFilter(text: String, userResidence: String = "") {
        _phoneOrNameFilter.value = text
        applyFilters(userResidence)
    }

    fun setRadiusKm(km: Int, userResidence: String = "") {
        _maxKmRadius.value = km
        applyFilters(userResidence)
    }

    fun addBanner(title: String, url: String, redirect: String, type: String, size: String, duration: Int, displayTime: String = "طوال اليوم") {
        val banner = BannerEntity(
            id = "banner_" + UUID.randomUUID().toString().take(6),
            title = title,
            url = url,
            redirectCategory = redirect,
            type = type,
            size = size,
            duration = duration,
            displayTime = displayTime
        )
        db.collection("banners").document(banner.id).set(banner)
        triggerToast("🖼️ تم إضافة إعلان جديد: $title")
    }

    fun deleteBanner(bannerId: String) {
        db.collection("banners").document(bannerId).delete()
        triggerToast("🗑️ تم حذف الإعلان")
    }

    fun addNewCategory(nameAr: String, nameEn: String, icon: String, description: String, parentId: String = "", isMainCategory: Boolean = true) {
        val nextId = UUID.randomUUID().toString().take(6)
        val extraCat = CategoryEntity(
            id = nextId,
            name = nameAr,
            nameEn = nameEn,
            icon = icon,
            description = description,
            order = _categories.value.size + 1,
            parentId = parentId,
            isMainCategory = isMainCategory
        )
        db.collection("categories").document(nextId).set(extraCat)
        triggerToast("📂 تم إضافة القسم الجديد $nameAr بنجاح!")
    }

    fun deleteCategory(categoryId: String) {
        db.collection("categories").document(categoryId).delete()
        triggerToast("🗑️ تم حذف القسم بنجاح")
    }

    // ==========================================
    // 🗺️ Map & Location Utilities (Transferred from MapViewModel)
    // ==========================================
    fun updateUserLocation(lat: Double, lng: Double) {
        if (lat != 0.0 && lng != 0.0) {
            _userLatitude.value = lat
            _userLongitude.value = lng
        }
    }

    fun setManualLocation(lat: Double, lng: Double, cityName: String = "موقع مخصص") {
        _isManualLocationMode.value = true
        _userLatitude.value = lat
        _userLongitude.value = lng
        _selectedCityName.value = cityName
    }

    fun selectYemeniCity(cityName: String) {
        val coords = when (cityName) {
            "عدن" -> Pair(12.7855, 45.0187)
            "تعز" -> Pair(13.5795, 44.0209)
            "إب" -> Pair(13.9667, 44.1833)
            "الحديدة" -> Pair(14.7978, 42.9545)
            "المكلا" -> Pair(14.5425, 49.1242)
            "مأرب" -> Pair(15.4633, 45.3267)
            "ذمار" -> Pair(14.5427, 44.4051)
            else -> Pair(15.3694, 44.1910) // صنعاء
        }
        setManualLocation(coords.first, coords.second, cityName)
    }

    fun calculateFormattedDistance(destLat: Double, destLng: Double): String {
        val meters = com.serviseyem.utils.calculateDistanceInMeters(_userLatitude.value, _userLongitude.value, destLat, destLng)
        return com.serviseyem.utils.formatDistance(meters)
    }

    fun calculateEtaText(destLat: Double, destLng: Double): String {
        val meters = com.serviseyem.utils.calculateDistanceInMeters(_userLatitude.value, _userLongitude.value, destLat, destLng)
        val km = meters / 1000.0
        return if (km < 1.0) {
            val mins = (km / 5.0 * 60.0).toInt().coerceAtLeast(1)
            "⏱️ ~ $mins دقيقة سيراً"
        } else {
            val mins = (km / 40.0 * 60.0).toInt().coerceAtLeast(2)
            "🚘 ~ $mins دقيقة بالسيارة"
        }
    }

    fun openExternalDirections(context: android.content.Context, destLat: Double, destLng: Double, label: String = "الوجهة") {
        val encodedLabel = android.net.Uri.encode(label.ifBlank { "الوجهة" })
        try {
            val gmapsUri = android.net.Uri.parse("geo:$destLat,$destLng?q=$destLat,$destLng($encodedLabel)")
            val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmapsUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val browserUri = android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng")
                val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, browserUri).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            try {
                val fallbackUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$destLat,$destLng")
                val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, fallbackUri).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (ex: Exception) {
                android.util.Log.e("HomeViewModel", "Failed to open external directions", ex)
                triggerToast("❌ تعذر فتح تطبيق الخرائط على هذا الجهاز")
            }
        }
    }
}

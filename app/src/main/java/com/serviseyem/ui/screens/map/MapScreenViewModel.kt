package com.serviseyem.ui.screens.map

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import kotlinx.coroutines.flow.*

/**
 * 🗺️ MapScreenViewModel
 * Specialized ViewModel handling map entities, filtering, and Radar-First state.
 *
 * ⚠️ ملاحظة: هذا الـ ViewModel غير مستخدم حالياً.
 * MapScreen يستخدم MapScreenState (Compose-only).
 * يمكن تفعيله لاحقاً للاستفادة من Hilt Injection و StateFlow.
 */
class MapScreenViewModel : ViewModel() {

    // [تعديل] الخريطة المباشرة OpenStreetMap هي الوضع الافتراضي لتقديم التجربة التفاعلية الكاملة
    private val _uiState = MutableStateFlow<MapScreenUiState>(MapScreenUiState.Success(isRadarMode = false))
    val uiState: StateFlow<MapScreenUiState> = _uiState.asStateFlow()

    private val _isRadarMode = MutableStateFlow(false)
    val isRadarMode: StateFlow<Boolean> = _isRadarMode.asStateFlow()

    private val _hasMapError = MutableStateFlow(false)
    val hasMapError: StateFlow<Boolean> = _hasMapError.asStateFlow()

    private val _retryCount = MutableStateFlow(0)
    val retryCount: StateFlow<Int> = _retryCount.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCity = MutableStateFlow("الكل")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun setRadarMode(enabled: Boolean) {
        _isRadarMode.value = enabled
        if (enabled) {
            _hasMapError.value = false
        }
        val cur = _uiState.value
        if (cur is MapScreenUiState.Success) {
            _uiState.value = cur.copy(isRadarMode = enabled)
        }
    }

    /**
     * التحويل الفوري للرادار المحلي عند انتهاء مهلة 3 ثوانٍ أو حدوث خطأ في تحميل الخريطة
     */
    fun onMapLoadError() {
        _isRadarMode.value = true
        _hasMapError.value = true
        val cur = _uiState.value
        if (cur is MapScreenUiState.Success) {
            _uiState.value = cur.copy(isRadarMode = true)
        }
    }

    /**
     * إعادة محاولة تحميل خريطة الشبكة (محاولة واحدة فقط كحد أقصى)
     */
    fun retryOnlineMap(): Boolean {
        if (_retryCount.value >= 1) return false
        _retryCount.value += 1
        _hasMapError.value = false
        setRadarMode(false)
        return true
    }

    fun dismissMapError() {
        _hasMapError.value = false
        setRadarMode(true)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCity(city: String) {
        _selectedCity.value = city
    }

    fun updateCategory(category: String) {
        _selectedCategory.value = category
    }

    /**
     * تصفية المزودين بناءً على حالة ViewModel الحالية (المدينة، التصنيف، والبحث)
     */
    fun filterProviders(providers: List<ProviderEntity>): List<ProviderEntity> {
        return MapScreenFilters.filterProviders(
            providers = providers,
            selectedCategory = _selectedCategory.value,
            selectedCity = _selectedCity.value,
            searchQuery = _searchQuery.value
        )
    }

    /**
     * تصفية المتاجر والمطاعم والمراكز الطبية
     */
    fun filterStores(stores: List<StoreEntity>): List<StoreEntity> {
        return MapScreenFilters.filterStores(
            stores = stores,
            selectedCategory = _selectedCategory.value,
            selectedCity = _selectedCity.value,
            searchQuery = _searchQuery.value
        )
    }

    /**
     * تصفية العقارات
     */
    fun filterProperties(properties: List<PropertyEntity>): List<PropertyEntity> {
        return MapScreenFilters.filterProperties(
            properties = properties,
            selectedCategory = _selectedCategory.value,
            selectedCity = _selectedCity.value,
            searchQuery = _searchQuery.value
        )
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCity.value = "الكل"
        _selectedCategory.value = "ALL"
        _isRadarMode.value = true
    }

    fun onEvent(event: MapScreenEvents) {
        when (event) {
            is MapScreenEvents.OnSearchQueryChanged -> updateSearchQuery(event.query)
            is MapScreenEvents.OnCitySelected -> updateCity(event.city)
            is MapScreenEvents.OnCategorySelected -> updateCategory(event.category)
            is MapScreenEvents.OnToggleRadarMode -> setRadarMode(event.isRadar)
            else -> { /* Delegated to UI or Repository */ }
        }
    }
}

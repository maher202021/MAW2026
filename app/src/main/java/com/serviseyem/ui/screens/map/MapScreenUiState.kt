package com.serviseyem.ui.screens.map

import com.serviseyem.data.PropertyEntity
import com.serviseyem.ui.*
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.components.MarkerRenderer

/**
 * 🗺️ MapScreenUiState
 * Comprehensive UI State model for Map and Radar screens.
 *
 * ⚠️ ملاحظة: هذا الملف جاهز للاستخدام المستقبلي عند الانتقال إلى MVI + ViewModel pattern.
 * حالياً، الحالة تُدار عبر MapScreenState (Compose-only state).
 * يمكن تفعيله لاحقاً عند توفر Hilt Injection.
 */
sealed class MapScreenUiState {
    object Loading : MapScreenUiState()
    
    data class Success(
        val providers: List<ProviderEntity> = emptyList(),
        val stores: List<StoreEntity> = emptyList(),
        val properties: List<PropertyEntity> = emptyList(),
        val radarPoints: List<MarkerRenderer.MapItemPoint> = emptyList(),
        val userLat: Double = 15.3694,
        val userLng: Double = 44.1910,
        // [تعديل] جعل الخريطة المباشرة OpenStreetMap هي العرض الافتراضي بدلاً من الرادار
        val isRadarMode: Boolean = false,
        val isHeatmapActive: Boolean = false,
        val selectedCategory: String = "ALL",
        val selectedCity: String = "الكل",
        val searchQuery: String = "",
        val selectedEntity: Any? = null,
        val bookingProviderTarget: ProviderEntity? = null
    ) : MapScreenUiState()

    data class Error(val message: String) : MapScreenUiState()
    
    object Empty : MapScreenUiState()
}

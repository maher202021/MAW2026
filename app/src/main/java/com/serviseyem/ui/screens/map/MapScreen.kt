@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.map
import com.serviseyem.ui.*

import androidx.compose.runtime.*
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.resolveThemePalette

/**
 * 🗺️ MapScreen
 * Modern, modular, high-performance Map Screen delegating to dedicated sub-modules:
 * - MapScreenContent (Primary presentation layout)
 * - MapScreenState (State container)
 * - MapScreenFilters (City & category filter logic)
 * - MapScreenUiState & MapScreenEvents (MVI architecture)
 * - RadarRenderer (Canvas radar with dual pulses & sweep animation)
 * - RealLeafletMapView (OSM Tiles + Clustering + Offline Cache)
 */
@Composable
fun MapScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit = {},
    onOpenProviderDetails: (ProviderEntity) -> Unit = {},
    onOpenStoreDetails: (StoreEntity) -> Unit = {},
    onOpenPropertyDetails: (PropertyEntity) -> Unit = {},
    onRequestBooking: (Any) -> Unit = {},
    themeColors: VisualThemePalette = resolveThemePalette(viewModel.settings.collectAsState().value)
) {
    val state = rememberMapScreenState()

    MapScreenContent(
        viewModel = viewModel,
        state = state,
        onBackClick = onBackClick,
        onOpenProviderDetails = onOpenProviderDetails,
        onOpenStoreDetails = onOpenStoreDetails,
        onOpenPropertyDetails = onOpenPropertyDetails,
        onRequestBooking = onRequestBooking,
        themeColors = themeColors
    )
}

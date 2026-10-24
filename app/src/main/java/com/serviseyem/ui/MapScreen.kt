package com.serviseyem.ui

import androidx.compose.runtime.Composable
import com.serviseyem.ui.*
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.MapScreen as ModularMapScreen
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.resolveThemePalette

/**
 * 🗺️ MapScreen (Root UI delegator to modular MapScreen)
 */
@Composable
fun MapScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit = {},
    onOpenProviderDetails: (ProviderEntity) -> Unit = {},
    onOpenStoreDetails: (StoreEntity) -> Unit = {},
    onOpenPropertyDetails: (PropertyEntity) -> Unit = {},
    onRequestBooking: (Any) -> Unit = {}
) {
    ModularMapScreen(
        viewModel = viewModel,
        onBackClick = onBackClick,
        onOpenProviderDetails = onOpenProviderDetails,
        onOpenStoreDetails = onOpenStoreDetails,
        onOpenPropertyDetails = onOpenPropertyDetails,
        onRequestBooking = onRequestBooking
    )
}

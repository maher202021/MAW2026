package com.serviseyem.ui.screens.map

import androidx.compose.runtime.Composable
import com.serviseyem.ui.*
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.MapScreen

/**
 * 🗺️ MapScreenLayout
 * واجهة تخطيط الخريطة التفاعلية التي تدمج شاشة الخريطة ونظام الرادار والـ Leaflet المباشر
 */
@Composable
fun MapScreenLayout(
    viewModel: MainViewModel,
    onBackClick: () -> Unit = {},
    onOpenProviderDetails: (ProviderEntity) -> Unit = {},
    onOpenStoreDetails: (StoreEntity) -> Unit = {},
    onOpenPropertyDetails: (PropertyEntity) -> Unit = {},
    onRequestBooking: (Any) -> Unit = {}
) {
    MapScreen(
        viewModel = viewModel,
        onBackClick = onBackClick,
        onOpenProviderDetails = onOpenProviderDetails,
        onOpenStoreDetails = onOpenStoreDetails,
        onOpenPropertyDetails = onOpenPropertyDetails,
        onRequestBooking = onRequestBooking
    )
}

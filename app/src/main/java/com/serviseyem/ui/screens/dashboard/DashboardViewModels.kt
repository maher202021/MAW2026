package com.serviseyem.ui.screens.dashboard

import com.serviseyem.ui.screens.dashboard.viewmodels.*
import com.serviseyem.ui.*

sealed class DashboardEvent {
    data class ShowToast(val message: String) : DashboardEvent()
    data class NavigateToDetail(val id: String) : DashboardEvent()
}

// Backward-compatibility exports & typealiases
typealias DashboardUiState = com.serviseyem.ui.screens.dashboard.viewmodels.DashboardUiState
typealias TechnicianDashboardViewModel = com.serviseyem.ui.screens.dashboard.viewmodels.TechnicianDashboardViewModel
typealias StoreDashboardViewModel = com.serviseyem.ui.screens.dashboard.viewmodels.StoreDashboardViewModel
typealias RestaurantDashboardViewModel = com.serviseyem.ui.screens.dashboard.viewmodels.RestaurantDashboardViewModel
typealias DoctorItem = com.serviseyem.domain.entities.DoctorItem
typealias MedicalDashboardViewModel = com.serviseyem.ui.screens.dashboard.viewmodels.MedicalDashboardViewModel
typealias PropertyDashboardViewModel = com.serviseyem.ui.screens.dashboard.viewmodels.PropertyDashboardViewModel
typealias JobPostItem = com.serviseyem.domain.entities.JobPostItem
typealias JobPosterDashboardViewModel = com.serviseyem.ui.screens.dashboard.viewmodels.JobPosterDashboardViewModel
typealias FavoritesUiState = com.serviseyem.ui.screens.dashboard.viewmodels.FavoritesUiState
typealias FavoritesViewModel = com.serviseyem.ui.screens.dashboard.viewmodels.FavoritesViewModel
typealias ServicesBrowserUiState = com.serviseyem.ui.screens.dashboard.viewmodels.ServicesBrowserUiState
typealias ServicesBrowserViewModel = com.serviseyem.ui.screens.dashboard.viewmodels.ServicesBrowserViewModel

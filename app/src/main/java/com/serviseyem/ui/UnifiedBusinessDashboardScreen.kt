package com.serviseyem.ui

import androidx.compose.runtime.Composable
import com.serviseyem.ui.*
import com.serviseyem.data.BusinessType
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.ui.screens.dashboard.JobPosterDashboard
import com.serviseyem.ui.screens.dashboard.MedicalDashboard
import com.serviseyem.ui.screens.dashboard.PropertyDashboard
import com.serviseyem.ui.screens.dashboard.RestaurantDashboard
import com.serviseyem.ui.screens.dashboard.StoreDashboard
import com.serviseyem.ui.screens.dashboard.TechnicianDashboard
import com.serviseyem.utils.VisualThemePalette

/**
 * 🏢 Clean Unified Delegation Entry Point for Dedicated SaaS Dashboards:
 * 1. Technicians & Service Providers (TechnicianDashboard)
 * 2. Stores & Malls (StoreDashboard)
 * 3. Restaurants & Cafes (RestaurantDashboard)
 * 4. Medical Centers & Clinics (MedicalDashboard)
 * 5. Job Advertisers & Recruiters (JobPosterDashboard)
 * 6. Real Estate Agencies (PropertyDashboard)
 */
@Composable
fun UnifiedBusinessDashboardScreen(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBackClick: () -> Unit
) {
    when (account.businessType) {
        BusinessType.TECHNICIAN -> TechnicianDashboard(
            account = account,
            viewModel = viewModel,
            themeColors = themeColors,
            onBackClick = onBackClick
        )
        BusinessType.JOB_POSTER -> JobPosterDashboard(
            account = account,
            viewModel = viewModel,
            themeColors = themeColors,
            onBackClick = onBackClick
        )
        BusinessType.REAL_ESTATE -> PropertyDashboard(
            account = account,
            viewModel = viewModel,
            themeColors = themeColors,
            onBackClick = onBackClick
        )
        BusinessType.MEDICAL -> MedicalDashboard(
            account = account,
            viewModel = viewModel,
            themeColors = themeColors,
            onBackClick = onBackClick
        )
        BusinessType.RESTAURANT -> RestaurantDashboard(
            account = account,
            viewModel = viewModel,
            themeColors = themeColors,
            onBackClick = onBackClick
        )
        BusinessType.STORE -> StoreDashboard(
            account = account,
            viewModel = viewModel,
            themeColors = themeColors,
            onBackClick = onBackClick
        )
    }
}

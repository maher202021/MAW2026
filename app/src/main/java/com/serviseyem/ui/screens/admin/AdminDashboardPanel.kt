package com.serviseyem.ui.screens.admin

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.viewmodels.AdminViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 📊 AdminDashboardPanel
 * لوحة المعلومات والإحصائيات الرئيسية ومؤشرات الأداء العامة
 */
@Composable
fun AdminDashboardPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    adminViewModel: AdminViewModel = viewModel.adminViewModel,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AdminAnalyticsPanel(
        onBack = onBack,
        adminViewModel = adminViewModel,
        modifier = modifier
    )
}

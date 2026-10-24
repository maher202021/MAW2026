package com.serviseyem.ui.screens.admin

import androidx.compose.runtime.Composable
import com.serviseyem.ui.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.viewmodels.AdminViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 👤 AdminUsersPanel
 * لوحة إدارة العملاء والمستخدمين وتعديل الصلاحيات والحظر
 */
@Composable
fun AdminUsersPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    adminViewModel: AdminViewModel = viewModel.adminViewModel,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AdminUserManager(
        onBack = onBack,
        adminViewModel = adminViewModel,
        mainViewModel = viewModel,
        themeColors = themeColors,
        modifier = modifier
    )
}

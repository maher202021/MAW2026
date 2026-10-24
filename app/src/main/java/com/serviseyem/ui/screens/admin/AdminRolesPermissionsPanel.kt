package com.serviseyem.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.admin.permissions.AdminPermissionsActionsSection
import com.serviseyem.ui.screens.admin.permissions.AdminPermissionsMatrixListSection
import com.serviseyem.ui.screens.admin.permissions.AdminPermissionsSaveBar
import com.serviseyem.ui.screens.admin.permissions.AdminPermissionsViewModel
import com.serviseyem.ui.screens.admin.permissions.AdminRoleSelectorSection
import com.serviseyem.utils.VisualThemePalette

/**
 * AdminRolesPermissionsPanel - Main Modular Router Composable
 * Coordinates the split components:
 * 1. AdminPermissionsActionsSection (Header, Bulk presets, Search & Filters)
 * 2. AdminRoleSelectorSection (Role picking)
 * 3. AdminPermissionsMatrixListSection (Full 538 permissions across 38 categories)
 * 4. AdminPermissionsSaveBar (Save and sync with Firestore)
 */
@Composable
fun AdminRolesPermissionsPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    permsViewModel: AdminPermissionsViewModel = viewModel()
) {
    val selectedRole by permsViewModel.selectedRole.collectAsState()
    val searchQuery by permsViewModel.searchQuery.collectAsState()
    val selectedLevelFilter by permsViewModel.selectedLevelFilter.collectAsState()
    val selectedCategoryFilter by permsViewModel.selectedCategoryFilter.collectAsState()
    val expandedCategories by permsViewModel.expandedCategories.collectAsState()
    val activePermissions by permsViewModel.activePermissions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Actions & Filters section
        AdminPermissionsActionsSection(
            viewModel = viewModel,
            permsViewModel = permsViewModel,
            themeColors = themeColors,
            selectedRole = selectedRole,
            activePermissionsCount = activePermissions.size,
            searchQuery = searchQuery,
            selectedLevelFilter = selectedLevelFilter,
            selectedCategoryFilter = selectedCategoryFilter
        )

        // 2. Role Selector section
        AdminRoleSelectorSection(
            selectedRole = selectedRole,
            onRoleSelected = { permsViewModel.selectRole(it) },
            themeColors = themeColors
        )

        // 3. Matrix of permissions section
        AdminPermissionsMatrixListSection(
            permsViewModel = permsViewModel,
            themeColors = themeColors,
            activePermissions = activePermissions,
            expandedCategories = expandedCategories,
            searchQuery = searchQuery,
            selectedLevelFilter = selectedLevelFilter,
            selectedCategoryFilter = selectedCategoryFilter
        )

        // 4. Save and sync bar
        AdminPermissionsSaveBar(
            viewModel = viewModel,
            themeColors = themeColors,
            selectedRole = selectedRole,
            activePermissions = activePermissions
        )
    }
}

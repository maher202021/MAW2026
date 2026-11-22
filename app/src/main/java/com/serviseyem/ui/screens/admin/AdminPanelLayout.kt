@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.serviseyem.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.serviseyem.ui.*
import com.serviseyem.utils.VisualThemePalette

// ✨ شاشة الإدارة الرئيسية
@Composable
fun AdminPanelLayout(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    AdminPanelLayoutContent(viewModel, themeColors)
}

@Composable
private fun AdminPanelLayoutContent(viewModel: MainViewModel, themeColors: VisualThemePalette) {
    val reports by viewModel.reports.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val supervisorPermissions by viewModel.supervisorPermissions.collectAsState()
    val adminPermissionsMatrix by viewModel.adminPermissionsMatrix.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val activatedProviders by viewModel.providers.collectAsState()
    val deletedList by viewModel.deletedProviders.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val chatChannels by viewModel.chatChannels.collectAsState()
    val settingsState by viewModel.settings.collectAsState()
    val bannersList by viewModel.banners.collectAsState()
    val supervisorsList by viewModel.supervisors.collectAsState()
    val colorPalettesList by viewModel.colorPalettes.collectAsState()
    val citiesList by viewModel.cities.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val passwordRecoveryRequests by viewModel.passwordRecoveryRequests.collectAsState()
    val callsLog by viewModel.callsLog.collectAsState()
    val couponsList by viewModel.coupons.collectAsState()
    val internalWallets by viewModel.internalWallets.collectAsState()
    val paymentWallets by viewModel.paymentWallets.collectAsState()
    val paymentsList by viewModel.payments.collectAsState()

    val context = LocalContext.current
    val state = rememberAdminPanelState(adminRole = adminRole, settingsState = settingsState)

    // ✨ تسجيل دخول الأدمن / المالك / المشرف الآمن عبر Firebase Auth + Custom Claims
    if (!state.isAuthorized) {
        AdminSecureLoginScreen(
            viewModel = viewModel,
            themeColors = themeColors,
            context = context,
            state = state
        )
        return
    }

    val customTabsListState by viewModel.customProfileTabs.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AdminDashboardTopBar(
                viewModel = viewModel,
                themeColors = themeColors,
                context = context,
                state = state
            )
        }

        item {
            AdminNavigationTabsBar(
                adminRole = adminRole,
                supervisorPermissions = supervisorPermissions,
                adminPermissionsMatrix = adminPermissionsMatrix,
                themeColors = themeColors,
                state = state
            )
        }

        // ------------------ MODULAR SUB-SCREENS RENDERING ------------------

        adminCustomizationSections(
            activeSubTab = state.activeSubTab,
            viewModel = viewModel,
            themeColors = themeColors,
            context = context,
            state = state,
            settingsState = settingsState,
            customTabsListState = customTabsListState
        )

        adminEntitiesAndCreationSections(
            activeSubTab = state.activeSubTab,
            viewModel = viewModel,
            themeColors = themeColors,
            context = context,
            state = state,
            categories = categories,
            citiesList = citiesList,
            activatedProviders = activatedProviders
        )

        adminOperationsSections(
            activeSubTab = state.activeSubTab,
            viewModel = viewModel,
            themeColors = themeColors,
            context = context,
            state = state,
            settingsState = settingsState,
            bookings = bookings,
            notifications = notifications,
            activatedProviders = activatedProviders,
            supervisorsList = supervisorsList
        )

        adminCommsAndEngagementSections(
            activeSubTab = state.activeSubTab,
            viewModel = viewModel,
            themeColors = themeColors,
            context = context,
            state = state,
            settingsState = settingsState,
            chatChannels = chatChannels,
            bannersList = bannersList,
            categories = categories,
            activatedProviders = activatedProviders,
            callsLog = callsLog,
            couponsList = couponsList
        )

        adminPaymentsAndWalletsSection(
            activeSubTab = state.activeSubTab,
            viewModel = viewModel,
            themeColors = themeColors,
            state = state,
            settingsState = settingsState,
            internalWallets = internalWallets,
            paymentWallets = paymentWallets,
            paymentsList = paymentsList
        )

        adminSecurityAndSystemSections(
            activeSubTab = state.activeSubTab,
            adminRole = adminRole,
            viewModel = viewModel,
            themeColors = themeColors,
            context = context,
            state = state,
            settingsState = settingsState,
            activatedProviders = activatedProviders,
            deletedList = deletedList,
            stores = stores,
            properties = properties,
            jobs = jobs,
            categories = categories,
            bookings = bookings,
            reports = reports,
            supervisorsList = supervisorsList,
            colorPalettesList = colorPalettesList,
            passwordRecoveryRequests = passwordRecoveryRequests
        )
    }

    // ------------------ POPUP CONFIRMATION CONTEXT DIALOGS ------------------

    AdminPanelDialogs(
        viewModel = viewModel,
        themeColors = themeColors,
        context = context,
        state = state,
        categories = categories
    )
}

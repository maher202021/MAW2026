package com.serviseyem.ui.screens.register.status

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.serviseyem.data.ChatChannelEntity
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.ui.*
import com.serviseyem.ui.screens.dashboard.ClientPersonalAccountDashboard
import com.serviseyem.ui.screens.dashboard.JobPosterDashboard
import com.serviseyem.ui.screens.dashboard.MedicalDashboard
import com.serviseyem.ui.screens.dashboard.PropertyDashboard
import com.serviseyem.ui.screens.dashboard.RestaurantDashboard
import com.serviseyem.ui.screens.dashboard.StoreDashboard
import com.serviseyem.ui.screens.dashboard.TechnicianDashboard
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.CoroutineScope

/**
 * JoinStatusRouter - موجه الشاشات واللوحات بناءً على حالة الطلب
 */
object JoinStatusRouter {

    @Composable
    fun RouteToDashboard(
        status: JoinStatus,
        viewModel: MainViewModel,
        themeColors: VisualThemePalette,
        context: Context,
        @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
        @Suppress("UNUSED_PARAMETER") snackbarHostState: SnackbarHostState,
        @Suppress("UNUSED_PARAMETER") onOpenChat: (ChatChannelEntity) -> Unit
    ) {
        when (status) {
            is JoinStatus.ActiveStore -> {
                val acc = UnifiedBusinessAccount.fromStore(status.store, status.businessType)
                when (status.businessType) {
                    "restaurants" -> RestaurantDashboard(
                        account = acc,
                        viewModel = viewModel,
                        themeColors = themeColors,
                        onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                    )
                    "medical" -> MedicalDashboard(
                        account = acc,
                        viewModel = viewModel,
                        themeColors = themeColors,
                        onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                    )
                    else -> StoreDashboard(
                        account = acc,
                        viewModel = viewModel,
                        themeColors = themeColors,
                        onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                    )
                }
            }
            is JoinStatus.ActiveProperty -> {
                val acc = UnifiedBusinessAccount.fromProperty(status.property)
                PropertyDashboard(
                    account = acc,
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                )
            }
            is JoinStatus.ApprovedTechnician -> {
                val acc = UnifiedBusinessAccount.fromProvider(status.provider)
                TechnicianDashboard(
                    account = acc,
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                )
            }
            is JoinStatus.ActiveJobPoster -> {
                val acc = UnifiedBusinessAccount.fromJob(status.job)
                JobPosterDashboard(
                    account = acc,
                    viewModel = viewModel,
                    themeColors = themeColors,
                    onBackClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) }
                )
            }
            is JoinStatus.ActiveJobSeeker -> {
                val phone = (status.userMap["phone"] as? String) ?: ""
                val name = (status.userMap["name"] as? String) ?: "باحث عن عمل"
                val residence = (status.userMap["residence"] as? String)
                    ?: (status.userMap["city"] as? String)
                    ?: "اليمن"
                val id = (status.userMap["id"] as? String) ?: "js_$phone"
                val bookings by viewModel.bookings.collectAsState()
                ClientPersonalAccountDashboard(
                    viewModel = viewModel,
                    themeColors = themeColors,
                    context = context,
                    currentUserName = name,
                    currentUserPhone = phone,
                    currentUserResidence = residence,
                    currentUserId = id,
                    bookings = bookings,
                    onShowRegistrationFormsAnyway = { viewModel.cancelOrResetJoinRequest(context) },
                    onNavigateToSupportChat = { viewModel.openSupportChat() }
                )
            }
            is JoinStatus.ActiveClient -> {
                val clientPhone = (status.userMap["phone"] as? String) ?: ""
                val clientName = (status.userMap["name"] as? String) ?: "العميل"
                val clientResidence = (status.userMap["residence"] as? String) ?: "اليمن"
                val clientId = (status.userMap["id"] as? String) ?: clientPhone
                val bookings by viewModel.bookings.collectAsState()
                ClientPersonalAccountDashboard(
                    viewModel = viewModel,
                    themeColors = themeColors,
                    context = context,
                    currentUserName = clientName,
                    currentUserPhone = clientPhone,
                    currentUserResidence = clientResidence,
                    currentUserId = clientId,
                    bookings = bookings,
                    onShowRegistrationFormsAnyway = { viewModel.cancelOrResetJoinRequest(context) },
                    onNavigateToSupportChat = { viewModel.openSupportChat() }
                )
            }
            else -> {
                // Handled in Pending / Rejected views
            }
        }
    }
}

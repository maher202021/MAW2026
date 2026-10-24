package com.serviseyem.ui.screens.notifications

import androidx.compose.animation.*
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.NotificationEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.notifications.components.NotificationEmptyState
import com.serviseyem.ui.screens.notifications.components.NotificationsFilterBar
import com.serviseyem.ui.screens.notifications.components.NotificationsList
import com.serviseyem.utils.VisualThemePalette

/**
 * 🔔 NotificationsScreen
 * Full-screen or modal host for user notifications management with tabs, mark-all-read, and fast filtering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onNavigateBack: () -> Unit,
    onNotificationClick: (NotificationEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val notifViewModel = viewModel.notificationViewModel

    val allNotifications by notifViewModel.notifications.collectAsState()
    val readIds by notifViewModel.readNotificationIds.collectAsState()
    val activeTab by notifViewModel.activeTab.collectAsState()
    val userPhone by notifViewModel.currentUserPhone.collectAsState()
    val userId by notifViewModel.currentUserId.collectAsState()
    val adminRole by notifViewModel.adminRole.collectAsState()

    LaunchedEffect(Unit) {
        notifViewModel.loadReadNotifications(context)
    }

    // Unified Audience Filtering
    val audienceFilteredList = remember(allNotifications, userPhone, userId, adminRole) {
        notifViewModel.filterAudienceNotifications(allNotifications, userPhone, userId, adminRole)
    }

    // Filter list
    val filteredList = remember(audienceFilteredList, readIds, activeTab) {
        when (activeTab) {
            "UNREAD" -> audienceFilteredList.filter { !readIds.contains(it.id) }
            "BOOKING" -> audienceFilteredList.filter { it.notificationType == "BOOKING" || it.title.contains("حجز") }
            "MESSAGE" -> audienceFilteredList.filter { it.notificationType == "MESSAGE" || it.title.contains("رسالة") || it.title.contains("دردشة") }
            "SPECIAL_OFFER" -> audienceFilteredList.filter { it.notificationType == "SPECIAL_OFFER" || it.title.contains("عرض") }
            "SYSTEM" -> audienceFilteredList.filter { it.notificationType == "SYSTEM" || it.notificationType == "ADMIN" }
            else -> audienceFilteredList
        }
    }

    // Counts Map
    val countsMap = remember(audienceFilteredList, readIds) {
        mapOf(
            "ALL" to audienceFilteredList.size,
            "UNREAD" to audienceFilteredList.count { !readIds.contains(it.id) },
            "BOOKING" to audienceFilteredList.count { it.notificationType == "BOOKING" || it.title.contains("حجز") },
            "MESSAGE" to audienceFilteredList.count { it.notificationType == "MESSAGE" || it.title.contains("رسالة") },
            "SPECIAL_OFFER" to audienceFilteredList.count { it.notificationType == "SPECIAL_OFFER" || it.title.contains("عرض") },
            "SYSTEM" to audienceFilteredList.count { it.notificationType == "SYSTEM" || it.notificationType == "ADMIN" }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "مركز الإشعارات",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (allNotifications.isNotEmpty()) {
                        IconButton(
                            onClick = { notifViewModel.markAllAsRead(context, allNotifications) }
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "تحديد الكل كمقروء",
                                tint = Color(0xFF00E5FF)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Bar
            NotificationsFilterBar(
                selectedCategory = activeTab,
                onSelectCategory = { notifViewModel.setActiveTab(it) },
                countsMap = countsMap,
                themeColors = themeColors
            )

            // Content Area
            if (filteredList.isEmpty()) {
                NotificationEmptyState(
                    activeTab = activeTab,
                    themeColors = themeColors,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                NotificationsList(
                    notifications = filteredList,
                    readIds = readIds,
                    onNotificationClick = { notif ->
                        notifViewModel.markNotificationAsRead(context, notif.id)
                        // توجيه النقر للإشعارات في الشاشة الكاملة حسب نوع الإشعار
                        val isMessage = notif.notificationType == "MESSAGE" || notif.title.contains("دردشة") || notif.message.contains("دردشة") || notif.title.contains("رسالة") || notif.message.contains("رسالة")
                        val isBooking = notif.notificationType == "BOOKING" || notif.title.contains("حجز") || notif.message.contains("حجز")
                        val isUrgentOrOffer = notif.notificationType == "SPECIAL_OFFER" || notif.title.contains("عاجل") || notif.message.contains("عاجل") || notif.title.contains("عرض") || notif.message.contains("عرض")

                        when {
                            isMessage -> {
                                val targetUserId = notif.targetValue.ifBlank { notif.senderId.ifBlank { "support" } }
                                viewModel.openDirectChat(
                                    targetUserId = targetUserId,
                                    targetUserName = notif.title.ifBlank { "محادثة" },
                                    relatedEntityId = notif.relatedRequestId.ifBlank { null },
                                    relatedEntityType = "REQUEST"
                                )
                            }
                            isBooking -> {
                                viewModel.navigateToScreen(AppScreens.BOOKINGS_VIEW)
                            }
                            isUrgentOrOffer -> {
                                viewModel.navigateToScreen(AppScreens.INSTANT_REQUESTS_VIEW)
                            }
                            else -> {
                                onNotificationClick(notif)
                            }
                        }
                    },
                    onDeleteNotification = { notif ->
                        notifViewModel.deleteNotification(notif.id)
                    },
                    themeColors = themeColors
                )
            }
        }
    }
}

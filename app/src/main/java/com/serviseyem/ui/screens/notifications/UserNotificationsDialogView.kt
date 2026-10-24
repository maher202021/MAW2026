@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.serviseyem.ui.screens.notifications
import com.serviseyem.ui.*

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.NotificationEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.ConfirmationDialog
import com.serviseyem.ui.screens.notifications.components.NotificationEmptyState
import com.serviseyem.ui.screens.notifications.components.NotificationFilterTabs
import com.serviseyem.ui.screens.notifications.components.NotificationItemCard
import com.serviseyem.ui.screens.notifications.components.NotificationLoadingState
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch

/**
 * 🔔 UserNotificationsBottomSheet
 * Modern Material 3 Modal Bottom Sheet with glassy surface, derivedStateOf filtering,
 * batch read operations, snackbars, and category chips.
 */
@Composable
fun UserNotificationsContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val notifViewModel = viewModel.notificationViewModel

    // Flow Collections via NotificationViewModel
    val allNotifications by notifViewModel.notifications.collectAsState()
    val userPhone by notifViewModel.currentUserPhone.collectAsState()
    val userId by notifViewModel.currentUserId.collectAsState()
    val adminRole by notifViewModel.adminRole.collectAsState()
    val readIds by notifViewModel.readNotificationIds.collectAsState()
    val bookings by viewModel.bookings.collectAsState()

    val activeTab by notifViewModel.activeTab.collectAsState()
    val selectedTypeFilter by notifViewModel.selectedTypeFilter.collectAsState()
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var selectedBookingForDetails by remember { mutableStateOf<com.serviseyem.data.BookingEntity?>(null) }
    var selectedGenericNotifForDetails by remember { mutableStateOf<NotificationEntity?>(null) }

    LaunchedEffect(Unit) {
        notifViewModel.loadReadNotifications(context)
    }

    // High performance filtering using derivedStateOf
    val validAndFilteredNotifs by remember {
        derivedStateOf {
            notifViewModel.filterAudienceNotifications(allNotifications, userPhone, userId, adminRole)
        }
    }

    val unreadCount by remember {
        derivedStateOf {
            validAndFilteredNotifs.count { !readIds.contains(it.id) }
        }
    }

    val finalNotifs by remember {
        derivedStateOf {
            validAndFilteredNotifs.filter { notif ->
                val matchesTab = when (activeTab) {
                    "READ" -> readIds.contains(notif.id)
                    "UNREAD" -> !readIds.contains(notif.id)
                    "IMPORTANT" -> notif.notificationType == "BOOKING" || notif.notificationType == "ADMIN" || notif.title.contains("عاجل") || notif.title.contains("مهم")
                    else -> true
                }
                val matchesType = when (selectedTypeFilter) {
                    "BOOKING" -> notif.notificationType == "BOOKING" || notif.title.contains("حجز")
                    "MESSAGE" -> notif.notificationType == "MESSAGE" || notif.title.contains("دردشة") || notif.title.contains("رسالة")
                    "SPECIAL_OFFER" -> notif.notificationType == "SPECIAL_OFFER" || notif.title.contains("عرض")
                    "SYSTEM" -> notif.notificationType == "SYSTEM" || notif.notificationType == "ADMIN"
                    else -> true
                }
                matchesTab && matchesType
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع إلى الشاشة الرئيسية",
                            tint = Color.White
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🔔", fontSize = 18.sp)
                            Text(
                                "مركز الإشعارات والتنبيهات",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        if (unreadCount > 0) {
                            Text(
                                "$unreadCount إشعار غير مقروء",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                "جميع الإشعارات مقروءة ومحدثة",
                                fontSize = 10.5.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Mark All as Read Button
                    if (unreadCount > 0) {
                        FilledTonalButton(
                            onClick = {
                                notifViewModel.markAllAsRead(context, validAndFilteredNotifs)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("تم تحديد جميع الإشعارات كمقروءة بنجاح ✓")
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = themeColors.accent.copy(alpha = 0.15f),
                                contentColor = themeColors.accent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("mark_all_read_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تحديد الكل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Clear All Button
                    if (validAndFilteredNotifs.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearAllConfirmDialog = true },
                            modifier = Modifier.testTag("clear_all_notifs_btn")
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "مسح الكل",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Tabs & Category Filter Chips
            val tabCounts = remember(validAndFilteredNotifs.size, unreadCount) {
                listOf(
                    Triple("ALL", "الكل", validAndFilteredNotifs.size),
                    Triple("UNREAD", "غير مقروءة", unreadCount),
                    Triple("IMPORTANT", "الهامة ⭐", validAndFilteredNotifs.count { it.notificationType == "BOOKING" || it.title.contains("عاجل") }),
                    Triple("READ", "مقروءة", validAndFilteredNotifs.size - unreadCount)
                )
            }

            NotificationFilterTabs(
                activeTab = activeTab,
                onTabSelected = { notifViewModel.setActiveTab(it) },
                tabCounts = tabCounts,
                selectedTypeFilter = selectedTypeFilter,
                onTypeFilterSelected = { notifViewModel.setSelectedTypeFilter(it) },
                themeColors = themeColors
            )

            // Main List or Empty State
            if (finalNotifs.isEmpty()) {
                NotificationEmptyState(
                    activeTab = activeTab,
                    themeColors = themeColors,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = finalNotifs,
                        key = { it.id.ifBlank { "${it.title}_${it.timestamp}" } }
                    ) { notif ->
                        NotificationItemCard(
                            notification = notif,
                            isUnread = !readIds.contains(notif.id),
                            onCardClick = {
                                notifViewModel.markNotificationAsRead(context, notif.id)
                                // توجيه النقر على الإشعار حسب نوعه (رسالة، حجز، طلب عاجل، أو عام)
                                val isMessage = notif.notificationType == "MESSAGE" || notif.title.contains("دردشة") || notif.message.contains("دردشة") || notif.title.contains("رسالة") || notif.message.contains("رسالة")
                                val isBookingNotif = notif.notificationType == "BOOKING" || notif.title.contains("حجز") || notif.message.contains("حجز")
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
                                    isBookingNotif -> {
                                        val matchedBooking = bookings.find { b ->
                                            (b.bookingNumber.isNotBlank() && (notif.title.contains(b.bookingNumber) || notif.message.contains(b.bookingNumber))) ||
                                            (b.bookingCode.isNotBlank() && (notif.title.contains(b.bookingCode) || notif.message.contains(b.bookingCode))) ||
                                            (b.id.isNotBlank() && notif.message.contains(b.id.take(6)))
                                        } ?: bookings.firstOrNull { b ->
                                            val cPhone = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(b.clientPhone)
                                            val pPhone = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(b.customerPhone)
                                            val notifTarget = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(notif.targetValue)
                                            notifTarget.isNotEmpty() && (notifTarget == cPhone || notifTarget == pPhone)
                                        }
                                        if (matchedBooking != null) {
                                            selectedBookingForDetails = matchedBooking
                                        } else {
                                            viewModel.navigateToScreen(AppScreens.BOOKINGS_VIEW)
                                        }
                                    }
                                    isUrgentOrOffer -> {
                                        viewModel.navigateToScreen(AppScreens.INSTANT_REQUESTS_VIEW)
                                    }
                                    else -> {
                                        selectedGenericNotifForDetails = notif
                                    }
                                }
                            },
                            onDeleteClick = {
                                notifViewModel.deleteNotification(notif.id)
                                coroutineScope.launch {
                                    val res = snackbarHostState.showSnackbar(
                                        message = "تم حذف الإشعار",
                                        actionLabel = "تراجع"
                                    )
                                    if (res == SnackbarResult.ActionPerformed) {
                                        notifViewModel.addNotification(
                                            title = notif.title,
                                            message = notif.message,
                                            targetType = notif.targetType,
                                            targetValue = notif.targetValue,
                                            targetAudience = notif.targetAudience,
                                            targetRoles = notif.targetRoles,
                                            targetUserIds = notif.targetUserIds,
                                            notificationType = notif.notificationType
                                        )
                                    }
                                }
                            },
                            themeColors = themeColors
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Confirmation Dialog for Clearing All Notifications
    ConfirmationDialog(
        isOpen = showClearAllConfirmDialog,
        title = "مسح جميع الإشعارات",
        message = "هل أنت متأكد من رغبتك في مسح كافة الإشعارات الظاهرة؟ لا يمكن التراجع عن هذا الإجراء.",
        confirmLabel = "مسح الكل",
        cancelLabel = "إلغاء",
        isDestructive = true,
        onConfirm = {
            notifViewModel.deleteAllNotifications()
            coroutineScope.launch {
                snackbarHostState.showSnackbar("تم مسح جميع الإشعارات بنجاح 🗑️")
            }
        },
        onDismiss = { showClearAllConfirmDialog = false }
    )

    // Detailed Dialog for Selected Booking Notification
    selectedBookingForDetails?.let { booking ->
        AlertDialog(
            onDismissRequest = { selectedBookingForDetails = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📋", fontSize = 20.sp)
                    Text(
                        "تفاصيل الحجز #${booking.bookingNumber.ifEmpty { booking.id.take(8) }}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val formattedPrice = if (booking.totalAmount > 0) "${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(booking.totalAmount)} ريال يمني" else "غير محدد"
                    Text("الخدمة: ${booking.serviceType.ifBlank { booking.category }}", color = Color.LightGray, fontSize = 13.sp)
                    Text("العميل: ${booking.customerName.ifBlank { booking.clientName }} (${booking.clientPhone.ifBlank { booking.customerPhone }})", color = Color.LightGray, fontSize = 13.sp)
                    Text("الفني / المزود: ${booking.providerName}", color = Color.LightGray, fontSize = 13.sp)
                    Text("الموعد: ${booking.date.ifBlank { booking.dateString }} | ${booking.time.ifBlank { booking.timeString }}", color = Color(0xFF38BDF8), fontSize = 13.sp)
                    Text("سعر الخدمة: $formattedPrice", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    Text("حالة الحجز: ${booking.status}", color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedBookingForDetails = null
                        viewModel.navigateToScreen(AppScreens.BOOKINGS_VIEW)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("الانتقال لجدول الحجوزات 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBookingForDetails = null }) {
                    Text("إغلاق", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // Generic Notification Details Dialog
    selectedGenericNotifForDetails?.let { notif ->
        AlertDialog(
            onDismissRequest = { selectedGenericNotifForDetails = null },
            title = {
                Text(notif.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(notif.message, color = Color.LightGray, fontSize = 13.5.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("🕒 ${com.serviseyem.utils.NotificationDateFormatter.format(notif.timestamp)}", color = Color(0xFF64748B), fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedGenericNotifForDetails = null },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("حسناً", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

/**
 * 🔔 UserNotificationsBottomSheet
 */
@Composable
fun UserNotificationsBottomSheet(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A).copy(alpha = 0.95f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = themeColors.accent.copy(alpha = 0.6f))
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        UserNotificationsContent(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = onDismiss,
            modifier = Modifier.fillMaxHeight(0.88f)
        )
    }
}

/**
 * Compatibility wrapper for UserNotificationsDialogView
 */
@Composable
fun UserNotificationsDialogView(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    UserNotificationsBottomSheet(
        viewModel = viewModel,
        themeColors = themeColors,
        onDismiss = onDismiss
    )
}

/**
 * Fullscreen Route variant for notifications
 */
@Composable
fun UserNotificationsScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBack: () -> Unit
) {
    Surface(
        color = themeColors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        UserNotificationsContent(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = onBack
        )
    }
}

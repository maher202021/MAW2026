package com.serviseyem.ui.components

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

@Composable
fun AppHeaderBar(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onNotificationsClick: () -> Unit,
    onChatsClick: () -> Unit,
    chatReadTrigger: Int = 0,
    onMenuClick: () -> Unit
) {
    val settingsState by viewModel.settings.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val allNotifications by viewModel.notifications.collectAsState()
    val userPhoneState by viewModel.currentUserPhone.collectAsState()
    val adminRoleState by viewModel.adminRole.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()
    val chatChannels by viewModel.chatChannels.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isEn = currentLang == "en"

    val myProvider = providers.find { it.phone == currentUserPhone }

    val cleanUserId = currentUserId.trim()
    val cleanUserPhone = currentUserPhone.trim().replace(" ", "").replace("+", "")
    val isAdminUser = cleanUserId == "admin" || cleanUserId.startsWith("super_") || adminRoleState != "GUEST"

    val myChannels = remember(chatChannels, cleanUserId, cleanUserPhone, myProvider, isAdminUser) {
        if (isAdminUser) {
            chatChannels
        } else if (cleanUserId.isEmpty() && cleanUserPhone.isEmpty()) {
            emptyList()
        } else {
            chatChannels.filter { ch ->
                val isMySupport = ch.id.startsWith("support_") || (cleanUserId.isNotEmpty() && ch.participants.contains(cleanUserId))
                val isMyUser = cleanUserId.isNotEmpty() && ch.participants.contains(cleanUserId)
                val isMyProvider = myProvider != null && ch.participants.contains(myProvider.id)

                isMySupport || isMyUser || isMyProvider
            }
        }
    }

    val headerContext = LocalContext.current
    val headerSp = remember(headerContext) { headerContext.getSharedPreferences("yemen_service_prefs", android.content.Context.MODE_PRIVATE) }
    val readNotificationIds by viewModel.readNotificationIds.collectAsState()
    
    // Calculate unread notifications count
    val filteredNotifs = remember(allNotifications, userPhoneState, adminRoleState) {
        val cleanPhone = userPhoneState.trim().replace(" ", "").replace("+", "")
        allNotifications.filter { notif ->
            // Filter sensitive notifications (password recovery, private orders)
            val isSensitive = notif.title.contains("كلمة مرور") || notif.message.contains("كلمة المرور") || notif.title.contains("استعادة") || notif.title.contains("طلب عاجل")
            if (isSensitive) {
                val isAdmin = adminRoleState != "GUEST"
                val isMyTarget = cleanPhone.isNotEmpty() && notif.targetValue.contains(cleanPhone)
                if (!isAdmin && !isMyTarget) return@filter false
            }

            when (notif.targetType) {
                "ALL" -> notif.targetAudience != "ADMIN_ONLY"
                "USER", "PROVIDER" -> notif.targetValue.isEmpty() || (cleanPhone.isNotEmpty() && notif.targetValue.contains(cleanPhone))
                "SUPERVISOR", "ADMIN_ONLY" -> adminRoleState != "GUEST"
                else -> true
            }
        }
    }
    val unreadNotifCount = remember(filteredNotifs, readNotificationIds) {
        if (filteredNotifs.isEmpty()) 0 else filteredNotifs.count { it.id !in readNotificationIds }
    }

    // Calculate unread chats count (تفضيل unreadCount من ChatChannel أولاً ثم fallback لـ SharedPreferences)
    val unreadChatsCount = remember(myChannels, chatChannels, headerSp, chatReadTrigger) {
        if (myChannels.isEmpty()) 0 else myChannels.count { ch ->
            val channelUnread = ch.unreadCount[currentUserId] ?: 0
            if (channelUnread > 0) {
                true
            } else {
                val lastMsg = ch.messages.lastOrNull()
                if (lastMsg == null) {
                    false
                } else {
                    val isMe = lastMsg.senderId == currentUserId || (myProvider != null && lastMsg.senderId == myProvider.id)
                    val readTime = headerSp.getLong("chat_read_${ch.id}", 0L)
                    !isMe && lastMsg.timestamp > readTime
                }
            }
        }
    }

    val screenBackStack by viewModel.screenBackStack.collectAsState()
    val showBackButton = screenBackStack.size > 1 || currentScreen != "USER_BROWSE"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColors.primary)
            .statusBarsPadding()
            .testTag("app_header_bar")
    ) {
        // Row 1: Unified TopBar Header with Back Button (Controlled by Admin hideTopHeaderBar)
        if (!settingsState.hideTopHeaderBar) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = { viewModel.goBack() },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isEn) "Back" else "رجوع",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                val defaultTitle = if (isEn) "Yemen Services Directory" else "دليل خدمات اليمن"
                val customTitle = settingsState.customAppName.ifEmpty { settingsState.appName.ifEmpty { defaultTitle } }

                val titleText = when (currentScreen) {
                    "REGISTER_FORM" -> if (isEn) "Join & Register" else "الانضمام والتسجيل"
                    "JOIN_REQUEST_STATUS" -> if (isEn) "Request Status" else "حالة طلب الانضمام"
                    "ADMIN_PANEL" -> if (isEn) "Admin Panel" else "لوحة التحكم والإدارة"
                    "OWNER_PANEL" -> if (isEn) "Owner Backdoor" else "البوابة الخلفية"
                    "ABOUT_APP" -> if (isEn) "About App" else "عن التطبيق"
                    "BOOKINGS_VIEW" -> if (isEn) "Bookings & Orders" else "الحجوزات والطلبات"
                    "MAP_VIEW" -> if (isEn) "Services Map" else "خريطة الخدمات"
                    else -> customTitle
                }

                Text(
                    text = titleText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.weight(1f))

                // [ طلباتي 📋 ] Action Button in Header
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                            )
                        )
                        .clickable { viewModel.navigateTo("ORDERS_VIEW") }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("📋", fontSize = 10.sp)
                        val isProvider = viewModel.selectedProvider != null || viewModel.selectedStore != null || viewModel.selectedProperty != null
                        Text(
                            text = if (isEn) (if (isProvider) "Requests" else "My Requests") else (if (isProvider) "الطلبات" else "طلباتي"),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                val isRefreshingHeader by viewModel.isRefreshing.collectAsState()
                IconButton(
                    onClick = { viewModel.refreshData(showNotification = true) },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        .size(26.dp)
                ) {
                    if (isRefreshingHeader) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 1.5.dp,
                            modifier = Modifier.size(13.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = if (isEn) "Refresh" else "تحديث البيانات",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Row 2: Navigation Items (6 luxury icons: الرئيسية 🏠, الخرائط 🗺️, الانضمام 👤, الإشعارات 🔔, المحادثات ✉️)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF131D33))
                .border(BorderStroke(1.dp, Color(0xFF1E2E4E)), RoundedCornerShape(16.dp))
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 1. الرئيسية
            val isBrowse = currentScreen == AppScreens.USER_BROWSE || currentScreen == "MAIN_DASHBOARD" || currentScreen == "HOME"
            Luxury3DNavIcon(
                emojiIcon = settingsState.topHomeIcon.ifEmpty { "🏠" },
                vectorIcon = Icons.Default.Home,
                label = if (isEn) "Home" else "الرئيسية",
                isSelected = isBrowse,
                iconSizeDp = settingsState.navIconSizeDp,
                iconStyle = settingsState.topNavIconStyle,
                onClick = {
                    viewModel.navigateToScreen(AppScreens.USER_BROWSE)
                    viewModel.registerBackdoorInteraction()
                }
            )

            // 2. الخرائط
            if (settingsState.isMapFeatureEnabled) {
                val isMap = currentScreen == AppScreens.MAP_VIEW
                Luxury3DNavIcon(
                    emojiIcon = settingsState.topMapsIcon.ifEmpty { "🗺️" },
                    vectorIcon = Icons.Default.Place,
                    label = if (isEn) "Maps" else "الخرائط",
                    isSelected = isMap,
                    iconSizeDp = settingsState.navIconSizeDp,
                    iconStyle = settingsState.topNavIconStyle,
                    onClick = { viewModel.navigateToScreen(AppScreens.MAP_VIEW) }
                )
            }

            // 3. الانضمام
            val isJoin = currentScreen == AppScreens.REGISTER_FORM || currentScreen == AppScreens.JOIN_REQUEST_STATUS
            Luxury3DNavIcon(
                emojiIcon = settingsState.topJoinIcon.ifEmpty { "👤" },
                vectorIcon = Icons.Default.Person,
                label = if (isEn) "Join" else "الانضمام",
                isSelected = isJoin,
                iconSizeDp = settingsState.navIconSizeDp,
                iconStyle = settingsState.topNavIconStyle,
                onClick = { viewModel.navigateToScreen(AppScreens.REGISTER_FORM) }
            )

            // 4. الإشعارات
            val isNotifScreen = currentScreen == AppScreens.NOTIFICATIONS_VIEW
            Luxury3DNavIcon(
                emojiIcon = settingsState.topNotifIcon.ifEmpty { "🔔" },
                vectorIcon = Icons.Default.Notifications,
                label = if (isEn) "Alerts" else "الإشعارات",
                isSelected = isNotifScreen,
                badgeCount = unreadNotifCount,
                iconSizeDp = settingsState.navIconSizeDp,
                iconStyle = settingsState.topNavIconStyle,
                onClick = {
                    onNotificationsClick()
                }
            )

            // 5. المحادثات
            val isChatsScreen = currentScreen == AppScreens.CHAT_LIST || currentScreen == AppScreens.CHAT_DIRECT
            Luxury3DNavIcon(
                emojiIcon = settingsState.topChatsIcon.ifEmpty { "✉️" },
                vectorIcon = Icons.Default.Email,
                label = if (isEn) "Chats" else "المحادثات",
                isSelected = isChatsScreen,
                badgeCount = unreadChatsCount,
                iconSizeDp = settingsState.navIconSizeDp,
                iconStyle = settingsState.topNavIconStyle,
                onClick = { onChatsClick() }
            )
        }
    }
}

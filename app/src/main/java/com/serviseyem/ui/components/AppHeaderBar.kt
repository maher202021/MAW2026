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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_header_bar"),
        color = com.serviseyem.ui.theme.AppBackground
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Unified Header Bar with App Logo / Title & Action Buttons
            if (!settingsState.hideTopHeaderBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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

                    // 1. الجهة اليمنى في RTL (البداية): زر الرجوع عند التفرع + زر الإشعارات
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (showBackButton) {
                            IconButton(
                                onClick = { viewModel.goBack() },
                                modifier = Modifier.testTag("header_back_button")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(com.serviseyem.ui.theme.CardSurface, CircleShape)
                                        .border(1.dp, com.serviseyem.ui.theme.CardBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = if (isEn) "Back" else "رجوع",
                                        tint = com.serviseyem.ui.theme.TextLightMint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier.testTag("header_notifications_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = if (isEn) "Notifications" else "الإشعارات",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                if (unreadNotifCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 3.dp, y = (-2).dp)
                                            .size(8.dp)
                                            .background(Color(0xFFEF4444), CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    // 2. المنتصف: اسم التطبيق والعلم في Row وسطية بوزن 1f مع Ellipsis
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = titleText,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = com.serviseyem.ui.theme.CardSurface,
                            border = BorderStroke(1.dp, com.serviseyem.ui.theme.CardBorder)
                        ) {
                            Text(
                                text = "🇾🇪",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // 3. الجهة اليسرى في RTL (النهاية): زر البحث + طلباتي + تحديث
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val isOrdersActive = currentScreen == "ORDERS_VIEW"
                        Surface(
                            onClick = { viewModel.navigateTo("ORDERS_VIEW") },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isOrdersActive) com.serviseyem.ui.theme.DarkCyanPrimary else Color(0xFFFF7A00),
                            border = BorderStroke(1.dp, Color(0xFFEA580C)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .heightIn(min = 34.dp)
                                .wrapContentWidth()
                                .testTag("header_my_orders_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text("📋", fontSize = 11.sp)
                                Text(
                                    text = if (isEn) "Orders" else "طلباتي",
                                    fontSize = 11.5.sp,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            }
                        }

                        val isRefreshingHeader by viewModel.isRefreshing.collectAsState()
                        IconButton(
                            onClick = { viewModel.refreshData(showNotification = true) },
                            modifier = Modifier.testTag("header_refresh_button")
                        ) {
                            if (isRefreshingHeader) {
                                CircularProgressIndicator(
                                    color = com.serviseyem.ui.theme.DarkCyanPrimary,
                                    strokeWidth = 1.5.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = if (isEn) "Refresh" else "تحديث البيانات",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.navigateToScreen(AppScreens.UNIFIED_SEARCH) },
                            modifier = Modifier.testTag("header_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = if (isEn) "Search" else "بحث",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

        // Row 2: Clean 4-Element Navigation Bar (weight 1f each, 32dp icons, full Arabic text):
        // 1. الخرائط | 2. الانضمام | 3. الإشعارات (جرس + عداد) | 4. المحادثات (مغلف + عداد)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (settingsState.hideTopHeaderBar) Modifier.statusBarsPadding() else Modifier)
                .padding(horizontal = 6.dp, vertical = 3.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(com.serviseyem.ui.theme.CardSurface)
                .border(BorderStroke(1.dp, com.serviseyem.ui.theme.CardBorder), RoundedCornerShape(14.dp))
                .padding(vertical = 4.dp)
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. الخرائط (خريطة)
            val isMap = currentScreen == AppScreens.MAP_VIEW
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Luxury3DNavIcon(
                    emojiIcon = settingsState.topMapsIcon.ifEmpty { "🗺️" },
                    vectorIcon = Icons.Default.Place,
                    label = if (isEn) "Maps" else "الخرائط",
                    isSelected = isMap,
                    iconSizeDp = 32,
                    iconStyle = settingsState.topNavIconStyle,
                    onClick = { viewModel.navigateToScreen(AppScreens.MAP_VIEW) }
                )
            }

            // 2. الانضمام (شخص)
            val isJoin = currentScreen == AppScreens.REGISTER_FORM || currentScreen == AppScreens.JOIN_REQUEST_STATUS
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Luxury3DNavIcon(
                    emojiIcon = settingsState.topJoinIcon.ifEmpty { "👤" },
                    vectorIcon = Icons.Default.Person,
                    label = if (isEn) "Join" else "الانضمام",
                    isSelected = isJoin,
                    iconSizeDp = 32,
                    iconStyle = settingsState.topNavIconStyle,
                    onClick = { viewModel.navigateToScreen(AppScreens.REGISTER_FORM) }
                )
            }

            // 3. الإشعارات (جرس + نقطة حمراء نابضة)
            val isNotifScreen = currentScreen == AppScreens.NOTIFICATIONS_VIEW
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Luxury3DNavIcon(
                    emojiIcon = settingsState.topNotifIcon.ifEmpty { "🔔" },
                    vectorIcon = Icons.Default.Notifications,
                    label = if (isEn) "Alerts" else "الإشعارات",
                    isSelected = isNotifScreen,
                    badgeCount = unreadNotifCount,
                    iconSizeDp = 32,
                    iconStyle = settingsState.topNavIconStyle,
                    onClick = {
                        onNotificationsClick()
                    }
                )
            }

            // 4. المحادثات (مغلف + شارة عداد)
            val isChatsScreen = currentScreen == AppScreens.CHAT_LIST || currentScreen == AppScreens.CHAT_DIRECT
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Luxury3DNavIcon(
                    emojiIcon = settingsState.topChatsIcon.ifEmpty { "✉️" },
                    vectorIcon = Icons.Default.Email,
                    label = if (isEn) "Chats" else "المحادثات",
                    isSelected = isChatsScreen,
                    badgeCount = unreadChatsCount,
                    iconSizeDp = 32,
                    iconStyle = settingsState.topNavIconStyle,
                    onClick = { onChatsClick() }
                )
            }
        }
        }
    }
}

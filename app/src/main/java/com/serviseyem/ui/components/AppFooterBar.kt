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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 👑 AppFooterBar (الشريط السفلي المتكامل المكون من 5 أيقونات واضحة ومتوازنة)
 * الترتيب الصارم الموزع بالتساوي:
 * 1. عن التطبيق (About)
 * 2. الحجوزات (Bookings)
 * 3. الرئيسية / ASH (Home Button في المنتصف بارتفاع دائري مميز)
 * 4. اللغة (English / العربية)
 * 5. الإدارة (Admin / القفل)
 */
@Composable
fun AppFooterBar(viewModel: MainViewModel, themeColors: VisualThemePalette, onInfoClick: () -> Unit) {
    val settingsState by viewModel.settings.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isEn = currentLang == "en"

    val footerBgColor = remember(settingsState.footerBgColorHex) {
        try {
            if (settingsState.footerBgColorHex.isNotBlank() && settingsState.footerBgColorHex != "#0A0F0D") {
                Color(android.graphics.Color.parseColor(settingsState.footerBgColorHex.trim()))
            } else {
                Color(0xFF121A26)
            }
        } catch (_: Exception) {
            Color(0xFF121A26)
        }
    }

    val rawOrderTokens = remember(settingsState.footerItemsOrder) {
        val parsed = settingsState.footerItemsOrder
            .split(",")
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() }
        if (parsed.isEmpty()) listOf("INFO", "BOOKINGS", "TEXT", "LANG", "ADMIN") else parsed
    }

    val visibleTokens = remember(
        rawOrderTokens,
        settingsState.showInfoIcon,
        settingsState.showBookingsIcon,
        settingsState.showLangIcon,
        settingsState.showAdminIcon
    ) {
        val list = rawOrderTokens.filter { token ->
            when (token) {
                "INFO" -> settingsState.showInfoIcon
                "BOOKINGS" -> settingsState.showBookingsIcon
                "LANG" -> settingsState.showLangIcon
                "ADMIN" -> settingsState.showAdminIcon
                "TEXT" -> true
                else -> false
            }
        }.toMutableList()
        if (!list.contains("TEXT")) {
            list.add(list.size / 2, "TEXT")
        }
        list.toList()
    }

    val navIconSize = settingsState.navIconSizeDp.coerceIn(20, 36)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .testTag("app_footer_bar")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter),
            color = footerBgColor,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .heightIn(min = 68.dp)
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                visibleTokens.forEach { token ->
                    when (token) {
                        "INFO" -> {
                            val isAbout = currentScreen == AppScreens.ABOUT_APP
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Luxury3DNavIcon(
                                    emojiIcon = settingsState.bottomInfoIcon.ifBlank { "ℹ️" },
                                    vectorIcon = Icons.Default.Info,
                                    label = if (isEn) "About" else "عن التطبيق",
                                    isSelected = isAbout,
                                    iconSizeDp = navIconSize,
                                    iconStyle = settingsState.topNavIconStyle,
                                    forceWhiteText = false,
                                    onClick = { onInfoClick() }
                                )
                            }
                        }
                        "BOOKINGS" -> {
                            val isBookings = currentScreen == AppScreens.BOOKINGS_VIEW
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Luxury3DNavIcon(
                                    emojiIcon = settingsState.bottomBookingsIcon.ifBlank { "📅" },
                                    vectorIcon = Icons.Default.DateRange,
                                    label = if (isEn) "Bookings" else "الحجوزات",
                                    isSelected = isBookings,
                                    iconSizeDp = navIconSize,
                                    iconStyle = settingsState.topNavIconStyle,
                                    forceWhiteText = false,
                                    onClick = { viewModel.navigateTo("BOOKINGS_VIEW") }
                                )
                            }
                        }
                        "TEXT" -> {
                            // مساحة متوازنة مخصصة لزر ASH / الرئيسية في المنتصف
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        "LANG" -> {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Luxury3DNavIcon(
                                    emojiIcon = settingsState.bottomLangIcon.ifBlank { "🌐" },
                                    vectorIcon = null,
                                    label = if (isEn) "العربية" else "English",
                                    isSelected = false,
                                    iconSizeDp = navIconSize,
                                    iconStyle = settingsState.topNavIconStyle,
                                    forceWhiteText = false,
                                    onClick = {
                                        viewModel.switchLanguage()
                                        viewModel.triggerNotification(
                                            if (isEn) "تم التحويل إلى اللغة العربية 🇾🇪" else "Language switched to English 🌐"
                                        )
                                    }
                                )
                            }
                        }
                        "ADMIN" -> {
                            val isAdmin = currentScreen == AppScreens.ADMIN_PANEL
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Luxury3DNavIcon(
                                    emojiIcon = settingsState.bottomAdminIcon.ifBlank { "🔒" },
                                    vectorIcon = Icons.Default.Lock,
                                    label = if (isEn) "Admin" else "الإدارة",
                                    isSelected = isAdmin,
                                    iconSizeDp = navIconSize,
                                    iconStyle = settingsState.topNavIconStyle,
                                    forceWhiteText = false,
                                    onClick = { viewModel.navigateTo("ADMIN_PANEL") }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 🌟 زر ASH الأوسط العائم (الرئيسية)
        val isHome = currentScreen == AppScreens.USER_BROWSE || currentScreen == AppScreens.HOME
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .navigationBarsPadding()
                .offset(y = (-10).dp)
                .padding(bottom = 6.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) },
                shape = CircleShape,
                containerColor = Color(0xFFFFB300),
                contentColor = Color.Black,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
                modifier = Modifier
                    .size(52.dp)
                    .border(2.5.dp, footerBgColor, CircleShape)
                    .testTag("footer_ash_button")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = if (isEn) "Home" else "الرئيسية",
                        tint = Color.Black,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = "ASH",
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = if (settingsState.showFooterText && settingsState.footerMessage.isNotBlank()) {
                    settingsState.footerMessage
                } else {
                    if (isEn) "Home" else "الرئيسية"
                },
                fontSize = 10.5.sp,
                lineHeight = 15.sp,
                maxLines = 1,
                softWrap = true,
                fontWeight = FontWeight.Bold,
                color = if (isHome) Color(0xFFFFB300) else Color(0xFFC9D2E0),
                textAlign = TextAlign.Center
            )
        }
    }
}

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

import androidx.compose.material.icons.filled.Home

/**
 * 👑 AppFooterBar (الشريط السفلي الذهبي مع زر الرئيسية البارز في المنتصف)
 * الترتيب الصارم:
 * 1. الحماية (Protection / Admin)
 * 2. اللغة (Language Switcher)
 * 3. [الرئيسية] (Floating Home button in Center)
 * 4. الباقات (Packages / Membership)
 * 5. معلومات (Info / About)
 */
@Composable
fun AppFooterBar(viewModel: MainViewModel, themeColors: VisualThemePalette, onInfoClick: () -> Unit) {
    val settingsState by viewModel.settings.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isEn = currentLang == "en"

    val isHome = currentScreen == AppScreens.USER_BROWSE || currentScreen == AppScreens.HOME

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("app_footer_bar"),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main Footer Container Surface
        Surface(
            color = com.serviseyem.ui.theme.DarkLuxuryBackground,
            shadowElevation = 12.dp,
            border = BorderStroke(1.dp, com.serviseyem.ui.theme.CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // 1. الحماية (Protection / Admin)
                val isAdmin = currentScreen == AppScreens.ADMIN_PANEL
                Luxury3DNavIcon(
                    emojiIcon = settingsState.bottomAdminIcon.ifEmpty { "🛡️" },
                    vectorIcon = Icons.Default.Lock,
                    label = if (isEn) "Protection" else "الحماية",
                    isSelected = isAdmin,
                    iconSizeDp = settingsState.navIconSizeDp,
                    iconStyle = settingsState.topNavIconStyle,
                    forceWhiteText = true,
                    onClick = { viewModel.navigateTo("ADMIN_PANEL") }
                )

                // 2. اللغة (Language Switcher)
                Luxury3DNavIcon(
                    emojiIcon = settingsState.bottomLangIcon.ifEmpty { if (isEn) "🌐" else "EN" },
                    vectorIcon = null,
                    label = if (isEn) "اللغة" else "English",
                    isSelected = false,
                    iconSizeDp = settingsState.navIconSizeDp,
                    iconStyle = settingsState.topNavIconStyle,
                    forceWhiteText = true,
                    onClick = {
                        viewModel.switchLanguage()
                        viewModel.triggerNotification(
                            if (isEn) "تم التحويل إلى اللغة العربية 🇾🇪" else "Language switched to English 🌐"
                        )
                    }
                )

                // Spacer for the Center Floating Button (الرئيسية)
                Spacer(modifier = Modifier.width(56.dp))

                // 4. الباقات (Packages / Membership)
                val isBookings = currentScreen == AppScreens.BOOKINGS_VIEW
                Luxury3DNavIcon(
                    emojiIcon = settingsState.bottomBookingsIcon.ifEmpty { "📦" },
                    vectorIcon = Icons.Default.Star,
                    label = if (isEn) "Packages" else "الباقات",
                    isSelected = isBookings,
                    iconSizeDp = settingsState.navIconSizeDp,
                    iconStyle = settingsState.topNavIconStyle,
                    forceWhiteText = true,
                    onClick = { viewModel.navigateTo("BOOKINGS_VIEW") }
                )

                // 5. معلومات (Info / About)
                val isAbout = currentScreen == AppScreens.ABOUT_APP
                Luxury3DNavIcon(
                    emojiIcon = settingsState.bottomInfoIcon.ifEmpty { "ℹ️" },
                    vectorIcon = Icons.Default.Info,
                    label = if (isEn) "Info" else "معلومات",
                    isSelected = isAbout,
                    iconSizeDp = settingsState.navIconSizeDp,
                    iconStyle = settingsState.topNavIconStyle,
                    forceWhiteText = true,
                    onClick = { onInfoClick() }
                )
            }
        }

        // 3. Center Floating Home Button (الرئيسية) - Protruding 26.dp with Gold Gradient Glow
        Box(
            modifier = Modifier
                .offset(y = (-18).dp)
                .size(58.dp)
                .clip(CircleShape)
                .background(com.serviseyem.ui.theme.LuxuryGoldGradient)
                .border(2.5.dp, com.serviseyem.ui.theme.DarkLuxuryBackground, CircleShape)
                .clickable {
                    viewModel.navigateToScreen(AppScreens.USER_BROWSE)
                    viewModel.registerBackdoorInteraction()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = if (isEn) "Home" else "الرئيسية",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = if (isEn) "Home" else "الرئيسية",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }
    }
}


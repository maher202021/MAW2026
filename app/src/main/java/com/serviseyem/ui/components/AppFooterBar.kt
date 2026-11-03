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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 👑 AppFooterBar (الشريط السفلي الذهبي مع زر الطلبات البارز)
 * الترتيب الصارم:
 * 1. الحماية (Protection / Admin)
 * 2. اللغة (Language Switcher)
 * 3. [الطلبات] (Floating Orders button in Center - بارز بارتفاع 26.dp وتوهج ذهبي)
 * 4. الباقات (Packages / Membership)
 * 5. معلومات (Info / About)
 */
@Composable
fun AppFooterBar(viewModel: MainViewModel, themeColors: VisualThemePalette, onInfoClick: () -> Unit) {
    val settingsState by viewModel.settings.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isEn = currentLang == "en"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("app_footer_bar"),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main Footer Container Surface (Glassmorphism with rounded top corners 20dp)
        Surface(
            color = com.serviseyem.ui.theme.DarkLuxuryBackground.copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, com.serviseyem.ui.theme.CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 62.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // 1. الإدارة (قفل)
                val isAdmin = currentScreen == AppScreens.ADMIN_PANEL
                Luxury3DNavIcon(
                    emojiIcon = "🔒",
                    vectorIcon = Icons.Default.Lock,
                    label = if (isEn) "Admin" else "الإدارة",
                    isSelected = isAdmin,
                    iconSizeDp = settingsState.navIconSizeDp,
                    iconStyle = settingsState.topNavIconStyle,
                    forceWhiteText = true,
                    onClick = { viewModel.navigateTo("ADMIN_PANEL") }
                )

                // 2. English (كرة أرضية)
                Luxury3DNavIcon(
                    emojiIcon = "🌐",
                    vectorIcon = null,
                    label = if (isEn) "العربية" else "English",
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

                // Spacer for the Center Floating Button (ASH)
                Spacer(modifier = Modifier.width(54.dp))

                // 4. الحجوزات (تقويم)
                val isBookings = currentScreen == AppScreens.BOOKINGS_VIEW
                Luxury3DNavIcon(
                    emojiIcon = "📅",
                    vectorIcon = Icons.Default.DateRange,
                    label = if (isEn) "Bookings" else "الحجوزات",
                    isSelected = isBookings,
                    iconSizeDp = settingsState.navIconSizeDp,
                    iconStyle = settingsState.topNavIconStyle,
                    forceWhiteText = true,
                    onClick = { viewModel.navigateTo("BOOKINGS_VIEW") }
                )

                // 5. عن التطبيق (معلومات)
                val isAbout = currentScreen == AppScreens.ABOUT_APP
                Luxury3DNavIcon(
                    emojiIcon = "ℹ️",
                    vectorIcon = Icons.Default.Info,
                    label = if (isEn) "About" else "عن التطبيق",
                    isSelected = isAbout,
                    iconSizeDp = settingsState.navIconSizeDp,
                    iconStyle = settingsState.topNavIconStyle,
                    forceWhiteText = true,
                    onClick = { onInfoClick() }
                )
            }
        }

        // 3. ASH (الزر المركزي: دائري 54dp بارز يرتفع 26dp فوق الشريط بتدرج ذهبي #FFC93C→#FF9F1C وحلقة بلون الخلفية وتوهج ذهبي ناعم)
        Box(
            modifier = Modifier
                .offset(y = (-26).dp)
                .size(54.dp)
                .shadow(16.dp, CircleShape, ambientColor = Color(0xFFFFC93C), spotColor = Color(0xFFFF9F1C))
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFFC93C), Color(0xFFFF9F1C))
                    )
                )
                .border(3.dp, com.serviseyem.ui.theme.DarkLuxuryBackground, CircleShape)
                .clickable { viewModel.navigateToScreen(AppScreens.USER_BROWSE) },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "ASH",
                    tint = Color(0xFF1A1000),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "ASH",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1000)
                )
            }
        }
    }
}


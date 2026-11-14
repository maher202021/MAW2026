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
            .testTag("app_footer_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 70.dp)
                .background(Color(0xFF121A26))
                .align(Alignment.BottomCenter)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. الإدارة (قفل)
            val isAdmin = currentScreen == AppScreens.ADMIN_PANEL
            Luxury3DNavIcon(
                emojiIcon = "🔒",
                vectorIcon = Icons.Default.Lock,
                label = if (isEn) "Admin" else "الإدارة",
                isSelected = isAdmin,
                iconSizeDp = 30,
                iconStyle = settingsState.topNavIconStyle,
                forceWhiteText = false,
                onClick = { viewModel.navigateTo("ADMIN_PANEL") }
            )

            // 2. English / العربية
            Luxury3DNavIcon(
                emojiIcon = "🌐",
                vectorIcon = null,
                label = if (isEn) "العربية" else "English",
                isSelected = false,
                iconSizeDp = 30,
                iconStyle = settingsState.topNavIconStyle,
                forceWhiteText = false,
                onClick = {
                    viewModel.switchLanguage()
                    viewModel.triggerNotification(
                        if (isEn) "تم التحويل إلى اللغة العربية 🇾🇪" else "Language switched to English 🌐"
                    )
                }
            )

            // حجز مساحة متوازنة لزر ASH الأوسط العائم
            Spacer(modifier = Modifier.width(64.dp))

            // 3. الحجوزات (تقويم)
            val isBookings = currentScreen == AppScreens.BOOKINGS_VIEW
            Luxury3DNavIcon(
                emojiIcon = "📅",
                vectorIcon = Icons.Default.DateRange,
                label = if (isEn) "Bookings" else "الحجوزات",
                isSelected = isBookings,
                iconSizeDp = 30,
                iconStyle = settingsState.topNavIconStyle,
                forceWhiteText = false,
                onClick = { viewModel.navigateTo("BOOKINGS_VIEW") }
            )

            // 4. عن التطبيق (معلومات)
            val isAbout = currentScreen == AppScreens.ABOUT_APP
            Luxury3DNavIcon(
                emojiIcon = "ℹ️",
                vectorIcon = Icons.Default.Info,
                label = if (isEn) "About" else "عن التطبيق",
                isSelected = isAbout,
                iconSizeDp = 30,
                iconStyle = settingsState.topNavIconStyle,
                forceWhiteText = false,
                onClick = { onInfoClick() }
            )
        }

        // زر ASH الأوسط داخل Box بمحاذاة TopCenter وإزاحة رأسية سالبة 14dp
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
        ) {
            androidx.compose.material3.FloatingActionButton(
                onClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) },
                shape = CircleShape,
                containerColor = Color(0xFFFFB300),
                contentColor = Color.Black,
                modifier = Modifier
                    .size(56.dp)
                    .border(2.dp, com.serviseyem.ui.theme.DarkLuxuryBackground, CircleShape)
                    .testTag("footer_ash_button")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "ASH",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ASH",
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }
    }
}


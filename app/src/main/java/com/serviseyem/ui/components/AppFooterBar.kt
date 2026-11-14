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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("app_footer_bar")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter),
            color = Color(0xFF121A26),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. عن التطبيق (معلومات)
                val isAbout = currentScreen == AppScreens.ABOUT_APP
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Luxury3DNavIcon(
                        emojiIcon = "ℹ️",
                        vectorIcon = Icons.Default.Info,
                        label = if (isEn) "About" else "عن التطبيق",
                        isSelected = isAbout,
                        iconSizeDp = 28,
                        iconStyle = settingsState.topNavIconStyle,
                        forceWhiteText = false,
                        onClick = { onInfoClick() }
                    )
                }

                // 2. الحجوزات (تقويم)
                val isBookings = currentScreen == AppScreens.BOOKINGS_VIEW
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Luxury3DNavIcon(
                        emojiIcon = "📅",
                        vectorIcon = Icons.Default.DateRange,
                        label = if (isEn) "Bookings" else "الحجوزات",
                        isSelected = isBookings,
                        iconSizeDp = 28,
                        iconStyle = settingsState.topNavIconStyle,
                        forceWhiteText = false,
                        onClick = { viewModel.navigateTo("BOOKINGS_VIEW") }
                    )
                }

                // 3. مساحة متوازنة مخصصة لزر ASH / الرئيسية في المنتصف
                Spacer(modifier = Modifier.weight(1f))

                // 4. English / العربية
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Luxury3DNavIcon(
                        emojiIcon = "🌐",
                        vectorIcon = null,
                        label = if (isEn) "العربية" else "English",
                        isSelected = false,
                        iconSizeDp = 28,
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

                // 5. الإدارة (قفل)
                val isAdmin = currentScreen == AppScreens.ADMIN_PANEL
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Luxury3DNavIcon(
                        emojiIcon = "🔒",
                        vectorIcon = Icons.Default.Lock,
                        label = if (isEn) "Admin" else "الإدارة",
                        isSelected = isAdmin,
                        iconSizeDp = 28,
                        iconStyle = settingsState.topNavIconStyle,
                        forceWhiteText = false,
                        onClick = { viewModel.navigateTo("ADMIN_PANEL") }
                    )
                }
            }
        }

        // 🌟 زر ASH الأوسط العائم (الرئيسية)
        val isHome = currentScreen == AppScreens.USER_BROWSE || currentScreen == AppScreens.HOME
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-12).dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.navigateToScreen(AppScreens.USER_BROWSE) },
                shape = CircleShape,
                containerColor = Color(0xFFFFB300),
                contentColor = Color.Black,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
                modifier = Modifier
                    .size(52.dp)
                    .border(2.5.dp, Color(0xFF121A26), CircleShape)
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
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isEn) "Home" else "الرئيسية",
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHome) Color(0xFFFFB300) else Color(0xFFC9D2E0),
                textAlign = TextAlign.Center
            )
        }
    }
}

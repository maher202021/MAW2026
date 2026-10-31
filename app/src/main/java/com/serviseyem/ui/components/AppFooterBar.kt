package com.serviseyem.ui.components

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

@Composable
fun AppFooterBar(viewModel: MainViewModel, themeColors: VisualThemePalette, onInfoClick: () -> Unit) {
    val settingsState by viewModel.settings.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isEn = currentLang == "en"

    val footerBg = remember(settingsState.footerBgColorHex) {
        try {
            if (settingsState.footerBgColorHex.isNotBlank() && settingsState.footerBgColorHex != "default") {
                Color(android.graphics.Color.parseColor(settingsState.footerBgColorHex))
            } else {
                Color(0xFF063328) // Luxury deep emerald container
            }
        } catch (e: Exception) {
            Color(0xFF063328)
        }
    }

    Surface(
        color = footerBg,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFF0F5243), Color(0xFFD4AF37), Color(0xFF0F5243)))),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("app_footer_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val bookings by viewModel.bookings.collectAsState()
            val currentUserPhone by viewModel.currentUserPhone.collectAsState()
            val providers by viewModel.providers.collectAsState()

            val activeStatuses = remember { setOf("PENDING", "ACCEPTED", "APPROVED", "IN_PROGRESS", "STARTED") }
            val cleanUserPhone = remember(currentUserPhone) { com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(currentUserPhone) }

            val matchingProvider = remember(providers, cleanUserPhone) {
                providers.find { com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(it.phone) == cleanUserPhone && cleanUserPhone.isNotEmpty() }
            }

            val unreadCount = remember(bookings, cleanUserPhone, matchingProvider) {
                val custCount = bookings.count { b ->
                    com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(b.customerPhone) == cleanUserPhone && cleanUserPhone.isNotEmpty() && b.status.uppercase() in activeStatuses
                }
                val provCount = if (matchingProvider != null) {
                    bookings.count { b ->
                        b.providerId == matchingProvider.id && b.status.uppercase() in activeStatuses
                    }
                } else 0
                custCount + provCount
            }

            // 1. Info Icon ("عن التطبيق")
            if (settingsState.showInfoIcon) {
                val isAbout = currentScreen == AppScreens.ABOUT_APP
                Luxury3DNavIcon(
                    emojiIcon = settingsState.bottomInfoIcon.ifEmpty { "ℹ️" },
                    vectorIcon = Icons.Default.Info,
                    label = if (isEn) "About" else "عن التطبيق",
                    isSelected = isAbout,
                    iconSizeDp = settingsState.navIconSizeDp,
                    iconStyle = settingsState.topNavIconStyle,
                    forceWhiteText = true,
                    onClick = { onInfoClick() }
                )
            }

            // 2. Bookings Icon ("الحجوزات")
            if (settingsState.showBookingsIcon) {
                val isBookings = currentScreen == AppScreens.BOOKINGS_VIEW
                Luxury3DNavIcon(
                    emojiIcon = settingsState.bottomBookingsIcon.ifEmpty { "📅" },
                    vectorIcon = Icons.Default.DateRange,
                    label = if (isEn) "Bookings" else "الحجوزات",
                    isSelected = isBookings,
                    badgeCount = unreadCount,
                    iconSizeDp = settingsState.navIconSizeDp,
                    iconStyle = settingsState.topNavIconStyle,
                    forceWhiteText = true,
                    onClick = { viewModel.navigateTo("BOOKINGS_VIEW") }
                )
            }

            // 3. Center Brand Text ("ASH")
            if (settingsState.showFooterText) {
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val rawBrand = settingsState.footerMessage
                    val brandText = when {
                        rawBrand.startsWith("card_custom_") -> "ASH"
                        rawBrand.isBlank() || rawBrand == "WAM2026" -> "ASH"
                        else -> rawBrand
                    }
                    Text(
                        text = brandText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 4. Single Clean Language Switcher Icon ("EN" / "🌐")
            if (settingsState.showLangIcon) {
                Luxury3DNavIcon(
                    emojiIcon = settingsState.bottomLangIcon.ifEmpty { if (isEn) "🌐" else "EN" },
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
            }

            // 5. Admin Lock Icon ("الإدارة")
            if (settingsState.showAdminIcon) {
                val isAdmin = currentScreen == AppScreens.ADMIN_PANEL
                Luxury3DNavIcon(
                    emojiIcon = settingsState.bottomAdminIcon.ifEmpty { "🔒" },
                    vectorIcon = Icons.Default.Lock,
                    label = if (isEn) "Admin" else "الإدارة",
                    isSelected = isAdmin,
                    iconSizeDp = settingsState.navIconSizeDp,
                    iconStyle = settingsState.topNavIconStyle,
                    forceWhiteText = true,
                    onClick = { viewModel.navigateTo("ADMIN_PANEL") }
                )
            }
        }
    }
}

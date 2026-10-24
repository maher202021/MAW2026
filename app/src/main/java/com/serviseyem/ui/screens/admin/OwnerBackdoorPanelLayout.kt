@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.admin
import android.content.Intent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import okhttp3.MediaType.Companion.toMediaType

import com.serviseyem.*

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.serviseyem.data.*
import com.serviseyem.utils.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.*
import com.serviseyem.ui.dialogs.*
import com.serviseyem.ui.screens.home.*
import com.serviseyem.ui.screens.map.*
import com.serviseyem.ui.screens.bookings.*
import com.serviseyem.ui.screens.admin.*
import com.serviseyem.ui.screens.assistant.*
import com.serviseyem.ui.screens.register.*
import com.serviseyem.ui.screens.status.*
import com.serviseyem.ui.screens.about.*
import com.serviseyem.ui.*
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
/* AdminPanelLayout has been moved to com.serviseyem.ui.screens.admin.AdminPanelLayout */
fun OwnerBackdoorPanelLayout(viewModel: MainViewModel, themeColors: VisualThemePalette) {
    val settingsState by viewModel.settings.collectAsState()

    var appName by remember { mutableStateOf(settingsState.appName) }
    var welcomeMessage by remember { mutableStateOf(settingsState.welcomeMessage) }
    var footerMessage by remember { mutableStateOf(settingsState.footerMessage) }
    var footerBgColorHex by remember { mutableStateOf(settingsState.footerBgColorHex) }
    var footerItemsOrder by remember { mutableStateOf(settingsState.footerItemsOrder) }
    var showInfoIcon by remember { mutableStateOf(settingsState.showInfoIcon) }
    var showBookingsIcon by remember { mutableStateOf(settingsState.showBookingsIcon) }
    var showLangIcon by remember { mutableStateOf(settingsState.showLangIcon) }
    var showAdminIcon by remember { mutableStateOf(settingsState.showAdminIcon) }
    var showFooterText by remember { mutableStateOf(settingsState.showFooterText) }
    var infoIconType by remember { mutableStateOf(settingsState.infoIconType) }
    var adminIconType by remember { mutableStateOf(settingsState.adminIconType) }
    var langIconType by remember { mutableStateOf(settingsState.langIconType) }
    var bookingsIconType by remember { mutableStateOf(settingsState.bookingsIconType) }
    var appVersion by remember { mutableStateOf(settingsState.appVersion) }
    var supportPhone by remember { mutableStateOf(settingsState.supportPhone) }
    var supportEmail by remember { mutableStateOf(settingsState.supportEmail) }
    var supportWhatsapp by remember { mutableStateOf(settingsState.supportWhatsapp) }
    var appDownloadUrl by remember { mutableStateOf(settingsState.appDownloadUrl) }
    var activeThemeId by remember { mutableStateOf(settingsState.activeThemeId) }
    var isMaintenanceActive by remember { mutableStateOf(settingsState.isMaintenanceActive) }
    var hidePromoFooter by remember { mutableStateOf(settingsState.hidePromoFooter) }
    var assistantHidden by remember { mutableStateOf(settingsState.assistantHidden) }
    var assistantSize by remember { mutableStateOf(56f) }
    var chatHidden by remember { mutableStateOf(settingsState.chatHidden) }
    var chatSize by remember { mutableStateOf(if (settingsState.chatSize > 0) settingsState.chatSize.toFloat() else 56f) }
    var maxSearchRadiusKm by remember { mutableStateOf(settingsState.maxSearchRadiusKm.toFloat()) }
    var isSpeechSearchEnabled by remember { mutableStateOf(settingsState.isSpeechSearchEnabled) }
    var isDataSaverEnabled by remember { mutableStateOf(false) }
    var appImageQuality by remember { mutableStateOf(90f) }
    var bypassVisitorRegistration by remember { mutableStateOf(settingsState.bypassVisitorRegistration) }
    var isUserPasswordRequired by remember { mutableStateOf(settingsState.isUserPasswordRequired) }
    var disableChatFirewall by remember { mutableStateOf(settingsState.disableChatFirewall) }
    var disableBookingFirewall by remember { mutableStateOf(settingsState.disableBookingFirewall) }
    var isMapFeatureEnabled by remember { mutableStateOf(settingsState.isMapFeatureEnabled) }
    var isStoresEnabled by remember { mutableStateOf(settingsState.isStoresEnabled) }
    var isPropertiesEnabled by remember { mutableStateOf(settingsState.isPropertiesEnabled) }
    var enableProvidersRegistration by remember { mutableStateOf(settingsState.enableProvidersRegistration) }
    var enableStoresRegistration by remember { mutableStateOf(settingsState.enableStoresRegistration) }
    var enableRestaurantsRegistration by remember { mutableStateOf(settingsState.enableRestaurantsRegistration) }
    var enablePropertiesRegistration by remember { mutableStateOf(settingsState.enablePropertiesRegistration) }
    var enableMedicalRegistration by remember { mutableStateOf(settingsState.enableMedicalRegistration) }
    var enableJobsRegistration by remember { mutableStateOf(settingsState.enableJobsRegistration) }
    var showRefreshIcon by remember { mutableStateOf(settingsState.showRefreshIcon) }
    var showSettingsIcon by remember { mutableStateOf(settingsState.showSettingsIcon) }
    var headerIconsOrder by remember { mutableStateOf(settingsState.headerIconsOrder) }
    var categoriesLayoutTypeState by remember { mutableStateOf(settingsState.categoriesLayoutType) }
    var websiteUrl by remember { mutableStateOf(settingsState.websiteUrl) }
    var telegramUrl by remember { mutableStateOf(settingsState.telegramUrl) }
    var facebookUrl by remember { mutableStateOf(settingsState.facebookUrl) }
    var twitterUrl by remember { mutableStateOf(settingsState.twitterUrl) }
    var instagramUrl by remember { mutableStateOf(settingsState.instagramUrl) }
    var youtubeUrl by remember { mutableStateOf(settingsState.youtubeUrl) }
    var aboutLayoutOrder by remember { mutableStateOf(settingsState.aboutLayoutOrder) }
    var hideTwitter by remember { mutableStateOf(settingsState.hideTwitter) }
    var hideInstagram by remember { mutableStateOf(settingsState.hideInstagram) }
    var hideYoutube by remember { mutableStateOf(settingsState.hideYoutube) }
    var hideFacebook by remember { mutableStateOf(settingsState.hideFacebook) }
    var hideTelegram by remember { mutableStateOf(settingsState.hideTelegram) }
    var hideWebsite by remember { mutableStateOf(settingsState.hideWebsite) }

    // Booking form full control states
    var bookingTermsInput by remember { mutableStateOf(settingsState.bookingTerms) }
    var bookingLabelNameInput by remember { mutableStateOf(settingsState.bookingLabelName) }
    var bookingLabelPhoneInput by remember { mutableStateOf(settingsState.bookingLabelPhone) }
    var bookingLabelAreaInput by remember { mutableStateOf(settingsState.bookingLabelArea) }
    var bookingLabelServiceInput by remember { mutableStateOf(settingsState.bookingLabelService) }

    // About App customization states
    var aboutCoverType by remember { mutableStateOf(settingsState.aboutCoverType) }
    var aboutCoverContent by remember { mutableStateOf(settingsState.aboutCoverContent) }
    var aboutCoverBase64 by remember { mutableStateOf(settingsState.aboutCoverBase64) }
    var aboutCustomInfo by remember { mutableStateOf(settingsState.aboutCustomInfo) }
    var encryptionTypeState by remember { mutableStateOf(settingsState.encryptionType) }
    var splashWelcomeMessageState by remember { mutableStateOf(settingsState.splashWelcomeMessage) }

    // Banner control states
    var bannerEnabled by remember { mutableStateOf(settingsState.bannerEnabled) }
    var bannerType by remember { mutableStateOf(settingsState.bannerType) }
    var bannerContent by remember { mutableStateOf(settingsState.bannerContent) }
    var bannerBase64 by remember { mutableStateOf(settingsState.bannerBase64) }
    var bannerLocation by remember { mutableStateOf(settingsState.bannerLocation) }
    var bannerDurationSeconds by remember { mutableStateOf(settingsState.bannerDurationSeconds.toString()) }
    var bannerDisplayStyle by remember { mutableStateOf(settingsState.bannerDisplayStyle) }

    val context = LocalContext.current
    var showLogsDialog by remember { mutableStateOf(false) }
    var showRegistrationTestScreen by remember { mutableStateOf(false) }
    var showAssistantTestScreen by remember { mutableStateOf(false) }
    val sp = remember { context.getSharedPreferences("yemen_service_prefs", android.content.Context.MODE_PRIVATE) }
    val secureStorage = remember { com.serviseyem.utils.SecureStorage(context) }
    var rememberLoginInput by remember { mutableStateOf(secureStorage.getAdminSession()?.role == "OWNER") }
    var adminUsernameInput by remember { mutableStateOf(settingsState.adminUsername) }

    val galleryLauncherForBanner = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val mimeType = context.contentResolver.getType(it)
                if (mimeType != null && mimeType.startsWith("video/")) {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bytes = inputStream?.readBytes()
                    if (bytes != null) {
                        bannerBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                        bannerType = "VIDEO"
                        Toast.makeText(context, "📹 تم تحميل الفيديو القصير للبنر بنجاح!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val base64Str = com.serviseyem.utils.compressAndResizeImageUri(context, it, 800, 70)
                    if (base64Str.isNotEmpty()) {
                        bannerBase64 = base64Str
                        bannerType = "IMAGE"
                        Toast.makeText(context, "📸 تم تحميل صورة البنر الإعلاني من المعرض بنجاح!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    val galleryLauncherForCover = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val base64Str = com.serviseyem.utils.compressAndResizeImageUri(context, it, 800, 70)
                if (base64Str.isNotEmpty()) {
                    aboutCoverBase64 = base64Str
                    aboutCoverType = "IMAGE"
                    Toast.makeText(context, "📸 تم تحميل صورة الغلاف من المعرض بنجاح!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔓 بوابة المالك والتحكم الخلفي الديناميكي", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Button(
                onClick = { viewModel.logout(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("إغلاق اللوحة", color = Color.White, fontSize = 10.sp)
            }
        }

        // 🛡️ زر استعراض سجل تشخيص أخطاء Firestore و API للمالك
        Button(
            onClick = { showLogsDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("🔍 سجل تشخيص أخطاء السحابة والـ API (Logs)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        OutlinedTextField(
            value = appName,
            onValueChange = { appName = it },
            label = { Text("اسم التطبيق الرئيسي") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = welcomeMessage,
            onValueChange = { welcomeMessage = it },
            label = { Text("رسالة الترحيب في الهيدر") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = splashWelcomeMessageState,
            onValueChange = { splashWelcomeMessageState = it },
            label = { Text("رسالة ترحيب شاشة البداية (Splash)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = footerMessage,
            onValueChange = { footerMessage = it },
            label = { Text("نص الفوتر الأوسط (افتراضي: wam777644)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = footerBgColorHex,
            onValueChange = { footerBgColorHex = it },
            label = { Text("لون خلفية الفوتر (كود Hex مثل #115E59)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = appVersion,
            onValueChange = { appVersion = it },
            label = { Text("رقم إصدار التطبيق بالفوتر (مثل v2.2026)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("⚙️ التحكم الكامل في أيقونات ومظهر الشريط السفلي (الفوتر):", fontSize = 12.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ℹ️ أيقونة 'عن التطبيق':", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = showInfoIcon,
                        onCheckedChange = { showInfoIcon = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📅 أيقونة 'الحجوزات':", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = showBookingsIcon,
                        onCheckedChange = { showBookingsIcon = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌐 أيقونة 'تبديل اللغة' (العربية / English):", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = showLangIcon,
                        onCheckedChange = { showLangIcon = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒 أيقونة 'لوحة الإدارة':", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = showAdminIcon,
                        onCheckedChange = { showAdminIcon = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💬 نص الشريط السفلي (الفوتر):", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = showFooterText,
                        onCheckedChange = { showFooterText = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                    )
                }

                Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

                Text("🎨 شكل أيقونة تبديل اللغة:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("GLOBE" to "كرة أرضية 🌐", "TRANSLATE" to "ترجمة 🔤", "TEXT" to "نص لغة 💬").forEach { (type, lbl) ->
                        FilterChip(
                            selected = langIconType == type,
                            onClick = { langIconType = type },
                            label = { Text(lbl, fontSize = 9.sp) }
                        )
                    }
                }

                Text("🎨 شكل أيقونة لوحة الإدارة:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("LOCK" to "قفل 🔒", "SETTINGS" to "ترس ⚙️", "KEY" to "مفتاح 🔑", "SHIELD" to "درع 🛡️").forEach { (type, lbl) ->
                        FilterChip(
                            selected = adminIconType == type,
                            onClick = { adminIconType = type },
                            label = { Text(lbl, fontSize = 9.sp) }
                        )
                    }
                }

                Text("تنسيق وترتيب شريط التذييل (الفوتر):", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = footerItemsOrder,
                    onValueChange = { footerItemsOrder = it },
                    label = { Text("ترتيب عناصر الفوتر (مفصولة بفاصلة: INFO,BOOKINGS,TEXT,LANG,ADMIN)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    val layouts = listOf(
                        "INFO,BOOKINGS,TEXT,LANG,ADMIN" to "تنسيق قياسي (يسار->يمين)",
                        "INFO,TEXT,LANG,ADMIN" to "بدون حجوزات",
                        "TEXT,LANG,ADMIN" to "نص ولغة وإدارة"
                    )
                    layouts.forEach { (order, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (footerItemsOrder == order) themeColors.accent else themeColors.surface)
                                .clickable { footerItemsOrder = order }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontSize = 8.sp, color = if (footerItemsOrder == order) Color.Black else Color.White, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = supportPhone,
            onValueChange = { supportPhone = it },
            label = { Text("رقم هاتف الدعم الفني للبرنامج") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = supportEmail,
            onValueChange = { supportEmail = it },
            label = { Text("البريد الإلكتروني للدعم والمظالم") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = supportWhatsapp,
            onValueChange = { supportWhatsapp = it },
            label = { Text("رابط أو رقم واتساب الدعم الفني") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = appDownloadUrl,
            onValueChange = { appDownloadUrl = it },
            label = { Text("رابط خارجي لتحميل التطبيق (نسخة APK)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = websiteUrl,
            onValueChange = { websiteUrl = it },
            label = { Text("رابط الموقع الإلكتروني الرسمي") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = telegramUrl,
            onValueChange = { telegramUrl = it },
            label = { Text("رابط حساب/قناة التليجرام") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = facebookUrl,
            onValueChange = { facebookUrl = it },
            label = { Text("رابط صفحة الفيسبوك الرسمية") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Text("🎨 سمة التطبيق الافتراضية:", fontSize = 12.sp, color = themeColors.textSecondary)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val themes = listOf("EMERALD_YEMEN", "COSMIC_SILVER", "LUXURY_GOLD", "ELITE_EMERALD", "SMOKE_BLACK", "LIGHT_PINK", "GOLDEN_WHITE", "CUSTOM_THEME")
            themes.forEach { th ->
                val label = when (th) {
                    "EMERALD_YEMEN" -> "الزمرد اليمني 🟢"
                    "COSMIC_SILVER" -> "كوزميك سيلفر 🪐"
                    "LUXURY_GOLD" -> "الذهبي الفاخر 🌟"
                    "ELITE_EMERALD" -> "الزمردي الراقي 💚"
                    "SMOKE_BLACK" -> "الأسود الدخاني ⚫"
                    "LIGHT_PINK" -> "الزهري الفاتح 🌸"
                    "GOLDEN_WHITE" -> "الأبيض الذهبي ⚪🟡"
                    "CUSTOM_THEME" -> "سمة مخصصة 🎨"
                    else -> th
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeThemeId == th) themeColors.accent else themeColors.surface)
                        .clickable { activeThemeId = th }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(label, fontSize = 9.sp, color = if (activeThemeId == th) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("وضع الصيانة المؤقت", color = Color.White, fontSize = 13.sp)
            Switch(checked = isMaintenanceActive, onCheckedChange = { isMaintenanceActive = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("إخفاء زر المساعد الذكي العائم", color = Color.White, fontSize = 13.sp)
            Switch(checked = assistantHidden, onCheckedChange = { assistantHidden = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تفعيل البحث الصوتي", color = Color.White, fontSize = 13.sp)
            Switch(checked = isSpeechSearchEnabled, onCheckedChange = { isSpeechSearchEnabled = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("إلغاء شرط تسجيل الزائرين للحجز والمحادثة", color = Color.White, fontSize = 13.sp)
            Switch(checked = bypassVisitorRegistration, onCheckedChange = { bypassVisitorRegistration = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔑 إلزامية كلمة المرور لجميع حسابات المواطنين الجديدة", color = Color.White, fontSize = 13.sp)
            Switch(checked = isUserPasswordRequired, onCheckedChange = { isUserPasswordRequired = it })
        }

        var mapProviderState by remember { mutableStateOf(settingsState.mapProvider) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تمكين ميزة الرادار والخريطة للجماهير", color = Color.White, fontSize = 13.sp)
            Switch(checked = isMapFeatureEnabled, onCheckedChange = { isMapFeatureEnabled = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("محرك الخرائط المعتمد:", color = Color.White, fontSize = 13.sp)
            Row {
                FilterChip(
                    selected = mapProviderState == "MAPLIBRE",
                    onClick = { mapProviderState = "MAPLIBRE" },
                    label = { Text("MapLibre", fontSize = 10.sp) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                FilterChip(
                    selected = mapProviderState == "GOOGLE",
                    onClick = { mapProviderState = "GOOGLE" },
                    label = { Text("Google", fontSize = 10.sp) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                FilterChip(
                    selected = mapProviderState == "MAPBOX",
                    onClick = { mapProviderState = "MAPBOX" },
                    label = { Text("Mapbox", fontSize = 10.sp) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔓 تعطيل جدار حماية المحادثات الفورية", color = Color.White, fontSize = 13.sp)
            Switch(checked = disableChatFirewall, onCheckedChange = { disableChatFirewall = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔓 تعطيل جدار حماية حجز المواعيد", color = Color.White, fontSize = 13.sp)
            Switch(checked = disableBookingFirewall, onCheckedChange = { disableBookingFirewall = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏪 تفعيل وتبويب قسم المحلات التجارية بالكامل", color = Color.White, fontSize = 13.sp)
            Switch(checked = isStoresEnabled, onCheckedChange = { isStoresEnabled = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏠 تفعيل وتبويب قسم العقارات بالكامل", color = Color.White, fontSize = 13.sp)
            Switch(checked = isPropertiesEnabled, onCheckedChange = { isPropertiesEnabled = it })
        }

        // Dedicated Card for Registration Forms Control
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("📋 التحكم بإظهار/إخفاء استمارات التسجيل للأقسام", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("تحديد الاستمارات المتاحة للمستخدمين للتسجيل والانضمام (المالك والأدمن يظهر له كل شيء دائماً):", fontSize = 10.sp, color = themeColors.textSecondary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🛠️ استمارة انضمام مقدمي الخدمات (أصحاب المهن)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = enableProvidersRegistration, onCheckedChange = { enableProvidersRegistration = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🏪 استمارة تسجيل المحلات والمراكز التجارية", color = Color.White, fontSize = 12.sp)
                    Switch(checked = enableStoresRegistration, onCheckedChange = { enableStoresRegistration = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🍔 استمارة إضافة المطاعم والكافيهات", color = Color.White, fontSize = 12.sp)
                    Switch(checked = enableRestaurantsRegistration, onCheckedChange = { enableRestaurantsRegistration = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🏢 استمارة إدراج العقارات والبيوت", color = Color.White, fontSize = 12.sp)
                    Switch(checked = enablePropertiesRegistration, onCheckedChange = { enablePropertiesRegistration = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("🏥 استمارة تسجيل المراكز الطبية والعيادات", color = Color.White, fontSize = 12.sp)
                    Switch(checked = enableMedicalRegistration, onCheckedChange = { enableMedicalRegistration = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("💼 استمارة نشر الوظائف والفرص", color = Color.White, fontSize = 12.sp)
                    Switch(checked = enableJobsRegistration, onCheckedChange = { enableJobsRegistration = it })
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("إخفاء البوت المساعد المساعد", color = Color.White, fontSize = 13.sp)
            Switch(checked = assistantHidden, onCheckedChange = { assistantHidden = it })
        }

        Column {
            Text("قطر دائرة البوت المساعد: ${assistantSize.toInt()} dp", color = Color.White, fontSize = 11.sp)
            Slider(value = assistantSize, onValueChange = { assistantSize = it }, valueRange = 40f..80f)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("إخفاء زر المحادثة العائم", color = Color.White, fontSize = 13.sp)
            Switch(checked = chatHidden, onCheckedChange = { chatHidden = it })
        }

        Column {
            Text("قطر دائرة زر المحادثة العائم: ${chatSize.toInt()} dp", color = Color.White, fontSize = 11.sp)
            Slider(value = chatSize, onValueChange = { chatSize = it }, valueRange = 28f..80f)
        }

        Column {
            Text("الحد الأقصى لنطاق البحث الجغرافي: ${maxSearchRadiusKm.toInt()} كم", color = Color.White, fontSize = 11.sp)
            Slider(value = maxSearchRadiusKm, onValueChange = { maxSearchRadiusKm = it }, valueRange = 10f..100f)
        }

        // --- Custom Top Bar Icons Controls ---
        Divider(color = themeColors.accent.copy(alpha = 0.3f), thickness = 1.dp)
        Text("🎨 تخصيص وترتيب أيقونات شريط الرأس العلوي (Header):", fontSize = 12.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("إظهار أيقونة المزامنة/التحديث (Refresh)", color = Color.White, fontSize = 13.sp)
            Switch(checked = showRefreshIcon, onCheckedChange = { showRefreshIcon = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("إظهار أيقونة الإعدادات/اللغة (Settings)", color = Color.White, fontSize = 13.sp)
            Switch(checked = showSettingsIcon, onCheckedChange = { showSettingsIcon = it })
        }

        OutlinedTextField(
            value = headerIconsOrder,
            onValueChange = { headerIconsOrder = it },
            label = { Text("ترتيب الأيقونات (مفصولة بفاصلة)") },
            placeholder = { Text("مثال: MENU,NOTIF,CHAT,REFRESH,SETTINGS") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Text(
            text = "الأيقونات المتاحة: MENU (القائمة), NOTIF (الإشعارات), CHAT (المحادثة), REFRESH (المزامنة), SETTINGS (الإعدادات). اكتبها بالترتيب المطلوب من اليمين لليسار.",
            color = Color.LightGray,
            fontSize = 9.sp
        )

        // Custom easy reorder buttons for the admin!
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(themeColors.surface)
                    .clickable { 
                        headerIconsOrder = "MENU,NOTIF,CHAT"
                        Toast.makeText(context, "🎯 تم ضبط الترتيب الافتراضي للأيقونات", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("الترتيب الافتراضي", fontSize = 9.sp, color = Color.White)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(themeColors.surface)
                    .clickable { 
                        headerIconsOrder = "CHAT,NOTIF,MENU"
                        Toast.makeText(context, "🎯 تم عكس ترتيب الأيقونات", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("عكس الترتيب", fontSize = 9.sp, color = Color.White)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(themeColors.surface)
                    .clickable { 
                        headerIconsOrder = "MENU,CHAT,NOTIF,REFRESH,SETTINGS"
                        showRefreshIcon = true
                        showSettingsIcon = true
                        Toast.makeText(context, "🎯 تم تفعيل كافة الأيقونات بالترتيب الكامل", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("تفعيل وترتيب الكل", fontSize = 9.sp, color = Color.White)
            }
        }

        // --- Custom Categories Layout Controls ---
        Divider(color = themeColors.accent.copy(alpha = 0.3f), thickness = 1.dp)
        Text("📁 طريقة عرض وحركة الأقسام (التصنيفات):", fontSize = 12.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
        
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val layouts = listOf(
                "GRID_HORIZONTAL" to "شبكة أفقية (تمرير جانبي) ↔️",
                "ROW_HORIZONTAL" to "صف أفقي (تمرير جانبي) ↔️",
                "GRID_VERTICAL" to "شبكة عمودية (تمرير طولي) ↕️",
                "LIST_VERTICAL" to "قائمة عمودية (تمرير طولي) ↕️"
            )
            layouts.forEach { (layVal, layLabel) ->
                val isSel = categoriesLayoutTypeState == layVal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) themeColors.accent else themeColors.surface)
                        .clickable { categoriesLayoutTypeState = layVal }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(layLabel, fontSize = 11.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    if (isSel) {
                        Text("✔️", fontSize = 10.sp, color = Color.Black)
                    }
                }
            }
        }

        // --- Custom Booking dynamic layouts & rules controls ---
        Divider(color = themeColors.accent.copy(alpha = 0.3f), thickness = 1.dp)
        Text("📅 تخصيص شروط واستمارة الحجز اليمني (كاملة بالتفصيل):", fontSize = 12.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = bookingTermsInput,
            onValueChange = { bookingTermsInput = it },
            label = { Text("نص شروط وقواعد الحجز بالدليل") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = bookingLabelNameInput,
            onValueChange = { bookingLabelNameInput = it },
            label = { Text("تسمية حقل الاسم بالاستمارة") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = bookingLabelPhoneInput,
            onValueChange = { bookingLabelPhoneInput = it },
            label = { Text("تسمية حقل الهاتف بالاستمارة") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = bookingLabelAreaInput,
            onValueChange = { bookingLabelAreaInput = it },
            label = { Text("تسمية حقل منطقة السكن بالاستمارة") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = bookingLabelServiceInput,
            onValueChange = { bookingLabelServiceInput = it },
            label = { Text("تسمية حقل نوع الخدمة بالاستمارة") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Divider(color = themeColors.accent.copy(alpha = 0.3f), thickness = 1.dp)
        Text("🔐 بيانات المدير (Admin) والمصادقة الآمنة:", fontSize = 12.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = adminUsernameInput,
            onValueChange = { adminUsernameInput = it },
            label = { Text("معرّف/بريد المدير (للقراءة فقط)") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Text("🔒 تتم إدارة المصادقة وكلمات المرور للمدير بشكل مشفر عبر Firebase Auth للحماية القصوى.", fontSize = 10.5.sp, color = Color.Gray)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تذكرني (حفظ الدخول بصورة دائمة كمالك)", color = Color.White, fontSize = 13.sp)
            Switch(checked = rememberLoginInput, onCheckedChange = { rememberLoginInput = it })
        }

        Spacer(modifier = Modifier.height(4.dp))
        Divider(color = themeColors.accent.copy(alpha = 0.3f), thickness = 1.dp)
        Text("ℹ️ تخصيص صفحة معلومات عن التطبيق (المحتوى والغلاف):", fontSize = 12.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)

        Text("نوع غلاف الصفحة المعروض:", fontSize = 11.sp, color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val types = listOf("IMAGE" to "صورة 🖼️", "VIDEO" to "فيديو 🎥", "TEXT" to "نص فقط 📝")
            types.forEach { (typeVal, typeLabel) ->
                val isSel = aboutCoverType == typeVal
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) themeColors.accent else themeColors.surface)
                        .clickable { aboutCoverType = typeVal }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(typeLabel, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White)
                }
            }
        }

        if (aboutCoverType == "IMAGE") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { galleryLauncherForCover.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("صورة الهاتف 📁", fontSize = 10.sp, color = Color.White)
                }
                
                OutlinedTextField(
                    value = aboutCoverContent,
                    onValueChange = { aboutCoverContent = it },
                    label = { Text("أو رابط صورة الإنترنت") },
                    modifier = Modifier.weight(1.5f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        } else if (aboutCoverType == "VIDEO") {
            OutlinedTextField(
                value = aboutCoverContent,
                onValueChange = { aboutCoverContent = it },
                label = { Text("رابط الفيديو (يوتيوب أو ملف مباشر)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
        } else {
            OutlinedTextField(
                value = aboutCoverContent,
                onValueChange = { aboutCoverContent = it },
                label = { Text("نص الغلاف البديل") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
        }

        OutlinedTextField(
            value = aboutCustomInfo,
            onValueChange = { aboutCustomInfo = it },
            label = { Text("المعلومات التفصيلية المكتوبة عن التطبيق") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        OutlinedTextField(
            value = encryptionTypeState,
            onValueChange = { encryptionTypeState = it },
            label = { Text("مستوى التشفير للتطبيق") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text("🔗 روابط وتعديل شبكات التواصل الاجتماعي للتطبيق:", fontSize = 11.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = twitterUrl,
            onValueChange = { twitterUrl = it },
            label = { Text("رابط تويتر (X)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("إخفاء تويتر (X):", fontSize = 11.sp, color = Color.White)
            Switch(checked = hideTwitter, onCheckedChange = { hideTwitter = it }, colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent))
        }

        OutlinedTextField(
            value = instagramUrl,
            onValueChange = { instagramUrl = it },
            label = { Text("رابط إنستغرام") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("إخفاء إنستغرام:", fontSize = 11.sp, color = Color.White)
            Switch(checked = hideInstagram, onCheckedChange = { hideInstagram = it }, colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent))
        }

        OutlinedTextField(
            value = youtubeUrl,
            onValueChange = { youtubeUrl = it },
            label = { Text("رابط يوتيوب") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("إخفاء يوتيوب:", fontSize = 11.sp, color = Color.White)
            Switch(checked = hideYoutube, onCheckedChange = { hideYoutube = it }, colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent))
        }

        OutlinedTextField(
            value = websiteUrl,
            onValueChange = { websiteUrl = it },
            label = { Text("رابط الموقع الإلكتروني") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("إخفاء الموقع الإلكتروني:", fontSize = 11.sp, color = Color.White)
            Switch(checked = hideWebsite, onCheckedChange = { hideWebsite = it }, colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent))
        }

        OutlinedTextField(
            value = facebookUrl,
            onValueChange = { facebookUrl = it },
            label = { Text("رابط فيسبوك") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("إخفاء فيسبوك:", fontSize = 11.sp, color = Color.White)
            Switch(checked = hideFacebook, onCheckedChange = { hideFacebook = it }, colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent))
        }

        OutlinedTextField(
            value = telegramUrl,
            onValueChange = { telegramUrl = it },
            label = { Text("رابط تليجرام") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("إخفاء تليجرام:", fontSize = 11.sp, color = Color.White)
            Switch(checked = hideTelegram, onCheckedChange = { hideTelegram = it }, colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent))
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("📐 ترتيب وهيكلة عناصر صفحة معلومات عن التطبيق:", fontSize = 11.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = aboutLayoutOrder,
            onValueChange = { aboutLayoutOrder = it },
            label = { Text("ترتيب العناصر (مفصولة بفاصلة ,)") },
            placeholder = { Text("COVER,LOGO,TITLE,ANNOUNCEMENT,ABOUT_CARD,DOWNLOAD_BTN,CONTACTS,SOCIALS") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        Text("مفاتيح الترتيب المدعومة: COVER, LOGO, TITLE, ANNOUNCEMENT, ABOUT_CARD, DOWNLOAD_BTN, CONTACTS, SOCIALS", fontSize = 9.sp, color = Color.LightGray)

        // 📢 Dynamic App Banner Configuration UI
        Divider(color = themeColors.accent.copy(alpha = 0.3f), thickness = 1.dp)
        Text("📢 إعداد وتخصيص البنر الإعلاني بالأعلى (الرئيسي):", fontSize = 13.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("تفعيل عرض البنر بالرئيسية:", fontSize = 12.sp, color = Color.White)
            Switch(
                checked = bannerEnabled,
                onCheckedChange = { bannerEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
            )
        }

        if (bannerEnabled) {
            Text("نوع محتوى البنر:", fontSize = 11.sp, color = themeColors.textSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val bannerTypes = listOf("TEXT" to "نص فقط 📝", "IMAGE" to "صورة 🖼️", "VIDEO" to "فيديو قصير 📹")
                bannerTypes.forEach { (typeKey, typeLabel) ->
                    val isSel = bannerType == typeKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) themeColors.accent else themeColors.surface)
                            .clickable { bannerType = typeKey }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(typeLabel, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text("مكان ظهور البنر بالرئيسية:", fontSize = 11.sp, color = themeColors.textSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val locations = listOf("TOP" to "أعلى الشاشة ⬆️", "BOTTOM" to "أسفل الشاشة ⬇️")
                locations.forEach { (locKey, locLabel) ->
                    val isSel = bannerLocation == locKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) themeColors.accent else themeColors.surface)
                            .clickable { bannerLocation = locKey }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(locLabel, fontSize = 11.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (bannerType == "IMAGE" || bannerType == "VIDEO") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (bannerType == "VIDEO") {
                                galleryLauncherForBanner.launch("video/*")
                            } else {
                                galleryLauncherForBanner.launch("image/*")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(if (bannerType == "VIDEO") "فيديو قصير 📹" else "صورة البنر 📁", fontSize = 10.sp, color = Color.White)
                    }
                    
                    OutlinedTextField(
                        value = bannerContent,
                        onValueChange = { bannerContent = it },
                        label = { Text("أو رابط الإنترنت") },
                        modifier = Modifier.weight(2f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            } else {
                OutlinedTextField(
                    value = bannerContent,
                    onValueChange = { bannerContent = it },
                    label = { Text("محتوى الإعلان النصي") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = bannerDurationSeconds,
                onValueChange = { bannerDurationSeconds = it },
                label = { Text("مدة ظهور البنر بالثواني (0 للبقاء المستمر)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("طريقة حركة أو ظهور البنر الإعلاني:", fontSize = 11.sp, color = themeColors.textSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val styles = listOf("SLIDE" to "انزلاق ➡️", "FADE" to "تلاشي 🌫️", "BLINK" to "وميض 💡", "SCROLL" to "تمرير 📜")
                styles.forEach { (styleKey, styleLabel) ->
                    val isSel = bannerDisplayStyle == styleKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) themeColors.accent else themeColors.surface)
                            .clickable { bannerDisplayStyle = styleKey }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(styleLabel, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Divider(color = themeColors.accent.copy(alpha = 0.3f), thickness = 1.dp)

        Spacer(modifier = Modifier.height(4.dp))
        Text("🖼️ شعار التطبيق المستخدم بالمنصة:", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val logos = listOf("شعار احترافي 🔧", "بسيط 🌟", "رسمي 🤝", "أيقونة الدليل 🎯")
            var selectedSimulatedLogo by remember { mutableStateOf("شعار احترافي 🔧") }
            logos.forEach { lg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedSimulatedLogo == lg) themeColors.accent else themeColors.surface)
                        .clickable { 
                            selectedSimulatedLogo = lg
                            Toast.makeText(context, "🖼️ تم تحديد الشعار ($lg) بنجاح للتطبيق!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(lg, fontSize = 9.sp, color = if (selectedSimulatedLogo == lg) Color.Black else Color.White)
                }
            }
        }

        Button(
            onClick = {
                val currentSettings = settingsState.copy(
                    appName = appName,
                    welcomeMessage = welcomeMessage,
                    footerMessage = footerMessage,
                    footerBgColorHex = footerBgColorHex,
                    footerItemsOrder = footerItemsOrder,
                    appVersion = appVersion,
                    activeThemeId = activeThemeId,
                    isMaintenanceActive = isMaintenanceActive,
                    hidePromoFooter = hidePromoFooter,
                    assistantHidden = assistantHidden,
                    assistantSize = assistantSize.toInt(),
                    chatHidden = chatHidden,
                    chatSize = chatSize.toInt(),
                    maxSearchRadiusKm = maxSearchRadiusKm.toInt(),
                    isSpeechSearchEnabled = isSpeechSearchEnabled,
                    bypassVisitorRegistration = bypassVisitorRegistration,
                    isUserPasswordRequired = isUserPasswordRequired,
                    disableChatFirewall = disableChatFirewall,
                    disableBookingFirewall = disableBookingFirewall,
                    isMapFeatureEnabled = isMapFeatureEnabled,
                    mapProvider = mapProviderState,
                    enableProvidersRegistration = enableProvidersRegistration,
                    enableStoresRegistration = enableStoresRegistration,
                    enableRestaurantsRegistration = enableRestaurantsRegistration,
                    enablePropertiesRegistration = enablePropertiesRegistration,
                    enableMedicalRegistration = enableMedicalRegistration,
                    enableJobsRegistration = enableJobsRegistration,
                    bookingTerms = bookingTermsInput,
                    bookingLabelName = bookingLabelNameInput,
                    bookingLabelPhone = bookingLabelPhoneInput,
                    bookingLabelArea = bookingLabelAreaInput,
                    bookingLabelService = bookingLabelServiceInput,
                    adminUsername = adminUsernameInput,
                    aboutCoverType = aboutCoverType,
                    aboutCoverContent = aboutCoverContent,
                    aboutCoverBase64 = aboutCoverBase64,
                    aboutCustomInfo = aboutCustomInfo,
                    encryptionType = encryptionTypeState,
                    splashWelcomeMessage = splashWelcomeMessageState,
                    bannerEnabled = bannerEnabled,
                    bannerType = bannerType,
                    bannerContent = bannerContent,
                    bannerBase64 = bannerBase64,
                    bannerLocation = bannerLocation,
                    bannerDurationSeconds = bannerDurationSeconds.toIntOrNull() ?: 0,
                    bannerDisplayStyle = bannerDisplayStyle,
                    appDownloadUrl = appDownloadUrl,
                    showRefreshIcon = showRefreshIcon,
                    showSettingsIcon = showSettingsIcon,
                    headerIconsOrder = headerIconsOrder,
                    categoriesLayoutType = categoriesLayoutTypeState,
                    isStoresEnabled = isStoresEnabled,
                    isPropertiesEnabled = isPropertiesEnabled,
                    websiteUrl = websiteUrl,
                    telegramUrl = telegramUrl,
                    facebookUrl = facebookUrl,
                    twitterUrl = twitterUrl,
                    instagramUrl = instagramUrl,
                    youtubeUrl = youtubeUrl,
                    aboutLayoutOrder = aboutLayoutOrder,
                    showInfoIcon = showInfoIcon,
                    showBookingsIcon = showBookingsIcon,
                    showLangIcon = showLangIcon,
                    showAdminIcon = showAdminIcon,
                    showFooterText = showFooterText,
                    infoIconType = infoIconType,
                    adminIconType = adminIconType,
                    langIconType = langIconType,
                    bookingsIconType = bookingsIconType,
                    hideTwitter = hideTwitter,
                    hideInstagram = hideInstagram,
                    hideYoutube = hideYoutube,
                    hideFacebook = hideFacebook,
                    hideTelegram = hideTelegram,
                    hideWebsite = hideWebsite
                )
                viewModel.saveCustomSettingsState(currentSettings)

                if (rememberLoginInput) {
                    val currentAdminEmail = currentSettings.ownerEmail.ifBlank { "owner" }
                    secureStorage.saveAdminSession(
                        com.serviseyem.utils.AdminSession(
                            uid = "owner_backdoor",
                            email = currentAdminEmail,
                            loginTime = System.currentTimeMillis(),
                            refreshToken = "",
                            role = "OWNER"
                        )
                    )
                } else {
                    secureStorage.clearAdminSession()
                }
                Toast.makeText(context, "💾 تم حفظ كافة التخصيصات والتحققات بنجاح!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💾 حفظ إعدادات البوابة والتخزين والتطبيق", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        var showWipeConfirmDialog by remember { mutableStateOf(false) }
        var wipePasswordInput by remember { mutableStateOf("") }
        var wipeErrorMsg by remember { mutableStateOf("") }

        if (showWipeConfirmDialog) {
            AlertDialog(
                onDismissRequest = {
                    showWipeConfirmDialog = false
                    wipePasswordInput = ""
                    wipeErrorMsg = ""
                },
                title = { Text("🔒 إدخال كلمة المرور لتأكيد الحذف والمسح", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Red) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("⚠️ تنبيه أمني: يتطلب حذف أو تطهير البيانات إدخال كلمة مرور الأدمن:", fontSize = 11.sp, color = Color.White)
                        OutlinedTextField(
                            value = wipePasswordInput,
                            onValueChange = {
                                wipePasswordInput = it
                                wipeErrorMsg = ""
                            },
                            label = { Text("كلمة مرور الأدمن") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (wipeErrorMsg.isNotEmpty()) {
                            Text(wipeErrorMsg, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (viewModel.verifyAdminOrOwnerPassword(wipePasswordInput)) {
                                viewModel.wipeAllMockAndTemporaryData()
                                showWipeConfirmDialog = false
                                wipePasswordInput = ""
                                wipeErrorMsg = ""
                            } else {
                                wipeErrorMsg = "❌ كلمة المرور غير صحيحة! تم منع التطهير والحذف."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("تأكيد الحذف النهائي 🗑️", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showWipeConfirmDialog = false
                        wipePasswordInput = ""
                        wipeErrorMsg = ""
                    }) {
                        Text("إلغاء", color = Color.LightGray)
                    }
                }
            )
        }

        Button(
            onClick = {
                showRegistrationTestScreen = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "تشغيل اختبار التسجيل",
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("🧪 تشغيل اختبار التسجيل الشامل للأقسام 8", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                showAssistantTestScreen = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "اختبار المساعد الذكي",
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("🤖 اختبار المساعد الذكي", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                showWipeConfirmDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "حذف البيانات الوهمية",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("🧹 حذف الفنيين والرسائل والإشعارات الوهمية", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (showLogsDialog) {
            com.serviseyem.utils.ErrorLogsViewerDialog(
                themeColors = themeColors,
                onDismiss = { showLogsDialog = false }
            )
        }

        if (showRegistrationTestScreen) {
            RegistrationTestScreen(
                viewModel = viewModel,
                themeColors = themeColors,
                onDismiss = { showRegistrationTestScreen = false }
            )
        }

        if (showAssistantTestScreen) {
            AssistantTestScreen(
                viewModel = viewModel,
                themeColors = themeColors,
                onDismiss = { showAssistantTestScreen = false }
            )
        }
    }
}


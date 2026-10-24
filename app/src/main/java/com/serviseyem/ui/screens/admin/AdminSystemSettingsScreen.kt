package com.serviseyem.ui.screens.admin

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.ui.screens.admin.components.*

@Composable
fun AdminSystemSettingsScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsState by viewModel.settings.collectAsState()

    var systemVersion by remember(settingsState.appVersion) {
        mutableStateOf(if (settingsState.appVersion.isNotBlank()) settingsState.appVersion else "2.6.0")
    }
    var minRequiredVersion by remember { mutableStateOf("2.5.0") }
    var forceUpdateEnabled by remember { mutableStateOf(false) }
    var updateDownloadUrl by remember(settingsState.appDownloadUrl) {
        mutableStateOf(if (settingsState.appDownloadUrl.isNotBlank()) settingsState.appDownloadUrl else "https://yemen-services.app/download")
    }

    var defaultLanguage by remember { mutableStateOf("العربية") }
    var showLanguageToggle by remember(settingsState.showLangIcon) { mutableStateOf(settingsState.showLangIcon) }
    var autoDetectLanguage by remember { mutableStateOf(true) }
    var forceRtlDirection by remember { mutableStateOf(true) }

    var defaultCurrency by remember { mutableStateOf("ريال يمني (YER)") }
    var appDisplayName by remember(settingsState.appName) { mutableStateOf(settingsState.appName) }
    var welcomeMessage by remember(settingsState.welcomeMessage) { mutableStateOf(settingsState.welcomeMessage) }
    var defaultThemeMode by remember { mutableStateOf("الوضع الداكن (Dark)") }

    val languages = listOf("العربية", "English")
    val currencies = listOf("ريال يمني (YER)", "ريال سعودي (SAR)", "دولار أمريكي (USD)")
    val themes = listOf("الوضع الداكن (Dark)", "الوضع الفاتح (Light)", "تلقائي النظام")

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. بطاقة إصدار النظام والتحديثات
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = themeColors.accent)
                    Text("🚀 إصدار النظام والتحديثات البرمجية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedTextField(
                    value = systemVersion,
                    onValueChange = { systemVersion = it },
                    label = { Text("إصدار النظام الحالي (System Version)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = minRequiredVersion,
                    onValueChange = { minRequiredVersion = it },
                    label = { Text("الحد الأدنى المطلوب للإصدار (Min Version)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("إلزام المستخدمين بالتحديث الفوري", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("منع العملاء من استخدام الإصدارات الأقدم وإلزامهم بالترقية", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(checked = forceUpdateEnabled, onCheckedChange = { forceUpdateEnabled = it })
                }

                OutlinedTextField(
                    value = updateDownloadUrl,
                    onValueChange = { updateDownloadUrl = it },
                    label = { Text("رابط تنزيل التحديث المباشر (APK / Store URL)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                var valhallaDownloadUrl by remember {
                    mutableStateOf(
                        context.getSharedPreferences("valhalla_prefs", android.content.Context.MODE_PRIVATE)
                            .getString("valhalla_url", "https://github.com/maher202021/Vela2026/releases/download/yemen-valhalla-1/yemen_valhalla_tiles.zip") ?: ""
                    )
                }

                OutlinedTextField(
                    value = valhallaDownloadUrl,
                    onValueChange = { valhallaDownloadUrl = it },
                    label = { Text("🌐 رابط تحميل خرائط Valhalla الأوفلاين (Admin URL)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Button(
                    onClick = {
                        context.getSharedPreferences("valhalla_prefs", android.content.Context.MODE_PRIVATE)
                            .edit()
                            .putString("valhalla_url", valhallaDownloadUrl)
                            .apply()
                        Toast.makeText(context, "تم حفظ رابط Valhalla بنجاح ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ رابط خرائط Valhalla المحدث", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. بطاقة تفضيلات اللغات
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF60A5FA))
                    Text("🌐 تفضيلات اللغات والترجمة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text("اللغة الافتراضية للنظام:", fontSize = 11.5.sp, color = Color.LightGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(languages) { lang ->
                        val isSelected = lang == defaultLanguage
                        Surface(
                            color = if (isSelected) themeColors.accent else Color(0xFF1E293B),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isSelected) themeColors.accent else Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.clickable { defaultLanguage = lang }
                        ) {
                            Text(lang, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("إظهار زر تبديل اللغة في القائمة السفلية", fontSize = 12.sp, color = Color.White)
                    Switch(checked = showLanguageToggle, onCheckedChange = { showLanguageToggle = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("اكتشاف لغة الجهاز تلقائياً", fontSize = 12.sp, color = Color.White)
                    Switch(checked = autoDetectLanguage, onCheckedChange = { autoDetectLanguage = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("فرض الاتجاه من اليمين لليسار (RTL Support)", fontSize = 12.sp, color = Color.White)
                    Switch(checked = forceRtlDirection, onCheckedChange = { forceRtlDirection = it })
                }
            }
        }

        // 3. تفضيلات النظام العامة والعملات
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFFBBF24))
                    Text("🎛️ تفضيلات النظام العامة والعملات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedTextField(
                    value = appDisplayName,
                    onValueChange = { appDisplayName = it },
                    label = { Text("اسم التطبيق الرسمي") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = welcomeMessage,
                    onValueChange = { welcomeMessage = it },
                    label = { Text("رسالة الترحيب في الواجهة الرئيسية") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Text("العملة الرسمية المعتمدة:", fontSize = 11.5.sp, color = Color.LightGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currencies) { curr ->
                        val isSelected = curr == defaultCurrency
                        Surface(
                            color = if (isSelected) themeColors.accent else Color(0xFF1E293B),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isSelected) themeColors.accent else Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.clickable { defaultCurrency = curr }
                        ) {
                            Text(curr, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }

                Text("مظهر النظام الافتراضي:", fontSize = 11.5.sp, color = Color.LightGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(themes) { thm ->
                        val isSelected = thm == defaultThemeMode
                        Surface(
                            color = if (isSelected) themeColors.accent else Color(0xFF1E293B),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isSelected) themeColors.accent else Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.clickable { defaultThemeMode = thm }
                        ) {
                            Text(thm, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }
            }
        }

        var isSavingSettings by remember { mutableStateOf(false) }

        Button(
            onClick = {
                if (isSavingSettings) return@Button
                isSavingSettings = true
                val st = settingsState
                viewModel.updateBackdoorSettings(
                    appDisplayName, welcomeMessage, st.footerMessage, st.activeThemeId,
                    st.supportPhone, st.supportEmail, st.supportWhatsapp,
                    st.isMaintenanceActive, st.hidePromoFooter, st.assistantHidden, st.assistantSize,
                    st.chatHidden, st.chatSize, st.maxSearchRadiusKm, st.isSpeechSearchEnabled,
                    false, 90
                )
                viewModel.db.collection("settings").document("main_settings").set(
                    mapOf(
                        "appVersion" to systemVersion,
                        "minRequiredVersion" to minRequiredVersion,
                        "forceUpdateEnabled" to forceUpdateEnabled,
                        "appDownloadUrl" to updateDownloadUrl,
                        "defaultLanguage" to defaultLanguage,
                        "showLangIcon" to showLanguageToggle,
                        "defaultCurrency" to defaultCurrency,
                        "defaultThemeMode" to defaultThemeMode,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                ).addOnSuccessListener {
                    isSavingSettings = false
                    Toast.makeText(context, "✅ تم حفظ ومزامنة إعدادات وتفضيلات النظام سحابياً!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { err ->
                    isSavingSettings = false
                    Toast.makeText(context, "❌ فشل الحفظ: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            },
            enabled = !isSavingSettings,
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (isSavingSettings) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("جاري الحفظ والمزامنة...", color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("💾 حفظ ومزامنة إعدادات النظام سحابياً", color = Color.Black, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun AdminSystemSettingsScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = AdminSystemSettingsScreenContent(viewModel, themeColors, modifier)

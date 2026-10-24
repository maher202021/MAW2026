package com.serviseyem.ui.screens.about

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.utils.VisualThemePalette

/**
 * 👑 لوحة تحكم كاملة للأدمن في شاشة "معلومات عن التطبيق"
 * تتيح التحكم في:
 * 1. أرقام الواتساب والهاتف والبريد الإلكتروني
 * 2. روابط التيليجرام، فيسبوك، تويتر/إكس، انستغرام، يوتيوب، والموقع الإلكتروني
 * 3. رابط تحميل وتحديث التطبيق (APK)
 * 4. إظهار أو إخفاء أي وسيلة تواصل
 * 5. ترتيب عناصر الشاشة والنص التعريفي المخصص
 */
@Composable
fun AboutLayoutEditor(
    settingsState: AdminSettingsEntity,
    themeColors: VisualThemePalette,
    viewModel: AboutViewModel,
    modifier: Modifier = Modifier
) {
    var isEditingAboutPanel by remember { mutableStateOf(false) }

    // Form states
    var whatsappTemp by remember(settingsState.supportWhatsapp) { mutableStateOf(settingsState.supportWhatsapp) }
    var phoneTemp by remember(settingsState.supportPhone) { mutableStateOf(settingsState.supportPhone) }
    var emailTemp by remember(settingsState.supportEmail) { mutableStateOf(settingsState.supportEmail) }

    var telegramTemp by remember(settingsState.telegramUrl) { mutableStateOf(settingsState.telegramUrl) }
    var twitterTemp by remember(settingsState.twitterUrl) { mutableStateOf(settingsState.twitterUrl) }
    var facebookTemp by remember(settingsState.facebookUrl) { mutableStateOf(settingsState.facebookUrl) }
    var websiteTemp by remember(settingsState.websiteUrl) { mutableStateOf(settingsState.websiteUrl) }
    var instagramTemp by remember(settingsState.instagramUrl) { mutableStateOf(settingsState.instagramUrl) }
    var youtubeTemp by remember(settingsState.youtubeUrl) { mutableStateOf(settingsState.youtubeUrl) }
    var downloadUrlTemp by remember(settingsState.appDownloadUrl) { mutableStateOf(settingsState.appDownloadUrl) }

    var appNameTemp by remember(settingsState.appName) { mutableStateOf(settingsState.appName) }
    var appVersionTemp by remember(settingsState.appVersion) { mutableStateOf(settingsState.appVersion) }
    var customTextTemp by remember(settingsState.aboutCustomInfo) { mutableStateOf(settingsState.aboutCustomInfo) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("👑", fontSize = 16.sp)
                    Text(
                        text = "تحكم الإدارة الكامل في شاشة (عن التطبيق)",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                IconButton(
                    onClick = {
                        isEditingAboutPanel = !isEditingAboutPanel
                        viewModel.setEditingMode(isEditingAboutPanel)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Text(if (isEditingAboutPanel) "🔽" else "⚙️", fontSize = 14.sp)
                }
            }

            if (isEditingAboutPanel) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                // Section A: Contact Info (WhatsApp, Phone, Email)
                Text(
                    text = "📞 أرقام التواصل وواتساب الدعم الفني:",
                    color = Color(0xFF10B981),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = whatsappTemp,
                    onValueChange = { whatsappTemp = it },
                    label = { Text("رقم واتساب الدعم الفني (مثال: 967777000000+)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White)
                )
                OutlinedTextField(
                    value = phoneTemp,
                    onValueChange = { phoneTemp = it },
                    label = { Text("رقم الاتصال المباشر (مثال: 777000000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White)
                )
                OutlinedTextField(
                    value = emailTemp,
                    onValueChange = { emailTemp = it },
                    label = { Text("البريد الإلكتروني للدعم") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White)
                )
                Button(
                    onClick = {
                        viewModel.updateContactInfo(whatsappTemp, phoneTemp, emailTemp)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Text("💾 حفظ أرقام ووسائل الدعم", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                // Section B: Social Media & Website URLs + Visibility
                Text(
                    text = "🌐 روابط التواصل الاجتماعي والموقع الإلكتروني:",
                    color = Color(0xFF60A5FA),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )

                // Telegram
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = telegramTemp,
                        onValueChange = { telegramTemp = it },
                        label = { Text("رابط قناة / حساب تيليجرام (Telegram)") },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White)
                    )
                    Checkbox(
                        checked = !settingsState.hideTelegram,
                        onCheckedChange = { isVisible -> viewModel.toggleSocialVisibility("TELEGRAM", !isVisible) }
                    )
                }

                // WhatsApp Channel / Web
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = websiteTemp,
                        onValueChange = { websiteTemp = it },
                        label = { Text("رابط الموقع الإلكتروني (Website)") },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White)
                    )
                    Checkbox(
                        checked = !settingsState.hideWebsite,
                        onCheckedChange = { isVisible -> viewModel.toggleSocialVisibility("WEBSITE", !isVisible) }
                    )
                }

                // Facebook
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = facebookTemp,
                        onValueChange = { facebookTemp = it },
                        label = { Text("رابط صفحة فيسبوك (Facebook)") },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White)
                    )
                    Checkbox(
                        checked = !settingsState.hideFacebook,
                        onCheckedChange = { isVisible -> viewModel.toggleSocialVisibility("FACEBOOK", !isVisible) }
                    )
                }

                // Twitter / X
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = twitterTemp,
                        onValueChange = { twitterTemp = it },
                        label = { Text("رابط حساب تويتر / إكس (Twitter/X)") },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White)
                    )
                    Checkbox(
                        checked = !settingsState.hideTwitter,
                        onCheckedChange = { isVisible -> viewModel.toggleSocialVisibility("TWITTER", !isVisible) }
                    )
                }

                // Instagram
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = instagramTemp,
                        onValueChange = { instagramTemp = it },
                        label = { Text("رابط حساب انستغرام (Instagram)") },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White)
                    )
                    Checkbox(
                        checked = !settingsState.hideInstagram,
                        onCheckedChange = { isVisible -> viewModel.toggleSocialVisibility("INSTAGRAM", !isVisible) }
                    )
                }

                // YouTube
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = youtubeTemp,
                        onValueChange = { youtubeTemp = it },
                        label = { Text("رابط قناة يوتيوب (YouTube)") },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White)
                    )
                    Checkbox(
                        checked = !settingsState.hideYoutube,
                        onCheckedChange = { isVisible -> viewModel.toggleSocialVisibility("YOUTUBE", !isVisible) }
                    )
                }

                // Download APK URL
                OutlinedTextField(
                    value = downloadUrlTemp,
                    onValueChange = { downloadUrlTemp = it },
                    label = { Text("رابط تنزيل وتحديث التطبيق المباشر (APK URL)") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White)
                )

                Button(
                    onClick = {
                        viewModel.updateSocialLinks(
                            telegram = telegramTemp,
                            twitter = twitterTemp,
                            facebook = facebookTemp,
                            website = websiteTemp,
                            instagram = instagramTemp,
                            youtube = youtubeTemp,
                            downloadUrl = downloadUrlTemp
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Text("💾 حفظ روابط التواصل والموقع", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                // Section C: App Identity & Custom Description
                Text(
                    text = "📝 هوية التطبيق والنص التعريفي:",
                    color = Color(0xFFFFD700),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = appNameTemp,
                    onValueChange = { appNameTemp = it },
                    label = { Text("اسم التطبيق المعروض") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White)
                )
                OutlinedTextField(
                    value = appVersionTemp,
                    onValueChange = { appVersionTemp = it },
                    label = { Text("رقم وتسمية الإصدار (مثال: v2.5.0 Gold Edition)") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White)
                )
                OutlinedTextField(
                    value = customTextTemp,
                    onValueChange = { customTextTemp = it },
                    label = { Text("النص التعريفي المخصص في شاشة عن التطبيق") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White)
                )
                Button(
                    onClick = {
                        viewModel.updateCustomInfo(customTextTemp)
                        viewModel.updateAppIdentity(appNameTemp, appVersionTemp, customTextTemp)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Text("💾 حفظ النص وهوية التطبيق", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                // Section D: Layout Order
                Text(
                    text = "🔀 ترتيب ظهور العناصر في شاشة (عن التطبيق):",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                val keyLabels = mapOf(
                    "COVER" to "🖼️ غلاف التطبيق",
                    "LOGO" to "🔴 شعار WAM",
                    "TITLE" to "🏷️ اسم وإصدار التطبيق",
                    "ANNOUNCEMENT" to "📢 إعلان المنصة",
                    "ABOUT_CARD" to "ℹ️ كارت نبذة عن التطبيق",
                    "DOWNLOAD_BTN" to "📥 زر تحميل وتحديث التطبيق",
                    "CONTACTS" to "📞 أرقام وتثبيت الدعم (واتساب واتصال)",
                    "SOCIALS" to "🌐 شبكات التواصل الاجتماعي"
                )

                val currentList = remember(settingsState.aboutLayoutOrder) {
                    settingsState.aboutLayoutOrder
                        .split(",")
                        .map { it.trim().uppercase() }
                        .filter { it.isNotEmpty() }
                }

                currentList.forEachIndexed { index, k ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(keyLabels[k] ?: k, color = Color.White, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (index > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(themeColors.primary)
                                        .clickable {
                                            viewModel.moveItem(index, moveUp = true)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("⬆️ تقديم", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (index < currentList.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF334155))
                                        .clickable {
                                            viewModel.moveItem(index, moveUp = false)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("⬇️ تأخير", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

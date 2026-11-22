package com.serviseyem.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.admin.components.adminSwitchColors
import com.serviseyem.utils.VisualThemePalette

/**
 * 🎙️ إدارة ميزة المكالمات الصوتية والاتصال السحابي (Voice Calls Admin Panel)
 * مرتبطة بإعدادات المنصة السحابية في Firestore للمزامنة الفورية
 */
@Composable
fun AdminVoiceCallPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    val settingsState by viewModel.settings.collectAsState()
    val cardBg = themeColors.surface
    val textColor = themeColors.textPrimary
    val primaryColor = themeColors.primary

    var voiceCallsEnabled by remember(settingsState.voiceCallsEnabled, settingsState.disableVoiceCalls) {
        mutableStateOf(settingsState.voiceCallsEnabled && !settingsState.disableVoiceCalls)
    }
    var showVoiceCallBtn by remember(settingsState.showVoiceCallButton) {
        mutableStateOf(settingsState.showVoiceCallButton)
    }
    var disabledAnnouncement by remember(settingsState.voiceCallsDisabledAnnouncement) {
        mutableStateOf(settingsState.voiceCallsDisabledAnnouncement)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = themeColors.accent,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        "إدارة ميزة المكالمات الصوتية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        "التحكم بمحرك الاتصال المباشر ومزامنته مع كافة الأجهزة",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = textColor.copy(alpha = 0.1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "تفعيل نظام المكالمات الصوتية بالمنصة",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = textColor
                    )
                    Text(
                        "إتاحة خيار الاتصال الصوتي المباشر في الدردشة والطلبات",
                        fontSize = 11.5.sp,
                        color = textColor.copy(alpha = 0.65f)
                    )
                }
                Switch(
                    checked = voiceCallsEnabled,
                    onCheckedChange = { voiceCallsEnabled = it },
                    colors = adminSwitchColors(themeColors)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "إظهار زر الاتصال الهاتفي الصوتي في البطاقات",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = textColor
                    )
                    Text(
                        "التحكم بظهور أيقونة الاتصال الصوتي للمستخدمين",
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.65f)
                    )
                }
                Switch(
                    checked = showVoiceCallBtn,
                    onCheckedChange = { showVoiceCallBtn = it },
                    colors = adminSwitchColors(themeColors)
                )
            }

            OutlinedTextField(
                value = disabledAnnouncement,
                onValueChange = { disabledAnnouncement = it },
                label = { Text("رسالة التنبيه عند إيقاف المكالمات (اختياري)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Surface(
                color = if (voiceCallsEnabled) Color(0xFF065F46).copy(alpha = 0.35f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = if (voiceCallsEnabled) Color(0xFF34D399) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (voiceCallsEnabled) {
                            "المكالمات مفعلة وتتم المزامنة مباشرة مع إعدادات المنصة السحابية."
                        } else {
                            "المكالمات متوقفة حالياً لحين إعادة تفعيلها وحفظ التعديل."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (voiceCallsEnabled) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Button(
                onClick = {
                    val updated = settingsState.copy(
                        voiceCallsEnabled = voiceCallsEnabled,
                        disableVoiceCalls = !voiceCallsEnabled,
                        showVoiceCallButton = showVoiceCallBtn,
                        voiceCallsDisabledAnnouncement = disabledAnnouncement.trim()
                    )
                    viewModel.saveCustomSettingsState(updated)
                    Toast.makeText(context, "✅ تم حفظ ومزامنة إعدادات المكالمات الصوتية سحابياً!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ ومزامنة إعدادات المكالمات 💾", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

package com.serviseyem.ui.screens.admin

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🎙️ إدارة ميزة المكالمات الصوتية والاتصال السحابي (Voice Calls Admin Panel)
 * تتيح التحكم بمكتبات الاتصال الصوتي المستقبلية وحالة التفعيل لتوفير حجم الحزمة والبيانات
 */
@Composable
fun AdminVoiceCallPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val cardBg = themeColors.surface
    val textColor = themeColors.textPrimary
    val primaryColor = themeColors.primary

    var voiceCallsEnabled by remember { mutableStateOf(false) } // معطل افتراضياً لتوفير الحجم
    var highQualityAudio by remember { mutableStateOf(false) }
    var recordCallsEnabled by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = primaryColor,
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
                        "التحكم بمحرك الاتصال الصوتي المباشر بين العملاء والفنيين",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = textColor.copy(alpha = 0.1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "تفعيل المكالمات الصوتية (مكتبة Agora)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = textColor
                    )
                    Text(
                        "إتاحة زر الاتصال الصوتي عبر الإنترنت بالدردشة والطلبات",
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = voiceCallsEnabled,
                    onCheckedChange = { voiceCallsEnabled = it }
                )
            }

            if (voiceCallsEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("جودة الصوت العالية (HD Audio)", fontSize = 13.sp, color = textColor)
                    Switch(checked = highQualityAudio, onCheckedChange = { highQualityAudio = it })
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
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
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ملاحظة: الميزة معطلة حالياً لتوفير مساحة وحجم التطبيق وسرعة التحميل على شبكات الإنترنت اليمنية.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

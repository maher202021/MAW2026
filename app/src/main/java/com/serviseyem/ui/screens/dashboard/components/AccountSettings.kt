package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * ⚙️ AccountSettings (إعدادات الحساب التجاري والملف الشخصي)
 * تحديث معلومات النشاط التجاري، تخصيص الإشعارات، إدارة الأمان وكلمة المرور.
 */
@Composable
fun AccountSettings(
    themeColors: VisualThemePalette,
    initialName: String = "",
    initialBio: String = "",
    onSaveProfile: (name: String, bio: String, notifyBookings: Boolean) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var businessName by remember { mutableStateOf(initialName) }
    var businessBio by remember { mutableStateOf(initialBio) }
    var notifyBookings by remember { mutableStateOf(true) }
    var notifyMessages by remember { mutableStateOf(true) }
    var notifyOffers by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF38BDF8))
                Text(
                    text = "إعدادات الحساب والملف التعريفي",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it; isSaved = false },
                label = { Text("اسم النشاط التجاري / المهني") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF38BDF8)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = businessBio,
                onValueChange = { businessBio = it; isSaved = false },
                label = { Text("نبذة مختصرة عن الخدمات والخبرات") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            HorizontalDivider(color = Color(0xFF334155))

            Text(
                text = "تفضيلات الإشعارات والتنبيهات:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCBD5E1)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("إشعارات الحجوزات والطلبات الجديدة", fontSize = 12.sp, color = Color.White)
                Switch(
                    checked = notifyBookings,
                    onCheckedChange = { notifyBookings = it; isSaved = false },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تنبيهات المحادثات والرسائل المباشرة", fontSize = 12.sp, color = Color.White)
                Switch(
                    checked = notifyMessages,
                    onCheckedChange = { notifyMessages = it; isSaved = false },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("العروض والرسائل التسويقية", fontSize = 12.sp, color = Color.White)
                Switch(
                    checked = notifyOffers,
                    onCheckedChange = { notifyOffers = it; isSaved = false },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                )
            }

            if (isSaved) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✓ تم حفظ التغييرات والإعدادات بنجاح",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Button(
                onClick = {
                    onSaveProfile(businessName, businessBio, notifyBookings)
                    isSaved = true
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("حفظ التغييرات", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "إصدار التطبيق: v${com.serviseyem.BuildConfig.VERSION_NAME}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

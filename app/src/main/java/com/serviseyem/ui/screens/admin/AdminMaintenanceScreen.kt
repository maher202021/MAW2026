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
fun AdminMaintenanceScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsState by viewModel.settings.collectAsState()

    var isMaint by remember(settingsState.isMaintenanceActive) { mutableStateOf(settingsState.isMaintenanceActive) }
    var emergencyMessage by remember(settingsState.chatDisabledAnnouncement) {
        mutableStateOf(if (settingsState.chatDisabledAnnouncement.isNotBlank()) settingsState.chatDisabledAnnouncement else "التطبيق تحت الصيانة الفنية الطارئة لتحسين الخدمات، سنعود قريباً.")
    }
    var emergencyFreezeOrders by remember { mutableStateOf(true) }
    var emergencyLockoutChat by remember(settingsState.disableChatAll) { mutableStateOf(settingsState.disableChatAll) }
    var adminBypassActive by remember { mutableStateOf(true) }
    var selectedDuration by remember { mutableStateOf("حتى الإشعار اليدوي") }
    var emergencyPhone by remember(settingsState.supportPhone) {
        mutableStateOf(if (settingsState.supportPhone.isNotBlank()) settingsState.supportPhone else "777644")
    }

    val durations = listOf("30 دقيقة", "ساعة واحدة", "ساعتين", "4 ساعات", "حتى الإشعار اليدوي")

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // بطاقة التحكم الرئيسية مع Checkbox وزر التفعيل
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isMaint) Color(0xFF450A0A) else themeColors.surface),
            border = BorderStroke(2.dp, if (isMaint) Color.Red else Color(0xFF10B981)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isMaint) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isMaint) Color.Red else Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "🚧 مركز إدارة وضع الصيانة والطوارئ",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent
                        )
                    }
                    AdminStatusBadge(
                        text = if (isMaint) "🚨 صيانة نشطة" else "✅ تشغيل طبيعي",
                        containerColor = if (isMaint) Color.Red else Color(0xFF10B981)
                    )
                }

                // خانة اختيار (Checkbox) لتأكيد حالة التفعيل أو الإيقاف بصرياً وبشكل دقيق
                Surface(
                    color = if (isMaint) Color.Red.copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isMaint) Color.Red.copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newStatus = !isMaint
                                isMaint = newStatus
                                val st = settingsState
                                viewModel.updateBackdoorSettings(
                                    st.appName, st.welcomeMessage, st.footerMessage, st.activeThemeId,
                                    st.supportPhone, st.supportEmail, st.supportWhatsapp,
                                    newStatus, st.hidePromoFooter, st.assistantHidden, st.assistantSize,
                                    st.chatHidden, st.chatSize, st.maxSearchRadiusKm, st.isSpeechSearchEnabled,
                                    false, 90
                                )
                                viewModel.db.collection("settings").document("main_settings").update(
                                    mapOf(
                                        "isMaintenanceActive" to newStatus,
                                        "chatDisabledAnnouncement" to emergencyMessage
                                    )
                                )
                                Toast.makeText(context, if (newStatus) "🚨 تم تفعيل وضع الصيانة فوراً لجميع الأجهزة!" else "🟢 تم إيقاف وضع الصيانة وإعادة فتح التطبيق للعملاء!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isMaint,
                            onCheckedChange = { checked ->
                                isMaint = checked
                                val st = settingsState
                                viewModel.updateBackdoorSettings(
                                    st.appName, st.welcomeMessage, st.footerMessage, st.activeThemeId,
                                    st.supportPhone, st.supportEmail, st.supportWhatsapp,
                                    checked, st.hidePromoFooter, st.assistantHidden, st.assistantSize,
                                    st.chatHidden, st.chatSize, st.maxSearchRadiusKm, st.isSpeechSearchEnabled,
                                    false, 90
                                )
                                viewModel.db.collection("settings").document("main_settings").update(
                                    mapOf(
                                        "isMaintenanceActive" to checked,
                                        "chatDisabledAnnouncement" to emergencyMessage
                                    )
                                )
                                Toast.makeText(context, if (checked) "🚨 تم تفعيل وضع الصيانة فوراً!" else "🟢 تم إيقاف وضع الصيانة بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color.Red, uncheckedColor = Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isMaint) "وضع الصيانة مفعل (انقر أو أزل التأشير للإلغاء)" else "وضع الصيانة متوقف (انقر أو ضع علامة للتفعيل)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMaint) Color.Red else Color(0xFF10B981)
                            )
                            Text(
                                text = "خانة الاختيار تؤكد حالة التفعيل والإيقاف بصرياً ومزامنة فورية مع قاعدة البيانات سحابياً",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                // زر التحويل الفوري
                Button(
                    onClick = {
                        val newStatus = !isMaint
                        isMaint = newStatus
                        val st = settingsState
                        viewModel.updateBackdoorSettings(
                            st.appName, st.welcomeMessage, st.footerMessage, st.activeThemeId,
                            st.supportPhone, st.supportEmail, st.supportWhatsapp,
                            newStatus, st.hidePromoFooter, st.assistantHidden, st.assistantSize,
                            st.chatHidden, st.chatSize, st.maxSearchRadiusKm, st.isSpeechSearchEnabled,
                            false, 90
                        )
                        viewModel.db.collection("settings").document("main_settings").update(
                            mapOf(
                                "isMaintenanceActive" to newStatus,
                                "chatDisabledAnnouncement" to emergencyMessage
                            )
                        )
                        Toast.makeText(context, if (newStatus) "🚨 تم تفعيل وضع الصيانة فوراً!" else "🟢 تم إيقاف وضع الصيانة وإعادة فتح التطبيق للعملاء!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isMaint) Color(0xFF10B981) else Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(if (isMaint) Icons.Default.CheckCircle else Icons.Default.Warning, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMaint) "🟢 إيقاف وضع الصيانة وإعادة فتح التطبيق للجميع" else "🔴 تفعيل وضع الصيانة والطوارئ فوراً 🛑",
                        fontSize = 12.5.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // بطاقة صلاحيات وإعدادات الطوارئ الشاملة
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("⚡ إعدادات الطوارئ والتحكم الشامل", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                OutlinedTextField(
                    value = emergencyMessage,
                    onValueChange = { emergencyMessage = it },
                    label = { Text("رسالة الصيانة والطوارئ للعملاء") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = emergencyPhone,
                    onValueChange = { emergencyPhone = it },
                    label = { Text("هاتف طوارئ الدعم الفني أثناء الصيانة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Text("المدة التقديرية لإنجاز الصيانة:", fontSize = 11.5.sp, color = Color.LightGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(durations) { dur ->
                        val isSelected = dur == selectedDuration
                        Surface(
                            color = if (isSelected) themeColors.accent else Color(0xFF1E293B),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isSelected) themeColors.accent else Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.clickable { selectedDuration = dur }
                        ) {
                            Text(dur, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("تجميد الحجوزات والطلبات الجديدة مؤقتاً", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("منع العملاء من إرسال طلبات جديدة أثناء حالة الطوارئ", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(checked = emergencyFreezeOrders, onCheckedChange = { emergencyFreezeOrders = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("تعطيل المحادثات المباشرة فوراً", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("إيقاف غرف الشات مؤقتاً لتخفيف الحمل على السيرفرات", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(checked = emergencyLockoutChat, onCheckedChange = { emergencyLockoutChat = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("استثناء المشرفين والإدارة من الحظر", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("السماح للأدمن فقط بفحص المنصة وتجربتها أثناء الصيانة", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(checked = adminBypassActive, onCheckedChange = { adminBypassActive = it })
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.triggerNotification("🚨 تنبيه صيانة: $emergencyMessage")
                            Toast.makeText(context, "📢 تم بث إشعار الطوارئ لكافة المستخدمين بنجاح!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بث إشعار طوارئ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.db.collection("settings").document("main_settings").update(
                                mapOf(
                                    "chatDisabledAnnouncement" to emergencyMessage,
                                    "supportPhone" to emergencyPhone,
                                    "disableChatAll" to (isMaint && emergencyLockoutChat),
                                    "emergencyFreezeOrders" to emergencyFreezeOrders,
                                    "adminBypassActive" to adminBypassActive,
                                    "maintenanceDuration" to selectedDuration
                                )
                            )
                            Toast.makeText(context, "💾 تم حفظ وتحديث كافة إعدادات الطوارئ سحابياً!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ سحابياً", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun AdminMaintenanceScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = AdminMaintenanceScreenContent(viewModel, themeColors, modifier)

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.admin
import com.serviseyem.ui.*

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.serviseyem.utils.PermissionGuard
import com.serviseyem.utils.RoleManager
import com.serviseyem.utils.VisualThemePalette

@Composable
fun AdminQuickServicePanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val adminRoleStr by viewModel.adminRole.collectAsState()
    val supervisorPermissions by viewModel.currentSupervisorPermissions.collectAsState()
    if (!PermissionGuard.hasPermission(
            role = RoleManager.fromRoleString(adminRoleStr),
            permission = PermissionGuard.PERMISSION_QUICK_SERVICE,
            supervisorGrantedPermissions = supervisorPermissions
        )
    ) {
        PermissionGuard.UnauthorizedView()
        return
    }

    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    var isQuickServiceActive by remember { mutableStateOf(true) }
    var quickServiceTitle by remember { mutableStateOf("اطلب خدمتك الفورية ⚡") }
    var quickServiceDescription by remember { mutableStateOf("أرسل طلبك وسيصلك أقرب مقدم خدمة معتمد في دقائق") }

    var allowAudioRecording by remember { mutableStateOf(true) }
    var allowImageUpload by remember { mutableStateOf(true) }
    var allowLiveLocation by remember { mutableStateOf(true) }

    var maxImagesAllowed by remember { mutableStateOf("3") }
    var maxAudioSeconds by remember { mutableStateOf("60") }

    var broadcastRadiusKm by remember { mutableStateOf("10") }
    var defaultResponseWindowMinutes by remember { mutableStateOf("5") }

    LaunchedEffect(Unit) {
        viewModel.db.collection("settings").document("quick_service").get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    isQuickServiceActive = doc.getBoolean("isQuickServiceActive") ?: true
                    quickServiceTitle = doc.getString("quickServiceTitle") ?: "اطلب خدمتك الفورية ⚡"
                    quickServiceDescription = doc.getString("quickServiceDescription") ?: "أرسل طلبك وسيصلك أقرب مقدم خدمة معتمد في دقائق"
                    allowAudioRecording = doc.getBoolean("allowAudioRecording") ?: true
                    allowImageUpload = doc.getBoolean("allowImageUpload") ?: true
                    allowLiveLocation = doc.getBoolean("allowLiveLocation") ?: true
                    maxImagesAllowed = doc.getString("maxImagesAllowed") ?: "3"
                    maxAudioSeconds = doc.getString("maxAudioSeconds") ?: "60"
                    broadcastRadiusKm = doc.getString("broadcastRadiusKm") ?: "10"
                    defaultResponseWindowMinutes = doc.getString("defaultResponseWindowMinutes") ?: "5"
                }
            }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = themeColors.accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚡ تخصيص استمارة اطلب خدمتك الفورية",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Switch(
                    checked = isQuickServiceActive,
                    onCheckedChange = { isQuickServiceActive = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = themeColors.accent,
                        checkedTrackColor = themeColors.primary
                    )
                )
            }

            Text(
                text = "التحكم في نموذج الطلب السريع، تفعيل التسجيلات الصوتية، الصور، الرادار الجغرافي، ونطاق البث الفوري:",
                fontSize = 11.sp,
                color = themeColors.textSecondary
            )

            Divider(color = Color.DarkGray)

            OutlinedTextField(
                value = quickServiceTitle,
                onValueChange = { quickServiceTitle = it },
                label = { Text("العنوان الرئيسي لاستمارة الطلب السريع") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            OutlinedTextField(
                value = quickServiceDescription,
                onValueChange = { quickServiceDescription = it },
                label = { Text("الوصف التوجيهي للعميل") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Divider(color = Color.DarkGray)

            Text("🎙️ والوسائط المسموح بها في الطلب:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تفعيل التسجيل الصوتي للطلب", color = Color.White, fontSize = 11.sp)
                Switch(
                    checked = allowAudioRecording,
                    onCheckedChange = { allowAudioRecording = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تفعيل إرفاق صور المشكلة / الطلب", color = Color.White, fontSize = 11.sp)
                Switch(
                    checked = allowImageUpload,
                    onCheckedChange = { allowImageUpload = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تفعيل التحديد التلقائي للموقع (GPS)", color = Color.White, fontSize = 11.sp)
                Switch(
                    checked = allowLiveLocation,
                    onCheckedChange = { allowLiveLocation = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                )
            }

            Divider(color = Color.DarkGray)

            Text("📡 نطاق الرادار والبث الجغرافي:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = broadcastRadiusKm,
                    onValueChange = { broadcastRadiusKm = it },
                    label = { Text("نصف قطر البث (كم)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = defaultResponseWindowMinutes,
                    onValueChange = { defaultResponseWindowMinutes = it },
                    label = { Text("نافذة الاستجابة (دقيقة)") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            Button(
                onClick = {
                    if (isSaving) return@Button
                    isSaving = true
                    val data = mapOf<String, Any>(
                        "isQuickServiceActive" to isQuickServiceActive,
                        "quickServiceTitle" to quickServiceTitle.trim(),
                        "quickServiceDescription" to quickServiceDescription.trim(),
                        "allowAudioRecording" to allowAudioRecording,
                        "allowImageUpload" to allowImageUpload,
                        "allowLiveLocation" to allowLiveLocation,
                        "maxImagesAllowed" to maxImagesAllowed.trim(),
                        "maxAudioSeconds" to maxAudioSeconds.trim(),
                        "broadcastRadiusKm" to broadcastRadiusKm.trim(),
                        "defaultResponseWindowMinutes" to defaultResponseWindowMinutes.trim(),
                        "updatedAt" to System.currentTimeMillis()
                    )
                    viewModel.db.collection("settings").document("quick_service")
                        .set(data)
                        .addOnSuccessListener {
                            isSaving = false
                            viewModel.triggerNotification("✅ تم حفظ وتحديث إعدادات الطلب السريع بنجاح")
                            Toast.makeText(context, "✅ تم حفظ إعدادات اطلب خدمتك بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { err ->
                            isSaving = false
                            Toast.makeText(context, "❌ فشل حفظ الإعدادات: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("جاري الحفظ...", color = Color.White)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ التغييرات", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

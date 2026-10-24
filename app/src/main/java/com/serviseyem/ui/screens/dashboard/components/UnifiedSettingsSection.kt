package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

@Composable
fun UnifiedSettingsSection(
    name: String,
    phone: String,
    cityArea: String,
    description: String,
    workingHours: String = "",
    isAvailable: Boolean = true,
    themeColors: VisualThemePalette,
    onSaveProfile: (name: String, phone: String, cityArea: String, description: String, workingHours: String, isAvailable: Boolean) -> Unit,
    onChangePassword: (oldPass: String, newPass: String) -> Unit
) {
    var nameInput by remember(name) { mutableStateOf(name) }
    var phoneInput by remember(phone) { mutableStateOf(phone) }
    var cityAreaInput by remember(cityArea) { mutableStateOf(cityArea) }
    var descInput by remember(description) { mutableStateOf(description) }
    var hoursInput by remember(workingHours) { mutableStateOf(workingHours) }
    var availableState by remember(isAvailable) { mutableStateOf(isAvailable) }

    var oldPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Business / Profile Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "📝 تعديل البيانات العامة والمهنية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("الاسم / اسم المنشأة") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColors.accent)
                )

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { /* Read only */ },
                    label = { Text("رقم الهاتف المسجل (غير قابل للتعديل 🔒)") },
                    enabled = false,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                        disabledTextColor = Color.LightGray,
                        disabledLabelColor = Color.Gray
                    )
                )

                OutlinedTextField(
                    value = cityAreaInput,
                    onValueChange = { cityAreaInput = it },
                    label = { Text("المدينة والحي") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColors.accent)
                )

                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("الوصف التعريفي / التخصص") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColors.accent)
                )

                if (workingHours.isNotBlank() || true) {
                    OutlinedTextField(
                        value = hoursInput,
                        onValueChange = { hoursInput = it },
                        label = { Text("ساعات أوقات العمل (مثال: 8 ص - 10 م)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColors.accent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (availableState) "حالة التوفر: متاح لاستقبال الطلبات 🟢" else "حالة التوفر: مشغول / غير متاح 🔴",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (availableState) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                    Switch(
                        checked = availableState,
                        onCheckedChange = { availableState = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                    )
                }

                Button(
                    onClick = {
                        onSaveProfile(nameInput, phoneInput, cityAreaInput, descInput, hoursInput, availableState)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ التغيرات 💾", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        // Change Password Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "🔒 تغيير كلمة المرور", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                OutlinedTextField(
                    value = oldPasswordInput,
                    onValueChange = { oldPasswordInput = it },
                    label = { Text("كلمة المرور الحالية") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPasswordInput,
                    onValueChange = { newPasswordInput = it },
                    label = { Text("كلمة المرور الجديدة") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (oldPasswordInput.isNotBlank() && newPasswordInput.isNotBlank()) {
                            onChangePassword(oldPasswordInput, newPasswordInput)
                            oldPasswordInput = ""
                            newPasswordInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = oldPasswordInput.isNotBlank() && newPasswordInput.isNotBlank()
                ) {
                    Text("تحديث كلمة المرور 🔑", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

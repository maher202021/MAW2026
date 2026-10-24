package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

@Composable
fun AdvancedAccountSettingsScreen(
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoAvailability by remember { mutableStateOf(true) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("إعدادات الحساب المتقدمة ⚙️", fontSize = 18.sp, color = themeColors.primary)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("تغيير كلمة المرور", fontSize = 14.sp, color = themeColors.primary)
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("كلمة المرور الحالية") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("كلمة المرور الجديدة") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { /* Save password logic */ },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary)
                ) {
                    Text("تحديث كلمة المرور")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("تفضيلات الإشعارات والتوفر", fontSize = 14.sp, color = themeColors.primary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تفعيل الإشعارات الفورية", fontSize = 13.sp)
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("التوفر التلقائي للطلبات", fontSize = 13.sp)
                    Switch(checked = autoAvailability, onCheckedChange = { autoAvailability = it })
                }
            }
        }

        OutlinedButton(
            onClick = { showDeleteConfirm = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("حذف الحساب نهائياً")
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("تأكيد حذف الحساب") },
                text = { Text("هل أنت متأكد من رغبتك في حذف الحساب نهائياً؟ لا يمكن التراجع عن هذا الإجراء.") },
                confirmButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("إلغاء") }
                }
            )
        }
    }
}

package com.serviseyem.ui.screens.admin.permissions

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

@Composable
fun AdminRoleSelectorSection(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rolesList = listOf(
        "OWNER" to "👑 المالك العام (Owner)",
        "ADMIN" to "🛡️ مدير النظام (Admin)",
        "SUPERVISOR" to "👔 المشرف العام (Supervisor)",
        "AUDITOR" to "🔍 المدقق والمراقب (Auditor)",
        "SUPPORT" to "🎧 الدعم الفني (Support)",
        "OPERATIONS" to "⚡ مدير العمليات (Operations)"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "👤 اختر الدور الإداري لتخصيص صلاحياته:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.accent
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rolesList.forEach { (roleKey, roleLabel) ->
                    val isSelected = selectedRole == roleKey
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onRoleSelected(roleKey)
                            Toast.makeText(context, "تم تحميل حزمة صلاحيات $roleLabel", Toast.LENGTH_SHORT).show()
                        },
                        label = {
                            Text(
                                text = roleLabel,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.accent,
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

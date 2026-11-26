package com.serviseyem.ui.dialogs

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.utils.HapticFeedbackHelper
import com.serviseyem.utils.RoadHazardManager
import kotlinx.coroutines.launch

@Composable
fun ReportHazardDialog(
    latitude: Double,
    longitude: Double,
    onDismiss: () -> Unit,
    onHazardReported: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedType by remember { mutableStateOf("ROAD_BLOCK") }
    var notesText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val hazardOptions = listOf(
        Triple("ROAD_BLOCK", "طريق مقطوع 🚫", "تحويلة إجبارية أو إغلاق تام"),
        Triple("FLOOD", "سيول ومياه جارية 🌊", "تجمعات مياه وأمطار خطرة"),
        Triple("ACCIDENT", "حادث سير 💥", "حادث مروري يعيق حركة السير"),
        Triple("TRAFFIC", "ازدحام شديد 🚗", "توقف حركة السير أو بطء خانق"),
        Triple("POTHOLE", "حفر وأضرار طريق ⚠️", "أضرار إسفلتية أو هبوط أرضي")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "📢 إبلاغ مجتمعي عن عائق في الطريق",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "سيظهر هذا التنبيه للسائقين القريبين من هذا الموقع لتجنب الطريق واختيار مسار آمن.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    hazardOptions.forEach { (typeKey, title, desc) ->
                        val isSelected = selectedType == typeKey
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0B132B),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    HapticFeedbackHelper.tick(context)
                                    selectedType = typeKey
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = desc,
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        HapticFeedbackHelper.tick(context)
                                        selectedType = typeKey
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E5FF))
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("ملاحظات إضافية (اختياري)...", fontSize = 12.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    maxLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            isSubmitting = true
                            HapticFeedbackHelper.success(context)
                            coroutineScope.launch {
                                val selectedTitle = hazardOptions.firstOrNull { it.first == selectedType }?.second ?: "تنبيه طريق"
                                RoadHazardManager.reportHazard(
                                    context = context,
                                    type = selectedType,
                                    latitude = latitude,
                                    longitude = longitude,
                                    title = selectedTitle,
                                    description = notesText.trim()
                                )
                                isSubmitting = false
                                Toast.makeText(context, "شكراً لمساهمتك! تم نشر التنبيه للسائقين بنجاح 👍", Toast.LENGTH_LONG).show()
                                onHazardReported()
                                onDismiss()
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                        } else {
                            Text("تأكيد ونشر التنبيه", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp),
                        border = BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Text("إلغاء", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

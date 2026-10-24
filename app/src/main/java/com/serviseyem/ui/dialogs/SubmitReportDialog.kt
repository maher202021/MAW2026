package com.serviseyem.ui.dialogs

import android.content.Context
import com.serviseyem.ui.*
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.ReportEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import java.util.UUID

/**
 * 🚨 نظام تقديم البلاغات والشكاوى
 * يتيح للعملاء والمستخدمين رفع شكوى مدعومة بالأدلة ومتابعة حالتها
 */
@Composable
fun SubmitReportDialog(
    targetId: String,
    targetName: String,
    targetType: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUserName by viewModel.currentUserName.collectAsState()
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()

    var reporterName by remember(currentUserName) { mutableStateOf(currentUserName) }
    var reporterPhone by remember(currentUserPhone) { mutableStateOf(currentUserPhone) }
    var selectedReason by remember { mutableStateOf("سلوك غير لائق أو إخلال بالمواعيد") }
    var explanation by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    val reportReasons = listOf(
        "سلوك غير لائق أو إخلال بالمواعيد",
        "عدم مطابقة الخدمة أو البضاعة للمواصفات",
        "أسعار مبالغ فيها أو احتيال",
        "معلومات وهمية أو حساب منتحل",
        "أخرى"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        Text(
                            text = "تقديم بلاغ / شكوى رسمية",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.LightGray)
                    }
                }

                if (!isSubmitted) {
                    Text(
                        text = "البلاغ موجه ضد: $targetName ($targetType)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.accent
                    )

                    OutlinedTextField(
                        value = reporterName,
                        onValueChange = { reporterName = it },
                        label = { Text("اسم مقدم الشكوى", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = reporterPhone,
                        onValueChange = { reporterPhone = it },
                        label = { Text("رقم هاتفك للتواصل ومتابعة الشكوى", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("سبب البلاغ:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    // Reasons Dropdown / Selection
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        reportReasons.forEach { reason ->
                            val isSel = selectedReason == reason
                            Surface(
                                color = if (isSel) Color(0xFFEF4444).copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFFEF4444) else Color.Gray.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedReason = reason }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSel,
                                        onClick = { selectedReason = reason },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEF4444))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(reason, fontSize = 11.sp, color = if (isSel) Color.White else Color.LightGray)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = explanation,
                        onValueChange = { explanation = it },
                        label = { Text("تفاصيل الشرح وما حدث بدقة", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Button(
                        onClick = {
                            if (explanation.isNotBlank()) {
                                val reportObj = ReportEntity(
                                    id = "rep_${UUID.randomUUID().toString().take(8)}",
                                    targetId = targetId,
                                    targetName = targetName,
                                    targetType = targetType,
                                    providerId = targetId,
                                    providerName = targetName,
                                    reporterName = reporterName.ifEmpty { "عميل" },
                                    reporterPhone = reporterPhone,
                                    reason = selectedReason,
                                    explanation = explanation,
                                    content = "[$selectedReason] $explanation",
                                    status = "PENDING",
                                    timestamp = System.currentTimeMillis()
                                )
                                viewModel.submitReport(reportObj) {
                                    isSubmitted = true
                                }
                            } else {
                                Toast.makeText(context, "⚠️ يرجى كتابة تفاصيل الشكوى", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("رفع الشكوى للإدارة الآن 🚨", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(44.dp))
                        Text("تم تسجيل البلاغ وسريان التحقيق الرسمي!", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White, textAlign = TextAlign.Center)
                        Text("تم تحويل البلاغ إلى قسم الرقابة والجودة لاتخاذ الإجراء القانوني اللازم وحماية حقوقك.", fontSize = 11.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إغلاق", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

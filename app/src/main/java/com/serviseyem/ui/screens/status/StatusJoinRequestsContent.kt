package com.serviseyem.ui.screens.status

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.PendingProviderEntity
import com.serviseyem.utils.VisualThemePalette

/**
 * 📝 StatusJoinRequestsContent
 * Manages provider and business join request approvals/rejections.
 */
@Composable
fun StatusJoinRequestsContent(
    requests: List<PendingProviderEntity>,
    themeColors: VisualThemePalette,
    onApprove: (PendingProviderEntity) -> Unit,
    onReject: (PendingProviderEntity, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var rejectingRequest by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<PendingProviderEntity?>(null) }
    var rejectionReason by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    if (requests.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨ لا توجد طلبات انضمام جديدة معلقة حالياً",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = themeColors.textSecondary
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "📝 طلبات انضمام الفنيين والمزودين (${requests.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
            }

            items(requests, key = { it.id }) { req ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = themeColors.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, themeColors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = req.name.ifBlank { "مقدم خدمة جديد" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "قيد المراجعة ⏳",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "📍 ${req.area.ifBlank { "غير محدد" }}",
                                fontSize = 12.sp,
                                color = themeColors.textSecondary
                            )
                            Text(
                                text = "🛠️ ${req.categoryId.ifBlank { "خدمات عامة" }}",
                                fontSize = 12.sp,
                                color = themeColors.textSecondary
                            )
                        }

                        if (req.phone.isNotBlank()) {
                            Text(
                                text = "📞 ${req.phone}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeColors.textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onApprove(req) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("موافقة وقبول ✅", fontSize = 12.sp, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = {
                                    rejectingRequest = req
                                    rejectionReason = ""
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("رفض الطلب ❌", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    val targetReject = rejectingRequest
    if (targetReject != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { rejectingRequest = null },
            title = { Text("سبب رفض طلب الانضمام", fontWeight = FontWeight.Bold) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = rejectionReason,
                    onValueChange = { rejectionReason = it },
                    label = { Text("سبب الرفض (اختياري)") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(targetReject, rejectionReason.trim().ifBlank { "غير مستوفي للشروط" })
                        rejectingRequest = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("تأكيد الرفض", color = Color.White)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { rejectingRequest = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

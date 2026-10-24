package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

@Composable
fun UnifiedBookingsSection(
    bookingId: String,
    clientName: String,
    clientPhone: String,
    serviceTitle: String,
    dateString: String,
    timeString: String,
    status: String,
    rejectionReason: String = "",
    themeColors: VisualThemePalette,
    onAcceptClick: (() -> Unit)? = null,
    onRejectClick: ((reason: String) -> Unit)? = null,
    onStartProgressClick: (() -> Unit)? = null,
    onCompleteClick: (() -> Unit)? = null,
    onChatClick: (() -> Unit)? = null,
    onUpdateOrderStatus: ((newStatus: String) -> Unit)? = null
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReasonInput by remember { mutableStateOf("") }

    val statusColor = when (status) {
        "APPROVED" -> Color(0xFF10B981)
        "REJECTED", "CANCELLED" -> Color(0xFFEF4444)
        "IN_PROGRESS", "IN_PREPARATION" -> Color(0xFF3B82F6)
        "COMPLETED", "READY", "DELIVERED" -> Color(0xFF10B981)
        else -> Color(0xFFF59E0B)
    }

    val statusLabel = when (status) {
        "APPROVED" -> "مقبول ✅"
        "REJECTED" -> "مرفوض ❌"
        "CANCELLED" -> "ملغى 🚫"
        "IN_PROGRESS" -> "قيد التنفيذ ⚙️"
        "IN_PREPARATION" -> "قيد التجهيز 🛒"
        "READY" -> "جاهز للتسليم 🎁"
        "DELIVERED" -> "تم التسليم 🚚"
        "COMPLETED" -> "مكتمل 🏁"
        else -> "قيد الانتظار ⏳"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 ${clientName.ifBlank { "عميل" }}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (serviceTitle.isNotBlank()) {
                Text(text = "🛠️ الخدمة/الطلب: $serviceTitle", fontSize = 12.sp, color = themeColors.textSecondary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (clientPhone.isNotBlank()) {
                    Text(text = "📞 $clientPhone", fontSize = 11.sp, color = themeColors.accent)
                }
                if (dateString.isNotBlank() || timeString.isNotBlank()) {
                    Text(text = "📅 $dateString $timeString", fontSize = 11.sp, color = themeColors.textSecondary)
                }
            }

            if (rejectionReason.isNotBlank()) {
                Text(text = "سبب الرفض: $rejectionReason", fontSize = 11.sp, color = Color(0xFFEF4444))
            }

            val isPending = status == com.serviseyem.utils.BookingStatus.PENDING.code
            val isAccepted = status == "APPROVED" || status == com.serviseyem.utils.BookingStatus.ACCEPTED.code
            val isInProgress = status == com.serviseyem.utils.BookingStatus.IN_PROGRESS.code

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✨ م2: استخدام الـ Enums الموحدة بدلاً من السلاسل النصية الخام
                if (isPending) {
                    if (onAcceptClick != null) {
                        Button(
                            onClick = onAcceptClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("قبول ✅", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (onRejectClick != null) {
                        Button(
                            onClick = { showRejectDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("رفض ❌", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ✨ م2: استخدام الـ Enums الموحدة بدلاً من السلاسل النصية الخام
                if (isAccepted) {
                    if (onStartProgressClick != null) {
                        Button(
                            onClick = onStartProgressClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("بدء التنفيذ ⚙️", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ✨ م2: استخدام الـ Enums الموحدة بدلاً من السلاسل النصية الخام
                if (isInProgress || isAccepted) {
                    if (onCompleteClick != null) {
                        Button(
                            onClick = onCompleteClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إكمال الخدمة 🏁", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Custom order status options for stores / restaurants / medical
                if (onUpdateOrderStatus != null) {
                    // ✨ م2: استخدام الـ Enums الموحدة بدلاً من السلاسل النصية الخام
                    if (isPending) {
                        Button(
                            onClick = { onUpdateOrderStatus("IN_PREPARATION") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("قيد التجهيز 🛒", fontSize = 11.sp, color = Color.White)
                        }
                    } else if (status == "IN_PREPARATION") {
                        Button(
                            onClick = { onUpdateOrderStatus("READY") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("جاهز للتسليم 🎁", fontSize = 11.sp, color = Color.Black)
                        }
                    } else if (status == "READY") {
                        Button(
                            onClick = { onUpdateOrderStatus("DELIVERED") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تم التسليم 🚚", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                if (onChatClick != null) {
                    IconButton(
                        onClick = onChatClick,
                        modifier = Modifier
                            .background(themeColors.accent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "💬", fontSize = 16.sp)
                    }
                }
            }
        }
    }

    if (showRejectDialog && onRejectClick != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("سبب رفض الحجز", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = rejectReasonInput,
                    onValueChange = { rejectReasonInput = it },
                    label = { Text("يرجى كتابة سبب الرفض لإبلاغ العميل...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectClick(rejectReasonInput.ifBlank { "غير متاح في الوقت الحالي" })
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("إرسال الرفض ❌", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

package com.serviseyem.ui.screens.bookings

import androidx.compose.animation.AnimatedVisibility
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.BookingEntity
import com.serviseyem.utils.BookingStateMachine
import com.serviseyem.utils.BookingUtils

/**
 * 📦 BookingCardItem
 * Card displaying individual booking details with 8-hour countdown rule indicator,
 * masked password toggle, and quick action buttons.
 */
@Composable
fun BookingCardItem(
    booking: BookingEntity,
    currentUserId: String = "",
    isAdmin: Boolean = false,
    isProvider: Boolean = false,
    onStatusChange: (BookingEntity, String) -> Unit = { _, _ -> },
    onEditClick: (BookingEntity) -> Unit,
    onCancelClick: (BookingEntity) -> Unit,
    onDeleteClick: (BookingEntity) -> Unit,
    onOpenChatClick: (BookingEntity) -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showPinVerifyDialog by remember { mutableStateOf(false) }
    var enteredPinText by remember { mutableStateOf("") }
    var verifiedPlainPin by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    val isUserTheClient = remember(booking, currentUserId, isAdmin) {
        val normalizedCurrentUser = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(currentUserId)
        val normalizedClientPhone = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(booking.clientPhone)
        val normalizedCustomerPhone = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(booking.customerPhone)
        !isAdmin && currentUserId.isNotBlank() && (
            booking.customerId == currentUserId ||
            booking.clientId == currentUserId ||
            (normalizedCurrentUser.isNotBlank() && (normalizedClientPhone == normalizedCurrentUser || normalizedCustomerPhone == normalizedCurrentUser))
        )
    }
    val canViewSensitivePin = isUserTheClient || isAdmin

    val canModifyOrCancel = BookingUtils.canModifyOrCancelBooking(
        scheduledAtTimestamp = booking.scheduledAt,
        dateString = booking.date.ifBlank { booking.dateString },
        timeString = booking.time.ifBlank { booking.timeString }
    )

    val remainingTimeText = remember(booking.scheduledAt, booking.date, booking.time) {
        BookingUtils.formatRemainingCancellationTime(
            scheduledAtTimestamp = booking.scheduledAt,
            dateString = booking.date.ifBlank { booking.dateString },
            timeString = booking.time.ifBlank { booking.timeString }
        )
    }

    val isTerminalState = booking.status in listOf("COMPLETED", "CANCELLED", "REJECTED")

    val statusColor = remember(booking.status) {
        try {
            Color(android.graphics.Color.parseColor(BookingStateMachine.getStatusColor(booking.status)))
        } catch (e: Exception) {
            Color(0xFFF59E0B)
        }
    }

    val statusText = remember(booking.status) {
        BookingStateMachine.getStatusLabel(booking.status)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booking_card_${booking.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Booking Number & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = booking.bookingNumber.ifBlank { booking.bookingCode.ifBlank { "حجز #${booking.id.take(6)}" } },
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = Color(0xFF334155), thickness = 0.8.dp)

            // Provider or Client Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (booking.providerPhoto.isNotBlank()) {
                    AsyncImage(
                        model = booking.providerPhoto,
                        contentDescription = "صورة",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                }
                val targetName = if (booking.providerName.isNotBlank()) "الفني: ${booking.providerName}" else "العميل: ${booking.customerName.ifBlank { booking.clientName }}"
                Text(
                    text = targetName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Service details
            val serviceInfo = booking.serviceType.ifBlank { booking.serviceDetails.ifBlank { booking.category } }
            if (serviceInfo.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    Text(
                        text = serviceInfo,
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                }
            }

            // Date and Time
            val dateStr = booking.date.ifBlank { booking.dateString }
            val timeStr = booking.time.ifBlank { booking.timeString }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                Text(
                    text = "$dateStr | $timeStr",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Price display if amount > 0
            if (booking.totalAmount > 0) {
                val formattedAmount = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(booking.totalAmount)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    Text(
                        text = "سعر الخدمة: $formattedAmount ريال يمني",
                        color = Color(0xFF10B981),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 8-Hour Rule Warning / Countdown Banner
            if (!isTerminalState) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (canModifyOrCancel) Color(0xFF0F766E).copy(alpha = 0.2f) else Color(0xFF7F1D1D).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (canModifyOrCancel) Color(0xFF14B8A6).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            if (canModifyOrCancel) Icons.Default.Info else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (canModifyOrCancel) Color(0xFF14B8A6) else Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = remainingTimeText,
                            fontSize = 11.sp,
                            color = if (canModifyOrCancel) Color(0xFF5EEAD4) else Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Password / PIN Display (Masked toggle - strictly visible only to booking owner or admin)
            val pass = booking.effectivePin
            if (pass.isNotBlank() && !isTerminalState && canViewSensitivePin) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                            Text(
                                text = "رمز أمان الحجز (PIN):",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = if (isPasswordVisible && verifiedPlainPin.isNotBlank()) {
                                    verifiedPlainPin
                                } else {
                                    "••••"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isPasswordVisible) {
                                    isPasswordVisible = false
                                    verifiedPlainPin = ""
                                } else {
                                    showPinVerifyDialog = true
                                    enteredPinText = ""
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Close else Icons.Default.Check,
                                contentDescription = "تبديل الرؤية",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Secure PIN Verification Dialog
            if (showPinVerifyDialog) {
                AlertDialog(
                    onDismissRequest = { showPinVerifyDialog = false },
                    title = { Text("🔒 أدخل رمز الأمان (PIN)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "يرجى كتابة رمز الأمان المكون من 4 أرقام لتأكيد الهوية وعرض الرمز الأصلي للحجز:",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            OutlinedTextField(
                                value = enteredPinText,
                                onValueChange = { if (it.length <= 6) enteredPinText = it },
                                placeholder = { Text("مثال: 1234", fontSize = 12.sp, color = Color(0xFF64748B)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color(0xFF334155)
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val targetHash = booking.effectivePin
                                val isVerified = com.serviseyem.utils.SecureHasher.verifyPin(enteredPinText, targetHash)
                                if (isVerified) {
                                    verifiedPlainPin = enteredPinText
                                    isPasswordVisible = true
                                    showPinVerifyDialog = false
                                    android.widget.Toast.makeText(context, "✅ تم التحقق من الرمز بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "❌ رمز الأمان (PIN) غير صحيح!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Text("تحقق ✅", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPinVerifyDialog = false }) {
                            Text("إلغاء ❌", color = Color(0xFF94A3B8))
                        }
                    },
                    containerColor = Color(0xFF1E293B)
                )
            }

            // Action Buttons based on Role
            if (isAdmin) {
                // Admin Actions: Full Control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onOpenChatClick(booking) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("محادثة", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onEditClick(booking) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("تعديل", fontSize = 11.sp)
                    }

                    if (!isTerminalState) {
                        Button(
                            onClick = { onCancelClick(booking) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("إلغاء", fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = { onDeleteClick(booking) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("حذف", fontSize = 11.sp)
                        }
                    }
                }
            } else if (!isUserTheClient) {
                // Provider / Technician Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onOpenChatClick(booking) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("العميل", fontSize = 11.sp)
                    }

                    when (booking.status) {
                        "PENDING" -> {
                            Button(
                                onClick = { onStatusChange(booking, "APPROVED") },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.weight(1.2f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("قبول الحجز", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onStatusChange(booking, "REJECTED") },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("اعتذار", fontSize = 11.sp)
                            }
                        }
                        "APPROVED" -> {
                            Button(
                                onClick = { onStatusChange(booking, "IN_PROGRESS") },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                modifier = Modifier.weight(1.5f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("بدء تنفيذ الخدمة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        "IN_PROGRESS" -> {
                            Button(
                                onClick = { onStatusChange(booking, "COMPLETED") },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                modifier = Modifier.weight(1.5f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("إكمال وتسليم الخدمة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Client / Customer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onOpenChatClick(booking) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("محادثة", fontSize = 11.sp)
                    }

                    if (!isTerminalState) {
                        if (booking.status == "IN_PROGRESS") {
                            Button(
                                onClick = { onStatusChange(booking, "COMPLETED") },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.weight(1.2f),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("تأكيد إكمال", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { onEditClick(booking) },
                            enabled = canModifyOrCancel,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("تعديل", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { onCancelClick(booking) },
                            enabled = canModifyOrCancel,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("إلغاء", fontSize = 11.sp)
                        }
                    } else {
                        // For terminal states (completed/cancelled), client can delete the card locally or from list
                        Button(
                            onClick = { onDeleteClick(booking) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("حذف من السجل", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

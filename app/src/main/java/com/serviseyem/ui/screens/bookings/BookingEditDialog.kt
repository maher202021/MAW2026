package com.serviseyem.ui.screens.bookings

import android.app.DatePickerDialog
import com.serviseyem.ui.*
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.BookingEntity
import com.serviseyem.security.BookingSecurityHelper
import com.serviseyem.utils.BookingUtils
import java.util.Calendar
import java.util.Locale

/**
 * ✏️ BookingEditDialog
 * Edit dialog with real-time 8-hour rule validation, PIN verification,
 * and 3-attempt 5-minute lockout security protection.
 */
@Composable
fun BookingEditDialog(
    booking: BookingEntity,
    isAdmin: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmEdit: (updatedBooking: BookingEntity, passwordInput: String) -> Unit
) {
    val context = LocalContext.current

    var fullName by remember { mutableStateOf(booking.fullName.ifBlank { booking.customerName.ifBlank { booking.clientName } }) }
    var fullAddress by remember { mutableStateOf(booking.fullAddress.ifBlank { booking.customerArea.ifBlank { booking.clientAddress } }) }
    var selectedDate by remember { mutableStateOf(booking.date.ifBlank { booking.dateString }) }
    var selectedTime by remember { mutableStateOf(booking.time.ifBlank { booking.timeString }) }
    var serviceDetails by remember { mutableStateOf(booking.serviceDetails.ifBlank { booking.serviceType }) }
    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLocked = remember(booking.id) {
        BookingSecurityHelper.isBookingLocked(context, booking.id)
    }

    val canModify = BookingUtils.canModifyOrCancelBooking(
        scheduledAtTimestamp = booking.scheduledAt,
        dateString = selectedDate,
        timeString = selectedTime
    )

    val remainingText = remember(booking.scheduledAt, selectedDate, selectedTime) {
        BookingUtils.formatRemainingCancellationTime(booking.scheduledAt, selectedDate, selectedTime)
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val amPm = if (hourOfDay < 12) "ص" else "م"
            val displayHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
            selectedTime = String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✏️ تعديل تفاصيل الحجز",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF94A3B8))
                    }
                }

                // 8-hour policy notice
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (canModify) Color(0xFF0F766E).copy(alpha = 0.2f) else Color(0xFF7F1D1D).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (canModify) Color(0xFF14B8A6).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "قاعدة الـ 8 ساعات للتعديل والإلغاء:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canModify) Color(0xFF5EEAD4) else Color(0xFFFCA5A5)
                        )
                        Text(
                            text = remainingText,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                if (isLocked && !isAdmin) {
                    val remainingSec = BookingSecurityHelper.getRemainingLockoutSeconds(context, booking.id)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🔒 تم قفل الحجز مؤقتاً بسبب 3 محاولات غير صحيحة. يرجى الانتظار (${remainingSec / 60} دقيقة).",
                            color = Color(0xFFFCA5A5),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Customer Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("الاسم الكامل", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Date Picker trigger
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (selectedDate.isNotBlank()) selectedDate else "تحديد التاريخ")
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF00E5FF))
                    }
                }

                // Time Picker trigger
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (selectedTime.isNotBlank()) selectedTime else "تحديد الوقت")
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFF00E5FF))
                    }
                }

                // Address
                OutlinedTextField(
                    value = fullAddress,
                    onValueChange = { fullAddress = it },
                    label = { Text("العنوان / الحي / الشارع", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Service Details
                OutlinedTextField(
                    value = serviceDetails,
                    onValueChange = { serviceDetails = it },
                    label = { Text("تفاصيل الخدمة المطلوبة", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                // Password / PIN Field for Verification
                if (!isAdmin) {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it; errorMessage = null },
                        label = { Text("رمز PIN أو كلمة مرور الحجز (4 أرقام)", color = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                errorMessage?.let { msg ->
                    Text(text = msg, color = Color(0xFFEF4444), fontSize = 12.sp)
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (!canModify && !isAdmin) {
                                errorMessage = "لا يمكن التعديل إذا تبقى أقل من 8 ساعات على الموعد."
                                return@Button
                            }
                            if (passwordInput.isBlank() && !isAdmin) {
                                errorMessage = "يرجى إدخال رمز PIN للحجز"
                                return@Button
                            }

                            if (!isAdmin) {
                                if (BookingSecurityHelper.isBookingLocked(context, booking.id)) {
                                    errorMessage = "تم قفل الحجز مؤقتاً بسبب 3 محاولات غير صحيحة. يرجى الانتظار."
                                    return@Button
                                }
                                val targetPin = booking.pinCode.trim()
                                if (targetPin.isNotBlank()) {
                                    val isPinValid = com.serviseyem.utils.SecureHasher.verifyPin(passwordInput.trim(), targetPin) ||
                                            BookingSecurityHelper.verifyPassword(passwordInput.trim(), targetPin)
                                    if (!isPinValid) {
                                        val remaining = BookingSecurityHelper.recordFailedAttempt(context, booking.id)
                                        errorMessage = if (remaining <= 0) {
                                            "تم تجاوز المحاولات المسموحة! تم قفل الحجز لمدة 30 دقيقة."
                                        } else {
                                            "رمز PIN غير صحيح. متبقي $remaining محاولات."
                                        }
                                        return@Button
                                    } else {
                                        BookingSecurityHelper.resetAttempts(context, booking.id)
                                    }
                                }
                            }

                            val updated = booking.copy(
                                customerName = fullName,
                                fullName = fullName,
                                clientName = fullName,
                                fullAddress = fullAddress,
                                customerArea = fullAddress,
                                clientAddress = fullAddress,
                                date = selectedDate,
                                dateString = selectedDate,
                                time = selectedTime,
                                timeString = selectedTime,
                                serviceDetails = serviceDetails,
                                serviceType = serviceDetails,
                                scheduledAt = BookingUtils.parseScheduledTimestamp(selectedDate, selectedTime)
                            )
                            onConfirmEdit(updated, passwordInput)
                        },
                        enabled = (!isLocked || isAdmin),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text("حفظ التعديلات", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

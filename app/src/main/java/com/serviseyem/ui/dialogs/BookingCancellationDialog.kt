package com.serviseyem.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.BookingEntity
import com.serviseyem.utils.BookingStateMachine
import kotlinx.coroutines.isActive

/**
 * 🛑 BookingCancellationDialog
 * نافذة حوارية مؤمنة لإلغاء الحجز مع التحقق من الرمز السري وقاعدة الـ 8 ساعات وحظر المحاولات الفاشلة.
 *
 * ⚠️ ملاحظة معمارية: هذا الحوار يستخدم Firestore مباشرة لتسجيل قفل الحجز عند استنفاد المحاولات.
 * القفل الحالي يعمل بشكل صحيح وآمن ولا يحتاج تغييراً عاجلاً.
 */
@Composable
fun BookingCancellationDialog(
    booking: BookingEntity,
    userRole: String = "CLIENT", // "CLIENT", "PROVIDER", "ADMIN"
    onDismiss: () -> Unit,
    onConfirmCancel: (password: String, reason: String) -> Unit,
    viewModel: com.serviseyem.ui.viewmodels.BookingViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.serviseyem.ui.MainViewModel>().bookingViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var passwordInput by remember { mutableStateOf("") }
    var reasonInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    androidx.activity.compose.BackHandler(enabled = true, onBack = onDismiss)
    
    val isLockedInitially = remember(booking.id) { com.serviseyem.security.BookingSecurityHelper.isBookingLocked(context, booking.id) }
    var attemptsLeft by remember(booking.id) {
        val prefs = context.getSharedPreferences("booking_security_vault", android.content.Context.MODE_PRIVATE)
        val currentAttempts = prefs.getInt("attempts_" + booking.id, 0)
        mutableIntStateOf((3 - currentAttempts).coerceAtLeast(0))
    }

    var remainingSeconds by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(isLockedInitially, attemptsLeft, booking.id) {
        val isLocked = com.serviseyem.security.BookingSecurityHelper.isBookingLocked(context, booking.id)
        if (isLocked) {
            while (isActive) {
                val secs = com.serviseyem.security.BookingSecurityHelper.getRemainingLockoutSeconds(context, booking.id)
                remainingSeconds = secs
                if (secs <= 0L) {
                    attemptsLeft = 3
                    errorMessage = null
                    break
                }
                errorMessage = "تم قفل الحجز بسبب محاولات خاطئة! متبقي ${secs / 60} دقيقة و ${secs % 60} ثانية."
                kotlinx.coroutines.delay(1000L)
            }
        }
    }

    val canCancelByRule = remember(booking) { BookingStateMachine.canCancel(booking) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "تحذير",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تأكيد إلغاء الحجز",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "رقم الحجز: #${booking.bookingNumber.ifEmpty { booking.id.take(8) }}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Summary info box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "📌 الخدمة: ${booking.serviceType}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🗓️ الموعد: ${booking.effectiveDate} - ${booking.effectiveTime}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!canCancelByRule) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (booking.isLocked) "تم قفل الحجز بسبب استنفاد محاولات الإلغاء. يرجى التواصل مع الدعم."
                                else "لا يمكن إلغاء الحجز قبل الموعد بأقل من 8 ساعات حفاظاً على التزام الفني.",
                                fontSize = 12.sp,
                                color = Color(0xFFEF4444),
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    if (userRole == "CLIENT") {
                        Text(
                            text = "أدخل الرمز السري للحجز (4 أرقام):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                if (it.length <= 8) passwordInput = it
                                errorMessage = null
                            },
                            placeholder = { Text("مثال: 1234") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            shape = RoundedCornerShape(14.dp)
                        )

                        Text(
                            text = "المحاولات المتبقية: $attemptsLeft من 3",
                            fontSize = 11.sp,
                            color = if (attemptsLeft <= 1) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "سبب الإلغاء:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        placeholder = { Text("اكتب سبب الإلغاء بوضوح...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp),
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تراجع")
                    }

                    Button(
                        onClick = {
                            if (!canCancelByRule) {
                                onDismiss()
                                return@Button
                            }

                            if (userRole == "CLIENT") {
                                val expectedTarget = booking.pinCode
                                val isValid = com.serviseyem.security.BookingSecurityHelper.verifyPassword(passwordInput.trim(), expectedTarget)
                                if (!isValid) {
                                    val remaining = com.serviseyem.security.BookingSecurityHelper.recordFailedAttempt(context, booking.id)
                                    attemptsLeft = remaining
                                    if (remaining <= 0) {
                                        errorMessage = "تم قفل الحجز بعد 3 محاولات خاطئة! يرجى المحاولة بعد 30 دقيقة."
                                        viewModel.lockBookingAfterFailedAttempts(booking.id, 30 * 60 * 1000L) { success ->
                                            if (!success) {
                                                errorMessage = "فشل في قفل الحجز. يرجى التحقق من اتصالك بالإنترنت."
                                            }
                                        }
                                    } else {
                                        errorMessage = "كلمة المرور غير صحيحة! متبقي $attemptsLeft محاولات."
                                    }
                                    return@Button
                                } else {
                                    com.serviseyem.security.BookingSecurityHelper.resetAttempts(context, booking.id)
                                }
                            }

                            if (reasonInput.isBlank() && userRole != "CLIENT") {
                                errorMessage = "يرجى كتابة سبب الإلغاء"
                                return@Button
                            }

                            onConfirmCancel(passwordInput.trim(), reasonInput.trim())
                        },
                        enabled = canCancelByRule && (userRole != "CLIENT" || (!com.serviseyem.security.BookingSecurityHelper.isBookingLocked(context, booking.id) && attemptsLeft > 0)),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تأكيد الإلغاء", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

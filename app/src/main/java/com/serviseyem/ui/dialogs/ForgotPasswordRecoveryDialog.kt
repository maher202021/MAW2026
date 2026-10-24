package com.serviseyem.ui.dialogs
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import android.content.Context
import com.serviseyem.ui.*
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🔑 نظام استعادة كلمة المرور بدون OTP (تكلفة صفرية وسرعة فائقة)
 * يتيح للمستخدم التواصل مع الإدارة عبر المحادثة الفورية، الواتساب، أو التيليجرام
 */
@Composable
fun ForgotPasswordRecoveryDialog(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onOpenChatWithAdmin: (channelId: String) -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("yemen_service_recovery_prefs", Context.MODE_PRIVATE) }
    var phoneInput by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf("IN_APP_CHAT") } // IN_APP_CHAT, WHATSAPP, TELEGRAM
    var noteInput by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var resetStatus by remember { mutableStateOf("PENDING") } // PENDING, APPROVED, REJECTED
    var tempPassword by remember { mutableStateOf("") }
    var submittedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        val savedPhone = sharedPrefs.getString("pending_recovery_phone", "") ?: ""
        val savedTime = sharedPrefs.getLong("pending_recovery_time", 0L)
        if (savedPhone.isNotBlank()) {
            phoneInput = savedPhone
            submittedTime = savedTime
            isSubmitted = true
        }
    }

    val recoveryStatus by viewModel.authViewModel.passwordRecoveryStatus.collectAsState()
    
    LaunchedEffect(recoveryStatus) {
        if (recoveryStatus.status.isNotBlank()) {
            resetStatus = recoveryStatus.status
            if (resetStatus == "APPROVED" || resetStatus == "RESOLVED") {
                tempPassword = recoveryStatus.tempPassword
                sharedPrefs.edit().clear().apply()
            } else if (resetStatus == "REJECTED" || resetStatus == "TIMEOUT") {
                sharedPrefs.edit().clear().apply()
            }
        }
    }

    LaunchedEffect(phoneInput) {
        if (phoneInput.isNotBlank()) {
            viewModel.authViewModel.listenToPasswordRecoveryStatus(phoneInput)
            // Timeout logic
            kotlinx.coroutines.delay(5 * 60 * 1000) // 5 minutes timeout
            if (resetStatus == "PENDING") {
                resetStatus = "TIMEOUT"
                sharedPrefs.edit().clear().apply()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔑 استعادة كلمة المرور",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.LightGray)
                    }
                }

                if (!isSubmitted) {
                    Text(
                        text = "أدخل رقم هاتفك المسجل واختر طريقة التواصل المباشرة مع فريق الدعم الفني لاستلام كلمة المرور الجديدة فوراً وبأمان تام دون انتظار رسائل SMS.",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("رقم الهاتف المسجل (مثال: 777123456)", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = themeColors.accent) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.accent,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Text("اختر وسيلة الاستعادة السريعة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    // Recovery Channel Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RecoveryChannelChip(
                            label = "محادثة فورية 💬",
                            isSelected = selectedChannel == "IN_APP_CHAT",
                            color = Color(0xFF3B82F6),
                            onClick = { selectedChannel = "IN_APP_CHAT" },
                            modifier = Modifier.weight(1f)
                        )
                        RecoveryChannelChip(
                            label = "واتساب 🟢",
                            isSelected = selectedChannel == "WHATSAPP",
                            color = Color(0xFF10B981),
                            onClick = { selectedChannel = "WHATSAPP" },
                            modifier = Modifier.weight(1f)
                        )
                        RecoveryChannelChip(
                            label = "تيليجرام ✈️",
                            isSelected = selectedChannel == "TELEGRAM",
                            color = Color(0xFF0EA5E9),
                            onClick = { selectedChannel = "TELEGRAM" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("ملاحظة إضافية (اختياري)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.accent,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Button(
                        onClick = {
                            if (phoneInput.trim().length >= 7) {
                                val cleanPhone = phoneInput.trim()
                                val currentTime = System.currentTimeMillis()

                                viewModel.authViewModel.submitPasswordRecoveryRequest(
                                    phone = cleanPhone,
                                    channel = selectedChannel,
                                    note = noteInput
                                )

                                sharedPrefs.edit()
                                    .putString("pending_recovery_phone", cleanPhone)
                                    .putLong("pending_recovery_time", currentTime)
                                    .apply()
                                submittedTime = currentTime

                                when (selectedChannel) {
                                    "WHATSAPP" -> {
                                        val rawSupport = viewModel.settings.value.supportWhatsapp.trim().ifBlank { "967777000000" }
                                        val adminPhone = when {
                                            rawSupport.startsWith("967") || rawSupport.startsWith("+967") -> rawSupport.removePrefix("+")
                                            rawSupport.length == 9 -> "967$rawSupport"
                                            else -> "967777000000"
                                        }
                                        val msg = "مرحباً إدارة دليل خدمات اليمن، أود استعادة كلمة المرور لرقمي المسجل: $cleanPhone"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$adminPhone?text=${Uri.encode(msg)}"))
                                        context.startActivity(intent)
                                        isSubmitted = true
                                    }
                                    "TELEGRAM" -> {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/yemenservices_support"))
                                        context.startActivity(intent)
                                        isSubmitted = true
                                    }
                                    else -> {
                                        // Open unified in-app support chat channel for password recovery
                                        viewModel.sendMessageInChat("مرحباً، أطلب استعادة كلمة المرور لرقم الحساب المسجل: $cleanPhone. $noteInput")
                                        viewModel.openDirectChat(
                                            targetUserId = com.serviseyem.data.repositories.ChatRepository.SUPPORT_ADMIN_ID,
                                            targetUserName = com.serviseyem.data.repositories.ChatRepository.SUPPORT_ADMIN_NAME,
                                            targetUserPhoto = "",
                                            relatedEntityId = cleanPhone,
                                            relatedEntityType = "PASSWORD_RECOVERY"
                                        )
                                        onDismiss()
                                        isSubmitted = true
                                    }
                                }
                            } else {
                                Toast.makeText(context, "⚠️ يرجى إدخال رقم هاتف صحيح", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("إرسال طلب الاستعادة الفوري 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                } else {
                    // Dynamic Polling Success / Awaiting Screen
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (resetStatus) {
                            "TIMEOUT" -> {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Text(
                                    text = "⏳ جارٍ المراجعة...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "يمكنك إغلاق هذه الشاشة والمتابعة لاحقاً، سيتم تنبيهك عند قبول الطلب.",
                                    fontSize = 12.sp,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface)
                                ) {
                                    Text("حسناً، إغلاق", color = Color.White)
                                }
                            }
                            "PENDING" -> {
                                CircularProgressIndicator(color = themeColors.accent, modifier = Modifier.size(36.dp))
                                Text(
                                    text = "⏳ قيد المراجعة والانتظار...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = themeColors.accent
                                )
                                
                                // ✨ م2: استخدام DateFormatter
                                val formattedTime = if (submittedTime > 0L) com.serviseyem.utils.DateFormatter.formatDisplay(submittedTime) else "الآن"
                                
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "🔢 رقم الطلب: REC-${phoneInput.takeLast(6)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "⏰ وقت التقديم: $formattedTime",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                Text(
                                    text = "لقد تم إرسال طلبك. نرجو عدم إغلاق هذه الصفحة، حيث يتم التحقق من طلبك الآن تلقائياً وتحديث الصفحة فور تعيين كلمة المرور الجديدة من قِبل المشرف.",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                            "APPROVED", "RESOLVED" -> {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                                Text(
                                    text = "🎉 تم قبول طلبك بنجاح!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = "كلمة المرور المؤقتة الخاصة بك هي:",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = tempPassword,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clip = android.content.ClipData.newPlainText("password", tempPassword)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "📋 تم نسخ كلمة المرور الجديدة", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "نسخ", tint = Color.LightGray)
                                        }
                                    }
                                }
                            }
                            "REJECTED" -> {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
                                Text(
                                    text = "❌ تم رفض طلب استعادة الحساب",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFEF4444)
                                )
                                Text(
                                    text = "عذراً، تعذر التحقق من ملكية الحساب. يرجى التواصل مع الدعم الفني مباشرة لحل المشكلة.",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

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

@Composable
fun RecoveryChannelChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) color.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, if (isSelected) color else Color.Gray.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else Color.LightGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

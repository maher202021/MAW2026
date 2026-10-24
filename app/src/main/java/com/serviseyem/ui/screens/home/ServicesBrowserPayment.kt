package com.serviseyem.ui.screens.home

import android.content.Context
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import com.serviseyem.ui.components.SmartAsyncImage
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.PaymentWalletEntity
import com.serviseyem.ui.*
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 💳 ServicesBrowserPayment - نافذة السداد والدفع الإلكتروني لخدمات دليل اليمن
 * يدعم: الكريمي، جوال، فلوسك، ون كاش، بنكيلي، والسداد النقدي المباشر
 */
@Composable
fun ServicesBrowserPaymentDialog(
    booking: BookingEntity,
    wallets: List<PaymentWalletEntity>,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedWallet by remember { mutableStateOf(wallets.firstOrNull()) }
    var transferNumber by rememberSaveable { mutableStateOf("") }
    var senderName by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var transferPhotoBase64 by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val base64 = withContext(Dispatchers.IO) {
                        try {
                            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            if (bytes != null && bytes.size < 2_000_000) {
                                Base64.encodeToString(bytes, Base64.NO_WRAP)
                            } else ""
                        } catch (_: Exception) { "" }
                    }
                    if (base64.isNotBlank()) {
                        transferPhotoBase64 = base64
                        Toast.makeText(context, "📸 تم إرفاق صورة الإشعار بنجاح!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ فشل تحميل الصورة أو حجمها كبير", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    val settingsState by viewModel.settings.collectAsState()
    val isPaymentEnabled = com.serviseyem.BuildConfig.IS_PAYMENT_ENABLED && settingsState.isPaymentEnabled

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, themeColors.accent),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💳 سداد حجز الخدمة إلكترونياً",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                    }
                }

                if (!isPaymentEnabled) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF9800)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⏳ الدفع الإلكتروني قيد التفعيل",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                            Text(
                                text = "خدمة السداد عبر المحافظ الإلكترونية غير مفرغة حالياً وسيتم إتاحتها رسمياً فور اكتمال التجهيزات مع المحافظ المعتمدة.",
                                fontSize = 11.5.sp,
                                color = Color.LightGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {

                // Booking Info Summary
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("الخدمة: ${booking.serviceType.ifBlank { booking.serviceDetails.ifBlank { "طلب خدمة" } }}", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        val amount = if (booking.totalAmount > 0) booking.totalAmount else if (booking.advancePayment > 0) booking.advancePayment else 5000.0
                        Text("المبلغ المطلوب: ${amount.toInt()} ريال يمني", fontSize = 12.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                        Text("مقدم الخدمة: ${booking.providerName}", fontSize = 10.5.sp, color = Color.LightGray)
                    }
                }

                Text("اختر المحفظة أو الحساب البنكي:", fontSize = 11.sp, color = Color.LightGray)

                // Wallets List
                if (wallets.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🏦 محفظة الكريمي إكسبرس (حساب رقم: 3001234567)\nأو جوالي (رقم: 777123456)",
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                } else {
                    wallets.forEach { wallet ->
                        val isSelected = selectedWallet?.id == wallet.id
                        Card(
                            onClick = { selectedWallet = wallet },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) themeColors.accent.copy(alpha = 0.2f) else Color(0xFF1E293B)
                            ),
                            border = BorderStroke(1.dp, if (isSelected) themeColors.accent else Color.Transparent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(wallet.accountName.ifBlank { wallet.bankName }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("رقم الحساب/المحفظة: ${wallet.walletNumber.ifBlank { wallet.bankAccountNumber }}", fontSize = 11.sp, color = themeColors.accent)
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedWallet = wallet },
                                    colors = RadioButtonDefaults.colors(selectedColor = themeColors.accent)
                                )
                            }
                        }
                    }
                }

                // Inputs
                OutlinedTextField(
                    value = transferNumber,
                    onValueChange = { transferNumber = it },
                    label = { Text("رقم الحوالة أو إشعار التحويل *", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    label = { Text("اسم المحوّل الثلاثي *", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Proof of transfer upload
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (transferPhotoBase64.isBlank()) "📸 إرفاق صورة إشعار التحويل (اختياري)" else "🔄 تغيير صورة إشعار التحويل",
                        fontSize = 11.sp,
                        color = themeColors.accent,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (transferPhotoBase64.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            SmartAsyncImage(
                                model = transferPhotoBase64,
                                contentDescription = "صورة الإشعار المرفقة",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        if (transferNumber.isBlank() || senderName.isBlank()) {
                            Toast.makeText(context, "يرجى إدخال رقم الحوالة واسم المحول", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        scope.launch {
                            try {
                                val amount = if (booking.totalAmount > 0) booking.totalAmount else 5000.0
                                viewModel.createPayment(
                                    userId = booking.clientId.ifBlank { booking.customerPhone },
                                    providerId = booking.providerId,
                                    amount = amount,
                                    method = "mobileWallet",
                                    bookingId = booking.id,
                                    isLinkedToBooking = true,
                                    bookingServiceType = booking.serviceType,
                                    walletProvider = selectedWallet?.accountName ?: "محفظة جوال",
                                    walletNumber = selectedWallet?.walletNumber ?: "",
                                    walletAccountName = senderName,
                                    transferId = transferNumber,
                                    transferPhoto = transferPhotoBase64,
                                    status = "PROCESSING"
                                )
                                Toast.makeText(context, "✅ تم إرسال إشعار السداد بنجاح وهو قيد التأكيد!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            } catch (e: Exception) {
                                Toast.makeText(context, "❌ فشل إرسال السداد: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("تأكيد وإرسال إشعار الدفع 🚀", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
                }
            }
        }
    }
}

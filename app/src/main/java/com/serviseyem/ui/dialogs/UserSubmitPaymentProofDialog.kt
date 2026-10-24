@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.dialogs




import android.content.Intent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import okhttp3.MediaType.Companion.toMediaType

import com.serviseyem.*

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.serviseyem.data.*
import com.serviseyem.utils.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.*
import com.serviseyem.ui.dialogs.*
import com.serviseyem.ui.screens.home.*
import com.serviseyem.ui.screens.map.*
import com.serviseyem.ui.screens.bookings.*
import com.serviseyem.ui.screens.admin.*
import com.serviseyem.ui.screens.assistant.*
import com.serviseyem.ui.screens.register.*
import com.serviseyem.ui.screens.status.*
import com.serviseyem.ui.screens.about.*
import com.serviseyem.ui.screens.chat.*
import com.serviseyem.ui.screens.notifications.*
import com.serviseyem.ui.screens.dashboard.*
import com.serviseyem.ui.*
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun UserSubmitPaymentProofDialog(
    booking: com.serviseyem.data.BookingEntity,
    viewModel: MainViewModel,
    paymentWallets: List<com.serviseyem.data.PaymentWalletEntity>,
    settingsState: com.serviseyem.data.AdminSettingsEntity,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedWallet by remember(paymentWallets) { 
        mutableStateOf(paymentWallets.firstOrNull { it.status == "active" && it.isVisibleToUsers && (it.walletType == "DEPOSIT" || it.walletType == "BOTH") } ?: paymentWallets.firstOrNull { it.status == "active" }) 
    }
    var transferIdInput by remember { mutableStateOf("") }
    var accountNameInput by remember { mutableStateOf("") }
    var photoInput by remember { mutableStateOf("") }
    var showConfirmSubmitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true, onBack = onDismiss)

    val requiredAmount = when {
        booking.totalAmount > 0.0 -> booking.totalAmount
        booking.advancePayment > 0.0 -> booking.advancePayment
        booking.price > 0.0 -> booking.price
        else -> 0.0
    }

    val proofPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val bitmap = context.contentResolver.openInputStream(uri)?.use {
                        android.graphics.BitmapFactory.decodeStream(it)
                    }
                    if (bitmap != null) {
                        val compressedBytes = com.serviseyem.utils.FirebaseStorageUploader.compressBitmapToBytes(
                            bitmap = bitmap,
                            maxDimension = 800,
                            maxSizeBytes = 300 * 1024L
                        )
                        val tempFile = java.io.File(context.cacheDir, "proof_${System.currentTimeMillis()}.webp")
                        java.io.FileOutputStream(tempFile).use { it.write(compressedBytes) }
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            photoInput = android.net.Uri.fromFile(tempFile).toString()
                            viewModel.triggerNotification("📸 تم اختيار وضغط صورة الإثبات بنجاح!")
                        }
                    } else {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            photoInput = uri.toString()
                        }
                    }
                } catch (e: Exception) {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        photoInput = uri.toString()
                    }
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("💳 سداد رسوم الحجز والخدمة بالمنصة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.accent.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, themeColors.accent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("💰 المبلغ المطلوب سداده:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${requiredAmount.toInt()} ريال يمني", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = themeColors.accent)
                    }
                }

                Text("يرجى اختيار أحد الحسابات / المحافظ التالية والتحويل إليها بقيمة المبلغ المطلوب:", fontSize = 11.sp, color = Color.LightGray)

                if (paymentWallets.isEmpty()) {
                    Text("⚠️ عذراً، لا توجد محافظ دفع مفعلة حالياً بالمنصة للتسديد. يرجى مراجعة المشرفين.", fontSize = 11.sp, color = Color.Red)
                } else {
                    Text("المحافظ والحسابات المتاحة للتحويل:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        paymentWallets.filter { it.status == "active" && it.isVisibleToUsers && (it.walletType == "DEPOSIT" || it.walletType == "BOTH") }.forEach { wallet ->
                            val isSel = selectedWallet?.id == wallet.id
                            val name = when (wallet.provider) {
                                "jeeb" -> "جيب 📱"
                                "alKarimi" -> "الكريمي 🏦"
                                "jawaly" -> "جوالي 📲"
                                "floosi" -> "فلوسي 💳"
                                "cashExchange" -> "حوالة 💸"
                                "foreignCurrency" -> "عملات 🌐"
                                "yemenMobile" -> "يمن كاش 🇾🇪"
                                else -> wallet.accountNameAr.ifBlank { wallet.accountName }.take(10)
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSel) themeColors.accent else Color.DarkGray,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedWallet = wallet }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(name, color = if (isSel) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                selectedWallet?.let { wallet ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("رقم الحساب/المحفظة للتحويل: ${wallet.walletNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                            Text("اسم صاحب الحساب المستلم: ${wallet.accountNameAr}", fontSize = 11.sp, color = Color.White)
                            if (wallet.description.isNotEmpty()) {
                                Text("تعليمات: ${wallet.description}", fontSize = 10.sp, color = Color.LightGray)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Text("يرجى تعبئة بيانات التحويل بعد إرسال المبلغ المالي:", fontSize = 11.sp, color = Color.LightGray)

                OutlinedTextField(
                    value = transferIdInput,
                    onValueChange = { transferIdInput = it },
                    label = { Text("رقم الحوالة المرجعي / رقم العملية (الـ ID)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = accountNameInput,
                    onValueChange = { accountNameInput = it },
                    label = { Text("اسم المرسل الكامل (صاحب المحفظة المحوِلة)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Button(
                    onClick = {
                        proofPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (photoInput.isNotEmpty()) Color(0xFF10B981) else themeColors.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text(
                        if (photoInput.isNotEmpty()) "✅ تم اختيار صورة الإثبات (اضغط لتغييرها)" else "📷 رفع صورة الإثبات أو لقطة الشاشة من الهاتف",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (transferIdInput.isBlank() || accountNameInput.isBlank()) {
                                viewModel.triggerNotification("❌ يرجى ملء رقم الحوالة واسم مرسل الحوالة كاملاً")
                                return@Button
                            }
                            if (settingsState.requirePaymentProofImage && photoInput.isBlank()) {
                                viewModel.triggerNotification("❌ الإدارة تتطلب إرفاق صورة الإثبات أو لقطة الشاشة للتحقق!")
                                return@Button
                            }
                            if (selectedWallet == null) {
                                viewModel.triggerNotification("❌ يرجى اختيار المحفظة / الحساب المحول إليه")
                                return@Button
                            }
                            showConfirmSubmitDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إرسال إثبات التحويل 📤", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("إلغاء", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showConfirmSubmitDialog) {
        val wallet = selectedWallet
        AlertDialog(
            onDismissRequest = { showConfirmSubmitDialog = false },
            title = { Text("تأكيد إرسال إثبات السداد", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("هل أنت متأكد من صحة بيانات التحويل؟")
                    Text("• المبلغ: ${requiredAmount.toInt()} ريال يمني", fontWeight = FontWeight.Bold, color = themeColors.accent)
                    if (wallet != null) {
                        Text("• المحفظة: ${wallet.provider} (${wallet.walletNumber.ifEmpty { wallet.bankAccountNumber }})")
                    }
                    Text("• رقم الحوالة: $transferIdInput")
                    Text("• اسم المرسل: $accountNameInput")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentWallet = selectedWallet ?: return@Button
                        val paymentId = "pay_" + java.util.UUID.randomUUID().toString().take(12)
                        val payment = com.serviseyem.data.PaymentEntity(
                            id = paymentId,
                            userId = booking.customerPhone,
                            providerId = booking.providerId,
                            bookingId = booking.id,
                            type = "service",
                            method = "mobileWallet",
                            status = "PROCESSING",
                            amount = requiredAmount,
                            advanceAmount = if (booking.advancePayment > 0.0) booking.advancePayment else 0.0,
                            remainingAmount = if (booking.totalAmount > requiredAmount) booking.totalAmount - requiredAmount else 0.0,
                            commission = 0.0,
                            providerShare = requiredAmount,
                            currency = "YER",
                            isLinkedToBooking = true,
                            transferId = transferIdInput,
                            transferPhoto = photoInput,
                            walletProvider = currentWallet.provider,
                            verificationNote = "بانتظار مراجعة وتأكيد الإدارة"
                        )
                        viewModel.paymentManagementViewModel.createPayment(payment)
                        viewModel.triggerNotification("✅ تم إرسال إثبات التحويل بنجاح! جاري مراجعته من الإدارة.")
                        showConfirmSubmitDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("تأكيد وإرسال", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSubmitDialog = false }) {
                    Text("تراجع")
                }
            }
        )
    }
}

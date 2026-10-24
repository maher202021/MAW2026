package com.serviseyem.ui.dialogs

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.ProviderEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.VoiceManager
import com.serviseyem.utils.VisualThemePalette

@Composable
fun BookingDialog(
    provider: ProviderEntity,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val settingsState by viewModel.settings.collectAsState()
    val currentUserNameState by viewModel.currentUserName.collectAsState()
    val currentUserPhoneState by viewModel.currentUserPhone.collectAsState()
    val currentUserResidenceState by viewModel.currentUserResidence.collectAsState()

    var customerNameInput by remember { mutableStateOf(currentUserNameState) }
    var customerPhoneInput by remember { mutableStateOf(currentUserPhoneState) }
    var customerAreaInput by remember { mutableStateOf(currentUserResidenceState) }
    var customerServiceInput by remember { mutableStateOf("") }
    var bookingDateInput by remember { mutableStateOf("") }
    var bookingTimeInput by remember { mutableStateOf("") }
    var bookingCouponCodeInput by remember { mutableStateOf("") }
    var bookingPinCodeInput by remember { mutableStateOf("") }
    var bookingCustomIdInput by remember { mutableStateOf("") }

    var showBookingConfirmDialog by remember { mutableStateOf(false) }
    var bookingFormSubmittedOnce by remember { mutableStateOf(false) }
    var bookingFormMissingFields by remember { mutableStateOf<List<String>>(emptyList()) }

    val currentCalendar = remember { java.util.Calendar.getInstance() }

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val monthFormatted = String.format(java.util.Locale.US, "%02d", month + 1)
                val dayFormatted = String.format(java.util.Locale.US, "%02d", dayOfMonth)
                bookingDateInput = "$year-$monthFormatted-$dayFormatted"
            },
            currentCalendar.get(java.util.Calendar.YEAR),
            currentCalendar.get(java.util.Calendar.MONTH),
            currentCalendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val amPm = if (hourOfDay < 12) "ص" else "م"
                val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                val formattedMin = String.format(java.util.Locale.US, "%02d", minute)
                bookingTimeInput = "$hour:$formattedMin $amPm"
            },
            currentCalendar.get(java.util.Calendar.HOUR_OF_DAY),
            currentCalendar.get(java.util.Calendar.MINUTE),
            false
        )
    }

    LaunchedEffect(Unit) {
        val year = currentCalendar.get(java.util.Calendar.YEAR)
        val month = currentCalendar.get(java.util.Calendar.MONTH) + 1
        val day = currentCalendar.get(java.util.Calendar.DAY_OF_MONTH)
        val monthFormatted = String.format("%02d", month)
        val dayFormatted = String.format("%02d", day)
        bookingDateInput = "$year-$monthFormatted-$dayFormatted"

        val hourOfDay = currentCalendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = currentCalendar.get(java.util.Calendar.MINUTE)
        val amPm = if (hourOfDay < 12) "ص" else "م"
        val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
        val formattedMin = String.format("%02d", minute)
        bookingTimeInput = "$hour:$formattedMin $amPm"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, themeColors.accent),
            modifier = Modifier.padding(12.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("📅 استمارة حجز فني: ${provider.name}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)

                if (bookingFormSubmittedOnce && bookingFormMissingFields.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("يرجى إكمال وتصحيح الحقول المطلوبة لتأكيد حجزك:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            bookingFormMissingFields.forEach { field ->
                                Text("• $field", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("⚠️ شروط وطريقة الحجز الموثقة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = settingsState.bookingTerms,
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            lineHeight = 14.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = customerNameInput,
                    onValueChange = { customerNameInput = it },
                    label = { Text("الاسم الكامل *", color = themeColors.textSecondary, fontSize = 11.sp) },
                    placeholder = { Text("أدخل الاسم الثلاثي", fontSize = 10.5.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = bookingFormSubmittedOnce && customerNameInput.trim().isEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = customerPhoneInput,
                    onValueChange = { customerPhoneInput = it },
                    label = { Text("رقم الهاتف *", color = themeColors.textSecondary, fontSize = 11.sp) },
                    placeholder = { Text("مثال: 777000111", fontSize = 10.5.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = bookingFormSubmittedOnce && customerPhoneInput.trim().isEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = customerAreaInput,
                    onValueChange = { customerAreaInput = it },
                    label = { Text("المنطقة والحي *", color = themeColors.textSecondary, fontSize = 11.sp) },
                    placeholder = { Text("مثال: صنعاء - حدة", fontSize = 10.5.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = bookingFormSubmittedOnce && customerAreaInput.trim().isEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = com.serviseyem.utils.BookingUtils.formatBookingDate(bookingDateInput),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تاريخ الحجز *", color = themeColors.textSecondary, fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).clickable { datePickerDialog.show() },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Text("📅", fontSize = 13.sp)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = bookingTimeInput,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("وقت الحجز *", color = themeColors.textSecondary, fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).clickable { timePickerDialog.show() },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        trailingIcon = {
                            IconButton(onClick = { timePickerDialog.show() }) {
                                Text("⏰", fontSize = 13.sp)
                            }
                        }
                    )
                }

                OutlinedTextField(
                    value = customerServiceInput,
                    onValueChange = { customerServiceInput = it },
                    label = { Text("الملاحظات (اختياري)", color = themeColors.textSecondary, fontSize = 11.sp) },
                    placeholder = { Text("شرح المشكلة أو ملاحظات إن وجدت...", color = Color.Gray, fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = bookingPinCodeInput,
                    onValueChange = { bookingPinCodeInput = it },
                    label = { Text("كلمة المرور للحجز *", color = themeColors.textSecondary, fontSize = 11.sp) },
                    placeholder = { Text("كلمة سر لحفظ حجزك", fontSize = 10.5.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = bookingFormSubmittedOnce && bookingPinCodeInput.trim().isEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val cleanName = customerNameInput.trim()
                            val cleanPhone = customerPhoneInput.trim().replace(" ", "").replace("+", "")
                            val cleanArea = customerAreaInput.trim()
                            val cleanPin = bookingPinCodeInput.trim()

                            val isValidYemeniPhone = cleanPhone.length == 9 && (
                                cleanPhone.startsWith("77") || cleanPhone.startsWith("73") || 
                                cleanPhone.startsWith("71") || cleanPhone.startsWith("70") || cleanPhone.startsWith("78")
                            )

                            val missing = mutableListOf<String>()
                            if (cleanName.isEmpty()) missing.add("الاسم الثلاثي بالكامل")
                            if (cleanPhone.isEmpty() || !isValidYemeniPhone) missing.add("رقم الهاتف اليمني المكون من 9 أرقام")
                            if (cleanArea.isEmpty()) missing.add("منطقة السكن والحي")
                            if (cleanPin.isEmpty()) missing.add("كلمة المرور السرية للحجز")

                            if (missing.isNotEmpty()) {
                                bookingFormSubmittedOnce = true
                                bookingFormMissingFields = missing
                                Toast.makeText(context, "⚠️ هناك حقول مطلوبة!", Toast.LENGTH_LONG).show()
                            } else {
                                bookingFormSubmittedOnce = false
                                bookingFormMissingFields = emptyList()
                                showBookingConfirmDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تأكيد الحجز", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء الحجز", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showBookingConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBookingConfirmDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("📋 هل كافة مدخلات الحجز صحيحة ودقيقة؟", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📍 تفاصيل طلب الحجز لمراجعتها قبل الإرسال:", fontSize = 11.sp, color = themeColors.accent)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("• الاسم: $customerNameInput", color = Color.White, fontSize = 11.sp)
                        Text("• رقم الهاتف: $customerPhoneInput", color = Color.White, fontSize = 11.sp)
                        Text("• منطقة السكن والحي: $customerAreaInput", color = Color.White, fontSize = 11.sp)
                        Text("• التخصص والمهنة: ${provider.profession.ifEmpty { "خدمة فنية" }}", color = Color.Yellow, fontSize = 11.sp)
                        Text("• تاريخ الحجز: ${com.serviseyem.utils.BookingUtils.formatBookingDate(bookingDateInput)}", color = Color.White, fontSize = 11.sp)
                        Text("• وقت الحجز: $bookingTimeInput", color = Color.White, fontSize = 11.sp)
                        Text("• تفاصيل المشكلة / ملاحظات: ${customerServiceInput.ifBlank { "لا يوجد" }}", color = Color.LightGray, fontSize = 11.sp)
                    }
                    Text("• الفني المسؤول: ${provider.name}", color = Color.White, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalServiceType = provider.profession.ifEmpty { "خدمة فنية" } +
                            if (customerServiceInput.trim().isNotEmpty()) " - ${customerServiceInput.trim()}" else ""
                        viewModel.addBooking(
                            name = customerNameInput,
                            phone = customerPhoneInput,
                            area = customerAreaInput,
                            serviceType = finalServiceType,
                            providerId = provider.id,
                            providerName = provider.name,
                            dateString = bookingDateInput,
                            timeString = bookingTimeInput,
                            couponCode = bookingCouponCodeInput,
                            pinCode = bookingPinCodeInput,
                            customBookingId = bookingCustomIdInput,
                            customPassword = bookingPinCodeInput
                        )
                        showBookingConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("تأكيد وإرسال طلب الحجز", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showBookingConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("تعديل الاستمارة", color = Color.White, fontSize = 11.sp)
                }
            }
        )
    }
}

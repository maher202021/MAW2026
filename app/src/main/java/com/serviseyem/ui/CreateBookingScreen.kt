package com.serviseyem.ui

import android.app.DatePickerDialog
import com.serviseyem.ui.*
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.BookingEntity
import java.util.Calendar
import java.util.Locale

object BookingValidation {
    fun validateBookingFields(
        name: String,
        phone: String,
        address: String,
        password: String
    ): List<String> {
        val missing = mutableListOf<String>()
        val cleanName = name.trim()
        val cleanPhone = phone.trim().replace(" ", "").replace("+", "")
        val cleanAddr = address.trim()
        val cleanPass = password.trim()

        if (cleanName.isEmpty()) missing.add("الاسم الثلاثي بالكامل")
        val isValidYemeniPhone = cleanPhone.length == 9 && (
            cleanPhone.startsWith("77") ||
            cleanPhone.startsWith("73") ||
            cleanPhone.startsWith("71") ||
            cleanPhone.startsWith("70") ||
            cleanPhone.startsWith("78")
        )
        if (cleanPhone.isEmpty()) {
            missing.add("رقم الهاتف اليمني")
        } else if (!isValidYemeniPhone) {
            missing.add("رقم الهاتف اليمني غير صحيح (يجب أن يتكون من 9 أرقام ويبدأ بـ 77، 73، 71، 70، 78)")
        }
        if (cleanAddr.isEmpty()) missing.add("العنوان أو مكان السكن والحي")
        if (cleanPass.isEmpty()) missing.add("كلمة المرور لحفظ الحجز والتعرف على الحساب")

        return missing
    }
}

/**
 * 📝 CreateBookingScreen
 * شاشة إنشاء وحجز موعد خدمة جديد مع التحقق الكامل من الحقول وتعيين كلمة مرور سرية.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBookingScreen(
    providerId: String = "",
    providerName: String = "",
    preselectedService: String = "",
    onBack: () -> Unit,
    onBookingCreated: (BookingEntity) -> Unit
) {
    val context = LocalContext.current

    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("صنعاء") }
    var customerArea by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf(preselectedService.ifEmpty { "صيانة عامة" }) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var serviceDetails by remember { mutableStateOf("") }
    var bookingPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCityDropdown by remember { mutableStateOf(false) }
    var showServiceDropdown by remember { mutableStateOf(false) }

    val yemeniCities = com.serviseyem.domain.YemenCities.list

    val bookingDepartments = listOf(
        "خدمات وفنيين",
        "محلات ومراكز تجارية",
        "مطاعم وكافيهات",
        "مراكز وعيادات طبية",
        "عقارات وشقق"
    )

    fun getBookingSubCategories(dept: String): List<String> {
        return when (dept) {
            "خدمات وفنيين" -> listOf("صيانة عامة", "صيانة كهرباء", "سباكة ومياه", "تكييف وتبريد", "صيانة هواتف", "صيانة إلكترونيات", "أعمال جبس ودهان", "تنظيف ونظافة")
            "محلات ومراكز تجارية" -> listOf("تسوق مواد غذائية", "طلب قطع غيار", "أدوات ومواد بناء", "أجهزة كهربائية ومنزلية", "ملابس ومستلزمات")
            "مطاعم وكافيهات" -> listOf("حجز طاولة وضيافة", "طلب وجبات مسبقة", "بوفيه ومناسبات", "مشروبات ومخبوزات")
            "مراكز وعيادات طبية" -> listOf("كشف طبي عام", "عيادة أسنان", "فحص مختبري وتحاليل", "علاج طبيعي وتمريض", "استشارة طبية متخصصة")
            "عقارات وشقق" -> listOf("معاينة شقة للإيجار", "معاينة عقار للبيع", "حجز شقة مفروشة", "معاينة أراضي ومحلات")
            else -> listOf("صيانة عامة")
        }
    }

    var selectedDepartment by remember { mutableStateOf("خدمات وفنيين") }
    var showDepartmentDropdown by remember { mutableStateOf(false) }

    val currentSubCategories = remember(selectedDepartment) {
        getBookingSubCategories(selectedDepartment)
    }

    LaunchedEffect(selectedDepartment) {
        if (preselectedService.isEmpty() && !currentSubCategories.contains(serviceType)) {
            serviceType = currentSubCategories.firstOrNull() ?: "صيانة عامة"
        }
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
            val period = if (hourOfDay < 12) "صباحاً" else "مساءً"
            val hourFormatted = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
            selectedTime = String.format(Locale.US, "\u200E%02d:%02d %s", hourFormatted, minute, period)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "طلب حجز جديد",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (providerName.isNotBlank()) "حجز موعد مع: $providerName" else "حجز موعد خدمة فنية مباشرة",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "أدخل بياناتك بدقة لتأكيد وتوثيق الحجز",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Customer Name
            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it; errorMessage = null },
                label = { Text("الاسم الثلاثي بالكامل *") },
                placeholder = { Text("مثال: أحمد محمد علي") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Phone
            OutlinedTextField(
                value = customerPhone,
                onValueChange = {
                    if (it.length <= 9) customerPhone = it
                    errorMessage = null
                },
                label = { Text("رقم الهاتف اليمني (9 أرقام) *") },
                placeholder = { Text("77XXXXXXX أو 73XXXXXXX") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // City & Area
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // City Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedCity,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المدينة *") },
                        trailingIcon = {
                            IconButton(onClick = { showCityDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { showCityDropdown = true },
                        shape = RoundedCornerShape(14.dp)
                    )

                    DropdownMenu(
                        expanded = showCityDropdown,
                        onDismissRequest = { showCityDropdown = false }
                    ) {
                        yemeniCities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    selectedCity = city
                                    showCityDropdown = false
                                }
                            )
                        }
                    }
                }

                // Area / District
                OutlinedTextField(
                    value = customerArea,
                    onValueChange = { customerArea = it; errorMessage = null },
                    label = { Text("الحي والشارع *") },
                    placeholder = { Text("مثال: شارع الستين") },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Service & Department Selection
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedDepartment,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("القسم الرئيسي *") },
                        trailingIcon = {
                            IconButton(onClick = { showDepartmentDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { showDepartmentDropdown = true },
                        shape = RoundedCornerShape(14.dp)
                    )

                    DropdownMenu(
                        expanded = showDepartmentDropdown,
                        onDismissRequest = { showDepartmentDropdown = false }
                    ) {
                        bookingDepartments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedDepartment = dept
                                    showDepartmentDropdown = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1.2f)) {
                    OutlinedTextField(
                        value = serviceType,
                        onValueChange = { serviceType = it },
                        label = { Text("نوع الخدمة الفرعية *") },
                        trailingIcon = {
                            IconButton(onClick = { showServiceDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { showServiceDropdown = true },
                        shape = RoundedCornerShape(14.dp)
                    )

                    DropdownMenu(
                        expanded = showServiceDropdown,
                        onDismissRequest = { showServiceDropdown = false }
                    ) {
                        currentSubCategories.forEach { service ->
                            DropdownMenuItem(
                                text = { Text(service) },
                                onClick = {
                                    serviceType = service
                                    showServiceDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Date & Time Picker Buttons
            Text(
                text = "📅 تحديد موعد الحجز (التاريخ والوقت) *",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedCard(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (selectedDate.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("التاريخ", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = if (selectedDate.isNotEmpty()) com.serviseyem.utils.BookingUtils.formatBookingDate(selectedDate) else "اختر التاريخ",
                                fontSize = 13.sp,
                                fontWeight = if (selectedDate.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedDate.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedCard(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.weight(1f).height(60.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (selectedTime.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("الوقت", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = selectedTime.ifEmpty { "اختر الوقت" },
                                fontSize = 13.sp,
                                fontWeight = if (selectedTime.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTime.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Additional Details
            OutlinedTextField(
                value = serviceDetails,
                onValueChange = { serviceDetails = it },
                label = { Text("تفاصيل وملاحظات إضافية (اختياري)") },
                placeholder = { Text("اكتب أي تفاصيل إضافية لمساعدة الفني...") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                maxLines = 3,
                shape = RoundedCornerShape(14.dp)
            )

            // Secret Password (4 Digits)
            OutlinedTextField(
                value = bookingPassword,
                onValueChange = {
                    if (it.length <= 4) bookingPassword = it
                    errorMessage = null
                },
                label = { Text("الرمز السري للحجز (4 أرقام) *") },
                placeholder = { Text("مثال: 1234") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Text(
                text = "💡 احفظ هذا الرمز السري؛ ستحتاجه لتعديل الحجز أو إلغائه لاحقاً بأمان.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Terms & Conditions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreeToTerms,
                    onCheckedChange = { agreeToTerms = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "أوافق على شروط وأحكام الخدمة وسياسة الإلغاء (حتى 8 ساعات قبل الموعد)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { agreeToTerms = !agreeToTerms }
                )
            }

            // Error display
            AnimatedVisibility(visible = errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.12f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = errorMessage ?: "", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Submit Button
            Button(
                onClick = {
                    val fullAddress = "$selectedCity - $customerArea".trim()
                    val validationErrors = BookingValidation.validateBookingFields(
                        customerName,
                        customerPhone,
                        fullAddress,
                        bookingPassword
                    )

                    if (validationErrors.isNotEmpty()) {
                        errorMessage = validationErrors.first()
                        return@Button
                    }

                    if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                        errorMessage = "يرجى تحديد التاريخ والوقت المناسب للحجز"
                        return@Button
                    }

                    if (!agreeToTerms) {
                        errorMessage = "يرجى الموافقة على شروط الخدمة للمتابعة"
                        return@Button
                    }

                    // NOTE: Currently synchronous. If async operation is added later,
                    // wrap in viewModelScope.launch { ... } and move isSubmitting = false inside finally.
                    isSubmitting = true
                    val randomCode = (1000..9999).random()
                    val bookingNumber = "BK-${System.currentTimeMillis().toString().takeLast(6)}-$randomCode"

                    val newBooking = BookingEntity(
                        id = java.util.UUID.randomUUID().toString(),
                        bookingNumber = bookingNumber,
                        customerName = customerName.trim(),
                        clientName = customerName.trim(),
                        customerPhone = customerPhone.trim(),
                        clientPhone = customerPhone.trim(),
                        customerArea = fullAddress,
                        clientAddress = fullAddress,
                        serviceType = serviceType,
                        providerId = providerId,
                        providerName = providerName.ifEmpty { "فني متخصص" },
                        dateString = selectedDate,
                        date = selectedDate,
                        timeString = selectedTime,
                        time = selectedTime,
                        serviceDetails = serviceDetails.trim(),
                        pinCode = com.serviseyem.utils.SecureHasher.hashPin(bookingPassword.trim()),
                        status = "PENDING",
                        createdAt = System.currentTimeMillis()
                    )

                    isSubmitting = false
                    Toast.makeText(context, "تم إرسال طلب الحجز بنجاح!", Toast.LENGTH_LONG).show()
                    onBookingCreated(newBooking)
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تأكيد وإرسال طلب الحجز", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

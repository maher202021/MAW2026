@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.register.forms

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.domain.YemenCities
import com.serviseyem.utils.VisualThemePalette

/**
 * 📝 UnifiedRegistrationForm - استمارة التسجيل الموحدة والمخصصة بالكامل لكل فئة من الفئات الـ 8
 * مطابقة 100% لكافة الحقول والشروط المطلوبة وبدون أي حقل بريد إلكتروني.
 */
@Composable
fun UnifiedRegistrationForm(
    role: String,
    themeColors: VisualThemePalette,
    onRegistrationSuccess: (Map<String, String>) -> Unit
) {
    val normalizedRole = role.uppercase()

    // الحقول الأساسية المشتركة
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("صنعاء") }
    var address by remember { mutableStateOf("") }

    // حقول إضافية مخصصة حسب الفئة
    var specialization by remember { mutableStateOf("") } // المهنة / التخصص / نوع النشاط / التخصصات
    var experienceYears by remember { mutableStateOf("") } // سنوات الخبرة
    var entityName by remember { mutableStateOf("") } // اسم المتجر / المطعم / المركز / المكتب / الشركة
    var workingHours by remember { mutableStateOf("") } // ساعات العمل
    var notesOrDescription by remember { mutableStateOf("") } // نبذة / وصف
    var extraOption1 by remember { mutableStateOf(false) } // هل يوفر توصيل؟ / هل يوفر طوارئ؟
    var cvUrlOrQualification by remember { mutableStateOf("") } // المؤهل العلمي أو رابط السيرة الذاتية

    // الصور والشعارات (اختيارية)
    var imageUri by remember { mutableStateOf("") }
    var idCardUri by remember { mutableStateOf("") }

    // التحكم في إظهار كلمات المرور
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }

    // حالات التنبيه والأخطاء
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri.toString()
    }

    val idCardPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) idCardUri = uri.toString()
    }

    val cities = YemenCities.MAIN_CITIES
    var expandedCity by remember { mutableStateOf(false) }

    // التحقق من صحة الحقول الإجبارية
    val isFormValid by remember(fullName, phone, password, confirmPassword, city, address, agreedToTerms, specialization, experienceYears, entityName, workingHours, normalizedRole) {
        derivedStateOf {
            val cleanPass = password.trim()
            val passValid = cleanPass.length >= 7 && cleanPass.any { it.isLetter() } && cleanPass.any { it.isDigit() } && cleanPass == confirmPassword.trim()
            
            val cleanPhone = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(phone)
            val yemenPhoneRegex = Regex("^(77|73|71|70|78)\\d{7}$")
            val phoneValid = cleanPhone.length == 9 && yemenPhoneRegex.matches(cleanPhone)
            
            val commonValid = fullName.trim().isNotBlank() && phoneValid && passValid && city.isNotBlank() && address.trim().isNotBlank() && agreedToTerms

            when (normalizedRole) {
                "CLIENT" -> commonValid
                "PROVIDER", "TECHNICIAN" -> commonValid && specialization.trim().isNotBlank() && experienceYears.trim().isNotBlank()
                "JOB_SEEKER" -> commonValid && specialization.trim().isNotBlank() && experienceYears.trim().isNotBlank()
                "STORE" -> commonValid && entityName.trim().isNotBlank() && specialization.trim().isNotBlank()
                "RESTAURANT" -> commonValid && entityName.trim().isNotBlank() && specialization.trim().isNotBlank() && workingHours.trim().isNotBlank()
                "MEDICAL" -> commonValid && entityName.trim().isNotBlank() && specialization.trim().isNotBlank()
                "PROPERTY" -> commonValid && entityName.trim().isNotBlank() && specialization.trim().isNotBlank()
                "JOB", "JOB_POSTER" -> commonValid && entityName.trim().isNotBlank() && specialization.trim().isNotBlank()
                else -> commonValid
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 🎯 رأس الاستمارة
            val headerTitle = when (normalizedRole) {
                "CLIENT" -> "استمارة تسجيل العميل (مستخدم عادي)"
                "PROVIDER", "TECHNICIAN" -> "استمارة انضمام الفني / مقدم الخدمة"
                "JOB_SEEKER" -> "استمارة المتقدم للوظائف (باحث عن عمل)"
                "STORE" -> "استمارة انضمام المتجر / المحل"
                "RESTAURANT" -> "استمارة انضمام المطعم / الكافيه"
                "MEDICAL" -> "استمارة انضمام المركز الطبي / الصيدلية"
                "PROPERTY" -> "استمارة انضمام المكتب العقاري / العقارات"
                "JOB", "JOB_POSTER" -> "استمارة تسجيل معلن الوظائف (شركة / منشأة)"
                else -> "استمارة التسجيل والانضمام"
            }

            Text(
                text = headerTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.accent
            )

            Text(
                text = "جميع الحقول بعلامة (*) إجبارية وسيتم مراجعتها من قِبل الإدارة.",
                fontSize = 11.sp,
                color = Color.LightGray
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ $errorMessage",
                        color = Color(0xFFEF4444),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // 📸 مربع تحميل الشعار أو الصورة (إذا كانت مدعومة لهذه الفئة)
            if (normalizedRole in listOf("PROVIDER", "TECHNICIAN", "STORE", "RESTAURANT", "MEDICAL", "PROPERTY", "JOB", "JOB_POSTER")) {
                val photoLabel = when (normalizedRole) {
                    "PROVIDER", "TECHNICIAN" -> "الصورة الشخصية (اختياري)"
                    "STORE" -> "شعار المتجر (اختياري)"
                    "RESTAURANT" -> "شعار المطعم (اختياري)"
                    "MEDICAL" -> "شعار المركز / الصيدلية (اختياري)"
                    "PROPERTY" -> "شعار المكتب العقاري (اختياري)"
                    "JOB", "JOB_POSTER" -> "شعار الشركة (اختياري)"
                    else -> "صورة الحساب / الشعار (اختياري)"
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (imageUri.isNotBlank()) {
                                AsyncImage(
                                    model = Uri.parse(imageUri),
                                    contentDescription = photoLabel,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = themeColors.accent.copy(alpha = 0.2f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = themeColors.accent)
                                    }
                                }
                            }
                            Column {
                                Text(photoLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(if (imageUri.isNotBlank()) "تم اختيار الصورة بنجاح ✅" else "انقر للاختيار من المعرض", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                        Button(
                            onClick = { imagePickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(if (imageUri.isNotBlank()) "تغيير 🔄" else "اختيار 📷", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 1️⃣ حقل الاسم الأساسي
            val nameFieldLabel = when (normalizedRole) {
                "STORE", "RESTAURANT", "PROPERTY" -> "الاسم الثلاثي للمالك *"
                "MEDICAL" -> "الاسم الثلاثي للمدير *"
                "JOB", "JOB_POSTER" -> "الاسم الثلاثي للمسؤول *"
                else -> "الاسم الثلاثي *"
            }
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(nameFieldLabel, fontSize = 12.sp) },
                placeholder = { Text("مثال: محمد علي الأحمدي", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 2️⃣ حقل اسم المنشأة / المتجر / المركز / الشركة (للفئات التجارية)
            if (normalizedRole in listOf("STORE", "RESTAURANT", "MEDICAL", "PROPERTY", "JOB", "JOB_POSTER")) {
                val entityLabel = when (normalizedRole) {
                    "STORE" -> "اسم المتجر *"
                    "RESTAURANT" -> "اسم المطعم / الكافيه *"
                    "MEDICAL" -> "اسم المركز / الصيدلية *"
                    "PROPERTY" -> "اسم المكتب العقاري *"
                    "JOB", "JOB_POSTER" -> "اسم الشركة *"
                    else -> "اسم المنشأة *"
                }
                OutlinedTextField(
                    value = entityName,
                    onValueChange = { entityName = it },
                    label = { Text(entityLabel, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3️⃣ حقل رقم الهاتف
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف *", fontSize = 12.sp) },
                placeholder = { Text("77XXXXXXX أو 73XXXXXXX (9 أرقام)", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 4️⃣ حقل كلمة المرور
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("كلمة المرور *", fontSize = 12.sp) },
                placeholder = { Text("7 خانات على الأقل (حروف وأرقام)", fontSize = 11.sp) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = themeColors.accent
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 5️⃣ حقل تأكيد كلمة المرور
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("تأكيد كلمة المرور *", fontSize = 12.sp) },
                placeholder = { Text("إعادة كتابة كلمة المرور", fontSize = 11.sp) },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = themeColors.accent
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 6️⃣ حقل التخصص / النشاط / المجال
            if (normalizedRole in listOf("PROVIDER", "TECHNICIAN", "JOB_SEEKER", "STORE", "RESTAURANT", "MEDICAL", "PROPERTY", "JOB", "JOB_POSTER")) {
                val specLabel = when (normalizedRole) {
                    "PROVIDER", "TECHNICIAN" -> "المهنة والتخصص *"
                    "JOB_SEEKER" -> "المجال المطلوب *"
                    "STORE" -> "نوع النشاط *"
                    "RESTAURANT" -> "نوع المأكولات *"
                    "MEDICAL" -> "التخصصات الطبيّة *"
                    "PROPERTY" -> "أنواع العقارات *"
                    "JOB", "JOB_POSTER" -> "مجال الشركة *"
                    else -> "التخصص والنشاط *"
                }
                val specPlaceholder = when (normalizedRole) {
                    "PROVIDER", "TECHNICIAN" -> "مثال: كهربائي منازل، سباك"
                    "JOB_SEEKER" -> "مثال: محاسبة، تسويق، إدارة"
                    "STORE" -> "مثال: إلكترونيات، مواد غذائية"
                    "RESTAURANT" -> "مثال: مأكولات شعبية، وجبات سريعة"
                    "MEDICAL" -> "مثال: باطنية، أسنان، صيدلية"
                    "PROPERTY" -> "مثال: شقق تمليك، أراضي، إيجارات"
                    "JOB", "JOB_POSTER" -> "مثال: تقنية معلومات، مقاولات"
                    else -> "أدخل التخصص أو النشاط"
                }
                OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text(specLabel, fontSize = 12.sp) },
                    placeholder = { Text(specPlaceholder, fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 7️⃣ حقل سنوات الخبرة
            if (normalizedRole in listOf("PROVIDER", "TECHNICIAN", "JOB_SEEKER")) {
                OutlinedTextField(
                    value = experienceYears,
                    onValueChange = { experienceYears = it },
                    label = { Text("سنوات الخبرة *", fontSize = 12.sp) },
                    placeholder = { Text("مثال: 3 سنوات", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 8️⃣ حقل ساعات العمل
            if (normalizedRole in listOf("RESTAURANT", "STORE")) {
                val hoursLabel = if (normalizedRole == "RESTAURANT") "ساعات العمل *" else "ساعات العمل (اختياري)"
                OutlinedTextField(
                    value = workingHours,
                    onValueChange = { workingHours = it },
                    label = { Text(hoursLabel, fontSize = 12.sp) },
                    placeholder = { Text("مثال: 8:00 ص - 12:00 م", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 9️⃣ المدينة (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expandedCity,
                onExpandedChange = { expandedCity = !expandedCity },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("المدينة / المحافظة *", fontSize = 12.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) },
                    singleLine = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCity,
                    onDismissRequest = { expandedCity = false }
                ) {
                    cities.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = {
                                city = c
                                expandedCity = false
                            }
                        )
                    }
                }
            }

            // 🔟 الحي / العنوان
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("الحي والعنوان *", fontSize = 12.sp) },
                placeholder = { Text("مثال: شارع الستين - بجوار جولة عصر", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 1️⃣1️⃣ خيارات إضافية خاصة (توصيل / طوارئ)
            if (normalizedRole == "RESTAURANT") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("خدمة التوصيل للمنازل (اختياري)", fontSize = 12.sp, color = Color.White)
                    Switch(
                        checked = extraOption1,
                        onCheckedChange = { extraOption1 = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                    )
                }
            }

            if (normalizedRole == "MEDICAL") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("خدمة الطوارئ 24 ساعة (اختياري)", fontSize = 12.sp, color = Color.White)
                    Switch(
                        checked = extraOption1,
                        onCheckedChange = { extraOption1 = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent)
                    )
                }
            }

            // 1️⃣2️⃣ حقول اختيارية للوصف أو السيرة الذاتية أو المؤهل العلمي
            if (normalizedRole in listOf("JOB_SEEKER", "PROVIDER", "TECHNICIAN", "STORE", "PROPERTY", "JOB", "JOB_POSTER")) {
                val notesLabel = when (normalizedRole) {
                    "PROVIDER", "TECHNICIAN" -> "نبذة عن الخدمات (اختياري)"
                    "JOB_SEEKER" -> "المؤهل ورابط السيرة الذاتية (اختياري)"
                    "STORE" -> "وصف مختصر للمتجر (اختياري)"
                    "PROPERTY" -> "نبذة عن المكتب العقاري (اختياري)"
                    "JOB", "JOB_POSTER" -> "نبذة عن الشركة ومزايا العمل (اختياري)"
                    else -> "ملاحظات إضافية (اختياري)"
                }
                OutlinedTextField(
                    value = notesOrDescription,
                    onValueChange = { notesOrDescription = it },
                    label = { Text(notesLabel, fontSize = 12.sp) },
                    placeholder = { Text("أدخل تفاصيل إضافية هنا...", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 1️⃣3️⃣ بطاقة الهوية للفنيين (اختياري)
            if (normalizedRole in listOf("PROVIDER", "TECHNICIAN")) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("صورة البطاقة الشخصية / رخصة المهنة (اختياري)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(if (idCardUri.isNotBlank()) "تم إرفاق المستند بنجاح ✅" else "لزيادة موثوقية حسابك وتسريع الاعتماد", fontSize = 10.sp, color = Color.Gray)
                        }
                        Button(
                            onClick = { idCardPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(if (idCardUri.isNotBlank()) "تغيير 🔄" else "إرفاق 📄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 1️⃣4️⃣ الموافقة على الشروط
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it },
                    colors = CheckboxDefaults.colors(checkedColor = themeColors.accent)
                )
                Text(
                    text = "أوافق على الشروط والأحكام وسياسة الخصوصية بالمنصة",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }

            // 🚀 زر الإرسال
            Button(
                onClick = {
                    val cleanPhone = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(phone)
                    val yemenPhoneRegex = Regex("^(77|73|71|70|78)\\d{7}$")
                    val cleanPass = password.trim()
                    
                    if (fullName.isBlank()) {
                        errorMessage = "يرجى تعبئة الاسم الرباعي"
                        return@Button
                    }
                    if (cleanPhone.length != 9 || !yemenPhoneRegex.matches(cleanPhone)) {
                        errorMessage = "رقم الهاتف غير صحيح. يجب أن يتكون من 9 أرقام ويبدأ بـ (77, 73, 71, 70)"
                        return@Button
                    }
                    if (cleanPass.length < 7 || !cleanPass.any { it.isLetter() } || !cleanPass.any { it.isDigit() }) {
                        errorMessage = "يجب أن تتكون كلمة المرور من 7 خانات على الأقل وتحتوي حروفاً وأرقاماً"
                        return@Button
                    }
                    if (cleanPass != confirmPassword.trim()) {
                        errorMessage = "كلمتا المرور غير متطابقتين"
                        return@Button
                    }

                    isSubmitting = true
                    errorMessage = null

                    val resultData: Map<String, String> = mapOf(
                        "role" to normalizedRole,
                        "fullName" to fullName.trim(),
                        "entityName" to entityName.ifBlank { fullName }.trim(),
                        "phone" to cleanPhone,
                        "password" to cleanPass,
                        "city" to city.trim(),
                        "address" to address.trim(),
                        "craftType" to specialization.trim(),
                        "specialization" to specialization.trim(),
                        "experienceYears" to experienceYears.trim(),
                        "workingHours" to workingHours.trim(),
                        "description" to notesOrDescription.trim(),
                        "hasDelivery" to extraOption1.toString(),
                        "hasEmergency" to extraOption1.toString(),
                        "imageUri" to imageUri,
                        "idCardUri" to idCardUri
                    )

                    onRegistrationSuccess(resultData)
                    // لا نضع isSubmitting = false مباشرة لتجنب الوميض، الشاشة ستختفي بسبب تغيير الحالة في الأب
                },
                enabled = isFormValid && !isSubmitting,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("إرسال طلب التسجيل للأدمن 🚀", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

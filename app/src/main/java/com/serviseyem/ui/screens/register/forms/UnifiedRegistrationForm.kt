@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.register.forms

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.models.RegistrationFormsConfig
import com.serviseyem.domain.YemenCities
import com.serviseyem.ui.helpers.AppPreferenceHelper
import com.serviseyem.ui.screens.admin.components.adminSwitchColors
import com.serviseyem.utils.SecurityCryptoUtils
import com.serviseyem.utils.VisualThemePalette

/**
 * UnifiedRegistrationForm - استمارة التسجيل الموحدة والمخصصة بالكامل لكل فئة من الفئات الـ 8
 * - رقم الهاتف فقط (بدون بريد إلكتروني)
 * - الشروط الإجبارية فقط: الاسم الثلاثي، رقم الهاتف، كلمة المرور، إعادة كلمة المرور، المدينة (+ اسم المنشأة للأقسام التجارية/المراكز/المطاعم/العقارات/الوظائف)
 * - جميع الحقول الإضافية اختيارية مع دعم الحقول الديناميكية والشروط المزامنة من لوحة الإدارة
 */
@Composable
fun UnifiedRegistrationForm(
    role: String,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier,
    formsConfig: RegistrationFormsConfig? = null,
    isSubmittingExternal: Boolean? = null,
    externalError: String? = null,
    onClearExternalError: () -> Unit = {},
    onRegistrationSuccess: (Map<String, String>) -> Unit
) {
    val context = LocalContext.current
    val normalizedRole = role.uppercase()
    val (sectionKey, legacySectionKey) = when (normalizedRole) {
        "PROVIDER", "TECHNICIAN" -> "SERVICES" to "الفنيين والصيانة"
        "STORE" -> "STORES" to "المحلات التجارية"
        "RESTAURANT" -> "RESTAURANTS" to "المطاعم"
        "MEDICAL" -> "MEDICAL" to "المراكز الطبية"
        "PROPERTY" -> "PROPERTIES" to "العقارات"
        "JOB", "JOB_POSTER", "JOB_SEEKER" -> "JOBS" to "المراكز التجارية والأسواق"
        else -> "SERVICES" to "الفنيين والصيانة"
    }
    val dynamicFields = remember(formsConfig, sectionKey, legacySectionKey) {
        formsConfig?.customFields?.filter {
            it.sectionKey == sectionKey || it.sectionKey == legacySectionKey
        }.orEmpty()
    }
    val sectionTermsText = remember(formsConfig, sectionKey, legacySectionKey) {
        formsConfig?.termsBySection?.get(sectionKey)?.takeIf { it.isNotBlank() }
            ?: formsConfig?.termsBySection?.get(legacySectionKey)?.takeIf { it.isNotBlank() }
    }
    val dynamicFieldValues = remember(normalizedRole) { mutableStateMapOf<String, String>() }
    val requiresEntityName = normalizedRole in listOf("STORE", "RESTAURANT", "MEDICAL", "PROPERTY", "JOB", "JOB_POSTER")
    val draftManager = remember(context) { RegistrationDraftManager(context) }
    val initialDraft = remember(normalizedRole) { draftManager.getDraft(normalizedRole) }

    // الحقول الأساسية المشتركة (مع حفظ تلقائي بالمسودة)
    var fullName by rememberSaveable(normalizedRole) { mutableStateOf(initialDraft["fullName"] ?: "") }
    var phone by rememberSaveable(normalizedRole) { mutableStateOf(initialDraft["phone"] ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var city by rememberSaveable(normalizedRole) { mutableStateOf(initialDraft["city"]?.takeIf { it.isNotBlank() } ?: "صنعاء") }
    var address by rememberSaveable(normalizedRole) { mutableStateOf(initialDraft["address"] ?: "") }

    // حقول إضافية مخصصة حسب الفئة (اسم المنشأة إجباري فقط للفئات التجارية/المراكز، والبقية اختيارية)
    var entityName by rememberSaveable(normalizedRole) { mutableStateOf(initialDraft["entityName"] ?: "") }
    var specialization by rememberSaveable(normalizedRole) { mutableStateOf(initialDraft["specialization"] ?: "") }
    var experienceYears by rememberSaveable(normalizedRole) { mutableStateOf(initialDraft["experienceYears"] ?: "") }
    var workingHours by rememberSaveable(normalizedRole) { mutableStateOf(initialDraft["workingHours"] ?: "") }
    var notesOrDescription by rememberSaveable(normalizedRole) { mutableStateOf(initialDraft["notesOrDescription"] ?: "") }
    var extraOption1 by rememberSaveable(normalizedRole) { mutableStateOf(initialDraft["extraOption1"] == "true") }

    // الصور والشعارات (اختيارية)
    var imageUri by rememberSaveable(normalizedRole) { mutableStateOf("") }
    var idCardUri by rememberSaveable(normalizedRole) { mutableStateOf("") }

    // التحكم في إظهار كلمات المرور والموافقة
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by rememberSaveable(normalizedRole) { mutableStateOf(false) }

    // حالات التنبيه والأخطاء والتحميل
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var localSubmitting by remember { mutableStateOf(false) }
    val isSubmitting = isSubmittingExternal ?: localSubmitting

    LaunchedEffect(externalError) {
        if (!externalError.isNullOrBlank()) {
            localSubmitting = false
            errorMessage = externalError
        }
    }

    LaunchedEffect(isSubmittingExternal) {
        if (isSubmittingExternal == false) {
            localSubmitting = false
        }
    }

    LaunchedEffect(localSubmitting, isSubmittingExternal) {
        if (isSubmittingExternal == null && localSubmitting) {
            kotlinx.coroutines.delay(15000)
            localSubmitting = false
        }
    }

    LaunchedEffect(
        normalizedRole,
        fullName,
        entityName,
        phone,
        city,
        address,
        specialization,
        experienceYears,
        workingHours,
        notesOrDescription,
        extraOption1
    ) {
        draftManager.saveDraft(
            normalizedRole,
            mapOf(
                "fullName" to fullName,
                "entityName" to entityName,
                "phone" to phone,
                "city" to city,
                "address" to address,
                "specialization" to specialization,
                "experienceYears" to experienceYears,
                "workingHours" to workingHours,
                "notesOrDescription" to notesOrDescription,
                "extraOption1" to extraOption1.toString()
            )
        )
    }

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

    // التحقق من صحة الشروط الأساسية الإجبارية والحقول الديناميكية الإجبارية
    val isFormValid by remember(
        fullName, phone, password, confirmPassword, city, agreedToTerms, entityName, requiresEntityName, dynamicFields
    ) {
        derivedStateOf {
            val cleanPass = password.trim()
            val policyOk = SecurityCryptoUtils.validatePasswordPolicy(cleanPass).first
            val passValid = policyOk && cleanPass == confirmPassword.trim()

            val cleanPhone = AppPreferenceHelper.normalizePhoneNumber(phone)
            val yemenPhoneRegex = Regex("^(77|73|71|70|78)\\d{7}$")
            val phoneValid = cleanPhone.length == 9 && yemenPhoneRegex.matches(cleanPhone)

            val dynamicOk = dynamicFields.all { fld ->
                !fld.isRequired || !dynamicFieldValues[fld.id].isNullOrBlank()
            }

            val commonValid = fullName.trim().isNotBlank() &&
                phoneValid &&
                passValid &&
                city.trim().isNotBlank() &&
                agreedToTerms &&
                dynamicOk

            if (requiresEntityName) commonValid && entityName.trim().isNotBlank() else commonValid
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                text = "الحقول بعلامة (*) إجبارية فقط، وبقية الحقول اختيارية وتُراجع جميع الطلبات لدى الإدارة عبر رقم الهاتف.",
                fontSize = 11.sp,
                color = Color.LightGray
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            val activeError = errorMessage ?: externalError
            if (!activeError.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ $activeError",
                        color = Color(0xFFEF4444),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // مربع تحميل الشعار أو الصورة (اختياري)
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
                                Text(
                                    if (imageUri.isNotBlank()) "تم اختيار الصورة بنجاح ✅" else "انقر للاختيار من المعرض",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Button(
                            onClick = {
                                imagePickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text(if (imageUri.isNotBlank()) "تغيير 🔄" else "اختيار 📷", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 1️⃣ الاسم الثلاثي (إجباري للجميع)
            val nameFieldLabel = when (normalizedRole) {
                "STORE", "RESTAURANT", "PROPERTY" -> "الاسم الثلاثي للمالك *"
                "MEDICAL" -> "الاسم الثلاثي للمدير *"
                "JOB", "JOB_POSTER" -> "الاسم الثلاثي للمسؤول *"
                else -> "الاسم الثلاثي *"
            }
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    errorMessage = null
                    onClearExternalError()
                },
                label = { Text(nameFieldLabel, fontSize = 12.sp) },
                placeholder = { Text("مثال: محمد علي الأحمدي", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reg_full_name_input")
            )

            // 2️⃣ اسم المنشأة / المتجر / المطعم / المركز / المكتب / الشركة (إجباري للأقسام التجارية والخدمية)
            if (requiresEntityName) {
                val entityLabel = when (normalizedRole) {
                    "STORE" -> "اسم المحل / المتجر *"
                    "RESTAURANT" -> "اسم المطعم / الكافيه *"
                    "MEDICAL" -> "اسم المركز الطبي / الصيدلية *"
                    "PROPERTY" -> "اسم المكتب العقاري / العقار *"
                    "JOB", "JOB_POSTER" -> "اسم الشركة / المنشأة *"
                    else -> "اسم المنشأة *"
                }
                OutlinedTextField(
                    value = entityName,
                    onValueChange = {
                        entityName = it
                        errorMessage = null
                        onClearExternalError()
                    },
                    label = { Text(entityLabel, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("reg_entity_name_input")
                )
            }

            // 3️⃣ رقم الهاتف (إجباري للجميع)
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    errorMessage = null
                    onClearExternalError()
                },
                label = { Text("رقم الهاتف *", fontSize = 12.sp) },
                placeholder = { Text("77XXXXXXX أو 73XXXXXXX (9 أرقام)", fontSize = 11.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reg_phone_input")
            )

            // 4️⃣ كلمة المرور (إجباري للجميع)
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                    onClearExternalError()
                },
                label = { Text("كلمة المرور *", fontSize = 12.sp) },
                placeholder = { Text("7 خانات على الأقل (حروف وأرقام)", fontSize = 11.sp) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "إظهار أو إخفاء كلمة المرور",
                            tint = themeColors.accent
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
            )

            // 5️⃣ تأكيد كلمة المرور (إجباري للجميع)
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                    onClearExternalError()
                },
                label = { Text("إعادة كلمة المرور *", fontSize = 12.sp) },
                placeholder = { Text("إعادة كتابة كلمة المرور", fontSize = 11.sp) },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "إظهار أو إخفاء تأكيد كلمة المرور",
                            tint = themeColors.accent
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password_input")
            )

            // 6️⃣ المدينة (إجباري للجميع)
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
                    modifier = Modifier.menuAnchor().fillMaxWidth().testTag("reg_city_input")
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

            // 7️⃣ التخصص / النشاط / المجال (اختياري)
            if (normalizedRole in listOf("PROVIDER", "TECHNICIAN", "JOB_SEEKER", "STORE", "RESTAURANT", "MEDICAL", "PROPERTY", "JOB", "JOB_POSTER")) {
                val specLabel = when (normalizedRole) {
                    "PROVIDER", "TECHNICIAN" -> "المهنة والتخصص (اختياري)"
                    "JOB_SEEKER" -> "المجال المطلوب (اختياري)"
                    "STORE" -> "نوع النشاط (اختياري)"
                    "RESTAURANT" -> "نوع المأكولات (اختياري)"
                    "MEDICAL" -> "التخصصات الطبيّة (اختياري)"
                    "PROPERTY" -> "أنواع العقارات (اختياري)"
                    "JOB", "JOB_POSTER" -> "مجال الشركة (اختياري)"
                    else -> "التخصص والنشاط (اختياري)"
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

            // 8️⃣ سنوات الخبرة (اختياري)
            if (normalizedRole in listOf("PROVIDER", "TECHNICIAN", "JOB_SEEKER")) {
                OutlinedTextField(
                    value = experienceYears,
                    onValueChange = { experienceYears = it },
                    label = { Text("سنوات الخبرة (اختياري)", fontSize = 12.sp) },
                    placeholder = { Text("مثال: 3 سنوات", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 9️⃣ ساعات العمل (اختياري)
            if (normalizedRole in listOf("RESTAURANT", "STORE", "MEDICAL")) {
                OutlinedTextField(
                    value = workingHours,
                    onValueChange = { workingHours = it },
                    label = { Text("ساعات العمل (اختياري)", fontSize = 12.sp) },
                    placeholder = { Text("مثال: 8:00 ص - 12:00 م", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 🔟 الحي / العنوان (اختياري)
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("الحي والعنوان (اختياري)", fontSize = 12.sp) },
                placeholder = { Text("مثال: شارع الستين - بجوار جولة عصر", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // 1️⃣1️⃣ خيارات إضافية خاصة (اختياري)
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
                        colors = adminSwitchColors(themeColors)
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
                        colors = adminSwitchColors(themeColors)
                    )
                }
            }

            // 1️⃣2️⃣ نبذة / وصف / مؤهل (اختياري)
            if (normalizedRole in listOf("JOB_SEEKER", "PROVIDER", "TECHNICIAN", "STORE", "RESTAURANT", "MEDICAL", "PROPERTY", "JOB", "JOB_POSTER")) {
                val notesLabel = when (normalizedRole) {
                    "PROVIDER", "TECHNICIAN" -> "نبذة عن الخدمات (اختياري)"
                    "JOB_SEEKER" -> "المؤهل ورابط السيرة الذاتية (اختياري)"
                    "STORE" -> "وصف مختصر للمتجر (اختياري)"
                    "RESTAURANT" -> "وصف مختصر للمطعم (اختياري)"
                    "MEDICAL" -> "وصف المركز والخدمات الطبية (اختياري)"
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
                            onClick = {
                                idCardPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text(if (idCardUri.isNotBlank()) "تغيير 🔄" else "إرفاق 📄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 1️⃣3️⃣.5️⃣ حقول ديناميكية مضافة من لوحة تحكم الإدارة
            if (dynamicFields.isNotEmpty()) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Text("📋 حقول إضافية مخصصة للقسم:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                dynamicFields.forEach { fld ->
                    val valText = dynamicFieldValues[fld.id] ?: ""
                    val kbType = when (fld.fieldType.uppercase()) {
                        "PHONE", "هاتف" -> KeyboardType.Phone
                        "NUMBER", "رقم" -> KeyboardType.Number
                        else -> KeyboardType.Text
                    }
                    OutlinedTextField(
                        value = valText,
                        onValueChange = {
                            dynamicFieldValues[fld.id] = it
                            errorMessage = null
                            onClearExternalError()
                        },
                        label = {
                            Text(
                                text = if (fld.isRequired) "${fld.label} *" else "${fld.label} (اختياري)",
                                fontSize = 12.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = kbType),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 1️⃣4️⃣ شروط القسم المعتمدة من الإدارة والموافقة
            if (!sectionTermsText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.28f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📜 شروط وضوابط التسجيل لهذا القسم:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        Text(sectionTermsText, fontSize = 10.5.sp, color = Color.LightGray, lineHeight = 16.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it },
                    colors = CheckboxDefaults.colors(checkedColor = themeColors.accent),
                    modifier = Modifier.testTag("reg_terms_checkbox")
                )
                Text(
                    text = "أوافق على الشروط والأحكام وسياسة الخصوصية بالمنصة",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }

            // زر الإرسال
            Button(
                onClick = {
                    val cleanPhone = AppPreferenceHelper.normalizePhoneNumber(phone)
                    val yemenPhoneRegex = Regex("^(77|73|71|70|78)\\d{7}$")
                    val cleanPass = password.trim()
                    val cleanConfirm = confirmPassword.trim()

                    if (fullName.trim().isBlank()) {
                        errorMessage = "يرجى تعبئة الاسم الثلاثي"
                        return@Button
                    }
                    if (requiresEntityName && entityName.trim().isBlank()) {
                        errorMessage = "يرجى تعبئة اسم المحل / المركز / المطعم / المنشأة"
                        return@Button
                    }
                    if (cleanPhone.length != 9 || !yemenPhoneRegex.matches(cleanPhone)) {
                        errorMessage = "رقم الهاتف غير صحيح. يجب أن يتكون من 9 أرقام ويبدأ بـ (77, 73, 71, 70, 78)"
                        return@Button
                    }
                    if (!SecurityCryptoUtils.validatePasswordPolicy(cleanPass).first) {
                        errorMessage = "يجب أن تتكون كلمة المرور من 7 خانات على الأقل وتحتوي حروفاً وأرقاماً"
                        return@Button
                    }
                    if (cleanPass != cleanConfirm) {
                        errorMessage = "كلمتا المرور غير متطابقتين"
                        return@Button
                    }
                    if (city.trim().isBlank()) {
                        errorMessage = "يرجى اختيار المدينة / المحافظة"
                        return@Button
                    }
                    val missingCustom = dynamicFields.firstOrNull { it.isRequired && dynamicFieldValues[it.id].isNullOrBlank() }
                    if (missingCustom != null) {
                        errorMessage = "يرجى تعبئة الحقل المطلوب: ${missingCustom.label}"
                        return@Button
                    }

                    localSubmitting = true
                    errorMessage = null
                    onClearExternalError()

                    val customMap = linkedMapOf<String, String>()
                    dynamicFields.forEach { fld ->
                        val v = (dynamicFieldValues[fld.id] ?: "").trim()
                        if (v.isNotBlank()) {
                            customMap[fld.label] = v
                        }
                    }
                    val customJson = org.json.JSONObject(customMap as Map<*, *>).toString()

                    val resultData = mutableMapOf(
                        "role" to normalizedRole,
                        "fullName" to fullName.trim(),
                        "ownerName" to fullName.trim(),
                        "entityName" to entityName.trim().ifBlank { fullName.trim() },
                        "phone" to cleanPhone,
                        "password" to cleanPass,
                        "confirmPassword" to cleanConfirm,
                        "city" to city.trim(),
                        "address" to address.trim(),
                        "craftType" to specialization.trim(),
                        "specialization" to specialization.trim(),
                        "experienceYears" to experienceYears.trim(),
                        "workingHours" to workingHours.trim(),
                        "description" to notesOrDescription.trim(),
                        "hasDelivery" to (normalizedRole == "RESTAURANT" && extraOption1).toString(),
                        "hasEmergency" to (normalizedRole == "MEDICAL" && extraOption1).toString(),
                        "imageUri" to imageUri,
                        "idCardUri" to idCardUri,
                        "customFieldsJson" to customJson
                    )
                    dynamicFields.forEach { fld ->
                        val v = (dynamicFieldValues[fld.id] ?: "").trim()
                        if (v.isNotBlank()) {
                            resultData["custom_${fld.id}"] = v
                        }
                    }

                    onRegistrationSuccess(resultData)
                },
                enabled = isFormValid && !isSubmitting,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent, contentColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("reg_submit_button")
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

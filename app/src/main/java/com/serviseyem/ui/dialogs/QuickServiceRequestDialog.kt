@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.dialogs
import com.serviseyem.ui.*

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🌟 3-Step Wizard for "اطلب خدمتك الآن" (Reverse Auction & Instant Requests)
 * Step 1: Category & Specialty (Big visual cards)
 * Step 2: Location & Details (Quick city selection + neighborhood + description)
 * Step 3: Contact & Submission (Auto-filled phone + PIN + prominent submit CTA)
 */
@Composable
fun QuickServiceRequestScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onBack: () -> Unit,
    onRequestCreated: () -> Unit
) {
    val context = LocalContext.current
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val settingsState by viewModel.settings.collectAsState()

    // Wizard Step State (1, 2, 3)
    var currentStep by remember { mutableIntStateOf(1) }

    // Form inputs
    var nameInput by remember(currentUserName, currentUserPhone) { 
        mutableStateOf(currentUserName.ifEmpty { if (currentUserPhone.isNotBlank()) "عميل ($currentUserPhone)" else "" }) 
    }
    var phoneInput by remember(currentUserPhone) { mutableStateOf(currentUserPhone) }
    var selectedCity by remember { mutableStateOf("صنعاء") }
    var neighborhoodInput by remember { mutableStateOf("") }
    
    // 4 Main Categories
    var selectedSection by remember { mutableStateOf("SERVICES") } // "SERVICES", "RESTAURANTS", "STORES", "MEDICAL"
    var selectedSpecialty by remember { mutableStateOf("كهرباء وصيانة") }
    var serviceTitleInput by remember { mutableStateOf("") }
    var orderDetailsInput by remember { mutableStateOf("") }
    var pinCodeInput by remember { mutableStateOf("") }
    var urgencyTime by remember { mutableStateOf("فوراً (خلال 30 دقيقة)") }
    var deliveryMethod by remember { mutableStateOf("توصيل") }

    var isSubmitting by remember { mutableStateOf(false) }
    var generatedRequestCode by remember { mutableStateOf<String?>(null) }
    var generatedSecretPin by remember { mutableStateOf<String?>(null) }

    val yemeniCities = listOf(
        "صنعاء", "عدن", "تعز", "الحديدة", "إب", "حضرموت", 
        "ذمار", "مأرب", "عمران", "صعدة", "شبوة", "لحج", "أبين", "المهرة", "البيضاء"
    )

    // Category options mapping
    val servicesOptions = listOf(
        "كهرباء وصيانة", "سباكة وتمديدات", "تكييف وتبريد", "طاقة شمسية وبطاريات",
        "دهان وديكور", "نجارة وألمنيوم", "صيانة سيارات", "إلكترونيات وهواتف", "تنظيف ونقل أثاث", "أخرى"
    )
    val restaurantsOptions = listOf(
        "مشويات ومندي", "وجبات سريعة وبرجر", "بيتزا ومعجنات", 
        "أطباق شعبية يمنية", "كافيه ومشروبات", "حلويات ومخبوزات", "بوفية وعصائر", "أخرى"
    )
    val storesOptions = listOf(
        "إلكترونيات وأجهزة", "مواد بناء وسيراميك", "سوبرماركت ومواد غذائية", 
        "أثاث ومفروشات منزلية", "طاقة شمسية وبطاريات", "ملابس ومستلزمات", "قطع غيار وزيوت", "أخرى"
    )
    val medicalOptions = listOf(
        "أدوية وصيدليات", "استشارات وعيادات تخصصية", "مختبرات وتحاليل طبية",
        "مستلزمات وأجهزة طبية", "رعاية منزلية وتمريض", "بصريات ونظارات", "أخرى"
    )

    val currentSpecialties = when (selectedSection) {
        "SERVICES" -> servicesOptions
        "RESTAURANTS" -> restaurantsOptions
        "STORES" -> storesOptions
        "MEDICAL" -> medicalOptions
        else -> servicesOptions
    }

    LaunchedEffect(selectedSection) {
        if (!currentSpecialties.contains(selectedSpecialty)) {
            selectedSpecialty = currentSpecialties.first()
        }
    }

    BackHandler {
        if (currentStep > 1) {
            currentStep--
        } else {
            onBack()
        }
    }

    Surface(
        color = Color(0xFF0F172A),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Header Bar
            Surface(
                color = Color(0xFF1E293B),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentStep > 1) {
                                    currentStep--
                                } else {
                                    onBack()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ اطلب خدمتك الآن (المزاد العكسي)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                            Text(
                                text = "خطوة $currentStep من 3: ${
                                    when(currentStep) {
                                        1 -> "اختر القسم والتخصص"
                                        2 -> "حدد الموقع وتفاصيل الطلب"
                                        else -> "بيانات التواصل والإرسال"
                                    }
                                }",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Indicator Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (step in 1..3) {
                            val isActive = currentStep >= step
                            val isCurrent = currentStep == step
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            isCurrent -> Color(0xFFEF4444)
                                            isActive -> Color(0xFF10B981)
                                            else -> Color.DarkGray.copy(alpha = 0.5f)
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            // Wizard Step Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    1 -> {
                        // Step 1: Category & Specialty Selection
                        Step1CategorySection(
                            selectedSection = selectedSection,
                            onSectionSelect = { selectedSection = it },
                            selectedSpecialty = selectedSpecialty,
                            onSpecialtySelect = { selectedSpecialty = it },
                            specialtiesList = currentSpecialties,
                            themeColors = themeColors
                        )
                    }
                    2 -> {
                        // Step 2: Location & Details
                        Step2LocationAndDetails(
                            citiesList = yemeniCities,
                            selectedCity = selectedCity,
                            onCitySelect = { selectedCity = it },
                            neighborhood = neighborhoodInput,
                            onNeighborhoodChange = { neighborhoodInput = it },
                            serviceTitle = serviceTitleInput,
                            onServiceTitleChange = { serviceTitleInput = it },
                            orderDetails = orderDetailsInput,
                            onOrderDetailsChange = { orderDetailsInput = it },
                            urgencyTime = urgencyTime,
                            onUrgencyChange = { urgencyTime = it },
                            deliveryMethod = deliveryMethod,
                            onDeliveryMethodChange = { deliveryMethod = it },
                            selectedSection = selectedSection,
                            themeColors = themeColors
                        )
                    }
                    3 -> {
                        // Step 3: Contact Info & Review Summary
                        Step3ContactAndSubmit(
                            phone = phoneInput,
                            onPhoneChange = { phoneInput = it },
                            name = nameInput,
                            onNameChange = { nameInput = it },
                            pin = pinCodeInput,
                            onPinChange = { pinCodeInput = it },
                            selectedSection = selectedSection,
                            selectedSpecialty = selectedSpecialty,
                            selectedCity = selectedCity,
                            neighborhood = neighborhoodInput,
                            serviceTitle = serviceTitleInput,
                            urgencyTime = urgencyTime,
                            themeColors = themeColors
                        )
                    }
                }
            }

            // Bottom Navigation Actions Dock
            Surface(
                color = Color(0xFF1E293B),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Gray),
                            modifier = Modifier
                                .weight(0.35f)
                                .height(48.dp)
                        ) {
                            Text("السابق", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            when (currentStep) {
                                1 -> {
                                    currentStep = 2
                                }
                                2 -> {
                                    if (serviceTitleInput.isBlank()) {
                                        Toast.makeText(context, "⚠️ يرجى كتابة عنوان أو نوع الخدمة المطلوبة للمتابعة", Toast.LENGTH_SHORT).show()
                                    } else {
                                        currentStep = 3
                                    }
                                }
                                3 -> {
                                    val cleanP = phoneInput.trim().replace(" ", "").replace("+967", "").replace("00967", "")
                                    if (cleanP.length != 9 || !(cleanP.startsWith("77") || cleanP.startsWith("73") || cleanP.startsWith("71") || cleanP.startsWith("70") || cleanP.startsWith("78"))) {
                                        Toast.makeText(context, "⚠️ يرجى إدخال رقم هاتف يمني صحيح مكون من 9 أرقام (77/73/71/70/78)", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }

                                    val pinToUse = if (pinCodeInput.isNotBlank()) pinCodeInput.trim() else (1000..9999).random().toString()
                                    val sectionLabel = when (selectedSection) {
                                        "SERVICES" -> "الخدمات والفنيين"
                                        "RESTAURANTS" -> "المطاعم والكافيهات"
                                        "STORES" -> "المراكز والمتاجر"
                                        "MEDICAL" -> "المراكز الطبية"
                                        else -> "الخدمات العامة"
                                    }

                                    val fullDesc = if (orderDetailsInput.isNotBlank()) {
                                        "$orderDetailsInput | [تخصص: $selectedSpecialty] | [المهلة: $urgencyTime] ${if(selectedSection != "SERVICES") "| التسليم: $deliveryMethod" else ""}"
                                    } else {
                                        "طلب عاجل [$selectedSpecialty] في $selectedCity (${neighborhoodInput.ifBlank { "المدينة" }}) | المهلة: $urgencyTime"
                                    }

                                    isSubmitting = true
                                    viewModel.setCurrentUserPhone(cleanP)

                                    viewModel.createInstantRequest(
                                        userId = currentUserId.ifBlank { "user_${cleanP}" },
                                        userName = nameInput.ifBlank { "عميل ($cleanP)" },
                                        userPhone = cleanP,
                                        userCity = selectedCity,
                                        userNeighborhood = neighborhoodInput.ifBlank { "وسط المدينة" },
                                        categoryId = selectedSection,
                                        categoryName = "$sectionLabel - ${if (currentSpecialties.contains(selectedSpecialty)) selectedSpecialty else currentSpecialties.first()}",
                                        serviceTitle = serviceTitleInput.trim(),
                                        description = fullDesc,
                                        images = emptyList(),
                                        urgencyTime = urgencyTime,
                                        deliveryMethod = if (selectedSection != "SERVICES") deliveryMethod else "",
                                        customPin = pinToUse,
                                        onResult = { success, reqCode, secretPin ->
                                            isSubmitting = false
                                            if (success) {
                                                generatedRequestCode = reqCode
                                                generatedSecretPin = secretPin
                                                Toast.makeText(context, "🔔 تم إطلاق طلبك العاجل بنجاح برقم ($reqCode)!\nتمت إضافته لشاشة 'طلباتي' وتنبيه الفنيين.", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "❌ حدث خطأ أثناء إرسال الطلب، يرجى المحاولة ثانية", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentStep == 3) Color(0xFF10B981) else Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(if (currentStep > 1) 0.65f else 1f)
                            .height(48.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جارٍ الإرسال وإطلاق المزاد...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            val btnText = when (currentStep) {
                                1 -> "متابعة للموقع والتفاصيل ➡️"
                                2 -> "متابعة لبيانات التواصل ➡️"
                                else -> "🚀 إرسال الطلب واستقبال العروض الآن"
                            }
                            Text(btnText, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Success Confirmation Modal
    if (generatedRequestCode != null) {
        AlertDialog(
            onDismissRequest = {
                generatedRequestCode = null
                onRequestCreated()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🚀", fontSize = 24.sp)
                    Text("تم إطلاق طلبك بنجاح!", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "تم توجيه طلبك فوراً للمزودين والمتخصصين في $selectedCity، وستصلك العروض والمزايدات مباشرة على حسابك.",
                        fontSize = 11.5.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, themeColors.accent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("كود متابعة الطلب:", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = generatedRequestCode ?: "",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            if (generatedSecretPin != null) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("الرمز السري (PIN):", fontSize = 10.sp, color = Color.Gray)
                                    Text(generatedSecretPin ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Request Code", "${generatedRequestCode} (PIN: ${generatedSecretPin})")
                                    clipboard?.setPrimaryClip(clip)
                                    Toast.makeText(context, "📋 تم نسخ كود الطلب للحافظة!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("نسخ الكود للحافظة", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text("يمكنك متابعة العروض المقدمة من قسم 'طلباتي' في أي وقت.", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        generatedRequestCode = null
                        onRequestCreated()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("الانتقال إلى طلباتي 📋", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        )
    }
}

// ----------------------------------------------------
// Step 1: Clean Visual Category & Specialty Cards
// ----------------------------------------------------
@Composable
private fun Step1CategorySection(
    selectedSection: String,
    onSectionSelect: (String) -> Unit,
    selectedSpecialty: String,
    onSpecialtySelect: (String) -> Unit,
    specialtiesList: List<String>,
    themeColors: VisualThemePalette
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "1️⃣ اختر القسم الرئيسي المناسب لطلبك:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // 4 Big distinct visual cards
        val categoriesData = listOf(
            Triple("SERVICES", "🔧 الخدمات والفنيين", "صيانة منازل، سباكة، كهرباء، تكييف، دهان وورش"),
            Triple("RESTAURANTS", "🍽️ المطاعم والكافيهات", "وجبات سريعة، مشويات ومندي، معجنات وكافيهات"),
            Triple("STORES", "🏬 المتاجر والمراكز", "أجهزة، مواد بناء، سوبرماركت، مستلزمات وأثاث"),
            Triple("MEDICAL", "🏥 المراكز والعيادات", "أدوية وصيدليات، عيادات واستشارات ورعاية طبية")
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            categoriesData.forEach { (id, title, subtitle) ->
                val isSelected = selectedSection == id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSectionSelect(id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF131D2E)
                    ),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) Color(0xFFEF4444) else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(0xFFEF4444).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(title.take(2), fontSize = 20.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title.drop(2).trim(),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFFEF4444) else Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                fontSize = 10.5.sp,
                                color = Color.LightGray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { onSectionSelect(id) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFFEF4444),
                                unselectedColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Sub-specialties pills
        Text(
            text = "👇 اختر التخصص الدقيق:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.accent
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            specialtiesList.forEach { specialty ->
                val isSelected = selectedSpecialty == specialty
                FilterChip(
                    selected = isSelected,
                    onClick = { onSpecialtySelect(specialty) },
                    label = {
                        Text(
                            text = specialty,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFEF4444),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color.LightGray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color(0xFFEF4444) else Color.DarkGray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------
// Step 2: Location & Service Details
// ----------------------------------------------------
@Composable
private fun Step2LocationAndDetails(
    citiesList: List<String>,
    selectedCity: String,
    onCitySelect: (String) -> Unit,
    neighborhood: String,
    onNeighborhoodChange: (String) -> Unit,
    serviceTitle: String,
    onServiceTitleChange: (String) -> Unit,
    orderDetails: String,
    onOrderDetailsChange: (String) -> Unit,
    urgencyTime: String,
    onUrgencyChange: (String) -> Unit,
    deliveryMethod: String,
    onDeliveryMethodChange: (String) -> Unit,
    selectedSection: String,
    themeColors: VisualThemePalette
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "2️⃣ حدد المحافظة والمنطقة:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Quick City Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(citiesList) { city ->
                val isSelected = selectedCity == city
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onCitySelect(city) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "📍 $city",
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = Color.White
                    )
                }
            }
        }

        // Neighborhood text field
        OutlinedTextField(
            value = neighborhood,
            onValueChange = onNeighborhoodChange,
            label = { Text("الحي / الشارع / المعلم البارز (مثال: شارع حدة - جولة الرويشان)") },
            placeholder = { Text("اكتب موقعك بالتحديد ليصل الفني أو الطلب سريعاً") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color.DarkGray,
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            )
        )

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        Text(
            text = "📝 تفاصيل ما تحتاجه بدقة:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Service Title
        OutlinedTextField(
            value = serviceTitle,
            onValueChange = onServiceTitleChange,
            label = { Text("عنوان الطلب / المشكلة (مطلوب) *") },
            placeholder = { Text("مثال: صيانة تسريب مياه في المطبخ / وجبة غداء لـ 4 أفراد") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFEF4444),
                unfocusedBorderColor = Color.DarkGray,
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            )
        )

        // Details / Description
        OutlinedTextField(
            value = orderDetails,
            onValueChange = onOrderDetailsChange,
            label = { Text("شرح إضافي / ملاحظات ومواصفات") },
            placeholder = { Text("اكتب أي تفاصيل أخرى أو مستلزمات مطلوبة لتصلك عروض أسعار دقيقة...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFEF4444),
                unfocusedBorderColor = Color.DarkGray,
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            )
        )

        // Urgency selector
        Text(
            text = "⏱️ وقت التنفيذ المطلوب:",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.accent
        )
        val urgencies = listOf("فوراً (خلال 30 دقيقة)", "اليوم خلال ساعات", "تحديد موعد لاحق")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            urgencies.forEach { opt ->
                val isSel = urgencyTime == opt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Color(0xFFEF4444) else Color(0xFF1E293B))
                        .clickable { onUrgencyChange(opt) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// Step 3: Contact & Submission Summary
// ----------------------------------------------------
@Composable
private fun Step3ContactAndSubmit(
    phone: String,
    onPhoneChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    pin: String,
    onPinChange: (String) -> Unit,
    selectedSection: String,
    selectedSpecialty: String,
    selectedCity: String,
    neighborhood: String,
    serviceTitle: String,
    urgencyTime: String,
    themeColors: VisualThemePalette
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "3️⃣ بيانات التواصل ومراجعة الطلب:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Phone Input (Auto-filled)
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("رقم الهاتف للتواصل واستلام العروض (9 أرقام) *") },
            placeholder = { Text("مثال: 771234567") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            leadingIcon = {
                Text("🇾🇪 +967", fontSize = 11.sp, color = themeColors.accent, modifier = Modifier.padding(start = 8.dp))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color.DarkGray,
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            )
        )

        // Name input
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("الاسم أو اللقب (اختياري)") },
            placeholder = { Text("مثال: أبو محمد") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color.DarkGray,
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            )
        )

        // PIN (Optional)
        OutlinedTextField(
            value = pin,
            onValueChange = onPinChange,
            label = { Text("رمز PIN السري لمتابعة الطلب (اختياري - 4 أرقام)") },
            placeholder = { Text("إذا تركته فارغاً سيتم توليد رمز سري تلقائياً") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color.DarkGray,
                focusedContainerColor = Color(0xFF1E293B),
                unfocusedContainerColor = Color(0xFF1E293B)
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Summary Review Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📋", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ملخص طلبك قبل الإطلاق:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Text("🏷️ القسم: $selectedSpecialty", fontSize = 11.sp, color = Color.White)
                Text("📍 الموقع: $selectedCity ${if (neighborhood.isNotBlank()) "($neighborhood)" else ""}", fontSize = 11.sp, color = Color.White)
                Text("📝 عنوان الطلب: ${serviceTitle.ifBlank { "طلب خدمة" }}", fontSize = 11.sp, color = Color.White)
                Text("⏱️ وقت التنفيذ: $urgencyTime", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuickServiceRequestDialog(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onRequestCreated: () -> Unit
) {
    QuickServiceRequestScreen(
        viewModel = viewModel,
        themeColors = themeColors,
        onBack = onDismiss,
        onRequestCreated = onRequestCreated
    )
}

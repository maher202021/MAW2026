package com.serviseyem

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

// --------------------------------------------------------
// 6. STORE CREATE/EDIT DIALOG
// --------------------------------------------------------
@Composable
fun StoreCreateEditDialog(
    store: StoreEntity?, // Null if creating
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    sectionId: String = "stores"
) {
    val cities by viewModel.cities.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val settingsState by viewModel.settings.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf(store?.name ?: "") }
    var description by remember { mutableStateOf(store?.description ?: "") }
    var phone by remember { mutableStateOf(store?.phone ?: currentUserPhone) }
    var neighborhood by remember { mutableStateOf(store?.localNeighborhood ?: "") }
    var selectedCityId by remember { mutableStateOf(store?.cityId ?: cities.firstOrNull()?.id ?: "") }
    val availableCategories = remember(categories, sectionId) {
        val filtered = categories.filter { it.parentId == sectionId && !it.isMainCategory }
        if (filtered.isNotEmpty()) filtered
        else {
            when (sectionId) {
                "restaurants" -> listOf(
                    CategoryEntity("sub_rest_1", "مطاعم يمنية وشرقية", "🍲", 1, parentId = "restaurants"),
                    CategoryEntity("sub_rest_2", "وجبات سريعة وبرجر", "🍔", 2, parentId = "restaurants"),
                    CategoryEntity("sub_rest_3", "كافيهات ومشروبات", "☕", 3, parentId = "restaurants"),
                    CategoryEntity("sub_rest_4", "حلويات ومخابز", "🍰", 4, parentId = "restaurants"),
                    CategoryEntity("sub_rest_5", "مشويات وأسماك", "🥩", 5, parentId = "restaurants")
                )
                "medical", "2" -> listOf(
                    CategoryEntity("sub_med_1", "عيادات وأطباء", "🩺", 1, parentId = "medical"),
                    CategoryEntity("sub_med_2", "صيدليات ومستلزمات", "💊", 2, parentId = "medical"),
                    CategoryEntity("sub_med_3", "مختبرات تحاليل", "🔬", 3, parentId = "medical"),
                    CategoryEntity("sub_med_4", "مراكز علاج طبيعي", "🧘", 4, parentId = "medical"),
                    CategoryEntity("sub_med_5", "مستشفيات ومراكز تخصصية", "🏥", 5, parentId = "medical")
                )
                "centers" -> listOf(
                    CategoryEntity("sub_center_1", "مراكز تجميل وصالونات", "✂️", 1, parentId = "centers"),
                    CategoryEntity("sub_center_2", "مراكز طبية وتخصصية", "🏥", 2, parentId = "centers"),
                    CategoryEntity("sub_center_3", "مراكز تعليم وتدريب", "🎓", 3, parentId = "centers"),
                    CategoryEntity("sub_center_4", "أندية وصالات رياضية", "🏋️", 4, parentId = "centers")
                )
                "realestate" -> listOf(
                    CategoryEntity("sub_prop_1", "شقق للإيجار والبيع", "🏢", 1, parentId = "realestate"),
                    CategoryEntity("sub_prop_2", "فلل وقصور", "🏰", 2, parentId = "realestate"),
                    CategoryEntity("sub_prop_3", "أراضي ومخططات", "🏞️", 3, parentId = "realestate"),
                    CategoryEntity("sub_prop_4", "مكاتب ومحلات تجارية", "🏪", 4, parentId = "realestate"),
                    CategoryEntity("sub_prop_5", "شاليهات واستراحات", "🏊", 5, parentId = "realestate")
                )
                "jobs" -> listOf(
                    CategoryEntity("sub_job_1", "وظائف هندسية وتقنية", "💻", 1, parentId = "jobs"),
                    CategoryEntity("sub_job_2", "وظائف طبية وصحية", "🩺", 2, parentId = "jobs"),
                    CategoryEntity("sub_job_3", "مبيعات وتسويق", "📈", 3, parentId = "jobs"),
                    CategoryEntity("sub_job_4", "محاسبة وإدارة", "📑", 4, parentId = "jobs"),
                    CategoryEntity("sub_job_5", "حرف وخدمات مهنية", "🔧", 5, parentId = "jobs")
                )
                else -> listOf( // "stores"
                    CategoryEntity("sub_store_1", "ملابس وأزياء", "👔", 1, parentId = "stores"),
                    CategoryEntity("sub_store_2", "إلكترونيات وهواتف", "📱", 2, parentId = "stores"),
                    CategoryEntity("sub_store_3", "أجهزة منزلية وكهربائية", "📺", 3, parentId = "stores"),
                    CategoryEntity("sub_store_4", "سوبرماركت ومواد غذائية", "🛒", 4, parentId = "stores"),
                    CategoryEntity("sub_store_5", "عطور ومستحضرات تجميل", "💄", 5, parentId = "stores")
                )
            }
        }
    }

    var selectedCatId by remember { mutableStateOf(store?.categoryId ?: availableCategories.firstOrNull()?.id ?: "") }
    var workingHours by remember { mutableStateOf(store?.workingHours ?: "9:00 AM - 10:00 PM") }
    
    // Passwords
    var password by remember { mutableStateOf(store?.password ?: "") }
    var confirmPassword by remember { mutableStateOf(store?.password ?: "") }

    // Admin limit
    var customMaxPhotosInput by remember { mutableStateOf(if (store != null) store.maxImages.toString() else "5") }

    // Cover & Logo Image Pickers
    var logoImageBase64 by remember { mutableStateOf(store?.logoImage ?: "") }
    var coverImageBase64 by remember { mutableStateOf(store?.coverImage ?: "") }

    val logoImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val base64 = com.serviseyem.utils.compressAndResizeImageUri(context, uri, 800, 70)
                if (base64.isNotEmpty()) {
                    logoImageBase64 = base64
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val coverImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val base64 = com.serviseyem.utils.compressAndResizeImageUri(context, uri, 800, 70)
                if (base64.isNotEmpty()) {
                    coverImageBase64 = base64
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // PDF files
    var pdfUriText by remember { mutableStateOf(store?.pdfFileUri ?: "") }
    var pdfBase64Text by remember { mutableStateOf(store?.pdfFileBase64 ?: "") }
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            pdfUriText = uri.toString()
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                if (bytes != null) {
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                    pdfBase64Text = base64
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Images
    var storePhotosList by remember { mutableStateOf<List<String>>(store?.images ?: emptyList()) }
    
    val currentMaxPhotos = if (store != null && store.maxImages > 0) store.maxImages else settingsState.maxStorePhotos
    val isUnlimited = currentMaxPhotos >= 999

    val storePhotosUriPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val converted = uris.mapNotNull { uri ->
            val base64 = com.serviseyem.utils.compressAndResizeImageUri(context, uri, 800, 70)
            if (base64.isNotEmpty()) "data:image/jpeg;base64,$base64" else null
        }
        val combined = if (isUnlimited) {
            storePhotosList + converted
        } else {
            (storePhotosList + converted).take(currentMaxPhotos)
        }
        storePhotosList = combined
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.background),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(max = 650.dp)
                .border(2.dp, themeColors.accent, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (store == null) "🏪 تسجيل وإضافة متجر جديد" else "🏪 تعديل بيانات المتجر",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.Red)
                    }
                }

                // Show Admin Terms
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = settingsState.storesRegistrationTerms,
                        fontSize = 10.sp,
                        color = themeColors.accent,
                        modifier = Modifier.padding(8.dp),
                        lineHeight = 14.sp
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المتجر التجاري", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("store_name_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف السلع والخدمات المعروضة بالتفصيل", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف المالك للتواصل (مثال: 777123456)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = { neighborhood = it },
                    label = { Text("الحي السكني أو الشارع الرئيسي", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = workingHours,
                    onValueChange = { workingHours = it },
                    label = { Text("ساعات العمل اليومية (مثال: 8:00 ص - 10:00 م)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Passwords with confirmation
                Text("🔑 كلمة المرور لحساب المالك والمشرف:", fontSize = 11.sp, color = themeColors.textSecondary)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور الخاصة بك", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("تأكيد كلمة المرور الخاصة بك", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Forgot Password link
                if (store != null || phone.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            if (password.isNotEmpty()) {
                                viewModel.requestPasswordRecoveryForStore(name.ifEmpty { "المتجر" }, phone, password)
                            } else {
                                android.widget.Toast.makeText(context, "الرجاء إدخال كلمة المرور أولاً ليتم حفظها وطلب استعادتها", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text("🔑 نسيت كلمة المرور؟ أرسل طلب للأدمن لاسترجاعها فوراً", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }

                // Dropdowns
                Text("المحافظة اليمنية:", fontSize = 11.sp, color = themeColors.textSecondary)
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    val currentCityName = cities.find { it.id == selectedCityId }?.nameAr ?: "اختر المحافظة"
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(currentCityName, color = Color.White)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city.nameAr) },
                                onClick = { selectedCityId = city.id; expanded = false }
                            )
                        }
                    }
                }

                Text("فئة الأنشطة التجارية:", fontSize = 11.sp, color = themeColors.textSecondary)
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    val currentCatName = availableCategories.find { it.id == selectedCatId }?.name ?: "اختر الفئة"
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(currentCatName, color = Color.White)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        availableCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.icon} ${cat.name}") },
                                onClick = { selectedCatId = cat.id; expanded = false }
                            )
                        }
                    }
                }

                // Multiple Images Upload
                if (settingsState.showStoresPhotosOption) {
                    val limitText = if (isUnlimited) "عدد غير محدود" else "$currentMaxPhotos"
                    Text("📸 معرض صور المتجر والمنتجات (${storePhotosList.size}/$limitText):", fontSize = 11.sp, color = themeColors.textSecondary)
                    if (storePhotosList.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().height(75.dp)
                        ) {
                            items(storePhotosList.size) { index ->
                                val photo = storePhotosList[index]
                                val bMap = rememberBase64Bitmap(photo)
                                Box(modifier = Modifier.size(70.dp)) {
                                    if (bMap != null) {
                                        Image(
                                            bitmap = bMap,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize().background(Color.Gray), contentAlignment = Alignment.Center) {
                                            Text("📸", fontSize = 20.sp)
                                        }
                                    }
                                    IconButton(
                                        onClick = { storePhotosList = storePhotosList.filterIndexed { idx, _ -> idx != index } },
                                        modifier = Modifier.align(Alignment.TopEnd).size(18.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (isUnlimited || storePhotosList.size < currentMaxPhotos) {
                        Button(
                            onClick = { storePhotosUriPicker.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📷 إضافة صور إضافية للمعرض", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }

                if (adminRole != "GUEST") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("⚙️ لوحة تحكم الإدارة (خاص بالأدمن):", fontSize = 11.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = customMaxPhotosInput,
                        onValueChange = { customMaxPhotosInput = it },
                        label = { Text("الحد الأقصى للصور (اكتب 999 لصور غير محدودة)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // Logo and Cover Upload Buttons
                Text("📷 الشعار وصورة غلاف المتجر الرئيسي:", fontSize = 11.sp, color = themeColors.textSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { logoImagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = if (logoImageBase64.isEmpty()) "📷 رفع الشعار" else "✅ تم تغيير الشعار",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { coverImagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = if (coverImageBase64.isEmpty()) "🖼️ رفع صورة الغلاف" else "✅ تم تغيير الغلاف",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }

                // PDF Upload Section
                Text("📄 ملفات PDF المرفقة (قوائم الأسعار، عروض السلع والمطعم):", fontSize = 11.sp, color = themeColors.textSecondary)
                Button(
                    onClick = { pdfPickerLauncher.launch("application/pdf") },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (pdfUriText.isEmpty()) "📄 إرفاق ملف PDF للسلع والخدمات من الهاتف" else "✅ تم إرفاق ملف الـ PDF بنجاح",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (name.trim().isEmpty()) {
                            android.widget.Toast.makeText(context, "❌ يرجى إدخال اسم المتجر/المحل التجاري أولاً!", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (phone.trim().isEmpty()) {
                            android.widget.Toast.makeText(context, "❌ يرجى إدخال رقم هاتف للتواصل!", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        val cleanPhone = phone.trim().replace(" ", "").replace("+", "")
                        val isValidYemeniPhone = cleanPhone.length == 9 && (
                            cleanPhone.startsWith("77") || 
                            cleanPhone.startsWith("73") || 
                            cleanPhone.startsWith("71") || 
                            cleanPhone.startsWith("70") || 
                            cleanPhone.startsWith("78")
                        )
                        if (!isValidYemeniPhone) {
                            android.widget.Toast.makeText(context, "❌ رقم الهاتف المدخل غير صحيح! يجب أن يبدأ بـ (77، 73، 71، 70، 78) ويتكون من 9 أرقام.", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (adminRole == "GUEST" && neighborhood.trim().isEmpty()) {
                            android.widget.Toast.makeText(context, "❌ يرجى تحديد الحي أو الحارة السكنية لتسهيل الوصول!", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (password.trim().isEmpty()) {
                            android.widget.Toast.makeText(context, "❌ يرجى تعيين كلمة مرور لحماية متجرك وإمكانية تعديله مستقبلاً!", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (password != confirmPassword) {
                            android.widget.Toast.makeText(context, "❌ كلمتا المرور غير متطابقتين!", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (true) {
                            val newStore = StoreEntity(
                                id = store?.id ?: "",
                                sectionId = store?.sectionId ?: sectionId,
                                name = name,
                                description = description,
                                ownerId = currentUserId,
                                ownerName = currentUserName.ifEmpty { "تاجر يمني" },
                                phone = phone,
                                categoryId = selectedCatId,
                                cityId = selectedCityId,
                                localNeighborhood = neighborhood.ifEmpty { "إدارة التطبيق" },
                                workingHours = workingHours,
                                logoImage = logoImageBase64,
                                coverImage = coverImageBase64,
                                isActive = if (adminRole != "GUEST") true else (store?.isActive ?: false), // Auto-approve if added by admin
                                isApproved = if (adminRole != "GUEST") true else (store?.isApproved ?: false),
                                passwordHash = password,
                                maxImages = customMaxPhotosInput.toIntOrNull() ?: (store?.maxImages ?: 5),
                                pdfFileUri = pdfUriText,
                                pdfFileBase64 = pdfBase64Text,
                                pdfStatus = if (pdfBase64Text.isNotEmpty()) (store?.pdfStatus ?: "PENDING") else "",
                                images = storePhotosList
                            )
                            viewModel.saveStore(newStore)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إرسال الطلب للمراجعة والتحقق", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

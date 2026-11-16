package com.serviseyem

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
// 7. PROPERTY CREATE/EDIT DIALOG
// --------------------------------------------------------
@Composable
fun PropertyCreateEditDialog(
    property: PropertyEntity?,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    sectionId: String = "properties"
) {
    val cities by viewModel.cities.collectAsState()
    val settingsState by viewModel.settings.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()
    val context = LocalContext.current

    var title by remember { mutableStateOf(property?.title ?: "") }
    var description by remember { mutableStateOf(property?.description ?: "") }
    var priceInput by remember { mutableStateOf(property?.price?.toString() ?: "0") }
    var phone by remember { mutableStateOf(property?.phone ?: currentUserPhone) }
    var neighborhood by remember { mutableStateOf(property?.localNeighborhood ?: "") }
    var type by remember { mutableStateOf(property?.type ?: "rent") } // rent, sale
    var propertyType by remember { mutableStateOf(property?.propertyType ?: "apartment") } // apartment, house, land, shop
    var selectedCityId by remember { mutableStateOf(property?.cityId ?: cities.firstOrNull()?.id ?: "") }
    
    // Passwords
    var password by remember { mutableStateOf(property?.password ?: "") }
    var confirmPassword by remember { mutableStateOf(property?.password ?: "") }

    // Admin limit
    var customMaxPhotosInput by remember { mutableStateOf(if (property != null) property.maxImages.toString() else "5") }

    // PDF files
    var pdfUriText by remember { mutableStateOf(property?.pdfFileUri ?: "") }
    var pdfBase64Text by remember { mutableStateOf(property?.pdfFileBase64 ?: "") }
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
    var propPhotosList by remember { mutableStateOf<List<String>>(property?.images ?: emptyList()) }

    val currentMaxPhotos = if (property != null && property.maxImages > 0) property.maxImages else settingsState.maxPropertyPhotos
    val isUnlimited = currentMaxPhotos >= 999

    val propPhotosUriPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val converted = uris.map { uri ->
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            "data:image/jpeg;base64,$base64"
        }
        val combined = if (isUnlimited) {
            propPhotosList + converted
        } else {
            (propPhotosList + converted).take(currentMaxPhotos)
        }
        propPhotosList = combined
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
                        text = if (property == null) "🏠 إضافة عقار جديد" else "🏠 تعديل بيانات العقار",
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
                        text = settingsState.propertiesRegistrationTerms,
                        fontSize = 10.sp,
                        color = themeColors.accent,
                        modifier = Modifier.padding(8.dp),
                        lineHeight = 14.sp
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الإعلان (مثال: شقة متميزة للإيجار في حدة)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("prop_title_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("المواصفات الكاملة (عدد الغرف، المرافق، الخدمات)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("القيمة السعرية", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("نوع المعاملة:", fontSize = 9.sp, color = themeColors.textSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(Pair("rent", "إيجار"), Pair("sale", "بيع")).forEach { (key, name) ->
                                val isSel = type == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSel) themeColors.accent else themeColors.surface, RoundedCornerShape(6.dp))
                                        .clickable { type = key }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(name, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف المالك / المعلن", fontSize = 11.sp) },
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
                    label = { Text("المنطقة / المربع السكني", fontSize = 11.sp) },
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
                if (property != null || phone.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            if (password.isNotEmpty()) {
                                viewModel.requestPasswordRecoveryForProperty(title.ifEmpty { "العقار" }, phone, password)
                            } else {
                                android.widget.Toast.makeText(context, "الرجاء إدخال كلمة المرور أولاً ليتم حفظها وطلب استعادتها", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text("🔑 نسيت كلمة المرور؟ أرسل طلب للأدمن لاسترجاعها فوراً", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }

                Text("نوع المنشأة العقارية:", fontSize = 11.sp, color = themeColors.textSecondary)
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    val pTypes = mapOf(
                        "apartment" to "شقة سكينة",
                        "house" to "بيت مستقل/فيلا",
                        "land" to "أرض عقارية",
                        "shop" to "محل تجاري/دكان"
                    )
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(pTypes[propertyType] ?: "شقة سكينة", color = Color.White)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        pTypes.forEach { (key, value) ->
                            DropdownMenuItem(
                                text = { Text(value) },
                                onClick = { propertyType = key; expanded = false }
                            )
                        }
                    }
                }

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

                // Multiple Images Upload
                if (settingsState.showPropertiesPhotosOption) {
                    val limitText = if (isUnlimited) "عدد غير محدود" else "$currentMaxPhotos"
                    Text("📸 معرض صور العقار الملحق (${propPhotosList.size}/$limitText):", fontSize = 11.sp, color = themeColors.textSecondary)
                    if (propPhotosList.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().height(75.dp)
                        ) {
                            items(propPhotosList.size) { index ->
                                val photo = propPhotosList[index]
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
                                        onClick = { propPhotosList = propPhotosList.filterIndexed { idx, _ -> idx != index } },
                                        modifier = Modifier.align(Alignment.TopEnd).size(18.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (isUnlimited || propPhotosList.size < currentMaxPhotos) {
                        Button(
                            onClick = { propPhotosUriPicker.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📷 إضافة صور عقارية إضافية للمجموعة", fontSize = 10.sp, color = Color.White)
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

                // PDF Upload Section
                Text("📄 الوثائق والتفاصيل الإضافية المرفقة بصيغة PDF:", fontSize = 11.sp, color = themeColors.textSecondary)
                Button(
                    onClick = { pdfPickerLauncher.launch("application/pdf") },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (pdfUriText.isEmpty()) "📄 إرفاق وثائق العقار PDF من الهاتف" else "✅ تم إرفاق ملف الـ PDF العقاري بنجاح",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (title.trim().isEmpty()) {
                            android.widget.Toast.makeText(context, "❌ يرجى إدخال عنوان الإعلان الرئيسي للعقار!", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (phone.trim().isEmpty()) {
                            android.widget.Toast.makeText(context, "❌ يرجى إدخال رقم الهاتف للتواصل بخصوص العقار!", android.widget.Toast.LENGTH_LONG).show()
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
                            android.widget.Toast.makeText(context, "❌ يرجى تحديد موقع الحي أو الشارع السكني للعقار!", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (password.trim().isEmpty()) {
                            android.widget.Toast.makeText(context, "❌ يرجى إدخال كلمة مرور لحماية الإعلان العقاري وتعديله مستقبلاً!", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (password != confirmPassword) {
                            android.widget.Toast.makeText(context, "❌ كلمتا المرور غير متطابقتين!", android.widget.Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (true) {
                            val newProp = PropertyEntity(
                                id = property?.id ?: "",
                                sectionId = property?.sectionId ?: sectionId,
                                title = title,
                                description = description,
                                price = priceInput.toDoubleOrNull() ?: 0.0,
                                phone = phone,
                                ownerId = currentUserId,
                                ownerName = currentUserName.ifEmpty { "معلن عقاري" },
                                cityId = selectedCityId,
                                localNeighborhood = neighborhood.ifEmpty { "إدارة التطبيق" },
                                type = type,
                                propertyType = propertyType,
                                isActive = if (adminRole != "GUEST") true else (property?.isActive ?: false), // Auto-approve if added by admin
                                isApproved = if (adminRole != "GUEST") true else (property?.isApproved ?: false),
                                passwordHash = password,
                                maxImages = customMaxPhotosInput.toIntOrNull() ?: (property?.maxImages ?: 5),
                                pdfFileUri = pdfUriText,
                                pdfFileBase64 = pdfBase64Text,
                                pdfStatus = if (pdfBase64Text.isNotEmpty()) (property?.pdfStatus ?: "PENDING") else "",
                                images = propPhotosList
                            )
                            viewModel.saveProperty(newProp)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إرسال طلب الإضافة للتحقق والتنشيط", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

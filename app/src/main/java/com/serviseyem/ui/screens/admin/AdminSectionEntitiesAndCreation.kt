package com.serviseyem.ui.screens.admin

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.CategoryEntity
import com.serviseyem.data.CityEntity
import com.serviseyem.data.JobEntity
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.*
import com.serviseyem.utils.FirebaseStorageUploader
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch
import java.util.UUID

internal fun LazyListScope.adminEntitiesAndCreationSections(
    activeSubTab: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context,
    state: AdminPanelState,
    categories: List<CategoryEntity>,
    citiesList: List<CityEntity>,
    activatedProviders: List<ProviderEntity>
) {
    if (activeSubTab == "MANUAL_ADD") {
        item {
            Text("✨ الإضافة المباشرة الفورية من قِبل إدارة المنصة:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Text("اختر القسم المطلوب لإضافة وتفعيل البيانات مباشرة على الدليل دون انتظار:", fontSize = 11.sp, color = themeColors.textSecondary)
            Spacer(modifier = Modifier.height(6.dp))

            val addSubTabs = listOf(
                Pair("SERVICES", "إضافة فني / مهني 🔧"),
                Pair("PROPERTIES", "إضافة عقار 🏠"),
                Pair("STORES", "مركز تجاري 🏪24"),
                Pair("MEDICAL", "مركز طبي / عيادة 🏥"),
                Pair("RESTAURANTS", "مطعم / كافيه 🍔"),
                Pair("JOBS", "إعلان وظيفي 💼")
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(addSubTabs.size) { index ->
                    val st = addSubTabs[index]
                    val isSel = state.adminAddSubTab == st.first
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(if (isSel) themeColors.accent else themeColors.surface)
                            .clickable { state.adminAddSubTab = st.first }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .border(if (isSel) 2.dp else 1.dp, if (isSel) Color.White else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(30.dp))
                    ) {
                        Text(st.second, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.Black else Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (state.adminAddSubTab == "SERVICES") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = state.manualName, onValueChange = { state.manualName = it }, label = { Text("الاسم الكامل للحرفي/المهندِس") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = state.manualPhone, onValueChange = { state.manualPhone = it }, label = { Text("رقم الهاتف (مثال: 777644)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = state.manualStreet, onValueChange = { state.manualStreet = it }, label = { Text("الشارع أو الحي التفصيلي") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))

                        Text("اختر قسم الصيانة المستهدف:", fontSize = 11.sp, color = themeColors.textSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories, key = { it.id }) { cat ->
                                val isSel = state.manualCategoryId == cat.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) themeColors.accent else themeColors.surface)
                                        .clickable { state.manualCategoryId = cat.id }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .border(1.dp, if (isSel) Color.White else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                ) {
                                    Text(cat.name, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White)
                                }
                            }
                        }

                        Text("اختر المدينة اليمنية المحتوية للحي:", fontSize = 11.sp, color = themeColors.textSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(citiesList, key = { it.id }) { city ->
                                val isSel = state.manualCityId == city.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSel) themeColors.accent else themeColors.surface)
                                        .clickable { state.manualCityId = city.id }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .border(1.dp, if (isSel) Color.White else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                ) {
                                    Text(city.nameAr, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White)
                                }
                            }
                        }

                        val galleryScope = rememberCoroutineScope()
                        val galleryLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri: Uri? ->
                            uri?.let { selectedUri ->
                                galleryScope.launch {
                                    val path = FirebaseStorageUploader.getProviderProfilePath(UUID.randomUUID().toString())
                                    val uploadedUrl = FirebaseStorageUploader.uploadImageUri(context, selectedUri, path)
                                        .getOrElse { selectedUri.toString() }
                                    if (uploadedUrl.isNotEmpty()) {
                                        state.manualPhotoUrl = uploadedUrl
                                        Toast.makeText(context, "✅ تم اختيار الصورة ورفعها بنجاح!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }

                        OutlinedTextField(value = state.manualPhotoUrl, onValueChange = { state.manualPhotoUrl = it }, label = { Text("رابط صورة الفني الشخصية أو كود Base64") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📸 اختيار صورة من استوديو الهاتف", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = state.manualIsVipGolden,
                                onCheckedChange = { state.manualIsVipGolden = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706))
                            )
                            Text("تفعيل كرت VIP الذهبي المميز على الدليل فورا", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (state.manualName.trim().isEmpty() || state.manualPhone.trim().isEmpty()) {
                                    Toast.makeText(context, "الرجاء تعبئة الاسم ورقم الهاتف على الأقل للتفعيل", Toast.LENGTH_SHORT).show()
                                } else {
                                    val finalCat = if (state.manualCategoryId.isEmpty()) (categories.firstOrNull()?.id ?: "1") else state.manualCategoryId
                                    val finalCity = if (state.manualCityId.isEmpty()) (citiesList.firstOrNull()?.id ?: "ye_san") else state.manualCityId
                                    val priceVal = state.manualPriceValue.toDoubleOrNull() ?: 1500.0
                                    val finalStreet = if (state.manualStreet.trim().isEmpty()) "غير محدد" else state.manualStreet.trim()

                                    viewModel.addNewProviderCustom(
                                        name = state.manualName.trim(),
                                        phone = state.manualPhone.trim(),
                                        catId = finalCat,
                                        street = finalStreet,
                                        cityId = finalCity,
                                        profileImage = state.manualPhotoUrl.trim(),
                                        idCardImage = state.manualIdCardUrl.trim(),
                                        forensicImage = state.manualForensicUrl.trim(),
                                        price = priceVal,
                                        isVip = state.manualIsVipGolden
                                    )

                                    state.manualName = ""
                                    state.manualPhone = ""
                                    state.manualStreet = ""
                                    state.manualPhotoUrl = ""
                                    Toast.makeText(context, "🚀 تم إضافة وتفعيل مقدم الخدمة بنجاح!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("➕ إضافة الفني وتفعيله بالكامل فوراً", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (state.adminAddSubTab == "PROPERTIES") {
            item {
                var pTitle by remember { mutableStateOf("") }
                var pType by remember { mutableStateOf("شقة") }
                var pPrice by remember { mutableStateOf("") }
                var pPhone by remember { mutableStateOf("") }
                var pCity by remember { mutableStateOf("صنعاء") }
                var pDistrict by remember { mutableStateOf("") }
                var pDesc by remember { mutableStateOf("") }

                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🏠 إضافة عقار جديد معتمد فوراً", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

                        OutlinedTextField(value = pTitle, onValueChange = { pTitle = it }, label = { Text("عنوان العقار (مثال: شقة مفروشة فاخرة للبيع)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pType, onValueChange = { pType = it }, label = { Text("نوع العقار (شقة / أرض / بيت / محل)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pPrice, onValueChange = { pPrice = it }, label = { Text("السعر أو الإيجار (مثال: 50,000 ريال)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pPhone, onValueChange = { pPhone = it }, label = { Text("رقم هاتف المالك/المكتب") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pCity, onValueChange = { pCity = it }, label = { Text("المدينة") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pDistrict, onValueChange = { pDistrict = it }, label = { Text("المنطقة / الحي") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pDesc, onValueChange = { pDesc = it }, label = { Text("تفاصيل ومواصفات العقار") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)

                        Button(
                            onClick = {
                                if (pTitle.trim().isEmpty() || pPhone.trim().isEmpty()) {
                                    Toast.makeText(context, "يرجى كتابة عنوان العقار ورقم الهاتف", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveProperty(
                                        PropertyEntity(
                                            id = UUID.randomUUID().toString(),
                                            title = pTitle.trim(),
                                            propertyType = pType.trim(),
                                            price = pPrice.trim().toDoubleOrNull() ?: 0.0,
                                            phone = pPhone.trim(),
                                            cityId = pCity.trim(),
                                            localNeighborhood = pDistrict.trim(),
                                            description = pDesc.trim(),
                                            isApproved = true,
                                            isActive = true,
                                            createdAt = System.currentTimeMillis()
                                        )
                                    )
                                    pTitle = ""; pPrice = ""; pPhone = ""; pDesc = ""
                                    Toast.makeText(context, "✅ تم إضافة ونشر العقار بنجاح!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("➕ نشر ونشر العقار فوراً", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (state.adminAddSubTab == "STORES" || state.adminAddSubTab == "MEDICAL" || state.adminAddSubTab == "RESTAURANTS") {
            item {
                var sName by remember { mutableStateOf("") }
                var sPhone by remember { mutableStateOf("") }
                var sCity by remember { mutableStateOf("صنعاء") }
                var sAddress by remember { mutableStateOf("") }
                var sDesc by remember { mutableStateOf("") }

                val categoryTitle = when (state.adminAddSubTab) {
                    "MEDICAL" -> "🏥 إضافة مركز طبي أو عيادة تخصصية"
                    "RESTAURANTS" -> "🍔 إضافة مطعم أو كافيه مميز"
                    else -> "🏪 إضافة مركز تجاري أو محل خدمي"
                }

                val defaultCategory = when (state.adminAddSubTab) {
                    "MEDICAL" -> "مراكز طبية وعيادات"
                    "RESTAURANTS" -> "مطاعم وكافيهات"
                    else -> "مراكز تجارية ومحلات"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, themeColors.accent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(categoryTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

                        OutlinedTextField(value = sName, onValueChange = { sName = it }, label = { Text("اسم المنشأة/المحل/المركز") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = sPhone, onValueChange = { sPhone = it }, label = { Text("رقم الهاتف أو الواتساب") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = sCity, onValueChange = { sCity = it }, label = { Text("المدينة") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = sAddress, onValueChange = { sAddress = it }, label = { Text("العنوان والشارع التفصيلي") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = sDesc, onValueChange = { sDesc = it }, label = { Text("وصف الخدمات والتخصصات وقائمة العمل") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)

                        Button(
                            onClick = {
                                if (sName.trim().isEmpty() || sPhone.trim().isEmpty()) {
                                    Toast.makeText(context, "يرجى إدخال الاسم ورقم الهاتف", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveStore(
                                        StoreEntity(
                                            id = UUID.randomUUID().toString(),
                                            name = sName.trim(),
                                            phone = sPhone.trim(),
                                            cityId = sCity.trim(),
                                            localNeighborhood = sAddress.trim(),
                                            description = sDesc.trim(),
                                            categoryId = defaultCategory,
                                            isApproved = true,
                                            isActive = true,
                                            createdAt = System.currentTimeMillis()
                                        )
                                    )
                                    sName = ""; sPhone = ""; sAddress = ""; sDesc = ""
                                    Toast.makeText(context, "✅ تم إضافة وتفعيل $defaultCategory بنجاح!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("➕ إضافة وتفعيل المنشأة فوراً", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (state.adminAddSubTab == "JOBS") {
            item {
                var jTitle by remember { mutableStateOf("") }
                var jCompany by remember { mutableStateOf("") }
                var jPhone by remember { mutableStateOf("") }
                var jSalary by remember { mutableStateOf("") }
                var jCity by remember { mutableStateOf("صنعاء") }
                var jDesc by remember { mutableStateOf("") }

                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("💼 إضافة إعلان وظيفي جديد معتمد", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

                        OutlinedTextField(value = jTitle, onValueChange = { jTitle = it }, label = { Text("المسمى الوظيفي (مثال: محاسب مالي خبرة)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = jCompany, onValueChange = { jCompany = it }, label = { Text("اسم الشركة أو جهة العمل") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = jPhone, onValueChange = { jPhone = it }, label = { Text("رقم التواصل للتقديم") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = jSalary, onValueChange = { jSalary = it }, label = { Text("الراتب المتوقع أو يحدد بعد المقابلة") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = jCity, onValueChange = { jCity = it }, label = { Text("المدينة") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = jDesc, onValueChange = { jDesc = it }, label = { Text("الشروط والمتطلبات وساعات العمل") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)

                        Button(
                            onClick = {
                                if (jTitle.trim().isEmpty() || jPhone.trim().isEmpty()) {
                                    Toast.makeText(context, "يرجى كتابة المسمى الوظيفي ورقم التواصل", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveJob(
                                        JobEntity(
                                            id = UUID.randomUUID().toString(),
                                            title = jTitle.trim(),
                                            companyName = jCompany.trim(),
                                            phone = jPhone.trim(),
                                            salary = jSalary.trim(),
                                            cityId = jCity.trim(),
                                            description = jDesc.trim(),
                                            isApproved = true,
                                            isActive = true,
                                            createdAt = System.currentTimeMillis()
                                        )
                                    )
                                    jTitle = ""; jCompany = ""; jPhone = ""; jDesc = ""
                                    Toast.makeText(context, "✅ تم إعلان ونشر الوظيفة بنجاح!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("➕ نشر ونشر الإعلان الوظيفي فوراً", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (activeSubTab == "PROVIDERS") {
        item {
            Text("🏅 ترقية الفنيين وأعضاء دليل الدليل الشامل", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Text("البحث والتحكم في شارات الفنيين والأوسمة والتحكم المستقل والحذف:", fontSize = 11.sp, color = themeColors.textSecondary)
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            OutlinedTextField(
                value = state.activeProvidersSearchQuery,
                onValueChange = { state.activeProvidersSearchQuery = it },
                label = { Text("البحث في دليل الفنيين المعتمدين...") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = themeColors.accent) }
            )
        }

        val filteredProviders = activatedProviders.filter {
            it.name.contains(state.activeProvidersSearchQuery, ignoreCase = true) ||
                it.phone.contains(state.activeProvidersSearchQuery, ignoreCase = true) ||
                it.area.contains(state.activeProvidersSearchQuery, ignoreCase = true)
        }

        items(filteredProviders, key = { it.id }) { p ->
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            val catName = if (p.categoryId == "other" && p.customCategoryName.isNotEmpty()) p.customCategoryName else (categories.find { it.id == p.categoryId }?.name ?: "خدمات عامة")
                            Text("المهنة: $catName | المنطقة: ${p.area}", fontSize = 11.sp, color = themeColors.textSecondary)
                        }
                        IconButton(onClick = { viewModel.removeProvider(p.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الفني نهائياً", tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(checked = p.isVip, onCheckedChange = { viewModel.pinProvider(p.id, it) }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706)), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("VIP ذهبي", fontSize = 10.sp, color = Color.White, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(checked = p.isVerified, onCheckedChange = { viewModel.verifyProviderBadge(p.id, it) }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6)), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("موثق حساب", fontSize = 10.sp, color = Color.White, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(checked = p.isRecommended, onCheckedChange = { viewModel.recommendProvider(p.id, it) }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEC4899)), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("موصى به", fontSize = 10.sp, color = Color.White, maxLines = 1)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(checked = p.isChatDisabled, onCheckedChange = { viewModel.setProviderChatDisabled(p.id, it) }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444)), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("إيقاف الدردشة 🔇", fontSize = 9.sp, color = Color.LightGray, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(checked = p.isNotificationsDisabled, onCheckedChange = { viewModel.setProviderNotificationsDisabled(p.id, it) }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444)), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("إيقاف الإشعارات 🔕", fontSize = 9.sp, color = Color.LightGray, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(checked = p.isPaymentRequired, onCheckedChange = { viewModel.setProviderPaymentRequired(p.id, it) }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981)), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("ربط بالدفع 💳", fontSize = 9.sp, color = Color.LightGray, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeSubTab == "CATEGORIES") {
        item {
            Text("🗂️ إدارة الأقسام والقطاعات بالمنصة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Text("يمكنك التحكم بأقسام الصيانة أو إضافة وتفعيل قطاعات رئيسية جديدة كلياً (كقسم التوصيل، المتاجر، العقارات):", fontSize = 11.sp, color = themeColors.textSecondary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { state.categoryManagementMode = "MAINTENANCE" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (state.categoryManagementMode == "MAINTENANCE") themeColors.accent else Color.DarkGray),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🛠️ أقسام الصيانة والمهن", fontSize = 10.sp, color = if (state.categoryManagementMode == "MAINTENANCE") Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { state.categoryManagementMode = "PLATFORM_SECTIONS" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (state.categoryManagementMode == "PLATFORM_SECTIONS") themeColors.accent else Color.DarkGray),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🌐 القطاعات والأنشطة الرئيسية", fontSize = 10.sp, color = if (state.categoryManagementMode == "PLATFORM_SECTIONS") Color.Black else Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (state.categoryManagementMode == "PLATFORM_SECTIONS") {
            item {
                AdminStoresPropertiesPanel(viewModel = viewModel, themeColors = themeColors)
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("إضافة قسم صيانة جديد ➕", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                        OutlinedTextField(value = state.newCatName, onValueChange = { state.newCatName = it }, label = { Text("اسم القسم (مثال: سباكة، كهرباء...)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        OutlinedTextField(value = state.newCatIcon, onValueChange = { state.newCatIcon = it }, label = { Text("أيقونة إيموجي مميزة (مثال: 🚰, ⚡)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))

                        var isNewMainCategory by remember { mutableStateOf(true) }
                        var selectedParentCatId by remember { mutableStateOf("") }

                        Text("نوع القسم:", fontSize = 11.sp, color = Color.White)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { isNewMainCategory = true },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isNewMainCategory) themeColors.accent else Color.DarkGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("قسم رئيسي 🟢", fontSize = 10.sp, color = if (isNewMainCategory) Color.Black else Color.White)
                            }
                            Button(
                                onClick = { isNewMainCategory = false },
                                colors = ButtonDefaults.buttonColors(containerColor = if (!isNewMainCategory) themeColors.accent else Color.DarkGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("قسم فرعي 🟡", fontSize = 10.sp, color = if (!isNewMainCategory) Color.Black else Color.White)
                            }
                        }

                        if (!isNewMainCategory) {
                            val mainCategories = categories.filter { it.isMainCategory || it.parentId.isEmpty() }
                            Text("اختر القسم الرئيسي التابع له:", fontSize = 10.sp, color = themeColors.accent)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                mainCategories.forEach { parent ->
                                    val isSelected = selectedParentCatId == parent.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedParentCatId = parent.id },
                                        label = { Text("${parent.icon} ${parent.name}", fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = themeColors.accent,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (state.newCatName.trim().isEmpty() || state.newCatIcon.trim().isEmpty()) {
                                    Toast.makeText(context, "الرجاء تعبئة اسم القسم والأيقونة", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addNewCategory(
                                        nameAr = state.newCatName.trim(),
                                        nameEn = state.newCatName.trim(),
                                        icon = state.newCatIcon.trim(),
                                        description = "",
                                        parentId = if (!isNewMainCategory) selectedParentCatId else "",
                                        isMainCategory = isNewMainCategory
                                    )
                                    state.newCatName = ""
                                    state.newCatIcon = ""
                                    selectedParentCatId = ""
                                    Toast.makeText(context, "تمت إضافة قسم الصيانة بنجاح 🗂️", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إضافة القسم وتفعيله فوراً", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(categories, key = { it.id }) { cat ->
                val index = categories.indexOf(cat)
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(cat.icon, fontSize = 20.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(cat.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                if (cat.isPinned) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = "مثبت", tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.togglePinCategory(cat.id) }) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "تثبيت القسم", tint = if (cat.isPinned) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.6f))
                            }
                            IconButton(onClick = {
                                state.showEditCategoryObj = cat
                                state.editCatName = cat.name
                                state.editCatIcon = cat.icon
                            }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل القسم", tint = themeColors.accent)
                            }
                            IconButton(onClick = {
                                state.showMergeCategoryObj = cat
                                state.selectedTargetCategoryIdForMerge = categories.firstOrNull { it.id != cat.id }?.id ?: ""
                            }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "دمج القسم", tint = Color.Cyan)
                            }
                            IconButton(
                                onClick = {
                                    val list = categories.toMutableList()
                                    if (index > 0) {
                                        list.removeAt(index)
                                        list.add(index - 1, cat)
                                        viewModel.reorderCategories(list)
                                    }
                                },
                                enabled = index > 0
                            ) {
                                Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "ترتيب للأعلى", tint = if (index > 0) themeColors.accent else Color.Gray.copy(alpha = 0.5f))
                            }
                            IconButton(
                                onClick = {
                                    val list = categories.toMutableList()
                                    if (index < list.size - 1) {
                                        list.removeAt(index)
                                        list.add(index + 1, cat)
                                        viewModel.reorderCategories(list)
                                    }
                                },
                                enabled = index < categories.size - 1
                            ) {
                                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "ترتيب للأسفل", tint = if (index < categories.size - 1) themeColors.accent else Color.Gray.copy(alpha = 0.5f))
                            }
                            IconButton(onClick = { state.showDeleteCategoryConfirmId = cat.id }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف القسم", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeSubTab == "CITIES") {
        item {
            Text("🗺️ إدارة محافظات ومدن الجمهورية اليمنية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Text("إضافة المحافظات والمدن المستهدفة بالخدمة وتصفح المضاف حالياً بالمنصة:", fontSize = 11.sp, color = themeColors.textSecondary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("إضافة مدينة/محافظة يمنية جديدة ➕", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)

                    OutlinedTextField(value = state.newCityArName, onValueChange = { state.newCityArName = it }, label = { Text("الاسم باللغة العربية (مثال: صنعاء، عدن...)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = state.newCityEnName, onValueChange = { state.newCityEnName = it }, label = { Text("الاسم باللغة الإنجليزية (مثال: Sana'a, Aden...)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = state.newCityIcon, onValueChange = { state.newCityIcon = it }, label = { Text("أيقونة/إيموجي رمزية (مثال: 🏰, 🏖️, 📍)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))

                    Button(
                        onClick = {
                            if (state.newCityArName.trim().isEmpty() || state.newCityEnName.trim().isEmpty()) {
                                Toast.makeText(context, "الرجاء ملء الاسم العربي والإنجليزي للمحافظة", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addNewCity(
                                    nameAr = state.newCityArName.trim(),
                                    nameEn = state.newCityEnName.trim(),
                                    icon = state.newCityIcon.trim().ifEmpty { "📍" }
                                )
                                state.newCityArName = ""
                                state.newCityEnName = ""
                                state.newCityIcon = "📍"
                                Toast.makeText(context, "تمت إضافة المحافظة بنجاح 🗺️", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تأكيد إضافة المحافظة", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(citiesList, key = { city -> city.id }) { city ->
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(city.icon.ifEmpty { "📍" }, fontSize = 20.sp)
                        Column {
                            Text(city.nameAr, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(city.nameEn, fontSize = 11.sp, color = themeColors.textSecondary)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { state.showEditCityObj = city }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل المحافظة", tint = themeColors.accent)
                        }
                        IconButton(onClick = {
                            viewModel.removeCity(city.id)
                            Toast.makeText(context, "تم حذف المحافظة بنجاح", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف المحافظة", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

package com.serviseyem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

// --------------------------------------------------------
// 9. LEGACY ADMIN STORES & PROPERTIES MANAGEMENT PANEL (MOVED TO AdminStoresPropertiesPanel.kt)
// --------------------------------------------------------
@Composable
fun LegacyAdminStoresPropertiesPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val ratings by viewModel.ratings.collectAsState()
    val settingsState by viewModel.settings.collectAsState()

    val activeStores = remember(stores) { stores.filter { !it.isDeleted } }
    val archivedStores = remember(stores) { stores.filter { it.isDeleted } }
    val activeProperties = remember(properties) { properties.filter { !it.isDeleted } }
    val archivedProperties = remember(properties) { properties.filter { it.isDeleted } }

    var storeToEdit by remember { mutableStateOf<StoreEntity?>(null) }
    var propertyToEdit by remember { mutableStateOf<PropertyEntity?>(null) }

    var isStoresEnabled by remember(settingsState.isStoresEnabled) { mutableStateOf(settingsState.isStoresEnabled) }
    var isPropertiesEnabled by remember(settingsState.isPropertiesEnabled) { mutableStateOf(settingsState.isPropertiesEnabled) }
    var dynamicTabs by remember(settingsState.dynamicTabsList) { mutableStateOf(settingsState.dynamicTabsList) }

    // Terms and Conditions inputs
    var storesTerms by remember(settingsState.storesRegistrationTerms) { mutableStateOf(settingsState.storesRegistrationTerms) }
    var propertiesTerms by remember(settingsState.propertiesRegistrationTerms) { mutableStateOf(settingsState.propertiesRegistrationTerms) }
    var maxStorePhotos by remember(settingsState.maxStorePhotos) { mutableStateOf(settingsState.maxStorePhotos.toString()) }
    var maxPropertyPhotos by remember(settingsState.maxPropertyPhotos) { mutableStateOf(settingsState.maxPropertyPhotos.toString()) }
    var showStoresPhotos by remember(settingsState.showStoresPhotosOption) { mutableStateOf(settingsState.showStoresPhotosOption) }
    var showPropertiesPhotos by remember(settingsState.showPropertiesPhotosOption) { mutableStateOf(settingsState.showPropertiesPhotosOption) }

    // Dynamic Sections state (parsed from database)
    var localSectionsList by remember(settingsState.dynamicSectionsData) {
        mutableStateOf(com.serviseyem.data.DynamicSection.parseDynamicSections(settingsState.dynamicSectionsData))
    }

    var confirmActionType by remember { mutableStateOf<String?>(null) }
    var confirmTargetId by remember { mutableStateOf("") }
    var confirmTargetName by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Creating section state
    var newSecId by remember { mutableStateOf("") }
    var newSecName by remember { mutableStateOf("") }
    var newSecIcon by remember { mutableStateOf("🏪") }
    var newSecType by remember { mutableStateOf("store") } // "store" or "property"
    var newSecTerms by remember { mutableStateOf("شروط تسجيل القسم الجديد: يرجى التحقق من صحة ومصداقية كافة البيانات.") }
    var newSecMaxPhotos by remember { mutableStateOf("5") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "⚙️ إعدادات التحكم بالأقسام والميزات الحية",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.accent
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تفعيل تبويب وقسم المحلات التجارية بالكامل", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = isStoresEnabled,
                        onCheckedChange = { isStoresEnabled = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تفعيل تبويب وقسم العقارات بالكامل", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = isPropertiesEnabled,
                        onCheckedChange = { isPropertiesEnabled = it }
                    )
                }

                OutlinedTextField(
                    value = dynamicTabs,
                    onValueChange = { dynamicTabs = it },
                    label = { Text("قائمة التبويبات الفعالة (مفصولة بفواصل)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text("📝 شروط وسياسات تسجيل المتاجر:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                OutlinedTextField(
                    value = storesTerms,
                    onValueChange = { storesTerms = it },
                    placeholder = { Text("أدخل الشروط والتعليمات لتسجيل المحلات والمطاعم...", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Text("📝 شروط وسياسات إدراج العقارات:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                OutlinedTextField(
                    value = propertiesTerms,
                    onValueChange = { propertiesTerms = it },
                    placeholder = { Text("أدخل شروط ووثائق إدراج العقارات والأراضي الحاكمة للتفعيل...", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("خيار رفع صور متعددة للمحلات", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = showStoresPhotos,
                        onCheckedChange = { showStoresPhotos = it }
                    )
                }

                OutlinedTextField(
                    value = maxStorePhotos,
                    onValueChange = { maxStorePhotos = it },
                    label = { Text("أقصى عدد صور مسموح به للمتجر (من 5 إلى 10)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("خيار رفع صور متعددة للعقارات", fontSize = 11.sp, color = Color.White)
                    Switch(
                        checked = showPropertiesPhotos,
                        onCheckedChange = { showPropertiesPhotos = it }
                    )
                }

                OutlinedTextField(
                    value = maxPropertyPhotos,
                    onValueChange = { maxPropertyPhotos = it },
                    label = { Text("أقصى عدد صور مسموح به للعقار (من 5 إلى 10)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }

        // --- DYNAMIC SECTIONS CRUD PANEL ---
        Text(
            text = "🛠️ إدارة وتعديل الأقسام والتبويبات الحية",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.accent
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("إضافة قسم ديناميكي مخصص جديد:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = newSecId,
                        onValueChange = { newSecId = it },
                        label = { Text("معرف القسم (ID)", fontSize = 9.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = newSecName,
                        onValueChange = { newSecName = it },
                        label = { Text("اسم القسم (التبويب)", fontSize = 9.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = newSecIcon,
                        onValueChange = { newSecIcon = it },
                        label = { Text("أيقونة/إيموجي", fontSize = 9.sp) },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("نوع طبيعة القسم:", fontSize = 10.sp, color = Color.White)
                    listOf(Pair("store", "محل تجاري 🏪"), Pair("property", "إعلان عقاري 🏠")).forEach { (key, name) ->
                        val isSel = newSecType == key
                        Button(
                            onClick = { newSecType = key },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSel) themeColors.accent else Color.DarkGray),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(name, fontSize = 9.sp, color = if (isSel) Color.Black else Color.White)
                        }
                    }
                }

                Button(
                    onClick = {
                        if (newSecId.isNotEmpty() && newSecName.isNotEmpty()) {
                            val newSection = com.serviseyem.data.DynamicSection(
                                id = newSecId.trim().lowercase(),
                                name = newSecName.trim(),
                                icon = newSecIcon.trim(),
                                type = newSecType,
                                isEnabled = true,
                                order = localSectionsList.size + 1,
                                terms = newSecTerms,
                                maxPhotos = newSecMaxPhotos.toIntOrNull() ?: 5
                            )
                            localSectionsList = localSectionsList + newSection
                            newSecId = ""
                            newSecName = ""
                            newSecIcon = "🏪"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إضافة هذا القسم الجديد للمسودة ➕", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)

                Text("أقسام المسودة الحالية (${localSectionsList.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                localSectionsList.forEachIndexed { index, section ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(section.icon, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${section.name} (${if (section.type == "store") "محل" else "عقار"})", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Order controls
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val list = localSectionsList.toMutableList()
                                                val temp = list[index]
                                                list[index] = list[index - 1]
                                                list[index - 1] = temp
                                                localSectionsList = list
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Text("🔼", fontSize = 10.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            if (index < localSectionsList.size - 1) {
                                                val list = localSectionsList.toMutableList()
                                                val temp = list[index]
                                                list[index] = list[index + 1]
                                                list[index + 1] = temp
                                                localSectionsList = list
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Text("🔽", fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = {
                                            localSectionsList = localSectionsList.filter { it.id != section.id }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(20.dp).widthIn(max = 50.dp)
                                    ) {
                                        Text("حذف", fontSize = 8.sp, color = Color.White)
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("تفعيل هذا التبويب للمواطنين", fontSize = 9.sp, color = Color.White)
                                Switch(
                                    checked = section.isEnabled,
                                    onCheckedChange = { checked ->
                                        localSectionsList = localSectionsList.map {
                                            if (it.id == section.id) it.copy(isEnabled = checked) else it
                                        }
                                    }
                                )
                            }

                            OutlinedTextField(
                                value = section.name,
                                onValueChange = { newVal ->
                                    localSectionsList = localSectionsList.map {
                                        if (it.id == section.id) it.copy(name = newVal) else it
                                    }
                                },
                                label = { Text("تعديل اسم القسم", fontSize = 9.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                        }
                    }
                }
            }
        }

        // --- ATOMIC MANUAL SAVE BUTTON ---
        Button(
            onClick = {
                val updatedSettings = settingsState.copy(
                    isStoresEnabled = isStoresEnabled,
                    isPropertiesEnabled = isPropertiesEnabled,
                    dynamicTabsList = dynamicTabs,
                    storesRegistrationTerms = storesTerms,
                    propertiesRegistrationTerms = propertiesTerms,
                    maxStorePhotos = maxStorePhotos.toIntOrNull() ?: 5,
                    maxPropertyPhotos = maxPropertyPhotos.toIntOrNull() ?: 5,
                    showStoresPhotosOption = showStoresPhotos,
                    showPropertiesPhotosOption = showPropertiesPhotos,
                    dynamicSectionsData = com.serviseyem.data.DynamicSection.serializeDynamicSections(localSectionsList)
                )
                viewModel.saveCustomSettingsState(updatedSettings)
                viewModel.triggerNotification("💾 تم حفظ وتطبيق كافة التعديلات والإعدادات بنجاح فورياً!")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("💾 حفظ وتطبيق جميع الإعدادات فورياً", fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Bold)
        }

        // Stores Management List
        Text("🏢 مراجعة وإدارة المتاجر المسجلة (${activeStores.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        activeStores.forEach { store ->
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(store.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (store.isActive) "● نشط" else "○ قيد المراجعة",
                            fontSize = 10.sp,
                            color = if (store.isActive) Color.Green else Color.Yellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("الهاتف: ${store.phone} | الحي: ${store.localNeighborhood}", fontSize = 9.sp, color = themeColors.textSecondary)
                    
                    if (store.password.isNotEmpty()) {
                        Text("🔑 كلمة مرور المالك المشرف: ${store.password}", fontSize = 9.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                    }

                    // PDF Document verification block
                    if (store.pdfFileBase64.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📄 وثيقة ملف الـ PDF للمحل مرفقة:", fontSize = 10.sp, color = Color.White)
                                    Text(
                                        text = when (store.pdfStatus) {
                                            "APPROVED" -> "✅ معتمد وموافق عليه"
                                            "REJECTED" -> "❌ مرفوض ومستبعد"
                                            else -> "⏳ قيد التدقيق والمراجعة"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (store.pdfStatus) {
                                            "APPROVED" -> Color.Green
                                            "REJECTED" -> Color.Red
                                            else -> Color.Yellow
                                        }
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { viewModel.approveStorePdf(store.id, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("قبول وتفعيل الـ PDF", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.approveStorePdf(store.id, false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("رفض المستند", fontSize = 9.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Premium Checkboxes Grid
                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = store.isVip,
                                    onCheckedChange = { viewModel.setStoreVip(store.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706)),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("VIP ذهبي", fontSize = 8.sp, color = Color.White)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = store.isVerified,
                                    onCheckedChange = { viewModel.setStoreVerified(store.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6)),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("موثق حساب", fontSize = 8.sp, color = Color.White)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = store.isRecommended,
                                    onCheckedChange = { viewModel.setStoreRecommended(store.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEC4899)),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("موصى به", fontSize = 8.sp, color = Color.White)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = store.isChatDisabled,
                                    onCheckedChange = { viewModel.setStoreChatDisabled(store.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444)),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("إيقاف الدردشة 🔇", fontSize = 8.sp, color = Color.LightGray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
                                Checkbox(
                                    checked = store.isNotificationsDisabled,
                                    onCheckedChange = { viewModel.setStoreNotificationsDisabled(store.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444)),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("إيقاف الإشعارات 🔕", fontSize = 8.sp, color = Color.LightGray)
                            }
                        }
                    }

                    if (store.isBlocked) {
                        Surface(
                            color = Color(0xFF7F1D1D),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "🚫 هذا المحل محظور حالياً من قبل الأدمن ${if (store.blockReason.isNotEmpty()) "(${store.blockReason})" else ""}",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { storeToEdit = store },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text("تعديل", fontSize = 10.sp, color = Color.Black)
                        }

                        Button(
                            onClick = { viewModel.toggleBlockStore(store.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (store.isBlocked) Color(0xFF16A34A) else Color(0xFFDC2626)),
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text(
                                text = if (store.isBlocked) "إلغاء الحظر 🟢" else "حظر المحل 🚫",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                confirmActionType = if (store.isActive) "DEACTIVATE_STORE" else "ACTIVATE_STORE"
                                confirmTargetId = store.id
                                confirmTargetName = store.name
                                showConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (store.isActive) Color.Gray else Color.Green),
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text(if (store.isActive) "إلغاء التفعيل" else "تفعيل / موافقة", fontSize = 9.sp, color = Color.Black)
                        }

                        Button(
                            onClick = { viewModel.setStorePinned(store.id, !store.isPinned) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (store.isPinned) Color.Yellow else Color.DarkGray),
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text(if (store.isPinned) "إلغاء التثبيت" else "تثبيت في الصدارة", fontSize = 9.sp, color = Color.Black)
                        }

                        Button(
                            onClick = {
                                confirmActionType = "DELETE_STORE"
                                confirmTargetId = store.id
                                confirmTargetName = store.name
                                showConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text("حذف", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Properties Management List
        Text("🏠 مراجعة وإدارة عقارات البيع والإيجار (${activeProperties.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        activeProperties.forEach { prop ->
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(prop.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (prop.isActive) "● نشط" else "○ قيد المراجعة",
                            fontSize = 10.sp,
                            color = if (prop.isActive) Color.Green else Color.Yellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("السعر: ${prop.price} YER | هاتف: ${prop.phone}", fontSize = 9.sp, color = themeColors.textSecondary)
                    
                    if (prop.password.isNotEmpty()) {
                        Text("🔑 كلمة مرور المعلن المشرف: ${prop.password}", fontSize = 9.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                    }

                    // PDF Document verification block
                    if (prop.pdfFileBase64.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📄 وثائق ملكية/تفاصيل PDF مرفقة:", fontSize = 10.sp, color = Color.White)
                                    Text(
                                        text = when (prop.pdfStatus) {
                                            "APPROVED" -> "✅ معتمد وموافق عليه"
                                            "REJECTED" -> "❌ مرفوض ومستبعد"
                                            else -> "⏳ قيد التدقيق والمراجعة"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (prop.pdfStatus) {
                                            "APPROVED" -> Color.Green
                                            "REJECTED" -> Color.Red
                                            else -> Color.Yellow
                                        }
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { viewModel.approvePropertyPdf(prop.id, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("قبول وتفعيل الـ PDF", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { viewModel.approvePropertyPdf(prop.id, false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("رفض المستند", fontSize = 9.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Premium Checkboxes Grid
                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = prop.isVip,
                                    onCheckedChange = { viewModel.setPropertyVip(prop.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706)),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("VIP ذهبي", fontSize = 8.sp, color = Color.White)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = prop.isVerified,
                                    onCheckedChange = { viewModel.setPropertyVerified(prop.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6)),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("موثق حساب", fontSize = 8.sp, color = Color.White)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = prop.isRecommended,
                                    onCheckedChange = { viewModel.setPropertyRecommended(prop.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEC4899)),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("موصى به", fontSize = 8.sp, color = Color.White)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = prop.isChatDisabled,
                                    onCheckedChange = { viewModel.setPropertyChatDisabled(prop.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444)),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("إيقاف الدردشة 🔇", fontSize = 8.sp, color = Color.LightGray)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
                                Checkbox(
                                    checked = prop.isNotificationsDisabled,
                                    onCheckedChange = { viewModel.setPropertyNotificationsDisabled(prop.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444)),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("إيقاف الإشعارات 🔕", fontSize = 8.sp, color = Color.LightGray)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { propertyToEdit = prop },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text("تعديل", fontSize = 10.sp, color = Color.Black)
                        }

                        Button(
                            onClick = {
                                confirmActionType = if (prop.isActive) "DEACTIVATE_PROP" else "ACTIVATE_PROP"
                                confirmTargetId = prop.id
                                confirmTargetName = prop.title
                                showConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (prop.isActive) Color.Gray else Color.Green),
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text(if (prop.isActive) "إلغاء تفعيل" else "تفعيل ونشر", fontSize = 9.sp, color = Color.Black)
                        }

                        Button(
                            onClick = { viewModel.setPropertyPinned(prop.id, !prop.isPinned) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (prop.isPinned) Color.Yellow else Color.DarkGray),
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text(if (prop.isPinned) "إلغاء التثبيت" else "تمييز وتثبيت", fontSize = 9.sp, color = Color.Black)
                        }

                        Button(
                            onClick = {
                                confirmActionType = "DELETE_PROP"
                                confirmTargetId = prop.id
                                confirmTargetName = prop.title
                                showConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            Text("حذف", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // 📦 ARCHIVE / DELETED ITEMS SECTION
        Text("📦 الأرشيف والمحذوفات (الحذف الناعم) - محلات (${archivedStores.size}) | عقارات (${archivedProperties.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
        if (archivedStores.isNotEmpty() || archivedProperties.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (archivedStores.isNotEmpty()) {
                        Text("🏢 المحلات المحذوفة في الأرشيف:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        archivedStores.forEach { store ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.2f)).padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(store.name, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("الهاتف: ${store.phone}", fontSize = 9.sp, color = Color.LightGray)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = { viewModel.restoreStore(store.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("استعادة", fontSize = 9.sp, color = Color.Black)
                                    }
                                    Button(
                                        onClick = {
                                            confirmActionType = "HARD_DELETE_STORE"
                                            confirmTargetId = store.id
                                            confirmTargetName = store.name
                                            showConfirmDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("حذف نهائي", fontSize = 9.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    if (archivedProperties.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🏠 العقارات المحذوفة في الأرشيف:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        archivedProperties.forEach { prop ->
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.2f)).padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prop.title, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("السعر: ${prop.price} YER", fontSize = 9.sp, color = Color.LightGray)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Button(
                                        onClick = { viewModel.restoreProperty(prop.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("استعادة", fontSize = 9.sp, color = Color.Black)
                                    }
                                    Button(
                                        onClick = {
                                            confirmActionType = "HARD_DELETE_PROP"
                                            confirmTargetId = prop.id
                                            confirmTargetName = prop.title
                                            showConfirmDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("حذف نهائي", fontSize = 9.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text("لا توجد عناصر في الأرشيف حالياً", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Orders Management List
        Text("🛍️ الطلبات والمعاملات المالية الحية (${orders.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        orders.forEach { ord ->
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("السلعة: ${ord.productName} (الكمية: ${ord.quantity})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("إجمالي السعر: ${ord.totalAmount} YER", fontSize = 11.sp, color = themeColors.accent)
                    Text("المشتري: ${ord.customerName} (${ord.customerPhone})", fontSize = 9.sp, color = themeColors.textSecondary)
                    Text("الموقع: ${ord.customerArea}", fontSize = 9.sp, color = themeColors.textSecondary)
                    Text("رقم العملية المالية: ${ord.paymentId}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.updateOrderStatus(ord.id, "COMPLETED") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تأكيد استلام الدفعة والطلب", fontSize = 9.sp, color = Color.Black)
                        }
                        Button(
                            onClick = { viewModel.updateOrderStatus(ord.id, "CANCELLED") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("رفض / إلغاء المعاملة", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        storeToEdit?.let { store ->
            StoreCreateEditDialog(
                store = store,
                viewModel = viewModel,
                themeColors = themeColors,
                onDismiss = { storeToEdit = null },
                sectionId = store.sectionId
            )
        }

        propertyToEdit?.let { prop ->
            PropertyCreateEditDialog(
                property = prop,
                viewModel = viewModel,
                themeColors = themeColors,
                onDismiss = { propertyToEdit = null },
                sectionId = prop.sectionId
            )
        }

        if (showConfirmDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("⚠️ تأكيد العملية", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Text(
                        text = when (confirmActionType) {
                            "ACTIVATE_STORE" -> "هل أنت متأكد من تفعيل وموافقة المتجر \"$confirmTargetName\" ونشره للجمهور؟"
                            "DEACTIVATE_STORE" -> "هل أنت متأكد من إلغاء تفعيل المتجر \"$confirmTargetName\" وإخفائه عن الجمهور؟"
                            "DELETE_STORE" -> "هل أنت متأكد من نقل المتجر \"$confirmTargetName\" إلى الأرشيف (حذف ناعم)؟"
                            "HARD_DELETE_STORE" -> "⚠️ تحذير: هل أنت متأكد من الحذف الجذري والنهائي للمتجر \"$confirmTargetName\" من Firestore؟ لا يمكن التراجع عن هذه الخطوة!"
                            "ACTIVATE_PROP" -> "هل أنت متأكد من تفعيل وموافقة العقار \"$confirmTargetName\" ونشره للجمهور؟"
                            "DEACTIVATE_PROP" -> "هل أنت متأكد من إلغاء تفعيل العقار \"$confirmTargetName\" وإخفائه عن الجمهور؟"
                            "DELETE_PROP" -> "هل أنت متأكد من نقل العقار \"$confirmTargetName\" إلى الأرشيف (حذف ناعم)؟"
                            "HARD_DELETE_PROP" -> "⚠️ تحذير: هل أنت متأكد من الحذف الجذري والنهائي للعقار \"$confirmTargetName\" من Firestore؟ لا يمكن التراجع عن هذه الخطوة!"
                            else -> "هل تريد الاستمرار في هذه العملية؟"
                        },
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmDialog = false
                            when (confirmActionType) {
                                "ACTIVATE_STORE" -> viewModel.setStoreActive(confirmTargetId, true)
                                "DEACTIVATE_STORE" -> viewModel.setStoreActive(confirmTargetId, false)
                                "DELETE_STORE" -> viewModel.deleteStore(confirmTargetId)
                                "HARD_DELETE_STORE" -> viewModel.deleteStorePermanently(confirmTargetId)
                                "ACTIVATE_PROP" -> viewModel.setPropertyActive(confirmTargetId, true)
                                "DEACTIVATE_PROP" -> viewModel.setPropertyActive(confirmTargetId, false)
                                "DELETE_PROP" -> viewModel.deleteProperty(confirmTargetId)
                                "HARD_DELETE_PROP" -> viewModel.deletePropertyPermanently(confirmTargetId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (confirmActionType?.contains("HARD_DELETE") == true) Color.Red else themeColors.accent)
                    ) {
                        Text("نعم، استمر", color = if (confirmActionType?.contains("HARD_DELETE") == true) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("إلغاء", color = Color.White)
                    }
                },
                containerColor = themeColors.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

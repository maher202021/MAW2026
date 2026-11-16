package com.serviseyem

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

// --------------------------------------------------------
// 2. STORES TAB CONTENT & STORE CARD
// --------------------------------------------------------
@Composable
fun StoresTabContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onStoreClick: (StoreEntity) -> Unit,
    onAddStoreClick: () -> Unit,
    sectionId: String = "stores"
) {
    val stores by viewModel.stores.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val currentPhone by viewModel.currentUserPhone.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isEn = currentLang == "en"

    val sectionCategories = remember(categories, sectionId) {
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

    var searchQuery by remember { mutableStateOf("") }
    var selectedCityId by remember { mutableStateOf("") }
    var selectedCatId by remember { mutableStateOf("") }
    var itemsToShowLimit by remember { mutableStateOf(10) }

    val activeStores = remember(stores, searchQuery, selectedCityId, selectedCatId, sectionId) {
        stores.filter { store ->
            if (store.isDeleted) return@filter false
            
            val sec = store.sectionId.lowercase().trim()
            val nameNorm = store.name.lowercase().trim()
                .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ة", "ه")
            val descNorm = store.description.lowercase().trim()
                .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ة", "ه")
            val cat = store.categoryId.lowercase().trim()

            val isFoodOrRest = sec == "restaurants" || sec == "food" || cat == "restaurants" || cat == "food" ||
                    nameNorm.contains("مطعم") || nameNorm.contains("كافيه") || nameNorm.contains("وجب") ||
                    nameNorm.contains("اسماك") || nameNorm.contains("ماكولات") || nameNorm.contains("شاورما") ||
                    nameNorm.contains("بيتزا") || nameNorm.contains("حلويات") || nameNorm.contains("مخبز") ||
                    nameNorm.contains("عصائر") || nameNorm.contains("عصير") || nameNorm.contains("كيك") ||
                    descNorm.contains("مطعم") || descNorm.contains("وجبات") || descNorm.contains("ماكولات") || descNorm.contains("كافيه")

            val isMedical = sec == "medical" || sec == "centers" || cat == "medical" || cat == "centers" ||
                    nameNorm.contains("مستشفى") || nameNorm.contains("عياد") || nameNorm.contains("مركز طبي") ||
                    nameNorm.contains("مختبر") || nameNorm.contains("صيدلي") || nameNorm.contains("دكتور") ||
                    nameNorm.contains("طبي") || nameNorm.contains("اطباء") || nameNorm.contains("اسنان") ||
                    descNorm.contains("مستشفى") || descNorm.contains("عياد") || descNorm.contains("طبي")

            val isRealEstate = sec == "realestate" || sec == "properties" || cat == "realestate" ||
                    nameNorm.contains("عقار") || nameNorm.contains("ارض") || nameNorm.contains("فيلا") || nameNorm.contains("شقه") ||
                    descNorm.contains("عقار") || descNorm.contains("ارض")

            val matchesSection = when (sectionId.lowercase()) {
                "restaurants", "food" -> isFoodOrRest
                "medical", "centers" -> isMedical
                "realestate", "properties" -> isRealEstate
                "stores", "shops", "malls" -> !isFoodOrRest && !isMedical && !isRealEstate
                else -> store.sectionId == sectionId
            }

            val queryNorm = searchQuery.trim().lowercase()
                .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ة", "ه")

            val matchesSearch = queryNorm.isEmpty() ||
                    nameNorm.contains(queryNorm) ||
                    descNorm.contains(queryNorm) ||
                    store.productAttachmentsJson.lowercase().replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ة", "ه").contains(queryNorm) ||
                    store.specialOffersJson.lowercase().replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ة", "ه").contains(queryNorm)

            val isAdminUser = adminRole == "ADMIN" || adminRole == "SUPER_ADMIN" || adminRole == "MAIN_ADMIN" || adminRole == "OWNER"

            matchesSection &&
            (store.isApproved || store.ownerId == currentPhone || isAdminUser) &&
            matchesSearch &&
            (selectedCityId.isEmpty() || store.cityId == selectedCityId) &&
            (selectedCatId.isEmpty() || store.categoryId == selectedCatId)
        }.sortedWith(compareByDescending<StoreEntity> { it.isPinned }.thenBy { it.displayOrder })
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Real-time join request status banner for shop owners (Transitions immediately to full profile view after approval)
        val myStore = stores.find { !it.isDeleted && it.ownerId == currentPhone }
        myStore?.let { store ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (store.isApproved) Color(0xFF0F291E) else Color(0xFF2C2414)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (store.isApproved) Color(0xFF22C55E).copy(alpha = 0.5f) else Color(0xFFEAB308).copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (store.isApproved) {
                                if (isEn) "✅ Your business profile was successfully activated!" else "✅ تم توثيق ونشر منشأتك التجاريّة بنجاح!"
                            } else {
                                if (isEn) "⏳ Your shop registration request is under review" else "⏳ طلب تسجيل منشأتك قيد المراجعة والموافقة الإداريّة"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (store.isApproved) Color(0xFF4ADE80) else Color(0xFFFACC15)
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (store.isApproved) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFFEAB308).copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (store.isApproved) {
                                    if (isEn) "Active" else "نشط وموثق"
                                } else {
                                    if (isEn) "Pending" else "بانتظار موافقة الأدمن ⏳"
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (store.isApproved) Color(0xFF4ADE80) else Color(0xFFFACC15)
                            )
                        }
                    }
                    Text(
                        text = if (store.isApproved) {
                            if (isEn) "Welcome! Your shop (${store.name}) has been accepted and published. You can now receive orders and customer reviews." else "أهلاً بك! تم قبول توثيق منشأتك (${store.name}) ونشرها رسمياً بالدليل. يمكنك الآن استقبال طلبات الشراء والتقييمات من العملاء."
                        } else {
                            if (isEn) "Your shop registration request (${store.name}) was successfully submitted. It is currently under administrative verification." else "تم تقديم طلب تسجيل منشأتك (${store.name}) بنجاح! ملفك ومستنداتك قيد التدقيق والفحص الآن وسنرسل لك إشعاراً فور اعتماد التوثيق والنشر."
                        },
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Button(
                        onClick = { onStoreClick(store) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (store.isApproved) Color(0xFF22C55E) else Color(0xFFEAB308)
                        ),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (store.isApproved) {
                                if (isEn) "📂 View & Manage Completed Profile" else "📂 عرض وإدارة ملفك الشخصي المكتمل"
                            } else {
                                if (isEn) "👁️ Preview Submitted Details" else "👁️ عرض ومعاينة تفاصيل ملفك المقدم"
                            },
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Search & Filters Header
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isEn) "Search for store name or service..." else "ابحث عن اسم المتجر أو الخدمة المعروضة...", fontSize = 12.sp, color = themeColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("store_search_input"),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = themeColors.textSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // City selector
                    Box(modifier = Modifier.weight(1f)) {
                        var expanded by remember { mutableStateOf(false) }
                        val cityLabel = if (selectedCityId.isEmpty()) {
                            if (isEn) "All Cities" else "كل المحافظات"
                        } else {
                            val city = cities.find { it.id == selectedCityId }
                            if (isEn) (city?.nameEn ?: city?.nameAr ?: "") else (city?.nameAr ?: "")
                        }
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cityLabel, fontSize = 11.sp, color = Color.White)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(if (isEn) "All Cities" else "كل المحافظات") },
                                onClick = { selectedCityId = ""; expanded = false }
                            )
                            cities.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(if (isEn) city.nameEn else city.nameAr) },
                                    onClick = { selectedCityId = city.id; expanded = false }
                                )
                            }
                        }
                    }

                    // Category selector
                    Box(modifier = Modifier.weight(1f)) {
                        var expanded by remember { mutableStateOf(false) }
                        val catLabel = sectionCategories.find { it.id == selectedCatId }?.let { cat ->
                            if (isEn) {
                                when (cat.name) {
                                    "المحلات والمراكز" -> "Malls & Stores"
                                    "المطاعم والكافيهات" -> "Restaurants & Cafes"
                                    "عقارات وأراضي" -> "Real Estate"
                                    else -> cat.name
                                }
                            } else cat.name
                        } ?: (if (isEn) "All Categories" else "كل الفئات")
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(catLabel, fontSize = 11.sp, color = Color.White)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(if (isEn) "All Categories" else "كل الفئات") },
                                onClick = { selectedCatId = ""; expanded = false }
                            )
                            sectionCategories.forEach { cat ->
                                val catName = if (isEn) {
                                    when (cat.name) {
                                        "المحلات والمراكز" -> "Malls & Stores"
                                        "المطاعم والكافيهات" -> "Restaurants & Cafes"
                                        "عقارات وأراضي" -> "Real Estate"
                                        else -> cat.name
                                    }
                                } else cat.name
                                DropdownMenuItem(
                                    text = { Text("${cat.icon} $catName") },
                                    onClick = { selectedCatId = cat.id; expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        }

        val sectionTitleAr = when (sectionId.lowercase()) {
            "restaurants", "food" -> "🍔 المطاعم والكافيهات اليمنية"
            "medical", "centers" -> "🏥 المراكز والعيادات الطبية والصيدليات"
            "realestate", "properties" -> "🏢 العقارات والمقاولات"
            "jobs" -> "💼 إعلانات الوظائف وفرص العمل"
            else -> "🏪 المتاجر والمحلات المسجلة"
        }
        val sectionTitleEn = when (sectionId.lowercase()) {
            "restaurants", "food" -> "🍔 Yemeni Restaurants & Cafes"
            "medical", "centers" -> "🏥 Medical Centers & Clinics"
            "realestate", "properties" -> "🏢 Real Estate & Properties"
            "jobs" -> "💼 Job Opportunities"
            else -> "🏪 Registered Yemeni Stores"
        }
        val addBtnTextAr = when (sectionId.lowercase()) {
            "restaurants", "food" -> "إضافة مطعم"
            "medical", "centers" -> "إضافة مركز"
            "realestate", "properties" -> "إضافة عقار"
            "jobs" -> "إضافة وظيفة"
            else -> "إضافة متجر"
        }
        val addBtnTextEn = when (sectionId.lowercase()) {
            "restaurants", "food" -> "Add Restaurant"
            "medical", "centers" -> "Add Medical"
            "realestate", "properties" -> "Add Property"
            "jobs" -> "Add Job"
            else -> "Add Store"
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEn) "$sectionTitleEn (${activeStores.size}):" else "$sectionTitleAr (${activeStores.size}):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { viewModel.triggerRestoreAccountDialog.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Text(if (isEn) "Restore 🔓" else "استرجاع 🔓", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAddStoreClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(if (isEn) addBtnTextEn else addBtnTextAr, color = Color.Black, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (activeStores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isEn) "No listings match the current search filters." else "لم يتم العثور على أي نتائج تطابق فلاتر البحث الحالية.", color = themeColors.textSecondary, fontSize = 12.sp)
            }
        } else {
            activeStores.take(itemsToShowLimit).forEach { store ->
                StoreListItemCard(store = store, themeColors = themeColors, onClick = { onStoreClick(store) }, viewModel = viewModel)
            }
            if (activeStores.size > itemsToShowLimit) {
                Button(
                    onClick = { itemsToShowLimit += 10 },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(if (isEn) "Load More ⏬" else "عرض المزيد ⏬", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StoreListItemCard(
    store: StoreEntity,
    themeColors: VisualThemePalette,
    onClick: () -> Unit,
    viewModel: MainViewModel,
    onChatClick: (() -> Unit)? = null
) {
    val settingsState = viewModel.settings.collectAsState().value ?: AdminSettingsEntity()
    val adminRole by viewModel.adminRole.collectAsState()
    val isAdmin = adminRole != "GUEST"
    val context = LocalContext.current

    var showAdminQuickEditDialog by remember { mutableStateOf(false) }
    var editStoreName by remember(store) { mutableStateOf(store.name) }
    var editStoreLogo by remember(store) { mutableStateOf(store.logoImage) }
    var editStoreCover by remember(store) { mutableStateOf(store.coverImage) }
    var editStorePhone by remember(store) { mutableStateOf(store.phone) }
    var editStoreHours by remember(store) { mutableStateOf(store.workingHours) }

    val logoBitmap = remember(store.logoImage) {
        if (store.logoImage.isNotEmpty() && !store.logoImage.startsWith("http") && !store.logoImage.startsWith("content")) {
            try {
                val cleanBase64 = if (store.logoImage.contains(",")) store.logoImage.substringAfter(",") else store.logoImage
                val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) { null }
        } else null
    }

    val coverBitmap = remember(store.coverImage) {
        if (store.coverImage.isNotEmpty() && !store.coverImage.startsWith("http") && !store.coverImage.startsWith("content")) {
            try {
                val cleanBase64 = if (store.coverImage.contains(",")) store.coverImage.substringAfter(",") else store.coverImage
                val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) { null }
        } else null
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = (settingsState.cardMarginHorizontal * 0.6f).dp, vertical = (settingsState.cardMarginVertical * 0.6f).dp)
            .clickable { onClick() }
            .border(
                1.dp,
                if (store.isPinned) themeColors.accent else themeColors.accent.copy(alpha = 0.12f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column {
            // Header Cover Image Banner (16:9 ratio, comfortable height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                themeColors.primary,
                                themeColors.secondary
                            )
                        )
                    )
            ) {
                if (coverBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = coverBitmap.asImageBitmap(),
                        contentDescription = "غلاف المركز",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (store.coverImage.isNotEmpty()) {
                    AsyncImage(
                        model = store.coverImage,
                        contentDescription = "غلاف المركز",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(themeColors.primary.copy(alpha = 0.6f), Color(0xFF1E293B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = store.name,
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                // Right Status Badge ("🟢 مفتوح" / "🔴 مغلق")
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            if (store.isActive) Color(0xFF065F46).copy(alpha = 0.92f) else Color(0xFF991B1B).copy(alpha = 0.92f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, if (store.isActive) Color(0xFF34D399) else Color(0xFFF87171), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (store.isActive) "🟢 مفتوح" else "🔴 مغلق",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Left Rating Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color(0xFF0F172A).copy(alpha = 0.88f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("⭐", fontSize = 10.sp)
                        Text(String.format(java.util.Locale.US, "%.1f", store.rating), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                    }
                }

                if (store.isPinned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 8.dp, bottom = 8.dp)
                            .background(themeColors.accent, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text("📌 مثبت", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                if (isAdmin) {
                    IconButton(
                        onClick = { showAdminQuickEditDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Text("✏️", fontSize = 12.sp)
                    }
                }
            }

            // Core Profile Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo/Avatar Container
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(themeColors.background)
                        .border(1.dp, themeColors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (logoBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = logoBitmap.asImageBitmap(),
                            contentDescription = "شعار المركز",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (store.logoImage.isNotEmpty()) {
                        AsyncImage(
                            model = store.logoImage,
                            contentDescription = "شعار المركز",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val categoryIcon = when {
                            store.categoryId.contains("medical") || store.name.contains("طبي") || store.name.contains("عيادة") -> "🏥"
                            store.categoryId.contains("restaurant") || store.name.contains("مطعم") || store.name.contains("كافيه") -> "🍔"
                            store.categoryId.contains("realestate") || store.name.contains("عقار") || store.name.contains("مقاولات") -> "🏢"
                            else -> "🏪"
                        }
                        Text(categoryIcon, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(store.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        if (store.isVip) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFD97706), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("🏆 VIP", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        if (store.isVerified) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF3B82F6), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("✅ موثق", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (store.description.isNotEmpty()) {
                        Text(store.description, fontSize = 10.sp, color = themeColors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🕒 ${store.workingHours.ifEmpty { "09:00 ص - 10:00 م" }}",
                            fontSize = 9.sp,
                            color = themeColors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (store.localNeighborhood.isNotEmpty()) {
                            Text(
                                text = "📍 ${store.localNeighborhood}",
                                fontSize = 9.sp,
                                color = themeColors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(String.format("%.1f", store.rating), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    }
                    Text("(${store.numReviews} تقييم)", fontSize = 8.sp, color = themeColors.textSecondary)
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 10.dp))

            // Communication & Action Buttons Bar (1-click Call, WhatsApp, Chat, Details)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Direct Dial
                if (settingsState.showCallButton) {
                    Button(
                        onClick = {
                            val u = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                            context.startActivity(u)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📞 اتصال", fontSize = 9.5.sp, lineHeight = 14.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2, softWrap = true)
                    }
                }

                // WhatsApp Direct 1-Click
                if (store.phone.isNotBlank()) {
                    Button(
                        onClick = {
                            try {
                                val cleanPhone = store.phone.trim().replace(" ", "").replace("+", "")
                                val yemenPhone = if (cleanPhone.startsWith("967")) cleanPhone else "967$cleanPhone"
                                val waUri = Uri.parse("https://api.whatsapp.com/send?phone=$yemenPhone&text=${Uri.encode("مرحباً متجر ${store.name}، استفسار عبر تطبيق خدمات اليمن")}")
                                val waIntent = Intent(Intent.ACTION_VIEW, waUri)
                                context.startActivity(waIntent)
                            } catch (e: Exception) {
                                val u = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                                context.startActivity(u)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("💬 واتساب", fontSize = 9.sp, lineHeight = 14.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2, softWrap = true)
                    }
                }

                // Instant Chat
                if (settingsState.showInstantChatButton) {
                    Button(
                        onClick = {
                            if (onChatClick != null) {
                                onChatClick()
                            } else if (viewModel != null) {
                                viewModel.openOrCreateChatChannel(
                                    targetId = store.id,
                                    targetType = "STORE",
                                    targetName = store.name,
                                    targetPhone = store.phone,
                                    onCreated = {
                                        viewModel.triggerNotification("💬 تم فتح المحادثة الفورية مع متجر ${store.name}")
                                    }
                                )
                            } else {
                                onClick()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
                    ) {
                        Text("📩 محادثة", fontSize = 9.5.sp, lineHeight = 14.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2, softWrap = true)
                    }
                }

                // Details
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🔍 تفاصيل", fontSize = 9.5.sp, lineHeight = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 2, softWrap = true)
                }
            }
        }
    }

    // Admin Quick Edit Modal for Store Card
    if (showAdminQuickEditDialog) {
        Dialog(onDismissRequest = { showAdminQuickEditDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, themeColors.accent),
                modifier = Modifier.padding(12.dp).fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⚙️ تعديل وتنسيق بطاقة المركز/المتجر", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                    OutlinedTextField(
                        value = editStoreName,
                        onValueChange = { editStoreName = it },
                        label = { Text("اسم المتجر / المركز", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editStoreLogo,
                        onValueChange = { editStoreLogo = it },
                        label = { Text("رابط الشعار / الصورة الشخصية (URL / Base64)", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editStoreCover,
                        onValueChange = { editStoreCover = it },
                        label = { Text("رابط صورة الغلاف العلوي (URL / Base64)", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editStorePhone,
                        onValueChange = { editStorePhone = it },
                        label = { Text("رقم الهاتف / الجوال", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editStoreHours,
                        onValueChange = { editStoreHours = it },
                        label = { Text("ساعات العمل والدوام", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showAdminQuickEditDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (viewModel != null) {
                                    viewModel.saveStore(
                                        store.copy(
                                            name = editStoreName,
                                            logoImage = editStoreLogo,
                                            coverImage = editStoreCover,
                                            phone = editStorePhone,
                                            workingHours = editStoreHours
                                        )
                                    )
                                }
                                showAdminQuickEditDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                        ) {
                            Text("حفظ التعديلات", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

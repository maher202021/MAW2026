package com.serviseyem

import com.serviseyem.utils.*
import com.serviseyem.ui.*

import com.serviseyem.ui.ProductAttachmentsSection
import com.serviseyem.ui.SpecialOffersSection
import com.serviseyem.ui.ProductListItemCard
import com.serviseyem.ui.StoreProductOrderDialog
import com.serviseyem.ui.ProductGrid
import com.serviseyem.ui.ProductDetailsDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Brush
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import com.serviseyem.data.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.components.ReviewInput

/**
 * Centralized Yemeni phone validator.
 * Used by: StoreCreateEditDialog, PropertyCreateEditDialog, CreateBookingScreen (via BookingValidation).
 * Kept private to this file to avoid cross-module coupling.
 */
private fun isValidYemeniPhone(phone: String): Boolean {
    val clean = phone.trim().replace(" ", "").replace("+", "")
    return clean.length == 9 &&
        (clean.startsWith("77") || clean.startsWith("73") ||
         clean.startsWith("71") || clean.startsWith("70") ||
         clean.startsWith("78"))
}

@Composable
fun rememberBase64Bitmap(base64Str: String): ImageBitmap? {
    return remember(base64Str) {
        if (base64Str.isNotEmpty() && !base64Str.startsWith("http") && !base64Str.startsWith("content")) {
            try {
                val cleanBase64 = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
                val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                bitmap?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}

// --------------------------------------------------------
// 1. SMART RECOMMENDATIONS SECTION
// --------------------------------------------------------
@Composable
fun SmartRecommendationsSection(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onStoreClick: (StoreEntity) -> Unit,
    onPropertyClick: (PropertyEntity) -> Unit
) {
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val settingsState by viewModel.settings.collectAsState()

    // Filter active items
    val activeStores = remember(stores) { stores.filter { it.isActive && !it.isDeleted } }
    val activeProps = remember(properties) { properties.filter { it.isActive && !it.isDeleted } }

    // Logic: Recommended, Verified, VIP or Pinned by Admin
    val recommendedStores = remember(activeStores, settingsState.isStoresEnabled) {
        if (!settingsState.isStoresEnabled) emptyList()
        else {
            val flagged = activeStores.filter { it.isRecommended || it.isPinned || it.isVerified || it.isVip }
            val list = if (flagged.isNotEmpty()) flagged else activeStores
            list.sortedWith(compareByDescending<StoreEntity> { it.rating }
                .thenByDescending { it.createdAt })
            .take(10)
        }
    }

    val recommendedProps = remember(activeProps, settingsState.isPropertiesEnabled) {
        if (!settingsState.isPropertiesEnabled) emptyList()
        else {
            val flagged = activeProps.filter { it.isRecommended || it.isPinned || it.isVerified || it.isVip }
            val list = if (flagged.isNotEmpty()) flagged else activeProps
            list.sortedWith(compareByDescending<PropertyEntity> { it.rating }
                .thenByDescending { it.createdAt })
            .take(10)
        }
    }

    if (recommendedStores.isNotEmpty() || recommendedProps.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeColors.surface, RoundedCornerShape(16.dp))
                .padding(14.dp)
                .border(1.dp, themeColors.accent.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💡", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "توصيات ذكية منتقاة لك",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    Text(
                        text = "محلات وعقارات متميزة بناءً على التقييمات وتفضيلات المستخدمين",
                        fontSize = 10.sp,
                        color = themeColors.textSecondary
                    )
                }
            }

            if (settingsState.recommendationsLayout == "GRID_HORIZONTAL") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recommendedStores) { store ->
                        RecommendationItemCard(
                            title = store.name,
                            subtitle = store.description,
                            coverImage = store.coverImage.ifEmpty { store.logoImage },
                            avatarImage = store.logoImage,
                            rating = store.rating,
                            badge = "متجر مميز",
                            badgeColor = Color(0xFFF59E0B),
                            themeColors = themeColors,
                            onClick = { onStoreClick(store) }
                        )
                    }
                    items(recommendedProps) { prop ->
                        val label = if (prop.type == "rent") "للإيجار" else "للبيع"
                        RecommendationItemCard(
                            title = prop.title,
                            subtitle = "${prop.price} YER - $label",
                            coverImage = prop.images.firstOrNull() ?: "",
                            avatarImage = "",
                            rating = prop.rating,
                            badge = "عقار متميز",
                            badgeColor = Color(0xFF10B981),
                            themeColors = themeColors,
                            onClick = { onPropertyClick(prop) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationItemCard(
    title: String,
    subtitle: String,
    coverImage: String,
    avatarImage: String,
    rating: Float,
    badge: String,
    badgeColor: Color = Color(0xFFF59E0B),
    themeColors: VisualThemePalette,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.8.dp, themeColors.accent.copy(alpha = 0.25f)),
        modifier = Modifier
            .width(170.dp)
            .height(200.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Dual-image layout: Cover Banner + Avatar Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            ) {
                // 1. Cover image / Banner background
                if (coverImage.isNotBlank()) {
                    AsyncImage(
                        model = coverImage,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(themeColors.primary.copy(alpha = 0.6f), Color(0xFF1E293B))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = themeColors.accent.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Dark gradient overlay on top for badge readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                )

                // Top Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                // Rating Badge Top-End
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 8.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(String.format("%.1f", rating), fontSize = 9.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                    }
                }

                // Circular Logo/Avatar Overlay at Bottom-Start
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 8.dp, bottom = 4.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(themeColors.surface)
                        .border(1.5.dp, themeColors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarImage.isNotBlank()) {
                        AsyncImage(
                            model = avatarImage,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = title.take(1).ifEmpty { "🏪" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent
                        )
                    }
                }
            }

            // Card Body Content (Title, Subtitle & Action)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle.ifEmpty { "خدمات متكاملة وعروض متميزة" },
                        fontSize = 9.5.sp,
                        color = themeColors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 13.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = themeColors.accent.copy(alpha = 0.15f),
                    border = BorderStroke(0.7.dp, themeColors.accent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "عرض التفاصيل 🔍",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }
    }
}

// --------------------------------------------------------
// 2. STORES TAB CONTENT
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
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        contentPadding = PaddingValues(0.dp)
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
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(if (isEn) "Restore 🔓" else "استرجاع 🔓", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAddStoreClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(if (isEn) addBtnTextEn else addBtnTextAr, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                            .size(28.dp)
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
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📞 اتصال", fontSize = 9.5.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
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
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("💬 واتساب", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
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
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f))
                    ) {
                        Text("📩 محادثة", fontSize = 9.5.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }

                // Details
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🔍 تفاصيل", fontSize = 9.5.sp, color = Color.Black, fontWeight = FontWeight.Bold, maxLines = 1)
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

// --------------------------------------------------------
// 3. PROPERTIES TAB CONTENT
// --------------------------------------------------------
@Composable
fun PropertiesTabContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onPropertyClick: (PropertyEntity) -> Unit,
    onAddPropertyClick: () -> Unit,
    sectionId: String = "properties"
) {
    val properties by viewModel.properties.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val currentPhone by viewModel.currentUserPhone.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isEn = currentLang == "en"

    var searchQuery by remember { mutableStateOf("") }
    var selectedCityId by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("") } // rent, sale, empty
    var filterPropType by remember { mutableStateOf("") } // apartment, house, land, shop, empty
    var itemsToShowLimit by remember { mutableStateOf(10) }

    val activeProps = remember(properties, searchQuery, selectedCityId, filterType, filterPropType, sectionId) {
        properties.filter {
            !it.isDeleted &&
            (it.sectionId == sectionId || (sectionId == "properties" && (it.sectionId == "properties" || it.sectionId == "realestate" || it.sectionId.isEmpty()))) &&
            (it.isActive || adminRole != "GUEST" || it.ownerId == currentPhone) &&
            (searchQuery.isEmpty() || it.title.contains(searchQuery, true) || it.description.contains(searchQuery, true)) &&
            (selectedCityId.isEmpty() || it.cityId == selectedCityId) &&
            (filterType.isEmpty() || it.type == filterType) &&
            (filterPropType.isEmpty() || it.propertyType == filterPropType)
        }.sortedByDescending { it.isPinned }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Real-time join request status banner for property advertisers (Transitions immediately to full profile view after approval)
        val myProp = properties.find { !it.isDeleted && it.ownerId == currentPhone }
        myProp?.let { prop ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (prop.isActive) Color(0xFF0F291E) else Color(0xFF2C2414)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (prop.isActive) Color(0xFF22C55E).copy(alpha = 0.5f) else Color(0xFFEAB308).copy(alpha = 0.5f)
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
                            text = if (prop.isActive) {
                                if (isEn) "✅ Property advertisement successfully activated!" else "✅ تم تفعيل إعلان العقار بنجاح!"
                            } else {
                                if (isEn) "⏳ Property listing request under review" else "⏳ طلب إدراج العقار قيد المراجعة"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (prop.isActive) Color(0xFF4ADE80) else Color(0xFFFACC15)
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (prop.isActive) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFFEAB308).copy(alpha = 0.2f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (prop.isActive) {
                                    if (isEn) "Published" else "منشور وعامل"
                                } else {
                                    if (isEn) "Pending" else "قيد التدقيق الإداري"
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (prop.isActive) Color(0xFF4ADE80) else Color(0xFFFACC15)
                            )
                        }
                    }
                    Text(
                        text = if (prop.isActive) {
                            if (isEn) "Welcome! Your property advertisement (${prop.title}) was approved and published. You can now receive calls and inquiries from buyers." else "أهلاً بك يا غالي! تم الموافقة على إعلان عقارك (${prop.title}) ونشره رسمياً للجمهور. يمكنك الآن استقبال الاتصالات والاستفسارات من المشترين المهتمين."
                        } else {
                            if (isEn) "Your property listing request (${prop.title}) was submitted. Data and documents are under administrative review, and you will be notified once published." else "تم تقديم طلب إدراج عقارك (${prop.title}) بنجاح. بياناتك والمستندات المرفقة قيد التدقيق والفحص الإداري حالياً من قبل الإدارة لضمان المصداقية وسننبهك فور نشره."
                        },
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Button(
                        onClick = { onPropertyClick(prop) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (prop.isActive) Color(0xFF22C55E) else Color(0xFFEAB308)
                        ),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (prop.isActive) {
                                if (isEn) "📂 View & Manage Published Property" else "📂 عرض وإدارة ملف العقار المنشور"
                            } else {
                                if (isEn) "👁️ Preview Listing Details" else "👁️ عرض ومعاينة تفاصيل طلبك وعقد الملكية"
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
                    placeholder = { Text(if (isEn) "Search for apartment, building, land..." else "ابحث عن شقة، عمارة، أرض، دكان...", fontSize = 12.sp, color = themeColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("property_search_input"),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = themeColors.textSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type selector (Rent/Sale)
                    val types = listOf(
                        Pair("", if (isEn) "All" else "الكل"),
                        Pair("rent", if (isEn) "For Rent" else "للإيجار"),
                        Pair("sale", if (isEn) "For Sale" else "للبيع")
                    )
                    types.forEach { t ->
                        val isSel = filterType == t.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSel) themeColors.accent else Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { filterType = t.first }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(t.second, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

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
                            Text(cityLabel, fontSize = 10.sp, color = Color.White)
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

                    // Property Type Selector
                    Box(modifier = Modifier.weight(1f)) {
                        var expanded by remember { mutableStateOf(false) }
                        val pTypes = mapOf(
                            "" to (if (isEn) "All Properties" else "جميع أنواع العقارات"),
                            "apartment" to (if (isEn) "Apartment" else "شقة سكينة"),
                            "house" to (if (isEn) "House / Villa" else "بيت مستقل/فيلا"),
                            "land" to (if (isEn) "Land" else "أرض عقارية"),
                            "shop" to (if (isEn) "Shop / Store" else "محل تجاري/دكان")
                        )
                        val buttonLabel = pTypes[filterPropType] ?: (if (isEn) "Property Type" else "نوع العقار")
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(buttonLabel, fontSize = 10.sp, color = Color.White)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            pTypes.forEach { (key, value) ->
                                DropdownMenuItem(
                                    text = { Text(value) },
                                    onClick = { filterPropType = key; expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEn) "🏠 Available Properties (${activeProps.size}):" else "🏠 العقارات والمنشآت المتاحة (${activeProps.size}):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { viewModel.triggerRestoreAccountDialog.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(if (isEn) "Restore 🔓" else "استرجاع 🔓", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAddPropertyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(if (isEn) "Add Property" else "أضف عقار", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (activeProps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isEn) "No properties match the current search filters." else "لم يتم العثور على أي عقار يطابق فلاتر البحث الحالية.", color = themeColors.textSecondary, fontSize = 12.sp)
            }
        } else {
            activeProps.take(itemsToShowLimit).forEach { prop ->
                PropertyListItemCard(prop = prop, themeColors = themeColors, onClick = { onPropertyClick(prop) }, viewModel = viewModel)
            }
            if (activeProps.size > itemsToShowLimit) {
                Button(
                    onClick = { itemsToShowLimit += 10 },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(if (isEn) "Load More Properties ⏬" else "عرض المزيد من العقارات والمنشآت ⏬", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PropertyListItemCard(
    prop: PropertyEntity,
    themeColors: VisualThemePalette,
    onClick: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val settingsState by viewModel.settings.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                1.dp,
                if (prop.isPinned) themeColors.accent else themeColors.accent.copy(alpha = 0.08f),
                RoundedCornerShape(8.dp)
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .background(Color.DarkGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(themeColors.primary.copy(alpha = 0.1f))
                ) {
                    Text("📸 صور العقار", modifier = Modifier.align(Alignment.Center), color = Color.Gray, fontSize = 12.sp)
                }

                // Type Badge (Rent/Sale)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(if (prop.type == "rent") Color(0xFF0284C7) else Color(0xFF16A34A), RoundedCornerShape(bottomStart = 8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (prop.type == "rent") "للإيجار" else "للبيع",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (prop.isPinned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(themeColors.accent, RoundedCornerShape(bottomEnd = 8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("📌 متميز", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                if (!prop.isActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(Color.Red)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("قيد التحقق", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Column(modifier = Modifier.padding(7.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(prop.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    if (prop.isVip) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD97706), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("🏆 VIP", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    if (prop.isVerified) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF3B82F6), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("✅ موثق", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    if (prop.isRecommended) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEC4899), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("💖 موصى به", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(prop.description, fontSize = 10.sp, color = themeColors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("سعر الطلب:", fontSize = 8.sp, color = themeColors.textSecondary)
                        Text("🔽 ${prop.price} ${prop.currency}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "📍 الحي: ${prop.localNeighborhood}",
                                fontSize = 9.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (settingsState.showCallButton) {
                        Button(
                            onClick = {
                                val u = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${prop.phone}"))
                                context.startActivity(u)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("📞 اتصال مباشر", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (settingsState.showVoiceCallButton && !settingsState.disableVoiceCalls) {
                        Button(
                            onClick = {
                                if (viewModel != null) {
                                    viewModel.startVoiceCall(prop.title, "صاحب العقار HD")
                                } else {
                                    val u = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:${prop.phone}"))
                                    context.startActivity(u)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("🎙️ مكالمة صوتية", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------
// 4. STORE DETAILS DIALOG
// --------------------------------------------------------
@Composable
fun StoreDetailsDialog(
    store: StoreEntity,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit,
    onOrderProductClick: (ProductEntity) -> Unit
) {
    val products by viewModel.products.collectAsState()
    val ratings by viewModel.ratings.collectAsState()
    val customReviews by remember(store.id) {
        DataManager.getRatingReviews(store.id)
    }.collectAsState(initial = emptyList())
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val adminRole by viewModel.adminRole.collectAsState()
    val context = LocalContext.current

    val isOwnerOrAdmin = remember(adminRole, currentUserId, store) {
        adminRole != "GUEST" || store.ownerId == currentUserId || store.phone == currentUserId
    }

    val storeProducts = remember(products, store.id) {
        products.filter { it.storeId == store.id && !it.isDeleted && it.isAvailable }
    }
    val storeReviews = remember(ratings, store.id) {
        ratings.filter { it.targetId == store.id && it.targetType == "STORE" && it.isApproved }
    }

    // Dynamic Categories Tabs
    val productCategories = remember(storeProducts) {
        val cats = storeProducts.map { it.category }.filter { it.isNotBlank() }.distinct().toMutableList()
        val defaultTabs = mutableListOf("الكل", "خدمات", "عروض")
        cats.forEach { c -> if (!defaultTabs.contains(c)) defaultTabs.add(c) }
        defaultTabs
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showEditStoreDialog by remember { mutableStateOf(false) }
    var showBulkPriceDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    var userRatingInput by remember { mutableStateOf(5f) }
    var userCommentInput by remember { mutableStateOf("") }

    // Cover & Logo Image Pickers for Owner/Admin
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val base64 = com.serviseyem.utils.compressAndResizeImageUri(context, it, 800, 70)
                if (base64.isNotEmpty()) {
                    viewModel.saveStore(store.copy(coverImage = base64))
                    android.widget.Toast.makeText(context, "📸 تم تحديث صورة الغلاف بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "فشل تحميل الصورة", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val base64 = com.serviseyem.utils.compressAndResizeImageUri(context, it, 800, 70)
                if (base64.isNotEmpty()) {
                    viewModel.saveStore(store.copy(logoImage = base64))
                    android.widget.Toast.makeText(context, "🖼️ تم تحديث صورة الشعار بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "فشل تحميل الصورة", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = themeColors.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // 1. Cover Image Banner
                val hasCover = store.coverImage.isNotEmpty()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (hasCover) 140.dp else 90.dp)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))))
                ) {
                    val coverBitmap = rememberBase64Bitmap(store.coverImage)
                    if (coverBitmap != null) {
                        Image(
                            bitmap = coverBitmap,
                            contentDescription = "Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (store.coverImage.startsWith("http")) {
                        AsyncImage(
                            model = store.coverImage,
                            contentDescription = "Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text("🏙️ ${store.name}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Top Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isOwnerOrAdmin) {
                            Button(
                                onClick = { coverPickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.65f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("📸 تغيير الغلاف", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                .size(34.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // 2. Logo Avatar & Business Details (Seamless clean layout without empty spaces)
                Column(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Logo Avatar Box
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A))
                                .border(3.dp, themeColors.accent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val logoBitmap = rememberBase64Bitmap(store.logoImage)
                            if (logoBitmap != null) {
                                Image(
                                    bitmap = logoBitmap,
                                    contentDescription = "Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (store.logoImage.startsWith("http")) {
                                AsyncImage(
                                    model = store.logoImage,
                                    contentDescription = "Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = if (store.categoryId.contains("rest") || store.name.contains("مطعم") || store.name.contains("كافيه")) "🍔" else "🏪",
                                    fontSize = 32.sp
                                )
                            }

                            if (isOwnerOrAdmin) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(themeColors.accent, CircleShape)
                                        .clickable { logoPickerLauncher.launch("image/*") }
                                        .padding(4.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Logo", tint = Color.Black, modifier = Modifier.size(12.dp))
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = store.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (store.isVerified) {
                                    Text("✔️ معتمد", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "👤 المالك: ${store.ownerName.ifEmpty { "إدارة المركز" }}",
                                fontSize = 11.sp,
                                color = themeColors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Basic Badges (Rating, Neighborhood, Hours)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = themeColors.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐ ${store.rating} (${store.numReviews} تقييم)", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Surface(
                            color = themeColors.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("📍 ${store.localNeighborhood.ifEmpty { "اليمن" }}", fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Surface(
                            color = themeColors.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🕒 ${store.workingHours.ifEmpty { "24 ساعة" }}", fontSize = 10.sp, color = Color.LightGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. World-Class Direct Contact Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                val u = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.phone}"))
                                context.startActivity(u)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("اتصال", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val cleanPhone = store.phone.replace("+", "").replace(" ", "")
                                val u = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone"))
                                context.startActivity(u)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Text("💬 واتساب", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "🏪 تفضل بزيارة صفحة ${store.name} على دليل خدمات اليمن!\n📞 للتواصل: ${store.phone}\n📍 ${store.localNeighborhood}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة المتجر"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("مشاركة", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Owner & Admin Management Panel (If Authorized)
                    if (isOwnerOrAdmin) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, themeColors.accent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🛠️ لوحة إدارة المتجر والأسعار (المالك / الأدمن):", color = themeColors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { showEditStoreDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                    ) {
                                        Text("📝 البيانات", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showAddProductDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                    ) {
                                        Text("➕ إضافة منتج", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showBulkPriceDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                    ) {
                                        Text("📈 رفع الأسعار", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (adminRole != "GUEST") {
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteStore(store.id)
                                                onDismiss()
                                                android.widget.Toast.makeText(context, "🗑️ تم حذف وحظر المتجر!", android.widget.Toast.LENGTH_LONG).show()
                                            },
                                            modifier = Modifier
                                                .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                                .size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showEditStoreDialog) {
                        StoreCreateEditDialog(
                            store = store,
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onDismiss = { showEditStoreDialog = false }
                        )
                    }

                    if (showBulkPriceDialog) {
                        BulkPriceAdjusterDialog(
                            storeId = store.id,
                            products = storeProducts,
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onDismiss = { showBulkPriceDialog = false }
                        )
                    }

                    if (showAddProductDialog) {
                        QuickAddProductDialog(
                            storeId = store.id,
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onDismiss = { showAddProductDialog = false }
                        )
                    }

                    HorizontalDivider(color = themeColors.accent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                    Text("📝 نبذة و وصف المتجر:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    Text(
                        text = store.description.ifEmpty { "متجر معتمد يمتلك أفضل المنتجات والسلع بأسعار منافسة." },
                        fontSize = 11.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Attachments & Special Offers
                    val storeAtts = remember(store.productAttachmentsJson) {
                        com.serviseyem.data.ProductAttachment.parseList(store.productAttachmentsJson)
                    }
                    ProductAttachmentsSection(
                        attachments = storeAtts,
                        onAttachmentsChanged = {},
                        mode = "VISITOR_VIEW",
                        themeColors = themeColors
                    )

                    SpecialOffersSection(
                        offersJson = store.specialOffersJson,
                        onOffersChanged = { newOffersJson ->
                            viewModel.saveStore(store.copy(specialOffersJson = newOffersJson))
                            android.widget.Toast.makeText(context, "🔥 تم حفظ التعديلات على العروض والتخفيضات!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        isEditable = isOwnerOrAdmin,
                        themeColors = themeColors
                    )

                    // Store Gallery Photos
                    if (store.images.isNotEmpty()) {
                        Text("📸 معرض الصور والمنتجات (${store.images.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 6.dp)
                        ) {
                            items(store.images.size) { index ->
                                val bMap = rememberBase64Bitmap(store.images[index])
                                if (bMap != null) {
                                    Image(
                                        bitmap = bMap,
                                        contentDescription = null,
                                        modifier = Modifier.width(160.dp).fillMaxHeight().clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    // PDF Catalog
                    if (store.pdfFileBase64.isNotEmpty() && store.pdfStatus == "APPROVED") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            modifier = Modifier.fillMaxWidth().clickable {
                                android.widget.Toast.makeText(context, "📄 تم تنزيل قائمة الخدمات والأسعار بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                            }
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("📄", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("عرض قائمة أسعار السلع والخدمات (PDF)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                                    Text("مستند رسمي معتمد وموثق من الإدارة للجمهور", fontSize = 9.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = themeColors.accent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))

                    // 5. SMART TABROW & PRODUCTS SECTION
                    val isMedical = store.categoryId.contains("طبي", true) || store.categoryId.contains("عياد", true) || store.categoryId.contains("medical", true) || store.name.contains("مستشفى", true) || store.name.contains("مركز طبي", true) || store.name.contains("عيادة", true)
                    val isRestaurant = store.categoryId.contains("مطعم", true) || store.categoryId.contains("كافيه", true) || store.categoryId.contains("restaurant", true)

                    val sectionTitle = when {
                        isMedical -> "🏥 الخدمات الطبية والأقسام الصحية للحجز (${storeProducts.size}):"
                        isRestaurant -> "🍽️ الوجبات والأطباق المتاحة للطلب (${storeProducts.size}):"
                        else -> "🛍️ السلع والمنتجات والخدمات (${storeProducts.size}):"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sectionTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        
                        if (isOwnerOrAdmin) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    onClick = { showAddProductDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Text("➕ إضافة ${if (isMedical) "خدمة" else if (isRestaurant) "وجبة" else "سلعة/خدمة"}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                Surface(
                                    onClick = { showBulkPriceDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Text("📈 رفع/تعديل الأسعار", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }

                    if (productCategories.isNotEmpty()) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color.Transparent,
                            contentColor = themeColors.accent,
                            edgePadding = 0.dp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            productCategories.forEachIndexed { index, catName ->
                                val count = when (catName) {
                                    "الكل" -> storeProducts.size
                                    "خدمات" -> storeProducts.count { it.description.contains("خدمة") || it.name.contains("خدمة") }
                                    "عروض" -> storeProducts.count { it.isOffer || it.discountPercent > 0 || it.oldPrice > it.price }
                                    else -> storeProducts.count { it.category == catName }
                                }
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = {
                                        Text(
                                            text = "$catName ($count)",
                                            fontSize = 11.sp,
                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selectedTabIndex == index) themeColors.accent else Color.LightGray
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Filtered Products List
                    val filteredProducts = remember(selectedTabIndex, storeProducts, productCategories) {
                        if (selectedTabIndex >= productCategories.size) storeProducts
                        else {
                            val activeCat = productCategories[selectedTabIndex]
                            when (activeCat) {
                                "الكل" -> storeProducts
                                "خدمات" -> storeProducts.filter { it.description.contains("خدمة") || it.name.contains("خدمة") }
                                "عروض" -> storeProducts.filter { it.isOffer || it.discountPercent > 0 || it.oldPrice > it.price }
                                else -> storeProducts.filter { it.category == activeCat }
                            }
                        }
                    }

                    if (filteredProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (isMedical) "لا توجد خدمات طبية مسجلة حالياً." else "لا توجد منتجات متوفرة في هذا التبويب حالياً.", color = themeColors.textSecondary, fontSize = 11.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredProducts.forEach { product ->
                                ProductListItemCard(
                                    product = product,
                                    isOwnerOrAdmin = isOwnerOrAdmin,
                                    themeColors = themeColors,
                                    viewModel = viewModel,
                                    isMedical = isMedical,
                                    isRestaurant = isRestaurant,
                                    onOrderClick = { onOrderProductClick(product) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = themeColors.accent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

                    // 6. REVIEWS & RATINGS SECTION
                    val totalReviewsCount = storeReviews.size + customReviews.size
                    Text("⭐ التقييمات وآراء العملاء ($totalReviewsCount):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    
                    // Custom real-time reviews
                    customReviews.forEach { rev ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(rev.userName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Row {
                                        repeat(rev.rating.toInt()) { Text("⭐", fontSize = 9.sp) }
                                    }
                                }
                                Text(rev.comment, fontSize = 10.sp, color = themeColors.textSecondary)
                            }
                        }
                    }

                    // Existing ratings
                    storeReviews.forEach { rev ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(rev.userName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Row {
                                        repeat(rev.rating.toInt()) { Text("⭐", fontSize = 9.sp) }
                                    }
                                }
                                Text(rev.comment, fontSize = 10.sp, color = themeColors.textSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 7. Interactive Review Input (Material 3 style)
                    ReviewInput(
                        onSubmit = { rating, comment ->
                            val newRating = RatingEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                targetId = store.id,
                                targetType = "STORE",
                                userId = currentUserId,
                                userName = currentUserName.ifEmpty { "عميل التطبيق" },
                                rating = rating.toFloat(),
                                comment = comment,
                                timestamp = System.currentTimeMillis()
                            )
                            DataManager.submitReview(
                                shopId = store.id,
                                ratingEntity = newRating,
                                onSuccess = {
                                    android.widget.Toast.makeText(context, "✅ تم إرسال تقييمك بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onFailure = {
                                    android.widget.Toast.makeText(context, "❌ فشل إرسال التقييم: ${it.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}



/**
 * 📈 Price Adjuster Modal for Store Owner / Admin (Supports All Items OR Single Selected Item)
 */
@Composable
fun BulkPriceAdjusterDialog(
    storeId: String,
    products: List<ProductEntity>,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    var targetScope by remember { mutableStateOf("SINGLE") } // "ALL" or "SINGLE"
    var selectedProductId by remember { mutableStateOf(products.firstOrNull()?.id ?: "") }
    var singleNewPriceInput by remember {
        mutableStateOf(products.find { it.id == (products.firstOrNull()?.id ?: "") }?.price?.toString() ?: "")
    }
    var percentInput by remember { mutableStateOf("10") }
    var actionType by remember { mutableStateOf("INCREASE") } // INCREASE or DECREASE
    var singleAdjustMode by remember { mutableStateOf("DIRECT") } // "DIRECT" (new price) or "PERCENT" (%)
    val context = LocalContext.current

    val currentSelectedProduct = remember(selectedProductId, products) {
        products.find { it.id == selectedProductId }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, themeColors.accent),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("📈 رفع وتعديل الأسعار:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                // Scope selector: Single item vs All items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = targetScope == "SINGLE",
                        onClick = {
                            targetScope = "SINGLE"
                            if (selectedProductId.isEmpty() && products.isNotEmpty()) {
                                selectedProductId = products.first().id
                                singleNewPriceInput = products.first().price.toString()
                            }
                        },
                        label = { Text("🎯 سلعة/خدمة واحدة فقط", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = targetScope == "ALL",
                        onClick = { targetScope = "ALL" },
                        label = { Text("🌐 جميع السلع (${products.size})", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (targetScope == "SINGLE") {
                    if (products.isEmpty()) {
                        Text("⚠️ لا توجد منتجات أو خدمات مضافة حالياً لتعديل سعرها.", fontSize = 12.sp, color = Color(0xFFEF4444))
                    } else {
                        // Product Picker
                        var expandedPicker by remember { mutableStateOf(false) }
                        Text("اختر السلعة أو الخدمة المراد تعديل سعرها:", fontSize = 11.sp, color = themeColors.textSecondary)
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { expandedPicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.5f)),
                                colors = CardDefaults.outlinedCardColors(containerColor = Color.Black.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentSelectedProduct?.name ?: "اختر منتج...",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "السعر الحالي: ${currentSelectedProduct?.price ?: 0} ${currentSelectedProduct?.currency ?: "ريال"}",
                                            fontSize = 10.sp,
                                            color = themeColors.accent
                                        )
                                    }
                                    Text("▼", color = Color.White, fontSize = 12.sp)
                                }
                            }

                            DropdownMenu(
                                expanded = expandedPicker,
                                onDismissRequest = { expandedPicker = false },
                                modifier = Modifier.background(Color(0xFF1E293B)).fillMaxWidth(0.85f)
                            ) {
                                products.forEach { p ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(p.name, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                                Text("${p.price} ${p.currency}", color = themeColors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = {
                                            selectedProductId = p.id
                                            singleNewPriceInput = p.price.toString()
                                            expandedPicker = false
                                        }
                                    )
                                }
                            }
                        }

                        // Single adjustment method: Direct Price or Percentage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = singleAdjustMode == "DIRECT",
                                onClick = { singleAdjustMode = "DIRECT" },
                                label = { Text("💰 إدخال السعر الجديد مباشرة", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = singleAdjustMode == "PERCENT",
                                onClick = { singleAdjustMode = "PERCENT" },
                                label = { Text("📊 نسبة مئوية (%)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (singleAdjustMode == "DIRECT") {
                            OutlinedTextField(
                                value = singleNewPriceInput,
                                onValueChange = { singleNewPriceInput = it },
                                label = { Text("السعر الجديد (${currentSelectedProduct?.currency ?: "ريال"})", color = themeColors.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = actionType == "INCREASE",
                                    onClick = { actionType = "INCREASE" },
                                    label = { Text("📈 رفع (+)") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = actionType == "DECREASE",
                                    onClick = { actionType = "DECREASE" },
                                    label = { Text("📉 تخفيض (-)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = percentInput,
                                onValueChange = { percentInput = it },
                                label = { Text("النسبة المئوية (%)", color = themeColors.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                } else {
                    // ALL ITEMS
                    Text("سيتم تطبيق نسبة التعديل على جميع السلع والخدمات (${products.size} عنصر):", fontSize = 11.sp, color = themeColors.textSecondary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = actionType == "INCREASE",
                            onClick = { actionType = "INCREASE" },
                            label = { Text("📈 رفع أسعار الجميع (+)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = actionType == "DECREASE",
                            onClick = { actionType = "DECREASE" },
                            label = { Text("📉 تخفيض أسعار الجميع (-)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = percentInput,
                        onValueChange = { percentInput = it },
                        label = { Text("النسبة المئوية لجميع المنتجات (%)", color = themeColors.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (targetScope == "SINGLE") {
                                val prod = currentSelectedProduct
                                if (prod != null) {
                                    val newPrice = if (singleAdjustMode == "DIRECT") {
                                        singleNewPriceInput.toDoubleOrNull() ?: prod.price
                                    } else {
                                        val pct = percentInput.toDoubleOrNull() ?: 0.0
                                        val factor = if (actionType == "INCREASE") (1.0 + pct / 100.0) else (1.0 - pct / 100.0)
                                        (prod.price * factor).let { Math.round(it * 10.0) / 10.0 }
                                    }
                                    viewModel.saveProduct(prod.copy(price = newPrice))
                                    android.widget.Toast.makeText(context, "✅ تم تحديث سعر '${prod.name}' إلى $newPrice ${prod.currency} بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }
                            } else {
                                val pct = percentInput.toDoubleOrNull() ?: 0.0
                                if (pct > 0) {
                                    val factor = if (actionType == "INCREASE") (1.0 + pct / 100.0) else (1.0 - pct / 100.0)
                                    products.forEach { p ->
                                        val newP = (p.price * factor).let { Math.round(it * 10.0) / 10.0 }
                                        viewModel.saveProduct(p.copy(price = newP))
                                    }
                                    android.widget.Toast.makeText(context, "✅ تم تحديث أسعار ${products.size} منتج وخدمة بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                    ) {
                        Text(if (targetScope == "SINGLE") "حفظ السعر المحدد" else "تطبيق على جميع المنتجات", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * ➕ Quick Add Product Modal for Store Owner / Admin
 */
@Composable
fun QuickAddProductDialog(
    storeId: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("خدمات") }
    var isOffer by remember { mutableStateOf(false) }
    var productImageBase64 by remember { mutableStateOf("") }

    val context = LocalContext.current

    val productImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val base64 = com.serviseyem.utils.compressAndResizeImageUri(context, uri, 800, 70)
                if (base64.isNotEmpty()) {
                    productImageBase64 = base64
                    android.widget.Toast.makeText(context, "📷 تم اختيار ضغط صورة المنتج بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, themeColors.accent),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("➕ إضافة منتج / سلعة جديدة للمتجر:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المنتج / السلعة", color = themeColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("وصف السلعة / التفاصيل", color = themeColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("السعر (YER)", color = themeColors.textSecondary) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("التبويب / القسم", color = themeColors.textSecondary) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { productImagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (productImageBase64.isNotEmpty()) Color(0xFF10B981) else themeColors.accent)
                    ) {
                        Text(if (productImageBase64.isNotEmpty()) "تم اختيار صورة 📷" else "رفع صورة المنتج 📷", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isOffer, onCheckedChange = { isOffer = it })
                        Text("تعيين كـ عرض خاص 🔥", fontSize = 11.sp, color = Color.White)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val p = ProductEntity(
                                    storeId = storeId,
                                    name = name,
                                    description = desc,
                                    price = priceStr.toDoubleOrNull() ?: 0.0,
                                    category = category.ifBlank { "خدمات" },
                                    isOffer = isOffer,
                                    imageUrl = if (productImageBase64.isNotEmpty()) "data:image/jpeg;base64,$productImageBase64" else ""
                                )
                                viewModel.saveProduct(p)
                                android.widget.Toast.makeText(context, "✅ تم إضافة المنتج بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                    ) {
                        Text("حفظ السلعة", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------
// 5. PROPERTY DETAILS DIALOG
// --------------------------------------------------------
@Composable
fun PropertyDetailsDialog(
    property: PropertyEntity,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val ratings by viewModel.ratings.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val context = LocalContext.current

    val propReviews = remember(ratings) { ratings.filter { it.targetId == property.id && it.targetType == "PROPERTY" && it.isApproved } }

    var userRatingInput by remember { mutableStateOf(5f) }
    var userCommentInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = themeColors.background,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Photo Gallery/Cover
                val hasImages = property.images.isNotEmpty()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (hasImages) 150.dp else 90.dp)
                        .background(themeColors.primary.copy(alpha = 0.15f))
                ) {
                    if (!hasImages) {
                        Text("🏠 ${property.title}", modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("📸 معرض صور العقار التفصيلية", modifier = Modifier.align(Alignment.Center), color = Color.Gray, fontSize = 12.sp)
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.Red)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .background(if (property.type == "rent") Color(0xFF0284C7) else Color(0xFF16A34A), RoundedCornerShape(bottomEnd = 8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (property.type == "rent") "للإيجار" else "للبيع",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val adminRole by viewModel.adminRole.collectAsState()
                    var showEditDialog by remember { mutableStateOf(false) }

                    if (adminRole != "GUEST") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, Color.Red),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🛠️ لوحة تحكم الإدارة الفورية لهذا العقار:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { showEditDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        modifier = Modifier.weight(1f).height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("📝 تعديل البيانات", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.deleteProperty(property.id)
                                            onDismiss()
                                            android.widget.Toast.makeText(context, "🗑️ تم حذف وحظر العقار من النظام بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier.weight(1f).height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("🗑️ حذف وحظر", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (showEditDialog) {
                        PropertyCreateEditDialog(
                            property = property,
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onDismiss = { showEditDialog = false }
                        )
                    }

                    Text(property.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("السعر المطلوب المالي:", fontSize = 9.sp, color = themeColors.textSecondary)
                            Text("${property.price} ${property.currency}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(String.format("%.1f", property.rating), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        }
                    }

                    Divider(color = themeColors.accent.copy(alpha = 0.15f))

                    Text("📝 تفاصيل و مواصفات العقار:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    Text(property.description, fontSize = 11.sp, color = Color.White)

                    // Show image gallery if available
                    if (property.images.isNotEmpty()) {
                        Text("📸 معرض صور العقار التفصيلية (${property.images.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        ) {
                            items(property.images.size) { index ->
                                val bMap = rememberBase64Bitmap(property.images[index])
                                if (bMap != null) {
                                    Image(
                                        bitmap = bMap,
                                        contentDescription = null,
                                        modifier = Modifier.width(160.dp).fillMaxHeight().clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    // Show PDF link if available and approved
                    if (property.pdfFileBase64.isNotEmpty() && property.pdfStatus == "APPROVED") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            modifier = Modifier.fillMaxWidth().clickable {
                                android.widget.Toast.makeText(context, "📄 تم تحميل مستندات وخرائط وثيقة الملكية المعتمدة للعقار بنجاح!", android.widget.Toast.LENGTH_LONG).show()
                            }
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("📄", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("عرض وثائق وتفاصيل المخططات الهندسية والملكية (PDF)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                                    Text("مستند رسمي معتمد وموثق ومفحوص من الإدارة", fontSize = 9.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("نوع العقار", fontSize = 8.sp, color = themeColors.textSecondary)
                                Text(property.propertyType, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📍 الحي والمربع السكني", fontSize = 8.sp, color = themeColors.textSecondary)
                                Text(property.localNeighborhood, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Contacts panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val u = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.phone}"))
                                context.startActivity(u)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اتصال بالمالك", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val u = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${property.phone.replace("+", "")}"))
                                context.startActivity(u)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💬 واتساب مالي", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    Divider(color = themeColors.accent.copy(alpha = 0.15f))

                    // Reviews/Comments list
                    Text("💬 الاستفسارات والتعليقات (${propReviews.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    propReviews.forEach { rev ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(rev.userName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Row {
                                        repeat(rev.rating.toInt()) { Text("★", fontSize = 8.sp, color = themeColors.accent) }
                                    }
                                }
                                Text(rev.comment, fontSize = 10.sp, color = themeColors.textSecondary)
                            }
                        }
                    }

                    // Add comment form
                    Card(
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("➕ أضف استفسارك أو تقييمك للعقار:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            OutlinedTextField(
                                value = userCommentInput,
                                onValueChange = { userCommentInput = it },
                                placeholder = { Text("اكتب سؤالك أو تعليقك...", fontSize = 11.sp, color = themeColors.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Button(
                                onClick = {
                                    if (userCommentInput.isNotEmpty()) {
                                        viewModel.addRating(
                                            RatingEntity(
                                                targetId = property.id,
                                                targetType = "PROPERTY",
                                                userId = currentUserId,
                                                userName = currentUserName.ifEmpty { "عميل عقارات" },
                                                rating = userRatingInput,
                                                comment = userCommentInput
                                            )
                                        )
                                        userCommentInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("إرسال", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

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

@Composable
fun AdminJobsPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val activatedProviders by viewModel.providers.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var activeJobsSearchQuery by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("💼 إدارة شواغر التوظيف والوظائف الشاملة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        Text("البحث والتحكم في شارات وتفاصيل إعلانات الوظائف وإمكانيات التواصل والاتصال والحذف:", fontSize = 11.sp, color = themeColors.textSecondary)
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = activeJobsSearchQuery,
            onValueChange = { activeJobsSearchQuery = it },
            label = { Text("البحث في شواغر الوظائف والمهن...") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = themeColors.accent) }
        )

        val filteredJobs = activatedProviders.filter {
            val isJob = it.categoryId.contains("job", ignoreCase = true) || 
                        it.profession.contains("job", ignoreCase = true) || 
                        it.profession.contains("وظيفة", ignoreCase = true) || 
                        it.profession.contains("توظيف", ignoreCase = true) || 
                        it.profession.contains("شاغر", ignoreCase = true) || 
                        it.profession.contains("وظائف", ignoreCase = true)
            isJob && (
                it.name.contains(activeJobsSearchQuery, ignoreCase = true) ||
                it.phone.contains(activeJobsSearchQuery, ignoreCase = true) ||
                it.area.contains(activeJobsSearchQuery, ignoreCase = true)
            )
        }

        if (filteredJobs.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد إعلانات وظائف مطابقة للبحث حالياً.", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        } else {
            filteredJobs.forEach { p ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                val catName = if (p.categoryId == "other" && p.customCategoryName.isNotEmpty()) p.customCategoryName else (categories.find { it.id == p.categoryId }?.name ?: "إعلان توظيف")
                                Text("المسمى الوظيفي: ${p.profession.ifEmpty { catName }} | المنطقة: ${p.area}", fontSize = 11.sp, color = themeColors.textSecondary)
                            }
                            IconButton(
                                onClick = { viewModel.removeProvider(p.id) }
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الإعلان نهائياً", tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isVip,
                                    onCheckedChange = { viewModel.pinProvider(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("VIP ذهبي", fontSize = 10.sp, color = Color.White, maxLines = 1)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isVerified,
                                    onCheckedChange = { viewModel.verifyProviderBadge(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("موثق حساب", fontSize = 10.sp, color = Color.White, maxLines = 1)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isRecommended,
                                    onCheckedChange = { viewModel.recommendProvider(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEC4899)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("موصى به", fontSize = 10.sp, color = Color.White, maxLines = 1)
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isChatDisabled,
                                    onCheckedChange = { viewModel.setProviderChatDisabled(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("إيقاف الدردشة 🔇", fontSize = 9.sp, color = Color.LightGray, maxLines = 1)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isNotificationsDisabled,
                                    onCheckedChange = { viewModel.setProviderNotificationsDisabled(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("إيقاف الإشعارات 🔕", fontSize = 9.sp, color = Color.LightGray, maxLines = 1)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = p.isPaymentRequired,
                                    onCheckedChange = { viewModel.setProviderPaymentRequired(p.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981)),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("ربط بالدفع 💳", fontSize = 9.sp, color = Color.LightGray, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminJobApplicantsPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val applications by viewModel.jobApplications.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var showNotifDialog by remember { mutableStateOf(false) }
    var notifTitle by remember { mutableStateOf("") }
    var notifMessage by remember { mutableStateOf("") }
    var rejectReasonAppId by remember { mutableStateOf<String?>(null) }
    var rejectReasonText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("📄 إدارة المتقدمين للوظائف والسير الذاتية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("مراجعة وتصدير وقبول/رفض طلبات المتقدمين مع إمكانية التواصل المباشر:", fontSize = 11.sp, color = themeColors.textSecondary)
            }
        }

        // Quick Export and Notify Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.exportJobApplicantsCsv(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("📋 تصدير CSV للحافظة", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { showNotifDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🔔 إشعار جماعي للمتقدمين", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("البحث بالاسم، الرقم، الوظيفة، أو المؤهل...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = themeColors.accent) }
        )

        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "ALL" to "الكل (${applications.size})",
                "PENDING" to "قيد الانتظار (${applications.count { it.status == "PENDING" }})",
                "ACCEPTED" to "مقبول (${applications.count { it.status == "ACCEPTED" }})",
                "REJECTED" to "مرفوض (${applications.count { it.status == "REJECTED" }})"
            ).forEach { (statusKey, label) ->
                val isSel = selectedStatusFilter == statusKey
                Button(
                    onClick = { selectedStatusFilter = statusKey },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSel) themeColors.accent else Color.DarkGray),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(label, fontSize = 9.sp, color = if (isSel) Color.Black else Color.White, maxLines = 1)
                }
            }
        }

        val filteredApps = applications.filter { app ->
            val matchesStatus = selectedStatusFilter == "ALL" || app.status == selectedStatusFilter
            val matchesSearch = searchQuery.isEmpty() ||
                    app.applicantName.contains(searchQuery, ignoreCase = true) ||
                    app.applicantPhone.contains(searchQuery, ignoreCase = true) ||
                    app.jobTitle.contains(searchQuery, ignoreCase = true) ||
                    app.applicantQuals.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }

        if (filteredApps.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("لا يوجد متقدمون مطابقون للبحث الحالي.", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        } else {
            filteredApps.forEach { app ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, when (app.status) {
                        "ACCEPTED" -> Color.Green.copy(alpha = 0.3f)
                        "REJECTED" -> Color.Red.copy(alpha = 0.3f)
                        else -> themeColors.accent.copy(alpha = 0.2f)
                    })
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(app.applicantName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Surface(
                                color = when (app.status) {
                                    "ACCEPTED" -> Color.Green.copy(alpha = 0.2f)
                                    "REJECTED" -> Color.Red.copy(alpha = 0.2f)
                                    else -> Color.Yellow.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = when (app.status) {
                                        "ACCEPTED" -> "✅ مقبول"
                                        "REJECTED" -> "❌ مرفوض"
                                        else -> "⏳ قيد الدراسية"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (app.status) {
                                        "ACCEPTED" -> Color.Green
                                        "REJECTED" -> Color.Red
                                        else -> Color.Yellow
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text("الوظيفة: ${app.jobTitle} (${app.companyName})", fontSize = 10.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                        Text("📱 رقم الهاتف: ${app.applicantPhone}", fontSize = 10.sp, color = Color.White)
                        
                        if (app.applicantQuals.isNotEmpty()) {
                            Text("📝 المؤهلات والخبرات:\n${app.applicantQuals}", fontSize = 9.sp, color = Color.LightGray)
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.acceptJobApplication(app.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("قبول الطلب ✅", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { rejectReasonAppId = app.id },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("رفض الطلب ❌", fontSize = 9.sp, color = Color.White)
                            }

                            IconButton(
                                onClick = { viewModel.deleteJobApplication(app.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الطلب", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // Broadcast Dialog
    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            title = { Text("🔔 إرسال إشعار للمتقدمين للوظائف", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = notifTitle,
                        onValueChange = { notifTitle = it },
                        label = { Text("عنوان الإشعار") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notifMessage,
                        onValueChange = { notifMessage = it },
                        label = { Text("نص الرسالة الإشعارية") },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (notifTitle.isNotEmpty() && notifMessage.isNotEmpty()) {
                            viewModel.sendNotificationToApplicants(notifTitle, notifMessage)
                            showNotifDialog = false
                            notifTitle = ""
                            notifMessage = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("إرسال الآن 🚀", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // Reject Reason Dialog
    rejectReasonAppId?.let { appId ->
        AlertDialog(
            onDismissRequest = { rejectReasonAppId = null },
            title = { Text("سبب رفض طلب التقديم", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                OutlinedTextField(
                    value = rejectReasonText,
                    onValueChange = { rejectReasonText = it },
                    label = { Text("اكتب سبب الرفض الموجه للمتقدم...") },
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectJobApplication(appId, rejectReasonText.ifEmpty { "عدم مطابقة الشروط المطلوب إيفاؤها" })
                        rejectReasonAppId = null
                        rejectReasonText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("تأكيد الرفض ❌", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectReasonAppId = null }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun AdminCentralizedBlockedPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    data class BlockedItem(
        val id: String,
        val type: String, // PROVIDER, STORE, RESTAURANT, MEDICAL, PROPERTY, JOB
        val name: String,
        val phone: String,
        val blockReason: String
    )

    val allBlocked = mutableListOf<BlockedItem>()

    providers.filter { it.isBlocked || it.subscriptionStatus == "SUSPENDED" }.forEach { p ->
        allBlocked.add(BlockedItem(p.id, "PROVIDER", p.name, p.phone, "حظر إداري عام"))
    }

    stores.filter { it.isBlocked }.forEach { s ->
        val typeStr = when {
            s.categoryId.contains("طبي") || s.categoryId.contains("عياد") -> "MEDICAL"
            s.categoryId.contains("مطعم") || s.categoryId.contains("كافيه") -> "RESTAURANT"
            else -> "STORE"
        }
        allBlocked.add(BlockedItem(s.id, typeStr, s.name, s.phone, s.blockReason.ifEmpty { "حظر إداري" }))
    }

    properties.filter { it.isBlocked }.forEach { pr ->
        allBlocked.add(BlockedItem(pr.id, "PROPERTY", pr.title, pr.phone, pr.blockReason.ifEmpty { "حظر إداري" }))
    }

    jobs.filter { it.isBlocked }.forEach { j ->
        allBlocked.add(BlockedItem(j.id, "JOB", j.title, j.phone, j.blockReason.ifEmpty { "حظر إداري" }))
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("🚫 القائمة المركزية لإدارة الكيانات المحظورة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        Text("عرض وإلغاء حظر جميع الفنيين والمحلات والمطاعم والمراكز الطبية والعقارات والوظائف من مكان واحد:", fontSize = 11.sp, color = themeColors.textSecondary)

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("البحث في الكيانات المحظورة...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = themeColors.accent) }
        )

        // Category filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "ALL" to "الكل (${allBlocked.size})",
                "PROVIDER" to "فنيين",
                "STORE" to "محلات",
                "RESTAURANT" to "مطاعم",
                "MEDICAL" to "طبية",
                "PROPERTY" to "عقارات",
                "JOB" to "وظائف"
            ).forEach { (catKey, label) ->
                val isSel = selectedCategoryFilter == catKey
                Button(
                    onClick = { selectedCategoryFilter = catKey },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSel) themeColors.accent else Color.DarkGray),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(label, fontSize = 8.sp, color = if (isSel) Color.Black else Color.White, maxLines = 1)
                }
            }
        }

        val filteredBlocked = allBlocked.filter { item ->
            val matchesType = selectedCategoryFilter == "ALL" || item.type == selectedCategoryFilter
            val matchesSearch = searchQuery.isEmpty() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.phone.contains(searchQuery, ignoreCase = true) ||
                    item.blockReason.contains(searchQuery, ignoreCase = true)
            matchesType && matchesSearch
        }

        if (filteredBlocked.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("🎉 لا توجد كيانات محظورة حالياً ضمن النطاق المSelected.", fontSize = 11.sp, color = Color.Green)
                }
            }
        } else {
            filteredBlocked.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.Red.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = when (item.type) {
                                            "PROVIDER" -> "🔧 فني"
                                            "STORE" -> "🏪 محل"
                                            "RESTAURANT" -> "🍔 مطعم"
                                            "MEDICAL" -> "🏥 مركز طبي"
                                            "PROPERTY" -> "🏠 عقار"
                                            "JOB" -> "💼 وظيفة"
                                            else -> "كيان"
                                        },
                                        fontSize = 9.sp,
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text("الهاتف: ${item.phone}", fontSize = 10.sp, color = themeColors.textSecondary)
                            Text("سبب الحظر: ${item.blockReason}", fontSize = 9.sp, color = Color(0xFFF87171))
                        }

                        Button(
                            onClick = { viewModel.unbanEntity(item.type, item.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إلغاء الحظر ✅", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCentralizedDeletedPanel(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val providers by viewModel.providers.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    data class DeletedItem(
        val id: String,
        val type: String, // PROVIDER, STORE, PROPERTY, JOB
        val name: String,
        val phone: String,
        val deletedAt: Long
    )

    val allDeleted = mutableListOf<DeletedItem>()

    providers.filter { it.isDeleted }.forEach { p ->
        allDeleted.add(DeletedItem(p.id, "PROVIDER", p.name, p.phone, p.deletedAt ?: 0L))
    }

    stores.filter { it.isDeleted }.forEach { s ->
        allDeleted.add(DeletedItem(s.id, "STORE", s.name, s.phone, s.deletedAt ?: 0L))
    }

    properties.filter { it.isDeleted }.forEach { pr ->
        allDeleted.add(DeletedItem(pr.id, "PROPERTY", pr.title, pr.phone, pr.deletedAt ?: 0L))
    }

    jobs.filter { it.isDeleted }.forEach { j ->
        allDeleted.add(DeletedItem(j.id, "JOB", j.title, j.phone, 0L))
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("🗑️ سلة المحذوفات المركزية لكافة الكيانات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        Text("استعادة الكيانات المحذوفة ناعماً (Soft Delete) أو حذفها نهائياً من قاعدة البيانات:", fontSize = 11.sp, color = themeColors.textSecondary)

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("البحث في العناصر المحذوفة...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            trailingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = themeColors.accent) }
        )

        // Category Filter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "ALL" to "الكل (${allDeleted.size})",
                "PROVIDER" to "فنيين",
                "STORE" to "محلات/مراكز",
                "PROPERTY" to "عقارات",
                "JOB" to "وظائف"
            ).forEach { (catKey, label) ->
                val isSel = selectedCategoryFilter == catKey
                Button(
                    onClick = { selectedCategoryFilter = catKey },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSel) themeColors.accent else Color.DarkGray),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(label, fontSize = 9.sp, color = if (isSel) Color.Black else Color.White, maxLines = 1)
                }
            }
        }

        val filteredDeleted = allDeleted.filter { item ->
            val matchesType = selectedCategoryFilter == "ALL" || item.type == selectedCategoryFilter
            val matchesSearch = searchQuery.isEmpty() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.phone.contains(searchQuery, ignoreCase = true)
            matchesType && matchesSearch
        }

        if (filteredDeleted.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("سلة المحذوفات فارغة حالياً.", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        } else {
            filteredDeleted.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = when (item.type) {
                                            "PROVIDER" -> "🔧 فني"
                                            "STORE" -> "🏪 محل/مركز"
                                            "PROPERTY" -> "🏠 عقار"
                                            "JOB" -> "💼 وظيفة"
                                            else -> "عنصر"
                                        },
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text("الهاتف: ${item.phone}", fontSize = 10.sp, color = themeColors.textSecondary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.restoreEntity(item.type, item.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("استعادة الكيان ♻️", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.hardDeleteEntity(item.type, item.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("حذف نهائي 🗑️", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}


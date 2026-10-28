package com.serviseyem.ui.screens.entities

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.*
import com.serviseyem.ui.components.BespokePremiumSystem
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.utils.VisualThemePalette

@Composable
fun EmptyStateBox(msg: String, themeColors: VisualThemePalette) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder)
    ) {
        Box(
            modifier = Modifier.padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = msg, color = Color(0xFF94A3B8), fontSize = 12.5.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ProfileTabContent(
    selectedTab: Int,
    entityType: ProfileEntityType,
    provider: ProviderEntity?,
    store: StoreEntity?,
    property: PropertyEntity?,
    job: JobEntity?,
    products: List<ProductEntity>,
    entityReviews: List<RatingEntity>,
    entityDescription: String,
    themeColors: VisualThemePalette,
    isOwner: Boolean = false,
    onAddReviewClick: () -> Unit
) {
    val context = LocalContext.current

    when (entityType) {
        ProfileEntityType.STORE -> {
            StoreTabContent(
                selectedTab = selectedTab,
                store = store,
                products = products,
                entityReviews = entityReviews,
                themeColors = themeColors,
                isOwner = isOwner,
                onAddReviewClick = onAddReviewClick
            )
        }
        ProfileEntityType.RESTAURANT -> {
            RestaurantTabContent(
                selectedTab = selectedTab,
                store = store,
                products = products,
                entityReviews = entityReviews,
                themeColors = themeColors,
                isOwner = isOwner,
                onAddReviewClick = onAddReviewClick
            )
        }
        ProfileEntityType.MEDICAL -> {
            MedicalTabContent(
                selectedTab = selectedTab,
                store = store,
                provider = provider,
                entityReviews = entityReviews,
                themeColors = themeColors,
                isOwner = isOwner,
                onAddReviewClick = onAddReviewClick
            )
        }
        ProfileEntityType.REAL_ESTATE -> {
            RealEstateTabContent(
                selectedTab = selectedTab,
                property = property,
                themeColors = themeColors
            )
        }
        ProfileEntityType.JOB -> {
            JobTabContent(
                selectedTab = selectedTab,
                job = job,
                themeColors = themeColors
            )
        }
        ProfileEntityType.TECHNICIAN -> {
            TechnicianTabContent(
                selectedTab = selectedTab,
                provider = provider,
                entityReviews = entityReviews,
                themeColors = themeColors,
                isOwner = isOwner,
                onAddReviewClick = onAddReviewClick
            )
        }
        ProfileEntityType.GENERAL -> {
            GeneralTabContent(
                selectedTab = selectedTab,
                entityDescription = entityDescription,
                entityReviews = entityReviews,
                themeColors = themeColors,
                isOwner = isOwner,
                onAddReviewClick = onAddReviewClick
            )
        }
    }
}

/* ══════════════════════════════════════════════════════════════════════
   1. STORE TAB CONTENT
   Tabs: 0:الرئيسية · 1:المنتجات · 2:الكتالوج · 3:العروض · 4:التقييمات · 5:السياسة · 6:الفروع
══════════════════════════════════════════════════════════════════════ */
@Composable
private fun StoreTabContent(
    selectedTab: Int,
    store: StoreEntity?,
    products: List<ProductEntity>,
    entityReviews: List<RatingEntity>,
    themeColors: VisualThemePalette,
    isOwner: Boolean,
    onAddReviewClick: () -> Unit
) {
    val context = LocalContext.current
    val storeProducts = remember(products, store) {
        products.filter { it.storeId == (store?.id ?: "") }
    }

    when (selectedTab) {
        0 -> { // الرئيسية
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Promo Banner
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))))
                            .padding(18.dp)
                    ) {
                        Column {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFD166)) {
                                Text("عرض خاص 🛍️", color = Color(0xFF1E1B4B), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("تسوق الآن واحصل على توصيل مجاني!", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                            Text("عند الطلب بمبلغ 20,000 ريال يمني أو أكثر", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                        }
                    }
                }

                // Stats strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatPill(modifier = Modifier.weight(1f), icon = "📦", title = "${storeProducts.size}+", subtitle = "منتج معتمد")
                    StatPill(modifier = Modifier.weight(1f), icon = "⭐", title = "5.0", subtitle = "تقييم المتجر")
                    StatPill(modifier = Modifier.weight(1f), icon = "⚡", title = "24 ساعة", subtitle = "توصيل سريع")
                }

                // Best sellers preview
                Text("الأكثر مبيعاً 🌟", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (storeProducts.isEmpty()) {
                    EmptyStateBox("جاري تحديث قائمة المنتجات الأكثر مبيعاً لهذا المتجر.", themeColors)
                } else {
                    storeProducts.take(4).chunked(2).forEach { rowPair ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowPair.forEach { prod ->
                                Box(modifier = Modifier.weight(1f)) { ProfileProductCard(product = prod, themeColors = themeColors) }
                            }
                            if (rowPair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        1 -> { // المنتجات
            StoreProductsGrid(storeProducts = storeProducts, store = store, themeColors = themeColors)
        }
        2 -> { // الكتالوج
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("كتالوج المعروضات الرسمي 📚", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (storeProducts.isEmpty()) {
                    EmptyStateBox("الكتالوج الإلكتروني قيد الإعداد والإضافة.", themeColors)
                } else {
                    storeProducts.forEach { prod ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BespokePremiumSystem.SurfaceDark,
                            border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SmartAsyncImage(
                                    model = prod.imageUrl,
                                    contentDescription = prod.name,
                                    modifier = Modifier.size(68.dp).clip(RoundedCornerShape(12.dp))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(prod.description.ifEmpty { "صنف أصلي ومضمون" }, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 1)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${prod.price.toInt()} ر.ي", color = Color(0xFFFFD166), fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                                Button(
                                    onClick = { Toast.makeText(context, "تمت إضافة ${prod.name} للسلة", Toast.LENGTH_SHORT).show() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("طلب", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        3 -> { // العروض والخصومات
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("كوبونات وخصومات حصرية 🎟️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                CouponCard(code = "YEMEN20", discount = "خصم 20%", desc = "على كافة المشتريات بقيمة تتجاوز 15,000 ريال")
                CouponCard(code = "FIRSTBUY", discount = "خصم 15%", desc = "هدية ترحيبية لأول طلب عبر دليل خدمات اليمن")
            }
        }
        4 -> { // التقييمات
            ReviewsSection(entityReviews = entityReviews, isOwner = isOwner, themeColors = themeColors, onAddReviewClick = onAddReviewClick)
        }
        5 -> { // سياسة المتجر
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("سياسات المتجر والضمان 🛡️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("🔄 سياسة الاستبدال والاسترجاع", "يمكن استبدال أو استرجاع المنتجات خلال 3 أيام من تاريخ الاستلام بشرط بقاء المنتج في حالته الأصلية وتغليفه الأصلي.")
                PolicyCard("🚚 أوقات وطرق التوصيل", "التوصيل يتم خلال 2-24 ساعة في أمانة العاصمة، وخلال 48 ساعة لبقية المحافظات اليمنية.")
                PolicyCard("💳 طرق الدفع المعتمدة", "الدفع عند الاستلام نقداً، أو عبر المحافظ الإلكترونية المعتمدة (الكريمي، جيب، جوالي، ون كاش).")
            }
        }
        6 -> { // الفروع
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("فروع المتجر المعتمدة 📍", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                BranchCard(name = "الفرع الرئيسي - صنعاء", address = store?.localNeighborhood?.ifEmpty { "شارع حدة - تقاطع القدس" } ?: "شارع حدة", hours = "9:00 ص - 11:00 م")
                BranchCard(name = "فرع عدن", address = "المنصورة - ريمي", hours = "8:30 ص - 10:30 م")
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════════════
   2. RESTAURANT TAB CONTENT
   Tabs: 0:القائمة · 1:الأطباق الأكثر طلبًا · 2:العروض · 3:التقييمات · 4:المعلومات · 5:الحجز
══════════════════════════════════════════════════════════════════════ */
@Composable
private fun RestaurantTabContent(
    selectedTab: Int,
    store: StoreEntity?,
    products: List<ProductEntity>,
    entityReviews: List<RatingEntity>,
    themeColors: VisualThemePalette,
    isOwner: Boolean,
    onAddReviewClick: () -> Unit
) {
    val context = LocalContext.current
    val storeProducts = remember(products, store) {
        products.filter { it.storeId == (store?.id ?: "") }
    }

    when (selectedTab) {
        0 -> { // القائمة
            StoreProductsGrid(storeProducts = storeProducts, store = store, themeColors = themeColors)
        }
        1 -> { // الأكثر طلباً
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("الأطباق الأكثر شهرة وطلباً 🥇", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                val topDishes = if (storeProducts.isNotEmpty()) storeProducts.take(4) else emptyList()
                if (topDishes.isEmpty()) {
                    EmptyStateBox("قائمة الأكثر طلباً قيد التحديث.", themeColors)
                } else {
                    topDishes.forEachIndexed { idx, dish ->
                        val medal = when (idx) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "⭐" }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BespokePremiumSystem.SurfaceDark,
                            border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(medal, fontSize = 24.sp)
                                SmartAsyncImage(
                                    model = dish.imageUrl,
                                    contentDescription = dish.name,
                                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dish.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("طبق مميز ومفضل للزبائن", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text("${dish.price.toInt()} ر.ي", color = Color(0xFFFF5A5F), fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                                Button(
                                    onClick = { Toast.makeText(context, "تمت إضافة ${dish.name} للطلب", Toast.LENGTH_SHORT).show() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("أضف", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        2 -> { // العروض
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("عروض الوجبات والخصومات 🍔", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                DealCard(title = "اشترِ 2 واحصل على 1 مجاناً", subtitle = "على جميع الوجبات العائلية والمشاوي", code = "MEAL2PLUS1")
                DealCard(title = "خصم 25% للطلب الأول", subtitle = "توصيل فوري خلال 30 دقيقة فقط", code = "TASTE25")
            }
        }
        3 -> { // التقييمات
            ReviewsSection(entityReviews = entityReviews, isOwner = isOwner, themeColors = themeColors, onAddReviewClick = onAddReviewClick)
        }
        4 -> { // المعلومات
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("معلومات وتفاصيل المطعم ℹ️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                InfoRowItem(icon = "⏱️", title = "وقت التوصيل التقريبي", desc = "30 - 45 دقيقة")
                InfoRowItem(icon = "🛵", title = "رسوم التوصيل", desc = "500 - 1000 ريال يمني حسب المنطقة")
                InfoRowItem(icon = "🕒", title = "أوقات العمل اليومية", desc = "10:00 ص حتى 1:00 بعد منتصف الليل")
                InfoRowItem(icon = "👨‍👩‍👧‍👦", title = "جلسات العائلات", desc = "تتوفر صالات عائلية خاصة ومكيفة بالكامل")
            }
        }
        5 -> { // الحجز
            ReservationCard(storeName = store?.name ?: "المطعم")
        }
    }
}

/* ══════════════════════════════════════════════════════════════════════
   3. MEDICAL TAB CONTENT
   Tabs: 0:الرئيسية · 1:الأطباء · 2:الأقسام · 3:الحجز · 4:المختبر · 5:التقييمات · 6:عن المركز · 7:الفروع
══════════════════════════════════════════════════════════════════════ */
@Composable
private fun MedicalTabContent(
    selectedTab: Int,
    store: StoreEntity?,
    provider: ProviderEntity?,
    entityReviews: List<RatingEntity>,
    themeColors: VisualThemePalette,
    isOwner: Boolean,
    onAddReviewClick: () -> Unit
) {
    val context = LocalContext.current
    when (selectedTab) {
        0 -> { // الرئيسية
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Official accreditation shield
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF0F766E).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(shape = CircleShape, color = Color(0xFF2DD4BF), modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF0F172A)) }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("منشأة طبية معتمدة ومرخصة 🛡️", color = Color(0xFF2DD4BF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("مرخصة رسمياً من وزارة الصحة العامة والسكان باليمن", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                        }
                    }
                }

                // Quick actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MedicalQuickAction(modifier = Modifier.weight(1f), icon = "🚑", title = "طوارئ 24/7", color = Color(0xFFEF4444)) {
                        Toast.makeText(context, "الاتصال المباشر بالطوارئ", Toast.LENGTH_SHORT).show()
                    }
                    MedicalQuickAction(modifier = Modifier.weight(1f), icon = "🩺", title = "حجز كشف", color = Color(0xFF2DD4BF)) {
                        Toast.makeText(context, "فتح شاشة حجز موعد الطبيب", Toast.LENGTH_SHORT).show()
                    }
                    MedicalQuickAction(modifier = Modifier.weight(1f), icon = "🧪", title = "نتائج التحاليل", color = Color(0xFF38BDF8)) {
                        Toast.makeText(context, "الاستعلام عن نتائج الفحوصات", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        1 -> { // الأطباء
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("نخبة الأطباء والاستشاريين 👨‍⚕️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                DoctorCard(name = "د. أحمد القدسي", specialty = "استشاري أمراض القلب والأوعية الدموية", exp = "خبرة 15 عاماً", rating = "4.9")
                DoctorCard(name = "د. نادية الشامي", specialty = "أخصائية طب الأطفال وحديثي الولادة", exp = "خبرة 10 أعوام", rating = "5.0")
                DoctorCard(name = "د. طارق الحكيمي", specialty = "استشاري جراحة العظام والمفاصل", exp = "خبرة 18 عاماً", rating = "4.8")
            }
        }
        2 -> { // الأقسام والتخصصات
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("الأقسام والعيادات التخصصية 🏥", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                val specialties = listOf(
                    "طب وجراحة القلب 🫀", "طب وجراحة العيون 👁️", "طب وجراحة الأسنان 🦷",
                    "طب الأطفال وحديثي الولادة 👶", "النساء والولادة 🩺", "العظام والمفاصل 🦴",
                    "الأنف والأذن والحنجرة 👂", "الأمراض الجلدية والليزر ✨"
                )
                specialties.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { spec ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BespokePremiumSystem.SurfaceDark,
                                border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = spec,
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        3 -> { // الحجز
            AppointmentBookingFlow(medicalName = store?.name ?: "المركز الطبي")
        }
        4 -> { // المختبر والأشعة
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("باقات الفحوصات والتحاليل المخبرية 🧪", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                LabPackageCard(title = "باقة الفحص الشامل", tests = "سكر، وظائف كبد وكلى، دهون، دم كامل CBC", price = "18,000 ر.ي")
                LabPackageCard(title = "باقة صحة القلب", tests = "تخطيط قلب، إنزيمات، دهون ثلاثية، كوليسترول", price = "22,000 ر.ي")
                LabPackageCard(title = "خدمة السحب المنزلي للعينات", tests = "فريق تمريضي متخصص يصل لمنزلك", price = "مجاناً مع الباقات")
            }
        }
        5 -> { // التقييمات
            ReviewsSection(entityReviews = entityReviews, isOwner = isOwner, themeColors = themeColors, onAddReviewClick = onAddReviewClick)
        }
        6 -> { // عن المركز
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("عن المركز الطبي والتجهيزات ℹ️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("🏥 التجهيزات الطبية", "أحدث أجهزة الرنين المغناطيسي والأشعة المقطعية ومختبرات آلية معتمدة عالمياً.")
                PolicyCard("🚑 طوارئ واستقبال 24 ساعة", "قسم طوارئ مجهز بكامل أجهزة الإنعاش والعناية الفائقة طوال 24 ساعة يومياً.")
                PolicyCard("🤝 شركات التأمين المعتمدة", "نقبل بطاقات التأمين الصحي لكبرى الشركات (الشركة المتحدة، كاك للتأمين، وغيرها).")
            }
        }
        7 -> { // الفروع
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("المستشفيات والعيادات التابعة 📍", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                BranchCard(name = "المركز الرئيسي والمستشفى", address = store?.localNeighborhood?.ifEmpty { "صنعاء - شارع الزبيري" } ?: "صنعاء", hours = "طوارئ 24/7")
                BranchCard(name = "مجمع العيادات الخارجية", address = "صنعاء - شارع بغداد", hours = "8:00 ص - 9:00 م")
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════════════
   4. REAL ESTATE TAB CONTENT
══════════════════════════════════════════════════════════════════════ */
@Composable
private fun RealEstateTabContent(
    selectedTab: Int,
    property: PropertyEntity?,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    when (selectedTab) {
        0 -> { // نظرة عامة
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("نظرة عامة على العقار 🏠", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BespokePremiumSystem.SurfaceDark,
                    border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(property?.title ?: "عقار متميز", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(property?.description?.ifEmpty { "عقار في موقع استراتيجي وتشطيبات فاخرة VIP" } ?: "", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("السعر الإجمالي:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("${property?.price?.toInt() ?: 0} ${property?.currency ?: "ر.ي"}", color = Color(0xFF10B981), fontSize = 15.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
        1 -> { // الموقع
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("تفاصيل الموقع والخدمات المحيطة 📍", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                InfoRowItem(icon = "📍", title = "المنطقة والحي", desc = property?.localNeighborhood?.ifEmpty { property.cityId } ?: "اليمن")
                InfoRowItem(icon = "🕌", title = "القرب من المسجد", desc = "على بعد 100 متر فقط")
                InfoRowItem(icon = "🏫", title = "المدارس والجامعات", desc = "5 دقائق سيراً على الأقدام")
                InfoRowItem(icon = "🛒", title = "الأسواق والمولات", desc = "بالقرب من مركز تجاري رئيسي")
            }
        }
        2 -> { // المواصفات
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoRowItem(icon = "📐", title = "المساحة الإجمالية", desc = "150 متر مربع")
                InfoRowItem(icon = "🛏️", title = "عدد الغرف", desc = "3 غرف نوم واسعة ومجلس")
                InfoRowItem(icon = "🚿", title = "دورات المياه", desc = "2 حمامات تشطيب سوبر لوكس")
                InfoRowItem(icon = "🏢", title = "نوع وتصنيف العقار", desc = property?.propertyType?.ifEmpty { "شقة سكنية" } ?: "شقة سكنية")
            }
        }
        3 -> { // التقسيط والدفع
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("خطط الدفع والتسهيلات المالية 💳", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("💵 الدفع النقدي الفوري", "خصم خاص 5% عند السداد المباشر كاش أو عبر التحويل البنكي.")
                PolicyCard("📅 التقسيط الميسر", "مقدم 30% مع تقسيط المتبقي على دفعات ميسرة تمتد حتى 24 شهراً.")
            }
        }
        4 -> { // التواصل
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("تواصل مع الوسيط العقاري المعتمد 📞", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BespokePremiumSystem.SurfaceDark,
                    border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("الوسيط المعتمد: ${property?.phone?.ifEmpty { "مكتب العقارات المعتمد" }}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Button(
                            onClick = { Toast.makeText(context, "جاري فتح المحادثة المباشرة", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تواصل عبر واتساب فوراً 💬", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════════════
   5. JOB TAB CONTENT
══════════════════════════════════════════════════════════════════════ */
@Composable
private fun JobTabContent(
    selectedTab: Int,
    job: JobEntity?,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    when (selectedTab) {
        0 -> { // تفاصيل الوظيفة
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("تفاصيل الشاغر الوظيفي 💼", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                InfoRowItem(icon = "🏢", title = "الجهة المعلنة", desc = job?.companyName?.ifEmpty { "شركة رائدة باليمن" } ?: "")
                InfoRowItem(icon = "📍", title = "مقر العمل", desc = job?.cityId?.ifEmpty { "صنعاء" } ?: "")
                InfoRowItem(icon = "⏱️", title = "نوع الدوام", desc = job?.jobType?.ifEmpty { "دوام كامل (حضوري)" } ?: "")
                InfoRowItem(icon = "📅", title = "آخر موعد للتقديم", desc = "خلال 14 يوماً من النشر")
            }
        }
        1 -> { // المتطلبات
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("المؤهلات والشروط المطلوبة 🎓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("🎓 المؤهل العلمي", job?.requirements?.ifEmpty { "بكالوريوس في التخصص المطلوب أو ما يعادله." } ?: "")
                PolicyCard("💼 سنوات الخبرة", "خبرة لا تقل عن سنتين في نفس المجال.")
                PolicyCard("⚡ المهارات الشخصية", "إتقان استخدام الحاسوب والعمل بروح الفريق الواحد.")
            }
        }
        2 -> { // الرواتب والمزايا
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("الراتب والمزايا والحوافز 🎁", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                InfoRowItem(icon = "💰", title = "الراتب الشهري", desc = if (job?.salary.isNullOrBlank()) "يحدد بعد المقابلة (مجزٍ)" else job?.salary ?: "")
                InfoRowItem(icon = "🏥", title = "التأمين الصحي", desc = "تأمين صحي شامل للموظف وعائلته")
                InfoRowItem(icon = "🚌", title = "بدل مواصلات", desc = "بدل نقل ومواصلات مجزي")
            }
        }
        3 -> { // عن الشركة
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("نبذة عن جهة العمل 🏢", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("🌟 بيئة العمل", "شركة يمنية وطنية رائدة توفر بيئة عمل محفزة وفرصاً للتطوير المستمر.")
            }
        }
        4 -> { // التقديم
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BespokePremiumSystem.SurfaceDark,
                border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("تقديم طلب التوظيف السريع 📄", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("سيتم إرسال سيرتك الذاتية ومعلوماتك مباشرة إلى قسم الموارد البشرية بالشركة.", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                    Button(
                        onClick = { Toast.makeText(context, "تم إرسال طلب التقديم بنجاح! سنتواصل معك قريباً.", Toast.LENGTH_LONG).show() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("إرسال السيرة الذاتية الآن 🚀", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════════════
   6. TECHNICIAN & GENERAL TAB CONTENT
══════════════════════════════════════════════════════════════════════ */
@Composable
private fun TechnicianTabContent(
    selectedTab: Int,
    provider: ProviderEntity?,
    entityReviews: List<RatingEntity>,
    themeColors: VisualThemePalette,
    isOwner: Boolean,
    onAddReviewClick: () -> Unit
) {
    when (selectedTab) {
        0 -> { // سابقة الأعمال
            val photos = provider?.workPhotosBase64 ?: emptyList()
            if (photos.isEmpty()) {
                EmptyStateBox("لا توجد صور سابقة أعمال مرفوعة حالياً.", themeColors)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photos) { photo ->
                        Card(modifier = Modifier.size(120.dp), shape = RoundedCornerShape(12.dp)) {
                            SmartAsyncImage(model = photo, contentDescription = "عمل سابق", modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
        1 -> { // الخدمات والتسعير
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("الخدمات والتسعير التقريبي 🛠️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                InfoRowItem(icon = "⚡", title = "الكشف والمعاينة", desc = "${provider?.previewPrice?.toInt() ?: 1500} ر.ي")
                InfoRowItem(icon = "🔧", title = "الضمان بعد الإصلاح", desc = "ضمان فني لمدة 30 يوماً")
            }
        }
        2 -> { // التقييمات
            ReviewsSection(entityReviews = entityReviews, isOwner = isOwner, themeColors = themeColors, onAddReviewClick = onAddReviewClick)
        }
        3 -> { // شروط الضمان
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ميثاق الجودة وحماية العميل 🛡️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("🛡️ ميثاق الجودة المعتمد", "يلتزم الفني بتقديم خدمة متقنة مع ضمان مجاني على الأعمال المنجزة لمدة 30 يوماً.")
            }
        }
    }
}

@Composable
private fun GeneralTabContent(
    selectedTab: Int,
    entityDescription: String,
    entityReviews: List<RatingEntity>,
    themeColors: VisualThemePalette,
    isOwner: Boolean,
    onAddReviewClick: () -> Unit
) {
    when (selectedTab) {
        0 -> {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BespokePremiumSystem.SurfaceDark,
                border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(entityDescription, color = Color.White, fontSize = 12.5.sp, modifier = Modifier.padding(14.dp))
            }
        }
        1 -> ReviewsSection(entityReviews = entityReviews, isOwner = isOwner, themeColors = themeColors, onAddReviewClick = onAddReviewClick)
        2 -> PolicyCard("ℹ️ معلومات النشاط", entityDescription)
    }
}

/* ══════════════════════════════════════════════════════════════════════
   HELPER UI COMPONENTS
══════════════════════════════════════════════════════════════════════ */
@Composable
private fun StoreProductsGrid(
    storeProducts: List<ProductEntity>,
    store: StoreEntity?,
    themeColors: VisualThemePalette
) {
    var selectedSubCategory by remember { mutableStateOf("الكل") }
    val subCats = remember(storeProducts) {
        val list = mutableListOf("الكل")
        storeProducts.forEach { p ->
            if (p.category.isNotBlank() && !list.contains(p.category)) list.add(p.category)
        }
        if (list.size == 1 && (store?.id == "store_demo_alyusri" || store?.name?.contains("بهارات") == true)) {
            listOf("الكل", "توابل مشكلة", "بهارات اللحوم", "بهارات شاي", "خلطات خاصة")
        } else list
    }

    val filteredProducts = remember(storeProducts, selectedSubCategory) {
        if (selectedSubCategory == "الكل") storeProducts
        else storeProducts.filter { it.category == selectedSubCategory }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("الأقسام والتصنيفات 🏷️", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(subCats) { catName ->
                val isSelected = selectedSubCategory == catName
                Surface(
                    onClick = { selectedSubCategory = catName },
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSelected) Color(0xFF8B5CF6) else BespokePremiumSystem.SurfaceDark,
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else BespokePremiumSystem.GlassBorder)
                ) {
                    Text(
                        catName,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (filteredProducts.isEmpty()) {
            EmptyStateBox("لا توجد أصناف مسجلة في هذا التصنيف حالياً.", themeColors)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredProducts.chunked(2).forEach { rowPair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowPair.forEach { prod ->
                            Box(modifier = Modifier.weight(1f)) { ProfileProductCard(product = prod, themeColors = themeColors) }
                        }
                        if (rowPair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewsSection(
    entityReviews: List<RatingEntity>,
    isOwner: Boolean,
    themeColors: VisualThemePalette,
    onAddReviewClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("التقييمات وآراء العملاء ⭐", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB400))
            if (!isOwner) {
                Button(
                    onClick = onAddReviewClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB400)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("أضف تقييمك ⭐", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (entityReviews.isEmpty()) {
            EmptyStateBox("لا توجد تقييمات مسجلة بعد. كن أول من يكتب تقييماً!", themeColors)
        } else {
            entityReviews.forEach { review ->
                ProfileReviewCard(review = review, themeColors = themeColors)
            }
        }
    }
}

@Composable
private fun StatPill(modifier: Modifier, icon: String, title: String, subtitle: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = Color(0xFF94A3B8), fontSize = 9.sp)
        }
    }
}

@Composable
private fun CouponCard(code: String, discount: String, desc: String) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF8B5CF6).copy(alpha = 0.2f)) {
                    Text(discount, color = Color(0xFF8B5CF6), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            OutlinedButton(
                onClick = { Toast.makeText(context, "تم نسخ الكود: $code", Toast.LENGTH_SHORT).show() },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6))
            ) {
                Text(code, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DealCard(title: String, subtitle: String, code: String) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, Color(0xFFFF5A5F).copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color(0xFFFF5A5F), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = Color.White, fontSize = 11.5.sp)
            }
            Button(
                onClick = { Toast.makeText(context, "تم تفعيل العرض $code", Toast.LENGTH_SHORT).show() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("تفعيل", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PolicyCard(title: String, text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            Text(text, color = Color(0xFF94A3B8), fontSize = 11.5.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun BranchCard(name: String, address: String, hours: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(name, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(address, color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text(hours, color = BespokePremiumSystem.EmeraldAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = BespokePremiumSystem.EmeraldAccent)
        }
    }
}

@Composable
private fun InfoRowItem(icon: String, title: String, desc: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(icon, fontSize = 20.sp)
            Column {
                Text(title, color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                Text(desc, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DoctorCard(name: String, specialty: String, exp: String, rating: String) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = CircleShape, color = Color(0xFF0F766E).copy(alpha = 0.3f), modifier = Modifier.size(50.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("👨‍⚕️", fontSize = 22.sp) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(specialty, color = Color(0xFF2DD4BF), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text("$exp · ⭐ $rating", color = Color(0xFF94A3B8), fontSize = 10.sp)
            }
            Button(
                onClick = { Toast.makeText(context, "حجز موعد مع $name", Toast.LENGTH_SHORT).show() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("احجز", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MedicalQuickAction(modifier: Modifier, icon: String, title: String, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LabPackageCard(title: String, tests: String, price: String) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(tests, color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(price, color = Color(0xFF2DD4BF), fontSize = 12.5.sp, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = { Toast.makeText(context, "تم طلب $title", Toast.LENGTH_SHORT).show() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("طلب فحص", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AppointmentBookingFlow(medicalName: String) {
    val context = LocalContext.current
    var selectedDay by remember { mutableStateOf("اليوم") }
    var selectedSlot by remember { mutableStateOf("05:00 م") }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("حجز موعد كشف فوري في $medicalName 📅", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            // Step 1: Day picker
            Text("1. اختر موعد الحضور:", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("اليوم", "غداً", "بعد غد").forEach { day ->
                    val isSel = selectedDay == day
                    Surface(
                        onClick = { selectedDay = day },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) Color(0xFF2DD4BF) else Color.White.copy(alpha = 0.05f)
                    ) {
                        Text(day, color = if (isSel) Color(0xFF0F172A) else Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                    }
                }
            }

            // Step 2: Time slot
            Text("2. اختر الوقت المناسب:", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("04:30 م", "05:00 م", "06:30 م", "07:00 م").forEach { slot ->
                    val isSel = selectedSlot == slot
                    Surface(
                        onClick = { selectedSlot = slot },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) Color(0xFF2DD4BF) else Color.White.copy(alpha = 0.05f)
                    ) {
                        Text(slot, color = if (isSel) Color(0xFF0F172A) else Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }

            Button(
                onClick = { Toast.makeText(context, "تم تأكيد موعدك بنجاح في $selectedDay الساعة $selectedSlot!", Toast.LENGTH_LONG).show() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("تأكيد حجز الموعد الطبي الآن ✅", color = Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ReservationCard(storeName: String) {
    val context = LocalContext.current
    var guests by remember { mutableIntStateOf(2) }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("حجز طاولة في $storeName 🍽️", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Text("عدد الأفراد:", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = { if (guests > 1) guests-- },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("-", fontSize = 18.sp, color = Color.White)
                }
                Text("$guests أشخاص", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Button(
                    onClick = { guests++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("+", fontSize = 18.sp, color = Color.White)
                }
            }

            Button(
                onClick = { Toast.makeText(context, "تم تأكيد طلب حجز طاولة لـ $guests أفراد بنجاح!", Toast.LENGTH_LONG).show() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("تأكيد حجز الطاولة 🍽️", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

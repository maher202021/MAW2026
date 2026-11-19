package com.serviseyem.ui.screens.entities

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.*
import com.serviseyem.domain.entities.DoctorItem
import com.serviseyem.domain.entities.GalleryAlbumEntity
import com.serviseyem.ui.*
import com.serviseyem.ui.components.BespokePremiumSystem
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.ui.screens.dashboard.components.UnifiedEmptyState
import com.serviseyem.ui.screens.register.GuestRegistrationDialog
import com.serviseyem.utils.EntityIdGenerator
import com.serviseyem.utils.PhoneDialerHelper
import com.serviseyem.utils.VisualThemePalette

@Composable
fun EmptyStateBox(msg: String, themeColors: VisualThemePalette) {
    UnifiedEmptyState(
        title = msg,
        description = "",
        iconText = "📦",
        themeColors = themeColors
    )
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
    onAddReviewClick: () -> Unit,
    viewModel: MainViewModel? = null,
    offers: List<SpecialOfferEntity> = emptyList(),
    doctors: List<DoctorItem> = emptyList(),
    galleryAlbums: List<GalleryAlbumEntity> = emptyList(),
    onSwitchTab: (Int) -> Unit = {},
    onRequestBooking: () -> Unit = {}
) {
    val context = LocalContext.current
    var productToOrder by remember { mutableStateOf<ProductEntity?>(null) }
    var showGuestAuthDialog by remember { mutableStateOf(false) }
    var pendingOrderPayload by remember { mutableStateOf<OrderEntity?>(null) }
    var pendingBookingPayload by remember { mutableStateOf<BookingEntity?>(null) }

    val entityId = store?.id ?: provider?.id ?: property?.id ?: job?.id ?: ""
    val entityDisplayName = store?.name ?: provider?.name ?: property?.title ?: job?.companyName ?: "النشاط التجاري"

    val dispatchOrder: (ProductEntity, Int, String) -> Unit = { product, qty, notes ->
        val uid = viewModel?.currentUserId?.value ?: ""
        val order = OrderEntity(
            id = "",
            storeId = entityId,
            storeName = entityDisplayName,
            productId = product.id,
            productName = product.name,
            customerPhone = viewModel?.currentUserPhone?.value ?: "",
            customerName = viewModel?.currentUserName?.value?.ifBlank { "عميل" } ?: "عميل",
            customerArea = viewModel?.currentUserResidence?.value ?: "اليمن",
            price = product.price,
            quantity = qty,
            totalAmount = product.price * qty,
            status = "PENDING",
            notes = notes
        )
        if (viewModel != null && (uid.isBlank() || uid == "guest")) {
            pendingOrderPayload = order
            showGuestAuthDialog = true
        } else if (viewModel != null) {
            viewModel.placeOrder(order)
            Toast.makeText(context, "✅ تم إرسال طلبك لـ ${product.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "✅ تم تجهيز طلب ${product.name}", Toast.LENGTH_SHORT).show()
        }
    }

    val dispatchBooking: (BookingEntity) -> Unit = { booking ->
        val uid = viewModel?.currentUserId?.value ?: ""
        if (viewModel != null && (uid.isBlank() || uid == "guest")) {
            pendingBookingPayload = booking
            showGuestAuthDialog = true
        } else if (viewModel != null) {
            val enriched = booking.copy(
                clientUserId = uid,
                clientId = uid,
                customerName = viewModel.currentUserName.value.ifBlank { booking.customerName.ifBlank { "عميل" } },
                customerPhone = viewModel.currentUserPhone.value.ifBlank { booking.customerPhone },
                customerArea = viewModel.currentUserResidence.value.ifBlank { booking.customerArea.ifBlank { "اليمن" } }
            )
            viewModel.createBooking(enriched) { ok ->
                if (ok) {
                    Toast.makeText(context, "✅ تم تسجيل حجزك بنجاح!", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            onRequestBooking()
        }
    }

    when (entityType) {
        ProfileEntityType.STORE -> {
            StoreTabContent(
                selectedTab = selectedTab,
                store = store,
                products = products,
                offers = offers,
                entityReviews = entityReviews,
                themeColors = themeColors,
                isOwner = isOwner,
                onAddReviewClick = onAddReviewClick,
                onOrderProduct = { productToOrder = it }
            )
        }
        ProfileEntityType.RESTAURANT -> {
            RestaurantTabContent(
                selectedTab = selectedTab,
                store = store,
                products = products,
                offers = offers,
                entityReviews = entityReviews,
                themeColors = themeColors,
                isOwner = isOwner,
                onAddReviewClick = onAddReviewClick,
                onOrderProduct = { productToOrder = it },
                onSubmitBooking = dispatchBooking
            )
        }
        ProfileEntityType.MEDICAL -> {
            MedicalTabContent(
                selectedTab = selectedTab,
                store = store,
                provider = provider,
                products = products,
                doctors = doctors,
                entityReviews = entityReviews,
                themeColors = themeColors,
                isOwner = isOwner,
                viewModel = viewModel,
                onAddReviewClick = onAddReviewClick,
                onSwitchTab = onSwitchTab,
                onOrderProduct = { productToOrder = it },
                onSubmitBooking = dispatchBooking
            )
        }
        ProfileEntityType.REAL_ESTATE -> {
            RealEstateTabContent(
                selectedTab = selectedTab,
                property = property
            )
        }
        ProfileEntityType.JOB -> {
            JobTabContent(
                selectedTab = selectedTab,
                job = job,
                viewModel = viewModel,
                onRequireGuestAuth = { showGuestAuthDialog = true }
            )
        }
        ProfileEntityType.TECHNICIAN -> {
            TechnicianTabContent(
                selectedTab = selectedTab,
                provider = provider,
                products = products,
                galleryAlbums = galleryAlbums,
                entityReviews = entityReviews,
                themeColors = themeColors,
                isOwner = isOwner,
                onAddReviewClick = onAddReviewClick,
                onOrderProduct = { productToOrder = it },
                onRequestBooking = onRequestBooking
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

    // Order Confirmation Dialog
    val targetProd = productToOrder
    if (targetProd != null) {
        var quantity by remember(targetProd.id) { mutableIntStateOf(1) }
        var orderNotes by remember(targetProd.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { productToOrder = null },
            containerColor = themeColors.surface,
            title = {
                Text(
                    "🛍️ تأكيد طلب: ${targetProd.name}",
                    color = themeColors.accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "السعر للوحدة: ${targetProd.price.toInt()} ر.ي",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("الكمية:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        FilledTonalIconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("$quantity", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        FilledTonalIconButton(
                            onClick = { quantity++ },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        "الإجمالي: ${(targetProd.price * quantity).toInt()} ر.ي",
                        color = Color(0xFF10B981),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    OutlinedTextField(
                        value = orderNotes,
                        onValueChange = { orderNotes = it },
                        label = { Text("ملاحظات إضافية أو عنوان التوصيل") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val prod = targetProd
                        productToOrder = null
                        dispatchOrder(prod, quantity, orderNotes.trim())
                    },
                    enabled = targetProd.isAvailable,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        if (targetProd.isAvailable) "تأكيد الطلب 🚀" else "غير متاح",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { productToOrder = null },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    if (showGuestAuthDialog && viewModel != null) {
        GuestRegistrationDialog(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = {
                showGuestAuthDialog = false
                pendingOrderPayload = null
                pendingBookingPayload = null
            },
            onRegisterCompleted = { name, phone, residence, _ ->
                showGuestAuthDialog = false
                pendingOrderPayload?.let { pending ->
                    viewModel.placeOrder(
                        pending.copy(
                            customerName = name.ifBlank { pending.customerName },
                            customerPhone = phone.ifBlank { pending.customerPhone },
                            customerArea = residence.ifBlank { pending.customerArea }
                        )
                    )
                    pendingOrderPayload = null
                    Toast.makeText(context, "✅ تم إرسال طلبك بنجاح!", Toast.LENGTH_SHORT).show()
                }
                pendingBookingPayload?.let { pendingBk ->
                    val uid = viewModel.currentUserId.value
                    viewModel.createBooking(
                        pendingBk.copy(
                            clientUserId = uid,
                            clientId = uid,
                            customerName = name.ifBlank { pendingBk.customerName },
                            customerPhone = phone.ifBlank { pendingBk.customerPhone },
                            customerArea = residence.ifBlank { pendingBk.customerArea }
                        )
                    )
                    pendingBookingPayload = null
                    Toast.makeText(context, "✅ تم تسجيل حجزك بنجاح!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

/* ══════════════════════════════════════════════════════════════════════
   1. STORE TAB CONTENT
══════════════════════════════════════════════════════════════════════ */
@Composable
private fun StoreTabContent(
    selectedTab: Int,
    store: StoreEntity?,
    products: List<ProductEntity>,
    offers: List<SpecialOfferEntity>,
    entityReviews: List<RatingEntity>,
    themeColors: VisualThemePalette,
    isOwner: Boolean,
    onAddReviewClick: () -> Unit,
    onOrderProduct: (ProductEntity) -> Unit
) {
    val storeId = store?.id ?: ""
    val storeProducts = remember(products, storeId) {
        products.filter { !it.isDeleted && (it.storeId == storeId || it.ownerId == storeId) }
    }

    when (selectedTab) {
        0 -> { // الرئيسية
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val activeOffer = offers.firstOrNull()
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
                                Text(
                                    text = if (activeOffer != null) "عرض خاص 🎁" else "متجر معتمد 🛍️",
                                    color = Color(0xFF1E1B4B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            if (activeOffer != null) {
                                Text(
                                    text = activeOffer.title.ifBlank { "خصم ${activeOffer.discountPercent}%" },
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = activeOffer.description.ifBlank {
                                        if (activeOffer.couponCode.isNotBlank()) "استخدم الكود: ${activeOffer.couponCode}" else "عرض متاح لفترة محدودة"
                                    },
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            } else {
                                Text(
                                    text = "أهلاً بكم في ${store?.name ?: "المتجر"}",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = store?.description?.ifBlank { "منتجات أصلية وطلبات مباشرة عبر التطبيق" }
                                        ?: "منتجات أصلية وطلبات مباشرة عبر التطبيق",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                // Stats strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatPill(modifier = Modifier.weight(1f), icon = "📦", title = "${storeProducts.size}", subtitle = "صنف متاح")
                    StatPill(modifier = Modifier.weight(1f), icon = "⭐", title = "%.1f".format(store?.rating ?: 5.0f), subtitle = "تقييم المتجر")
                    StatPill(
                        modifier = Modifier.weight(1f),
                        icon = "🕒",
                        title = if (store?.isActive != false) "مفتوح" else "مغلق",
                        subtitle = store?.workingHours?.ifBlank { "يومياً" } ?: "يومياً"
                    )
                }

                // Best sellers preview
                Text("أبرز المنتجات 🌟", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (storeProducts.isEmpty()) {
                    EmptyStateBox("لا توجد منتجات مضافة لهذا المتجر حتى الآن.", themeColors)
                } else {
                    storeProducts.take(4).chunked(2).forEach { rowPair ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowPair.forEach { prod ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ProfileProductCard(
                                        product = prod,
                                        themeColors = themeColors,
                                        onAddToCartClick = { onOrderProduct(prod) },
                                        onClick = { onOrderProduct(prod) }
                                    )
                                }
                            }
                            if (rowPair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        1 -> StoreProductsGrid(storeProducts = storeProducts, themeColors = themeColors, onOrderProduct = onOrderProduct)
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
                                    Text(prod.description.ifEmpty { "صنف معتمد من المتجر" }, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 1)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${prod.price.toInt()} ر.ي", color = Color(0xFFFFD166), fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                                Button(
                                    onClick = { onOrderProduct(prod) },
                                    enabled = prod.isAvailable,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(if (prod.isAvailable) "طلب" else "نفذ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                if (offers.isEmpty()) {
                    EmptyStateBox("لا توجد عروض أو كوبونات مضافة حالياً.", themeColors)
                } else {
                    offers.forEach { offer ->
                        CouponCard(offer = offer)
                    }
                }
            }
        }
        4 -> ReviewsSection(entityReviews = entityReviews, isOwner = isOwner, themeColors = themeColors, onAddReviewClick = onAddReviewClick)
        5 -> { // سياسة المتجر
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("سياسات المتجر والضمان 🛡️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("🔄 سياسة الاستبدال والاسترجاع", "تخضع لطبيعة الصنف وبحسب اتفاق العميل مع المتجر عند الاستلام.")
                PolicyCard("🚚 التوصيل والتجهيز", "يتم تنسيق موعد التوصيل مباشرة بعد تأكيد الطلب من لوحة التاجر.")
                PolicyCard("💳 طرق الدفع المعتمدة", "الدفع عند الاستلام أو عبر التحويل/المحافظ الإلكترونية المتفق عليها.")
            }
        }
        6 -> { // الفروع
            val branchAddress = listOfNotNull(
                store?.cityId?.ifBlank { null },
                store?.localNeighborhood?.ifBlank { null }
            ).joinToString(" - ").ifBlank { "اليمن" }
            val hoursText = store?.workingHours?.ifBlank { "9:00 ص - 10:00 م" } ?: "9:00 ص - 10:00 م"
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("فروع المتجر المعتمدة 📍", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                BranchCard(name = store?.name ?: "الفرع الرئيسي", address = branchAddress, hours = hoursText)
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════════════
   2. RESTAURANT TAB CONTENT
══════════════════════════════════════════════════════════════════════ */
@Composable
private fun RestaurantTabContent(
    selectedTab: Int,
    store: StoreEntity?,
    products: List<ProductEntity>,
    offers: List<SpecialOfferEntity>,
    entityReviews: List<RatingEntity>,
    themeColors: VisualThemePalette,
    isOwner: Boolean,
    onAddReviewClick: () -> Unit,
    onOrderProduct: (ProductEntity) -> Unit,
    onSubmitBooking: (BookingEntity) -> Unit
) {
    val storeId = store?.id ?: ""
    val storeProducts = remember(products, storeId) {
        products.filter { !it.isDeleted && (it.storeId == storeId || it.ownerId == storeId) }
    }

    when (selectedTab) {
        0 -> StoreProductsGrid(storeProducts = storeProducts, themeColors = themeColors, onOrderProduct = onOrderProduct)
        1 -> { // الأكثر طلباً
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("الأطباق المميزة والموصى بها 🥇", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                val topDishes = storeProducts.take(4)
                if (topDishes.isEmpty()) {
                    EmptyStateBox("لم تتم إضافة أطباق في القائمة بعد.", themeColors)
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
                                    Text(dish.description.ifBlank { "وجبة طازجة من قائمة المطعم" }, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 1)
                                    Text("${dish.price.toInt()} ر.ي", color = Color(0xFFFF5A5F), fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                                Button(
                                    onClick = { onOrderProduct(dish) },
                                    enabled = dish.isAvailable,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text(if (dish.isAvailable) "أضف" else "غير متاح", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                if (offers.isEmpty()) {
                    EmptyStateBox("لا توجد عروض وجبات مضافة حالياً.", themeColors)
                } else {
                    offers.forEach { offer ->
                        DealCard(offer = offer)
                    }
                }
            }
        }
        3 -> ReviewsSection(entityReviews = entityReviews, isOwner = isOwner, themeColors = themeColors, onAddReviewClick = onAddReviewClick)
        4 -> { // المعلومات
            val locationText = listOfNotNull(
                store?.cityId?.ifBlank { null },
                store?.localNeighborhood?.ifBlank { null }
            ).joinToString(" - ").ifBlank { "اليمن" }
            val hoursText = store?.workingHours?.ifBlank { "10:00 ص - 12:00 منتصف الليل" } ?: "10:00 ص - 12:00 منتصف الليل"
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("معلومات وتفاصيل المطعم ℹ️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                InfoRowItem(icon = "📍", title = "الموقع والحي", desc = locationText)
                InfoRowItem(icon = "🕒", title = "أوقات العمل اليومية", desc = hoursText)
                InfoRowItem(icon = "📝", title = "نبذة", desc = store?.description?.ifBlank { "مأكولات وضيافة متميزة" } ?: "مأكولات وضيافة متميزة")
            }
        }
        5 -> ReservationCard(store = store, onSubmitBooking = onSubmitBooking)
    }
}

/* ══════════════════════════════════════════════════════════════════════
   3. MEDICAL TAB CONTENT
══════════════════════════════════════════════════════════════════════ */
@Composable
private fun MedicalTabContent(
    selectedTab: Int,
    store: StoreEntity?,
    provider: ProviderEntity?,
    products: List<ProductEntity>,
    doctors: List<DoctorItem>,
    entityReviews: List<RatingEntity>,
    themeColors: VisualThemePalette,
    isOwner: Boolean,
    viewModel: MainViewModel?,
    onAddReviewClick: () -> Unit,
    onSwitchTab: (Int) -> Unit,
    onOrderProduct: (ProductEntity) -> Unit,
    onSubmitBooking: (BookingEntity) -> Unit
) {
    val context = LocalContext.current
    val medId = store?.id ?: provider?.id ?: ""
    val medName = store?.name ?: provider?.name ?: "المركز الطبي"
    val medPhone = store?.phone ?: provider?.phone ?: ""
    val medProducts = remember(products, medId) {
        products.filter { !it.isDeleted && (it.storeId == medId || it.ownerId == medId) }
    }
    var selectedDoctor by remember { mutableStateOf<DoctorItem?>(null) }

    when (selectedTab) {
        0 -> { // الرئيسية
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Verified, contentDescription = "منشأة معتمدة", tint = Color(0xFF0F172A))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(medName, color = Color(0xFF2DD4BF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                store?.description?.ifBlank { "خدمات طبية واستشارية متكاملة" }
                                    ?: provider?.specialization?.ifBlank { "خدمات طبية واستشارية متكاملة" }
                                    ?: "خدمات طبية واستشارية متكاملة",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MedicalQuickAction(modifier = Modifier.weight(1f), icon = "🚑", title = "طوارئ واتصال", color = Color(0xFFEF4444)) {
                        if (medPhone.isNotBlank()) PhoneDialerHelper.dial(context, medPhone) else onSwitchTab(3)
                    }
                    MedicalQuickAction(modifier = Modifier.weight(1f), icon = "🩺", title = "حجز كشف", color = Color(0xFF2DD4BF)) {
                        onSwitchTab(3)
                    }
                    MedicalQuickAction(modifier = Modifier.weight(1f), icon = "🧪", title = "المختبر والفحوص", color = Color(0xFF38BDF8)) {
                        onSwitchTab(4)
                    }
                }
            }
        }
        1 -> { // الأطباء
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("نخبة الأطباء والاستشاريين 👨‍⚕️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (doctors.isEmpty()) {
                    UnifiedEmptyState(
                        title = "لا يوجد أطباء مسجلون حالياً",
                        description = "يمكن للمنشأة الطبية إضافة كادرها الطبي والاستشاريين من لوحة التحكم.",
                        iconText = "👨‍⚕️",
                        actionLabel = if (isOwner && viewModel != null) "أضف طبيباً ➕" else null,
                        onActionClick = {
                            if (viewModel != null) {
                                viewModel.selectedStore = store
                                viewModel.selectedProvider = provider
                                viewModel.selectedDashboardInitialTab = 0
                                viewModel.navigateTo(AppScreens.MEDICAL_DASHBOARD)
                            }
                        },
                        themeColors = themeColors
                    )
                } else {
                    doctors.forEach { doctor ->
                        DoctorCard(
                            doctor = doctor,
                            onBookClick = {
                                selectedDoctor = doctor
                                onSwitchTab(3)
                            }
                        )
                    }
                }
            }
        }
        2 -> { // الأقسام والتخصصات
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("الأقسام والعيادات المتوفرة 🏥", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                val derivedSpecialties = remember(doctors) {
                    val docSpecs = doctors.map { it.specialty.trim() }.filter { it.isNotBlank() }.distinct()
                    docSpecs.ifEmpty {
                        listOf("الباطنية والقلب 🫀", "طب الأطفال 👶", "الأسنان 🦷", "المختبر والأشعة 🧪")
                    }
                }
                derivedSpecialties.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEach { spec ->
                            Surface(
                                onClick = { onSwitchTab(3) },
                                shape = RoundedCornerShape(12.dp),
                                color = BespokePremiumSystem.SurfaceDark,
                                border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                    Text(
                                        text = spec,
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        3 -> { // الحجز
            AppointmentBookingFlow(
                medicalId = medId,
                medicalName = medName,
                medicalPhone = medPhone,
                selectedDoctor = selectedDoctor,
                onClearDoctor = { selectedDoctor = null },
                onSubmitBooking = onSubmitBooking
            )
        }
        4 -> { // المختبر والأشعة والخدمات الطبية
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("باقات الفحوصات والخدمات الطبية 🧪", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (medProducts.isEmpty()) {
                    UnifiedEmptyState(
                        title = "لا توجد باقات مخبرية محددة حالياً",
                        description = "تواصل مع المركز أو احجز كشفاً مباشراً للاستفسار عن الفحوصات المتاحة.",
                        iconText = "🧪",
                        actionLabel = "احجز موعداً الآن 📅",
                        onActionClick = { onSwitchTab(3) },
                        themeColors = themeColors
                    )
                } else {
                    medProducts.forEach { prod ->
                        LabPackageCard(
                            title = prod.name,
                            tests = prod.description.ifBlank { prod.category.ifBlank { "فحص طبي متكامل" } },
                            price = "${prod.price.toInt()} ر.ي",
                            isAvailable = prod.isAvailable,
                            onOrderClick = { onOrderProduct(prod) }
                        )
                    }
                }
            }
        }
        5 -> ReviewsSection(entityReviews = entityReviews, isOwner = isOwner, themeColors = themeColors, onAddReviewClick = onAddReviewClick)
        6 -> { // عن المركز
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("عن المركز الطبي ℹ️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("🏥 نبذة عن المنشأة", store?.description?.ifBlank { "منشأة طبية متكاملة" } ?: "منشأة طبية متكاملة")
                PolicyCard("🕒 أوقات الدوام", store?.workingHours?.ifBlank { "يومياً من 8 ص حتى 10 م" } ?: "يومياً من 8 ص حتى 10 م")
            }
        }
        7 -> { // الفروع
            val address = listOfNotNull(
                (store?.cityId ?: provider?.cityId)?.ifBlank { null },
                (store?.localNeighborhood ?: provider?.localNeighborhood)?.ifBlank { null }
            ).joinToString(" - ").ifBlank { "اليمن" }
            val hours = store?.workingHours?.ifBlank { "مفتوح حسب جدول العيادات" } ?: "مفتوح حسب جدول العيادات"
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("المستشفى / المركز الرئيسي 📍", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                BranchCard(name = medName, address = address, hours = hours)
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
    property: PropertyEntity?
) {
    val context = LocalContext.current
    when (selectedTab) {
        0 -> { // نظرة عامة
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("نظرة عامة على العقار 🏠", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if ((property?.images?.size ?: 0) > 1) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(property?.images.orEmpty()) { img ->
                            Card(modifier = Modifier.size(width = 160.dp, height = 110.dp), shape = RoundedCornerShape(12.dp)) {
                                SmartAsyncImage(model = img, contentDescription = property?.title ?: "صورة عقار", modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BespokePremiumSystem.SurfaceDark,
                    border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(property?.title ?: "عقار متميز", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(property?.description?.ifEmpty { "لا يوجد وصف إضافي" } ?: "", color = Color(0xFF94A3B8), fontSize = 12.sp)
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
            val city = property?.cityId?.ifBlank { "اليمن" } ?: "اليمن"
            val neighborhood = property?.localNeighborhood?.ifBlank { "غير محدد" } ?: "غير محدد"
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("موقع العقار المسجل 📍", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                InfoRowItem(icon = "🏙️", title = "المدينة / المحافظة", desc = city)
                InfoRowItem(icon = "📍", title = "الحي والمنطقة", desc = neighborhood)
                InfoRowItem(icon = "🏷️", title = "نوع العرض", desc = property?.type?.ifBlank { "للبيع / الإيجار" } ?: "للبيع / الإيجار")
            }
        }
        2 -> { // المواصفات
            val areaText = if ((property?.areaSqm ?: 0.0) > 0) "${property?.areaSqm?.toInt()} متر مربع" else "غير محددة"
            val roomsText = if ((property?.rooms ?: 0) > 0) "${property?.rooms} غرف" else "غير محدد"
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoRowItem(icon = "📐", title = "المساحة الإجمالية", desc = areaText)
                InfoRowItem(icon = "🛏️", title = "عدد الغرف", desc = roomsText)
                InfoRowItem(icon = "🏢", title = "نوع وتصنيف العقار", desc = property?.propertyType?.ifEmpty { "عقار" } ?: "عقار")
                InfoRowItem(icon = "🤝", title = "صفة الإعلان", desc = property?.type?.ifEmpty { "عرض عقاري" } ?: "عرض عقاري")
            }
        }
        3 -> { // التقسيط والدفع
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("آلية الدفع والتعاقد 💳", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("💵 قيمة العقار", "السعر المطلوب: ${property?.price?.toInt() ?: 0} ${property?.currency ?: "ر.ي"} (قابل للتفاوض حسب الاتفاق مع المعلن).")
                PolicyCard("📝 المعاينة والتعاقد", "يفضل معاينة العقار ميدانياً والتأكد من الوثائق قبل إتمام أي دفعات مالية.")
            }
        }
        4 -> { // التواصل
            val rawPhone = property?.phone.orEmpty()
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("تواصل مع المعلن / المكتب العقاري 📞", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BespokePremiumSystem.SurfaceDark,
                    border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "المعلن: ${property?.ownerName?.ifBlank { rawPhone.ifBlank { "مكتب العقارات" } } ?: "مكتب العقارات"}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (rawPhone.isNotBlank()) {
                            Text("الهاتف: $rawPhone", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                val digits = rawPhone.filter { it.isDigit() }
                                if (digits.isNotBlank()) {
                                    val intl = when {
                                        digits.startsWith("967") -> digits
                                        digits.startsWith("0") -> "967" + digits.drop(1)
                                        else -> "967$digits"
                                    }
                                    val msg = Uri.encode("مرحباً، أستفسر عن العقار: ${property?.title ?: ""}")
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$intl?text=$msg")))
                                    } catch (_: Exception) {
                                        PhoneDialerHelper.dial(context, rawPhone)
                                    }
                                } else {
                                    Toast.makeText(context, "لا يوجد رقم هاتف مسجل لهذا العقار", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
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
    viewModel: MainViewModel?,
    onRequireGuestAuth: () -> Unit
) {
    val context = LocalContext.current
    var qualsInput by remember { mutableStateOf("") }
    var alreadyApplied by remember(job?.id) { mutableStateOf(false) }

    when (selectedTab) {
        0 -> { // تفاصيل الوظيفة
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("تفاصيل الشاغر الوظيفي 💼", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                InfoRowItem(icon = "🏢", title = "الجهة المعلنة", desc = job?.companyName?.ifEmpty { "شركة رائدة" } ?: "")
                InfoRowItem(icon = "📍", title = "مقر العمل", desc = listOfNotNull(job?.cityId?.ifBlank { null }, job?.address?.ifBlank { null }).joinToString(" - ").ifBlank { "اليمن" })
                InfoRowItem(icon = "⏱️", title = "نوع الدوام", desc = job?.jobType?.ifEmpty { "دوام كامل" } ?: "")
            }
        }
        1 -> { // المتطلبات
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("المؤهلات والشروط المطلوبة 🎓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("🎓 المؤهلات المطلوبة", job?.requirements?.ifEmpty { "تحدد وفق الوصف الوظيفي." } ?: "")
            }
        }
        2 -> { // الرواتب والمزايا
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("الراتب والمزايا 🎁", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                InfoRowItem(icon = "💰", title = "الراتب الشهري", desc = if (job?.salary.isNullOrBlank()) "يحدد بعد المقابلة" else job?.salary ?: "")
            }
        }
        3 -> { // عن الشركة
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("نبذة عن جهة العمل والوظيفة 🏢", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("🌟 التفاصيل", job?.description?.ifBlank { "فرصة عمل متاحة للتقديم المباشر." } ?: "فرصة عمل متاحة للتقديم المباشر.")
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
                    OutlinedTextField(
                        value = qualsInput,
                        onValueChange = { qualsInput = it },
                        label = { Text("اذكر مؤهلك العلمي وخبراتك باختصار") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Button(
                        onClick = {
                            if (job == null) return@Button
                            val uid = viewModel?.currentUserId?.value ?: ""
                            if (viewModel != null && (uid.isBlank() || uid == "guest")) {
                                onRequireGuestAuth()
                            } else if (qualsInput.isBlank()) {
                                Toast.makeText(context, "يرجى كتابة نبذة عن مؤهلاتك وخبراتك", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel?.submitJobApplication(
                                    JobApplicationEntity(
                                        id = EntityIdGenerator.generate("APP"),
                                        jobId = job.id,
                                        jobTitle = job.title,
                                        companyName = job.companyName,
                                        applicantName = viewModel.currentUserName.value.ifBlank { "متقدم" },
                                        applicantPhone = viewModel.currentUserPhone.value,
                                        applicantQuals = qualsInput.trim(),
                                        status = "PENDING",
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                                alreadyApplied = true
                                qualsInput = ""
                                Toast.makeText(context, "✅ تم إرسال طلبك لشركة ${job.companyName} بنجاح!", Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !alreadyApplied,
                        colors = ButtonDefaults.buttonColors(containerColor = if (alreadyApplied) Color(0xFF10B981) else Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text(
                            if (alreadyApplied) "تم إرسال طلبك بنجاح ✅" else "إرسال طلب التوظيف الآن 🚀",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
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
    products: List<ProductEntity>,
    galleryAlbums: List<GalleryAlbumEntity>,
    entityReviews: List<RatingEntity>,
    themeColors: VisualThemePalette,
    isOwner: Boolean,
    onAddReviewClick: () -> Unit,
    onOrderProduct: (ProductEntity) -> Unit,
    onRequestBooking: () -> Unit
) {
    val providerId = provider?.id ?: ""
    val techProducts = remember(products, providerId) {
        products.filter { !it.isDeleted && (it.storeId == providerId || it.ownerId == providerId) }
    }

    when (selectedTab) {
        0 -> { // سابقة الأعمال
            val photos = remember(provider, galleryAlbums) {
                ((provider?.workPhotosBase64 ?: emptyList()) + galleryAlbums.flatMap { it.imageUrls })
                    .filter { it.isNotBlank() }
                    .distinct()
            }
            if (photos.isEmpty()) {
                EmptyStateBox("لا توجد صور سابقة أعمال مرفوعة حالياً.", themeColors)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photos) { photo ->
                        Card(modifier = Modifier.size(130.dp), shape = RoundedCornerShape(12.dp)) {
                            SmartAsyncImage(model = photo, contentDescription = "عمل سابق", modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
        1 -> { // الخدمات والتسعير
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("الخدمات والتسعير 🛠️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                InfoRowItem(
                    icon = "⚡",
                    title = "الكشف والمعاينة الميدانية",
                    desc = if ((provider?.previewPrice ?: 0.0) > 0) "${provider?.previewPrice?.toInt()} ر.ي" else "يحدد حسب الاتفاق"
                )
                if (techProducts.isEmpty()) {
                    InfoRowItem(icon = "🔧", title = "تخصص الفني", desc = provider?.specialization?.ifBlank { provider.profession } ?: "خدمات فنية متخصصة")
                    Button(
                        onClick = onRequestBooking,
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text("طلب معاينة / حجز خدمة 📅", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    techProducts.forEach { service ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = BespokePremiumSystem.SurfaceDark,
                            border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(service.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    if (service.description.isNotBlank()) {
                                        Text(service.description, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 2)
                                    }
                                    Text("${service.price.toInt()} ر.ي", color = themeColors.accent, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                                Button(
                                    onClick = { onOrderProduct(service) },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text("طلب الخدمة", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        2 -> ReviewsSection(entityReviews = entityReviews, isOwner = isOwner, themeColors = themeColors, onAddReviewClick = onAddReviewClick)
        3 -> { // شروط الضمان
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ميثاق الجودة وحماية العميل 🛡️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                PolicyCard("🛡️ ميثاق الجودة المعتمد", "يلتزم الفني بتقديم خدمة متقنة ومناقشة التكلفة النهائية بشفافية قبل بدء الإصلاح.")
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
    themeColors: VisualThemePalette,
    onOrderProduct: (ProductEntity) -> Unit
) {
    var selectedSubCategory by remember { mutableStateOf("الكل") }
    val subCats = remember(storeProducts) {
        val list = mutableListOf("الكل")
        storeProducts.forEach { p ->
            if (p.category.isNotBlank() && !list.contains(p.category)) list.add(p.category)
        }
        list
    }

    val filteredProducts = remember(storeProducts, selectedSubCategory) {
        if (selectedSubCategory == "الكل") storeProducts
        else storeProducts.filter { it.category == selectedSubCategory }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (subCats.size > 1) {
            Text("الأقسام والتصنيفات 🏷️", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(subCats) { catName ->
                    val isSelected = selectedSubCategory == catName
                    Surface(
                        onClick = { selectedSubCategory = catName },
                        shape = RoundedCornerShape(999.dp),
                        color = if (isSelected) Color(0xFF8B5CF6) else BespokePremiumSystem.SurfaceDark,
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else BespokePremiumSystem.GlassBorder),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(
                                catName,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
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
                            Box(modifier = Modifier.weight(1f)) {
                                ProfileProductCard(
                                    product = prod,
                                    themeColors = themeColors,
                                    onAddToCartClick = { onOrderProduct(prod) },
                                    onClick = { onOrderProduct(prod) }
                                )
                            }
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
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
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
            Text(subtitle, color = Color(0xFF94A3B8), fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun CouponCard(offer: SpecialOfferEntity) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val codeText = offer.couponCode.ifBlank { offer.title.take(10) }
    val discountLabel = if (offer.discountPercent > 0) "خصم ${offer.discountPercent}%" else offer.title

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
                    Text(discountLabel, color = Color(0xFF8B5CF6), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(offer.title, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                if (offer.description.isNotBlank()) {
                    Text(offer.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
            if (offer.couponCode.isNotBlank()) {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(codeText))
                        Toast.makeText(context, "✅ تم نسخ الكود: $codeText", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(codeText, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DealCard(offer: SpecialOfferEntity) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val codeToCopy = offer.couponCode
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
                Text(offer.title.ifBlank { "خصم ${offer.discountPercent}%" }, color = Color(0xFFFF5A5F), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (offer.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(offer.description, color = Color.White, fontSize = 11.5.sp)
                }
            }
            if (codeToCopy.isNotBlank()) {
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(codeToCopy))
                        Toast.makeText(context, "✅ تم نسخ كود العرض: $codeToCopy", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(codeToCopy, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
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
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(address, color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text(hours, color = BespokePremiumSystem.EmeraldAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
            Icon(Icons.Default.LocationOn, contentDescription = "موقع الفرع", tint = BespokePremiumSystem.EmeraldAccent)
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
private fun DoctorCard(doctor: DoctorItem, onBookClick: () -> Unit) {
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
                Text(doctor.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(doctor.specialty.ifBlank { "طبيب عام" }, color = Color(0xFF2DD4BF), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                if (doctor.workingHours.isNotBlank()) {
                    Text("🕒 ${doctor.workingHours}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                }
            }
            Button(
                onClick = onBookClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.heightIn(min = 48.dp)
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
        modifier = modifier.heightIn(min = 48.dp),
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
private fun LabPackageCard(
    title: String,
    tests: String,
    price: String,
    isAvailable: Boolean,
    onOrderClick: () -> Unit
) {
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
                onClick = onOrderClick,
                enabled = isAvailable,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(if (isAvailable) "طلب فحص" else "غير متاح", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AppointmentBookingFlow(
    medicalId: String,
    medicalName: String,
    medicalPhone: String,
    selectedDoctor: DoctorItem?,
    onClearDoctor: () -> Unit,
    onSubmitBooking: (BookingEntity) -> Unit
) {
    var selectedDay by remember { mutableStateOf("اليوم") }
    var selectedSlot by remember { mutableStateOf("05:00 م") }
    var patientNotes by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BespokePremiumSystem.SurfaceDark,
        border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("حجز موعد كشف فوري في $medicalName 📅", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            if (selectedDoctor != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F766E).copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "الطبيب المختار: ${selectedDoctor.name} (${selectedDoctor.specialty})",
                            color = Color(0xFF2DD4BF),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearDoctor, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "إزالة اختيار الطبيب", tint = Color.White)
                        }
                    }
                }
            }

            Text("1. اختر موعد الحضور:", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("اليوم", "غداً", "بعد غد").forEach { day ->
                    val isSel = selectedDay == day
                    Surface(
                        onClick = { selectedDay = day },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) Color(0xFF2DD4BF) else Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(day, color = if (isSel) Color(0xFF0F172A) else Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text("2. اختر الوقت المناسب:", color = Color(0xFF94A3B8), fontSize = 11.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("04:30 م", "05:00 م", "06:30 م", "07:00 م")) { slot ->
                    val isSel = selectedSlot == slot
                    Surface(
                        onClick = { selectedSlot = slot },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) Color(0xFF2DD4BF) else Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(slot, color = if (isSel) Color(0xFF0F172A) else Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = patientNotes,
                onValueChange = { patientNotes = it },
                label = { Text("سبب الزيارة أو الأعراض (اختياري)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Button(
                onClick = {
                    val details = buildString {
                        append("حجز موعد طبي")
                        if (selectedDoctor != null) append(" مع ${selectedDoctor.name}")
                        if (patientNotes.isNotBlank()) append(" — ${patientNotes.trim()}")
                    }
                    onSubmitBooking(
                        BookingEntity(
                            id = "",
                            serviceType = "حجز موعد طبي",
                            providerId = medicalId,
                            providerName = medicalName,
                            providerPhone = medicalPhone,
                            serviceDetails = details,
                            date = selectedDay,
                            time = selectedSlot,
                            status = "PENDING",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("تأكيد حجز الموعد الطبي الآن ✅", color = Color(0xFF0F172A), fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ReservationCard(
    store: StoreEntity?,
    onSubmitBooking: (BookingEntity) -> Unit
) {
    var guests by remember { mutableIntStateOf(2) }
    var selectedTime by remember { mutableStateOf("08:00 م") }
    var tableNotes by remember { mutableStateOf("") }
    val storeName = store?.name ?: "المطعم"

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
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("-", fontSize = 18.sp, color = Color.White)
                }
                Text("$guests أشخاص", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Button(
                    onClick = { guests++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("+", fontSize = 18.sp, color = Color.White)
                }
            }

            Text("الوقت المفضل:", color = Color(0xFF94A3B8), fontSize = 11.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("01:00 م", "07:00 م", "08:00 م", "09:30 م")) { timeOption ->
                    val isSel = selectedTime == timeOption
                    Surface(
                        onClick = { selectedTime = timeOption },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) Color(0xFFFF5A5F) else Color.White.copy(alpha = 0.05f),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(timeOption, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = tableNotes,
                onValueChange = { tableNotes = it },
                label = { Text("ملاحظات (جلسة عائلية / مناسبة خاصة)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Button(
                onClick = {
                    val details = buildString {
                        append("حجز طاولة لـ $guests أفراد")
                        if (tableNotes.isNotBlank()) append(" — ${tableNotes.trim()}")
                    }
                    onSubmitBooking(
                        BookingEntity(
                            id = "",
                            serviceType = "حجز طاولة مطعم",
                            providerId = store?.id ?: "",
                            providerName = storeName,
                            providerPhone = store?.phone ?: "",
                            serviceDetails = details,
                            date = "اليوم",
                            time = selectedTime,
                            status = "PENDING",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5A5F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text("تأكيد حجز الطاولة 🍽️", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

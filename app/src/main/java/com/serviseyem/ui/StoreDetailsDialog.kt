package com.serviseyem

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.ProductAttachmentsSection
import com.serviseyem.ui.ProductListItemCard
import com.serviseyem.ui.SpecialOffersSection
import com.serviseyem.ui.components.ReviewInput
import com.serviseyem.utils.VisualThemePalette

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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 840.dp)
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp)
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
                            .statusBarsPadding()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isOwnerOrAdmin) {
                            Button(
                                onClick = { coverPickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.65f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.heightIn(min = 44.dp)
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
                                .size(44.dp)
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
                                        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)
                                    ) {
                                        Text("📝 البيانات", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showAddProductDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)
                                    ) {
                                        Text("➕ إضافة منتج", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showBulkPriceDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)
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
                                                .size(44.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(18.dp))
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
                        Text(
                            sectionTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent,
                            modifier = Modifier.weight(1f).padding(end = 6.dp)
                        )

                        if (isOwnerOrAdmin) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    onClick = { showAddProductDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.heightIn(min = 44.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.heightIn(min = 44.dp).padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text("➕ إضافة ${if (isMedical) "خدمة" else if (isRestaurant) "وجبة" else "سلعة/خدمة"}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                Surface(
                                    onClick = { showBulkPriceDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier.heightIn(min = 44.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.heightIn(min = 44.dp).padding(horizontal = 8.dp, vertical = 6.dp)
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
}

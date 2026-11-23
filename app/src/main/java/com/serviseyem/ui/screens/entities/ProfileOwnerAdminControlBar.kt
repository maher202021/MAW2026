package com.serviseyem.ui.screens.entities

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.utils.VisualThemePalette

@Composable
fun ProfileOwnerAdminControlBar(
    entityId: String,
    entityType: ProfileEntityType,
    provider: ProviderEntity?,
    store: StoreEntity?,
    property: PropertyEntity?,
    job: JobEntity?,
    isOwner: Boolean,
    isAdmin: Boolean,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    if (!isOwner && !isAdmin) return

    val context = LocalContext.current
    var showEditDetailsDialog by remember { mutableStateOf(false) }
    var showChangePhotosDialog by remember { mutableStateOf(false) }
    var showPriceOfferDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val collectionName = when {
        provider != null -> "providers"
        store != null -> "stores"
        property != null -> "properties"
        job != null -> "jobs"
        else -> "providers"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(
            1.5.dp,
            if (isAdmin) Color(0xFFEAB308).copy(alpha = 0.6f) else themeColors.accent.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAdmin) Icons.Default.Settings else Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = if (isAdmin) Color(0xFFEAB308) else themeColors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAdmin) "👑 لوحة التحكم والإشراف الشامل للأدمن" else "🛠️ لوحة تحكم حسابك (${entityType.labelAr})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAdmin) Color(0xFFEAB308) else Color.White
                    )
                }
                if (isAdmin) {
                    Text(
                        text = "صلاحيات كاملة",
                        fontSize = 10.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Role-Specific Action Buttons
            when (entityType) {
                ProfileEntityType.TECHNICIAN -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateToScreen(AppScreens.BOOKINGS_VIEW) },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الحجوزات 📅", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.navigateToScreen(AppScreens.CHAT_LIST) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("المحادثات 💬", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showPriceOfferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الأسعار 🏷️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                ProfileEntityType.STORE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { showAddProductDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة سلعة 📦", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showPriceOfferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("العروض 🏷️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.navigateToScreen(AppScreens.ORDERS_VIEW) },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الطلبيات 🛒", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                ProfileEntityType.MEDICAL -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateToScreen(AppScreens.BOOKINGS_VIEW) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مواعيد المرضى 🩺", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showPriceOfferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("كشفية وخدمات 🏷️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.navigateToScreen(AppScreens.CHAT_LIST) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استشارات 💬", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                ProfileEntityType.RESTAURANT -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { showAddProductDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة وجبة 🍔", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { showPriceOfferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("عروض اليوم 🎁", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.navigateToScreen(AppScreens.ORDERS_VIEW) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الطلبات والطاولات 🛵", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                ProfileEntityType.REAL_ESTATE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { showPriceOfferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الأسعار والإيجار 🏠", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.navigateToScreen(AppScreens.CHAT_LIST) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(38.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استفسارات العملاء 💬", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {}
            }

            // Universal Edit Options (Avatar, Cover, Details)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { showChangePhotosDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp),
                    border = BorderStroke(1.dp, themeColors.accent)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = themeColors.accent)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الصورة والغلاف 🖼️", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showEditDetailsDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF3B82F6))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعديل البيانات 📝", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (isAdmin) {
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("حذف النشاط 🗑️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 1. DIALOG: Edit Photos (Avatar & Cover)
    if (showChangePhotosDialog) {
        Dialog(onDismissRequest = { showChangePhotosDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                var avatarUrl by remember {
                    mutableStateOf(
                        provider?.profileImage ?: store?.logoImage ?: ""
                    )
                }
                var coverUrl by remember {
                    mutableStateOf(
                        provider?.coverImage ?: store?.coverImage ?: property?.images?.firstOrNull() ?: ""
                    )
                }
                var isUploadingAvatar by remember { mutableStateOf(false) }
                var isUploadingCover by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                val avatarPicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri: android.net.Uri? ->
                    if (uri != null) {
                        coroutineScope.launch {
                            isUploadingAvatar = true
                            val path = "avatars/${System.currentTimeMillis()}_$entityId.webp"
                            val result = com.serviseyem.utils.FirebaseStorageUploader.uploadImageToStorage(context, uri, path)
                            isUploadingAvatar = false
                            result.onSuccess { url ->
                                avatarUrl = url
                                Toast.makeText(context, "تم رفع الصورة الشخصية بنجاح ✅", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "فشل الرفع: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                val coverPicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri: android.net.Uri? ->
                    if (uri != null) {
                        coroutineScope.launch {
                            isUploadingCover = true
                            val path = "covers/${System.currentTimeMillis()}_$entityId.webp"
                            val result = com.serviseyem.utils.FirebaseStorageUploader.uploadImageToStorage(context, uri, path)
                            isUploadingCover = false
                            result.onSuccess { url ->
                                coverUrl = url
                                Toast.makeText(context, "تم رفع صورة الغلاف بنجاح ✅", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "فشل الرفع: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📸 تغيير الصورة الشخصية وصورة الغلاف", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = avatarUrl,
                            onValueChange = { avatarUrl = it },
                            label = { Text("الصورة الشخصية / الشعار", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Button(
                            onClick = { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            enabled = !isUploadingAvatar,
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(54.dp)
                        ) {
                            if (isUploadingAvatar) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                            } else {
                                Text("اختر 📷", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = coverUrl,
                            onValueChange = { coverUrl = it },
                            label = { Text("صورة الغلاف (Banner)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Button(
                            onClick = { coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            enabled = !isUploadingCover,
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(54.dp)
                        ) {
                            if (isUploadingCover) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                            } else {
                                Text("اختر 🖼️", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.updateEntityImages(collectionName, entityId, avatarUrl, coverUrl)
                                Toast.makeText(context, "📸 تم حفظ وتحديث الصور بنجاح!", Toast.LENGTH_SHORT).show()
                                showChangePhotosDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ التغييرات 💾", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showChangePhotosDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // 2. DIALOG: Edit Profile Details
    if (showEditDetailsDialog) {
        Dialog(onDismissRequest = { showEditDetailsDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                var editName by remember { mutableStateOf(provider?.name ?: store?.name ?: property?.title ?: "") }
                var editPhone by remember { mutableStateOf(provider?.phone ?: store?.phone ?: property?.phone ?: "") }
                var editArea by remember { mutableStateOf(provider?.area ?: store?.cityId ?: property?.cityId ?: "") }
                var editDesc by remember { mutableStateOf(provider?.profession ?: store?.description ?: property?.description ?: "") }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📝 تعديل البيانات الأساسية للنشاط", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("اسم النشاط / صاحب العمل") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("رقم الهاتف") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editArea,
                        onValueChange = { editArea = it },
                        label = { Text("المدينة / المحافظة / الحي") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("الوصف / التخصص / الخدمات") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (provider != null) {
                                    val updated = provider.copy(name = editName, phone = editPhone, area = editArea, profession = editDesc)
                                    viewModel.updateProviderEntity(updated)
                                } else if (store != null) {
                                    val updated = store.copy(name = editName, phone = editPhone, localNeighborhood = editArea, description = editDesc)
                                    viewModel.updateStoreEntity(updated)
                                } else if (property != null) {
                                    val updated = property.copy(title = editName, phone = editPhone, localNeighborhood = editArea, description = editDesc)
                                    viewModel.updatePropertyEntity(updated)
                                }
                                Toast.makeText(context, "✅ تم حفظ التعديلات بنجاح!", Toast.LENGTH_SHORT).show()
                                showEditDetailsDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ التعديلات 💾", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showEditDetailsDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // 3. DIALOG: Edit Prices & Offers
    if (showPriceOfferDialog) {
        Dialog(onDismissRequest = { showPriceOfferDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                val coroutineScope = rememberCoroutineScope()
                var editPrice by remember {
                    mutableStateOf(
                        (provider?.previewPrice ?: property?.price ?: 0.0).toInt().toString()
                    )
                }
                val existingOffers = remember(provider, store) {
                    val raw = provider?.specialOffersJson ?: store?.specialOffersJson ?: ""
                    SpecialOfferEntity.parseList(raw)
                }
                var editOffer by remember {
                    mutableStateOf(
                        existingOffers.firstOrNull()?.description?.ifBlank { existingOffers.firstOrNull()?.title ?: "" }
                            ?: (provider?.specialOffersJson ?: store?.specialOffersJson ?: "").takeIf {
                                it.isNotBlank() && !it.trim().startsWith("[")
                            } ?: ""
                    )
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🏷️ إدارة الأسعار والعروض الترويجية", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text("السعر الأساسي / رسوم المعاينة والكشفية (ريال يمني)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editOffer,
                        onValueChange = { editOffer = it },
                        label = { Text("تفاصيل العرض الترويجي والخصومات") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val pVal = editPrice.trim().toDoubleOrNull() ?: 0.0
                                val cleanOffer = editOffer.trim()
                                val offerObj = if (cleanOffer.isNotBlank()) {
                                    SpecialOfferEntity(
                                        id = "offer_${entityId}_${System.currentTimeMillis()}",
                                        providerId = entityId,
                                        title = cleanOffer.take(60),
                                        description = cleanOffer,
                                        offerPrice = pVal,
                                        isEnabled = true
                                    )
                                } else null
                                val nextOffersJson = if (offerObj != null) {
                                    SpecialOfferEntity.serializeList(listOf(offerObj) + existingOffers.filter { it.description != cleanOffer }.take(4))
                                } else {
                                    ""
                                }
                                if (provider != null) {
                                    viewModel.updateProviderEntity(provider.copy(previewPrice = pVal, specialOffersJson = nextOffersJson))
                                } else if (store != null) {
                                    viewModel.updateStoreEntity(store.copy(specialOffersJson = nextOffersJson))
                                } else if (property != null) {
                                    viewModel.updatePropertyEntity(property.copy(price = pVal))
                                }
                                if (offerObj != null) {
                                    coroutineScope.launch {
                                        val repo = com.serviseyem.data.repositories.OffersRepository(
                                            com.google.firebase.firestore.FirebaseFirestore.getInstance(),
                                            com.serviseyem.data.LocalAppCacheManager(context)
                                        )
                                        repo.addOffer(offerObj)
                                    }
                                }
                                Toast.makeText(context, "🏷️ تم تحديث الأسعار والعروض بنجاح!", Toast.LENGTH_SHORT).show()
                                showPriceOfferDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text("حفظ التحديث 🏷️", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        OutlinedButton(
                            onClick = { showPriceOfferDialog = false },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // 4. DIALOG: Add Product / Meal / Service Item
    if (showAddProductDialog) {
        Dialog(onDismissRequest = { showAddProductDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                var prodTitle by remember { mutableStateOf("") }
                var prodCategory by remember { mutableStateOf("") }
                var prodPrice by remember { mutableStateOf("") }
                var prodDesc by remember { mutableStateOf("") }
                var prodImageUrl by remember { mutableStateOf("") }
                var isUploadingProdImage by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                val prodImagePicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri: android.net.Uri? ->
                    if (uri != null) {
                        coroutineScope.launch {
                            isUploadingProdImage = true
                            val path = "products/${System.currentTimeMillis()}_$entityId.webp"
                            val result = com.serviseyem.utils.FirebaseStorageUploader.uploadImageToStorage(context, uri, path)
                            isUploadingProdImage = false
                            result.onSuccess { url ->
                                prodImageUrl = url
                                Toast.makeText(context, "تم رفع صورة الصنف ✅", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "فشل الرفع: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("➕ إضافة منتج / صنف / خدمة جديدة", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

                    OutlinedTextField(
                        value = prodTitle,
                        onValueChange = { prodTitle = it },
                        label = { Text("اسم الصنف أو الخدمة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = prodCategory,
                        onValueChange = { prodCategory = it },
                        label = { Text("التصنيف (اختياري)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = prodPrice,
                        onValueChange = { prodPrice = it },
                        label = { Text("السعر بالريال اليمني") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = prodDesc,
                        onValueChange = { prodDesc = it },
                        label = { Text("الوصف والمواصفات") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = prodImageUrl,
                            onValueChange = { prodImageUrl = it },
                            label = { Text("صورة الصنف (اختياري)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Button(
                            onClick = { prodImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            enabled = !isUploadingProdImage,
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            if (isUploadingProdImage) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                            } else {
                                Text("صورة 📷", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val cleanTitle = prodTitle.trim()
                                val parsedPrice = prodPrice.trim().toDoubleOrNull()
                                if (cleanTitle.isBlank()) {
                                    Toast.makeText(context, "يرجى كتابة اسم الصنف", Toast.LENGTH_SHORT).show()
                                } else if (parsedPrice == null || parsedPrice < 0.0) {
                                    Toast.makeText(context, "يرجى إدخال سعر صحيح", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newProduct = ProductEntity(
                                        id = "prod_${System.currentTimeMillis()}",
                                        storeId = entityId,
                                        ownerId = entityId,
                                        name = cleanTitle,
                                        category = prodCategory.trim(),
                                        price = parsedPrice,
                                        description = prodDesc.trim(),
                                        imageUrl = prodImageUrl.trim()
                                    )
                                    viewModel.addProductDirect(
                                        product = newProduct,
                                        onSuccess = {
                                            Toast.makeText(context, "📦 تم إضافة الصنف بنجاح!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { e ->
                                            Toast.makeText(context, "فشل حفظ الصنف: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    showAddProductDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text("إضافة الصنف ➕", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showAddProductDialog = false },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text("إلغاء", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // 5. DIALOG: Delete Confirmation (Admin Only)
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("⚠️ تأكيد حذف النشاط", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في حذف هذا النشاط؟ سيتم إخفاؤه من قائمة الدليل والتطبيق، مع إمكانية استعادته لاحقاً من لوحة تحكم الأدمن.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.softDeleteEntity(
                            collection = collectionName,
                            entityId = entityId,
                            onSuccess = {
                                Toast.makeText(context, "🗑️ تم حذف النشاط وإخفاؤه بنجاح", Toast.LENGTH_LONG).show()
                                viewModel.navigateToScreen(AppScreens.USER_BROWSE)
                            },
                            onError = { e ->
                                Toast.makeText(context, "فشل حذف النشاط: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        )
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("نعم، حذف النشاط", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("تراجع")
                }
            }
        )
    }
}

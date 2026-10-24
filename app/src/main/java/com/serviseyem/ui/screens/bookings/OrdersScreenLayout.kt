/**
 * ⚠️ ملاحظة معمارية:
 * هذا الملف يحتوي على 500+ سطر في Composable واحد.
 * يجب تقسيمه في مرحلة التنظيف إلى:
 * 
 * 1. OrdersScreenLayout.kt - الحاوية الرئيسية
 * 2. InstantRequestOrderCard.kt - بطاقة الطلب الفوري
 * 3. StoreOrderCard.kt - بطاقة طلب المتجر
 * 4. OrderDeletionDialog.kt - حوار حذف الطلب
 * 5. DeleteAllOrdersDialog.kt - حوار حذف الكل
 * 
 * لا تقسّم الملف الآن، فقط وثّق الخطة.
 */

@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.bookings
import com.serviseyem.ui.*

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.*
import com.serviseyem.utils.*
import com.serviseyem.ui.MainViewModel

@Composable
fun OrdersScreenLayout(viewModel: MainViewModel, themeColors: VisualThemePalette, onRequestQuickService: () -> Unit = {}) {
    val orders by viewModel.orders.collectAsState()
    val instantRequests by viewModel.instantRequests.collectAsState()
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val context = LocalContext.current

    // Allow user to query by phone if currentUserPhone is empty
    var customPhoneInput by remember { mutableStateOf("") }
    val activePhone = remember(currentUserPhone, customPhoneInput) {
        currentUserPhone.ifEmpty { customPhoneInput }
    }

    val cleanActivePhone = remember(activePhone) {
        activePhone.trim().replace(" ", "").replace("+967", "").replace("00967", "")
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Instant Requests (اطلب خدمتك الآن), 1: Store Orders (المشتريات)

    val myInstantRequests = remember(instantRequests, cleanActivePhone, currentUserId) {
        instantRequests.filter { req ->
            val reqPhoneClean = req.userPhone.trim().replace(" ", "").replace("+967", "").replace("00967", "")
            cleanActivePhone.isBlank() || reqPhoneClean == cleanActivePhone || (currentUserId.isNotBlank() && req.userId == currentUserId)
        }.sortedByDescending { it.createdAt }
    }

    val myOrders = remember(orders, activePhone) {
        if (activePhone.isBlank()) {
            orders.sortedByDescending { it.timestamp }
        } else {
            orders.filter { 
                it.customerPhone.trim() == activePhone.trim()
            }.sortedByDescending { it.timestamp }
        }
    }

    var selectedOrderForDeletion by remember { mutableStateOf<OrderEntity?>(null) }
    var deletionCodeInput by remember { mutableStateOf("") }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
            .padding(12.dp)
    ) {
        // Top Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(themeColors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (selectedTab == 0) "⚡" else "🛍️", fontSize = 16.sp)
                }
                Column {
                    Text(
                        text = "📋 مركز متابعة طلباتي وضمان الخدمة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "تتبع الطلبات الفورية العاجلة ومشتريات المتاجر والمطاعم",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
            }

            if (selectedTab == 1 && myOrders.isNotEmpty()) {
                Button(
                    onClick = { showDeleteAllConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف الكل", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف الكل 🗑️", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Navigation Tabs: ⚡ طلبات الخدمات العاجلة vs 🛍️ مشتريات المتاجر
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = themeColors.surface,
            contentColor = themeColors.accent,
            modifier = Modifier.padding(bottom = 8.dp).clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "⚡ طلبات الخدمات العاجلة (${myInstantRequests.size})",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) themeColors.accent else Color.LightGray
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "🛍️ مشتريات المتاجر (${myOrders.size})",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) themeColors.accent else Color.LightGray
                    )
                }
            )
        }

        // If user phone is empty, prompt for phone entry
        if (currentUserPhone.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔐 استعراض فوري لطلباتك",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent,
                        textAlign = TextAlign.Center
                    )
                    OutlinedTextField(
                        value = customPhoneInput,
                        onValueChange = { customPhoneInput = it },
                        placeholder = { Text("أدخل رقم هاتفك لتصفح جميع طلباتك (مثال: 777123456)", color = Color.Gray, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "الهاتف", tint = themeColors.accent, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = themeColors.accent,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // TAB 0: Instant Service Requests (اطلب خدمتك الآن / المزاد العكسي)
        if (selectedTab == 0) {
            if (myInstantRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("⚡", fontSize = 52.sp)
                        Text(
                            text = if (activePhone.isBlank()) "يرجى كتابة رقم هاتفك أعلى الشاشة لعرض طلباتك" else "لا توجد طلبات خدمات عاجلة مسجلة لهذا الرقم",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "يمكنك إطلاق طلب فوري للحصول على عروض أسعار ومزايدات مباشرة من الفنيين والمزودين خلال دقائق",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onRequestQuickService,
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("⚡ اطلب خدمتك الآن (المزاد العكسي)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myInstantRequests) { req ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Header: Request code & Status Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFF10B981))
                                    ) {
                                        Text(
                                            text = "كود الطلب: ${req.requestCode}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    val statusLabel = when (req.status) {
                                        "WAITING_FOR_OFFERS" -> "⏳ بانتظار العروض"
                                        "REVIEWING_OFFERS" -> "👀 جاري مراجعة العروض"
                                        "IN_PROGRESS" -> "⚙️ جاري التنفيذ"
                                        "COMPLETED" -> "✅ مكتمل"
                                        "CANCELLED" -> "❌ ملغي"
                                        else -> "⏳ بانتظار العروض"
                                    }
                                    val statusColor = when (req.status) {
                                        "COMPLETED" -> Color(0xFF10B981)
                                        "IN_PROGRESS" -> Color(0xFFF59E0B)
                                        "CANCELLED" -> Color.Gray
                                        else -> Color(0xFF00E5FF)
                                    }

                                    Surface(
                                        color = statusColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = statusLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = req.serviceTitle.ifBlank { "طلب خدمة عاجلة" },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📍 ${req.userCity} ${if(req.userNeighborhood.isNotBlank()) "(${req.userNeighborhood})" else ""}", fontSize = 11.sp, color = Color.LightGray)
                                    Text("⏱️ ${req.urgencyTime}", fontSize = 11.sp, color = themeColors.accent, fontWeight = FontWeight.Bold)
                                }

                                if (req.rawPin.isNotBlank()) {
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "🔑 الرمز السري (PIN): ${req.rawPin}",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFFFDE047),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (req.description.isNotBlank()) {
                                    Text(
                                        text = req.description,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 2
                                    )
                                }

                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 2.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { viewModel.navigateToScreen(AppScreens.INSTANT_REQUESTS_VIEW) },
                                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("تصفح العروض والأسعار 📥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (req.status != "COMPLETED" && req.status != "CANCELLED") {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.cancelInstantRequest(req.id, req.rawPin)
                                                Toast.makeText(context, "🗑️ تم إلغاء الطلب بنجاح", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("إلغاء ❌", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 1: Store & Restaurant Orders
            // ✨ م2: استخدام الـ OrderStatus الموحد بدلاً من السلاسل النصية الخام
            val newCount = remember(myOrders) { myOrders.count { it.status == com.serviseyem.data.models.OrderStatus.PENDING.code || it.status.isEmpty() } }
            val inProgressCount = remember(myOrders) { myOrders.count { it.status == com.serviseyem.data.models.OrderStatus.PROCESSING.code } }
            val completedCount = remember(myOrders) { myOrders.count { it.status == com.serviseyem.data.models.OrderStatus.COMPLETED.code } }
            val cancelledCount = remember(myOrders) { myOrders.count { it.status == com.serviseyem.data.models.OrderStatus.CANCELLED.code } }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.surface.copy(alpha = 0.8f)),
                border = BorderStroke(0.6.dp, themeColors.accent.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔴 $newCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        Text("جديدة", fontSize = 9.sp, color = Color.LightGray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🟡 $inProgressCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        Text("قيد التجهيز", fontSize = 9.sp, color = Color.LightGray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🟢 $completedCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        Text("مكتملة", fontSize = 9.sp, color = Color.LightGray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚫ $cancelledCount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("ملغية", fontSize = 9.sp, color = Color.LightGray)
                    }
                }
            }

            if (activePhone.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🛍️", fontSize = 48.sp)
                        Text("اكتب رقم هاتفك أعلاه لعرض طلباتك", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                    }
                }
            } else if (myOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📭", fontSize = 48.sp)
                        Text("لا توجد أي طلبات شراء مسجلة لهذا الرقم", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                        Text("تأكد من رقم الهاتف أو اطلب سلعاً من المتاجر والمطاعم", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            } else {
                var orderPageLimit by remember { mutableIntStateOf(20) }
                val paginatedOrders = remember(myOrders, orderPageLimit) {
                    myOrders.take(orderPageLimit)
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(paginatedOrders) { order ->
                    val deleteCode = remember(order.id) { 
                        (order.id.hashCode().let { kotlin.math.abs(it) } % 9000 + 1000).toString()
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Order ID & Status Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "طلب شراء #${order.id.takeLast(6)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = themeColors.accent
                                )
                                Surface(
                                    color = when (order.status) {
                                        "COMPLETED" -> Color(0xFF10B981)
                                        "CANCELLED" -> Color(0xFFEF4444)
                                        "PROCESSING" -> Color(0xFF3B82F6)
                                        else -> Color(0xFFF59E0B)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = when (order.status) {
                                            com.serviseyem.data.models.OrderStatus.COMPLETED.code -> "مكتمل ومستلم ✅"
                                            com.serviseyem.data.models.OrderStatus.CANCELLED.code -> "ملغي ❌"
                                            com.serviseyem.data.models.OrderStatus.PROCESSING.code -> "قيد التجهيز والتوصيل 🛵"
                                            else -> "قيد المراجعة ⏳"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.status == com.serviseyem.data.models.OrderStatus.PROCESSING.code || order.status == com.serviseyem.data.models.OrderStatus.CANCELLED.code) Color.White else Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Product and Order details
                            Text(
                                text = "📦 المنتج: ${order.productName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🏪 المتجر: ${order.storeName.ifEmpty { "متجر معتمد" }}",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                            Text(
                                text = "📝 الملاحظات: ${order.notes.ifBlank { "لا توجد" }}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "الكمية: ${order.quantity} | السعر: ${order.price} ر.ي",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = "💰 الإجمالي: ${order.totalAmount} ر.ي",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.accent
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Deletion Code display
                                Box(
                                    modifier = Modifier
                                        .background(themeColors.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "رمز الحذف: $deleteCode",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.accent
                                    )
                                }

                                // Delete Order Button with PIN verification
                                IconButton(
                                    onClick = {
                                        selectedOrderForDeletion = order
                                        deletionCodeInput = ""
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.Red.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف",
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (myOrders.size > paginatedOrders.size) {
                    item {
                        Button(
                            onClick = { orderPageLimit += 20 },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface)
                        ) {
                            Text("تحميل المزيد من الطلبات (${paginatedOrders.size} من ${myOrders.size})", color = themeColors.accent, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // 🔒 Confirm Individual Order Deletion Code Dialog
    val orderToDelete = selectedOrderForDeletion
    if (orderToDelete != null) {
        val order = orderToDelete
        val correctCode = (order.id.hashCode().let { kotlin.math.abs(it) } % 9000 + 1000).toString()

        Dialog(onDismissRequest = { selectedOrderForDeletion = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, themeColors.accent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔒 تأكيد رمز الحذف الآمن",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    Text(
                        text = "لحذف هذا الطلب، يرجى كتابة كود الحذف الآمن وهو ($correctCode):",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    OutlinedTextField(
                        value = deletionCodeInput,
                        onValueChange = { deletionCodeInput = it },
                        placeholder = { Text("اكتب الرمز المكون من 4 أرقام") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = themeColors.accent
                        ),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (deletionCodeInput.trim() == correctCode) {
                                    viewModel.deleteOrder(order.id)
                                    selectedOrderForDeletion = null
                                    Toast.makeText(context, "✅ تم حذف وإلغاء الطلب بنجاح!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "❌ رمز الحذف غير صحيح! يرجى المحاولة مرة أخرى.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تأكيد الحذف 🗑️", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        OutlinedButton(
                            onClick = { selectedOrderForDeletion = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("تراجع", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // 🗑️ Delete All Confirmation Dialog
    if (showDeleteAllConfirm) {
        Dialog(onDismissRequest = { showDeleteAllConfirm = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️ تنبيه هام: حذف جميع الطلبات",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Text(
                        text = "هل أنت متأكد من رغبتك في حذف جميع طلبات الشراء المسجلة برقم هاتفك ($activePhone)؟",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.deleteAllOrders(activePhone)
                                showDeleteAllConfirm = false
                                Toast.makeText(context, "✅ تم حذف جميع الطلبات بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("نعم، احذف الكل", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        OutlinedButton(
                            onClick = { showDeleteAllConfirm = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("تراجع", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
}

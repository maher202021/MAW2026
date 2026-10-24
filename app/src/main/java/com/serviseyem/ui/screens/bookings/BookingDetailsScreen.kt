package com.serviseyem.ui.screens.bookings

import android.content.Intent
import com.serviseyem.ui.*
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.BookingEntity
import com.serviseyem.ui.dialogs.BookingCancellationDialog
import com.serviseyem.utils.BookingStateMachine

/**
 * 📋 BookingDetailsScreen
 * شاشة استعراض تفاصيل الحجز الشاملة مع إظهار البيانات والأزرار التفاعلية حسب دور المستخدم (عميل / فني / أدمن).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    booking: BookingEntity,
    userRole: String = "CLIENT", // "CLIENT", "PROVIDER", "ADMIN"
    currentUserId: String = "",
    onBack: () -> Unit,
    onNavigateToChat: (recipientId: String, recipientName: String) -> Unit = { _, _ -> },
    onNavigateToStatusTracking: (BookingEntity) -> Unit = {},
    onUpdateStatus: (bookingId: String, newStatus: String) -> Unit = { _, _ -> },
    onCancelBooking: (bookingId: String, password: String, reason: String) -> Unit = { _, _, _ -> },
    onDeleteBooking: ((bookingId: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val resolvedUserId = remember(currentUserId, context) {
        currentUserId.ifBlank {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    ?: context.getSharedPreferences("yemen_service_prefs", android.content.Context.MODE_PRIVATE)
                        .getString("user_phone", "") ?: ""
            } catch (_: Exception) {
                ""
            }
        }
    }
    val isAdmin = userRole == "ADMIN" || userRole == "OWNER"
    val isBookingOwner = remember(booking, resolvedUserId) {
        val normCurrent = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(resolvedUserId)
        val normCustomerPhone = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(booking.effectiveCustomerPhone)
        resolvedUserId.isNotBlank() && (
            booking.customerId == resolvedUserId ||
            booking.clientId == resolvedUserId ||
            (normCurrent.isNotBlank() && normCustomerPhone == normCurrent)
        )
    }
    val isAssignedProvider = remember(booking, resolvedUserId, userRole) {
        val normCurrent = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(resolvedUserId)
        val normProvPhone = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(booking.providerPhone)
        userRole == "PROVIDER" && resolvedUserId.isNotBlank() && (
            booking.providerId == resolvedUserId ||
            (normCurrent.isNotBlank() && normProvPhone == normCurrent)
        )
    }
    val canViewSensitiveCustomerData = (booking.customerId == resolvedUserId && resolvedUserId.isNotBlank()) || isBookingOwner || isAdmin

    var showCancelDialog by remember { mutableStateOf(false) }
    var showPasswordVisible by remember { mutableStateOf(false) }
    var showPinVerifyDialog by remember { mutableStateOf(false) }
    var enteredPinText by remember { mutableStateOf("") }
    var verifiedPlainPin by remember { mutableStateOf("") }
    var showRateDialog by remember { mutableStateOf(false) }
    var ratingValue by remember { mutableFloatStateOf(5f) }

    androidx.activity.compose.BackHandler(enabled = true, onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "تفاصيل الحجز",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "#${booking.bookingNumber.ifEmpty { booking.id.take(8) }}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToStatusTracking(booking) }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "تتبع المسار",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status and Track Bar Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val statusLabel = BookingStateMachine.getStatusLabel(booking.status)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "حالة الحجز الحالية",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = statusLabel,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val statusColor = Color(android.graphics.Color.parseColor(BookingStateMachine.getStatusColor(booking.status)))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = statusLabel,
                                color = statusColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Track button
                    OutlinedButton(
                        onClick = { onNavigateToStatusTracking(booking) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("عرض مسار ومراحل الطلب التفصيلي (7 مراحل)")
                    }
                }
            }

            // Service & Appointment Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "تفاصيل الخدمة والموعد",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    DetailRow(icon = Icons.Default.Build, label = "نوع الخدمة", value = booking.serviceType)
                    DetailRow(icon = Icons.Default.DateRange, label = "التاريخ", value = booking.dateString.ifEmpty { booking.date })
                    DetailRow(icon = Icons.Default.Info, label = "الوقت", value = booking.timeString.ifEmpty { booking.time })
                    DetailRow(icon = Icons.Default.LocationOn, label = "الموقع والحي", value = booking.customerArea.ifEmpty { booking.clientAddress.ifEmpty { "غير محدد" } })

                    if (booking.serviceDetails.isNotBlank()) {
                        DetailRow(icon = Icons.Default.Edit, label = "ملاحظات وتفاصيل إضافية", value = booking.serviceDetails)
                    }
                }
            }

            // Secret Password Card (Visible to authorized Customer & Admin only: booking.customerId == currentUserId || isAdmin)
            if (canViewSensitiveCustomerData) {
                val hasSecret = booking.effectivePin.isNotBlank()
                if (hasSecret) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "الرمز السري لتعديل أو إلغاء الحجز",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                IconButton(onClick = {
                                    if (showPasswordVisible) {
                                        showPasswordVisible = false
                                        verifiedPlainPin = ""
                                    } else {
                                        showPinVerifyDialog = true
                                        enteredPinText = ""
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (showPasswordVisible) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = "إظهار الرمز",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (showPasswordVisible && verifiedPlainPin.isNotBlank()) {
                                    verifiedPlainPin
                                } else {
                                    "••••"
                                },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 4.sp
                            )
                        }
                    }
                }
            }

            // Secure PIN Verification Dialog in Details
            if (showPinVerifyDialog && canViewSensitiveCustomerData) {
                AlertDialog(
                    onDismissRequest = { showPinVerifyDialog = false },
                    title = { Text("🔒 تحقق من رمز الأمان (PIN)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "يرجى كتابة رمز الأمان المكون من 4 أرقام لتأكيد الهوية وعرض الرمز الأصلي للحجز:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = enteredPinText,
                                onValueChange = { if (it.length <= 6) enteredPinText = it },
                                placeholder = { Text("مثال: 1234", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val targetHash = booking.effectivePin
                                val isVerified = com.serviseyem.utils.SecureHasher.verifyPin(enteredPinText, targetHash)
                                if (isVerified) {
                                    verifiedPlainPin = enteredPinText
                                    showPasswordVisible = true
                                    showPinVerifyDialog = false
                                    Toast.makeText(context, "✅ تم التحقق من الرمز بنجاح!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "❌ رمز الأمان (PIN) غير صحيح!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("تحقق ✅", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPinVerifyDialog = false }) {
                            Text("إلغاء ❌")
                        }
                    }
                )
            }

            // Client Info Card (Visible to Assigned Provider & Admin)
            if (isAssignedProvider || isAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "بيانات العميل",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        DetailRow(icon = Icons.Default.Person, label = "اسم العميل", value = booking.customerName.ifEmpty { booking.clientName })
                        DetailRow(icon = Icons.Default.Phone, label = "رقم الهاتف", value = booking.customerPhone.ifEmpty { booking.clientPhone })
                        DetailRow(icon = Icons.Default.Home, label = "العنوان", value = booking.customerArea.ifEmpty { booking.clientAddress })
                    }
                }
            }

            // Provider Info Card (Visible to authorized Customer & Admin)
            if (canViewSensitiveCustomerData) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "بيانات الفني ومقدم الخدمة",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (booking.providerPhoto.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AsyncImage(
                                    model = booking.providerPhoto,
                                    contentDescription = "صورة مقدم الخدمة",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = booking.providerName.ifEmpty { "فني معتمد" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            DetailRow(icon = Icons.Default.Person, label = "اسم الفني", value = booking.providerName.ifEmpty { "فني معتمد" })
                        }
                        if (booking.providerPhone.isNotBlank()) {
                            DetailRow(icon = Icons.Default.Phone, label = "رقم الاتصال", value = booking.providerPhone)
                        }
                    }
                }
            }

            // Payment Information Card
            if (canViewSensitiveCustomerData) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "معلومات الدفع والتكاليف",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        DetailRow(
                            icon = Icons.Default.ShoppingCart,
                            label = "حالة الدفع",
                            value = if (booking.paymentStatus == "paid" || booking.status == "PAID") "تم الدفع بالكامل" else "غير مسدد (الدفع عند إكمال الخدمة)"
                        )

                        if (booking.totalAmount > 0) {
                            DetailRow(
                                icon = Icons.Default.CheckCircle,
                                label = "المبلغ الإجمالي",
                                value = "${booking.totalAmount} ريال يمني"
                            )
                        }
                    }
                }
            }

            // Interactive Actions Section based on Role
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "خيارات التواصل والإجراءات",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Communication buttons (Call & Chat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val targetPhone = if (userRole == "CLIENT") booking.providerPhone else booking.customerPhone.ifEmpty { booking.clientPhone }
                        Button(
                            onClick = {
                                if (targetPhone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$targetPhone"))
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "رقم الهاتف غير متوفر", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اتصال", color = Color.White)
                        }

                        val targetName = if (userRole == "CLIENT") booking.providerName else booking.customerName.ifEmpty { booking.clientName }
                        val targetId = if (userRole == "CLIENT") booking.providerId else booking.clientId.ifEmpty { booking.customerPhone }
                        Button(
                            onClick = { onNavigateToChat(targetId, targetName) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("محادثة", color = Color.White)
                        }
                    }

                    // Role-Specific State Transition Buttons
                    when (userRole) {
                        "CLIENT" -> {
                            if (booking.status == "PENDING" || booking.status == "ACCEPTED" || booking.status == "UNDER_REVIEW") {
                                Button(
                                    onClick = { showCancelDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("إلغاء الحجز")
                                }
                            }

                            if (booking.status == "COMPLETED") {
                                Button(
                                    onClick = { showRateDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تقييم مستوى الخدمة")
                                }
                            }
                        }

                        "PROVIDER" -> {
                            when (booking.status.uppercase()) {
                                "PENDING" -> {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Button(
                                            onClick = { onUpdateStatus(booking.id, "ACCEPTED") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                        ) {
                                            Text("قبول الحجز")
                                        }

                                        Button(
                                            onClick = { onUpdateStatus(booking.id, "REJECTED") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                        ) {
                                            Text("اعتذار")
                                        }
                                    }
                                }

                                "ACCEPTED" -> {
                                    Button(
                                        onClick = { onUpdateStatus(booking.id, "IN_PROGRESS") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                                    ) {
                                        Text("بدء تنفيذ الخدمة")
                                    }
                                }

                                "IN_PROGRESS" -> {
                                    Button(
                                        onClick = { onUpdateStatus(booking.id, "COMPLETED") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                    ) {
                                        Text("تم إكمال الخدمة بنجاح")
                                    }
                                }
                            }
                        }

                        "ADMIN" -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onUpdateStatus(booking.id, "COMPLETED") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Text("إكمال", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { showCancelDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                                ) {
                                    Text("إلغاء", fontSize = 12.sp)
                                }

                                if (onDeleteBooking != null) {
                                    Button(
                                        onClick = { onDeleteBooking(booking.id) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                    ) {
                                        Text("حذف", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Cancellation Dialog
    if (showCancelDialog) {
        BookingCancellationDialog(
            booking = booking,
            userRole = userRole,
            onDismiss = { showCancelDialog = false },
            onConfirmCancel = { password, reason ->
                showCancelDialog = false
                onCancelBooking(booking.id, password, reason)
            }
        )
    }

    // Rating Dialog
    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("تقييم الخدمة") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("ما رأيك في جودة الخدمة المقدمة؟", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        (1..5).forEach { star ->
                            IconButton(onClick = { ratingValue = star.toFloat() }) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (star <= ratingValue) Color(0xFFF59E0B) else Color.Gray
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showRateDialog = false
                    Toast.makeText(context, "شكراً لتقييمكم!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("إرسال التقييم")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "$label: ",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

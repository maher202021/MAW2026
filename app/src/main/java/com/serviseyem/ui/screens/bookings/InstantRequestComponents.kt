package com.serviseyem.ui.screens.bookings

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.serviseyem.NetworkUtils
import com.serviseyem.ui.screens.dashboard.components.UnifiedEmptyState
import androidx.compose.ui.unit.sp
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.data.models.RequestOfferEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

@Composable
fun InstantRequestList(
    requests: List<InstantRequestEntity>,
    offers: List<RequestOfferEntity>,
    currentUserId: String,
    themeColors: VisualThemePalette,
    isLoading: Boolean = false,
    onCreateRequestClick: (() -> Unit)? = null,
    onRetryClick: (() -> Unit)? = null,
    onViewOffers: (InstantRequestEntity) -> Unit,
    onSubmitOffer: (InstantRequestEntity) -> Unit
) {
    val context = LocalContext.current
    val isOnline = remember(requests) { NetworkUtils.isNetworkAvailable(context) }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = themeColors.accent)
                Text(
                    text = "جاري تحميل الطلبات الفورية... ⏳",
                    fontSize = 13.sp,
                    color = themeColors.textSecondary
                )
            }
        }
    } else if (!isOnline && requests.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            UnifiedEmptyState(
                title = "لا يوجد اتصال بالإنترنت",
                description = "تعذر تحميل الطلبات لعدم توفر اتصال بالشبكة. يرجى التحقق من الاتصال والمحاولة مرة أخرى.",
                iconText = "📡",
                actionLabel = "إعادة المحاولة 🔄",
                onActionClick = onRetryClick,
                themeColors = themeColors
            )
        }
    } else if (requests.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            UnifiedEmptyState(
                title = "لا توجد طلبات فورية حالياً",
                description = "يمكنك تقديم طلب جديد وتلقي عروض أسعار تنافسية من الفنيين والمزودين خلال ساعتين.",
                iconText = "⚡",
                actionLabel = "اطلب خدمتك الآن ⚡",
                onActionClick = onCreateRequestClick,
                themeColors = themeColors
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(requests) { req ->
                val reqOffers = offers.filter { it.requestId == req.id }
                InstantRequestCard(
                    req = req,
                    offersCount = reqOffers.size,
                    currentUserId = currentUserId,
                    themeColors = themeColors,
                    onViewOffers = { onViewOffers(req) },
                    onSubmitOffer = { onSubmitOffer(req) }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun InstantRequestCard(
    req: InstantRequestEntity,
    offersCount: Int,
    currentUserId: String,
    themeColors: VisualThemePalette,
    onViewOffers: () -> Unit,
    onSubmitOffer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(req.serviceTitle, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                Text(req.status, color = themeColors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text(req.description, color = Color.LightGray, fontSize = 12.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("المدينة: ${req.userCity}", color = Color.Gray, fontSize = 11.sp)
                Text("العروض: $offersCount", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (req.userId == currentUserId) {
                    Button(
                        onClick = onViewOffers,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                    ) {
                        Text("مراجعة العروض", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (req.status == "WAITING_FOR_OFFERS" || req.status == "REVIEWING_OFFERS") {
                    Button(
                        onClick = onSubmitOffer,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("تقديم عرض سعر", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateInstantRequestDialog(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("طلب خدمة فورية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الخدمة المطلوبة") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("التفاصيل والمشكلة") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("المدينة / الحي") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || city.isBlank()) {
                        Toast.makeText(context, "الرجاء تعبئة العنوان والمدينة", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSubmitting = true
                    viewModel.createInstantRequest(
                        userId = viewModel.currentUserId.value,
                        userName = viewModel.currentUserName.value,
                        userPhone = viewModel.currentUserPhone.value,
                        userCity = city,
                        userNeighborhood = city,
                        categoryId = "GENERAL",
                        categoryName = "عام",
                        serviceTitle = title,
                        description = details,
                        images = emptyList(),
                        urgencyTime = "فوراً (خلال 30 دقيقة)"
                    ) { success, reqId, reqCode ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "✅ تم إرسال طلبك للفنيين بنجاح", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "❌ تعذر إرسال الطلب، يرجى التأكد من اتصال الإنترنت والمحاولة ثانية", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isSubmitting && title.isNotBlank() && city.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
            ) {
                if (isSubmitting) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                else Text("نشر الطلب 🚀", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = Color.Gray) }
        },
        containerColor = Color(0xFF1E293B)
    )
}

@Composable
fun SubmitOfferDialog(
    request: InstantRequestEntity,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    var price by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تقديم عرض سعر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("الطلب: ${request.serviceTitle}", color = Color.LightGray, fontSize = 12.sp)
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("السعر المقترح (ريال)") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("وقت الوصول المتوقع (دقائق)") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية للعميل") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    textStyle = LocalTextStyle.current.copy(color = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (price.isBlank()) {
                        Toast.makeText(context, "الرجاء تحديد السعر", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSubmitting = true
                    viewModel.submitOfferForRequest(
                        requestId = request.id,
                        requestCode = request.requestCode,
                        technicianId = viewModel.currentUserId.value,
                        technicianName = viewModel.currentUserName.value,
                        technicianPhone = viewModel.currentUserPhone.value,
                        technicianAvatar = "",
                        technicianRating = 5.0f,
                        price = price.toDoubleOrNull() ?: 0.0,
                        estimatedArrivalTime = "$time دقيقة",
                        estimatedDuration = "ساعتان",
                        notes = notes
                    )
                    isSubmitting = false
                    Toast.makeText(context, "✅ تم تقديم عرضك بنجاح للعميل", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                enabled = !isSubmitting && price.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
            ) {
                if (isSubmitting) CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                else Text("إرسال العرض 📤", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = Color.Gray) }
        },
        containerColor = Color(0xFF1E293B)
    )
}

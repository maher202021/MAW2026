package com.serviseyem.ui.dialogs

import android.content.Context
import com.serviseyem.ui.*
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.RatingEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import java.util.UUID

/**
 * ⭐ نظام التقييمات متعدد الأبعاد (الجودة، السرعة، الاحترافية، مناسبة السعر)
 */
@Composable
fun MultiDimensionRatingDialog(
    targetId: String,
    targetName: String,
    targetType: String,
    bookingId: String = "",
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()

    var qualityRating by remember { mutableFloatStateOf(5.0f) }
    var speedRating by remember { mutableFloatStateOf(5.0f) }
    var professionalismRating by remember { mutableFloatStateOf(5.0f) }
    var priceRating by remember { mutableFloatStateOf(5.0f) }
    var commentInput by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    val overallRating = remember(qualityRating, speedRating, professionalismRating, priceRating) {
        (qualityRating + speedRating + professionalismRating + priceRating) / 4.0f
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, Color(0xFFFFA000).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(22.dp))
                        Text(
                            text = "تقييم مستوى الخدمة والجودة",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.LightGray)
                    }
                }

                if (!isSubmitted) {
                    Text(
                        text = "تقييمك المباشر لـ: $targetName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )

                    // Dimension 1: Quality
                    RatingDimensionRow(
                        title = "جودة العمل والتنفيذ 🛠️",
                        rating = qualityRating,
                        onRatingChange = { qualityRating = it }
                    )

                    // Dimension 2: Speed
                    RatingDimensionRow(
                        title = "سرعة الإنجاز والالتزام بالموعد ⚡",
                        rating = speedRating,
                        onRatingChange = { speedRating = it }
                    )

                    // Dimension 3: Professionalism
                    RatingDimensionRow(
                        title = "التعامل والاحترافية 🤝",
                        rating = professionalismRating,
                        onRatingChange = { professionalismRating = it }
                    )

                    // Dimension 4: Price
                    RatingDimensionRow(
                        title = "مناسبة السعر مقابل الخدمة 💰",
                        rating = priceRating,
                        onRatingChange = { priceRating = it }
                    )

                    Divider(color = Color.DarkGray, thickness = 1.dp)

                    // Overall summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("المتوسط الإجمالي:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("⭐ ${String.format(java.util.Locale.US, "%.1f", overallRating)} / 5.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                    }

                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        label = { Text("أكتب تفاصيل تجربتك الصادقة لمساعدة العملاء الآخرين", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Button(
                        onClick = {
                            if (currentUserId.isBlank() || currentUserId == "guest" || currentUserId.startsWith("guest_")) {
                                Toast.makeText(context, "عذراً! التقييم متاح فقط للمستخدمين والفنيين وأصحاب المحلات المسجلين بالنظام.", Toast.LENGTH_LONG).show()
                            } else if (commentInput.isNotBlank()) {
                                val doSubmit = {
                                    val ratingEntity = RatingEntity(
                                        id = "rate_${UUID.randomUUID().toString().take(8)}",
                                        targetId = targetId,
                                        targetType = targetType,
                                        userId = currentUserPhone.ifEmpty { "user" },
                                        userName = currentUserName.ifEmpty { "عميل موثق" },
                                        userPhone = currentUserPhone,
                                        bookingId = bookingId,
                                        rating = overallRating,
                                        qualityRating = qualityRating,
                                        speedRating = speedRating,
                                        professionalismRating = professionalismRating,
                                        priceFairnessRating = priceRating,
                                        comment = commentInput,
                                        isApproved = true,
                                        timestamp = System.currentTimeMillis()
                                    )
                                    viewModel.submitRating(ratingEntity) {
                                        isSubmitted = true
                                    }
                                }

                                if (bookingId.isNotEmpty()) {
                                    val bk = viewModel.bookings.value.find { it.id == bookingId }
                                    val status = bk?.status?.uppercase() ?: ""
                                    if (bk != null && status != "COMPLETED") {
                                        Toast.makeText(context, "لا يمكن التقييم إلا بعد اكتمال الخدمة", Toast.LENGTH_SHORT).show()
                                    } else {
                                        doSubmit()
                                    }
                                } else {
                                    doSubmit()
                                }
                            } else {
                                Toast.makeText(context, "⚠️ يرجى كتابة تعليق لوصف الخدمة", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("نشر التقييم المعتمد ⭐", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(44.dp))
                        Text("شكراً لمشاركتك تقييمك القيّم!", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White, textAlign = TextAlign.Center)
                        Text("تقييمك يساهم في رفع جودة الخدمات بالمنصة ومكافأة المتميزين.", fontSize = 11.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إغلاق", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingDimensionRow(
    title: String,
    rating: Float,
    onRatingChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 1..5) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (i <= rating) Color(0xFFFFB300) else Color.DarkGray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onRatingChange(i.toFloat()) }
                )
            }
        }
    }
}

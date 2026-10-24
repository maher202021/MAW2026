package com.serviseyem.ui.components

import android.content.Context
import com.serviseyem.ui.*
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.RatingEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.getStarsString

@Composable
fun ProviderDetailsDialog(
    provider: ProviderEntity,
    themeColors: VisualThemePalette,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onChatOpen: ((String) -> Unit)? = null,
    onBookClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val detailProfessionText = provider.profession.ifEmpty { "صيانة فنية شاملة وخدمات" }
    val detailSpecializationText = provider.specialization.ifEmpty { "جميع أعمال وتطبيقات الصيانة المعتمدة" }
    val isVerified = provider.isVerified || provider.subscriptionStatus == "APPROVED" || provider.isAvailable

    val coverImg = provider.coverImage.ifBlank { "" }
    val avatarImg = provider.profileImage.ifBlank { "" }
    val workPhotos = provider.portfolioImages.ifEmpty { provider.workPhotosBase64 }

    var selectedPreviewImage by remember { mutableStateOf<String?>(null) }
    var showReviewSection by remember { mutableStateOf(false) }

    val allRatingsState = viewModel.ratings.collectAsState()
    val providerRatings = remember(allRatingsState.value, provider.id) {
        allRatingsState.value.filter { it.targetId == provider.id }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0B1120)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top App Bar
                Surface(
                    color = Color(0xFF0F172A),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }

                        Text(
                            text = "الملف التعريفي لمقدم الخدمة",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "تعرّف على مقدم الخدمة ${provider.name} (${provider.profession}) عبر تطبيق دليل اليمن!\nهاتف: ${provider.phone}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة بيانات مقدم الخدمة"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = themeColors.accent)
                        }
                    }
                }

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Cover Photo Hero Banner (Full Screen Width)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFF1E293B))
                    ) {
                        if (coverImg.isNotBlank()) {
                            SmartAsyncImage(
                                model = coverImg,
                                contentDescription = "Cover Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { selectedPreviewImage = coverImg }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF334155))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🛠️", fontSize = 42.sp)
                                    Text("دليل اليمن للخدمات المعتمدة", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                        }

                        // Gradient overlay for smooth transition
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0x99000000), Color(0xFF0B1120)),
                                        startY = 60f
                                    )
                                )
                        )

                        // Top Badges
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (provider.isVip) {
                                Surface(
                                    color = Color(0xFFF59E0B),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "👑 فني معتمد VIP",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            } else if (isVerified) {
                                Surface(
                                    color = Color(0xFF10B981),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "موثق ومعتمد ✓",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Surface(
                                color = Color.Black.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${String.format("%.1f", provider.rating)} (${providerRatings.size} تقييم)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // 2. Profile Avatar & Basic Information Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Large Circular Profile Avatar
                            Box(
                                modifier = Modifier
                                    .offset(y = (-35).dp)
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A))
                                    .border(3.dp, themeColors.accent, CircleShape)
                                    .clickable {
                                        if (avatarImg.isNotBlank()) selectedPreviewImage = avatarImg
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarImg.isNotBlank()) {
                                    SmartAsyncImage(
                                        model = avatarImg,
                                        contentDescription = provider.name,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text("👤", fontSize = 36.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = provider.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("✔️", fontSize = 13.sp, color = themeColors.accent)
                                    }
                                }

                                Text(
                                    text = detailProfessionText,
                                    fontSize = 12.5.sp,
                                    color = themeColors.accent,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = if (provider.isAvailable) "🟢 متاح للعمل الفوري" else "🔴 مشغول حالياً",
                                    fontSize = 11.sp,
                                    color = if (provider.isAvailable) Color(0xFF10B981) else Color(0xFFEF5350),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 3. Quick Action Buttons Row (Chat, Call, Book)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-15).dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Direct Chat Button (إصلاح التوجيه لمقدم الخدمة مباشرة بدلاً من الدعم الفني)
                            Button(
                                onClick = {
                                    onDismiss()
                                    if (onChatOpen != null) {
                                        onChatOpen(provider.id.ifBlank { provider.phone })
                                    } else {
                                        viewModel.openDirectChat(
                                            targetUserId = provider.id.ifBlank { provider.phone },
                                            targetUserName = provider.name,
                                            targetUserPhoto = provider.profileImage,
                                            relatedEntityId = provider.id,
                                            relatedEntityType = "PROVIDER"
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(44.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("محادثة فورية 💬", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            // Direct Phone Call Button
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "تعذر فتح الهاتف", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(44.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اتصال 📞", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            // Instant Booking Button
                            Button(
                                onClick = {
                                    onDismiss()
                                    onBookClick?.invoke()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(44.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text("حجز موعد 📅", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }

                        // 4. Details Information Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "📋 البيانات الرسمية والتخصص",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.accent
                                )

                                DetailRowItem(icon = Icons.Default.Person, label = "الاسم الكامل:", value = provider.name)
                                DetailRowItem(icon = Icons.Default.Build, label = "المهنة:", value = detailProfessionText)
                                DetailRowItem(icon = Icons.Default.CheckCircle, label = "التخصص الدقيق:", value = detailSpecializationText)
                                DetailRowItem(
                                    icon = Icons.Default.LocationOn,
                                    label = "المنطقة والحي:",
                                    value = "${provider.area} - ${provider.localNeighborhood.ifBlank { "المركز الرئيسي" }}"
                                )
                                DetailRowItem(icon = Icons.Default.Phone, label = "رقم الهاتف المعتمد:", value = provider.phone)
                                DetailRowItem(
                                    icon = Icons.Default.ShoppingCart,
                                    label = "سعر المعاينة المبدئي:",
                                    value = "${provider.previewPrice.toInt()} ريال يمني تقريباً"
                                )
                                DetailRowItem(
                                    icon = Icons.Default.Star,
                                    label = "النقاط المهنية التراكمية:",
                                    value = "${provider.points} نقطة ثقة"
                                )
                            }
                        }

                        // 5. Portfolio & Work Photos Gallery
                        if (workPhotos.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🖼️ معرض صور الأعمال والمشاريع السابقة (${workPhotos.size})",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColors.accent
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(workPhotos) { photo ->
                                            Box(
                                                modifier = Modifier
                                                    .size(110.dp, 85.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF0F172A))
                                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                    .clickable { selectedPreviewImage = photo }
                                            ) {
                                                SmartAsyncImage(
                                                    model = photo,
                                                    contentDescription = "Work photo",
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6. Reviews Section & Experience Ratings
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "💬 آراء وتجارب العملاء (${providerRatings.size})",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.accent
                                    )
                                    TextButton(onClick = { showReviewSection = !showReviewSection }) {
                                        Text(if (showReviewSection) "إخفاء التقييم ✕" else "إضافة تقييمك ✍️", fontSize = 11.5.sp, color = themeColors.accent)
                                    }
                                }

                                if (showReviewSection) {
                                    AddReviewForm(
                                        providerId = provider.id,
                                        viewModel = viewModel,
                                        themeColors = themeColors,
                                        onSubmitted = { showReviewSection = false }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                if (providerRatings.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "لا توجد تقييمات مكتوبة حتى الآن، شارك أول تجربة!",
                                            fontSize = 11.5.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                } else {
                                    providerRatings.take(4).forEach { rev ->
                                        Surface(
                                            color = Color(0xFF0F172A),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = rev.userName.ifBlank { "عميل موثوق" },
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = themeColors.accent
                                                    )
                                                    Text(getStarsString(rev.rating), color = Color.Yellow, fontSize = 11.sp)
                                                }
                                                if (rev.comment.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Text(rev.comment, fontSize = 11.sp, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Full Close Button
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("رجوع للقائمة 🔙", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Full Size Image Zoom Viewer Dialog
    if (selectedPreviewImage != null) {
        Dialog(
            onDismissRequest = { selectedPreviewImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { selectedPreviewImage = null },
                contentAlignment = Alignment.Center
            ) {
                selectedPreviewImage?.let { previewImg ->
                    SmartAsyncImage(
                        model = previewImg,
                        contentDescription = "Full photo preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                IconButton(
                    onClick = { selectedPreviewImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 11.5.sp, color = Color.LightGray)
        }
        Text(value, fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

@Composable
private fun AddReviewForm(
    providerId: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onSubmitted: () -> Unit
) {
    val context = LocalContext.current
    var ratingStars by remember { mutableStateOf(5) }
    var commentText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("تقييمك لمقدم الخدمة:", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..5).forEach { star ->
                    IconButton(
                        onClick = { ratingStars = star },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text(if (star <= ratingStars) "⭐" else "☆", fontSize = 16.sp)
                    }
                }
            }

            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("اكتب تجربتك أو رأيك هنا...", fontSize = 11.sp, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = themeColors.accent,
                    unfocusedBorderColor = Color.DarkGray
                ),
                maxLines = 2
            )

            Button(
                onClick = {
                    if (commentText.trim().isBlank()) {
                        Toast.makeText(context, "يرجى كتابة تعليقك أولاً", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSending = true
                    val r = RatingEntity(
                        id = "rev_" + System.currentTimeMillis(),
                        targetId = providerId,
                        targetType = "PROVIDER",
                        rating = ratingStars.toFloat(),
                        comment = commentText.trim(),
                        userName = "عميل تطبيق دليل اليمن",
                        timestamp = System.currentTimeMillis()
                    )
                    viewModel.addRating(r)
                    Toast.makeText(context, "✅ شكرًا لك! تم إرسال تقييمك بنجاح.", Toast.LENGTH_SHORT).show()
                    isSending = false
                    onSubmitted()
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSending,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("إرسال التقييم 🚀", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProviderReviewsListDialog(
    provider: ProviderEntity,
    themeColors: VisualThemePalette,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val allRatingsState = viewModel.ratings.collectAsState()
    val providerRatings = remember(allRatingsState.value, provider.id) {
        allRatingsState.value.filter { it.targetId == provider.id }
    }

    var selectedRating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(12.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("💬 الآراء والتجارب لـ ${provider.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("التقييم العام: ${String.format("%.1f", provider.rating)} / 5.0 (إجمالي ${providerRatings.size} رأي وتجربة حقيقية)", fontSize = 11.sp, color = Color.LightGray)

                if (providerRatings.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📝 لا توجد آراء أو تجارب مسجلة حالياً.", fontSize = 11.5.sp, color = Color.LightGray, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("كن أول من يشارك رأيه وتجربته مع هذا المقدم!", fontSize = 10.5.sp, color = themeColors.accent)
                        }
                    }
                } else {
                    providerRatings.forEach { rev ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (rev.userName.isNotBlank()) rev.userName else "عميل مجهول",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.accent
                                    )
                                    Text(getStarsString(rev.rating), color = Color.Yellow, fontSize = 11.sp)
                                }
                                if (rev.comment.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(rev.comment, fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

                Text("✍️ إضافة رأيك وتجربتك:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { selectedRating = star },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(
                                text = if (star <= selectedRating) "⭐" else "☆",
                                fontSize = 18.sp
                            )
                        }
                    }
                    Text("($selectedRating / 5)", fontSize = 11.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = reviewComment,
                    onValueChange = { reviewComment = it },
                    placeholder = { Text("اكتب رأيك وتجربتك بالتفصيل هنا...", fontSize = 11.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.Gray
                    ),
                    maxLines = 3
                )

                Button(
                    onClick = {
                        if (reviewComment.trim().isBlank()) {
                            Toast.makeText(context, "يرجى كتابة ملاحظتك أو تجربتك أولاً", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        val newRating = RatingEntity(
                            id = "rev_" + System.currentTimeMillis(),
                            targetId = provider.id,
                            targetType = "PROVIDER",
                            rating = selectedRating.toFloat(),
                            comment = reviewComment.trim(),
                            userName = "عميل تطبيق دليل اليمن",
                            timestamp = System.currentTimeMillis()
                        )
                        viewModel?.addRating(newRating)
                        Toast.makeText(context, "شكرًا لك! تم إرسال رأيك وتجربتك بنجاح.", Toast.LENGTH_LONG).show()
                        reviewComment = ""
                        isSubmitting = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إرسال الرأي والتجربة 🚀", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إغلاق ❌", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun GenericEntityReviewsDialog(
    title: String,
    rating: Float,
    numReviews: Int,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("💬 الآراء والتجارب لـ $title", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Text("التقييم العام: ${String.format("%.1f", rating)} / 5.0 (إجمالي $numReviews تقييم)", fontSize = 10.5.sp, color = Color.LightGray)

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📝 لا توجد آراء أو تجارب مسجلة حالياً لهذا القسم.", fontSize = 11.sp, color = Color.LightGray)
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق ❌", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

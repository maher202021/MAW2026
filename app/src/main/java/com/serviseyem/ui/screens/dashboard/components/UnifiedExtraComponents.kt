package com.serviseyem.ui.screens.dashboard.components

import androidx.activity.compose.rememberLauncherForActivityResult
import com.serviseyem.ui.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 📝 Unified Edit Details Card
 */
@Composable
fun UnifiedEditDetailsCard(
    title: String,
    fields: List<Triple<String, String, (String) -> Unit>>, // Label, Value, OnChange
    onSaveClick: () -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.25f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.accent
            )
            
            fields.forEach { (label, value, onValueChange) ->
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(label, fontSize = 10.5.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.accent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = themeColors.accent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
            
            Button(
                onClick = onSaveClick,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("حفظ التحديثات 💾", color = Color.Black, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * ⭐ Unified Reviews Section & Reply Dialog
 */

/**
 * 🗑️ Unified Delete Confirmation
 */
@Composable
fun UnifiedDeleteConfirmation(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    themeColors: VisualThemePalette
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF5350)
                )
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Text(message, fontSize = 11.sp, color = Color.LightGray)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
            ) {
                Text("تأكيد الحذف 🗑️", fontSize = 11.sp, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("تراجع", fontSize = 11.sp, color = Color.LightGray)
            }
        }
    )
}

/**
 * 📸 Unified Image Picker with Coil
 */

/**
 * ⏳ Unified Loading Indicator
 */

/**
 * 📭 Unified Empty State Component
 */

/**
 * 👑 Professional Dashboard Top Header (غلاف + صورة شخصية متداخلة + أزرار سريعة + شارة التوثيق)
 */
@Composable
fun ProfessionalDashboardHeader(
    account: com.serviseyem.data.UnifiedBusinessAccount,
    subtitle: String,
    isVerified: Boolean,
    isServiceActive: Boolean,
    onToggleServiceActive: (Boolean) -> Unit,
    onEditProfileClick: () -> Unit,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    themeColors: VisualThemePalette,
    coverUrl: String = "",
    avatarUrl: String = ""
) {
    val effectiveCover = coverUrl.ifBlank { account.rawProvider?.coverImage ?: account.rawStore?.coverImage ?: "" }
    val effectiveAvatar = avatarUrl.ifBlank { account.rawProvider?.profileImage ?: account.rawStore?.logoImage ?: account.logoImage }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B))
    ) {
        // Cover Container (Height 145dp) with overlapping avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
        ) {
            if (effectiveCover.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = effectiveCover,
                    contentDescription = "غلاف الحساب",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFF0F172A))
                            )
                        )
                )
            }

            // Top action bar overlay over cover
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = if (isVerified) Color(0xFF10B981) else Color(0xFFF59E0B),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isVerified) "موثق ✓" else "قيد المراجعة ⏳",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Text("↗️", fontSize = 14.sp)
                    }
                }
            }

            // Overlapping Avatar Circle at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp)
                    .offset(y = 26.dp)
                    .size(68.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color(0xFF0F172A))
                    .border(2.5.dp, themeColors.accent, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (effectiveAvatar.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = effectiveAvatar,
                        contentDescription = "الصورة الشخصية",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Text(
                        text = account.name.take(1).ifBlank { "⭐" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Info & Action Buttons row below avatar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 92.dp, top = 6.dp, end = 12.dp, bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name.ifBlank { "لوحة التحكم الاحترافية" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = themeColors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Rating + Quick Action Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Star Rating
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text("⭐", fontSize = 10.sp)
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", if (account.rating > 0) account.rating else 4.9),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text("(${account.reviewsCount.coerceAtLeast(12)})", fontSize = 9.5.sp, color = Color.Gray)
                    }
                }

                // Edit Profile Button
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onEditProfileClick() }
                ) {
                    Text(
                        text = "✏️ تعديل الملف",
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Service Status Toggle
                Surface(
                    color = if (isServiceActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF5350).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isServiceActive) Color(0xFF10B981) else Color(0xFFEF5350)),
                    modifier = Modifier.clickable { onToggleServiceActive(!isServiceActive) }
                ) {
                    Text(
                        text = if (isServiceActive) "🟢 استقبال الطلبات: نشط" else "🔴 الاستقبال: متوقف",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isServiceActive) Color(0xFF10B981) else Color(0xFFEF5350),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 📊 Professional Quick Stats (شبكة 2×2 إحصائيات الأداء السريع)
 */
@Composable
fun ProfessionalQuickStatsGrid(
    todayOrdersCount: Int,
    overallRating: Number,
    activeOffersCount: Int,
    approxRevenue: String,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📈 مؤشرات الأداء الحية اليوم", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Surface(color = Color(0xFF10B981).copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                    Text("محدث لحظياً ⚡", fontSize = 9.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Card 1: Today Orders
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("طلبات اليوم", fontSize = 10.sp, color = Color.Gray)
                        Text("$todayOrdersCount طلب", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Card 2: Rating
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("التقييم العام", fontSize = 10.sp, color = Color.Gray)
                        val rVal = overallRating.toDouble()
                        Text("⭐ ${String.format(java.util.Locale.US, "%.1f", if (rVal > 0) rVal else 4.9)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Card 3: Active Offers
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("العروض النشطة", fontSize = 10.sp, color = Color.Gray)
                        Text("$activeOffersCount عروض", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    }
                }

                // Card 4: Est. Revenue
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("الإيرادات التقديرية", fontSize = 10.sp, color = Color.Gray)
                        Text(approxRevenue.ifBlank { "نشاط متصاعد" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399), maxLines = 1)
                    }
                }
            }
        }
    }
}

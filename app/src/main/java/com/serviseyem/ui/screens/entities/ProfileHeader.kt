package com.serviseyem.ui.screens.entities

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.components.SmartAsyncImage
import com.serviseyem.utils.VisualThemePalette

@Composable
fun ProfileHeader(
    entityName: String,
    entityType: ProfileEntityType,
    entityAddress: String,
    ratingValue: Float,
    reviewsCount: Int,
    profileCover: String,
    entityDescription: String,
    isVerified: Boolean,
    isVip: Boolean,
    isOwner: Boolean,
    bookingsCount: Int,
    completedRevenue: Double,
    themeColors: VisualThemePalette,
    workingHours: String = "9:00 ص - 10:00 م",
    onMapClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, Color(0xFF1E2E4E))
    ) {
        Column {
            // Cover & Hero image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(themeColors.primary, themeColors.secondary)
                        )
                    )
            ) {
                val cover = if (profileCover.isNullOrBlank()) "" else profileCover
                SmartAsyncImage(
                    model = cover,
                    contentDescription = entityName,
                    modifier = Modifier.fillMaxSize(),
                    fallbackEmoji = "🏙️"
                )
                // Type Tag
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .background(entityType.badgeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = entityType.labelAr,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info Body
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name & Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entityName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "موثق رسميًا",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (isVip) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFA000), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("VIP", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Rating Badge (⭐ 5.0 (42))
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, Color(0xFF1E2E4E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(String.format(java.util.Locale.US, "%.1f", ratingValue), color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text(" ($reviewsCount)", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                    }
                }

                // Description
                if (entityDescription.isNotBlank()) {
                    Text(
                        text = entityDescription,
                        fontSize = 11.5.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                }

                // Metadata Row: Location + Hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📍", fontSize = 12.sp)
                        Text(
                            text = entityAddress,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (workingHours.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⏰", fontSize = 12.sp)
                            Text(
                                text = com.serviseyem.utils.formatArabicWorkingHours(workingHours),
                                fontSize = 11.sp,
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // صندوق بيانات موثوق مع شارة التوثيق وزر الخرائط
                Surface(
                    color = Color(0xFF0F172A).copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isVerified) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                            Text(if (isVerified) "🛡️" else "📋", fontSize = 15.sp)
                            Column {
                                Text(
                                    text = if (isVerified) "نشاط تجاري موثق ومعتمد" else "بيانات نشاط مسجل في الدليل",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "موقع دقيق على خريطة Vela Map المفتوحة",
                                    fontSize = 9.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        Button(
                            onClick = onMapClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("🗺️ الخريطة", fontSize = 9.5.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // شريط عروض وتخفيضات متحرك
                Surface(
                    color = Color(0xFF7C2D12).copy(alpha = 0.35f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFEA580C).copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🔥", fontSize = 13.sp)
                        Text(
                            text = "عروض وتخفيضات حصرية خاصة متوفرة الآن لدى هذا المحل!",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDBA74)
                        )
                    }
                }

                Text(
                    text = entityDescription,
                    fontSize = 12.5.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 17.sp
                )

                // Owner Business Statistics Section
                if (isOwner) {
                    HorizontalDivider(color = themeColors.accent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "📊 إحصائيات الأداء (خاصة بك كمالك):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.accent
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatMetricCard("الحجوزات", "$bookingsCount حجز", Modifier.weight(1f), themeColors)
                        StatMetricCard("التقييمات", "$reviewsCount تقييم", Modifier.weight(1f), themeColors)
                        if (entityType == ProfileEntityType.TECHNICIAN || entityType == ProfileEntityType.STORE || entityType == ProfileEntityType.RESTAURANT) {
                            StatMetricCard("الإيرادات", "${completedRevenue.toInt()} ر.ي", Modifier.weight(1.2f), themeColors, Color(0xFF10B981))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    themeColors: VisualThemePalette,
    valueColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

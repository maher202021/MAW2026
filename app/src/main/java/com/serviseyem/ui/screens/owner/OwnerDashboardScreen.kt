package com.serviseyem.ui.screens.owner

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

@Composable
fun OwnerDashboardScreen(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onNavigateTab: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(themeColors.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                }
                Column {
                    Text(
                        text = account.name.ifBlank { "لوحة تحكم المالك" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${account.businessType.icon} ${account.businessType.titleArabic} • ID: ${account.id.take(8)}",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }
            }

            Surface(
                color = if (account.isVerified) Color(0xFF10B981) else Color(0xFFF59E0B),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (account.isVerified) "حساب موثق ⚡" else "قيد التوثيق",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        val allBookings by viewModel.bookings.collectAsState()
        val myBookings = remember(allBookings, account) {
            allBookings.filter { it.providerId == account.id || it.customerPhone == account.phone }
        }
        val visitorsCount = (myBookings.size * 3 + 12)
        val bookingsCount = myBookings.size
        val ratingVal = if (account.rating > 0.0f) account.rating.toDouble() else 4.8
        val ratingCount = account.numReviews.coerceAtLeast(myBookings.size)
        val revenueYer = myBookings.filter { it.status == "COMPLETED" || it.status == "ACCEPTED" }
            .sumOf { it.totalAmount }
            .takeIf { it > 0.0 } ?: 0.0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 4 Stats Cards Grid
            Text("📊 الإحصائيات والأداء اليومي:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Card 1: Visitors
                StatCard(
                    title = "عدد الزوار",
                    value = "$visitorsCount",
                    unit = "زائر",
                    icon = "👥",
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )

                // Card 2: Bookings
                StatCard(
                    title = "الحجوزات/الطلبات",
                    value = "$bookingsCount",
                    unit = "طلب مسجل",
                    icon = "📋",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Card 3: Ratings
                StatCard(
                    title = "التقييم العام",
                    value = "%.1f".format(ratingVal),
                    unit = "/ 5 ($ratingCount تقييم)",
                    icon = "⭐",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )

                // Card 4: Revenue
                StatCard(
                    title = "الإيرادات التقديرية",
                    value = "%,.0f".format(revenueYer),
                    unit = "YER",
                    icon = "💰",
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = Color.Gray.copy(alpha = 0.2f))

            // 4 Quick Actions Grid
            Text("⚡ الأعمال والعمليات السريعة:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionButton(
                    title = "إضافة منتج/خدمة",
                    icon = "➕",
                    bgColor = Color(0xFF10B981),
                    onClick = { onNavigateTab(1) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionButton(
                    title = "إضافة عرض جديد",
                    icon = "🎁",
                    bgColor = Color(0xFFF59E0B),
                    onClick = { onNavigateTab(2) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionButton(
                    title = "تعديل الأسعار",
                    icon = "🏷️",
                    bgColor = Color(0xFF3B82F6),
                    onClick = { onNavigateTab(3) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionButton(
                    title = "عرض التقييمات",
                    icon = "⭐",
                    bgColor = Color(0xFFEC4899),
                    onClick = { onNavigateTab(4) },
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = Color.Gray.copy(alpha = 0.2f))

            // Management Navigation Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚙️ خيارات الإدارة الشاملة:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onNavigateTab(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                            border = BorderStroke(1.dp, themeColors.accent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("المنتجات 🛒", fontSize = 11.sp, color = themeColors.accent)
                        }

                        Button(
                            onClick = { onNavigateTab(5) },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("الصور والمعرض 🖼️", fontSize = 11.sp, color = Color(0xFF3B82F6))
                        }

                        Button(
                            onClick = { onNavigateTab(6) },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                            border = BorderStroke(1.dp, Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("الملف الشخصي 👤", fontSize = 11.sp, color = Color(0xFF10B981))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 10.sp, color = Color.LightGray)
                Text(icon, fontSize = 16.sp)
            }
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(unit, fontSize = 9.sp, color = Color.Gray)
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: String,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(10.dp),
        modifier = modifier.height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

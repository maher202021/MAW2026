package com.serviseyem.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.serviseyem.ui.viewmodels.AdminViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 📊 AdminAnalyticsPanel
 * لوحة التحليلات والإحصائيات الشاملة للإدارة، توزيع المحافظات، القطاعات، الإيرادات والنمو
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsPanel(
    onBack: () -> Unit = {},
    adminViewModel: AdminViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val numberFormat = remember { DecimalFormat("#,###", DecimalFormatSymbols(Locale.US)) }
    var selectedPeriod by remember { mutableStateOf("MONTH") } // TODAY, WEEK, MONTH, YEAR

    if (adminViewModel == null) return
    val allBookings = adminViewModel.appState._bookings.collectAsState().value
    val allPayments = adminViewModel.appState._payments.collectAsState().value
    val allProviders = adminViewModel.appState._providers.collectAsState().value
    val allStores = adminViewModel.appState._stores.collectAsState().value
    val allUsersCount = adminViewModel.appState._registeredUsersCount.collectAsState().value

    val now = System.currentTimeMillis()
    val periodStartTimestamp = remember(selectedPeriod) {
        when (selectedPeriod) {
            "TODAY" -> now - (now % (24 * 60 * 60 * 1000L))
            "WEEK" -> now - (7L * 24 * 60 * 60 * 1000L)
            "MONTH" -> now - (30L * 24 * 60 * 60 * 1000L)
            "YEAR" -> now - (365L * 24 * 60 * 60 * 1000L)
            else -> 0L
        }
    }

    val filteredBookings = remember(allBookings, periodStartTimestamp) {
        if (periodStartTimestamp == 0L) allBookings else allBookings.filter { it.createdAt >= periodStartTimestamp }
    }
    val periodBookingsCount = filteredBookings.size
    val periodRevenue = remember(allPayments, periodStartTimestamp) {
        val list = if (periodStartTimestamp == 0L) allPayments else allPayments.filter { it.createdAt >= periodStartTimestamp }
        list.sumOf { it.amount }
    }

    val totalProvidersCount = allProviders.size
    val totalDisplayUsers = if (allUsersCount > 0) allUsersCount else (allProviders.size + allStores.size)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Row
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("📊 لوحة الإحصائيات ومؤشرات الأداء", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text("متابعة حية للنمو، القطاعات، والعمليات في كافة المحافظات", fontSize = 10.5.sp, color = Color.Gray)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            adminViewModel.loadSystemStats()
                            Toast.makeText(context, "تم تحديث البيانات والإحصائيات 🔄", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = Color(0xFFF59E0B))
                    }
                    IconButton(
                        onClick = {
                            adminViewModel.exportReport("الإحصائيات والتحليلات")
                            Toast.makeText(context, "تم تصدير التقرير التحليلي بنجاح 📋", Toast.LENGTH_LONG).show()
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "تصدير", tint = Color(0xFFF59E0B))
                    }
                }
            }
        }

        // شريط الفترات الزمنية
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val periods = listOf(
                "TODAY" to "اليوم",
                "WEEK" to "هذا الأسبوع",
                "MONTH" to "هذا الشهر",
                "YEAR" to "هذا العام"
            )
            items(periods) { (key, label) ->
                val isSelected = selectedPeriod == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedPeriod = key },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF59E0B),
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color.White
                    )
                )
            }
        }

        // بطاقات الأرقام القياسية الرئيسية (Grid of Stats)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // إجمالي المستخدمين
                StatMetricCard(
                    title = "إجمالي المستخدمين",
                    value = "$totalDisplayUsers",
                    trend = "",
                    icon = Icons.Default.Person,
                    bgColor = Color(0xFFE0F2FE),
                    iconColor = Color(0xFF0288D1),
                    modifier = Modifier.weight(1f)
                )
                // الفنيين المعتمدين
                StatMetricCard(
                    title = "الفنيين المعتمدين",
                    value = "$totalProvidersCount",
                    trend = "",
                    icon = Icons.Default.CheckCircle,
                    bgColor = Color(0xFFE8F5E9),
                    iconColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // إجمالي الحجوزات والطلبات للفترة
                StatMetricCard(
                    title = "حجوزات الفترة",
                    value = "$periodBookingsCount",
                    trend = "",
                    icon = Icons.Default.List,
                    bgColor = Color(0xFFFFF3E0),
                    iconColor = Color(0xFFE65100),
                    modifier = Modifier.weight(1f)
                )
                // الإيرادات والعمولات للفترة
                StatMetricCard(
                    title = "إيرادات الفترة",
                    value = "${numberFormat.format(periodRevenue)} ر.ي",
                    trend = "",
                    icon = Icons.Default.Star,
                    bgColor = Color(0xFFF3E5F5),
                    iconColor = Color(0xFF7B1FA2),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // مخطط التوزيع حسب المحافظات اليمنية
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📍 التوزيع الجغرافي (المحافظات)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color.White
                    )
                    Text("بيانات فعلية حية", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                val allCityOccurrences = (allProviders.map { it.area.ifBlank { it.cityId } } +
                    allStores.map { it.cityId.ifBlank { it.localNeighborhood } }).filter { it.isNotBlank() }
                val totalLocCount = allCityOccurrences.size.coerceAtLeast(1)

                val sanaaCount = allCityOccurrences.count { it.contains("صنعاء", ignoreCase = true) || it.contains("sana", ignoreCase = true) }
                val adenCount = allCityOccurrences.count { it.contains("عدن", ignoreCase = true) || it.contains("aden", ignoreCase = true) }
                val taizCount = allCityOccurrences.count { it.contains("تعز", ignoreCase = true) || it.contains("taiz", ignoreCase = true) }
                val ibbCount = allCityOccurrences.count { it.contains("إب", ignoreCase = true) || it.contains("اب", ignoreCase = true) }
                val hadhramoutCount = allCityOccurrences.count { it.contains("حضرموت", ignoreCase = true) || it.contains("المكلا", ignoreCase = true) || it.contains("سيئون", ignoreCase = true) }

                val governorates = listOf(
                    Triple("أمانة العاصمة / صنعاء", (sanaaCount.toFloat() / totalLocCount).coerceIn(0f, 1f), "${((sanaaCount.toFloat() / totalLocCount) * 100).toInt()}%"),
                    Triple("عدن", (adenCount.toFloat() / totalLocCount).coerceIn(0f, 1f), "${((adenCount.toFloat() / totalLocCount) * 100).toInt()}%"),
                    Triple("تعز", (taizCount.toFloat() / totalLocCount).coerceIn(0f, 1f), "${((taizCount.toFloat() / totalLocCount) * 100).toInt()}%"),
                    Triple("إب", (ibbCount.toFloat() / totalLocCount).coerceIn(0f, 1f), "${((ibbCount.toFloat() / totalLocCount) * 100).toInt()}%"),
                    Triple("حضرموت (المكلا / سيئون)", (hadhramoutCount.toFloat() / totalLocCount).coerceIn(0f, 1f), "${((hadhramoutCount.toFloat() / totalLocCount) * 100).toInt()}%")
                )

                governorates.forEach { (city, progress, percent) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(city, fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.Medium)
                            Text(percent, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = Color(0xFFF59E0B),
                            trackColor = Color.DarkGray
                        )
                    }
                }
            }
        }

        // مخطط توزيع القطاعات والخدمات
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "🔧 التوزيع حسب القطاعات والخدمات",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                val catStats = adminViewModel.getCategoryStats()
                val maintCount = catStats.maintenanceCount
                val storeCount = catStats.storeCount
                val restCount = catStats.restaurantCount
                val medCount = catStats.medicalCount
                val totalCat = (maintCount + storeCount + restCount + medCount).coerceAtLeast(1)

                val categories = listOf(
                    Triple("خدمات الصيانة والكهرباء ⚡", (maintCount.toFloat() / totalCat).coerceIn(0f, 1f), Color(0xFF38BDF8)),
                    Triple("المتاجر وقطع الغيار 🛍️", (storeCount.toFloat() / totalCat).coerceIn(0f, 1f), Color(0xFF4ADE80)),
                    Triple("المطاعم وتوصيل الوجبات 🍽️", (restCount.toFloat() / totalCat).coerceIn(0f, 1f), Color(0xFFFB923C)),
                    Triple("الرعاية الصحية والمراكز 🏥", (medCount.toFloat() / totalCat).coerceIn(0f, 1f), Color(0xFFF87171))
                )

                categories.forEach { (cat, progress, color) ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(cat, fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.Medium)
                            Text("${(progress * 100).toInt()}%", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = color)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = color,
                            trackColor = color.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    trend: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(bgColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(trend, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, fontSize = 10.5.sp, color = Color.Gray)
        }
    }
}

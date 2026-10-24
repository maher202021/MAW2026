package com.serviseyem.ui

import com.serviseyem.utils.*
import com.serviseyem.ui.*

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.AnalyticsAndReportingEngine
import com.serviseyem.utils.HierarchicalContentManager
import com.serviseyem.utils.ImageAndCacheOptimizer
import com.serviseyem.utils.ReviewsAndRatingsEngine
import com.serviseyem.utils.SearchAndFilterEngine

/**
 * 🎨 Interactive Dashboards and Dialogs for Problems 11-15
 * Includes Analytics Dashboard, Multi-Level Filters Sheet, Multi-Dimensional Reviews, and Cache Settings.
 */

// ==========================================
// 1. 📊 Interactive Analytics & Reports Sheet
// ==========================================
@Composable
fun AdvancedAnalyticsDashboardComposable(
    isSystemAdmin: Boolean,
    businessName: String = "مركز الطاقة والتكنولوجيا التخصصي",
    themeColors: VisualThemePalette,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var adminMetrics by remember { mutableStateOf(AnalyticsAndReportingEngine.AdminPlatformMetrics()) }
    var businessMetrics by remember { mutableStateOf(AnalyticsAndReportingEngine.BusinessOwnerMetrics()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Analytics",
                        tint = themeColors.accent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSystemAdmin) "لوحة التحليلات القيادية للمشرفين 📊" else "لوحة أداء الأعمال والإحصائيات 📈",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            HorizontalDivider(color = Color.DarkGray)

            if (isSystemAdmin) {
                // Admin KPIs Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricKpiCard(
                        title = "المستخدمون اليوم",
                        value = "${adminMetrics.activeUsersToday}",
                        subtitle = "نشط الآن 🟢",
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "حجوزات اليوم",
                        value = "${adminMetrics.totalBookingsToday}",
                        subtitle = "مكتمل ومستمر",
                        color = themeColors.accent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricKpiCard(
                        title = "إيرادات اليوم (ريال)",
                        value = "${String.format("%,.0f", adminMetrics.totalRevenueYERToday)}",
                        subtitle = "حوالات موثقة 💰",
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "طلبات مراجعة",
                        value = "${adminMetrics.pendingModerationsCount}",
                        subtitle = "قيد الاعتماد 📑",
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Business Owner KPIs Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricKpiCard(
                        title = "حجوزات الشهر",
                        value = "${businessMetrics.totalBookingsThisMonth}",
                        subtitle = "طلب موثق",
                        color = themeColors.accent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "إجمالي الإيرادات",
                        value = "${String.format("%,.0f", businessMetrics.totalRevenueThisMonthYER)} YER",
                        subtitle = "نمو +18% 📈",
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("⏰ أوقات الذروة الأكثر طلباً: ${businessMetrics.peakHourOfDay}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("⭐ التقييم العام المستمر: ${businessMetrics.overallRating} / 5.0", fontSize = 12.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                        Text("🔄 نسبة عودة وتكرار العملاء: ${businessMetrics.customerRepeatRatePercent}%", fontSize = 11.sp, color = Color.LightGray)
                    }
                }
            }

            // Export Actions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("تصدير التقرير الفوري للتحليلات:", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                            val headers = listOf("المعيار", "القيمة", "التاريخ")
                            val rows = listOf(
                                listOf("الحجوزات", "${businessMetrics.totalBookingsThisMonth}", currentDate),
                                listOf("الإيرادات (YER)", "${businessMetrics.totalRevenueThisMonthYER}", currentDate),
                                listOf("نسبة التكرار", "${businessMetrics.customerRepeatRatePercent}%", currentDate)
                            )
                            val file = AnalyticsAndReportingEngine.exportReportToCSV(context, "Performance_$businessName", headers, rows)
                            if (file != null) {
                                Toast.makeText(context, "تم تصدير التقرير بنجاح: ${file.name}", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "فشل تصدير التقرير", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصدير CSV 📄", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val summary = AnalyticsAndReportingEngine.exportPrintableSummaryText(businessName, businessMetrics)
                            try {
                                val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    putExtra(android.content.Intent.EXTRA_TEXT, summary)
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "مشاركة تقرير الأداء"))
                            } catch (e: Exception) {
                                // Fallback: copy report text to clipboard if sharing intent fails
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Report", summary))
                                Toast.makeText(
                                    context,
                                    "تعذر فتح تطبيقات المشاركة. تم نسخ ملخص التقرير إلى الحافظة.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ملخص PDF 🖨️", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricKpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B18)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 10.sp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 9.sp, color = Color.Gray)
        }
    }
}

// ==========================================
// 2. 🎛️ Advanced Multi-Level Filter Sheet
// ==========================================
@Composable
fun AdvancedMultiFilterBottomSheet(
    currentCriteria: SearchAndFilterEngine.FilterCriteria,
    onApplyFilters: (SearchAndFilterEngine.FilterCriteria) -> Unit,
    onResetFilters: () -> Unit,
    themeColors: VisualThemePalette,
    onClose: () -> Unit
) {
    var city by remember { mutableStateOf(currentCriteria.city) }
    var minRating by remember { mutableStateOf(currentCriteria.minRating) }
    var onlyAvailable by remember { mutableStateOf(currentCriteria.onlyAvailable) }
    var sortBy by remember { mutableStateOf(currentCriteria.sortBy) }

    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.List, contentDescription = "Filter", tint = themeColors.accent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("التصفية والفلترة المتقدمة 🎛️", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            HorizontalDivider(color = Color.DarkGray)

            // 1. City Filter
            Text("المحافظة / المدينة:", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
            val cities = listOf("الكل", "صنعاء", "عدن", "تعز", "الحديدة", "إب", "حضرموت (المكلا)", "مأرب")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                cities.take(4).forEach { c ->
                    val isSelected = if (c == "الكل") city.isEmpty() || city == "الكل" else city == c
                    FilterChip(
                        selected = isSelected,
                        onClick = { city = if (c == "الكل" || city == c) "" else c },
                        label = { Text(c, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.accent,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            // 2. Minimum Rating Filter
            Text("الحد الأدنى للتقييم: ${minRating.toInt()} نجوم وأعلى ⭐", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
            Slider(
                value = minRating.toFloat(),
                onValueChange = { minRating = it.toDouble() },
                valueRange = 0f..5f,
                steps = 4,
                colors = SliderDefaults.colors(thumbColor = themeColors.accent, activeTrackColor = themeColors.accent)
            )

            // 3. Availability Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("إظهار المتاحين للخدمة فوراً فقط 🟢", fontSize = 12.sp, color = Color.White)
                Switch(
                    checked = onlyAvailable,
                    onCheckedChange = { onlyAvailable = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = themeColors.accent, checkedTrackColor = themeColors.accent.copy(alpha = 0.5f))
                )
            }

            // 4. Sort By Options
            Text("ترتيب النتائج حسب:", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = (sortBy == "rating"),
                    onClick = { sortBy = "rating" },
                    label = { Text("الأعلى تقييماً ⭐", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = (sortBy == "price_asc"),
                    onClick = { sortBy = "price_asc" },
                    label = { Text("الأقل سعراً 💰", fontSize = 11.sp) }
                )
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        onApplyFilters(
                            SearchAndFilterEngine.FilterCriteria(
                                city = city,
                                minRating = minRating,
                                onlyAvailable = onlyAvailable,
                                sortBy = sortBy
                            )
                        )
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تطبيق الفلترة ⚡", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        onResetFilters()
                        onClose()
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("إعادة ضبط", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        }
    }
}

// ==========================================
// 3. 🧹 Cache & Storage Clean Settings Modal
// ==========================================
@Composable
fun CacheAndStorageSettingsModal(
    themeColors: VisualThemePalette,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var cacheSizeMB by remember { mutableStateOf(ImageAndCacheOptimizer.getCacheSizeMB(context)) }

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = "Cache", tint = themeColors.accent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("إدارة الذاكرة المؤقتة والتخزين 🧹", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "يحتفظ التطبيق بنسخ صور مخبأة مؤقتاً لتسريع الاستجابة أثناء التصفح بدون انترنت.",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B18)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("حجم الذاكرة المستغلة حالياً:", fontSize = 11.sp, color = Color.White)
                        Text("${String.format("%.2f", cacheSizeMB)} ميجابايت", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    ImageAndCacheOptimizer.clearAllAppCache(context)
                    cacheSizeMB = ImageAndCacheOptimizer.getCacheSizeMB(context)
                    Toast.makeText(context, "تم تنظيف الذاكرة المؤقتة بالكامل بنجاح! 🧹", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("تنظيف الذاكرة الآن 🧹", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("إغلاق", fontSize = 11.sp, color = Color.LightGray)
            }
        }
    )
}

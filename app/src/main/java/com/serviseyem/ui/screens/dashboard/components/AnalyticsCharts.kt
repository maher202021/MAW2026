package com.serviseyem.ui.screens.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import com.serviseyem.ui.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.ReportExporter
import com.serviseyem.utils.VisualThemePalette
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 📊 AnalyticsCharts (لوحة تحليلات وإحصاءات الأداء والنمو)
 * رسوم بيانية ومؤشرات تفاعلية للأرباح، دعم تعدد العملات (YER, SAR, USD)، وتصدير التقارير المالية CSV.
 */
@Composable
fun AnalyticsCharts(
    themeColors: VisualThemePalette,
    businessName: String = "حساب الأعمال",
    stats: com.serviseyem.domain.entities.DashboardStatsEntity? = null,
    totalRevenueYer: Double = 0.0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var timePeriod by remember { mutableStateOf("أسبوعي") } // أسبوعي / شهري / سنوي
    var selectedCurrency by remember { mutableStateOf("YER") } // YER, SAR, USD

    val baseIncomeYer = if (totalRevenueYer > 0.0) totalRevenueYer else (stats?.totalRevenueYer ?: 0.0)
    val numberFormat = remember { DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.US)) }

    val (convertedIncome, currencySymbol) = remember(selectedCurrency, baseIncomeYer) {
        when (selectedCurrency) {
            "SAR" -> (baseIncomeYer / 420.0) to "ر.س"
            "USD" -> (baseIncomeYer / 1600.0) to "$"
            else -> baseIncomeYer to "ر.ي"
        }
    }

    val weeklyData = listOf(
        "السبت" to 0.45f,
        "الأحد" to 0.70f,
        "الإثنين" to 0.55f,
        "الثلاثاء" to 0.90f,
        "الأربعاء" to 0.80f,
        "الخميس" to 1.00f,
        "الجمعة" to 0.65f
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF10B981))
                    Text(
                        text = "التحليلات ومؤشرات الأداء",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(onClick = {
                    val exportItems = listOf(
                        "إيرادات الطلبات المباشرة" to (convertedIncome * 0.65),
                        "إيرادات الاشتراكات والخدمات" to (convertedIncome * 0.25),
                        "إيرادات العروض الترويجية" to (convertedIncome * 0.10)
                    )
                    val result = ReportExporter.exportFinancialReportToCsv(
                        context = context,
                        accountName = businessName,
                        totalIncome = convertedIncome,
                        currency = currencySymbol,
                        items = exportItems
                    )
                    result.onSuccess { file ->
                        ReportExporter.shareExportedFile(context, file, "تقرير الأداء المالي - $businessName")
                    }
                }) {
                    Icon(Icons.Default.Share, contentDescription = "تصدير CSV", tint = Color(0xFF00E5FF))
                }
            }

            // Currency & Time Period Switchers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Currency Switcher
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("YER" to "ريال يمني", "SAR" to "سعودي", "USD" to "دولار").forEach { (code, label) ->
                        FilterChip(
                            selected = selectedCurrency == code,
                            onClick = { selectedCurrency = code },
                            label = { Text(label, fontSize = 10.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Time Period Filter
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("أسبوعي", "شهري").forEach { period ->
                        FilterChip(
                            selected = timePeriod == period,
                            onClick = { timePeriod = period },
                            label = { Text(period, fontSize = 10.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }

            // Key KPI Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF334155),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("إجمالي الإيرادات", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("${numberFormat.format(convertedIncome)} $currencySymbol", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        Text("+18.4% نمو", fontSize = 10.sp, color = Color(0xFF10B981))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF334155),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("الطلبات والحجوزات", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("142 طلب", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                        Text("98.2% اكتمال", fontSize = 10.sp, color = Color(0xFF00E5FF))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF334155),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("تقييم العملاء", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("★ 4.9", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        Text("230 مراجعة", fontSize = 10.sp, color = Color(0xFFF59E0B))
                    }
                }
            }

            // Interactive Bar Chart
            Text(
                text = "حجم النشاط والطلبات اليومية:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCBD5E1)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { (day, ratio) ->
                    val animatedHeight by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = tween(durationMillis = 800),
                        label = "bar_anim"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                            color = if (ratio >= 0.85f) Color(0xFF00E5FF) else Color(0xFF38BDF8),
                            modifier = Modifier
                                .width(18.dp)
                                .height((80 * animatedHeight).dp)
                        ) {}
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = day,
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}


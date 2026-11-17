package com.serviseyem.ui.screens.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.ui.screens.map.utils.MapDistanceCalculator
import com.serviseyem.ui.screens.map.utils.OfflineMapManager
import com.serviseyem.utils.VisualThemePalette
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 🗺️ RoutePlannerDialog
 * تخطيط مسار متعدد المحطات بين أي مدن يمنية مع حساب ديناميكي للمسافة وزمن الرحلة وترتيب المحطات محلياً.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoutePlannerDialog(
    userLat: Double,
    userLng: Double,
    currentCity: String,
    themeColors: VisualThemePalette,
    onCalculatePlannedRoute: (startCity: String, stops: List<String>, destCity: String) -> Unit,
    onDismiss: () -> Unit
) {
    val allCityNames = remember { OfflineMapManager.allCities().map { it.nameAr } }
    val resolvedStart = remember(currentCity, allCityNames) {
        allCityNames.firstOrNull { it.contains(currentCity) || currentCity.contains(it) } ?: "صنعاء"
    }

    var startCity by remember { mutableStateOf(resolvedStart) }
    var destinationCity by remember { mutableStateOf(if (resolvedStart == "عدن") "صنعاء" else "عدن") }
    var selectedStops by remember { mutableStateOf(listOf("ذمار", "تعز")) }
    var startMenuExpanded by remember { mutableStateOf(false) }
    var destMenuExpanded by remember { mutableStateOf(false) }

    val popularIntermediate = remember {
        listOf("ذمار", "إب", "تعز", "مأرب", "الحديدة", "سيئون", "عتق (شبوة)")
    }

    val routeMetrics = remember(startCity, selectedStops, destinationCity) {
        val orderedPoints = buildList {
            add(OfflineMapManager.getCityCoordinates(startCity))
            selectedStops
                .filter { it != startCity && it != destinationCity }
                .forEach { add(OfflineMapManager.getCityCoordinates(it)) }
            add(OfflineMapManager.getCityCoordinates(destinationCity))
        }
        var straightMeters = 0.0
        for (i in 0 until orderedPoints.size - 1) {
            straightMeters += MapDistanceCalculator.calculateDistanceMeters(
                orderedPoints[i].latitude,
                orderedPoints[i].longitude,
                orderedPoints[i + 1].latitude,
                orderedPoints[i + 1].longitude
            )
        }
        // Yemeni mountainous terrain winding factor ~1.30x
        val roadKm = (straightMeters / 1000.0) * 1.30
        val totalMinutes = max(10, ((roadKm / 58.0) * 60.0).roundToInt())
        val hrs = totalMinutes / 60
        val mins = totalMinutes % 60
        val etaStr = if (hrs > 0) "$hrs ساعة و $mins دقيقة" else "$mins دقيقة"
        roadKm to etaStr
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Polyline,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "تخطيط مسار متعدد المحطات 🗺️",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "يدعم جميع المحافظات اليمنية وترتيب المحطات محلياً",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                // Point 1 & 3: Start & Destination Dropdown Selectors (All 22 Yemeni Cities)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { startMenuExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("1️⃣ الانطلاق", fontSize = 9.5.sp, color = Color(0xFF00E5FF))
                                Text(startCity, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "اختيار مدينة الانطلاق", tint = Color(0xFF00E5FF))
                        }
                        DropdownMenu(
                            expanded = startMenuExpanded,
                            onDismissRequest = { startMenuExpanded = false }
                        ) {
                            allCityNames.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        startCity = city
                                        selectedStops = selectedStops.filterNot { it == city }
                                        startMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { destMenuExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.8f)),
                            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("3️⃣ الوجهة", fontSize = 9.5.sp, color = Color(0xFF10B981))
                                Text(destinationCity, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "اختيار مدينة الوجهة", tint = Color(0xFF10B981))
                        }
                        DropdownMenu(
                            expanded = destMenuExpanded,
                            onDismissRequest = { destMenuExpanded = false }
                        ) {
                            allCityNames.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        destinationCity = city
                                        selectedStops = selectedStops.filterNot { it == city }
                                        destMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Point 2: Intermediate Multi-Stop Selection
                Text(
                    text = "2️⃣ محطات التوقف الوسيطة (اختر واحدة أو أكثر):",
                    color = Color(0xFFF59E0B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    popularIntermediate
                        .filter { it != startCity && it != destinationCity }
                        .forEach { stop ->
                            val isSelected = stop in selectedStops
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedStops = if (isSelected) {
                                        selectedStops - stop
                                    } else {
                                        selectedStops + stop
                                    }
                                },
                                label = { Text(stop, fontSize = 10.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFF59E0B),
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color.White
                                )
                            )
                        }
                }

                val activeStops = selectedStops.filter { it != startCity && it != destinationCity }
                val routeChainText = if (activeStops.isEmpty()) {
                    "$startCity ⬅️ $destinationCity"
                } else {
                    "$startCity ⬅️ ${activeStops.joinToString(" ⬅️ ")} ⬅️ $destinationCity"
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🛣️ خط السير: $routeChainText",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "المسافة المقدرة: ~${String.format(Locale.US, "%.0f", routeMetrics.first)} كم • زمن الرحلة: ~${routeMetrics.second}",
                            fontSize = 11.sp,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }

                // Optimize multi-stop order locally
                if (activeStops.size > 1) {
                    OutlinedButton(
                        onClick = {
                            val startCoord = OfflineMapManager.getCityCoordinates(startCity)
                            val destCoord = OfflineMapManager.getCityCoordinates(destinationCity)
                            val waypoints = activeStops.map { name ->
                                val c = OfflineMapManager.getCityCoordinates(name)
                                MapDistanceCalculator.Waypoint(name, c.latitude, c.longitude)
                            }
                            val opt = MapDistanceCalculator.optimizeWaypoints(
                                startCoord.latitude,
                                startCoord.longitude,
                                waypoints,
                                destCoord.latitude,
                                destCoord.longitude
                            )
                            selectedStops = opt.map { it.name }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ترتيب المحطات تلقائياً لأقصر مسار ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        onCalculatePlannedRoute(startCity, activeStops, destinationCity)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                ) {
                    Icon(Icons.Default.Route, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("رسم وتطبيق المسار المخطط على الخريطة 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        }
    }
}

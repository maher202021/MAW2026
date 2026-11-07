package com.serviseyem.ui.screens.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.serviseyem.ui.screens.map.utils.YemeniNavigationHub
import com.serviseyem.utils.VisualThemePalette

/**
 * 🗺️ RoutePlannerDialog
 * 🎖️ الفكرة الخامسة: "الخريطة كأداة تخطيط للمسارات والمحطات" (Multi-Stop Route Planner)
 * يتيح للمستخدم التخطيط لزيارة عدة فنيين أو متاجر في خط سير واحد دون الحاجة لـ Google Directions API.
 */
@Composable
fun RoutePlannerDialog(
    userLat: Double,
    userLng: Double,
    currentCity: String,
    themeColors: VisualThemePalette,
    onCalculatePlannedRoute: (startCity: String, stops: List<String>, destCity: String) -> Unit,
    onDismiss: () -> Unit
) {
    var startCity by remember { mutableStateOf(currentCity.ifBlank { "صنعاء" }) }
    var destinationCity by remember { mutableStateOf("عدن") }
    var selectedStop by remember { mutableStateOf("ذمار") }

    val cities = listOf("صنعاء", "عدن", "تعز", "الحديدة", "إب", "ذمار", "المكلا", "مأرب")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                text = "حساب المسار وتوزيع المحطات مجاناً 100% دون خوادم خارجية",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray)
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.15f))

                // Point 1: Start
                Text("1️⃣ نقطة الانطلاق الأولى:", color = Color.LightGray, fontSize = 11.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    cities.take(4).forEach { c ->
                        FilterChip(
                            selected = startCity == c,
                            onClick = { startCity = c },
                            label = { Text(c, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF),
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                // Point 2: Middle Stop
                Text("2️⃣ محطة التوقف في الطريق (لزيارة فني أو متجر):", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("ذمار", "إب", "تعز", "يريم").forEach { stop ->
                        FilterChip(
                            selected = selectedStop == stop,
                            onClick = { selectedStop = stop },
                            label = { Text(stop, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF59E0B),
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                // Point 3: Final Destination
                Text("3️⃣ الوجهة النهائية:", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("عدن", "المكلا", "الحديدة", "مأرب").forEach { dest ->
                        FilterChip(
                            selected = destinationCity == dest,
                            onClick = { destinationCity = dest },
                            label = { Text(dest, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🛣️ خط السير المخطط: $startCity ⬅️ $selectedStop ⬅️ $destinationCity",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "المسافة الإجمالية المقدرة: ~385 كم • زمن الرحلة: ~6 ساعات ونصف",
                            fontSize = 11.sp,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }

                Button(
                    onClick = {
                        onCalculatePlannedRoute(startCity, listOf(selectedStop), destinationCity)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Icon(Icons.Default.Route, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("رسم وتطبيق المسار المخطط على الخريطة 🚀", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        }
    }
}

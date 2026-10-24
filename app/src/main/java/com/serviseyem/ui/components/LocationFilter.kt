package com.serviseyem.ui.components

import androidx.compose.foundation.clickable
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 📍 LocationFilter (محدد ومصفّي النطاق الجغرافي والمدن)
 * يتيح تصفية المحتوى والخدمات حسب المحافظات والمدن اليمنية أو مسافة القرب الجغرافي (GPS Radius).
 */
@Composable
fun LocationFilter(
    selectedCity: String,
    onCityChanged: (String) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var searchRadiusKm by remember { mutableFloatStateOf(15f) }

    val yemenCities = listOf(
        "كافة المدن",
        "صنعاء",
        "عدن",
        "تعز",
        "إب",
        "حضرموت (المكلا)",
        "حضرموت (سيئون)",
        "الحديدة",
        "مأرب",
        "ذمار",
        "عمران",
        "لحج",
        "أبين",
        "شبوة",
        "صعدة"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1E293B),
        shadowElevation = 2.dp,
        modifier = modifier.clickable { showDialog = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = selectedCity,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444))
                    Text("تحديد المدينة ونطاق البحث")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "اختر المحافظة أو المدينة لعرض الخدمات المتاحة بالقرب منك:",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    // Radius slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نطاق البحث الجغرافي:", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            Text("${searchRadiusKm.toInt()} كم", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                        }
                        Slider(
                            value = searchRadiusKm,
                            onValueChange = { searchRadiusKm = it },
                            valueRange = 5f..100f,
                            steps = 19,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00E5FF),
                                activeTrackColor = Color(0xFF00E5FF)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // Cities Chips Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(yemenCities) { city ->
                            val isCurrent = selectedCity == city
                            FilterChip(
                                selected = isCurrent,
                                onClick = {
                                    onCityChanged(city)
                                    showDialog = false
                                },
                                label = { Text(city, fontSize = 11.sp) },
                                leadingIcon = if (isCurrent) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

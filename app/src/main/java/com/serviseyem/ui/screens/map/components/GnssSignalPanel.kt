package com.serviseyem.ui.screens.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.SatelliteAlt
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
import androidx.compose.ui.window.Dialog
import com.serviseyem.utils.GnssSatelliteInfo
import com.serviseyem.utils.GnssStatusSummary
import com.serviseyem.utils.VisualThemePalette

/**
 * 🛰️ GnssSignalPanel - Live GNSS Satellite & GPS Diagnostic Overlay Modal
 * Displays connected satellites, signal power (C/N0 dB-Hz), constellation types, and location fix accuracy.
 * 100% Free & Local Native Android Implementation.
 */
@Composable
fun GnssSignalPanel(
    statusSummary: GnssStatusSummary,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A), // Dark Luxury Slate Surface
            border = BorderStroke(1.5.dp, themeColors.accent.copy(alpha = 0.6f)),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
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
                            imageVector = Icons.Default.SatelliteAlt,
                            contentDescription = "GNSS Satellites",
                            tint = themeColors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "لوحة تشخيص إشارة GNSS 🛰️",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "تتبع الأقمار الصناعية بالوقت الفعلي (بدون إنترنت)",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.Gray
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.15f))

                // Summary Stats Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حالة الإشارة:",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = statusSummary.qualityRating,
                                color = themeColors.accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatBox("إجمالي الأقمار 🛰️", "${statusSummary.totalSatellites}", themeColors.accent)
                            StatBox("الأقمار النشطة ⚡", "${statusSummary.usedSatellites}", Color(0xFF34D399))
                            StatBox("معدل الإشارة C/N0", "${statusSummary.averageCn0.toInt()} dB-Hz", Color(0xFF60A5FA))
                        }
                    }
                }

                // Constellations Summary Badges
                val constellationCounts = remember(statusSummary.satellites) {
                    statusSummary.satellites.groupBy { it.constellationName }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Text(
                            text = "الأقمار المكتشفة في الأفق (${statusSummary.satellites.size}):",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    if (statusSummary.satellites.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "جاري الاتصال بالأقمار الصناعية... تأكد من تواجدك تحت السماء 🌤️",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(statusSummary.satellites) { sat ->
                            SatelliteRow(sat = sat, themeColors = themeColors)
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إغلاق اللوحة ⚡", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text = label, color = Color.LightGray, fontSize = 9.5.sp)
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun SatelliteRow(sat: GnssSatelliteInfo, themeColors: VisualThemePalette) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.7f))
            .border(
                BorderStroke(
                    0.8.dp,
                    if (sat.usedInFix) Color(0xFF10B981) else Color.White.copy(alpha = 0.1f)
                ),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (sat.usedInFix) Color(0xFF10B981) else Color.Gray)
            )
            Text(
                text = sat.constellationName,
                color = Color.White,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "#${sat.svid}",
                color = Color.LightGray,
                fontSize = 9.5.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Signal Bar
            val signalRatio = (sat.cn0DbHz / 45f).coerceIn(0f, 1f)
            val barColor = when {
                sat.cn0DbHz >= 30f -> Color(0xFF10B981)
                sat.cn0DbHz >= 20f -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            }

            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.DarkGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(signalRatio)
                        .background(barColor)
                )
            }

            Text(
                text = "${sat.cn0DbHz.toInt()} dB-Hz",
                color = barColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

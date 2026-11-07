package com.serviseyem.ui.screens.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.utils.OsmContributionManager
import com.serviseyem.utils.VisualThemePalette

/**
 * 🗺️ AddOsmLandmarkDialog
 * نافذة إضافة معلم يمني جديد لتوأمة وتحسين OpenStreetMap محلياً
 */
@Composable
fun AddOsmLandmarkDialog(
    userLat: Double,
    userLng: Double,
    currentCity: String,
    themeColors: VisualThemePalette,
    onSaveLandmark: (OsmContributionManager.LandmarkNode) -> Unit,
    onDismiss: () -> Unit
) {
    var landmarkName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("مسجد 🕌") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("مسجد 🕌", "سبيل / ماء 💧", "سوق شعبي 🛒", "محطة وقود ⛽", "ورشة / بنشر 🔧")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Color(0xFF10B981)),
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
                            imageVector = Icons.Default.AddLocationAlt,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "إضافة معلم يمني للخريطة 🗺️",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "مساهمة مفتوحة المصدر بدون إنترنت أو قيود",
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

                OutlinedTextField(
                    value = landmarkName,
                    onValueChange = { landmarkName = it },
                    label = { Text("اسم المعلم (مثلاً: جامع التقوى، بئر الخير)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("نوع المعلم:", color = Color.LightGray, fontSize = 11.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية (اختياري)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "📍 الإحداثيات المسجلة: ${String.format("%.4f", userLat)}, ${String.format("%.4f", userLng)} ($currentCity)",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp
                )

                Button(
                    onClick = {
                        if (landmarkName.isNotBlank()) {
                            onSaveLandmark(
                                OsmContributionManager.LandmarkNode(
                                    name = landmarkName.trim(),
                                    category = selectedCategory,
                                    latitude = userLat,
                                    longitude = userLng,
                                    city = currentCity,
                                    notes = notes.trim()
                                )
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text("حفظ المعلم والمساهمة في الخريطة 🌟", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

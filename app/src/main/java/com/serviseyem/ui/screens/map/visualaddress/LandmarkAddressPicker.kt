package com.serviseyem.ui.screens.map.visualaddress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.ui.components.BespokePremiumSystem
import com.serviseyem.ui.theme.*

/**
 * 🗺️ LandmarkAddressPicker
 * واجهة سهلة وسريعة لاختيار العنوان البصري الموثوق في اليمن
 */
@Composable
fun LandmarkAddressPicker(
    city: String,
    initialAddress: VisualAddress? = null,
    onAddressSelected: (VisualAddress) -> Unit,
    onDismiss: () -> Unit
) {
    val landmarks = remember(city) { LandmarkRepository.getLandmarksForCity(city) }
    var selectedLandmark by remember { mutableStateOf(landmarks.firstOrNull()) }
    var selectedDirection by remember { mutableStateOf(initialAddress?.direction ?: OffsetDirection.NORTH) }
    var distanceMeters by remember { mutableStateOf(initialAddress?.approximateDistanceMeters ?: 200) }
    var visualNotes by remember { mutableStateOf(initialAddress?.visualNotes ?: "") }

    val presetDistances = listOf(50, 100, 200, 300, 500, 800, 1000)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardSurface,
            border = BorderStroke(1.5.dp, CardBorder),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = CircleShape, color = GoldShine.copy(alpha = 0.2f), modifier = Modifier.size(36.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏛️", fontSize = 18.sp)
                            }
                        }
                        Column {
                            Text("العنوان البصري اليمني", color = TextLightMint, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Text("حدد معلمك القريب لضمان وصول دقيق 100%", color = TextMutedMint, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "إغلاق", tint = TextMutedMint)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Choose Landmark
                Text("1. اختر أقرب معلم معروف في مدينتك:", color = TextLightMint, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(landmarks) { lm ->
                        val isSelected = selectedLandmark?.id == lm.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) DarkCyanPrimary else AppBackground,
                            border = BorderStroke(1.dp, if (isSelected) GoldShine else CardBorder),
                            modifier = Modifier.clickable { selectedLandmark = lm }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(lm.iconEmoji, fontSize = 14.sp)
                                Text(
                                    lm.name,
                                    color = if (isSelected) ButtonLightText else TextLightMint,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Choose Distance
                Text("2. المسافة التقريبية من المعلم:", color = TextLightMint, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetDistances) { d ->
                        val isSelected = distanceMeters == d
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) DarkCyanPrimary else AppBackground,
                            border = BorderStroke(1.dp, if (isSelected) GoldShine else CardBorder),
                            modifier = Modifier.clickable { distanceMeters = d }
                        ) {
                            Text(
                                "$d م",
                                color = if (isSelected) ButtonLightText else TextLightMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Choose Direction
                Text("3. الاتجاه من المعلم:", color = TextLightMint, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(OffsetDirection.values()) { dir ->
                        val isSelected = selectedDirection == dir
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) DarkCyanPrimary else AppBackground,
                            border = BorderStroke(1.dp, if (isSelected) GoldShine else CardBorder),
                            modifier = Modifier.clickable { selectedDirection = dir }
                        ) {
                            Text(
                                dir.labelArabic,
                                color = if (isSelected) ButtonLightText else TextLightMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Visual Notes
                Text("4. وصف بصري إضافي (مبنى، لون، لافتة):", color = TextLightMint, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = visualNotes,
                    onValueChange = { visualNotes = it },
                    placeholder = { Text("مثال: بجوار صيدلية الأمل، العمارة البيضاء الدور الثاني", color = TextMutedMint, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkCyanPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextLightMint,
                        unfocusedTextColor = TextLightMint
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Button
                Button(
                    onClick = {
                        val lm = selectedLandmark ?: landmarks.first()
                        val computed = LandmarkOffsetCalculator.calculateDestinationCoordinates(
                            lm.latitude,
                            lm.longitude,
                            distanceMeters.toDouble(),
                            selectedDirection
                        )
                        val address = VisualAddress(
                            landmarkId = lm.id,
                            landmarkName = lm.name,
                            cityId = lm.cityId,
                            direction = selectedDirection,
                            approximateDistanceMeters = distanceMeters,
                            visualNotes = visualNotes,
                            calculatedLatitude = computed.first,
                            calculatedLongitude = computed.second
                        )
                        onAddressSelected(address)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCyanPrimary)
                ) {
                    Text("حفظ العنوان البصري ✓", color = ButtonLightText, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

package com.serviseyem.ui.screens.map.components

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.serviseyem.data.ProviderEntity
import com.serviseyem.ui.screens.map.utils.MapDistanceCalculator
import com.serviseyem.utils.VisualThemePalette

/**
 * 📅 MapBookingDialog
 * Direct booking dialog directly from the map screen with notes and estimated arrival time
 */
@Composable
fun MapBookingDialog(
    provider: ProviderEntity,
    userLat: Double,
    userLng: Double,
    onDismiss: () -> Unit,
    onConfirmBooking: (notes: String) -> Unit,
    themeColors: VisualThemePalette
) {
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val distanceMeters = MapDistanceCalculator.calculateDistanceMeters(userLat, userLng, provider.latitude, provider.longitude)
    val distanceText = MapDistanceCalculator.formatDistance(distanceMeters)
    val etaText = MapDistanceCalculator.computeEta(distanceMeters)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("map_booking_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF00E5FF))
                    Text(
                        "طلب حجز خدمة فوري",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Provider Info Summary
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "👷 ${provider.name} (${provider.profession})",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "📍 المسافة: $distanceText ($etaText)",
                            fontSize = 11.5.sp,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }

                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("تفاصيل المشكلة أو الطلب (اختياري)", fontSize = 12.sp) },
                    placeholder = { Text("مثال: صيانة غسالة أوتوماتيك، العنوان بالتفصيل...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                    minLines = 3,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B),
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF64748B)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            isSubmitting = true
                            onConfirmBooking(notes)
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color(0xFF0F172A)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF0F172A), strokeWidth = 2.dp)
                        } else {
                            Text("تأكيد الحجز", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

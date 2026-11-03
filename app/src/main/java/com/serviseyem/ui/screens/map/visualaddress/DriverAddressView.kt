package com.serviseyem.ui.screens.map.visualaddress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.theme.*

/**
 * 🚚 DriverAddressView
 * بطاقة إرشادية للسائق ومندوب التوصيل:
 * - توجه نحو المعلم أولاً (الشارع الرئيسي/الجولة)
 * - ثم اتبع إرشادات الإزاحة البصرية (المسافة والاتجاه والوصف)
 * - شارة الشفافية غير المتصلة (Offline Ready)
 */
@Composable
fun DriverAddressView(
    visualAddress: VisualAddress,
    isOfflineReady: Boolean = true,
    onNavigateToLandmark: () -> Unit,
    onNavigateFinalTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Offline Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkCyanPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, DarkCyanPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = DarkCyanPrimary, modifier = Modifier.size(12.dp))
                        Text(
                            text = if (isOfflineReady) "الخريطة محمّلة ✓ — ملاحة بدون إنترنت عبر العنوان البصري" else "ملاحة العنوان البصري",
                            color = DarkCyanPrimary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldShine.copy(alpha = 0.15f)
                ) {
                    Text("نظام العنوان اليمني 🇾🇪", color = GoldShine, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step 1: Route to Landmark
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = CircleShape, color = DarkCyanPrimary, modifier = Modifier.size(28.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("1", color = ButtonLightText, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("المرحلة 1: الوجهة الرئيسية (المعلم الأقرب)", color = TextMutedMint, fontSize = 11.sp)
                    Text(visualAddress.landmarkName, color = TextLightMint, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
                FilledTonalButton(
                    onClick = onNavigateToLandmark,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkCyanPrimary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("ملاحة للمعلم 🚗", color = DarkCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = CardBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Step 2: Offset to exact house/store
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = CircleShape, color = GoldShine, modifier = Modifier.size(28.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("2", color = TextLightMint, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("المرحلة 2: الوصول للعنوان النهائي", color = TextMutedMint, fontSize = 11.sp)
                    Text(
                        "${visualAddress.approximateDistanceMeters} متر ${visualAddress.direction.labelArabic}",
                        color = TextLightMint,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (visualAddress.visualNotes.isNotBlank()) {
                        Text(visualAddress.visualNotes, color = TextMutedMint, fontSize = 11.sp)
                    }
                }
                FilledTonalButton(
                    onClick = onNavigateFinalTarget,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = GoldShine.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("الهدف 🎯", color = GoldShine, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

package com.serviseyem.ui.screens.register.status

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.BookingEntity
import com.serviseyem.utils.VisualThemePalette

/**
 * 📅 StatusBookingsSection - قسم الحجوزات والطلبات الموجهة للمزود المعتمد
 */
@Composable
fun StatusBookingsSection(
    bookings: List<BookingEntity>,
    onAcceptBooking: (String) -> Unit,
    onRejectBooking: (String) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📅 طلبات الحجز والعمل الموجهة لك:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = themeColors.accent
        )

        if (bookings.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📭",
                        fontSize = 32.sp
                    )
                    Text(
                        text = "لا توجد طلبات حجز موجهة لك حالياً",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "تأكد من إبقاء حالتك النشطة \"متاح للعمل فوراً\" لتلقي طلبات مباشرة من زوار الدليل في منطقتك.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            bookings.forEach { b ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("العميل: ${b.customerName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = when (b.status) {
                                    com.serviseyem.utils.BookingStatus.PENDING.code -> "⏳ بانتظار تأكيدك"
                                    "APPROVED", com.serviseyem.utils.BookingStatus.ACCEPTED.code, com.serviseyem.utils.BookingStatus.IN_PROGRESS.code -> "🟢 مقبول وجاري التنفيذ"
                                    com.serviseyem.utils.BookingStatus.REJECTED.code -> "❌ مرفوض"
                                    com.serviseyem.utils.BookingStatus.COMPLETED.code -> "✅ مكتمل"
                                    else -> b.status
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (b.status) {
                                    com.serviseyem.utils.BookingStatus.PENDING.code -> Color.Yellow
                                    "APPROVED", com.serviseyem.utils.BookingStatus.ACCEPTED.code, com.serviseyem.utils.BookingStatus.IN_PROGRESS.code, com.serviseyem.utils.BookingStatus.COMPLETED.code -> Color.Green
                                    else -> Color.Red
                                }
                            )
                        }
                        Text("📞 هاتف العميل للتواصل: ${b.customerPhone}", fontSize = 10.sp, color = themeColors.accent)
                        Text("📍 الموقع والمحافظة: ${b.customerArea}", fontSize = 10.sp, color = Color.LightGray)
                        Text("🔧 الخدمة المطلوبة: ${b.serviceType}", fontSize = 10.sp, color = Color.LightGray)
                        Text("⏰ الموعد: ${b.dateString} - ${b.timeString}", fontSize = 10.sp, color = Color.LightGray)

                        if (b.status == com.serviseyem.utils.BookingStatus.PENDING.code) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { onAcceptBooking(b.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("موافقة وقبول العمل ✅", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { onRejectBooking(b.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("اعتذار ورفض العمل ❌", fontSize = 9.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

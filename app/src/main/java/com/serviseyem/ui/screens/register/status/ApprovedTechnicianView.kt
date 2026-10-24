package com.serviseyem.ui.screens.register.status

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.NotificationEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.utils.VisualThemePalette

/**
 * ✅ ApprovedTechnicianView - عرض شاشة الفني المعتمد مع لوحة التحكم والحالة والحجوزات والإشعارات
 */
@Composable
fun ApprovedTechnicianView(
    provider: ProviderEntity,
    categoryName: String,
    bookings: List<BookingEntity>,
    notifications: List<NotificationEntity>,
    onToggleAvailability: () -> Unit,
    onAcceptBooking: (String) -> Unit,
    onRejectBooking: (String) -> Unit,
    onOpenChatWithCustomer: (String, String) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.5.dp, Color(0xFF10B981))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF10B981).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✅", fontSize = 28.sp)
            }

            Text(
                text = "🎉 تم تفعيل حسابك كفني معتمد!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981),
                textAlign = TextAlign.Center
            )

            Text(
                text = "مرحباً بك يا غالي! حسابك نشط الآن في دليل خدمات اليمن ومتاح لجميع العملاء للتواصل والحجز المباشر.",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            // Profile Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111C15)),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👷", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = provider.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("💼 $categoryName", fontSize = 10.5.sp, color = Color.LightGray)
                        Text("📍 ${provider.area} - ${provider.localNeighborhood}", fontSize = 10.5.sp, color = Color.LightGray)
                    }
                }
            }

            // Status Toggle Button
            Button(
                onClick = onToggleAvailability,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (provider.isAvailable) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, if (provider.isAvailable) Color(0xFFEF4444) else Color(0xFF10B981)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Text(
                    text = if (provider.isAvailable) "🔴 تغيير حالتك الحالية إلى: مشغول مؤقتاً" else "🟢 تغيير حالتك الحالية إلى: متاح للعمل فوراً",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (provider.isAvailable) Color(0xFFEF4444) else Color(0xFF10B981)
                )
            }

            // Bookings Section
            StatusBookingsSection(
                bookings = bookings,
                onAcceptBooking = onAcceptBooking,
                onRejectBooking = onRejectBooking,
                themeColors = themeColors
            )

            // Notifications Section
            StatusNotificationsSection(
                notifications = notifications,
                onOpenChatWithCustomer = onOpenChatWithCustomer,
                themeColors = themeColors
            )
        }
    }
}

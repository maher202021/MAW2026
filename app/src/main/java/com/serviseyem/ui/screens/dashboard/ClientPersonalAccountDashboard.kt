@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.dashboard
import com.serviseyem.ui.*

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import com.serviseyem.data.BookingEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 👤 ClientPersonalAccountDashboard - لوحة الحساب الشخصي للعميل
 * تتيح استعراض بيانات الحساب، طلبات الحجز المباشرة، والمحادثة مع الدعم الفني
 */
@Composable
fun ClientPersonalAccountDashboard(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context,
    currentUserName: String,
    currentUserPhone: String,
    currentUserResidence: String,
    currentUserId: String,
    bookings: List<BookingEntity>,
    onShowRegistrationFormsAnyway: () -> Unit,
    onNavigateToSupportChat: () -> Unit = { viewModel.navigateTo("CHAT_SUPPORT") }
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Welcome Header
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, themeColors.accent)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(themeColors.accent.copy(alpha = 0.2f))
                        .border(2.dp, themeColors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 32.sp)
                }
                Text(
                    text = "مرحباً بك: $currentUserName",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "الحساب الشخصي والتحكم الموحد بالخدمات",
                    fontSize = 11.5.sp,
                    color = themeColors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Account Details Card
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📋 بيانات الحساب الموثقة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("📱 رقم الهاتف:", fontSize = 11.sp, color = themeColors.textSecondary)
                    Text(currentUserPhone, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("📍 منطقة السكن:", fontSize = 11.sp, color = themeColors.textSecondary)
                    Text(currentUserResidence, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Live Chat Support Button
        Button(
            onClick = { onNavigateToSupportChat() },
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("💬 محادثة فورية مباشرة مع الإدارة والدعم الفني", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        }

        // Bookings section
        Text("📅 طلبات وحجوزات الخدمة الخاصة بك:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
        val myBookings = remember(bookings, currentUserPhone) { bookings.filter { it.customerPhone == currentUserPhone } }

        if (myBookings.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "لم تقم بإجراء أي حجوزات بعد. بمجرد قيامك بالحجز، ستظهر تفاصيل الحجز هنا مباشرة.",
                    fontSize = 11.sp,
                    color = themeColors.textSecondary,
                    modifier = Modifier.padding(14.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            myBookings.forEach { b ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الطلب #${b.bookingNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                            Text(b.status, fontSize = 10.sp, color = themeColors.accent)
                        }
                        Text("الفني: ${b.providerName}", fontSize = 11.sp, color = Color.White)
                        Text("الموعد: ${b.dateString} - ${b.timeString}", fontSize = 10.sp, color = Color.LightGray)
                    }
                }
            }
        }

        // Bottom Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onShowRegistrationFormsAnyway,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.surface),
                border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Text("💼 الانضمام كشريك", color = themeColors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { viewModel.logout(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, Color.Red),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Text("🚪 تسجيل الخروج", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

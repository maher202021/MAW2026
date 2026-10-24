package com.serviseyem.ui.screens.bookings

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.BookingEntity
import com.serviseyem.utils.BookingStateMachine
import com.serviseyem.utils.BookingStatus

/**
 * 📈 BookingStatusScreen
 * شاشة استعراض مسار ومراحل تقدم الحجز التفصيلي (7 مراحل) مع الخطوات التالية والجدول الزمني.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingStatusScreen(
    booking: BookingEntity,
    onBack: () -> Unit
) {
    val steps = listOf(
        BookingStatus.PENDING,
        BookingStatus.UNDER_REVIEW,
        BookingStatus.ACCEPTED,
        BookingStatus.IN_PROGRESS,
        BookingStatus.COMPLETED,
        BookingStatus.PAID,
        BookingStatus.CLOSED
    )

    val currentStatusIndex = when (booking.status.uppercase()) {
        "PENDING" -> 0
        "UNDER_REVIEW" -> 1
        "ACCEPTED", "APPROVED" -> 2
        "IN_PROGRESS" -> 3
        "COMPLETED" -> 4
        "PAID" -> 5
        "CLOSED" -> 6
        "CANCELLED", "REJECTED" -> -1
        else -> 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "متابعة مسار الحجز",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "#${booking.bookingNumber.ifEmpty { booking.id.take(8) }}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = booking.serviceType,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val statusColor = Color(android.graphics.Color.parseColor(BookingStateMachine.getStatusColor(booking.status)))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = BookingStateMachine.getStatusLabel(booking.status),
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "👤 الفني / مقدم الخدمة: ${booking.providerName}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🗓️ الموعد المحدد: ${booking.dateString.ifEmpty { booking.date }} - ${booking.timeString.ifEmpty { booking.time }}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 7-Stage Horizontal Progress Timeline
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "مراحل تنفيذ الطلب (7 خطوات)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.forEachIndexed { index, step ->
                            val isCompleted = currentStatusIndex > index
                            val isCurrent = currentStatusIndex == index

                            val circleColor = when {
                                isCompleted -> Color(0xFF10B981) // Green
                                isCurrent -> Color(0xFF3B82F6)   // Blue
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(90.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(circleColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = step.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }

                            if (index < steps.size - 1) {
                                val lineColor = if (currentStatusIndex > index) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                                Box(
                                    modifier = Modifier
                                        .width(30.dp)
                                        .height(3.dp)
                                        .background(lineColor)
                                )
                            }
                        }
                    }
                }
            }

            // Current Step Description and Next Actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ماذا يحدث الآن؟",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val (currentExplanation, nextAction) = when (currentStatusIndex) {
                        0 -> "طلب الحجز قيد الانتظار لموافقة الفني ومراجعة التوقيت." to "سيتم إشعارك فور تأكيد الفني أو قبول الطلب."
                        1 -> "الطلب قيد المراجعة الفنية وتحديد التكاليف الأولية." to "انتظر تأكيد الموعد النهائي."
                        2 -> "تم قبول طلبك والفني يستعد للموعد في الوقت المحدد." to "سيبدأ الفني بالتحرك أو المباشرة بالعمل في موعدك."
                        3 -> "الخدمة قيد التنفيذ المباشر حالياً مع الفني." to "سيقوم الفني بتسليم العمل ووضع علامة الإنجاز."
                        4 -> "تم إنجاز الخدمة بنجاح من قبل الفني." to "يرجى تسوية الدفع وتقييم مستوى جودة الخدمة."
                        5 -> "تم استلام وسداد مستحقات الخدمة بنجاح." to "سيتم إغلاق الحجز وأرشفته في سجلك الدائم."
                        6 -> "الحجز مغلق ومؤرشف ومكتمل بالكامل." to "شكراً لاستخدامكم تطبيق الخدمات الشامل."
                        else -> "الحجز ملغي أو مرفوض." to "يمكنك تقديم طلب حجز جديد في أي وقت."
                    }

                    Text(
                        text = currentExplanation,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "👉 الخطوة القادمة: $nextAction",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Timeline logs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "سجل التحديثات والنشاط",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TimelineItem(
                        title = "إنشاء الحجز",
                        subtitle = "تم إرسال الطلب بواسطة العميل",
                        time = booking.dateString.ifEmpty { "اليوم" },
                        isFirst = true
                    )

                    if (currentStatusIndex >= 2) {
                        TimelineItem(
                            title = "قبول الحجز",
                            subtitle = "تمت الموافقة من قِبل ${booking.providerName}",
                            time = "مكتمل"
                        )
                    }

                    if (currentStatusIndex >= 3) {
                        TimelineItem(
                            title = "بدء التنفيذ",
                            subtitle = "مباشرة تقديم الخدمة في الموقع",
                            time = "مكتمل"
                        )
                    }

                    if (currentStatusIndex >= 4) {
                        TimelineItem(
                            title = "إكمال الخدمة",
                            subtitle = "تم الانتهاء من العمل المطلوب",
                            time = "مكتمل",
                            isLast = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(
    title: String,
    subtitle: String,
    time: String,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = time, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

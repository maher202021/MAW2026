@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.bookings
import com.serviseyem.ui.*

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.serviseyem.data.BookingEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.createBooking
import com.serviseyem.utils.BookingReminderService
import com.serviseyem.utils.DateFormatter
import com.serviseyem.utils.HolidayManager
import com.serviseyem.utils.ScheduleManager
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.*

/**
 * 📅 BookingCalendarScreen
 * شاشة تقويم وجدولة الحجوزات التفاعلية:
 * - تصفح الأيام والشهور العربية مع تلوين العطلات الرسمية وأيام الجمعة.
 * - اختيار الفترات الزمنية المتاحة (Time Slots) مع فحص فترات الراحة والتعارضات.
 * - دعم الحجوزات المتكررة (أسبوعي / شهري).
 * - تأكيد الحجز وإطلاق التنبيهات المسبقة (24h / 1h).
 */
@Composable
fun BookingCalendarScreen(
    provider: ProviderEntity,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    calendarViewModel: BookingCalendarViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onBookingSuccess: (BookingEntity) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bookingsList by viewModel.bookings.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val currentUserPhone by viewModel.currentUserPhone.collectAsState()

    androidx.activity.compose.BackHandler(enabled = true, onBack = onBack)

    val uiState by calendarViewModel.uiState.collectAsState()
    val calendarMonthOffset = uiState.calendarMonthOffset
    val selectedDateString = uiState.selectedDateString
    val selectedTimeSlot = uiState.selectedTimeSlot
    val recurrenceOption = uiState.recurrenceOption
    val clientNotes = uiState.clientNotes
    val clientAddress = uiState.clientAddress
    val isSubmitting = uiState.isSubmitting

    LaunchedEffect(isSubmitting) {
        if (isSubmitting) {
            delay(30_000L)
            if (calendarViewModel.uiState.value.isSubmitting) {
                calendarViewModel.setSubmitting(false)
                Toast.makeText(context, "انتهت مهلة الإرسال، يرجى التحقق من الاتصال والمحاولة مجدداً", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val calendar = remember(calendarMonthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, calendarMonthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val currentMonthYearLabel = remember(calendarMonthOffset) {
        // ✨ م2-ج3: استخدام DateFormatter
        DateFormatter.formatCustom(calendar.timeInMillis, "MMMM yyyy")
    }

    // Days in current selected month
    val daysInMonth = remember(calendarMonthOffset) {
        val days = mutableListOf<CalendarDayInfo>()
        val tempCal = calendar.clone() as Calendar
        val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, ..., 7=Saturday

        // Empty padding cells before first day
        val emptySlots = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7
        for (i in 0 until emptySlots) {
            days.add(CalendarDayInfo(dayNumber = 0, dateString = "", isHoliday = false, holidayName = null))
        }

        for (d in 1..maxDays) {
            tempCal.set(Calendar.DAY_OF_MONTH, d)
            val dStr = DateFormatter.formatDateDash(tempCal.timeInMillis)
            val (isHol, holName) = HolidayManager.isDateHoliday(dStr, provider.id)
            days.add(CalendarDayInfo(dayNumber = d, dateString = dStr, isHoliday = isHol, holidayName = holName))
        }
        days
    }

    // Available Slots for selected day
    val availableSlots = remember(selectedDateString, provider.id, bookingsList) {
        ScheduleManager.generateAvailableSlots(selectedDateString, provider.id, bookingsList)
    }

    val (isSelectedDayHoliday, selectedHolidayName) = remember(selectedDateString, provider.id) {
        HolidayManager.isDateHoliday(selectedDateString, provider.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("جدولة موعد وحجز خدمة 📅", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text(provider.name + " (${provider.profession.ifEmpty { "فني معتمد" }})", fontSize = 11.sp, color = themeColors.accent)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = themeColors.surface)
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Month Selector Bar
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { calendarViewModel.previousMonth() },
                            enabled = calendarMonthOffset > 0
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الشهر السابق", tint = if (calendarMonthOffset > 0) Color.White else Color.DarkGray)
                        }

                        Text(
                            text = currentMonthYearLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )

                        IconButton(
                            onClick = { calendarViewModel.nextMonth() },
                            enabled = calendarMonthOffset < 6
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "الشهر القادم", tint = if (calendarMonthOffset < 6) Color.White else Color.DarkGray)
                        }
                    }
                }
            }

            // Days of Week Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val weekDays = listOf("أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
                    weekDays.forEach { wDay ->
                        Text(
                            text = wDay,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (wDay == "جمعة") Color(0xFFEF4444) else Color.LightGray,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Calendar Days Grid
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // ✨ م2-ج3: استخدام DateFormatter
                        val todayStr = remember { DateFormatter.formatDateDash(System.currentTimeMillis()) }
                        val max30Str = remember {
                            val c = Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Aden"))
                            c.add(Calendar.DAY_OF_YEAR, 30)
                            DateFormatter.formatDateDash(c.timeInMillis)
                        }
                        val chunkedDays = daysInMonth.chunked(7)
                        chunkedDays.forEach { rowDays ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                rowDays.forEach { dayInfo ->
                                    if (dayInfo.dayNumber == 0) {
                                        Spacer(modifier = Modifier.weight(1f).height(44.dp))
                                    } else {
                                            val isSelected = dayInfo.dateString == selectedDateString
                                            val isPast = dayInfo.dateString < todayStr
                                            val isBeyond30 = dayInfo.dateString > max30Str

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        when {
                                                            isSelected -> themeColors.accent
                                                            isPast || isBeyond30 -> Color.Gray.copy(alpha = 0.2f)
                                                            dayInfo.isHoliday -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                            else -> Color(0xFF0F172A).copy(alpha = 0.5f)
                                                        }
                                                    )
                                                    .clickable {
                                                        if (isPast) {
                                                            Toast.makeText(context, "لا يمكن حجز موعد في تاريخ سابق", Toast.LENGTH_SHORT).show()
                                                        } else if (isBeyond30) {
                                                            Toast.makeText(context, "لا يمكن الحجز لأكثر من 30 يوماً مقدماً", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            calendarViewModel.selectDate(dayInfo.dateString)
                                                        }
                                                    },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "${dayInfo.dayNumber}",
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 12.sp,
                                                    color = when {
                                                        isSelected -> Color.Black
                                                        dayInfo.isHoliday -> Color(0xFFEF4444)
                                                        else -> Color.White
                                                    }
                                                )
                                                if (dayInfo.isHoliday) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.Black else Color(0xFFEF4444))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                // Fill remaining spaces if row < 7
                                if (rowDays.size < 7) {
                                    for (k in 0 until (7 - rowDays.size)) {
                                        Spacer(modifier = Modifier.weight(1f).height(44.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Holiday Alert Banner if selected day is holiday
            if (isSelectedDayHoliday) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                            Column {
                                Text("تنبيه: اليوم المحدد عطلة غير متاحة", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFEF4444))
                                Text(selectedHolidayName ?: "إجازة رسمية", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Time Slots Header & Grid
            item {
                Text(
                    text = "⏰ الأوقات والفترات المتاحة ليوم ($selectedDateString):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (availableSlots.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSelectedDayHoliday) "لا تتوفر فترات حجز في أيام العطلات والإجازات." else "لا تتوفر فترات متاحة في هذا اليوم، يرجى اختيار تاريخ آخر.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableSlots) { slot ->
                            val isSelected = selectedTimeSlot?.timeString == slot.timeString
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isSelected -> themeColors.accent
                                        slot.isAvailable -> Color(0xFF1E293B)
                                        else -> Color(0xFF0F172A)
                                    }
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) themeColors.accent else if (slot.isAvailable) Color.DarkGray else Color.Red.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .clickable(enabled = slot.isAvailable) {
                                        calendarViewModel.selectTimeSlot(slot)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = slot.timeString,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = when {
                                            isSelected -> Color.Black
                                            slot.isAvailable -> Color.White
                                            else -> Color.Gray
                                        }
                                    )
                                    slot.reasonIfNotAvailable?.let { r ->
                                        Text(r, fontSize = 9.sp, color = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recurrence Options
            item {
                Text(
                    text = "🔁 تكرار الموعد الدوري (اختياري):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val recurrenceList = listOf("NONE" to "مرة واحدة 1️⃣", "WEEKLY" to "أسبوعياً 📅", "MONTHLY" to "شهرياً 🔄")
                    recurrenceList.forEach { (code, label) ->
                        val isSel = recurrenceOption == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) themeColors.accent.copy(alpha = 0.2f) else Color(0xFF1E293B))
                                .border(1.dp, if (isSel) themeColors.accent else Color.DarkGray, RoundedCornerShape(10.dp))
                                .clickable { calendarViewModel.setRecurrenceOption(code) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) themeColors.accent else Color.LightGray
                            )
                        }
                    }
                }
            }

            // Client Input Fields
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = clientAddress,
                        onValueChange = { calendarViewModel.setClientAddress(it) },
                        label = { Text("عنوانك وموقع الخدمة (المدينة، الحي، الشارع)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = clientNotes,
                        onValueChange = { calendarViewModel.setClientNotes(it) },
                        label = { Text("ملاحظات إضافية أو وصف المشكلة للفني", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Confirm Booking Button
            item {
                Button(
                    onClick = {
                        if (selectedTimeSlot == null) {
                            Toast.makeText(context, "الرجاء اختيار وقت محدد للموعد", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isSelectedDayHoliday) {
                            Toast.makeText(context, "لا يمكن الحجز في يوم عطلة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        calendarViewModel.setSubmitting(true)
                        val rawGeneratedPass = "${(1000..9999).random()}"
                        val newBooking = BookingEntity(
                            id = "book_${System.currentTimeMillis()}_${(1000..9999).random()}",
                            bookingNumber = "YEM-${(10000..99999).random()}",
                            pinCode = com.serviseyem.utils.SecureHasher.hashPin(rawGeneratedPass),
                            clientId = currentUserPhone.ifEmpty { "client_${System.currentTimeMillis()}" },
                            clientName = currentUserName.ifEmpty { "عميل معتمد" },
                            clientPhone = currentUserPhone,
                            customerName = currentUserName.ifEmpty { "عميل معتمد" },
                            customerPhone = currentUserPhone,
                            customerArea = clientAddress.ifBlank { provider.area },
                            clientAddress = clientAddress.ifBlank { provider.area },
                            serviceType = provider.profession.ifEmpty { "خدمة صيانة ومعاينة" },
                            serviceDetails = clientNotes,
                            providerId = provider.id,
                            providerName = provider.name,
                            providerPhone = provider.phone,
                            category = provider.categoryId,
                            date = selectedDateString,
                            dateString = selectedDateString,
                            time = selectedTimeSlot?.timeString.orEmpty(),
                            timeString = selectedTimeSlot?.timeString.orEmpty(),
                            status = "PENDING",
                            isRecurring = recurrenceOption != "NONE",
                            recurrenceRule = recurrenceOption,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )

                        viewModel.createBooking(newBooking) { success ->
                            calendarViewModel.setSubmitting(false)
                            if (success) {
                                // Schedule local 24h & 1h notification reminders
                                BookingReminderService.scheduleBookingReminders(context, newBooking)
                                Toast.makeText(context, "✅ تم إرسال طلب الحجز بنجاح وجدولة التنبيهات!", Toast.LENGTH_LONG).show()
                                onBookingSuccess(newBooking)
                            } else {
                                Toast.makeText(context, "فشل إنشاء الحجز، يرجى المحاولة مرة أخرى", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isSubmitting && selectedTimeSlot != null && !isSelectedDayHoliday,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                            Text("تأكيد وحجز الموعد 📅", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

private data class CalendarDayInfo(
    val dayNumber: Int,
    val dateString: String,
    val isHoliday: Boolean,
    val holidayName: String?
)

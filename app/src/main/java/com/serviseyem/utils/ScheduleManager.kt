package com.serviseyem.utils

import com.serviseyem.data.BookingEntity
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * 📅 ScheduleManager
 * إدارة الجداول الزمنية، أوقات الدوام، فترات الراحة، وتوليد المواعيد المتاحة
 * مع دعم الحجوزات المتكررة (أسبوعي / شهري) لمنع التداخل والتعارض
 */
object ScheduleManager {

    data class TimeSlot(
        val timeString: String,      // e.g. "09:00 ص", "04:00 م"
        val hour24: Int,             // e.g. 9, 16
        val isAvailable: Boolean,    // true if not booked and not during break
        val reasonIfNotAvailable: String? = null // e.g. "محجوز مسبقاً", "فترة استراحة وصلاة"
    )

    data class ProviderWorkingHours(
        val startHour: Int = 8,      // 08:00 AM
        val endHour: Int = 20,       // 08:00 PM
        val breakStartHour: Int = 13,// 01:00 PM (صلاة وغداء)
        val breakEndHour: Int = 14,  // 02:00 PM
        val slotDurationHours: Int = 1
    )

    private val providerCustomHours = mutableMapOf<String, ProviderWorkingHours>()

    fun setProviderWorkingHours(providerId: String, hours: ProviderWorkingHours) {
        providerCustomHours[providerId] = hours
    }

    fun getProviderWorkingHours(providerId: String): ProviderWorkingHours {
        return providerCustomHours[providerId] ?: ProviderWorkingHours()
    }

    /**
     * توليد الفترات الزمنية المتاحة ليوم محدد مع فحص التعارضات والعطلات
     */
    fun generateAvailableSlots(
        dateString: String,
        providerId: String,
        existingBookings: List<BookingEntity>
    ): List<TimeSlot> {
        val (isHoliday, holidayName) = HolidayManager.isDateHoliday(dateString, providerId)
        if (isHoliday) {
            return emptyList()
        }

        val hours = getProviderWorkingHours(providerId)
        val slots = mutableListOf<TimeSlot>()

        val bookedTimes = existingBookings.filter {
            it.providerId == providerId &&
            (it.date == dateString || it.dateString == dateString) &&
            it.status != "CANCELLED" && it.status != "REJECTED"
        }.map { (if (it.time.isNotBlank()) it.time else it.timeString).trim() }.toSet()

        for (h in hours.startHour until hours.endHour step hours.slotDurationHours) {
            val isBreak = h >= hours.breakStartHour && h < hours.breakEndHour
            val formattedTime = formatHourToArabicString(h)

            val isBooked = bookedTimes.any { it.contains(formattedTime) || formattedTime.contains(it) }

            val available = !isBreak && !isBooked
            val reason = when {
                isBreak -> "استراحة وصلاة 🕌"
                isBooked -> "محجوز مسبقاً"
                else -> null
            }

            slots.add(
                TimeSlot(
                    timeString = formattedTime,
                    hour24 = h,
                    isAvailable = available,
                    reasonIfNotAvailable = reason
                )
            )
        }

        return slots
    }

    /**
     * حساب المواعيد القادمة للحجز المتكرر (أسبوعياً أو شهرياً)
     */
    fun calculateRecurringDates(
        startDateString: String,
        recurrenceRule: String, // "WEEKLY", "MONTHLY"
        occurrences: Int = 4
    ): List<String> {
        if (recurrenceRule == "NONE" || recurrenceRule.isBlank()) {
            return listOf(startDateString)
        }

        val result = mutableListOf<String>()

        try {
            // ✨ م2: استخدام DateFormatter وتحليل يدوي آمن
            val parts = startDateString.split("-")
            if (parts.size != 3) return listOf(startDateString)
            val year = parts[0].toIntOrNull() ?: return listOf(startDateString)
            val month = parts[1].toIntOrNull() ?: return listOf(startDateString)
            val day = parts[2].toIntOrNull() ?: return listOf(startDateString)
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Aden")).apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
            }

            for (i in 0 until occurrences) {
                result.add(DateFormatter.formatDateDash(cal.timeInMillis))
                if (recurrenceRule.equals("WEEKLY", ignoreCase = true)) {
                    cal.add(Calendar.WEEK_OF_YEAR, 1)
                } else if (recurrenceRule.equals("MONTHLY", ignoreCase = true)) {
                    cal.add(Calendar.MONTH, 1)
                }
            }
        } catch (e: Exception) {
            result.add(startDateString)
        }

        return result
    }

    fun formatHour12(hour24: Int): String {
        return formatHourToArabicString(hour24)
    }

    private fun formatHourToArabicString(hour24: Int): String {
        val period = if (hour24 < 12) "ص" else "م"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale.US, "%02d:00 %s", hour12, period)
    }
}

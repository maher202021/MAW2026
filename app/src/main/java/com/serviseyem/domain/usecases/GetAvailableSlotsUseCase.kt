package com.serviseyem.domain.usecases

import androidx.annotation.Keep
import com.serviseyem.data.BookingEntity
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/**
 * 🎯 GetAvailableSlotsUseCase
 * Calculates available time slots for a given date and provider,
 * excluding booked slots.
 */
class GetAvailableSlotsUseCase @Inject constructor() {

    @Keep
    data class TimeSlot(
        val timeLabel: String,
        val isAvailable: Boolean,
        val slotTimestamp: Long
    )

    private val defaultStandardSlots = listOf(
        "08:00 AM" to "08:00 ص",
        "09:00 AM" to "09:00 ص",
        "10:00 AM" to "10:00 ص",
        "11:00 AM" to "11:00 ص",
        "12:00 PM" to "12:00 م",
        "02:00 PM" to "02:00 م",
        "03:00 PM" to "03:00 م",
        "04:00 PM" to "04:00 م",
        "05:00 PM" to "05:00 م",
        "06:00 PM" to "06:00 م",
        "07:00 PM" to "07:00 م",
        "08:00 PM" to "08:00 م"
    )

    operator fun invoke(
        selectedDateString: String,
        providerId: String,
        existingBookings: List<BookingEntity>
    ): List<TimeSlot> {
        val cleanDate = normalizeDateString(selectedDateString)
        val cleanProviderId = providerId.trim()

        if (cleanDate.isBlank()) {
            return emptyList()
        }

        val matchingBookingsOnDate = existingBookings.filter { booking ->
            val bProvider = booking.providerId.ifBlank { booking.technicianId }.trim()
            val bDate = normalizeDateString(booking.date.ifBlank { booking.dateString })
            (cleanProviderId.isBlank() || bProvider == cleanProviderId) &&
                bDate == cleanDate &&
                com.serviseyem.utils.BookingStateMachine.isSlotOccupiedStatus(booking.status)
        }

        val bookedTimesRaw = matchingBookingsOnDate
            .map { normalizeDigits(it.time.ifBlank { it.timeString }.trim()) }
            .filter { it.isNotEmpty() }
            .toSet()

        val bookedHours24 = bookedTimesRaw.mapNotNull { parseHour24OrNull(it) }.toSet()

        val adenTz = TimeZone.getTimeZone("Asia/Aden")
        val nowCal = Calendar.getInstance(adenTz)
        val todayStr = String.format(
            Locale.US,
            "%04d-%02d-%02d",
            nowCal.get(Calendar.YEAR),
            nowCal.get(Calendar.MONTH) + 1,
            nowCal.get(Calendar.DAY_OF_MONTH)
        )
        val isToday = cleanDate == todayStr
        val currentHour = nowCal.get(Calendar.HOUR_OF_DAY)

        val dateParts = cleanDate.split("-")
        val selYear = dateParts.getOrNull(0)?.toIntOrNull() ?: nowCal.get(Calendar.YEAR)
        val selMonth = (dateParts.getOrNull(1)?.toIntOrNull() ?: (nowCal.get(Calendar.MONTH) + 1)) - 1
        val selDay = dateParts.getOrNull(2)?.toIntOrNull() ?: nowCal.get(Calendar.DAY_OF_MONTH)

        return defaultStandardSlots.map { (enSlot, arSlot) ->
            val slotHour24 = parseHour24(enSlot)
            val isPassedToday = isToday && (slotHour24 <= currentHour)
            val isBooked = bookedHours24.contains(slotHour24) ||
                bookedTimesRaw.any { it.contains(arSlot) || it.contains(enSlot, ignoreCase = true) }

            val slotCal = Calendar.getInstance(adenTz).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.YEAR, selYear)
                set(Calendar.MONTH, selMonth.coerceIn(0, 11))
                val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                set(Calendar.DAY_OF_MONTH, selDay.coerceIn(1, maxDay))
                set(Calendar.HOUR_OF_DAY, slotHour24)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            TimeSlot(
                timeLabel = arSlot,
                isAvailable = !isBooked && !isPassedToday,
                slotTimestamp = slotCal.timeInMillis
            )
        }
    }

    private fun normalizeDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '٠'..'٩' -> sb.append((ch - '٠' + '0'.code).toChar())
                in '۰'..'۹' -> sb.append((ch - '۰' + '0'.code).toChar())
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun normalizeDateString(raw: String): String {
        val latin = normalizeDigits(raw.trim()).replace('/', '-')
        val parts = latin.split("-")
        if (parts.size == 3) {
            val p0 = parts[0].toIntOrNull()
            val p1 = parts[1].toIntOrNull()
            val p2 = parts[2].toIntOrNull()
            if (p0 != null && p1 != null && p2 != null) {
                return if (p0 > 31) {
                    val month = if (p1 > 12 && p2 <= 12) p2 else p1
                    val day = if (p1 > 12 && p2 <= 12) p1 else p2
                    String.format(Locale.US, "%04d-%02d-%02d", p0, month, day)
                } else if (p2 > 31) {
                    val month = if (p1 > 12 && p0 <= 12) p0 else p1
                    val day = if (p1 > 12 && p0 <= 12) p1 else p0
                    String.format(Locale.US, "%04d-%02d-%02d", p2, month, day)
                } else {
                    latin
                }
            }
        }
        return latin
    }

    private fun parseHour24(slotEn: String): Int {
        return parseHour24OrNull(slotEn) ?: 0
    }

    private fun parseHour24OrNull(rawTime: String): Int? {
        val norm = normalizeDigits(rawTime).uppercase(Locale.US)
        val hourToken = norm.split(":").firstOrNull()?.filter { it in '0'..'9' } ?: return null
        var hour = hourToken.toIntOrNull() ?: return null
        val isPm = norm.contains("PM") || norm.contains("مساء") || Regex("(^|\\s|\\d)م(\\s|$)").containsMatchIn(norm)
        val isAm = norm.contains("AM") || norm.contains("صباح") || Regex("(^|\\s|\\d)ص(\\s|$)").containsMatchIn(norm)
        if (isPm && !isAm && hour < 12) hour += 12
        if (isAm && !isPm && hour == 12) hour = 0
        return hour.coerceIn(0, 23)
    }
}

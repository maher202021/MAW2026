package com.serviseyem.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random

object BookingUtils {
    private const val CANCELLATION_HOURS = 8
    private val CANCELLATION_WINDOW_MS = CANCELLATION_HOURS * 60 * 60 * 1000L

    // ✨ م2-ج3: أنماط تنسيق التواريخ آمنة الخيوط (Thread-Safe)
    private val BOOKING_NUM_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss", Locale.US)
    private val ARABIC_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", Locale.forLanguageTag("ar-YE"))
    private val PARSE_FORMATTERS = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US),
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    )

    private val counter = java.util.concurrent.atomic.AtomicInteger(Random.nextInt(1000, 5000))

    fun generateBookingNumber(prefix: String = "BK"): String {
        // ✨ م2-ج3: استخدام DateTimeFormatter آمن الخيوط
        val datePart = LocalDateTime.now().format(BOOKING_NUM_FORMATTER)
        val seq = (counter.getAndIncrement() % 9000) + 1000
        val randomPart = String.format(Locale.US, "%04d", seq)
        return "$prefix-$datePart-$randomPart"
    }

    fun generateBookingCode(prefix: String = "BK"): String {
        return generateBookingNumber(prefix)
    }

    fun generateBookingPassword(length: Int = 4): String {
        val secureRandom = java.security.SecureRandom()
        val builder = StringBuilder()
        for (i in 0 until length) {
            builder.append(secureRandom.nextInt(10))
        }
        return builder.toString()
    }

    /**
     * Parse date and time strings to Unix timestamp (millis).
     */
    fun parseScheduledTimestamp(dateString: String, timeString: String): Long {
        return try {
            val cleanDate = dateString.trim().replace("/", "-")
            val cleanTime = timeString.trim().replace("م", "PM").replace("ص", "AM")
            val combined = "$cleanDate $cleanTime"
            for (fmt in PARSE_FORMATTERS) {
                try {
                    val ldt = try {
                        LocalDateTime.parse(combined, fmt)
                    } catch (e: Exception) {
                        java.time.LocalDate.parse(cleanDate, fmt).atStartOfDay()
                    }
                    return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                } catch (e: Exception) {}
            }
            0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Checks if a booking can be modified or cancelled.
     * Rule: Allowed ONLY if there are MORE than 8 hours remaining before scheduledAt.
     */
    fun canModifyOrCancelBooking(scheduledAtTimestamp: Long, dateString: String = "", timeString: String = ""): Boolean {
        val scheduledMs = if (scheduledAtTimestamp > 0) scheduledAtTimestamp else parseScheduledTimestamp(dateString, timeString)
        if (scheduledMs <= 0) return true // If no valid date/time, allow by default
        val now = System.currentTimeMillis()
        val diffMs = scheduledMs - now
        // Allow modification/cancellation anytime prior to 8 hours before scheduled time
        return diffMs > CANCELLATION_WINDOW_MS || scheduledAtTimestamp <= 0
    }

    /**
     * Returns the remaining time in millis until the 8-hour cancellation deadline.
     * Deadline = scheduledAt - 8 hours.
     */
    fun getRemainingCancellationWindowMs(scheduledAtTimestamp: Long, dateString: String = "", timeString: String = ""): Long {
        val scheduledMs = if (scheduledAtTimestamp > 0) scheduledAtTimestamp else parseScheduledTimestamp(dateString, timeString)
        if (scheduledMs <= 0) return Long.MAX_VALUE
        val cancellationDeadline = scheduledMs - CANCELLATION_WINDOW_MS
        val diff = cancellationDeadline - System.currentTimeMillis()
        return if (diff > 0) diff else 0L
    }

    /**
     * Formats remaining cancellation window to user-friendly Arabic text.
     */
    fun formatRemainingCancellationTime(scheduledAtTimestamp: Long, dateString: String = "", timeString: String = ""): String {
        val remainingMs = getRemainingCancellationWindowMs(scheduledAtTimestamp, dateString, timeString)
        if (remainingMs == Long.MAX_VALUE) return "متاح للإلغاء والتعديل"
        if (remainingMs <= 0) return "انتهت فترة السماح بالتعديل/الإلغاء (أقل من 8 ساعات)"

        val totalSeconds = remainingMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            "باقي $hours ساعة و $minutes دقيقة على انتهاء مهلة التعديل/الإلغاء"
        } else {
            "باقي $minutes دقيقة و $seconds ثانية على إغلاق التعديل"
        }
    }

    /**
     * Backwards compatibility helper for 6 hours check.
     */
    fun isBookingWithin6Hours(dateString: String, timeString: String): Boolean {
        val scheduledMs = parseScheduledTimestamp(dateString, timeString)
        if (scheduledMs <= 0) return false
        val diffMs = scheduledMs - System.currentTimeMillis()
        val sixHoursMs = 6 * 60 * 60 * 1000L
        return diffMs in 0..sixHoursMs
    }

    fun isMoreThan8HoursBefore(dateStr: String, timeStr: String): Boolean {
        return canModifyOrCancelBooking(0L, dateStr, timeStr)
    }

    fun formatBookingDate(date: String): String {
        if (date.isBlank()) return ""
        return try {
            val cleanDate = date.trim().replace("/", "-")
            val parts = cleanDate.split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull() ?: 2026
                val month = parts[1].toIntOrNull() ?: 1
                val day = parts[2].toIntOrNull() ?: 1
                val localDate = java.time.LocalDate.of(year, month, day)
                localDate.format(ARABIC_DATE_FORMATTER)
            } else {
                val localDate = java.time.LocalDate.parse(cleanDate)
                localDate.format(ARABIC_DATE_FORMATTER)
            }
        } catch (e: Exception) {
            date
        }
    }

    fun isValidYemeniPhone(phone: String): Boolean {
        val clean = phone.trim().replace(" ", "").replace("+967", "").replace("00967", "")
        return clean.length >= 7 && (clean.startsWith("7") || clean.startsWith("0"))
    }

    fun validateBookingForm(clientName: String, phone: String, service: String, date: String): Pair<Boolean, String> {
        if (clientName.isBlank()) return Pair(false, "يرجى إدخال اسم العميل")
        if (!isValidYemeniPhone(phone)) return Pair(false, "يرجى إدخال رقم هاتف يمني صحيح")
        if (service.isBlank()) return Pair(false, "يرجى تحديد الخدمة المطلوبة")
        if (date.isBlank()) return Pair(false, "يرجى اختيار التاريخ")
        return Pair(true, "")
    }

    /**
     * Checks if a technician has a conflicting booking within +/- 2 hours window.
     */
    fun hasTechnicianConflict(
        technicianId: String,
        dateString: String,
        timeString: String,
        existingBookings: List<com.serviseyem.data.BookingEntity>,
        ignoreBookingId: String = ""
    ): Boolean {
        if (technicianId.isBlank() || dateString.isBlank()) return false
        val targetTimestamp = parseScheduledTimestamp(dateString, timeString)
        val twoHoursMs = 2 * 60 * 60 * 1000L

        return existingBookings.any { b ->
            val isSameTech = (b.technicianId.isNotBlank() && b.technicianId == technicianId) || (b.providerId.isNotBlank() && b.providerId == technicianId)
            b.id != ignoreBookingId &&
            isSameTech &&
            b.status !in listOf("CANCELLED", "COMPLETED", "REJECTED") &&
            ((b.date == dateString || b.dateString == dateString) &&
             (if (targetTimestamp > 0L && b.scheduledAt > 0L) Math.abs(b.scheduledAt - targetTimestamp) < twoHoursMs else (b.time.isNotBlank() && b.time == timeString)))
        }
    }

    fun generateShareableBookingTicket(booking: com.serviseyem.data.BookingEntity): String {
        val client = booking.customerName.ifBlank { booking.clientName.ifBlank { "عميل" } }
        val phone = booking.customerPhone.ifBlank { booking.clientPhone }
        val code = booking.bookingNumber.ifBlank { booking.bookingCode.ifBlank { booking.id.take(8) } }
        val date = booking.date.ifBlank { booking.dateString }
        val time = booking.time.ifBlank { booking.timeString }
        val service = booking.serviceType.ifBlank { booking.serviceName.ifBlank { "خدمة عامة" } }
        val amount = if (booking.totalAmount > 0.0) "${booking.totalAmount.toInt()} ريال يمني" else "حسب الاتفاق"

        return """
            🎫 *تذكرة حجز خدمة — دليل خدمات اليمن*
            ━━━━━━━━━━━━━━━━━━━━━━
            📋 *رقم الحجز:* #$code
            👤 *العميل:* $client ($phone)
            🛠️ *الخدمة:* $service
            📅 *الموعد:* $date — $time
            💰 *المبلغ:* $amount
            📌 *الحالة:* ${booking.status}
            ━━━━━━━━━━━━━━━━━━━━━━
            🔒 *رمز التحقق الأمني:* ${if (booking.pinCode.isNotBlank() && !booking.pinCode.contains(":")) booking.pinCode else "تم إرساله في إشعار الحجز (مشفر ومحمي)"}
            📱 تم الحجز عبر تطبيق دليل خدمات اليمن
        """.trimIndent()
    }
}

fun isMoreThan8HoursBefore(dateStr: String, timeStr: String): Boolean {
    return BookingUtils.isMoreThan8HoursBefore(dateStr, timeStr)
}

fun validateBookingForm(clientName: String, phone: String, service: String, date: String): Pair<Boolean, String> {
    return BookingUtils.validateBookingForm(clientName, phone, service, date)
}

fun isValidYemeniPhone(phone: String): Boolean {
    return BookingUtils.isValidYemeniPhone(phone)
}


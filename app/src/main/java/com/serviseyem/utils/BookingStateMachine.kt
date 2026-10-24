package com.serviseyem.utils

import com.serviseyem.data.BookingEntity
import androidx.annotation.Keep
import java.util.Locale

/**
 * 📌 Architectural Note: State Machine BookingStatus enum defining valid transitions
 * and workflow status lifecycle logic across the domain layer.
 */
@Keep
enum class BookingStatus(
    val code: String,
    val label: String,
    val colorHex: String
) {
    PENDING("PENDING", "قيد الانتظار", "#F59E0B"),
    UNDER_REVIEW("UNDER_REVIEW", "قيد المراجعة", "#FCD34D"),
    /**
     * ⚠️ ملاحظة معمارية:
     * APPROVED هي مرادف قديم للقيمة ACCEPTED.
     * القيمة المعتمدة الجديدة هي ACCEPTED.
     * APPROVED موجودة للتوافقية فقط.
     */
    ACCEPTED("ACCEPTED", "مقبول", "#10B981"),
    REJECTED("REJECTED", "مرفوض", "#EF4444"),
    IN_PROGRESS("IN_PROGRESS", "قيد التنفيذ", "#3B82F6"),
    COMPLETED("COMPLETED", "مكتمل", "#059669"),
    PAID("PAID", "تم الدفع", "#8B5CF6"),
    CLOSED("CLOSED", "مغلق ومؤرشف", "#6B7280"),
    CANCELLED("CANCELLED", "ملغي", "#EF4444");

    companion object {
        fun fromCode(code: String): BookingStatus {
            val clean = code.trim().uppercase(Locale.ROOT).let {
                if (it == "APPROVED") "ACCEPTED" else it
            }
            return values().firstOrNull { it.code == clean } ?: PENDING
        }
    }
}

/**
 * ⚙️ BookingStateMachine
 * محرك إدارة دورة حياة وحالات الحجز وضبط الانتقالات المسموحة وقواعد الأمان والإلغاء.
 */
object BookingStateMachine {

    private val allowedTransitions = mapOf(
        "PENDING" to listOf("UNDER_REVIEW", "ACCEPTED", "REJECTED", "CANCELLED"),
        "UNDER_REVIEW" to listOf("ACCEPTED", "REJECTED", "CANCELLED", "PENDING"),
        "ACCEPTED" to listOf("IN_PROGRESS", "COMPLETED", "PAID", "CANCELLED"),
        "IN_PROGRESS" to listOf("COMPLETED", "PAID", "CANCELLED"),
        "COMPLETED" to listOf("PAID", "CLOSED"),
        "PAID" to listOf("COMPLETED", "CLOSED"),
        "CLOSED" to emptyList(),
        "CANCELLED" to emptyList(),
        "REJECTED" to emptyList()
    )

    /**
     * الحالات التي تشغل الموعد وتمنع حجز آخر في نفس التوقيت (توحيد APPROVED و ACCEPTED)
     */
    fun isSlotOccupiedStatus(status: String): Boolean {
        val s = normalizeStatus(status)
        return s in listOf("PENDING", "UNDER_REVIEW", "ACCEPTED", "IN_PROGRESS")
    }

    private fun normalizeStatus(status: String): String {
        return when (val upper = status.trim().uppercase(Locale.ROOT)) {
            "APPROVED", "CONFIRMED" -> "ACCEPTED"
            "IN_PREPARATION", "READY" -> "IN_PROGRESS"
            "DELIVERED" -> "COMPLETED"
            else -> upper
        }
    }

    /**
     * 1. التحقق من إمكانية الانتقال من الحالة الحالية للحالة الجديدة
     */
    fun canTransition(currentStatus: String, newStatus: String): Boolean {
        val curr = normalizeStatus(currentStatus)
        val target = normalizeStatus(newStatus)
        if (curr == target) return true
        val validNext = allowedTransitions[curr] ?: emptyList()
        return validNext.contains(target)
    }

    /**
     * 2. الحصول على قائمة الحالات المتاحة للانتقال إليها
     */
    fun getAvailableTransitions(currentStatus: String): List<String> {
        val curr = normalizeStatus(currentStatus)
        return allowedTransitions[curr] ?: emptyList()
    }

    /**
     * 3. الحصول على المسمى العربي للحالة
     */
    fun getStatusLabel(status: String): String {
        val norm = normalizeStatus(status)
        return try {
            BookingStatus.valueOf(norm).label
        } catch (e: Exception) {
            when (norm) {
                "ACCEPTED" -> "مقبول"
                "REJECTED" -> "مرفوض"
                else -> status
            }
        }
    }

    /**
     * 4. الحصول على كود اللون للحالة
     */
    fun getStatusColor(status: String): String {
        val norm = normalizeStatus(status)
        return try {
            BookingStatus.valueOf(norm).colorHex
        } catch (e: Exception) {
            "#F59E0B"
        }
    }

    /**
     * 5. هل الحالة نهائية لا تقبل التعديل
     */
    fun isTerminalStatus(status: String): Boolean {
        val s = normalizeStatus(status)
        return s == "CLOSED" || s == "CANCELLED" || s == "REJECTED"
    }

    /**
     * 6. التحقق من إمكانية الإلغاء (تطبيق قاعدة 8 ساعات وقفل المحاولات)
     */
    fun canCancel(booking: BookingEntity): Boolean {
        if (booking.isLocked) return false
        val normStatus = normalizeStatus(booking.status)
        if (isTerminalStatus(normStatus)) return false
        if (normStatus == "IN_PROGRESS" || normStatus == "COMPLETED" || normStatus == "PAID") {
            return false
        }

        // فحص قاعدة الـ 8 ساعات قبل موعد الحجز
        val appointmentTime = if (booking.scheduledAt > 0L) {
            booking.scheduledAt
        } else {
            parseAppointmentTimestamp(booking.effectiveDate, booking.effectiveTime)
        }
        if (appointmentTime > 0L) {
            val diffMs = appointmentTime - System.currentTimeMillis()
            val eightHoursMs = 8 * 60 * 60 * 1000L
            if (diffMs in 1..eightHoursMs) {
                // متبقي أقل من 8 ساعات على الموعد
                return false
            }
        }
        return true
    }

    /**
     * 7. الحصول على عدد محاولات الإلغاء
     */
    fun getCancelAttempts(booking: BookingEntity): Int {
        return booking.cancellationAttempts
    }

    /**
     * 8. استخراج توقيت الموعد بدقة مع دعم الصيغ العربية والإنجليزية (ص/م، AM/PM، 24 ساعة)
     */
    private fun parseAppointmentTimestamp(dateStr: String, timeStr: String): Long {
        val cleanDate = toLatinDigits(dateStr).trim()
        if (cleanDate.isBlank()) return 0L
        return try {
            val normalizedDate = normalizeDateIso(cleanDate)
            val cleanTime = toLatinDigits(timeStr).trim()
            val (hour24, minute) = parseTimeHourMinute(cleanTime)
            val normalizedFull = String.format(Locale.US, "%s %02d:%02d", normalizedDate, hour24, minute)
            val parsedNormalized = DateFormatter.parseCustom(normalizedFull, "yyyy-MM-dd HH:mm")
            if (parsedNormalized != null && parsedNormalized > 0L) {
                return parsedNormalized
            }

            val formats = listOf("yyyy-MM-dd HH:mm", "yyyy/MM/dd HH:mm", "dd/MM/yyyy HH:mm", "yyyy-MM-dd")
            val fullStr = "$cleanDate ${cleanTime.ifBlank { "00:00" }}".trim()
            for (fmt in formats) {
                val t = DateFormatter.parseCustom(fullStr, fmt)
                if (t != null) return t
            }
            DateFormatter.parseCustom(normalizedDate, "yyyy-MM-dd") ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun toLatinDigits(input: String): String {
        if (input.isEmpty()) return input
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

    private fun normalizeDateIso(rawDate: String): String {
        val parts = rawDate.replace('/', '-').split("-")
        if (parts.size == 3) {
            val p0 = parts[0].toIntOrNull()
            val p1 = parts[1].toIntOrNull()
            val p2 = parts[2].toIntOrNull()
            if (p0 != null && p1 != null && p2 != null) {
                if (p0 > 31) {
                    return String.format(Locale.US, "%04d-%02d-%02d", p0, p1.coerceIn(1, 12), p2.coerceIn(1, 31))
                } else if (p2 > 31) {
                    val month = if (p1 > 12 && p0 <= 12) p0 else p1
                    val day = if (p1 > 12 && p0 <= 12) p1 else p0
                    return String.format(Locale.US, "%04d-%02d-%02d", p2, month.coerceIn(1, 12), day.coerceIn(1, 31))
                }
            }
        }
        return rawDate
    }

    private fun parseTimeHourMinute(rawTime: String): Pair<Int, Int> {
        if (rawTime.isBlank()) return 0 to 0
        val upper = rawTime.uppercase(Locale.US)
        val timeTokens = upper.split(":")
        var hour = timeTokens.getOrNull(0)?.filter { it in '0'..'9' }?.toIntOrNull() ?: 0
        val minute = timeTokens.getOrNull(1)?.takeWhile { !it.isLetter() }?.filter { it in '0'..'9' }?.toIntOrNull() ?: 0
        val isPm = upper.contains("PM") || upper.contains("مساء") || Regex("(^|\\s|\\d)م(\\s|$)").containsMatchIn(upper)
        val isAm = upper.contains("AM") || upper.contains("صباح") || Regex("(^|\\s|\\d)ص(\\s|$)").containsMatchIn(upper)
        if (isPm && !isAm && hour < 12) hour += 12
        if (isAm && !isPm && hour == 12) hour = 0
        return hour.coerceIn(0, 23) to minute.coerceIn(0, 59)
    }
}

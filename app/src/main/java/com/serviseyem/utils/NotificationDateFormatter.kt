package com.serviseyem.utils

/**
 * ⏰ NotificationDateFormatter
 * Helper to format notification timestamps into a unified Arabic format:
 * "الثلاثاء، 25 أغسطس 2026، 02:30 م"
 * with smart relative time support for recent events.
 */
object NotificationDateFormatter {

    fun format(timestamp: Long): String {
        if (timestamp <= 0L) return "الآن"
        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp

        val seconds = diffMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            diffMillis < 0 -> DateFormatter.formatFull(timestamp)
            minutes < 1 -> "منذ لحظات"
            minutes < 60 -> "منذ $minutes دقيقة"
            hours < 24 -> "منذ $hours ساعة (${DateFormatter.formatTime(timestamp)})"
            days == 1L -> "أمس في ${DateFormatter.formatTime(timestamp)}"
            days < 7 -> "منذ $days أيام (${DateFormatter.formatTime(timestamp)})"
            else -> DateFormatter.formatFull(timestamp)
        }
    }

    fun formatFull(timestamp: Long): String {
        if (timestamp <= 0L) return "تاريخ غير محدد"
        return DateFormatter.formatFull(timestamp)
    }
}

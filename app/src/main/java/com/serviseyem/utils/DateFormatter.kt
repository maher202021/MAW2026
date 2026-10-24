package com.serviseyem.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ✨ م2: موحد لتنسيق التواريخ (thread-safe)
 * يستبدل جميع استخدامات SimpleDateFormat
 */
object DateFormatter {
    
    private val LOCALE_AR = Locale.forLanguageTag("ar-YE")
    
    // ============ الأنماط الأساسية ============
    private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern(
        "yyyy/MM/dd - hh:mm a", LOCALE_AR
    )
    
    private val SHORT_DATE_FORMAT = DateTimeFormatter.ofPattern(
        "yyyy/MM/dd", LOCALE_AR
    )
    
    private val TIME_FORMAT = DateTimeFormatter.ofPattern(
        "hh:mm a", LOCALE_AR
    )
    
    private val FULL_FORMAT = DateTimeFormatter.ofPattern(
        "EEEE، d MMMM yyyy - hh:mm a", LOCALE_AR
    )
    
    // ============ أنماط إضافية ============
    private val DATE_DASH_FORMAT = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd", LOCALE_AR
    )
    
    private val DATETIME_FORMAT = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss", LOCALE_AR
    )
    
    private val ISO_FORMAT = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd'T'HH:mm:ss", LOCALE_AR
    )
    
    private val NUMERIC_TIME_FORMAT = DateTimeFormatter.ofPattern(
        "HH:mm", LOCALE_AR
    )
    
    private val formatterCache = java.util.concurrent.ConcurrentHashMap<String, DateTimeFormatter>()
    
    private fun getOrCreateFormatter(pattern: String, locale: Locale = LOCALE_AR): DateTimeFormatter {
        val cacheKey = "$pattern-${locale.toLanguageTag()}"
        return formatterCache.getOrPut(cacheKey) {
            DateTimeFormatter.ofPattern(pattern, locale)
        }
    }
    
    // ============ دوال التنسيق ============
    
    /** 2026/01/15 - 03:30 م */
    fun formatDisplay(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(DISPLAY_FORMAT)
    }
    
    /** 2026/01/15 */
    fun formatShort(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(SHORT_DATE_FORMAT)
    }
    
    /** 2026/01/15 */
    fun formatDate(timestamp: Long): String = formatShort(timestamp)
    
    /** 2026-01-15 */
    fun formatDateDash(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(DATE_DASH_FORMAT)
    }
    
    /** 03:30 م */
    fun formatTime(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(TIME_FORMAT)
    }
    
    /** 15:30 (24-hour) */
    fun formatTime24(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(NUMERIC_TIME_FORMAT)
    }
    
    /** 2026-01-15 15:30:45 */
    fun formatDateTime(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(DATETIME_FORMAT)
    }
    
    /** 2026-01-15T15:30:45 */
    fun formatIso(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(ISO_FORMAT)
    }
    
    /** الأربعاء، 15 يناير 2026 - 03:30 م */
    fun formatFull(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(FULL_FORMAT)
    }
    
    /** تنسيق مخصص */
    fun formatCustom(timestamp: Long, pattern: String): String {
        return try {
            Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(getOrCreateFormatter(pattern))
        } catch (e: Exception) {
            ""
        }
    }

    /** تحليل تاريخ مخصص آمن */
    fun parseCustom(text: String, pattern: String): Long? {
        return try {
            val formatter = getOrCreateFormatter(pattern, Locale.US)
            if (pattern.contains("HH:mm") || pattern.contains("HH:mm:ss") || pattern.contains("hh:mm")) {
                java.time.LocalDateTime.parse(text, formatter)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            } else {
                java.time.LocalDate.parse(text, formatter)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
        } catch (e: Exception) {
            null
        }
    }
}

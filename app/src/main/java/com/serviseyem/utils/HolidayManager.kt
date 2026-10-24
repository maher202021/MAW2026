package com.serviseyem.utils

import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * 🏖️ HolidayManager
 * إدارة العطلات الرسمية اليمنية والإجازات الأسبوعية والخاصة بمزودي الخدمة
 * لمنع الحجز في الأيام غير المتاحة وتنبيه المستخدم مسبقاً
 */
object HolidayManager {

    private val customProviderHolidays = java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.CopyOnWriteArraySet<String>>() // providerId -> Set of "yyyy-MM-dd"
    private val firestoreHolidays = java.util.concurrent.ConcurrentHashMap<String, String>()

    // Official Fixed & Common Yemeni Holidays
    private val fixedHolidays = mapOf(
        "05-01" to "عيد العمال العالمي 🛠️",
        "05-22" to "عيد الوحدة اليمنية 🇾🇪",
        "09-26" to "ذكرى ثورة 26 سبتمبر 🇾🇪",
        "10-14" to "ذكرى ثورة 14 أكتوبر 🇾🇪",
        "11-30" to "عيد الاستقلال 30 نوفمبر 🇾🇪",
        "01-01" to "رأس السنة الميلادية 🎆"
    )

    suspend fun loadHolidaysFromFirestore() {
        try {
            val snapshot = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("settings")
                .document("holidays")
                .get()
                .await()
            @Suppress("UNCHECKED_CAST")
            val holidays = snapshot.get("holidays") as? Map<String, String> ?: emptyMap()
            if (holidays.isNotEmpty()) {
                firestoreHolidays.clear()
                firestoreHolidays.putAll(holidays)
            }
        } catch (e: Exception) {
            // keep default
        }
    }

    /**
     * التحقق مما إذا كان التاريخ عطلة رسمية أو يوم جمعة أو إجازة فني
     */
    fun isDateHoliday(dateString: String, providerId: String? = null): Pair<Boolean, String?> {
        try {
            // ✨ م2: تحليل التاريخ بشكل آمن وبدون SimpleDateFormat
            val cleanDate = dateString.trim().replace("/", "-")
            val parts = cleanDate.split("-")
            if (parts.size != 3) return Pair(false, null)
            val year = parts[0].toIntOrNull() ?: return Pair(false, null)
            val month = parts[1].toIntOrNull() ?: return Pair(false, null)
            val day = parts[2].toIntOrNull() ?: return Pair(false, null)
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Aden")).apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
            }

            // 1. فحص العطلات الرسمية الثابتة والديناميكية
            val allHolidays = fixedHolidays + firestoreHolidays
            val monthDay = String.format(Locale.US, "%02d-%02d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
            if (allHolidays.containsKey(monthDay)) {
                return Pair(true, allHolidays[monthDay])
            }
            if (allHolidays.containsKey(cleanDate)) {
                return Pair(true, allHolidays[cleanDate])
            }

            // 2. فحص إجازات الفني الخاصة
            if (providerId != null) {
                val providerDays = customProviderHolidays[providerId]
                if (providerDays?.contains(cleanDate) == true) {
                    return Pair(true, "إجازة خاصة لمقدم الخدمة 🏖️")
                }
            }

            // 3. فحص يوم الجمعة (Friday)
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                return Pair(true, "يوم الجمعة (عطلة أسبوعية) 🕌")
            }

            return Pair(false, null)
        } catch (e: Exception) {
            return Pair(false, null)
        }
    }

    /**
     * إضافة يوم إجازة خاص بمزود خدمة
     */
    fun addProviderHoliday(providerId: String, dateString: String) {
        val cleanDate = dateString.trim().replace("/", "-")
        val set = customProviderHolidays.getOrPut(providerId) { java.util.concurrent.CopyOnWriteArraySet() }
        set.add(cleanDate)
    }

    /**
     * إزالة يوم إجازة خاص بمزود خدمة
     */
    fun removeProviderHoliday(providerId: String, dateString: String) {
        val cleanDate = dateString.trim().replace("/", "-")
        customProviderHolidays[providerId]?.remove(cleanDate)
    }

    /**
     * جلب جميع أيام إجازة المزود
     */
    fun getProviderHolidays(providerId: String): Set<String> {
        return customProviderHolidays[providerId] ?: emptySet()
    }
}

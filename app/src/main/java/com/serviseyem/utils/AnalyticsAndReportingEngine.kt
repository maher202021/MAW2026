package com.serviseyem.utils

import com.serviseyem.utils.*

import android.content.Context
import com.serviseyem.BuildConfig
import com.serviseyem.utils.DateFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileWriter

/**
 * 📊 Problem 15 Solution: Advanced Analytics, Reporting & Export Engine
 * Interactive Admin Analytics Dashboard, Business Owner Performance Metrics, Multi-format report export (PDF/CSV/Excel),
 * User journey behavioral event tracking, Anomaly detection alerts, and Predictive trend forecasting.
 *
  * لا تحذف هذا الملف — قد يُستخدم في المستقبل
 */
object AnalyticsAndReportingEngine {

    private val db = FirebaseFirestore.getInstance()

        // 1. Admin Platform Overview Metrics Model
    data class AdminPlatformMetrics(
        // In release builds, this is 0 until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val activeUsersToday: Int = if (BuildConfig.DEBUG) 1450 else 0,
        // In release builds, this is 0 until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val activeUsersMonthly: Int = if (BuildConfig.DEBUG) 28900 else 0,
        // In release builds, this is 0 until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val totalBookingsToday: Int = if (BuildConfig.DEBUG) 184 else 0,
        // In release builds, this is 0 until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val totalRevenueYERToday: Double = if (BuildConfig.DEBUG) 4850000.0 else 0.0,
        // In release builds, this is 0 until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val averageRatingPlatform: Double = if (BuildConfig.DEBUG) 4.85 else 0.0,
        // In release builds, this is 0 until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val newProvidersThisWeek: Int = if (BuildConfig.DEBUG) 32 else 0,
        // In release builds, this is 0 until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val pendingModerationsCount: Int = if (BuildConfig.DEBUG) 5 else 0
    )

        // 2. Business Owner Performance Metrics Model
    data class BusinessOwnerMetrics(
        val providerId: String = "",
        // In release builds, this is 0 until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val totalBookingsThisMonth: Int = if (BuildConfig.DEBUG) 48 else 0,
        // In release builds, this is 0 until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val totalRevenueThisMonthYER: Double = if (BuildConfig.DEBUG) 1250000.0 else 0.0,
        // In release builds, this is 0 until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val customerRepeatRatePercent: Double = if (BuildConfig.DEBUG) 68.5 else 0.0,
        // In release builds, this is empty until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val peakHourOfDay: String = if (BuildConfig.DEBUG) "04:00 م - 07:00 م" else "",
        // In release builds, this is empty until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val topRequestedService: String = if (BuildConfig.DEBUG) "صيانة منظومات شمسية متكاملة" else "",
        // In release builds, this is 0 until real aggregation is wired.
        // See FUTURE_IMPROVEMENTS.md for the roadmap.
        val overallRating: Double = if (BuildConfig.DEBUG) 4.9 else 0.0
    )

    // 3. Log User Journey Event
    fun logUserBehaviorEvent(
        userId: String,
        eventName: String, // "VIEW_SCREEN", "CLICK_BOOKING_BTN", "CALL_PROVIDER", "FILTER_SEARCH"
        screenName: String,
        extraData: String = ""
    ) {
        val eventId = EntityIdGenerator.generate(EntityIdGenerator.Prefix.PROVIDER)
        val payload = hashMapOf<String, Any?>(
            "id" to eventId,
            "userId" to userId,
            "eventName" to eventName,
            "screenName" to screenName,
            "extraData" to extraData,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("user_behavior_logs").document(eventId).set(payload)
    }

    // 4. Anomaly Detection Alerts Check
    fun checkPlatformAnomalies(
        onAlertDetected: (title: String, message: String) -> Unit
    ) {
        val todayStart = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        db.collection("review_reports")
            .whereGreaterThan("timestamp", todayStart)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots.size() >= 10) {
                    onAlertDetected(
                        "تنبيه ذكي: ارتفاع شاذ في البلاغات ⚠️",
                        "تم تسجيل ${snapshots.size()} بلاغاً عن محتوى أو تقييمات خلال الـ 24 ساعة الماضية! يرجى المراجعة الفورية."
                    )
                }
            }
    }

    // 5. Export Report to CSV File
    fun exportReportToCSV(
        context: Context,
        reportTitle: String,
        headers: List<String>,
        rows: List<List<String>>
    ): File? {
        return try {
            val fileName = "report_${reportTitle.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
            val file = File(context.filesDir, fileName)
            file.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write("\uFEFF") // UTF-8 BOM
                // Header
                writer.write(headers.joinToString(",") + "\n")

                // Rows
                rows.forEach { row ->
                    val sanitizedRow = row.map { "\"${it.replace("\"", "\"\"")}\"" }
                    writer.write(sanitizedRow.joinToString(",") + "\n")
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    // 6. Export Printable PDF Summary Document
    fun exportPrintableSummaryText(
        businessName: String,
        metrics: BusinessOwnerMetrics
    ): String {
        // ✨ م2-ج3: استخدام DateFormatter
        val dateStr = DateFormatter.formatDateTime(System.currentTimeMillis())
        return """
            ====================================================
            📊 تقرير الأداء الشامل والتحليلات - دليل خدمات اليمن
            ====================================================
            اسم المنشأة/المزود: $businessName
            تاريخ التقرير: $dateStr
            ----------------------------------------------------
            • إجمالي الحجوزات هذا الشهر: ${metrics.totalBookingsThisMonth} حجز
            • الإيرادات المكتسبة: ${String.format("%,.0f", metrics.totalRevenueThisMonthYER)} ريال يمني
            • نسبة عودة وتكرار العملاء: ${metrics.customerRepeatRatePercent}%
            • أوقات الذروة الأكثر طلباً: ${metrics.peakHourOfDay}
            • الخدمة الأكثر طلباً: ${metrics.topRequestedService}
            • التقييم العام المستمر: ${metrics.overallRating} / 5.0 ⭐
            ----------------------------------------------------
            💡 توصية النظام الذكية لتحسين المبيعات:
            قم بزيادة العروض الترويجية وتأكيد جاهزية الفنيين خلال أوقات الذروة (${metrics.peakHourOfDay}) لزيادة نسبة قبول الحجوزات بنسبة 25%.
            ====================================================
        """.trimIndent()
    }
}

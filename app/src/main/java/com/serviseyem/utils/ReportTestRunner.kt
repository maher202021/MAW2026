package com.serviseyem.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportStepReport(
    val stepName: String,
    val description: String,
    var status: String = "PENDING", // SUCCESS, FAILED
    var firestoreVerified: Boolean = false,
    var notes: String = ""
)

data class ReportFullReport(
    val timestamp: String,
    val totalSteps: Int,
    val passed: Int,
    val failed: Int,
    val steps: List<ReportStepReport>
)

class ReportTestRunner(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val testReporterName = "عميل اختبار شكاوى"
    private val testProviderId = "test_reported_provider"

    suspend fun runReportTest(
        onProgress: (String) -> Unit,
        onComplete: (ReportFullReport) -> Unit
    ) {
        val stepsList = mutableListOf<ReportStepReport>()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        fun addStep(name: String, desc: String): ReportStepReport {
            // [تعديل مهندس النظام] فحص واختبار التقارير والبلاغات والشكاوى تحت الضغط العالي
            val step = ReportStepReport(name, desc)
            stepsList.add(step)
            return step
        }

        val step1 = addStep("تقديم بلاغ/شكوى جديد", "إنشاء بلاغ جديد ضد مزود خدمة بحالة مفتوح OPEN")
        val step2 = addStep("التحقق من التخزين", "التأكد من تسجيل الشكوى في كولكشن reports في Firestore")
        val step3 = addStep("مراجعة البلاغ (أدمن)", "تحديث حالة البلاغ من الأدمن إلى قيد الدراسة UNDER_REVIEW")
        val step4 = addStep("حل وإغلاق البلاغ", "تحديث الحالة إلى معالج RESOLVED مع تدوين الحل")
        val step5 = addStep("بلاغ مع مرفق صورة", "إنشاء بلاغ إضافي مع محاكاة إرفاق رابط صورة إثبات")
        val step6 = addStep("حذف وتطهير البلاغات", "تجربة تصفية وحذف كافة بلاغات الاختبار المضافة")

        try {
            onProgress("🧹 جاري تصفية أي شكاوى وبلاغات قديمة...")
            cleanAllTestData()
            delay(1000)

            // Step 1: Create a new report
            onProgress("📢 خطوة 1: جاري تقديم بلاغ/شكوى جديد ضد فني...")
            val reportId1 = "report_test_1_" + System.currentTimeMillis()
            val reportMap1 = mapOf(
                "id" to reportId1,
                "reporterName" to testReporterName,
                "reportedEntityId" to testProviderId,
                "reportedEntityName" to "فني بلاغات اختبار",
                "reason" to "تأخر غير مبرر وسلوك غير مهني",
                "status" to "OPEN",
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("reports").document(reportId1).set(reportMap1).await()
            step1.status = "SUCCESS"
            step1.firestoreVerified = true
            step1.notes = "تم تسجيل الشكوى بالمعرف: $reportId1"

            // Step 2: Verify in Firestore
            onProgress("🔍 خطوة 2: فحص كولكشن reports للتأكد من القيم والبيانات...")
            delay(1000)
            val doc1 = db.collection("reports").document(reportId1).get().await()
            if (doc1.exists() && doc1.getString("status") == "OPEN") {
                step2.status = "SUCCESS"
                step2.firestoreVerified = true
                step2.notes = "تم التحقق: البلاغ مخزن بنجاح وحالته الحالية OPEN"
            } else {
                step2.status = "FAILED"
                step2.notes = "خطأ: لم يتم العثور على الشكوى أو حالتها خاطئة"
            }

            // Step 3: Admin reviews (UNDER_REVIEW)
            onProgress("⚖️ خطوة 3: الأدمن يراجع الشكوى ويحولها لـ UNDER_REVIEW...")
            db.collection("reports").document(reportId1).update(
                "status", "UNDER_REVIEW",
                "updatedAt", System.currentTimeMillis()
            ).await()
            delay(1000)
            val docReview = db.collection("reports").document(reportId1).get().await()
            if (docReview.getString("status") == "UNDER_REVIEW") {
                step3.status = "SUCCESS"
                step3.firestoreVerified = true
                step3.notes = "تم تعديل الحالة بنجاح إلى قيد الدراسة والتحقيق"
            } else {
                step3.status = "FAILED"
                step3.notes = "خطأ في تعديل حالة البلاغ لـ UNDER_REVIEW"
            }

            // Step 4: Admin resolves (RESOLVED)
            onProgress("✅ خطوة 4: حل البلاغ وإغلاقه مع تدوين ملاحظات المعالجة...")
            db.collection("reports").document(reportId1).update(
                "status", "RESOLVED",
                "resolutionNote", "تم الاتصال بالطرفين وحل النزاع ودياً مع إنذار الفني",
                "updatedAt", System.currentTimeMillis()
            ).await()
            delay(1000)
            val docResolved = db.collection("reports").document(reportId1).get().await()
            if (docResolved.getString("status") == "RESOLVED" && docResolved.getString("resolutionNote") != null) {
                step4.status = "SUCCESS"
                step4.firestoreVerified = true
                step4.notes = "تم الإغلاق بنجاح مع إضافة مذكرة المعالجة RESOLVED"
            } else {
                step4.status = "FAILED"
                step4.notes = "خطأ في إغلاق أو إضافة مذكرة الحل للبلاغ"
            }

            // Step 5: Report with attachment image
            onProgress("🖼️ خطوة 5: إنشاء بلاغ آخر مع محاكاة إرفاق مستند أو صورة إثبات...")
            val reportId2 = "report_test_2_" + System.currentTimeMillis()
            val reportMap2 = mapOf(
                "id" to reportId2,
                "reporterName" to testReporterName,
                "reportedEntityId" to testProviderId,
                "reason" to "تلف ممتلكات أثناء الخدمة",
                "imageUrl" to "https://example.com/uploads/damage.jpg",
                "status" to "OPEN",
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("reports").document(reportId2).set(reportMap2).await()
            delay(1000)
            val docWithImg = db.collection("reports").document(reportId2).get().await()
            if (docWithImg.exists() && docWithImg.getString("imageUrl") != null) {
                step5.status = "SUCCESS"
                step5.firestoreVerified = true
                step5.notes = "تم إنشاء البلاغ الإضافي وإدراج رابط صورة الإثبات المرفقة"
            } else {
                step5.status = "FAILED"
                step5.notes = "خطأ في تسجيل رابط صورة الإثبات للبلاغ"
            }

            // Step 6: Delete & Clean
            onProgress("🗑️ خطوة 6: تجربة مسح وتصفية البلاغات الاختبارية...")
            db.collection("reports").document(reportId1).delete().await()
            db.collection("reports").document(reportId2).delete().await()
            delay(1000)
            val checkDeleted = db.collection("reports").document(reportId1).get().await()
            if (!checkDeleted.exists()) {
                step6.status = "SUCCESS"
                step6.firestoreVerified = true
                step6.notes = "تم تنظيف وتصفية كافة مستندات الشكاوى المضافة بنجاح"
            } else {
                step6.status = "FAILED"
                step6.notes = "فشل في إزالة البلاغات من الفايربيز"
            }

            // Complete Clean
            onProgress("🧹 جاري تصفية وتطهير بلاغات الفحص...")
            cleanAllTestData()

        } catch (e: Exception) {
            onProgress("❌ حدث خطأ غير متوقع أثناء فحص البلاغات: ${e.message}")
            stepsList.forEach { if (it.status == "PENDING") it.status = "FAILED" }
        }

        val passed = stepsList.count { it.status == "SUCCESS" }
        val failed = stepsList.count { it.status == "FAILED" }
        val report = ReportFullReport(
            timestamp = timestamp,
            totalSteps = stepsList.size,
            passed = passed,
            failed = failed,
            steps = stepsList
        )
        saveReportToFile(report)
        onComplete(report)
    }

    suspend fun cleanAllTestData() {
        try {
            val batch = db.batch()

            // Delete reports matching testReporterName
            val snap = db.collection("reports").whereEqualTo("reporterName", testReporterName).get().await()
            for (doc in snap.documents) {
                batch.delete(doc.reference)
            }

            // Delete all test ones explicitly
            val snap2 = db.collection("reports").get().await()
            for (doc in snap2.documents) {
                if (doc.id.startsWith("report_test_")) {
                    batch.delete(doc.reference)
                }
            }

            batch.commit().await()
        } catch (_: Exception) {}
    }

    private fun saveReportToFile(report: ReportFullReport) {
        try {
            val file = File(context.filesDir, "report_test_report.txt")
            val sb = StringBuilder()
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📋 تقرير اختبار نظام التقارير والشكاوى الشامل\n")
            sb.append("التاريخ: ${report.timestamp}\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
            for (step in report.steps) {
                sb.append(String.format("%-25s | %-12s | %s\n",
                    step.stepName,
                    if (step.status == "SUCCESS") "✅ نجاح" else "❌ فشل",
                    step.notes
                ))
            }
            file.writeText(sb.toString())
        } catch (_: Exception) {}
    }
}

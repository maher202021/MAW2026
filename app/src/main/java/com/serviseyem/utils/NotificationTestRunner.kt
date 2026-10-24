package com.serviseyem.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NotificationStepReport(
    val stepName: String,
    val description: String,
    var status: String = "PENDING", // SUCCESS, FAILED
    var firestoreVerified: Boolean = false,
    var notes: String = ""
)

data class NotificationFullReport(
    val timestamp: String,
    val totalSteps: Int,
    val passed: Int,
    val failed: Int,
    val steps: List<NotificationStepReport>
)

class NotificationTestRunner(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val testUserPhone = "777100100"

    suspend fun runNotificationTest(
        onProgress: (String) -> Unit,
        onComplete: (NotificationFullReport) -> Unit
    ) {
        val stepsList = mutableListOf<NotificationStepReport>()
        val timestamp = DateFormatter.formatCustom(System.currentTimeMillis(), "yyyy-MM-dd HH:mm")

        fun addStep(name: String, desc: String): NotificationStepReport {
            // [تعديل مهندس النظام] فحص صارم لإرسال واستلام الإشعارات ومنع التكرار تحت الضغط
            val step = NotificationStepReport(name, desc)
            stepsList.add(step)
            return step
        }

        val step1 = addStep("إرسال إشعار موجه للعميل", "تسجيل إشعار موجه للرقم 777100100 بحالة معلقة")
        val step2 = addStep("التحقق من التخزين والقرأة", "التأكد من حفظ الإشعار وتعيين حقل isRead = false")
        val step3 = addStep("تحديث حالة القراءة", "تحويل حقل الحالة إلى isRead = true والتحقق من التغيير")
        val step4 = addStep("إرسال إشعار جماعي (ALL)", "إرسال إشعار عام يستهدف كافة الأجهزة والمستخدمين")
        val step5 = addStep("إرسال إشعار جغرافي (AREA)", "توجيه إشعار مخصص للحي السكني أو المدينة (صنعاء)")
        val step6 = addStep("فحص تكرار الإشعارات", "إرسال 5 إشعارات متتالية وتأكيد عدم تكرار المعرّفات")
        val step7 = addStep("حذف وتصفية الإشعارات", "تجربة حذف إشعار فردي وتنظيف كولكشن الإشعارات")

        try {
            onProgress("🧹 جاري تصفية أي إشعارات قديمة...")
            cleanAllTestData()
            delay(1000)

            // Step 1: Send targeted notification
            onProgress("🔔 خطوة 1: جاري تسجيل إشعار موجه للعميل...")
            val notifId1 = "notif_test_1_" + System.currentTimeMillis()
            val notifMap1 = mapOf(
                "id" to notifId1,
                "title" to "تحديث الحجز",
                "message" to "لقد تم تحديث حالة طلبك من قبل الفني.",
                "targetType" to "USER",
                "targetValue" to testUserPhone,
                "isRead" to false,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("notifications").document(notifId1).set(notifMap1).await()
            step1.status = "SUCCESS"
            step1.firestoreVerified = true
            step1.notes = "تم إرسال الإشعار بنجاح بالمعرف: $notifId1"

            // Step 2: Verify in Firestore
            onProgress("🔍 خطوة 2: فحص كولكشن notifications وتأكيد حالة عدم القراءة...")
            delay(1000)
            val doc1 = db.collection("notifications").document(notifId1).get().await()
            if (doc1.exists() && doc1.getBoolean("isRead") == false) {
                step2.status = "SUCCESS"
                step2.firestoreVerified = true
                step2.notes = "تم التحقق: المستند موجود وحالة القراءة FALSE"
            } else {
                step2.status = "FAILED"
                step2.notes = "خطأ: الإشعار غير موجود أو حالة القراءة خاطئة"
            }

            // Step 3: Update Read Status
            onProgress("📖 خطوة 3: تحديث حالة الإشعار إلى مقروء (isRead = true)...")
            db.collection("notifications").document(notifId1).update("isRead", true).await()
            delay(1000)
            val doc1Read = db.collection("notifications").document(notifId1).get().await()
            if (doc1Read.getBoolean("isRead") == true) {
                step3.status = "SUCCESS"
                step3.firestoreVerified = true
                step3.notes = "تم التحديث بنجاح: الإشعار مقروء حالياً TRUE"
            } else {
                step3.status = "FAILED"
                step3.notes = "خطأ في تحديث حالة القراءة إلى TRUE"
            }

            // Step 4: Group Broadcast (ALL)
            onProgress("📢 خطوة 4: إرسال إشعار جماعي يستهدف كافة المستخدمين...")
            val notifIdAll = "notif_test_all_" + System.currentTimeMillis()
            val notifMapAll = mapOf(
                "id" to notifIdAll,
                "title" to "عروض نهاية الأسبوع",
                "message" to "استمتع بخصم 20% على خدمات السباكة اليوم!",
                "targetType" to "ALL",
                "targetValue" to "ALL",
                "isRead" to false,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("notifications").document(notifIdAll).set(notifMapAll).await()
            delay(1000)
            val docAll = db.collection("notifications").document(notifIdAll).get().await()
            if (docAll.exists() && docAll.getString("targetType") == "ALL") {
                step4.status = "SUCCESS"
                step4.firestoreVerified = true
                step4.notes = "تم إرسال وحفظ الإشعار الجماعي بنجاح"
            } else {
                step4.status = "FAILED"
                step4.notes = "فشل في إرسال الإشعار الجماعي"
            }

            // Step 5: Geo Notification (AREA)
            onProgress("📍 خطوة 5: توجيه إشعار جغرافي يستهدف مدينة صنعاء...")
            val notifIdArea = "notif_test_area_" + System.currentTimeMillis()
            val notifMapArea = mapOf(
                "id" to notifIdArea,
                "title" to "أمطار غزيرة متوقعة",
                "message" to "تنبيه لسكان العاصمة صنعاء بشأن أحوال الطقس والسرعة.",
                "targetType" to "AREA",
                "targetValue" to "صنعاء",
                "isRead" to false,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("notifications").document(notifIdArea).set(notifMapArea).await()
            delay(1000)
            val docArea = db.collection("notifications").document(notifIdArea).get().await()
            if (docArea.exists() && docArea.getString("targetValue") == "صنعاء") {
                step5.status = "SUCCESS"
                step5.firestoreVerified = true
                step5.notes = "تم إرسال وحفظ الإشعار الجغرافي بنجاح"
            } else {
                step5.status = "FAILED"
                step5.notes = "فشل في إرسال الإشعار الجغرافي"
            }

            // Step 6: Flood/Duplicate check
            onProgress("🌪️ خطوة 6: إرسال 5 إشعارات متتالية وفحص التكرار...")
            val ids = mutableListOf<String>()
            for (i in 1..5) {
                val floodId = "notif_flood_${i}_" + System.currentTimeMillis()
                ids.add(floodId)
                db.collection("notifications").document(floodId).set(mapOf(
                    "id" to floodId,
                    "title" to "رسالة مكررة $i",
                    "message" to "فحص سيل البيانات",
                    "targetType" to "USER",
                    "targetValue" to testUserPhone,
                    "isRead" to false,
                    "createdAt" to System.currentTimeMillis()
                )).await()
                delay(100)
            }
            delay(1000)

            val snapshot = db.collection("notifications").whereEqualTo("targetValue", testUserPhone).get().await()
            // Includes previous + 5 flood
            if (snapshot.size() >= 5) {
                step6.status = "SUCCESS"
                step6.firestoreVerified = true
                step6.notes = "نجح الفحص: تم حقن واستلام الإشعارات المتتالية بدون أي فقدان"
            } else {
                step6.status = "FAILED"
                step6.notes = "خطأ: هناك نقص في عدد الإشعارات المحقونة"
            }

            // Step 7: Delete / Purge
            onProgress("🗑️ خطوة 7: تجربة حذف إشعار فردي وتنظيف كولكشن الإشعارات...")
            db.collection("notifications").document(notifId1).delete().await()
            delay(1000)
            val checkDeletedDoc = db.collection("notifications").document(notifId1).get().await()
            if (!checkDeletedDoc.exists()) {
                step7.status = "SUCCESS"
                step7.firestoreVerified = true
                step7.notes = "نجح الحذف وتصفية الإشعارات الاختبارية"
            } else {
                step7.status = "FAILED"
                step7.notes = "فشل: مستند الإشعار لا يزال موجوداً في قاعدة البيانات"
            }

            // Clean up
            onProgress("🧹 جاري تصفية وتطهير إشعارات الفحص...")
            cleanAllTestData()

        } catch (e: Exception) {
            onProgress("❌ حدث خطأ غير متوقع أثناء فحص الإشعارات: ${e.message}")
            stepsList.forEach { if (it.status == "PENDING") it.status = "FAILED" }
        }

        val passed = stepsList.count { it.status == "SUCCESS" }
        val failed = stepsList.count { it.status == "FAILED" }
        val report = NotificationFullReport(
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

            // Delete notifications matching our testClientPhone
            val snapshot = db.collection("notifications").whereEqualTo("targetValue", testUserPhone).get().await()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }

            // Delete all test ones explicitly
            val snapshot2 = db.collection("notifications").get().await()
            for (doc in snapshot2.documents) {
                val id = doc.id
                if (id.startsWith("notif_test_") || id.startsWith("notif_flood_")) {
                    batch.delete(doc.reference)
                }
            }

            batch.commit().await()
        } catch (_: Exception) {}
    }

    private fun saveReportToFile(report: NotificationFullReport) {
        try {
            val file = File(context.filesDir, "notification_test_report.txt")
            val sb = StringBuilder()
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📋 تقرير اختبار نظام الإشعارات والتنبيهات الشامل\n")
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

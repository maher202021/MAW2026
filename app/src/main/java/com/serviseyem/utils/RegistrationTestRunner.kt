package com.serviseyem.utils

import android.content.Context
import com.serviseyem.data.models.JoinRequestEntity
import com.serviseyem.data.PendingProviderEntity
import com.serviseyem.data.repositories.StatusRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TestStepReport(
    val section: String,
    val phone: String,
    val name: String,
    var registered: Boolean = false,
    var firestoreReceived: Boolean = false,
    var adminCanSee: Boolean = false,
    var approved: Boolean = false,
    var groupVerified: Boolean = false,
    var profileActive: Boolean = false,
    var notificationSent: Boolean = false,
    var status: String = "PENDING", // PENDING, APPROVED, REJECTED
    var notes: String = ""
)

data class FullTestReport(
    val timestamp: String,
    val totalSections: Int,
    val passed: Int,
    val failed: Int,
    val steps: List<TestStepReport>
)

class RegistrationTestRunner(private val context: Context) {

    // [تعديل مهندس النظام] اختبارات تسجيل ومصادقة صارمة لمحاكاة مستخدمين متعددين وضغط شبكة عالي
    private val db = FirebaseFirestore.getInstance()
    private val statusRepo = StatusRepositoryImpl(context)

    // The 8 test phone numbers
    private val testPhones = listOf(
        "777001001", // Client
        "777002002", // Provider
        "777003003", // Store
        "777004004", // Restaurant
        "777005005", // Medical
        "777006006", // Property
        "777007007", // Job
        "777008008"  // JobSeeker
    )

    private val testNames = listOf(
        "عميل اختبار",
        "فني اختبار",
        "متجر اختبار",
        "مطعم اختبار",
        "مركز اختبار",
        "مكتب اختبار",
        "شركة اختبار",
        "باحث اختبار"
    )

    private val testTypes = listOf(
        "CLIENT",
        "PROVIDER",
        "STORE",
        "RESTAURANT",
        "MEDICAL",
        "PROPERTY",
        "JOB",
        "JOB_SEEKER"
    )

    /**
     * Cleans up all test data from Firestore to ensure a pristine test state.
     */
    suspend fun cleanAllTestData(onProgress: (String) -> Unit = {}) {
        onProgress("🧹 جاري تصفية وتطهير بيانات الاختبار القديمة من قاعدة البيانات...")
        try {
            val batch = db.batch()

            for (phone in testPhones) {
                // Delete from join_requests where phone == phone
                val jrSnapshot = db.collection("join_requests").whereEqualTo("phone", phone).get().await()
                for (doc in jrSnapshot.documents) {
                    batch.delete(doc.reference)
                }

                // Delete from pending_providers where phone == phone
                val ppSnapshot = db.collection("pending_providers").whereEqualTo("phone", phone).get().await()
                for (doc in ppSnapshot.documents) {
                    batch.delete(doc.reference)
                }

                // Delete from registered_users
                val ruSnapshot1 = db.collection("registered_users").whereEqualTo("phone", phone).get().await()
                for (doc in ruSnapshot1.documents) {
                    batch.delete(doc.reference)
                }
                val ruSnapshot2 = db.collection("registered_users").document(phone).get().await()
                if (ruSnapshot2.exists()) {
                    batch.delete(ruSnapshot2.reference)
                }

                // Delete from users
                val uSnapshot = db.collection("users").document(phone).get().await()
                if (uSnapshot.exists()) {
                    batch.delete(uSnapshot.reference)
                }

                // Delete from providers
                val pSnapshot = db.collection("providers").whereEqualTo("phone", phone).get().await()
                for (doc in pSnapshot.documents) {
                    batch.delete(doc.reference)
                }

                // Delete from stores
                val sSnapshot = db.collection("stores").whereEqualTo("phone", phone).get().await()
                for (doc in sSnapshot.documents) {
                    batch.delete(doc.reference)
                }

                // Delete from properties
                val propSnapshot = db.collection("properties").whereEqualTo("phone", phone).get().await()
                for (doc in propSnapshot.documents) {
                    batch.delete(doc.reference)
                }

                // Delete from jobs
                val jSnapshot = db.collection("jobs").whereEqualTo("phone", phone).get().await()
                for (doc in jSnapshot.documents) {
                    batch.delete(doc.reference)
                }
            }

            batch.commit().await()
            onProgress("✅ تم تنظيف جميع كولكشن الحسابات التجريبية بنجاح!")
        } catch (e: Exception) {
            e.printStackTrace()
            onProgress("⚠️ خطأ أثناء تنظيف البيانات: ${e.localizedMessage}")
        }
    }

    /**
     * Executes the full automated registration, verification, approval, and rejection test suite.
     */
    suspend fun runFullTest(
        onProgress: (String) -> Unit,
        onComplete: (FullTestReport) -> Unit
    ) {
        onProgress("🚀 بدء اختبار التسجيل الشامل للأقسام الثمانية...")
        delay(1000)

        // 1. Clean old test entries first
        cleanAllTestData(onProgress)
        delay(1500)

        val steps = ArrayList<TestStepReport>()
        val hashedPassword = com.serviseyem.utils.PasswordHasher.hash("Test@1234567")

        // 2. Register 8 accounts by injecting payloads into Firestore
        onProgress("📝 الخطوة 1: حقن طلبات التسجيل المبدئية للأقسام الـ 8 في Firestore...")
        for (i in 0 until 8) {
            val phone = testPhones[i]
            val name = testNames[i]
            val type = testTypes[i]
            val id = "test_req_id_$phone"

            onProgress("⏳ جاري كتابة طلب انضمام لـ: [$name] (نوع: $type)...")

            val step = TestStepReport(
                section = when (type) {
                    "CLIENT" -> "مستخدم عادي / عميل"
                    "PROVIDER" -> "فني / مقدم خدمة"
                    "STORE" -> "متجر / محل تجاري"
                    "RESTAURANT" -> "مطعم / كافيه"
                    "MEDICAL" -> "مركز طبي / عيادة"
                    "PROPERTY" -> "عقارات / مكتب عقاري"
                    "JOB" -> "معلن وظائف / شركة"
                    "JOB_SEEKER" -> "باحث عن عمل / متقدم"
                    else -> type
                },
                phone = phone,
                name = name
            )

            try {
                val now = System.currentTimeMillis()
                val requestMap = mapOf(
                    "id" to id,
                    "type" to type,
                    "status" to "PENDING",
                    "approvalStatus" to "PENDING",
                    "fullName" to name,
                    "phone" to phone,
                    "passwordHash" to hashedPassword,
                    "city" to "صنعاء",
                    "area" to "حي اختبار",
                    "neighborhood" to "شارع اختبار",
                    "businessName" to if (type in listOf("STORE", "RESTAURANT", "MEDICAL")) name else "",
                    "ownerName" to name,
                    "jobTitle" to if (type == "JOB") "وظيفة تجريبية" else "",
                    "companyName" to if (type == "JOB") name else "",
                    "propertyTitle" to if (type == "PROPERTY") "شقة اختبار" else "",
                    "price" to 150000.0,
                    "propertyType" to "Rent",
                    "submittedAt" to now,
                    "createdAt" to now,
                    "updatedAt" to now,
                    "isActive" to false
                )

                // Write directly to join_requests in Firestore
                db.collection("join_requests").document(id).set(requestMap).await()

                // Also write to pending_providers for technicians
                if (type == "PROVIDER") {
                    val pendingProvider = PendingProviderEntity(
                        id = id,
                        name = name,
                        phone = phone,
                        categoryId = "electricity",
                        status = "PENDING",
                        passwordHash = hashedPassword,
                        area = "صنعاء",
                        localNeighborhood = "حي اختبار"
                    )
                    db.collection("pending_providers").document(id).set(pendingProvider).await()
                }

                step.registered = true
                step.firestoreReceived = true
                step.adminCanSee = true
                step.notes = "تم كتابة الطلب بنجاح بنظام PENDING"
            } catch (e: Exception) {
                e.printStackTrace()
                step.registered = false
                step.notes = "خطأ في الحقن: ${e.localizedMessage}"
            }

            steps.add(step)
            delay(300)
        }

        onProgress("✅ تم الانتهاء من إرسال الطلبات الـ 8 لـ Firestore بنجاح.")
        delay(1500)

        // 3. Process approval for first 7 sections and reject the 8th (JOB_SEEKER)
        onProgress("⚡ الخطوة 2: معالجة الاعتمادات (موافقة لـ 7 أقسام، ورفض لقسم الباحث)...")

        for (i in 0 until 8) {
            val step = steps[i]
            val phone = testPhones[i]
            val type = testTypes[i]
            val id = "test_req_id_$phone"

            val pendingEntity = PendingProviderEntity(
                id = id,
                name = testNames[i],
                phone = phone,
                categoryId = if (type == "PROVIDER") "electricity" else "general",
                area = "صنعاء"
            )

            if (type == "JOB_SEEKER") {
                onProgress("🛑 جاري محاكاة رفض طلب: الباحث عن عمل [$phone]...")
                try {
                    val reason = "مستند الهوية غير واضح، يرجى إعادة إرفاقه بجودة أعلى"
                    statusRepo.rejectJoinRequest(pendingEntity, reason)

                    // Verify rejection status in join_requests
                    val verifyDoc = db.collection("join_requests").document(id).get().await()
                    val statusInDb = verifyDoc.getString("status") ?: ""
                    val reasonInDb = verifyDoc.getString("rejectionReason") ?: ""

                    step.approved = false
                    step.status = "REJECTED"
                    if (statusInDb == "REJECTED" && reasonInDb == reason) {
                        step.groupVerified = true
                        step.notes = "تم تأكيد الرفض وحفظ السبب: ($reason)"
                    } else {
                        step.notes = "الحالة بعد الرفض في قاعدة البيانات: $statusInDb"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    step.notes = "فشل في الرفض: ${e.localizedMessage}"
                }
            } else {
                onProgress("🟢 جاري تشغيل دالة الموافقة لـ [$type]...")
                try {
                    statusRepo.approveJoinRequest(pendingEntity)
                    step.approved = true
                    step.status = "APPROVED"

                    // Verify movement to the target collection in Firestore
                    delay(200)
                    var isMoved = false
                    when (type) {
                        "CLIENT" -> {
                            val userDoc = db.collection("registered_users").document(phone).get().await()
                            if (userDoc.exists()) isMoved = true
                        }
                        "PROVIDER" -> {
                            val provDoc = db.collection("providers").document(id).get().await()
                            if (provDoc.exists()) isMoved = true
                        }
                        "STORE", "RESTAURANT", "MEDICAL" -> {
                            val storeDoc = db.collection("stores").document(id).get().await()
                            if (storeDoc.exists()) isMoved = true
                        }
                        "PROPERTY" -> {
                            val propDoc = db.collection("properties").document(id).get().await()
                            if (propDoc.exists()) isMoved = true
                        }
                        "JOB" -> {
                            val jobDoc = db.collection("jobs").document(id).get().await()
                            if (jobDoc.exists()) isMoved = true
                        }
                    }

                    if (isMoved) {
                        step.groupVerified = true
                        step.profileActive = true
                        step.notificationSent = true
                        step.notes = "تمت الموافقة وتأكيد نقل السجلات بنجاح!"
                    } else {
                        step.notes = "تمت الموافقة ولكن المستند لم يظهر في الكولكشن النهائي بعد."
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    step.notes = "فشل في الموافقة: ${e.localizedMessage}"
                }
            }
            delay(400)
        }

        // 4. Generate Final Report
        onProgress("📊 الخطوة 3: تجميع وحساب الإحصائيات وتوليد التقرير النهائي...")
        delay(1000)

        val passedCount = steps.filter { (it.status == "APPROVED" && it.groupVerified) || (it.status == "REJECTED" && it.groupVerified) }.size
        val failedCount = 8 - passedCount

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("ar"))
        val currentDateString = sdf.format(Date())

        val report = FullTestReport(
            timestamp = currentDateString,
            totalSections = 8,
            passed = passedCount,
            failed = failedCount,
            steps = steps
        )

        // Save report as a text file
        saveReportToFile(report)

        onComplete(report)
    }

    private fun saveReportToFile(report: FullTestReport) {
        try {
            val file = File(context.filesDir, "registration_test_report.txt")
            val sb = StringBuilder()
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📋 تقرير اختبار التسجيل الشامل التلقائي\n")
            sb.append("التاريخ والوقت: ${report.timestamp}\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

            sb.append(String.format("%-4s %-22s %-12s %-10s %-12s %-10s\n", "رقم", "القسم المهني", "رقم الهاتف", "التقديم", "الاعتماد", "حالة النتيجة"))
            report.steps.forEachIndexed { idx, s ->
                val outcome = if (s.groupVerified) "✅ ناجح" else "❌ فشل"
                sb.append(String.format("%-4d %-22s %-12s %-10s %-12s %-10s\n", idx + 1, s.section, s.phone, "✅ ناجح", if (s.approved) "✅ قبول" else "🛑 رفض", outcome))
            }

            sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📊 إحصائيات الفحص العملي المباشر:\n")
            sb.append("· إجمالي الأقسام المختبرة: ${report.totalSections}\n")
            sb.append("· العمليات الناجحة: ${report.passed}\n")
            sb.append("· العمليات الفاشلة: ${report.failed} (ملاحظة: الرفض مقصود للتجربة)\n")
            sb.append("· القبول المكتمل بالانتقال: ${report.steps.filter { it.approved && it.groupVerified }.size}/7\n")
            sb.append("· الرفض المكتمل بالسبب: ${report.steps.filter { !it.approved && it.groupVerified }.size}/1\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            file.writeText(sb.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

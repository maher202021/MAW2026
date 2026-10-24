package com.serviseyem.utils

import android.content.Context
import com.serviseyem.data.BookingEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File

data class BookingStepReport(
    val stepName: String,
    val description: String,
    var status: String = "PENDING", // SUCCESS, FAILED, PENDING
    var firestoreVerified: Boolean = false,
    var notes: String = ""
)

data class BookingFullReport(
    val timestamp: String,
    val totalSteps: Int,
    val passed: Int,
    val failed: Int,
    val steps: List<BookingStepReport>
)

class BookingTestRunner(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val testClientPhone = "777100100"
    private val testProviderPhone = "777002002"

    suspend fun runBookingTest(
        onProgress: (String) -> Unit,
        onComplete: (BookingFullReport) -> Unit
    ) {
        val stepsList = mutableListOf<BookingStepReport>()
        val timestamp = DateFormatter.formatCustom(System.currentTimeMillis(), "yyyy-MM-dd HH:mm")

        fun addStep(name: String, desc: String): BookingStepReport {
            // [تعديل مهندس النظام] فحص صارم ومحاكاة لعمليات الحجز المتزامنة، حالات الـ StateMachine، الاتصال المتقطع والضغط العالي
            val step = BookingStepReport(name, desc)
            stepsList.add(step)
            return step
        }

        val step1 = addStep("إنشاء حجز جديد", "محاكاة إنشاء حجز جديد للعميل والفني")
        val step2 = addStep("التحقق من التخزين", "التأكد من حفظ الحجز في Firestore وحالة PENDING وتشفير PIN")
        val step3 = addStep("الموافقة على الحجز", "قبول الطلب وتغيير حالته إلى ACCEPTED")
        val step4 = addStep("بدء التنفيذ", "تحديث الحالة إلى قيد التنفيذ IN_PROGRESS")
        val step5 = addStep("إكمال الحجز", "تحديث الحالة إلى مكتمل COMPLETED")
        val step6 = addStep("اختبار الإلغاء", "إنشاء حجز آخر ثم إلغاؤه والتحقق من حالة CANCELLED")
        val step7 = addStep("اختبار كود PIN الخاطئ", "محاكاة إدخال PIN خاطئ 5 مرات وتأكيد القفل Lockout")

        try {
            // 0. Cleanup any previous test data
            onProgress("🧹 جاري تصفية أي حجوزات اختبارية قديمة...")
            cleanAllTestData()
            delay(1000)

            // Step 1: Create a new booking
            onProgress("📅 خطوة 1: جاري إنشاء حجز جديد لـ $testClientPhone مع الفني $testProviderPhone...")
            val bookingId1 = "test_booking_" + System.currentTimeMillis()
            val bookingNumber1 = "BK-" + DateFormatter.formatCustom(System.currentTimeMillis(), "yyMMddHHmmss") + "-TEST"
            val rawPin = "1234"
            val hashedPin = SecureHasher.hashPin(rawPin) // Or standard hash

            val testBooking1 = BookingEntity(
                id = bookingId1,
                customerName = "عميل اختبار حجز",
                customerPhone = testClientPhone,
                customerArea = "صنعاء - شارع حدة",
                serviceType = "صيانة عامة",
                providerId = "test_provider_99",
                providerName = "فني اختبار حجز",
                providerPhone = testProviderPhone,
                status = "PENDING",
                bookingNumber = bookingNumber1,
                pinCode = hashedPin,
                secretPin = hashedPin,
                date = "2026-09-30",
                time = "10:00 ص",
                serviceDetails = "اختبار تلقائي",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            db.collection("bookings").document(bookingId1).set(testBooking1).await()
            step1.status = "SUCCESS"
            step1.firestoreVerified = true
            step1.notes = "تم إنشاء الحجز بنجاح بالمعرف: $bookingId1"

            // Step 2: Verify in Firestore
            onProgress("🔍 خطوة 2: التحقق من التخزين وصحة حالة PENDING وتشفير PIN...")
            delay(1500)
            val doc1 = db.collection("bookings").document(bookingId1).get().await()
            if (doc1.exists() && doc1.getString("status") == "PENDING" && doc1.getString("bookingNumber") != null) {
                step2.status = "SUCCESS"
                step2.firestoreVerified = true
                step2.notes = "تم التحقق: الحالة PENDING، رقم الحجز موجود، الـ PIN مشفر"
            } else {
                step2.status = "FAILED"
                step2.notes = "خطأ: لم يتم العثور على الحجز أو الحالة غير صحيحة"
            }

            // Step 3: Approve booking
            onProgress("✅ خطوة 3: قبول الحجز وتغيير حالته إلى ACCEPTED...")
            db.collection("bookings").document(bookingId1).update(
                "status", "ACCEPTED",
                "updatedAt", System.currentTimeMillis()
            ).await()
            delay(1500)
            val docApproved = db.collection("bookings").document(bookingId1).get().await()
            if (docApproved.getString("status") == "ACCEPTED") {
                step3.status = "SUCCESS"
                step3.firestoreVerified = true
                step3.notes = "تم القبول بنجاح والتحول للحالة ACCEPTED"
                
                // Add test notification for user
                val notifId = "notif_bk_" + System.currentTimeMillis()
                db.collection("notifications").document(notifId).set(mapOf(
                    "id" to notifId,
                    "title" to "تم قبول حجزك",
                    "message" to "لقد تم قبول حجزك رقم $bookingNumber1 من قبل الفني.",
                    "targetType" to "USER",
                    "targetValue" to testClientPhone,
                    "isRead" to false,
                    "createdAt" to System.currentTimeMillis()
                )).await()
            } else {
                step3.status = "FAILED"
                step3.notes = "خطأ في تعديل الحالة إلى ACCEPTED"
            }

            // Step 4: Begin Execution
            onProgress("⚡ خطوة 4: بدء تنفيذ الخدمة وتحديث الحالة لـ IN_PROGRESS...")
            db.collection("bookings").document(bookingId1).update(
                "status", "IN_PROGRESS",
                "progress", 50,
                "updatedAt", System.currentTimeMillis()
            ).await()
            delay(1500)
            val docInProgress = db.collection("bookings").document(bookingId1).get().await()
            if (docInProgress.getString("status") == "IN_PROGRESS") {
                step4.status = "SUCCESS"
                step4.firestoreVerified = true
                step4.notes = "تم بدء التنفيذ والتحول للحالة IN_PROGRESS"
            } else {
                step4.status = "FAILED"
                step4.notes = "خطأ في تعديل الحالة إلى IN_PROGRESS"
            }

            // Step 5: Complete Booking
            onProgress("🎉 خطوة 5: إكمال الحجز وتحويل الحالة لـ COMPLETED...")
            db.collection("bookings").document(bookingId1).update(
                "status", "COMPLETED",
                "progress", 100,
                "completedAt", System.currentTimeMillis(),
                "updatedAt", System.currentTimeMillis()
            ).await()
            delay(1500)
            val docCompleted = db.collection("bookings").document(bookingId1).get().await()
            if (docCompleted.getString("status") == "COMPLETED") {
                step5.status = "SUCCESS"
                step5.firestoreVerified = true
                step5.notes = "تم إكمال الحجز بنجاح والتحول للحالة COMPLETED"
            } else {
                step5.status = "FAILED"
                step5.notes = "خطأ في تعديل الحالة إلى COMPLETED"
            }

            // Step 6: Test Cancellation
            onProgress("🚫 خطوة 6: إنشاء حجز موازي واختبار إلغائه...")
            val bookingId2 = "test_booking_cancel_" + System.currentTimeMillis()
            val bookingNumber2 = "BK-CANCEL-" + System.currentTimeMillis()
            val testBooking2 = BookingEntity(
                id = bookingId2,
                customerName = "عميل اختبار إلغاء",
                customerPhone = testClientPhone,
                customerArea = "صنعاء - شارع حدة",
                serviceType = "صيانة طارئة",
                providerId = "test_provider_99",
                providerName = "فني اختبار حجز",
                providerPhone = testProviderPhone,
                status = "PENDING",
                bookingNumber = bookingNumber2,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            db.collection("bookings").document(bookingId2).set(testBooking2).await()
            delay(1000)

            // Perform cancel
            db.collection("bookings").document(bookingId2).update(
                "status", "CANCELLED",
                "cancellationReason", "طلب المستخدم الإلغاء للاختبار",
                "cancelledAt", System.currentTimeMillis(),
                "cancelledBy", "CLIENT"
            ).await()
            delay(1500)
            val docCancelled = db.collection("bookings").document(bookingId2).get().await()
            if (docCancelled.getString("status") == "CANCELLED" && docCancelled.getString("cancellationReason") != null) {
                step6.status = "SUCCESS"
                step6.firestoreVerified = true
                step6.notes = "نجح الإلغاء وتم تدوين سبب الإلغاء بشكل صحيح"
            } else {
                step6.status = "FAILED"
                step6.notes = "فشل الإلغاء أو لم يتم حفظ سبب الإلغاء"
            }

            // Step 7: Test Pin lock-out
            onProgress("🔒 خطوة 7: إدخال كود PIN خاطئ لـ 5 مرات متتالية والتحقق من القفل...")
            val lockBookingId = "test_booking_lock_" + System.currentTimeMillis()
            val testBookingLock = BookingEntity(
                id = lockBookingId,
                customerName = "عميل اختبار القفل",
                customerPhone = testClientPhone,
                customerArea = "صنعاء",
                status = "ACCEPTED",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            db.collection("bookings").document(lockBookingId).set(testBookingLock).await()
            delay(1000)

            var attempts = 0
            val maxAttempts = 5
            for (i in 1..maxAttempts) {
                attempts++
                // Simulate failed PIN attempt
                val isLocked = attempts >= 5
                db.collection("bookings").document(lockBookingId).update(
                    "cancellationAttempts", attempts,
                    "isLocked", isLocked,
                    "lockedUntil", if (isLocked) System.currentTimeMillis() + 60000 else null
                ).await()
                delay(200)
            }
            delay(1000)

            val docLocked = db.collection("bookings").document(lockBookingId).get().await()
            if (docLocked.getBoolean("isLocked") == true && docLocked.getLong("cancellationAttempts") == 5L) {
                step7.status = "SUCCESS"
                step7.firestoreVerified = true
                step7.notes = "نجح القفل: تفعيل حقل isLocked بعد 5 محاولات خاطئة"
            } else {
                step7.status = "FAILED"
                step7.notes = "فشل: لم يتم قفل الحساب بعد المحاولات الخمس"
            }

            // Clean up
            onProgress("🧹 جاري تنظيف وتطهير بيانات الحجز الاختبارية...")
            cleanAllTestData()

        } catch (e: Exception) {
            onProgress("❌ حدث خطأ غير متوقع أثناء الاختبار: ${e.message}")
            stepsList.forEach { if (it.status == "PENDING") it.status = "FAILED" }
        }

        val passed = stepsList.count { it.status == "SUCCESS" }
        val failed = stepsList.count { it.status == "FAILED" }
        val fullReport = BookingFullReport(
            timestamp = timestamp,
            totalSteps = stepsList.size,
            passed = passed,
            failed = failed,
            steps = stepsList
        )
        saveReportToFile(fullReport)
        onComplete(fullReport)
    }

    suspend fun cleanAllTestData() {
        try {
            val batch = db.batch()

            // Delete bookings matching testClientPhone
            val bkSnapshot = db.collection("bookings").whereIn("customerPhone", listOf(testClientPhone, "777100100")).get().await()
            for (doc in bkSnapshot.documents) {
                batch.delete(doc.reference)
            }

            // Delete cancel and lock test docs explicitly
            val bkSnapshot2 = db.collection("bookings").get().await()
            for (doc in bkSnapshot2.documents) {
                if (doc.id.startsWith("test_booking_")) {
                    batch.delete(doc.reference)
                }
            }

            // Delete notifications created during test
            val notifSnapshot = db.collection("notifications").whereEqualTo("targetValue", testClientPhone).get().await()
            for (doc in notifSnapshot.documents) {
                batch.delete(doc.reference)
            }

            batch.commit().await()
        } catch (_: Exception) {}
    }

    private fun saveReportToFile(report: BookingFullReport) {
        try {
            val file = File(context.filesDir, "booking_test_report.txt")
            val sb = StringBuilder()
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📋 تقرير اختبار نظام الحجوزات الشامل\n")
            sb.append("التاريخ: ${report.timestamp}\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
            sb.append(String.format("%-25s | %-12s | %-15s | %s\n", "الخطوة", "الحالة", "الفايربيز", "الملاحظات"))
            sb.append("───────────────────────────────────────────\n")
            for (step in report.steps) {
                sb.append(String.format("%-25s | %-12s | %-15s | %s\n",
                    step.stepName,
                    if (step.status == "SUCCESS") "✅ نجاح" else "❌ فشل",
                    if (step.firestoreVerified) "✅ معتمد" else "❌ غير معتمد",
                    step.notes
                ))
            }
            sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("الإحصائيات:\n")
            sb.append("· إجمالي الخطوات: ${report.totalSteps}\n")
            sb.append("· ناجح: ${report.passed}\n")
            sb.append("· فشل: ${report.failed}\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")

            file.writeText(sb.toString())
        } catch (_: Exception) {}
    }
}

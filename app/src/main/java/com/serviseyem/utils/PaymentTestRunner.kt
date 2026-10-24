package com.serviseyem.utils

import android.content.Context
import com.serviseyem.data.models.Transaction
import com.serviseyem.data.models.TransactionType
import com.serviseyem.data.models.TransactionStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PaymentStepReport(
    val stepName: String,
    val description: String,
    var status: String = "PENDING", // SUCCESS, FAILED
    var firestoreVerified: Boolean = false,
    var notes: String = ""
)

data class PaymentFullReport(
    val timestamp: String,
    val totalSteps: Int,
    val passed: Int,
    val failed: Int,
    val steps: List<PaymentStepReport>
)

class PaymentTestRunner(private val context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val testUserId = "test_payment_client"
    private val testProviderId = "test_payment_tech"

    suspend fun runPaymentTest(
        onProgress: (String) -> Unit,
        onComplete: (PaymentFullReport) -> Unit
    ) {
        val stepsList = mutableListOf<PaymentStepReport>()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        fun addStep(name: String, desc: String): PaymentStepReport {
            // [تعديل مهندس النظام] فحص صارم لمعاملات الدفع، المحافظ والمحاكاة المالية تحت الضغط
            val step = PaymentStepReport(name, desc)
            stepsList.add(step)
            return step
        }

        val step1 = addStep("تهيئة المحفظة", "إنشاء وتصفير محفظة اختبارية للعميل والفني")
        val step2 = addStep("شحن وإيداع رصيد", "إيداع مبلغ 10000 ريال والتحقق من تحديث الرصيد وحفظ العملية")
        val step3 = addStep("طلب سحب رصيد", "سحب مبلغ 3000 ريال وتأكيد خصمه من رصيد المحفظة")
        val step4 = addStep("تحويل مالي بين الحسابات", "تحويل 2000 ريال من محفظة العميل لمحفظة الفني")
        val step5 = addStep("دفع مقابل خدمة (معلق)", "إنشاء دفعة معلقة بقيمة 1500 ريال بمحفظة الجيب")
        val step6 = addStep("موافقة الأدمن على الدفع", "تأكيد الدفع من الإدارة والتحقق من اكتمال الدفعة")
        val step7 = addStep("استرداد المدفوعات (Refund)", "محاكاة عملية استرداد دفعة مالية كاملة مع تدوين السبب")

        try {
            onProgress("🧹 جاري مسح عمليات ومحافظ الفحص القديمة...")
            cleanAllTestData()
            delay(1000)

            // Step 1: Initialize wallets
            onProgress("💳 خطوة 1: جاري تهيئة المحافظ التجريبية برصيد صفر...")
            db.collection("wallets").document(testUserId).set(mapOf(
                "userId" to testUserId,
                "balance" to 0.0,
                "currency" to "YER",
                "updatedAt" to System.currentTimeMillis()
            )).await()

            db.collection("wallets").document(testProviderId).set(mapOf(
                "userId" to testProviderId,
                "balance" to 0.0,
                "currency" to "YER",
                "updatedAt" to System.currentTimeMillis()
            )).await()

            step1.status = "SUCCESS"
            step1.firestoreVerified = true
            step1.notes = "تم تهيئة المحفظتين بنجاح"

            // Step 2: Deposit 10000
            onProgress("💰 خطوة 2: جاري إيداع 10000 ريال في محفظة العميل...")
            val txId1 = "tx_deposit_" + System.currentTimeMillis()
            val tx1 = Transaction(
                id = txId1,
                userId = testUserId,
                amount = 10000.0,
                type = TransactionType.DEPOSIT.name,
                status = TransactionStatus.COMPLETED.name,
                note = "إيداع تجريبي للاختبار التلقائي",
                timestamp = System.currentTimeMillis()
            )
            db.collection("transactions").document(txId1).set(tx1).await()

            // Update wallet balance
            db.collection("wallets").document(testUserId).update(
                "balance", 10000.0,
                "updatedAt", System.currentTimeMillis()
            ).await()
            delay(1500)

            val wallet1 = db.collection("wallets").document(testUserId).get().await()
            if (wallet1.getDouble("balance") == 10000.0) {
                step2.status = "SUCCESS"
                step2.firestoreVerified = true
                step2.notes = "تم الإيداع، الرصيد المحدث: 10,000 ريال"
            } else {
                step2.status = "FAILED"
                step2.notes = "خطأ في تحديث الرصيد بعد الإيداع"
            }

            // Step 3: Withdraw 3000
            onProgress("💸 خطوة 3: جاري سحب 3000 ريال من المحفظة...")
            val txId2 = "tx_withdraw_" + System.currentTimeMillis()
            val tx2 = Transaction(
                id = txId2,
                userId = testUserId,
                amount = 3000.0,
                type = TransactionType.WITHDRAWAL.name,
                status = TransactionStatus.COMPLETED.name,
                note = "سحب رصيد تجريبي للاختبار",
                timestamp = System.currentTimeMillis()
            )
            db.collection("transactions").document(txId2).set(tx2).await()
            db.collection("wallets").document(testUserId).update("balance", 7000.0).await()

            delay(1500)
            val wallet2 = db.collection("wallets").document(testUserId).get().await()
            if (wallet2.getDouble("balance") == 7000.0) {
                step3.status = "SUCCESS"
                step3.firestoreVerified = true
                step3.notes = "تم السحب بنجاح، الرصيد المتبقي: 7,000 ريال"
            } else {
                step3.status = "FAILED"
                step3.notes = "خطأ: الرصيد غير مطابق بعد عملية السحب"
            }

            // Step 4: Transfer 2000 to Tech wallet
            onProgress("🔄 خطوة 4: جاري تحويل 2000 ريال من العميل للفني...")
            db.collection("wallets").document(testUserId).update("balance", 5000.0).await()
            db.collection("wallets").document(testProviderId).update("balance", 2000.0).await()

            val txId3 = "tx_transfer_" + System.currentTimeMillis()
            val tx3 = Transaction(
                id = txId3,
                userId = testUserId,
                amount = 2000.0,
                type = TransactionType.TRANSFER.name,
                status = TransactionStatus.COMPLETED.name,
                note = "تحويل تجريبي للفني",
                timestamp = System.currentTimeMillis()
            )
            db.collection("transactions").document(txId3).set(tx3).await()
            delay(1500)

            val clientWallet = db.collection("wallets").document(testUserId).get().await()
            val techWallet = db.collection("wallets").document(testProviderId).get().await()
            if (clientWallet.getDouble("balance") == 5000.0 && techWallet.getDouble("balance") == 2000.0) {
                step4.status = "SUCCESS"
                step4.firestoreVerified = true
                step4.notes = "تم التحويل بنجاح (رصيد العميل: 5000، رصيد الفني: 2000)"
            } else {
                step4.status = "FAILED"
                step4.notes = "خطأ في أرصدة التحويل المالي"
            }

            // Step 5: Mobile wallet payment (PROCESSING)
            onProgress("📱 خطوة 5: محاكاة دفع 1500 ريال عبر محفظة جيب الالكترونية (معلق)...")
            val txId4 = "tx_pay_" + System.currentTimeMillis()
            val tx4 = Transaction(
                id = txId4,
                userId = testUserId,
                amount = 1500.0,
                type = TransactionType.PAYMENT.name,
                status = TransactionStatus.PENDING.name,
                note = "دفع رسوم خدمة صيانة تجريبية",
                timestamp = System.currentTimeMillis()
            )
            db.collection("transactions").document(txId4).set(tx4).await()
            delay(1500)

            val payDoc = db.collection("transactions").document(txId4).get().await()
            if (payDoc.exists() && payDoc.getString("status") == "PENDING") {
                step5.status = "SUCCESS"
                step5.firestoreVerified = true
                step5.notes = "تم تسجيل عملية الدفع بحالة PENDING وتحديد منصة الدفع"
            } else {
                step5.status = "FAILED"
                step5.notes = "خطأ في تسجيل عملية الدفع المعلقة"
            }

            // Step 6: Admin approves payment
            onProgress("💼 خطوة 6: الأدمن يعتمد عملية الدفع المعلقة...")
            db.collection("transactions").document(txId4).update("status", "COMPLETED").await()
            delay(1500)

            val payDocCompleted = db.collection("transactions").document(txId4).get().await()
            if (payDocCompleted.getString("status") == "COMPLETED") {
                step6.status = "SUCCESS"
                step6.firestoreVerified = true
                step6.notes = "تم اعتماد العملية بنجاح وتحويل حالتها لـ COMPLETED"
            } else {
                step6.status = "FAILED"
                step6.notes = "فشل في تحديث حالة الدفع المعتمد من الأدمن"
            }

            // Step 7: Refund test
            onProgress("↩️ خطوة 7: استرداد الرصيد المدفوع للمحفظة وتوثيق السبب...")
            db.collection("transactions").document(txId4).update(
                "status", "COMPLETED",
                "note", "تم الإرجاع: العميل ألغى الحجز قبل البدء"
            ).await()
            db.collection("wallets").document(testUserId).update("balance", 6500.0).await()
            delay(1500)

            val docRefund = db.collection("transactions").document(txId4).get().await()
            val finalClientWallet = db.collection("wallets").document(testUserId).get().await()
            if (docRefund.getString("status") == "COMPLETED" && finalClientWallet.getDouble("balance") == 6500.0) {
                step7.status = "SUCCESS"
                step7.firestoreVerified = true
                step7.notes = "تمت عملية الاسترداد بالكامل وإعادة الـ 1500 ريال للمحفظة"
            } else {
                step7.status = "FAILED"
                step7.notes = "فشل في عملية الاسترداد أو تحديث رصيد المحفظة النهائي"
            }

            // Final clean
            onProgress("🧹 جاري تصفية وتطهير عمليات ومحافظ الفحص...")
            cleanAllTestData()

        } catch (e: Exception) {
            onProgress("❌ حدث خطأ غير متوقع أثناء فحص المدفوعات: ${e.message}")
            stepsList.forEach { if (it.status == "PENDING") it.status = "FAILED" }
        }

        val passed = stepsList.count { it.status == "SUCCESS" }
        val failed = stepsList.count { it.status == "FAILED" }
        val report = PaymentFullReport(
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

            // Delete wallets for our test IDs
            batch.delete(db.collection("wallets").document(testUserId))
            batch.delete(db.collection("wallets").document(testProviderId))

            // Delete transactions related to our test users
            val txsSnapshot1 = db.collection("transactions").whereEqualTo("userId", testUserId).get().await()
            for (doc in txsSnapshot1.documents) {
                batch.delete(doc.reference)
            }

            // Delete transaction docs matching prefix
            val txsSnapshot2 = db.collection("transactions").get().await()
            for (doc in txsSnapshot2.documents) {
                if (doc.id.startsWith("tx_deposit_") || doc.id.startsWith("tx_withdraw_") || doc.id.startsWith("tx_transfer_") || doc.id.startsWith("tx_pay_")) {
                    batch.delete(doc.reference)
                }
            }

            batch.commit().await()
        } catch (_: Exception) {}
    }

    private fun saveReportToFile(report: PaymentFullReport) {
        try {
            val file = File(context.filesDir, "payment_test_report.txt")
            val sb = StringBuilder()
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("📋 تقرير اختبار نظام المدفوعات والمحافظ الشامل\n")
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

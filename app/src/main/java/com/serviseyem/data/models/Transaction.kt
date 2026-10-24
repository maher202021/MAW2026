package com.serviseyem.data.models

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import java.io.Serializable
import java.util.UUID

/**
 * 💳 نوع العملية المالية
 */
@Keep
enum class TransactionType {
    DEPOSIT,     // إيداع رصيد
    WITHDRAWAL,  // سحب رصيد
    PAYMENT,     // سداد قيمة خدمة أو حجز
    TRANSFER,    // تحويل رصيد
    REFUND       // استرداد أموال
}

/**
 * 📊 حالة العملية المالية
 */
@Keep
enum class TransactionStatus {
    PENDING,    // قيد المراجعة والمعالجة
    COMPLETED,  // مكتملة بنجاح
    FAILED,     // فشلت
    CANCELLED   // ملغاة
}

/**
 * 💵 نموذج بيانات المعاملة المالية (Transaction Entity)
 * يدعم التسلسل لـ Firestore و Moshi للتخزين المحلي والسحابي
 */
@Keep
@JsonClass(generateAdapter = true)
data class Transaction(
    val id: String = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}",
    val walletId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val balanceAfter: Double = 0.0,
    val type: String = TransactionType.DEPOSIT.name,
    val status: String = TransactionStatus.COMPLETED.name,
    val note: String = "",
    val currency: String = "YER", // YER, USD, SAR
    val targetWalletId: String = "",
    val paymentMethod: String = "INTERNAL_WALLET", // KURAINI, ONE_CASH, JAWALI, INTERNAL_WALLET
    val referenceNumber: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable {

    val isCredit: Boolean
        get() = type == TransactionType.DEPOSIT.name || type == TransactionType.REFUND.name

    val formattedAmount: String
        get() {
            val prefix = if (isCredit) "+" else "-"
            return "$prefix${String.format(java.util.Locale.US, "%,.0f", amount)} $currency"
        }
}

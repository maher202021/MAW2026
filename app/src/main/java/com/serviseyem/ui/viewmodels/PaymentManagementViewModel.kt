package com.serviseyem.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.InternalWalletEntity
import com.serviseyem.data.PaymentEntity
import com.serviseyem.data.PaymentWalletEntity
import com.serviseyem.data.WalletTransactionEntity
import com.serviseyem.ui.helpers.AppState
import com.serviseyem.utils.AnalyticsEventsHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class PaymentManagementViewModel @Inject constructor(
    val appState: AppState,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    val crud = AdminCrudOperations(db)

    val _internalWallets: MutableStateFlow<List<InternalWalletEntity>> get() = appState._internalWallets
    val internalWallets: StateFlow<List<InternalWalletEntity>> = _internalWallets.asStateFlow()

    val _walletTransactions: MutableStateFlow<List<WalletTransactionEntity>> get() = appState._walletTransactions
    val walletTransactions: StateFlow<List<WalletTransactionEntity>> = _walletTransactions.asStateFlow()

    val _paymentWallets: MutableStateFlow<List<PaymentWalletEntity>> get() = appState._paymentWallets
    val paymentWallets: StateFlow<List<PaymentWalletEntity>> = _paymentWallets.asStateFlow()

    val _payments: MutableStateFlow<List<PaymentEntity>> get() = appState._payments
    val payments: StateFlow<List<PaymentEntity>> = _payments.asStateFlow()

    var onTriggerNotification: ((String) -> Unit)? = null

    fun saveInternalWallet(wallet: InternalWalletEntity) {
        val targetId = wallet.id.ifBlank { UUID.randomUUID().toString() }
        val finalWallet = if (wallet.id.isBlank()) wallet.copy(id = targetId) else wallet
        viewModelScope.launch {
            crud.saveEntity("internal_wallets", targetId, finalWallet,
                onSuccess = { onTriggerNotification?.invoke("✅ تم تحديث المحفظة بنجاح") },
                onError = { onTriggerNotification?.invoke("❌ فشل تحديث المحفظة: ${it.message}") }
            )
        }
    }

    fun performWalletTransaction(
        walletId: String,
        amount: Double,
        type: String, // DEPOSIT, WITHDRAW, TRANSFER, FEE, REWARD
        description: String,
        adminName: String = "الأدمن"
    ) {
        if (walletId.isBlank() || amount <= 0.0) {
            onTriggerNotification?.invoke("⚠️ بيانات الحركة المالية غير صالحة")
            return
        }
        val localWallet = _internalWallets.value.find { it.id == walletId }
        val isCredit = type == "DEPOSIT" || type == "REWARD"
        if (!isCredit && localWallet != null && amount > localWallet.balance) {
            onTriggerNotification?.invoke("❌ الرصيد غير كافٍ لإتمام العملية (الرصيد الحالي: ${localWallet.balance})")
            return
        }

        val now = System.currentTimeMillis()
        val txId = UUID.randomUUID().toString()
        val safeAdminName = adminName.trim().ifBlank { "الأدمن" }
        val finalNote = if (description.contains(safeAdminName)) description else "$description (بواسطة: $safeAdminName)"

        db.runTransaction { transaction ->
            val walletRef = db.collection("internal_wallets").document(walletId)
            val snap = transaction.get(walletRef)
            val currentBalance = if (snap.exists()) {
                snap.getDouble("balance") ?: localWallet?.balance ?: 0.0
            } else {
                localWallet?.balance ?: 0.0
            }

            if (!isCredit && amount > currentBalance) {
                throw IllegalStateException("الرصيد غير كافٍ لإتمام العملية (الرصيد المتاح: $currentBalance)")
            }

            val newBalance = if (isCredit) {
                currentBalance + amount
            } else {
                currentBalance - amount
            }

            val tx = WalletTransactionEntity(
                id = txId,
                walletId = walletId,
                type = type,
                amount = amount,
                balanceAfter = newBalance,
                note = finalNote,
                performByAdmin = true,
                timestamp = now,
                adminName = safeAdminName
            )

            if (snap.exists()) {
                transaction.update(
                    walletRef,
                    mapOf(
                        "balance" to newBalance,
                        "updatedAt" to now,
                        "lastTransactionAt" to now
                    )
                )
            } else if (localWallet != null) {
                transaction.set(
                    walletRef,
                    localWallet.copy(balance = newBalance, updatedAt = now)
                )
            } else {
                throw IllegalStateException("المحفظة المطلوبة غير موجودة")
            }

            val txRef = db.collection("wallet_transactions").document(txId)
            transaction.set(txRef, tx)
            newBalance
        }.addOnSuccessListener { updatedBalance ->
            _internalWallets.value = _internalWallets.value.map {
                if (it.id == walletId) it.copy(balance = updatedBalance, updatedAt = now) else it
            }
            onTriggerNotification?.invoke("✅ تم تنفيذ حركة مالية ($type) بمبلغ $amount بنجاح بواسطة $safeAdminName")
        }.addOnFailureListener { e ->
            onTriggerNotification?.invoke("❌ فشل تنفيذ الحركة المالية: ${e.message}")
        }
    }

    fun addPaymentWallet(wallet: PaymentWalletEntity) {
        val targetId = wallet.id.ifBlank { UUID.randomUUID().toString() }
        val finalWallet = if (wallet.id.isBlank()) wallet.copy(id = targetId) else wallet
        viewModelScope.launch {
            crud.saveEntity("payment_wallets", targetId, finalWallet,
                onSuccess = { onTriggerNotification?.invoke("✅ تم إضافة المحفظة المالية بنجاح") },
                onError = { onTriggerNotification?.invoke("❌ فشل إضافة المحفظة: ${it.message}") }
            )
        }
    }

    fun updatePaymentWallet(wallet: PaymentWalletEntity) {
        val targetId = wallet.id.ifBlank { UUID.randomUUID().toString() }
        val finalWallet = if (wallet.id.isBlank()) wallet.copy(id = targetId) else wallet
        viewModelScope.launch {
            crud.saveEntity("payment_wallets", targetId, finalWallet,
                onSuccess = { onTriggerNotification?.invoke("✏️ تم تحديث بيانات المحفظة بنجاح") },
                onError = { onTriggerNotification?.invoke("❌ فشل تحديث المحفظة: ${it.message}") }
            )
        }
    }

    fun deletePaymentWallet(walletId: String) {
        viewModelScope.launch {
            crud.deleteEntity("payment_wallets", walletId, softDelete = false,
                onSuccess = { onTriggerNotification?.invoke("🗑️ تم حذف المحفظة بنجاح") }
            )
        }
    }

    fun togglePaymentWalletVisibility(walletId: String, currentVisible: Boolean) {
        viewModelScope.launch {
            crud.toggleEntityStatus("payment_wallets", walletId, "isVisible", !currentVisible)
        }
    }

    fun createPayment(payment: PaymentEntity) {
        val targetId = payment.id.ifBlank { UUID.randomUUID().toString() }
        val finalPayment = if (payment.id.isBlank()) payment.copy(id = targetId) else payment
        viewModelScope.launch {
            AnalyticsEventsHelper.logPaymentInitiated(null, targetId, finalPayment.method, finalPayment.amount)
            crud.saveEntity("payments", targetId, finalPayment,
                onSuccess = { onTriggerNotification?.invoke("💳 تم تسجيل عملية الدفع بنجاح") },
                onError = { onTriggerNotification?.invoke("❌ فشل تسجيل الدفع: ${it.message}") }
            )
        }
    }

    fun confirmPayment(paymentId: String, receiptNumber: String, adminName: String) {
        viewModelScope.launch {
            crud.updateFields("payments", paymentId, mapOf(
                "status" to "CONFIRMED",
                "receiptNumber" to receiptNumber,
                "verifiedBy" to adminName,
                "verifiedAt" to System.currentTimeMillis()
            ), onSuccess = {
                onTriggerNotification?.invoke("✅ تم تأكيد استلام الدفعة")
            })
        }
    }

    fun verifyPayment(paymentId: String, isVerified: Boolean, note: String, adminName: String) {
        val cleanId = paymentId.trim()
        if (cleanId.isBlank()) {
            onTriggerNotification?.invoke("❌ معرف الدفعة غير صالح")
            return
        }
        val existingPayment = _payments.value.find { it.id == cleanId }
        if (existingPayment == null) {
            onTriggerNotification?.invoke("❌ لم يتم العثور على معاملة الدفع المطلوبة: $cleanId")
            return
        }
        val status = if (isVerified) "VERIFIED" else "REJECTED"
        viewModelScope.launch {
            crud.updateFields("payments", cleanId, mapOf(
                "status" to status,
                "adminNote" to note,
                "verifiedBy" to adminName,
                "verifiedAt" to System.currentTimeMillis()
            ), onSuccess = {
                onTriggerNotification?.invoke("تم مراجعة الدفعة: $status")
            })
        }
    }

    fun refundPayment(paymentId: String, reason: String) {
        viewModelScope.launch {
            crud.updateFields("payments", paymentId, mapOf(
                "status" to "REFUNDED",
                "refundReason" to reason,
                "refundedAt" to System.currentTimeMillis()
            ), onSuccess = {
                onTriggerNotification?.invoke("تم استرجاع الدفعة بنجاح")
            })
        }
    }
}

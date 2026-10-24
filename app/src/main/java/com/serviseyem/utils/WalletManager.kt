package com.serviseyem.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Keep
import com.serviseyem.data.models.Transaction
import com.serviseyem.data.models.TransactionStatus
import com.serviseyem.data.models.TransactionType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

typealias Transaction = com.serviseyem.data.models.Transaction

@Keep
data class Wallet(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val type: String = "USER", // "USER", "PROVIDER", "STORE", "RESTAURANT"
    val balanceYer: Double = 0.0,
    val balanceUsd: Double = 0.0,
    val balanceSar: Double = 0.0,
    val balance: Double = 0.0, // Default YER compatibility
    val currency: String = "YER",
    val status: String = "ACTIVE", // "ACTIVE", "FROZEN", "CLOSED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 💰 WalletManager
 * إدارة المحافظ الإلكترونية المتعددة العملات والمعاملات المالية مع المزامنة السحابية والتخزين المحلي
 */
class WalletManager(private val context: Context? = null) {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val sharedPrefs: SharedPreferences? by lazy {
        context?.getSharedPreferences("yemen_wallet_secure_cache", Context.MODE_PRIVATE)
    }

    // In-memory / reactive state cache
    private val localWallets = mutableMapOf<String, Wallet>()
    private val _transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())
    val transactionsFlow: StateFlow<List<Transaction>> = _transactionsFlow.asStateFlow()

    init {
        loadFromLocalStorage()
    }

    private fun loadFromLocalStorage() {
        // Clear out any legacy fake wallet_user_default cache if present
        sharedPrefs?.edit()?.remove("wallet_user_default")?.apply()

        // Try load cached transactions
        sharedPrefs?.getString("cached_txs_json", null)?.let { json ->
            try {
                val type = Types.newParameterizedType(List::class.java, Transaction::class.java)
                val adapter = moshi.adapter<List<Transaction>>(type)
                val list = adapter.fromJson(json) ?: emptyList()
                // Filter out any legacy dummy transactions
                val cleanList = list.filterNot { it.id == "tx_101" || it.id == "tx_102" || it.userId == "user_default" }
                _transactionsFlow.value = cleanList
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
    }

    private fun saveTransactionsToCache(list: List<Transaction>) {
        try {
            val type = Types.newParameterizedType(List::class.java, Transaction::class.java)
            val adapter = moshi.adapter<List<Transaction>>(type)
            val json = adapter.toJson(list)
            sharedPrefs?.edit()?.putString("cached_txs_json", json)?.apply()
        } catch (ignored: Exception) {}
    }

    /**
     * 1. إنشاء أو جلب محفظة رقمية
     */
    fun createWallet(userId: String, type: String = "USER"): Result<Wallet> {
        return try {
            val walletId = "wallet_${userId.ifBlank { UUID.randomUUID().toString() }}"
            val wallet = Wallet(
                id = walletId,
                userId = userId,
                type = type.uppercase(),
                balanceYer = 0.0,
                balanceUsd = 0.0,
                balanceSar = 0.0,
                balance = 0.0,
                currency = "YER",
                status = "ACTIVE",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            localWallets[wallet.id] = wallet
            localWallets[userId] = wallet

            try {
                firestore.collection("wallets").document(wallet.id).set(wallet)
            } catch (ignored: Exception) {}

            Result.success(wallet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 2. إيداع رصيد في المحفظة بعملات متعددة باستخدام Transcation لضمان التزامن والأمان
     */
    suspend fun deposit(
        walletId: String,
        amount: Double,
        currency: String = "YER",
        note: String = "إيداع رصيد",
        paymentMethod: String = "INTERNAL_WALLET"
    ): Result<Transaction> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            if (amount <= 0) return@withContext Result.failure(IllegalArgumentException("مبلغ الإيداع يجب أن يكون أكبر من الصفر"))
            if (amount % 1 != 0.0 && currency.uppercase() == "YER") {
                return@withContext Result.failure(IllegalArgumentException("الريال اليمني لا يدعم الكسور"))
            }

            val cur = currency.uppercase()
            val walletRef = firestore.collection("wallets").document(walletId)

            val txResult = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(walletRef)
                
                val current = if (snapshot.exists()) {
                    snapshot.toObject(Wallet::class.java) ?: Wallet(id = walletId, userId = walletId.replace("wallet_", ""))
                } else {
                    Wallet(id = walletId, userId = walletId.replace("wallet_", ""))
                }

                if (current.status == "FROZEN") {
                    throw IllegalStateException("المحفظة مجمدة ولا يمكن إجراء إيداعات حالياً")
                }

                val newYer = if (cur == "YER") current.balanceYer + amount else current.balanceYer
                val newUsd = if (cur == "USD") current.balanceUsd + amount else current.balanceUsd
                val newSar = if (cur == "SAR") current.balanceSar + amount else current.balanceSar
                val primaryBalance = newYer

                val updated = current.copy(
                    balanceYer = newYer,
                    balanceUsd = newUsd,
                    balanceSar = newSar,
                    balance = primaryBalance,
                    updatedAt = System.currentTimeMillis()
                )
                
                transaction.set(walletRef, updated)

                val tx = Transaction(
                    id = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}",
                    walletId = updated.id,
                    userId = updated.userId,
                    type = TransactionType.DEPOSIT.name,
                    amount = amount,
                    balanceAfter = when (cur) {
                        "USD" -> newUsd
                        "SAR" -> newSar
                        else -> newYer
                    },
                    currency = cur,
                    paymentMethod = paymentMethod,
                    note = note.ifBlank { "إيداع رصيد" },
                    timestamp = System.currentTimeMillis(),
                    status = TransactionStatus.COMPLETED.name
                )

                val txRef = firestore.collection("wallet_transactions").document(tx.id)
                transaction.set(txRef, tx)

                Pair(updated, tx)
            }.await()

            val updated = txResult.first
            val tx = txResult.second
            
            localWallets[updated.id] = updated
            localWallets[updated.userId] = updated
            val currentList = _transactionsFlow.value.toMutableList()
            currentList.add(0, tx)
            _transactionsFlow.value = currentList
            saveTransactionsToCache(currentList)

            Result.success(tx)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 3. سحب رصيد من المحفظة باستخدام runTransaction للأمان المالي
     */
    suspend fun withdraw(
        walletId: String,
        amount: Double,
        currency: String = "YER",
        note: String = "سحب رصيد"
    ): Result<Transaction> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            if (amount <= 0) return@withContext Result.failure(IllegalArgumentException("مبلغ السحب يجب أن يكون أكبر من الصفر"))
            if (amount % 1 != 0.0 && currency.uppercase() == "YER") {
                return@withContext Result.failure(IllegalArgumentException("الريال اليمني لا يدعم الكسور"))
            }

            val cur = currency.uppercase()
            val walletRef = firestore.collection("wallets").document(walletId)

            val txResult = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(walletRef)
                
                val current = if (snapshot.exists()) {
                    snapshot.toObject(Wallet::class.java) ?: Wallet(id = walletId, userId = walletId.replace("wallet_", ""))
                } else {
                    Wallet(id = walletId, userId = walletId.replace("wallet_", ""))
                }

                if (current.status == "FROZEN") {
                    throw IllegalStateException("المحفظة مجمدة ولا يمكن السحب منها")
                }

                val available = when (cur) {
                    "USD" -> current.balanceUsd
                    "SAR" -> current.balanceSar
                    else -> current.balanceYer
                }

                if (available < amount) {
                    throw IllegalStateException("الرصيد غير كافٍ. الرصيد المتاح: $available $cur")
                }

                val newYer = if (cur == "YER") current.balanceYer - amount else current.balanceYer
                val newUsd = if (cur == "USD") current.balanceUsd - amount else current.balanceUsd
                val newSar = if (cur == "SAR") current.balanceSar - amount else current.balanceSar

                val updated = current.copy(
                    balanceYer = newYer,
                    balanceUsd = newUsd,
                    balanceSar = newSar,
                    balance = newYer,
                    updatedAt = System.currentTimeMillis()
                )
                
                transaction.set(walletRef, updated)

                val tx = Transaction(
                    id = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}",
                    walletId = updated.id,
                    userId = updated.userId,
                    type = TransactionType.WITHDRAWAL.name,
                    amount = amount,
                    balanceAfter = when (cur) {
                        "USD" -> newUsd
                        "SAR" -> newSar
                        else -> newYer
                    },
                    currency = cur,
                    note = note.ifBlank { "سحب رصيد" },
                    timestamp = System.currentTimeMillis(),
                    status = TransactionStatus.COMPLETED.name
                )
                
                val txRef = firestore.collection("wallet_transactions").document(tx.id)
                transaction.set(txRef, tx)

                Pair(updated, tx)
            }.await()

            val updated = txResult.first
            val tx = txResult.second
            
            localWallets[updated.id] = updated
            localWallets[updated.userId] = updated
            val currentList = _transactionsFlow.value.toMutableList()
            currentList.add(0, tx)
            _transactionsFlow.value = currentList
            saveTransactionsToCache(currentList)

            Result.success(tx)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 4. تحويل رصيد بين محفظتين باستخدام runTransaction المزدوج
     */
    suspend fun transfer(
        fromWalletId: String,
        toWalletId: String,
        amount: Double,
        currency: String = "YER"
    ): Result<Transaction> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            if (amount <= 0) return@withContext Result.failure(IllegalArgumentException("مبلغ التحويل غير صالح"))
            
            val cur = currency.uppercase()
            val senderRef = firestore.collection("wallets").document(fromWalletId)
            val receiverRef = firestore.collection("wallets").document(toWalletId)

            val txResult = firestore.runTransaction { transaction ->
                val senderSnap = transaction.get(senderRef)
                val receiverSnap = transaction.get(receiverRef)
                
                val sender = if (senderSnap.exists()) senderSnap.toObject(Wallet::class.java)!! else Wallet(id = fromWalletId, userId = fromWalletId.replace("wallet_", ""))
                val receiver = if (receiverSnap.exists()) receiverSnap.toObject(Wallet::class.java)!! else Wallet(id = toWalletId, userId = toWalletId.replace("wallet_", ""))

                if (sender.status == "FROZEN" || receiver.status == "FROZEN") {
                    throw IllegalStateException("إحدى المحافظ مجمدة")
                }

                val senderAvailable = when (cur) {
                    "USD" -> sender.balanceUsd
                    "SAR" -> sender.balanceSar
                    else -> sender.balanceYer
                }

                if (senderAvailable < amount) {
                    throw IllegalStateException("رصيد المرسل غير كافٍ")
                }

                val updatedSender = sender.copy(
                    balanceYer = if (cur == "YER") sender.balanceYer - amount else sender.balanceYer,
                    balanceUsd = if (cur == "USD") sender.balanceUsd - amount else sender.balanceUsd,
                    balanceSar = if (cur == "SAR") sender.balanceSar - amount else sender.balanceSar,
                    balance = if (cur == "YER") sender.balanceYer - amount else sender.balanceYer,
                    updatedAt = System.currentTimeMillis()
                )

                val updatedReceiver = receiver.copy(
                    balanceYer = if (cur == "YER") receiver.balanceYer + amount else receiver.balanceYer,
                    balanceUsd = if (cur == "USD") receiver.balanceUsd + amount else receiver.balanceUsd,
                    balanceSar = if (cur == "SAR") receiver.balanceSar + amount else receiver.balanceSar,
                    balance = if (cur == "YER") receiver.balanceYer + amount else receiver.balanceYer,
                    updatedAt = System.currentTimeMillis()
                )
                
                transaction.set(senderRef, updatedSender)
                transaction.set(receiverRef, updatedReceiver)

                val tx = Transaction(
                    id = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}",
                    walletId = updatedSender.id,
                    userId = updatedSender.userId,
                    type = TransactionType.TRANSFER.name,
                    amount = amount,
                    balanceAfter = when (cur) {
                        "USD" -> updatedSender.balanceUsd
                        "SAR" -> updatedSender.balanceSar
                        else -> updatedSender.balanceYer
                    },
                    currency = cur,
                    targetWalletId = updatedReceiver.id,
                    note = "تحويل إلى $toWalletId",
                    timestamp = System.currentTimeMillis(),
                    status = TransactionStatus.COMPLETED.name
                )
                
                val txRef = firestore.collection("wallet_transactions").document(tx.id)
                transaction.set(txRef, tx)
                
                Pair(updatedSender, tx)
            }.await()

            val updatedSender = txResult.first
            val tx = txResult.second
            
            localWallets[updatedSender.id] = updatedSender
            localWallets[updatedSender.userId] = updatedSender
            val currentList = _transactionsFlow.value.toMutableList()
            currentList.add(0, tx)
            _transactionsFlow.value = currentList
            saveTransactionsToCache(currentList)

            Result.success(tx)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 5. الحصول على الرصيد الحالي حسب العملة
     */
    fun getBalance(walletId: String, currency: String = "YER"): Double {
        val wallet = getOrCreateWallet(walletId)
        return when (currency.uppercase()) {
            "USD" -> wallet.balanceUsd
            "SAR" -> wallet.balanceSar
            else -> wallet.balanceYer
        }
    }

    /**
     * 6. سجل المعاملات لمحفظة معينة
     */
    fun getTransactions(walletId: String, limit: Int = 50): List<Transaction> {
        val clean = walletId.trim()
        return _transactionsFlow.value
            .filter { it.walletId == clean || it.userId == clean }
            .take(limit)
    }

    /**
     * 7. سجل المعاملات لمستخدم معين
     */
    fun getTransactionHistory(userId: String): List<Transaction> {
        return getTransactions(userId)
    }

    /**
     * 8. تدفق حي لمعاملات المستخدم عبر Firestore
     */
    fun getLiveTransactionsFlow(userId: String): Flow<List<Transaction>> = callbackFlow {
        val listener = firestore.collection("wallet_transactions")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(getTransactions(userId))
                    return@addSnapshotListener
                }
                val list = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun freezeWallet(walletId: String, reason: String = ""): Result<Boolean> {
        return try {
            val wallet = getOrCreateWallet(walletId)
            val updated = wallet.copy(status = "FROZEN", updatedAt = System.currentTimeMillis())
            localWallets[wallet.id] = updated
            localWallets[wallet.userId] = updated
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun unfreezeWallet(walletId: String): Result<Boolean> {
        return try {
            val wallet = getOrCreateWallet(walletId)
            val updated = wallet.copy(status = "ACTIVE", updatedAt = System.currentTimeMillis())
            localWallets[wallet.id] = updated
            localWallets[wallet.userId] = updated
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getOrCreateWallet(walletId: String): Wallet {
        return localWallets[walletId] ?: run {
            val w = Wallet(id = walletId, userId = walletId, balanceYer = 0.0, balance = 0.0)
            localWallets[walletId] = w
            w
        }
    }
}

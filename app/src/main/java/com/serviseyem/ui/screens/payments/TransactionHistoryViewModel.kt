package com.serviseyem.ui.screens.payments

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.models.Transaction
import com.serviseyem.utils.WalletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Sealed class representing UI States for transaction history.
 */
sealed class TransactionHistoryUiState {
    object Loading : TransactionHistoryUiState()
    data class Success(
        val balance: Double,
        val transactions: List<Transaction>,
        val totalDeposits: Double,
        val totalWithdrawals: Double
    ) : TransactionHistoryUiState()
    data class Error(val message: String) : TransactionHistoryUiState()
}

/**
 * ViewModel managing payment wallet and transactions.
 */
class TransactionHistoryViewModel(
    private val walletManager: WalletManager,
    private val walletId: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow("ALL")
    val typeFilter: StateFlow<String> = _typeFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val _balance = MutableStateFlow(0.0)

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        _allTransactions, _searchQuery, _typeFilter, _statusFilter
    ) { transactions, query, type, status ->
        transactions.filter { tx ->
            val matchesType = when (type) {
                "DEPOSIT" -> tx.type == "DEPOSIT"
                "WITHDRAWAL" -> tx.type == "WITHDRAWAL"
                "PAYMENT" -> tx.type == "PAYMENT"
                "TRANSFER" -> tx.type == "TRANSFER"
                "REFUND" -> tx.type == "REFUND"
                else -> true
            }
            val matchesStatus = when (status) {
                "COMPLETED" -> tx.status == "COMPLETED"
                "PENDING" -> tx.status == "PENDING"
                "FAILED" -> tx.status == "FAILED"
                "CANCELLED" -> tx.status == "CANCELLED"
                else -> true
            }
            val matchesSearch = query.isBlank() ||
                    tx.id.contains(query, ignoreCase = true) ||
                    tx.note.contains(query, ignoreCase = true)

            matchesType && matchesStatus && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<TransactionHistoryUiState>(TransactionHistoryUiState.Loading)
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    private var lastRefreshTime = 0L
    private val CACHE_TTL_MS = 15_000L

    init {
        refreshData(forceRefresh = true)
    }

    /**
     * Refreshes wallet balances and logs.
     */
    fun refreshData(forceRefresh: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!forceRefresh && now - lastRefreshTime < CACHE_TTL_MS && _allTransactions.value.isNotEmpty()) return
        lastRefreshTime = now
        viewModelScope.launch {
            try {
                val balance = walletManager.getBalance(walletId)
                val allTx = walletManager.getTransactions(walletId)

                _balance.value = balance
                _allTransactions.value = allTx

                val totalDep = allTx.filter { it.type == "DEPOSIT" && it.status == "COMPLETED" }.sumOf { it.amount }
                val totalWith = allTx.filter { (it.type == "WITHDRAWAL" || it.type == "PAYMENT") && it.status == "COMPLETED" }.sumOf { it.amount }

                _uiState.value = TransactionHistoryUiState.Success(
                    balance = balance,
                    transactions = allTx,
                    totalDeposits = totalDep,
                    totalWithdrawals = totalWith
                )
            } catch (e: Exception) {
                _uiState.value = TransactionHistoryUiState.Error(e.message ?: "فشل تحميل البيانات المالية")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(filter: String) {
        _typeFilter.value = filter
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    /**
     * Execute a custom deposit into the wallet.
     */
    fun deposit(amount: Double, note: String) {
        viewModelScope.launch {
            val result = walletManager.deposit(walletId, amount, "YER", note)
            if (result.isSuccess) {
                refreshData(forceRefresh = true)
            }
        }
    }

    /**
     * Execute a custom withdrawal from the wallet.
     */
    fun withdraw(amount: Double, note: String) {
        viewModelScope.launch {
            val result = walletManager.withdraw(walletId, amount, "YER", note)
            if (result.isSuccess) {
                refreshData(forceRefresh = true)
            }
        }
    }
}

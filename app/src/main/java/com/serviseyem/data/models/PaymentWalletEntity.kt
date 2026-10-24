package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class PaymentWalletEntity(
    val id: String = "",
    val ownerId: String = "",
    val balance: Double = 0.0,
    val isFrozen: Boolean = false,
    val provider: String = "other", // jeeb, alKarimi, jawaly, floosi, cashExchange, foreignCurrency, yemenMobile, mtc, sabafon, youssef, other
    val walletNumber: String = "",
    val accountName: String = "",
    val accountNameAr: String = "",
    val logoUrl: String = "",
    val bankName: String = "",
    val bankAccountNumber: String = "",
    val bankAccountName: String = "",
    val description: String = "",
    val status: String = "active", // active, inactive, suspended, deleted
    val walletType: String = "BOTH", // DEPOSIT, WITHDRAWAL, BOTH
    val currency: String = "YER", // YER, USD, SAR
    val isVisibleToUsers: Boolean = true,
    val qrCodePhoto: String = "",
    val isDefault: Boolean = false,
    val displayOrder: Int = 0,
    val minTransferAmount: Double = 0.0,
    val maxTransferAmount: Double = 1000000.0,
    val transferFee: Double = 0.0,
    val commissionRate: Double = 0.0,
    val instructions: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = ""
)

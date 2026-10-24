package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class PaymentEntity(
    val id: String = "",
    val userId: String = "",
    val providerId: String = "",
    val bookingId: String = "",
    val type: String = "service",
    val method: String = "cash", // cash, bankTransfer, mobileWallet, wallet
    val status: String = "PENDING", // PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, CANCELLED, DISPUTED
    val amount: Double = 0.0,
    val advanceAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val commission: Double = 0.0,
    val providerShare: Double = 0.0,
    val currency: String = "YER",
    val walletProvider: String = "",
    val walletNumber: String = "",
    val walletAccountName: String = "",
    val transferId: String = "",
    val transferPhoto: String = "", // base64 / URL
    val bankName: String = "",
    val accountNumber: String = "",
    val accountHolderName: String = "",
    val serviceType: String = "", // BOOKINGS, STORES, RESTAURANTS, MEDICAL, PROPERTIES, JOBS
    val isLinkedToBooking: Boolean = false,
    val bookingDate: Long? = null,
    val bookingServiceType: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,
    val paidAt: Long? = null,
    val verifiedAt: Long? = null,
    val verifiedBy: String = "",
    val verificationStatus: String = "PENDING", // PENDING, VERIFIED, REJECTED, DISPUTED
    val verificationNote: String = "",
    val adminNote: String = ""
)

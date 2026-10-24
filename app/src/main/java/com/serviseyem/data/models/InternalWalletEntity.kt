package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class InternalWalletEntity(
    val id: String = "", // owner id or phone
    val ownerType: String = "PROVIDER", // PROVIDER, STORE, RESTAURANT, CENTER, USER
    val ownerName: String = "",
    val ownerPhone: String = "",
    val balance: Double = 0.0,
    val currency: String = "YER",
    val isBlocked: Boolean = false,
    val defaultWalletNumber: String = "",
    val defaultWalletType: String = "alKarimi",
    val updatedAt: Long = System.currentTimeMillis(),
    val displayNameAr: String = "الكريمي"
) {
    val resolvedWalletCode: String
        get() = normalizeWalletCode(defaultWalletType)

    val resolvedDisplayNameAr: String
        get() = displayNameAr.ifBlank { getArabicNameForCode(resolvedWalletCode) }

    companion object {
        fun normalizeWalletCode(typeOrName: String): String {
            return when (typeOrName.trim().lowercase()) {
                "alkarimi", "al_karimi", "kuraimi", "الكريمي", "بنك الكريمي", "ام فلوس" -> "alKarimi"
                "jawali", "جوالي", "محفظة جوالي" -> "jawali"
                "jeeb", "جيب", "محفظة جيب" -> "jeeb"
                "onecash", "one_cash", "ون كاش", "محفظة ون كاش" -> "oneCash"
                "floosak", "فلوسك", "محفظة فلوسك" -> "floosak"
                "cash", "كاش", "محفظة كاش" -> "cash"
                "pyyes", "بيس", "محفظة بيس" -> "pyyes"
                "mobilemoney", "mobile_money", "موبايل موني" -> "mobileMoney"
                "tadhamon", "التضامن", "بنك التضامن" -> "tadhamon"
                else -> if (typeOrName.isNotBlank()) typeOrName.trim() else "alKarimi"
            }
        }

        fun getArabicNameForCode(code: String): String {
            return when (normalizeWalletCode(code)) {
                "alKarimi" -> "الكريمي"
                "jawali" -> "جوالي"
                "jeeb" -> "جيب"
                "oneCash" -> "ون كاش"
                "floosak" -> "فلوسك"
                "cash" -> "كاش"
                "pyyes" -> "بيس"
                "mobileMoney" -> "موبايل موني"
                "tadhamon" -> "بنك التضامن"
                else -> "الكريمي"
            }
        }
    }
}

@Keep
data class WalletTransactionEntity(
    val id: String = "",
    val walletId: String = "",
    val type: String = "DEPOSIT", // DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, REFUND
    val amount: Double = 0.0,
    val balanceAfter: Double = 0.0,
    val note: String = "",
    val performByAdmin: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val adminName: String = ""
)

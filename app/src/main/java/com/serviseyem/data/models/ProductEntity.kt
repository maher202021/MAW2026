package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class ProductEntity(
    val id: String = "",
    val storeId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val currency: String = "YER",
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val category: String = "",
    val isOffer: Boolean = false,
    val discountPercent: Int = 0,
    val oldPrice: Double = 0.0,
    val ownerId: String = ""
)

@Keep
data class OrderEntity(
    val id: String = "",
    val storeId: String = "",
    val storeName: String = "",
    val productId: String = "",
    val productName: String = "",
    val customerPhone: String = "",
    val customerName: String = "",
    val customerArea: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val totalAmount: Double = 0.0,
    val paymentId: String = "",
    val paymentStatus: String = "PENDING", // PENDING, PROCESSING, COMPLETED, FAILED
    val status: String = "PENDING", // PENDING, PROCESSING, COMPLETED, CANCELLED
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

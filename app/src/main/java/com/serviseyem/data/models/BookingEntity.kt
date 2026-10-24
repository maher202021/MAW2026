package com.serviseyem.data

import androidx.annotation.Keep

/**
 * 📝 نموذج الحجز (BookingEntity)
 *
 * الحقول المعتمدة:
 * - الاسم: [customerName]
 * - الهاتف: [customerPhone]
 * - المنطقة/العنوان: [customerArea]
 *
 * الحقول المهجورة (موجودة للتوافقية مع الأنظمة والملفات القديمة فقط ولا يجب استخدامها في المنطق الجديد):
 * - الأسماء البديلة: [fullName]، [clientName]
 * - الهواتف البديلة: [clientPhone]
 * - العناوين البديلة: [fullAddress]، [clientAddress]
 * - التواريخ البديلة: [dateString] (التاريخ المعتمد هو [date])
 * - الأوقات البديلة: [timeString] (الوقت المعتمد هو [time])
 */
@Keep
data class BookingEntity(
    val id: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerArea: String = "",
    val serviceType: String = "",
    val providerId: String = "",
    val providerName: String = "",
    @Deprecated("استخدم date بدلاً منها", ReplaceWith("date"))
    val dateString: String = "",
    @Deprecated("استخدم time بدلاً منها", ReplaceWith("time"))
    val timeString: String = "",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val rejectionReason: String = "",
    val pinCode: String = "",
    
    // New Fields requested for the enhanced booking system
    @Deprecated("استخدم customerName بدلاً منها", ReplaceWith("customerName"))
    val fullName: String = "",
    @Deprecated("استخدم customerArea بدلاً منها", ReplaceWith("customerArea"))
    val fullAddress: String = "",
    val bookingCode: String = "",
    val bookingNumber: String = "",      // BK-YYMMDDHHMMSS-XXXX
    val clientId: String = "",
    @Deprecated("استخدم customerName بدلاً منها", ReplaceWith("customerName"))
    val clientName: String = "",
    @Deprecated("استخدم customerPhone بدلاً منها", ReplaceWith("customerPhone"))
    val clientPhone: String = "",
    @Deprecated("استخدم customerArea بدلاً منها", ReplaceWith("customerArea"))
    val clientAddress: String = "",
    val providerPhone: String = "",
    val category: String = "",
    val subCategory: String = "",
    val serviceDetails: String = "",
    val date: String = "",
    val time: String = "",
    val scheduledAt: Long = 0L,
    val advancePayment: Double = 0.0,
    val paymentStatus: String = "unpaid",
    val paymentProofUrl: String = "",
    val totalAmount: Double = 0.0,
    val progress: Int = 0,
    val relatedChatChannelId: String = "",
    
    val cancellationReason: String? = null,
    val cancelledAt: Long? = null,
    val cancelledBy: String? = null,
    
    val requiresPasswordForCancellation: Boolean = true,
    val cancellationAttempts: Int = 0,
    val maxCancellationAttempts: Int = 3,
    val isLocked: Boolean = false,
    val lockedUntil: Long? = null,
    
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val completedAt: Long? = null,
    val isRated: Boolean = false,
    val isRecurring: Boolean = false,
    val parentId: String = "",
    val recurrenceRule: String = "NONE", // NONE, WEEKLY, MONTHLY, BIWEEKLY
    val recurrenceDays: List<Int> = emptyList(),
    val currency: String = "YER", // YER, USD, SAR
    val price: Double = 0.0,
    @Deprecated("استخدم customerName بدلاً منها", ReplaceWith("customerName"))
    val userName: String = "",
    @Deprecated("استخدم customerPhone بدلاً منها", ReplaceWith("customerPhone"))
    val userPhone: String = "",
    val userCity: String = "",
    @Deprecated("استخدم customerArea بدلاً منها", ReplaceWith("customerArea"))
    val userNeighborhood: String = "",
    val serviceName: String = "",
    @Deprecated("استخدم pinCode بدلاً منها للتشفير", ReplaceWith("pinCode"))
    val secretPin: String = "",
    val technicianId: String = "",
    val technicianName: String = "",
    val providerPhoto: String = ""
) {
    val customerId: String
        get() = clientId.ifBlank { customerPhone.ifBlank { clientPhone } }

    val effectiveCustomerName: String
        get() = customerName.ifBlank { fullName.ifBlank { clientName.ifBlank { userName.ifBlank { "العميل" } } } }

    val effectiveCustomerPhone: String
        get() = customerPhone.ifBlank { clientPhone.ifBlank { userPhone } }

    val effectiveCustomerArea: String
        get() = customerArea.ifBlank { fullAddress.ifBlank { clientAddress.ifBlank { userNeighborhood } } }

    val effectiveDate: String
        get() = date.ifBlank { dateString }

    val effectiveTime: String
        get() = time.ifBlank { timeString }

    @Suppress("DEPRECATION")
    val effectivePin: String
        get() = pinCode.ifBlank { secretPin }

    val effectivePinCode: String
        get() = effectivePin
}

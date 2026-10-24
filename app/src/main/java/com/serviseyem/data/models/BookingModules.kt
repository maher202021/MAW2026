package com.serviseyem.data.models

import androidx.annotation.Keep
import com.serviseyem.data.BookingEntity

@Keep
data class BookingCore(
    val id: String = "",
    val bookingNumber: String = "",
    val bookingCode: String = "",
    val status: String = "PENDING",
    val clientId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerArea: String = ""
) {
    val clientName: String get() = customerName
    val clientPhone: String get() = customerPhone
    val clientAddress: String get() = customerArea
}

@Keep
data class BookingService(
    val serviceType: String = "",
    val category: String = "",
    val subCategory: String = "",
    val serviceDetails: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val providerPhone: String = ""
)

@Keep
data class BookingSchedule(
    val date: String = "",
    val time: String = "",
    val scheduledAt: Long = 0L
) {
    val dateString: String get() = date
    val timeString: String get() = time
}

@Keep
data class BookingSecurity(
    val pinCode: String = "",
    val rejectionReason: String = ""
)

fun BookingEntity.toCore() = BookingCore(
    id = id,
    bookingNumber = bookingNumber.ifBlank { bookingCode },
    bookingCode = bookingCode.ifBlank { bookingNumber },
    status = status,
    clientId = clientId,
    customerName = customerName.ifBlank { clientName.ifBlank { fullName } },
    customerPhone = customerPhone.ifBlank { clientPhone.ifBlank { userPhone } },
    customerArea = customerArea.ifBlank { clientAddress.ifBlank { fullAddress } }
)

fun BookingEntity.toService() = BookingService(
    serviceType = serviceType.ifBlank { serviceName.ifBlank { category } },
    category = category.ifBlank { serviceType },
    subCategory = subCategory,
    serviceDetails = serviceDetails,
    providerId = providerId.ifBlank { technicianId },
    providerName = providerName.ifBlank { technicianName },
    providerPhone = providerPhone
)

fun BookingEntity.toSchedule() = BookingSchedule(
    date = date.ifBlank { dateString },
    time = time.ifBlank { timeString },
    scheduledAt = scheduledAt
)

fun BookingEntity.toSecurity() = BookingSecurity(
    pinCode = pinCode,
    rejectionReason = rejectionReason
)

fun createBookingFromModules(
    core: BookingCore,
    service: BookingService,
    schedule: BookingSchedule,
    security: BookingSecurity = BookingSecurity()
): BookingEntity = BookingEntity(
    id = core.id,
    bookingNumber = core.bookingNumber,
    bookingCode = core.bookingCode,
    status = core.status,
    clientId = core.clientId,
    customerName = core.customerName,
    customerPhone = core.customerPhone,
    customerArea = core.customerArea,
    serviceType = service.serviceType,
    category = service.category,
    subCategory = service.subCategory,
    serviceDetails = service.serviceDetails,
    providerId = service.providerId,
    providerName = service.providerName,
    providerPhone = service.providerPhone,
    date = schedule.date,
    time = schedule.time,
    dateString = schedule.date,
    timeString = schedule.time,
    scheduledAt = schedule.scheduledAt,
    pinCode = security.pinCode,
    rejectionReason = security.rejectionReason
)

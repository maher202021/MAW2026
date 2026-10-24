package com.serviseyem.domain.usecases

import com.serviseyem.data.BookingEntity
import com.serviseyem.data.repositories.BookingRepository
import javax.inject.Inject

/**
 * 🎯 CreateBookingUseCase
 * Handles booking creation validation, code generation, and storage.
 */
class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    private val validatePhone = ValidatePhoneUseCase()

    operator fun invoke(
        booking: BookingEntity,
        rawPasswordPin: String = "",
        onSuccess: (BookingEntity) -> Unit,
        onError: (String) -> Unit
    ) {
        val effectiveName = booking.customerName.ifBlank {
            booking.clientName.ifBlank { booking.fullName.ifBlank { booking.userName } }
        }.trim()
        if (effectiveName.isBlank()) {
            onError("يرجى إدخال اسم العميل الكامل")
            return
        }

        val effectivePhone = booking.customerPhone.ifBlank {
            booking.clientPhone.ifBlank { booking.userPhone }
        }.trim()
        if (effectivePhone.isBlank()) {
            onError("يرجى إدخال رقم هاتف للتواصل")
            return
        }
        val phoneValidation = validatePhone(effectivePhone)
        if (!phoneValidation.isValid) {
            onError(phoneValidation.errorMessage)
            return
        }

        val effectiveDate = booking.date.ifBlank { booking.dateString }.trim()
        if (effectiveDate.isBlank()) {
            onError("يرجى تحديد تاريخ الموعد")
            return
        }

        val effectiveTime = booking.time.ifBlank { booking.timeString }.trim()
        if (effectiveTime.isBlank()) {
            onError("يرجى تحديد وقت الموعد")
            return
        }

        if (
            booking.totalAmount < 0.0 ||
            booking.totalAmount.isNaN() ||
            booking.totalAmount.isInfinite() ||
            booking.advancePayment < 0.0 ||
            booking.advancePayment.isNaN() ||
            booking.advancePayment.isInfinite() ||
            (booking.totalAmount > 0.0 && booking.advancePayment > booking.totalAmount)
        ) {
            onError("قيمة المبلغ المدخل غير صالحة")
            return
        }

        val normalizedPhone = ValidatePhoneUseCase.normalizePhone(effectivePhone)
        val effectiveArea = booking.customerArea.ifBlank {
            booking.fullAddress.ifBlank { booking.clientAddress }
        }.trim()

        val normalizedBooking = booking.copy(
            customerName = effectiveName,
            clientName = booking.clientName.ifBlank { effectiveName },
            fullName = booking.fullName.ifBlank { effectiveName },
            userName = booking.userName.ifBlank { effectiveName },
            customerPhone = normalizedPhone,
            clientPhone = booking.clientPhone.ifBlank { normalizedPhone },
            userPhone = booking.userPhone.ifBlank { normalizedPhone },
            customerArea = effectiveArea,
            fullAddress = booking.fullAddress.ifBlank { effectiveArea },
            clientAddress = booking.clientAddress.ifBlank { effectiveArea },
            date = effectiveDate,
            time = effectiveTime,
            dateString = effectiveDate,
            timeString = effectiveTime
        )

        bookingRepository.createBooking(
            booking = normalizedBooking,
            rawPasswordPin = rawPasswordPin,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}

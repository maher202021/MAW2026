package com.serviseyem.ui

import com.serviseyem.data.BookingEntity
import com.serviseyem.ui.viewmodels.BookingDistributionMode
import com.serviseyem.ui.viewmodels.BookingFormFields
import com.serviseyem.utils.BookingStatus

fun MainViewModel.updateBookingFormFields(fields: BookingFormFields) = bookingViewModel.updateBookingFormFields(fields)
fun MainViewModel.updateDistributionMode(mode: BookingDistributionMode) = bookingViewModel.updateDistributionMode(mode)
fun MainViewModel.getBookingStatusColor(status: String): String = bookingViewModel.getBookingStatusColor(status)
fun MainViewModel.getBookingStatusLabel(status: String): String = bookingViewModel.getBookingStatusLabel(status)
fun MainViewModel.getBookingProgress(status: String): Float = bookingViewModel.getBookingProgress(status)

fun MainViewModel.addBooking(
    name: String,
    phone: String,
    area: String,
    serviceType: String,
    providerId: String,
    providerName: String,
    dateString: String,
    timeString: String,
    couponCode: String = "",
    pinCode: String = "",
    customBookingId: String = "",
    customPassword: String = ""
) = bookingViewModel.addBooking(
    name = name,
    phone = phone,
    area = area,
    serviceType = serviceType,
    providerId = providerId,
    providerName = providerName,
    dateString = dateString,
    timeString = timeString,
    couponCode = couponCode,
    pinCode = pinCode,
    customBookingId = customBookingId,
    customPassword = customPassword
)

fun MainViewModel.updateBookingStatus(bookingId: String, newStatus: String, rejectionReason: String = "") =
    bookingViewModel.updateBookingStatus(bookingId, newStatus, rejectionReason)

fun MainViewModel.updateBookingStatus(bookingId: String, newStatus: BookingStatus) =
    bookingViewModel.updateBookingStatus(bookingId, newStatus)

fun MainViewModel.deleteBooking(bookingId: String) =
    bookingViewModel.deleteBooking(bookingId)

fun MainViewModel.updateBooking(booking: BookingEntity) =
    bookingViewModel.updateBooking(booking)

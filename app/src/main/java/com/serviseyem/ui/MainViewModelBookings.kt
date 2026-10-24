package com.serviseyem.ui

import com.serviseyem.data.BookingEntity
import com.serviseyem.data.ProviderEntity

fun MainViewModel.deleteBookingImpl(bookingId: String) {
    bookingViewModel.deleteBookingImpl(bookingId)
}

fun MainViewModel.updateBookingImpl(booking: BookingEntity) {
    bookingViewModel.updateBookingImpl(booking)
}

fun MainViewModel.createBooking(booking: BookingEntity, onResult: (Boolean) -> Unit = {}) {
    bookingViewModel.createBooking(booking, onResult)
}

fun MainViewModel.createBookingDirectly(
    provider: ProviderEntity,
    notes: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    bookingViewModel.createBookingDirectly(provider, notes, onSuccess, onError)
}

fun MainViewModel.attemptCancelBookingImpl(
    bookingId: String,
    input: String,
    reason: String = "ملغي بطلب العميل",
    cancelledByParam: String = "USER",
    onResult: (Boolean, String) -> Unit
) {
    bookingViewModel.attemptCancelBookingImpl(bookingId, input, reason, cancelledByParam, onResult)
}

fun MainViewModel.updateBookingRelatedChatChannel(bookingId: String, channelId: String) {
    bookingViewModel.updateBookingRelatedChatChannel(bookingId, channelId)
}

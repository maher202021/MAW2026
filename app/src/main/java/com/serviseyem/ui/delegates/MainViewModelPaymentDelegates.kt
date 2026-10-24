package com.serviseyem.ui

fun MainViewModel.createPayment(
    userId: String,
    providerId: String,
    amount: Double,
    method: String,
    bookingId: String = "",
    isLinkedToBooking: Boolean = false,
    bookingServiceType: String = "",
    walletProvider: String = "",
    walletNumber: String = "",
    walletAccountName: String = "",
    transferId: String = "",
    transferPhoto: String = "",
    status: String = "PENDING"
) = adminViewModel.createPayment(
    userId = userId,
    providerId = providerId,
    amount = amount,
    method = method,
    bookingId = bookingId,
    isLinkedToBooking = isLinkedToBooking,
    bookingServiceType = bookingServiceType,
    walletProvider = walletProvider,
    walletNumber = walletNumber,
    walletAccountName = walletAccountName,
    transferId = transferId,
    transferPhoto = transferPhoto,
    status = status
)

fun MainViewModel.addNewBanner(title: String, url: String, redirect: String, type: String, size: String, duration: Int, displayTime: String = "طوال اليوم") =
    adminViewModel.addNewBanner(title, url, redirect, type, size, duration, displayTime)

fun MainViewModel.addBanner(title: String, url: String, redirect: String, type: String, size: String, duration: Int, displayTime: String = "طوال اليوم") =
    adminViewModel.addBanner(title, url, redirect, type, size, duration, displayTime)

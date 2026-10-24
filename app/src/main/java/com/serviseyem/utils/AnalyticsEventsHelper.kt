package com.serviseyem.utils

import android.content.Context
import android.os.Bundle
import com.serviseyem.MyApplication
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsEventsHelper {

    fun logEvent(context: Context?, eventName: String, params: Bundle = Bundle()) {
        try {
            if (context != null) {
                FirebaseAnalytics.getInstance(context).logEvent(eventName, params)
            } else {
                MyApplication.logFirebaseEvent(eventName, params)
            }
        } catch (e: Throwable) {
            // Safe logging fallback - do not crash if Firebase is not initialized or in test environment
        }
    }

    fun logBookingCreated(context: Context?, bookingId: String, category: String, amount: Double = 0.0) {
        val bundle = Bundle().apply {
            putString("booking_id", bookingId)
            putString("category", category)
            putDouble("amount", amount)
        }
        logEvent(context, "booking_created", bundle)
    }

    fun logBookingAccepted(context: Context?, bookingId: String) {
        val bundle = Bundle().apply {
            putString("booking_id", bookingId)
        }
        logEvent(context, "booking_accepted", bundle)
    }

    fun logBookingCancelled(context: Context?, bookingId: String, reason: String = "") {
        val bundle = Bundle().apply {
            putString("booking_id", bookingId)
            putString("reason", reason)
        }
        logEvent(context, "booking_cancelled", bundle)
    }

    fun logUrgentRequestCreated(context: Context?, requestId: String, category: String) {
        val bundle = Bundle().apply {
            putString("request_id", requestId)
            putString("category", category)
        }
        logEvent(context, "urgent_request_created", bundle)
    }

    fun logOfferSubmitted(context: Context?, requestId: String, technicianId: String, price: Double) {
        val bundle = Bundle().apply {
            putString("request_id", requestId)
            putString("technician_id", technicianId)
            putDouble("price", price)
        }
        logEvent(context, "offer_submitted", bundle)
    }

    fun logOfferAccepted(context: Context?, requestId: String, technicianId: String) {
        val bundle = Bundle().apply {
            putString("request_id", requestId)
            putString("technician_id", technicianId)
        }
        logEvent(context, "offer_accepted", bundle)
    }

    fun logPaymentInitiated(context: Context?, paymentId: String, method: String, amount: Double) {
        val bundle = Bundle().apply {
            putString("payment_id", paymentId)
            putString("payment_method", method)
            putDouble("amount", amount)
        }
        logEvent(context, "payment_initiated", bundle)
    }

    fun logSearchPerformed(context: Context?, query: String, category: String = "") {
        val bundle = Bundle().apply {
            putString("search_term", query)
            putString("search_category", category)
        }
        logEvent(context, "search_performed", bundle)
    }

    fun logChatOpened(context: Context?, channelId: String, recipientId: String = "") {
        val bundle = Bundle().apply {
            putString("channel_id", channelId)
            putString("recipient_id", recipientId)
        }
        logEvent(context, "chat_opened", bundle)
    }
}

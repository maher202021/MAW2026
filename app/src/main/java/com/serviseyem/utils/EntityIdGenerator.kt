package com.serviseyem.utils

import com.serviseyem.utils.*

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * 🔑 EntityIdGenerator - System-wide Prefix-Based Unique Identifier Strategy
 * 
 * Replaces random UUIDs with structured, timestamp-ordered, human-readable IDs:
 * Format: <PREFIX>_<TIMESTAMP_MILLIS>_<6_CHAR_HEX_RANDOM>
 * Examples:
 * - User: USR_1722345678000_A9B8C7
 * - Provider: PRV_1722345678000_D1E2F3
 * - Store: STR_1722345678000_1A2B3C
 * - Property: PROP_1722345678000_4D5E6F
 * - Job: JOB_1722345678000_7G8H9I
 * - Booking: BKG_1722345678000_9J8H7G
 * - Payment: PAY_1722345678000_6F5E4D
 * - Chat: CHT_1722345678000_3C2B1A
 * - Product: PRD_1722345678000_F3E2D1
 * - Review: REV_1722345678000_C7B8A9
 * - Offer: OFFER_1722345678000_0A1B2C
 */
object EntityIdGenerator {

    private val lastTimestamp = AtomicLong(0L)

    enum class Prefix(val tag: String) {
        USER("USR"),
        PROVIDER("PRV"),
        STORE("STR"),
        PROPERTY("PROP"),
        JOB("JOB"),
        BOOKING("BKG"),
        PAYMENT("PAY"),
        CHAT("CHT"),
        PRODUCT("PRD"),
        REVIEW("REV"),
        OFFER("OFFER"),
        CATEGORY("CAT"),
        CITY("CITY")
    }

    /**
     * Generates a unique prefixed ID with timestamp sequence guarantee
     */
    fun generate(prefix: Prefix): String = generate(prefix.tag)

    fun generate(prefixStr: String): String {
        var now = System.currentTimeMillis()
        var prev = lastTimestamp.get()
        while (now <= prev) {
            now = prev + 1
        }
        lastTimestamp.set(now)

        val cleanTag = prefixStr.trim().uppercase().ifBlank { "ID" }
        val randomSuffix = UUID.randomUUID().toString().replace("-", "").take(6).uppercase()
        return "${cleanTag}_${now}_$randomSuffix"
    }

    fun generateUserId(): String = generate(Prefix.USER)
    fun generateProviderId(): String = generate(Prefix.PROVIDER)
    fun generateStoreId(): String = generate(Prefix.STORE)
    fun generatePropertyId(): String = generate(Prefix.PROPERTY)
    fun generateJobId(): String = generate(Prefix.JOB)
    fun generateBookingId(): String = generate(Prefix.BOOKING)
    fun generatePaymentId(): String = generate(Prefix.PAYMENT)
    fun generateChatId(): String = generate(Prefix.CHAT)
    fun generateProductId(): String = generate(Prefix.PRODUCT)
    fun generateReviewId(): String = generate(Prefix.REVIEW)
    fun generateOfferId(): String = generate(Prefix.OFFER)
    fun generateCategoryId(): String = generate(Prefix.CATEGORY)
    fun generateCityId(): String = generate(Prefix.CITY)

    /**
     * Extracts entity type prefix from a given ID
     */
    fun getPrefix(id: String): String {
        return id.substringBefore("_", "")
    }

    /**
     * Validates if an ID adheres to the prefixed format
     */
    fun isValidPrefixedId(id: String): Boolean {
        if (id.isBlank() || !id.contains("_")) return false
        val parts = id.split("_")
        return parts.size >= 3 && parts[0].length in 2..6 && parts[1].toLongOrNull() != null
    }
}

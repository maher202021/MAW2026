package com.serviseyem.utils

/**
 * Delegated to SecureHasher to ensure backward compatibility across the app.
 */
object PinHasher {
    fun hashPin(pin: String): String = SecureHasher.hashPin(pin)
    fun verifyPin(pin: String, storedHash: String): Boolean = SecureHasher.verifyPin(pin, storedHash)
}

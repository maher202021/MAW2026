package com.serviseyem.utils

/**
 * Delegated to SecureHasher to ensure backward compatibility across the app.
 */
object PasswordHasher {
    fun generateSalt(): ByteArray = SecureHasher.generateSalt()
    fun isValidHash(hash: String): Boolean = SecureHasher.isValidHash(hash)
    fun hashPassword(password: String, salt: ByteArray = generateSalt()): String {
        if (SecureHasher.isValidHash(password)) return password
        return SecureHasher.hashPassword(password, salt)
    }
    fun hash(password: String): String {
        if (SecureHasher.isValidHash(password)) return password
        return SecureHasher.hashPassword(password)
    }
    fun createSaltedHash(password: String): String {
        if (SecureHasher.isValidHash(password)) return password
        return SecureHasher.hashPassword(password)
    }
    fun verifyPassword(password: String, storedHash: String): Boolean = SecureHasher.verifyPassword(password, storedHash)
}

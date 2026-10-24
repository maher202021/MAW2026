package com.serviseyem.utils

import com.serviseyem.utils.*

/**
 * Validation Layer for Yemen Services Platform.
 * Contains Validators for Entities, Yemen Phone numbers, Password Policy, and real-time inputs.
 */

data class ValidationResult(
    val isValid: Boolean,
    val message: String = "",
    val errorMessage: String? = message.ifEmpty { null }
) {
    constructor(isValid: Boolean, message: String) : this(isValid, message, message.ifEmpty { null })
}

object Validators {

    /**
     * Validates a Yemeni Phone Number.
     * Must be 9 digits starting with 77, 73, 71, 70, or 78 (or international prefix +967).
     */
    fun validateYemenPhone(phone: String?): ValidationResult {
        if (phone.isNullOrBlank()) {
            return ValidationResult(false, "يرجى إدخال رقم الهاتف")
        }
        val clean = phone.trim().replace(" ", "").replace("-", "")
        val localDigits = when {
            clean.startsWith("+967") -> clean.substring(4)
            clean.startsWith("00967") -> clean.substring(5)
            clean.startsWith("967") -> clean.substring(3)
            clean.startsWith("0") -> clean.substring(1)
            else -> clean
        }

        if (localDigits.length != 9 || !localDigits.all { it.isDigit() }) {
            return ValidationResult(false, "رقم الهاتف يجب أن يتكون من 9 أرقام (مثال: 771234567)")
        }

        val prefix = localDigits.substring(0, 2)
        val validPrefixes = listOf("77", "73", "71", "70", "78")
        if (prefix !in validPrefixes) {
            return ValidationResult(false, "رقم الهاتف يجب أن يبدأ بشركة اتصالات يمنية معتمدة (77، 73، 71، 70، 78)")
        }

        return ValidationResult(true)
    }

    /**
     * Validates password strength (minimum 8 characters, at least 1 letter and 1 number, non-weak).
     */
    fun validatePassword(password: String?): ValidationResult {
        if (password.isNullOrBlank()) {
            return ValidationResult(false, "يرجى إدخال كلمة المرور")
        }
        val (policyValid, policyError) = SecurityCryptoUtils.validatePasswordPolicy(password)
        if (!policyValid) {
            return ValidationResult(false, policyError ?: "كلمة المرور غير مطابقة للشروط")
        }
        val pass = password.trim()
        val hasLetter = pass.any { it.isLetter() }
        val hasDigit = pass.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            return ValidationResult(false, "كلمة المرور يجب أن تحتوي على أحرف وأرقام معاً")
        }
        return ValidationResult(true)
    }

    /**
     * Validates standard name field (minimum 3 characters).
     */
    fun validateName(name: String?, fieldLabel: String = "الاسم"): ValidationResult {
        if (name.isNullOrBlank()) {
            return ValidationResult(false, "يرجى إدخال $fieldLabel")
        }
        val cleanName = SecurityCryptoUtils.sanitizeInput(name)
        if (cleanName.length < 3) {
            return ValidationResult(false, "$fieldLabel يجب أن يتكون من 3 أحرف على الأقل")
        }
        return ValidationResult(true)
    }

    /**
     * Validates email format.
     */
    fun validateEmail(email: String?): ValidationResult {
        if (email.isNullOrBlank()) {
            return ValidationResult(false, "يرجى إدخال البريد الإلكتروني")
        }
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return if (emailRegex.matches(email.trim())) {
            ValidationResult(true)
        } else {
            ValidationResult(false, "تنسيق البريد الإلكتروني غير صحيح")
        }
    }
}

/**
 * Provider Entity Validator
 */
object ProviderValidator {
    fun validate(name: String?, phone: String?, serviceCategory: String?): ValidationResult {
        val nameRes = Validators.validateName(name, "اسم مزود الخدمة")
        if (!nameRes.isValid) return nameRes

        val phoneRes = Validators.validateYemenPhone(phone)
        if (!phoneRes.isValid) return phoneRes

        if (serviceCategory.isNullOrBlank()) {
            return ValidationResult(false, "يرجى اختيار القسم الرئيسي للخدمة")
        }

        return ValidationResult(true)
    }
}

/**
 * Store Entity Validator
 */
object StoreValidator {
    fun validate(storeName: String?, phone: String?, category: String?, address: String?): ValidationResult {
        val nameRes = Validators.validateName(storeName, "اسم المحل / المركز")
        if (!nameRes.isValid) return nameRes

        val phoneRes = Validators.validateYemenPhone(phone)
        if (!phoneRes.isValid) return phoneRes

        if (category.isNullOrBlank()) {
            return ValidationResult(false, "يرجى تحديد النشاط أو القسم التجارية")
        }

        if (address.isNullOrBlank() || address.trim().length < 3) {
            return ValidationResult(false, "يرجى تحديد تفاصيل العنوان أو المنطقة")
        }

        return ValidationResult(true)
    }
}

/**
 * Booking Validator
 */
object BookingValidator {
    fun validate(customerName: String?, customerPhone: String?, serviceId: String?, date: String?): ValidationResult {
        val nameRes = Validators.validateName(customerName, "اسم صاحب الطلب")
        if (!nameRes.isValid) return nameRes

        val phoneRes = Validators.validateYemenPhone(customerPhone)
        if (!phoneRes.isValid) return phoneRes

        if (serviceId.isNullOrBlank()) {
            return ValidationResult(false, "لم يتم تحديد الخدمة المطلوبة للحجز")
        }

        if (date.isNullOrBlank()) {
            return ValidationResult(false, "يرجى تحديد التاريخ والوقت المناسب للحجز")
        }

        return ValidationResult(true)
    }
}

/**
 * User Registration / Login Validator
 */
object UserValidator {
    fun validateRegistration(name: String?, phone: String?, password: String?): ValidationResult {
        val nameRes = Validators.validateName(name, "اسم المستخدم")
        if (!nameRes.isValid) return nameRes

        val phoneRes = Validators.validateYemenPhone(phone)
        if (!phoneRes.isValid) return phoneRes

        val passRes = Validators.validatePassword(password)
        if (!passRes.isValid) return passRes

        return ValidationResult(true)
    }

    fun validateLogin(phone: String?, password: String?): ValidationResult {
        val phoneRes = Validators.validateYemenPhone(phone)
        if (!phoneRes.isValid) return phoneRes

        if (password.isNullOrBlank()) {
            return ValidationResult(false, "يرجى إدخال كلمة المرور")
        }

        return ValidationResult(true)
    }
}

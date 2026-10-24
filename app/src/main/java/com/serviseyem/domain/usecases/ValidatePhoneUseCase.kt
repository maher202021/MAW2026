package com.serviseyem.domain.usecases

import androidx.annotation.Keep
import javax.inject.Inject

/**
 * 🔒 ValidatePhoneUseCase
 * Validates Yemeni phone numbers format (77, 73, 71, 70, 78) - 9 digits.
 */
class ValidatePhoneUseCase @Inject constructor() {
    operator fun invoke(phone: String): ValidationResult {
        val clean = toLatinDigits(phone).trim()
        if (clean.isEmpty()) {
            return ValidationResult(isValid = false, errorMessage = "يرجى إدخال رقم الهاتف")
        }
        // Reject strings containing letters or unexpected symbols
        if (clean.any { !it.isDigit() && it != '+' && it != '-' && it != ' ' && it != '(' && it != ')' }) {
            return ValidationResult(isValid = false, errorMessage = "يجب أن يتكون رقم الهاتف من 9 أرقام (مثال: 771234567)")
        }

        var digitsOnly = clean.filter { it in '0'..'9' }
        if (digitsOnly.startsWith("00967")) {
            digitsOnly = digitsOnly.substring(5)
        } else if (digitsOnly.startsWith("967") && digitsOnly.length >= 12) {
            digitsOnly = digitsOnly.substring(3)
        }
        while (digitsOnly.startsWith("0") && digitsOnly.length > 9) {
            digitsOnly = digitsOnly.substring(1)
        }

        if (digitsOnly.length != 9) {
            return ValidationResult(isValid = false, errorMessage = "يجب أن يتكون رقم الهاتف من 9 أرقام (مثال: 771234567)")
        }
        if (VALID_PREFIXES.none { digitsOnly.startsWith(it) }) {
            return ValidationResult(isValid = false, errorMessage = "رمز مزود الخدمة غير معروف (يجب أن يبدأ بـ 77, 73, 71, 70, 78)")
        }
        return ValidationResult(isValid = true)
    }

    companion object {
        private val VALID_PREFIXES = listOf("77", "73", "71", "70", "78")

        fun toLatinDigits(input: String): String {
            if (input.isEmpty()) return input
            val sb = StringBuilder(input.length)
            for (ch in input) {
                when (ch) {
                    in '٠'..'٩' -> sb.append((ch - '٠' + '0'.code).toChar())
                    in '۰'..'۹' -> sb.append((ch - '۰' + '0'.code).toChar())
                    '\u00A0' -> sb.append(' ')
                    in '\u200B'..'\u200F', in '\u202A'..'\u202E', in '\u2060'..'\u2069', '\uFEFF' -> {
                        // Ignore invisible BiDi / zero-width formatting characters
                    }
                    else -> sb.append(ch)
                }
            }
            return sb.toString()
        }

        fun normalizePhone(phone: String): String {
            val latin = toLatinDigits(phone).trim()
            if (latin.isBlank()) return ""
            val digits = latin.filter { it in '0'..'9' }
            if (digits.isEmpty()) return latin
            if (digits.length < 7) return digits

            var processed = digits
            if (processed.startsWith("00967")) {
                processed = processed.substring(5)
            } else if (processed.startsWith("967") && processed.length >= 12) {
                processed = processed.substring(3)
            }

            while (processed.startsWith("0") && processed.length > 9) {
                processed = processed.substring(1)
            }

            if (processed.length == 9) {
                return processed
            }

            if (processed.length > 9) {
                val last9 = processed.takeLast(9)
                if (VALID_PREFIXES.any { last9.startsWith(it) }) {
                    return last9
                }
            }

            return processed
        }
    }

    @Keep
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String = ""
    )
}

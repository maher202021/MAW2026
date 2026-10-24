package com.serviseyem.domain.usecases

import androidx.annotation.Keep
import com.serviseyem.utils.Validators
import javax.inject.Inject

/**
 * 🔒 ValidatePasswordUseCase
 * Ensures strong password/PIN requirements (minimum 8 characters with letters and numbers).
 */
class ValidatePasswordUseCase @Inject constructor() {
    operator fun invoke(password: String): ValidationResult {
        val clean = password.trim()
        if (clean.isEmpty()) {
            return ValidationResult(isValid = false, errorMessage = "يرجى إدخال كلمة المرور")
        }
        val validation = Validators.validatePassword(clean)
        return ValidationResult(
            isValid = validation.isValid,
            errorMessage = validation.errorMessage ?: ""
        )
    }

    @Keep
    data class ValidationResult(
        val isValid: Boolean = false,
        val errorMessage: String = ""
    )
}

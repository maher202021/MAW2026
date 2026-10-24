package com.serviseyem.ui.screens.register

/**
 * 🎨 Unified UiState for Registration Flow
 */
data class RegisterUiState(
    val currentStep: Int = 1,
    val totalSteps: Int = 3,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Draft / Form Data
    val fullName: String = "",
    val phone: String = "",
    val passwordPin: String = "",
    val city: String = "صنعاء",
    val category: String = "",
    val experienceYears: String = "1",
    val bio: String = "",
    val identityUrl: String = "",
    val selectedImageUris: List<String> = emptyList(),
    val uploadingImagesProgress: Float = 0f
) {
    fun reset(): RegisterUiState {
        return RegisterUiState(
            currentStep = 1,
            isLoading = false,
            isSuccess = false,
            errorMessage = null,
            successMessage = null,
            fullName = "",
            phone = "",
            passwordPin = "",
            city = "صنعاء",
            category = "",
            experienceYears = "1",
            bio = "",
            identityUrl = "",
            selectedImageUris = emptyList(),
            uploadingImagesProgress = 0f
        )
    }
}

sealed class RegistrationEvent {
    data class ShowToast(val message: String) : RegistrationEvent()
    data class NavigateToSuccess(val requestId: String) : RegistrationEvent()
    object NavigateBack : RegistrationEvent()
}

package com.serviseyem.ui.screens.register

/**
 * ⏳ حالة التحميل والعمليات لشاشات التسجيل
 */
sealed class RegistrationLoadingState {
    object Idle : RegistrationLoadingState()
    data class Loading(
        val message: String = "جاري معالجة الطلب...",
        val progress: Float? = null
    ) : RegistrationLoadingState()
    data class Success(val message: String) : RegistrationLoadingState()
    data class Error(val message: String) : RegistrationLoadingState()

    fun reset(): RegistrationLoadingState = Idle
}

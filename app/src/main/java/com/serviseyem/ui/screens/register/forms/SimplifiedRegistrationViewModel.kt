package com.serviseyem.ui.screens.register.forms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

data class SimplifiedRegistrationState(
    val imageUri: String = "",
    val entityName: String = "",
    val entityNameError: String? = null,
    val managerName: String = "",
    val managerNameError: String? = null,
    val phone: String = "",
    val phoneError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val city: String = "صنعاء",
    val specialization: String = "",
    val agreedToTerms: Boolean = false,
    val isLoading: Boolean = false,
    val successMessage: String? = null
) {
    fun isFormValidForRole(role: String): Boolean {
        val pass = password.trim()
        val hasLetter = pass.any { it.isLetter() }
        val hasDigit = pass.any { it.isDigit() }
        return entityName.isNotBlank() && phone.trim().length >= 9 && pass.length >= 7 && hasLetter && hasDigit && password == confirmPassword && agreedToTerms
    }

    val isFormValid: Boolean
        get() = isFormValidForRole("CLIENT")
}

sealed class RegistrationUiEvent {
    data class ImageChanged(val uri: String) : RegistrationUiEvent()
    data class EntityNameChanged(val name: String) : RegistrationUiEvent()
    data class ManagerNameChanged(val name: String) : RegistrationUiEvent()
    data class PhoneChanged(val phone: String) : RegistrationUiEvent()
    data class PasswordChanged(val pass: String) : RegistrationUiEvent()
    data class ConfirmPasswordChanged(val pass: String) : RegistrationUiEvent()
    data class CityChanged(val city: String) : RegistrationUiEvent()
    data class SpecializationChanged(val spec: String) : RegistrationUiEvent()
    data class AgreedToTermsChanged(val agreed: Boolean) : RegistrationUiEvent()
}

class SimplifiedRegistrationViewModel(application: Application) : AndroidViewModel(application) {
    private val draftManager = RegistrationDraftManager(application)
    private val _state = MutableStateFlow(SimplifiedRegistrationState())
    val state: StateFlow<SimplifiedRegistrationState> = _state.asStateFlow()
    private var currentRole: String = "PROVIDER"

    fun loadDraft(role: String) {
        currentRole = role
        val draft = draftManager.getDraft(role)
        _state.value = _state.value.copy(
            entityName = draft["entityName"] ?: _state.value.entityName,
            managerName = draft["managerName"] ?: _state.value.managerName,
            phone = draft["phone"] ?: _state.value.phone,
            city = draft["city"] ?: _state.value.city,
            specialization = draft["specialization"] ?: _state.value.specialization
        )
    }

    fun onEvent(event: RegistrationUiEvent) {
        when (event) {
            is RegistrationUiEvent.ImageChanged -> _state.value = _state.value.copy(imageUri = event.uri)
            is RegistrationUiEvent.EntityNameChanged -> _state.value = _state.value.copy(entityName = event.name, entityNameError = null)
            is RegistrationUiEvent.ManagerNameChanged -> _state.value = _state.value.copy(managerName = event.name, managerNameError = null)
            is RegistrationUiEvent.PhoneChanged -> _state.value = _state.value.copy(phone = event.phone, phoneError = null)
            is RegistrationUiEvent.PasswordChanged -> _state.value = _state.value.copy(password = event.pass, passwordError = null)
            is RegistrationUiEvent.ConfirmPasswordChanged -> _state.value = _state.value.copy(confirmPassword = event.pass, confirmPasswordError = null)
            is RegistrationUiEvent.CityChanged -> _state.value = _state.value.copy(city = event.city)
            is RegistrationUiEvent.SpecializationChanged -> _state.value = _state.value.copy(specialization = event.spec)
            is RegistrationUiEvent.AgreedToTermsChanged -> _state.value = _state.value.copy(agreedToTerms = event.agreed)
        }
    }

    fun submit(onSuccess: (Map<String, String>) -> Unit) {
        val s = _state.value
        val pass = s.password.trim()
        val hasLetter = pass.any { it.isLetter() }
        val hasDigit = pass.any { it.isDigit() }
        
        val passErr = when {
            pass.isBlank() -> "يرجى إدخال كلمة المرور"
            pass.length < 7 -> "يجب ألا تقل كلمة المرور عن 7 خانات"
            !hasLetter || !hasDigit -> "يجب أن تحتوي كلمة المرور على أحرف وأرقام معاً"
            else -> null
        }
        
        val cleanPhone = com.serviseyem.ui.helpers.AppPreferenceHelper.normalizePhoneNumber(s.phone)
        val yemenPhoneRegex = Regex("^(77|73|71|70|78)\\d{7}$")
        
        var hasError = false
        var newState = s.copy(
            entityNameError = if (s.entityName.isBlank()) "يرجى كتابة الاسم الرباعي" else null,
            phoneError = if (cleanPhone.length != 9 || !yemenPhoneRegex.matches(cleanPhone)) "رقم الهاتف غير صحيح (9 أرقام تبدأ بـ 77، 73، 71، 70)" else null,
            passwordError = passErr,
            confirmPasswordError = if (s.password != s.confirmPassword) "كلمة المرور غير متطابقة" else null
        )

        if (newState.entityNameError != null || newState.phoneError != null || 
            newState.passwordError != null || newState.confirmPasswordError != null) {
            hasError = true
        }

        if (!s.agreedToTerms) {
            hasError = true
        }

        if (hasError) {
            _state.value = newState
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val data = mapOf(
                "role" to currentRole,
                "fullName" to _state.value.entityName,
                "managerName" to _state.value.managerName,
                "phone" to cleanPhone,
                "city" to _state.value.city,
                "specialization" to _state.value.specialization,
                "imageUri" to _state.value.imageUri,
                "password" to _state.value.password,
                "status" to "PENDING",
                "createdAt" to Timestamp.now(),
                "type" to currentRole
            )
            
            try {
                FirebaseFirestore.getInstance().collection("join_requests").document(cleanPhone).set(data).await()
                
                draftManager.clearDraft(currentRole)
                _state.value = _state.value.copy(isLoading = false, successMessage = "تم إرسال طلب التسجيل بنجاح للأدمن")
                onSuccess(data.mapValues { it.value.toString() })
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, successMessage = null)
                e.printStackTrace()
            }
        }
    }
}

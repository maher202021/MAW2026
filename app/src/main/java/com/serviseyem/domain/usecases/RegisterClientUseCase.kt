package com.serviseyem.domain.usecases

import com.serviseyem.data.repositories.IRegistrationRepository
import com.serviseyem.domain.entities.RegistrationEntity
import javax.inject.Inject

/**
 * 🎯 RegisterClientUseCase
 */
class RegisterClientUseCase @Inject constructor(
    private val repository: IRegistrationRepository,
    private val validatePhone: ValidatePhoneUseCase = ValidatePhoneUseCase(),
    private val validatePassword: ValidatePasswordUseCase = ValidatePasswordUseCase()
) {
    suspend operator fun invoke(client: RegistrationEntity.Client): Result<String> {
        val cleanName = client.fullName.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال الاسم الكامل"))
        }
        if (cleanName.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل الاسم عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val phoneCheck = validatePhone(client.phone)
        if (!phoneCheck.isValid) {
            return Result.failure(IllegalArgumentException(phoneCheck.errorMessage))
        }

        val cleanPassword = client.rawPassword.trim()
        val passCheck = validatePassword(cleanPassword)
        if (!passCheck.isValid) {
            return Result.failure(IllegalArgumentException(passCheck.errorMessage))
        }

        val cleanCity = client.city.trim()
        if (cleanCity.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى اختيار المحافظة/المدينة"))
        }

        val normalized = client.copy(
            fullName = cleanName,
            phone = ValidatePhoneUseCase.normalizePhone(client.phone),
            city = cleanCity,
            rawPassword = cleanPassword,
            profileImageUrl = client.profileImageUrl.trim()
        )
        return repository.registerClient(normalized)
    }
}

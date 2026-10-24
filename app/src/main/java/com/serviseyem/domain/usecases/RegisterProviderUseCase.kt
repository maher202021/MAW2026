package com.serviseyem.domain.usecases

import com.serviseyem.data.repositories.IRegistrationRepository
import com.serviseyem.domain.entities.RegistrationEntity
import javax.inject.Inject

/**
 * 🎯 RegisterProviderUseCase
 */
class RegisterProviderUseCase @Inject constructor(
    private val repository: IRegistrationRepository,
    private val validatePhone: ValidatePhoneUseCase = ValidatePhoneUseCase(),
    private val validatePassword: ValidatePasswordUseCase = ValidatePasswordUseCase()
) {
    suspend operator fun invoke(provider: RegistrationEntity.Provider): Result<String> {
        val cleanName = provider.fullName.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال اسم الفني الكامل"))
        }
        if (cleanName.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل اسم الفني عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val phoneCheck = validatePhone(provider.phone)
        if (!phoneCheck.isValid) {
            return Result.failure(IllegalArgumentException(phoneCheck.errorMessage))
        }

        val cleanPassword = provider.rawPassword.trim()
        val passCheck = validatePassword(cleanPassword)
        if (!passCheck.isValid) {
            return Result.failure(IllegalArgumentException(passCheck.errorMessage))
        }

        val cleanCategory = provider.professionCategory.trim()
        if (cleanCategory.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى اختيار تخصص المهنة/الحرفة"))
        }

        val cleanCity = provider.city.trim()
        if (cleanCity.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى تحديد المدينة/المحافظة"))
        }

        if (provider.experienceYears < 0) {
            return Result.failure(IllegalArgumentException("عدد سنوات الخبرة غير صالح"))
        }

        val normalized = provider.copy(
            fullName = cleanName,
            phone = ValidatePhoneUseCase.normalizePhone(provider.phone),
            professionCategory = cleanCategory,
            city = cleanCity,
            bio = provider.bio.trim(),
            identityDocumentUrl = provider.identityDocumentUrl.trim(),
            licenseNumber = provider.licenseNumber.trim(),
            workImages = provider.workImages.map { it.trim() }.filter { it.isNotBlank() },
            rawPassword = cleanPassword
        )
        return repository.registerProvider(normalized)
    }
}

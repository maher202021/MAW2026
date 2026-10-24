package com.serviseyem.domain.usecases

import com.serviseyem.data.repositories.IRegistrationRepository
import com.serviseyem.domain.entities.RegistrationEntity
import javax.inject.Inject

/**
 * 🎯 RegisterPropertyUseCase - منطق عمل تسجيل إعلان العقار أو مكتب العقارات
 *
 * @param repository مستودع التسجيل
 * @param validatePhone التحقق من صحة رقم الهاتف
 * @param validatePassword التحقق من كلمة المرور
 */
class RegisterPropertyUseCase @Inject constructor(
    private val repository: IRegistrationRepository,
    private val validatePhone: ValidatePhoneUseCase = ValidatePhoneUseCase(),
    private val validatePassword: ValidatePasswordUseCase = ValidatePasswordUseCase()
) {
    /**
     * تنفيذ طلب تسجيل العقار
     *
     * @param property نموذج بيانات العقار
     * @return [Result] يحوي المعرف أو الاستثناء
     */
    suspend operator fun invoke(property: RegistrationEntity.Property): Result<String> {
        val cleanTitle = property.title.trim()
        if (cleanTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال عنوان الإعلان العقاري"))
        }
        if (cleanTitle.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل عنوان العقار عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val cleanOwnerName = property.ownerName.trim()
        if (cleanOwnerName.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال اسم صاحب العقار/الوكيل"))
        }
        if (cleanOwnerName.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل اسم المالك/الوكيل عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val phoneCheck = validatePhone(property.phone)
        if (!phoneCheck.isValid) {
            return Result.failure(IllegalArgumentException(phoneCheck.errorMessage))
        }

        val cleanPassword = property.rawPassword.trim()
        val passCheck = validatePassword(cleanPassword)
        if (!passCheck.isValid) {
            return Result.failure(IllegalArgumentException(passCheck.errorMessage))
        }

        val cleanCity = property.city.trim()
        if (cleanCity.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى تحديد المدينة/المحافظة"))
        }

        if (property.priceYer < 0.0 || property.priceYer.isNaN() || property.priceYer.isInfinite()) {
            return Result.failure(IllegalArgumentException("سعر العقار غير صالح"))
        }

        val normalized = property.copy(
            title = cleanTitle,
            propertyType = property.propertyType.trim(),
            category = property.category.trim(),
            ownerName = cleanOwnerName,
            phone = ValidatePhoneUseCase.normalizePhone(property.phone),
            city = cleanCity,
            areaDetails = property.areaDetails.trim(),
            description = property.description.trim(),
            imageUrls = property.imageUrls.map { it.trim() }.filter { it.isNotBlank() },
            rawPassword = cleanPassword
        )
        return repository.registerProperty(normalized)
    }
}

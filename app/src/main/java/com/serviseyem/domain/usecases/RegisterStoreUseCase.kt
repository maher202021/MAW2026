package com.serviseyem.domain.usecases

import com.serviseyem.data.repositories.IRegistrationRepository
import com.serviseyem.domain.entities.RegistrationEntity
import javax.inject.Inject

/**
 * 🎯 RegisterStoreUseCase - منطق عمل تسجيل المتجر التجاري
 *
 * @param repository مستودع التسجيل
 * @param validatePhone التحقق من صحة رقم الهاتف
 * @param validatePassword التحقق من كلمة المرور
 */
class RegisterStoreUseCase @Inject constructor(
    private val repository: IRegistrationRepository,
    private val validatePhone: ValidatePhoneUseCase = ValidatePhoneUseCase(),
    private val validatePassword: ValidatePasswordUseCase = ValidatePasswordUseCase()
) {
    /**
     * تنفيذ طلب تسجيل المتجر مع التحقق التام من البيانات المدخلة
     *
     * @param store نموذج بيانات المتجر
     * @return [Result] يحوي المعرف المولد أو رسالة الخطأ
     */
    suspend operator fun invoke(store: RegistrationEntity.Store): Result<String> {
        val cleanStoreName = store.storeName.trim()
        if (cleanStoreName.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال اسم المتجر/المحل التجاري"))
        }
        if (cleanStoreName.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل اسم المتجر عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val cleanOwnerName = store.ownerName.trim()
        if (cleanOwnerName.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال اسم صاحب المتجر"))
        }
        if (cleanOwnerName.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل اسم صاحب المتجر عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val phoneCheck = validatePhone(store.phone)
        if (!phoneCheck.isValid) {
            return Result.failure(IllegalArgumentException(phoneCheck.errorMessage))
        }

        val cleanPassword = store.rawPassword.trim()
        val passCheck = validatePassword(cleanPassword)
        if (!passCheck.isValid) {
            return Result.failure(IllegalArgumentException(passCheck.errorMessage))
        }

        val cleanCategory = store.storeCategory.trim()
        if (cleanCategory.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى اختيار تصنيف نشاط المتجر"))
        }

        val cleanCity = store.city.trim()
        if (cleanCity.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى اختيار المدينة/المحافظة"))
        }

        val normalized = store.copy(
            storeName = cleanStoreName,
            ownerName = cleanOwnerName,
            phone = ValidatePhoneUseCase.normalizePhone(store.phone),
            storeCategory = cleanCategory,
            city = cleanCity,
            addressDetails = store.addressDetails.trim(),
            commercialRegisterNumber = store.commercialRegisterNumber.trim(),
            logoUrl = store.logoUrl.trim(),
            storeImages = store.storeImages.map { it.trim() }.filter { it.isNotBlank() },
            rawPassword = cleanPassword
        )
        return repository.registerStore(normalized)
    }
}

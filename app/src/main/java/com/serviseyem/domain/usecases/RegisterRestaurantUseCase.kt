package com.serviseyem.domain.usecases

import com.serviseyem.data.repositories.IRegistrationRepository
import com.serviseyem.domain.entities.RegistrationEntity
import javax.inject.Inject

/**
 * 🎯 RegisterRestaurantUseCase - منطق عمل تسجيل المطعم/الكافيه
 *
 * @param repository مستودع التسجيل
 * @param validatePhone التحقق من صحة رقم الهاتف
 * @param validatePassword التحقق من كلمة المرور
 */
class RegisterRestaurantUseCase @Inject constructor(
    private val repository: IRegistrationRepository,
    private val validatePhone: ValidatePhoneUseCase = ValidatePhoneUseCase(),
    private val validatePassword: ValidatePasswordUseCase = ValidatePasswordUseCase()
) {
    /**
     * تنفيذ طلب تسجيل المطعم
     *
     * @param restaurant نموذج بيانات المطعم
     * @return [Result] يحوي معرف الطلب المولد أو الخطأ
     */
    suspend operator fun invoke(restaurant: RegistrationEntity.Restaurant): Result<String> {
        val cleanRestaurantName = restaurant.restaurantName.trim()
        if (cleanRestaurantName.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال اسم المطعم أو البوفيه"))
        }
        if (cleanRestaurantName.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل اسم المطعم عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val cleanOwnerName = restaurant.ownerName.trim()
        if (cleanOwnerName.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال اسم صاحب المطعم/المدير المسؤول"))
        }
        if (cleanOwnerName.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل اسم المالك عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val phoneCheck = validatePhone(restaurant.phone)
        if (!phoneCheck.isValid) {
            return Result.failure(IllegalArgumentException(phoneCheck.errorMessage))
        }

        val cleanPassword = restaurant.rawPassword.trim()
        val passCheck = validatePassword(cleanPassword)
        if (!passCheck.isValid) {
            return Result.failure(IllegalArgumentException(passCheck.errorMessage))
        }

        val cleanCuisine = restaurant.cuisineType.trim()
        if (cleanCuisine.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى اختيار نوع المأكولات والمطبخ"))
        }

        val cleanCity = restaurant.city.trim()
        if (cleanCity.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى تحديد المدينة/المحافظة"))
        }

        val normalized = restaurant.copy(
            restaurantName = cleanRestaurantName,
            ownerName = cleanOwnerName,
            phone = ValidatePhoneUseCase.normalizePhone(restaurant.phone),
            cuisineType = cleanCuisine,
            city = cleanCity,
            addressDetails = restaurant.addressDetails.trim(),
            logoUrl = restaurant.logoUrl.trim(),
            menuImageUrls = restaurant.menuImageUrls.map { it.trim() }.filter { it.isNotBlank() },
            rawPassword = cleanPassword
        )
        return repository.registerRestaurant(normalized)
    }
}

package com.serviseyem.domain.usecases

import com.serviseyem.data.repositories.IRegistrationRepository
import com.serviseyem.domain.entities.RegistrationEntity
import javax.inject.Inject

/**
 * 🎯 RegisterMedicalUseCase - منطق عمل تسجيل المركز الطبي أو الطبيب
 *
 * @param repository مستودع التسجيل
 * @param validatePhone التحقق من صحة رقم الهاتف
 * @param validatePassword التحقق من كلمة المرور
 */
class RegisterMedicalUseCase @Inject constructor(
    private val repository: IRegistrationRepository,
    private val validatePhone: ValidatePhoneUseCase = ValidatePhoneUseCase(),
    private val validatePassword: ValidatePasswordUseCase = ValidatePasswordUseCase()
) {
    /**
     * تنفيذ طلب تسجيل المركز الطبي/العيادة
     *
     * @param medical نموذج بيانات المركز الطبي
     * @return [Result] يحوي المعرف أو الاستثناء
     */
    suspend operator fun invoke(medical: RegistrationEntity.MedicalCenter): Result<String> {
        val cleanCenterName = medical.centerName.trim()
        if (cleanCenterName.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال اسم العيادة أو المركز الطبي"))
        }
        if (cleanCenterName.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل اسم المركز الطبي عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val cleanDoctorName = medical.doctorName.trim()
        if (cleanDoctorName.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال اسم الطبيب المسؤول"))
        }
        if (cleanDoctorName.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل اسم الطبيب عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val phoneCheck = validatePhone(medical.phone)
        if (!phoneCheck.isValid) {
            return Result.failure(IllegalArgumentException(phoneCheck.errorMessage))
        }

        val cleanPassword = medical.rawPassword.trim()
        val passCheck = validatePassword(cleanPassword)
        if (!passCheck.isValid) {
            return Result.failure(IllegalArgumentException(passCheck.errorMessage))
        }

        val cleanSpecialty = medical.specialtyCategory.trim()
        if (cleanSpecialty.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى تحديد التخصص الطبي الرئيسي"))
        }

        val cleanCity = medical.city.trim()
        if (cleanCity.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى اختيار المدينة/المحافظة"))
        }

        val normalized = medical.copy(
            centerName = cleanCenterName,
            specialtyCategory = cleanSpecialty,
            doctorName = cleanDoctorName,
            phone = ValidatePhoneUseCase.normalizePhone(medical.phone),
            city = cleanCity,
            addressDetails = medical.addressDetails.trim(),
            licenseNumber = medical.licenseNumber.trim(),
            logoUrl = medical.logoUrl.trim(),
            rawPassword = cleanPassword
        )
        return repository.registerMedicalCenter(normalized)
    }
}

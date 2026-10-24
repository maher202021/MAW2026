package com.serviseyem.domain.usecases

import com.serviseyem.data.repositories.IRegistrationRepository
import com.serviseyem.domain.entities.RegistrationEntity
import com.serviseyem.utils.Validators
import javax.inject.Inject

/**
 * 🎯 RegisterJobPosterUseCase - منطق عمل تسجيل معلن الوظائف أو الشركات
 *
 * @param repository مستودع التسجيل
 * @param validatePhone التحقق من صحة رقم الهاتف
 * @param validatePassword التحقق من كلمة المرور
 */
class RegisterJobPosterUseCase @Inject constructor(
    private val repository: IRegistrationRepository,
    private val validatePhone: ValidatePhoneUseCase = ValidatePhoneUseCase(),
    private val validatePassword: ValidatePasswordUseCase = ValidatePasswordUseCase()
) {
    /**
     * تنفيذ طلب تسجيل إعلان الوظيفة
     *
     * @param job نموذج بيانات الوظيفة
     * @return [Result] يحوي معرف الطلب المولد أو الاستثناء
     */
    suspend operator fun invoke(job: RegistrationEntity.Job): Result<String> {
        val cleanTitle = job.jobTitle.trim()
        if (cleanTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال المسمى الوظيفي المطلوب"))
        }
        if (cleanTitle.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل المسمى الوظيفي عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val cleanCompany = job.companyName.trim()
        if (cleanCompany.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال اسم الشركة أو معلن الوظيفة"))
        }
        if (cleanCompany.length < com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("يجب ألا يقل اسم الشركة عن ${com.serviseyem.utils.AppConstants.MIN_NAME_LENGTH} أحرف"))
        }

        val phoneCheck = validatePhone(job.contactPhone)
        if (!phoneCheck.isValid) {
            return Result.failure(IllegalArgumentException(phoneCheck.errorMessage))
        }

        val cleanPassword = job.rawPassword.trim()
        val passCheck = validatePassword(cleanPassword)
        if (!passCheck.isValid) {
            return Result.failure(IllegalArgumentException(passCheck.errorMessage))
        }

        val cleanCity = job.city.trim()
        if (cleanCity.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى تحديد المدينة/المحافظة"))
        }

        val cleanEmail = job.contactEmail.trim()
        if (cleanEmail.isNotBlank()) {
            val emailCheck = Validators.validateEmail(cleanEmail)
            if (!emailCheck.isValid) {
                return Result.failure(IllegalArgumentException(emailCheck.message))
            }
        }

        val normalized = job.copy(
            jobTitle = cleanTitle,
            companyName = cleanCompany,
            category = job.category.trim(),
            contactPhone = ValidatePhoneUseCase.normalizePhone(job.contactPhone),
            contactEmail = cleanEmail,
            city = cleanCity,
            requirements = job.requirements.trim(),
            salaryRange = job.salaryRange.trim(),
            rawPassword = cleanPassword
        )
        return repository.registerJob(normalized)
    }
}

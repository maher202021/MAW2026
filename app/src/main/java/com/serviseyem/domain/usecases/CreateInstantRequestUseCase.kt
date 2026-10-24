package com.serviseyem.domain.usecases

import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.data.repositories.InstantRequestRepository
import com.serviseyem.utils.AppError
import com.serviseyem.utils.AppResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume

class CreateInstantRequestUseCase @Inject constructor(
    private val repository: InstantRequestRepository
) {
    suspend operator fun invoke(request: InstantRequestEntity): AppResult<InstantRequestEntity> {
        val serviceLabel = request.serviceTitle.ifBlank {
            request.categoryName.ifBlank { request.categoryId }
        }.trim()
        if (serviceLabel.isBlank() && request.description.isBlank()) {
            return AppResult.Error(AppError.ValidationError("serviceType", "يرجى تحديد نوع الخدمة أو وصف الطلب"))
        }
        if (request.acceptedPrice < 0.0 || request.acceptedPrice.isNaN() || request.acceptedPrice.isInfinite()) {
            return AppResult.Error(AppError.ValidationError("price", "الميزانية أو السعر المحدد غير صالح"))
        }
        val normalizedPhone = ValidatePhoneUseCase.normalizePhone(request.userPhone)
        val normalizedRequest = request.copy(
            userId = request.userId.trim(),
            userName = request.userName.trim(),
            userPhone = normalizedPhone,
            userCity = request.userCity.trim(),
            userNeighborhood = request.userNeighborhood.trim(),
            serviceTitle = request.serviceTitle.trim().ifBlank { serviceLabel },
            categoryName = request.categoryName.trim().ifBlank { serviceLabel },
            description = request.description.trim(),
            images = request.images.map { it.trim() }.filter { it.isNotBlank() }
        )

        return withTimeoutOrNull(30_000L) {
            suspendCancellableCoroutine { cont ->
                try {
                    repository.createInstantRequest(
                        request = normalizedRequest,
                        onSuccess = {
                            if (cont.isActive) cont.resume(AppResult.Success(it))
                        },
                        onError = {
                            if (cont.isActive) cont.resume(AppResult.Error(AppError.UnknownError(it)))
                        }
                    )
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    if (cont.isActive) cont.resume(AppResult.Error(AppError.UnknownError(e.localizedMessage ?: "فشل إنشاء الطلب العاجل", e)))
                }
            }
        } ?: AppResult.Error(AppError.UnknownError("انتهت مهلة الاتصال أثناء إرسال الطلب العاجل"))
    }
}

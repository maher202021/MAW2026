package com.serviseyem.domain.usecases

import com.serviseyem.data.repositories.InstantRequestRepository
import com.serviseyem.utils.AppError
import com.serviseyem.utils.AppResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume

class AcceptOfferUseCase @Inject constructor(
    private val repository: InstantRequestRepository
) {
    suspend operator fun invoke(
        requestId: String,
        offerId: String,
        providerId: String,
        providerName: String,
        providerPhone: String,
        acceptedPrice: Double
    ): AppResult<Unit> {
        val cleanRequestId = requestId.trim()
        val cleanOfferId = offerId.trim()
        val cleanProviderId = providerId.trim()
        val cleanProviderName = providerName.trim().ifBlank { "مزود خدمة" }
        if (cleanRequestId.isBlank() || cleanOfferId.isBlank() || cleanProviderId.isBlank()) {
            return AppResult.Error(AppError.ValidationError("offer", "بيانات العرض أو الطلب غير مكتملة"))
        }
        if (acceptedPrice < 0.0 || acceptedPrice.isNaN() || acceptedPrice.isInfinite()) {
            return AppResult.Error(AppError.ValidationError("price", "سعر العرض غير صالح"))
        }
        val cleanPhone = ValidatePhoneUseCase.normalizePhone(providerPhone)
        return withTimeoutOrNull(30_000L) {
            suspendCancellableCoroutine { cont ->
                try {
                    repository.acceptOffer(
                        requestId = cleanRequestId,
                        offerId = cleanOfferId,
                        providerId = cleanProviderId,
                        providerName = cleanProviderName,
                        providerPhone = cleanPhone,
                        acceptedPrice = acceptedPrice,
                        onSuccess = {
                            if (cont.isActive) cont.resume(AppResult.Success(Unit))
                        },
                        onError = {
                            if (cont.isActive) cont.resume(AppResult.Error(AppError.UnknownError(it)))
                        }
                    )
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    if (cont.isActive) cont.resume(AppResult.Error(AppError.UnknownError(e.localizedMessage ?: "فشل قبول العرض", e)))
                }
            }
        } ?: AppResult.Error(AppError.UnknownError("انتهت مهلة الاتصال أثناء قبول العرض، يرجى المحاولة مرة أخرى"))
    }
}

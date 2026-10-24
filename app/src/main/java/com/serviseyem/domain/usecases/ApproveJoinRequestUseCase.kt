package com.serviseyem.domain.usecases

import com.serviseyem.data.PendingProviderEntity
import com.serviseyem.data.repositories.IStatusRepository
import javax.inject.Inject

class ApproveJoinRequestUseCase @Inject constructor(
    private val statusRepository: IStatusRepository
) {

    suspend operator fun invoke(request: PendingProviderEntity, actorRole: String = "ADMIN"): Result<Unit> {
        if (actorRole !in listOf("OWNER", "ADMIN", "SUPERVISOR")) {
            return Result.failure(SecurityException("ليس لديك صلاحية للموافقة على طلبات الانضمام"))
        }
        if (request.id.isBlank()) {
            return Result.failure(IllegalArgumentException("معرف طلب الانضمام غير صالح"))
        }
        val normalized = request.copy(
            id = request.id.trim(),
            name = request.name.trim(),
            phone = ValidatePhoneUseCase.normalizePhone(request.phone)
        )
        return statusRepository.approveJoinRequest(normalized)
    }
}


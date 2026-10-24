package com.serviseyem.domain.usecases

import com.serviseyem.data.repositories.IRegistrationRepository
import com.serviseyem.domain.entities.JoinStatusEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 🎯 GetJoinStatusUseCase
 */
class GetJoinStatusUseCase @Inject constructor(
    private val repository: IRegistrationRepository
) {
    operator fun invoke(phone: String): Flow<JoinStatusEntity?> {
        val normalized = ValidatePhoneUseCase.normalizePhone(phone).trim()
        if (normalized.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(null)
        }
        return repository.getJoinStatusFlow(normalized)
    }
}

package com.serviseyem.domain.usecases

import com.serviseyem.data.repositories.IChatRepository
import com.serviseyem.data.models.ChannelType
import com.serviseyem.data.models.ChatChannel
import com.serviseyem.utils.AppError
import com.serviseyem.utils.AppResult
import javax.inject.Inject

class OpenSupportChatUseCase @Inject constructor(
    private val chatRepository: IChatRepository
) {
    suspend operator fun invoke(
        userId: String,
        userName: String,
        userPhone: String,
        supportAgentId: String = "support_official"
    ): AppResult<ChatChannel> {
        val cleanPhone = ValidatePhoneUseCase.normalizePhone(userPhone)
        val effectiveUserId = userId.trim().ifBlank { cleanPhone }
        if (effectiveUserId.isBlank()) {
            return AppResult.Error(AppError.ValidationError("userId", "بيانات المستخدم غير مكتملة لبدء محادثة الدعم"))
        }
        val effectiveUserName = userName.trim().ifBlank { cleanPhone.ifBlank { "مستخدم" } }
        val effectiveSupportId = supportAgentId.trim().ifBlank { "support_official" }
        return chatRepository.getOrCreateChannel(
            currentUserId = effectiveUserId,
            currentUserName = effectiveUserName,
            currentUserPhoto = "",
            otherUserId = effectiveSupportId,
            otherUserName = "الدعم الفني للإدارة",
            otherUserPhoto = "",
            type = ChannelType.SUPPORT,
            relatedEntityId = cleanPhone.ifBlank { null },
            relatedEntityType = if (cleanPhone.isNotBlank()) "SUPPORT_USER_PHONE" else null
        )
    }
}

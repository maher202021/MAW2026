package com.serviseyem.domain.usecases

import com.serviseyem.data.models.ChatAttachment
import com.serviseyem.data.models.ChatChannel
import com.serviseyem.data.models.ChatMessage
import com.serviseyem.data.models.MediaType
import com.serviseyem.data.repositories.IChatRepository
import com.serviseyem.utils.AppError
import com.serviseyem.utils.AppResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChannelsUseCase @Inject constructor(private val repository: IChatRepository) {
    operator fun invoke(userId: String): Flow<List<ChatChannel>> {
        val cleanUserId = userId.trim()
        if (cleanUserId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        return repository.getUserChannels(cleanUserId)
    }
}

class GetMessagesUseCase @Inject constructor(private val repository: IChatRepository) {
    operator fun invoke(channelId: String, currentUserId: String, limit: Int = 50): Flow<List<ChatMessage>> {
        val cleanChannelId = channelId.trim()
        val cleanUserId = currentUserId.trim()
        if (cleanChannelId.isBlank() || cleanUserId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        return repository.getChannelMessages(cleanChannelId, cleanUserId, limit.coerceAtLeast(1))
    }
}

class SendMessageUseCase @Inject constructor(private val repository: IChatRepository) {
    suspend operator fun invoke(
        channelId: String,
        senderId: String,
        senderName: String,
        messageText: String,
        mediaType: MediaType = MediaType.TEXT,
        mediaUrl: String = "",
        replyToId: String? = null,
        replyToText: String? = null,
        attachment: ChatAttachment? = null
    ): AppResult<ChatMessage> {
        if (mediaType == MediaType.TEXT && messageText.isBlank() && mediaUrl.isBlank() && attachment == null) {
            return AppResult.Error(AppError.ValidationError("messageText", "لا يمكن إرسال رسالة نصية فارغة"))
        }
        return repository.sendMessage(
            channelId = channelId.trim(),
            senderId = senderId.trim(),
            senderName = senderName.trim(),
            messageText = messageText.trim(),
            mediaType = mediaType,
            mediaUrl = mediaUrl.trim(),
            replyToId = replyToId?.trim(),
            replyToText = replyToText?.trim(),
            attachment = attachment
        )
    }
}

class DeleteMessageUseCase @Inject constructor(private val repository: IChatRepository) {
    suspend operator fun invoke(
        channelId: String,
        messageId: String,
        forEveryone: Boolean,
        currentUserId: String
    ): AppResult<Unit> {
        return repository.deleteMessage(channelId.trim(), messageId.trim(), forEveryone, currentUserId.trim())
    }
}

class MarkAsReadUseCase @Inject constructor(private val repository: IChatRepository) {
    suspend operator fun invoke(channelId: String, currentUserId: String): AppResult<Unit> {
        return repository.markChannelAsRead(channelId.trim(), currentUserId.trim())
    }
}

package com.serviseyem.ui.screens.chat.managers

import com.serviseyem.data.models.ChatMessage
import com.serviseyem.data.repositories.ChatRepository
import com.serviseyem.utils.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatEditManager(
    private val repository: ChatRepository,
    private val scope: CoroutineScope
) {
    private val _replyingToMessage = MutableStateFlow<ChatMessage?>(null)
    val replyingToMessage: StateFlow<ChatMessage?> = _replyingToMessage.asStateFlow()

    fun setReplyingTo(message: ChatMessage?) {
        _replyingToMessage.value = message
    }

    fun editMessage(
        channelId: String,
        messageId: String,
        newText: String,
        currentUserId: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        scope.launch {
            try {
                val res = repository.editMessage(channelId, messageId, newText, currentUserId)
                when (res) {
                    is AppResult.Success -> onSuccess()
                    is AppResult.Error -> onError(res.error.messageArabic)
                }
            } catch (e: Exception) {
                onError(e.message ?: "فشل تعديل الرسالة")
            }
        }
    }

    fun deleteMessage(
        channelId: String,
        messageId: String,
        forEveryone: Boolean = true,
        currentUserId: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        scope.launch {
            try {
                val res = repository.deleteMessage(channelId, messageId, forEveryone, currentUserId)
                when (res) {
                    is AppResult.Success -> onSuccess()
                    is AppResult.Error -> onError(res.error.messageArabic)
                }
            } catch (e: Exception) {
                onError(e.message ?: "فشل حذف الرسالة")
            }
        }
    }

    fun clear() {
        _replyingToMessage.value = null
    }
}

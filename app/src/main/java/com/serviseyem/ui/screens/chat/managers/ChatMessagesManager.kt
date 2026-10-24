package com.serviseyem.ui.screens.chat.managers

import com.serviseyem.data.models.ChatAttachment
import com.serviseyem.data.models.ChatMessage
import com.serviseyem.data.models.MediaType
import com.serviseyem.data.models.MessageStatus
import com.serviseyem.data.repositories.IChatRepository
import com.serviseyem.utils.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.serviseyem.utils.EntityIdGenerator

class ChatMessagesManager(
    private val repository: IChatRepository,
    private val scope: CoroutineScope
) {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun updateMessagesList(list: List<ChatMessage>) {
        _messages.value = list
    }

    fun setSending(sending: Boolean) {
        _isSending.value = sending
    }

    fun sendMessage(
        channelId: String,
        senderId: String,
        senderName: String,
        text: String,
        mediaType: MediaType = MediaType.TEXT,
        mediaUrl: String = "",
        replyTo: ChatMessage? = null,
        attachment: ChatAttachment? = null,
        audioWaveform: List<Int> = emptyList(),
        onSuccess: (ChatMessage) -> Unit,
        onError: (String) -> Unit
    ) {
        val messageId = EntityIdGenerator.generate("msg")
        val localMessage = ChatMessage(
            id = messageId,
            channelId = channelId,
            senderId = senderId,
            senderName = senderName,
            message = text,
            mediaType = mediaType,
            mediaUrl = mediaUrl,
            replyToId = replyTo?.id,
            replyToText = replyTo?.message,
            attachment = attachment,
            audioWaveform = audioWaveform,
            status = MessageStatus.SENDING,
            timestamp = System.currentTimeMillis()
        )

        // 1. عرض فوري محلي في الواجهة (Instant Optimistic UI < 50ms)
        // [تعديل] إضافة الرسالة فوراً بحالة SENDING قبل المزامنة مع السيرفر
        _messages.value = _messages.value + localMessage
        _isSending.value = true

        // 2. كتابة متفائلة واحدة - التخزين المحلي لـ Firestore وقائمة الانتظار تتولى المزامنة التلقائية دون تأخير
        scope.launch {
            try {
                val result = repository.sendMessage(
                    channelId = channelId,
                    senderId = senderId,
                    senderName = senderName,
                    messageText = text,
                    mediaType = mediaType,
                    mediaUrl = mediaUrl,
                    replyToId = replyTo?.id,
                    replyToText = replyTo?.message,
                    attachment = attachment,
                    audioWaveform = audioWaveform
                )

                _isSending.value = false
                when (result) {
                    is AppResult.Success -> {
                        // تحديث الرسالة بالنسخة المؤكدة من السيرفر/الكاش
                        _messages.value = _messages.value.map {
                            if (it.id == localMessage.id) result.data else it
                        }
                        onSuccess(result.data)
                    }
                    is AppResult.Error -> {
                        _messages.value = _messages.value.map {
                            if (it.id == localMessage.id) it.copy(status = MessageStatus.FAILED) else it
                        }
                        onError(result.error.messageArabic)
                    }
                }
            } catch (e: Exception) {
                _isSending.value = false
                _messages.value = _messages.value.map {
                    if (it.id == localMessage.id) it.copy(status = MessageStatus.FAILED) else it
                }
                onError(e.message ?: "حدث خطأ أثناء الإرسال")
            }
        }
    }

    fun resendMessage(
        channelId: String,
        currentUserId: String,
        targetMsg: ChatMessage,
        onSuccess: (ChatMessage) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            // إعادة المحاولة على نفس الرسالة بنفس الترتيب الزمني والمعرف
            _messages.value = _messages.value.map {
                if (it.id == targetMsg.id) it.copy(status = MessageStatus.SENDING) else it
            }
            try {
                val result = repository.sendMessage(
                    channelId = channelId,
                    senderId = targetMsg.senderId,
                    senderName = targetMsg.senderName,
                    messageText = targetMsg.message,
                    mediaType = targetMsg.mediaType,
                    mediaUrl = targetMsg.mediaUrl,
                    replyToId = targetMsg.replyToId,
                    replyToText = targetMsg.replyToText,
                    attachment = targetMsg.attachment,
                    audioWaveform = targetMsg.audioWaveform
                )
                when (result) {
                    is AppResult.Success -> {
                        _messages.value = _messages.value.map {
                            if (it.id == targetMsg.id) result.data else it
                        }
                        onSuccess(result.data)
                    }
                    is AppResult.Error -> {
                        _messages.value = _messages.value.map {
                            if (it.id == targetMsg.id) it.copy(status = MessageStatus.FAILED) else it
                        }
                        onError(result.error.messageArabic)
                    }
                }
            } catch (e: Exception) {
                _messages.value = _messages.value.map {
                    if (it.id == targetMsg.id) it.copy(status = MessageStatus.FAILED) else it
                }
                onError(e.message ?: "فشلت إعادة الإرسال")
            }
        }
    }

    fun updateMessageStatus(messageId: String, status: MessageStatus) {
        _messages.value = _messages.value.map {
            if (it.id == messageId) it.copy(status = status) else it
        }
    }
}

package com.serviseyem.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.models.*
import com.serviseyem.data.repositories.ChatRepository
import com.serviseyem.ui.screens.chat.managers.ChatEditManager
import com.serviseyem.ui.screens.chat.managers.ChatMessagesManager
import com.serviseyem.ui.screens.chat.managers.ChatPresenceManager
import com.serviseyem.ui.screens.chat.managers.ChatTypingManager
import com.serviseyem.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChatError(open val messageArabic: String) {
    data class NetworkError(override val messageArabic: String = "خطأ في الاتصال بالشبكة") : ChatError(messageArabic)
    data class PermissionDenied(override val messageArabic: String = "تم رفض الإذن المطلوب") : ChatError(messageArabic)
    data class UnknownError(val details: String) : ChatError(details)
}

sealed class ChatEvent {
    data class ShowError(val message: String) : ChatEvent()
    data class MessageSent(val messageId: String) : ChatEvent()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    // --- Specialized Sub-Managers ---
    val messagesManager = ChatMessagesManager(repository, viewModelScope)
    val typingManager = ChatTypingManager(repository, viewModelScope)
    val presenceManager = ChatPresenceManager(repository, viewModelScope)
    val editManager = ChatEditManager(repository, viewModelScope)

    // --- Events & Global States ---
    private val _eventFlow = MutableSharedFlow<ChatEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _currentChannel = MutableStateFlow<ChatChannel?>(null)
    val currentChannel: StateFlow<ChatChannel?> = _currentChannel.asStateFlow()

    val messages: StateFlow<List<ChatMessage>> = messagesManager.messages
    val isSending: StateFlow<Boolean> = messagesManager.isSending

    val otherUserPresence: StateFlow<UserPresence?> = presenceManager.otherUserPresence
    val isTypingOther: StateFlow<Boolean> = typingManager.isTypingOther
    val replyingToMessage: StateFlow<ChatMessage?> = editManager.replyingToMessage

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var messagesJob: Job? = null
    private var markAsReadJob: Job? = null
    private var lastMarkAsReadTime: Long = 0L
    private var lastMarkAsReadChannelId: String = ""
    private var activeUserId: String = ""
    private var currentLimit = 25

    fun openChannel(channel: ChatChannel, currentUserId: String) {
        resetState()
        _currentChannel.value = channel
        activeUserId = currentUserId
        currentLimit = 25

        val otherUserId = channel.participants.firstOrNull { it != currentUserId } ?: ""
        listenToMessages(channel.id, currentUserId)
        presenceManager.listenToPresence(otherUserId)
        typingManager.listenToTyping(channel.id, otherUserId)
        markAsRead(channel.id, currentUserId)
    }

    fun openChannelById(
        channelId: String,
        currentUserId: String,
        currentUserName: String = "المستخدم",
        fallbackTargetUserId: String? = null,
        fallbackUserName: String? = null,
        relatedEntityId: String? = null,
        relatedEntityType: String? = null
    ) {
        activeUserId = currentUserId
        if (_currentChannel.value?.id != channelId) {
            val provisional = ChatChannel(
                id = channelId,
                participants = listOf(currentUserId, fallbackTargetUserId ?: channelId).filter { it.isNotBlank() },
                title = fallbackUserName ?: "المحادثة",
                type = if (relatedEntityType == "PASSWORD_RECOVERY" || relatedEntityType == "SUPPORT") ChannelType.SUPPORT else ChannelType.PRIVATE,
                relatedEntityId = relatedEntityId,
                relatedEntityType = relatedEntityType
            )
            _currentChannel.value = provisional
            listenToMessages(channelId, currentUserId)
        }
        viewModelScope.launch {
            val channelResult = repository.getChannelById(channelId)
            val channel = channelResult.getOrNull()
            if (channel != null) {
                openChannel(channel, currentUserId)
            } else {
                // [تعديل] التأكد من صحة توجيه المحادثة وحفظ relatedEntityId و relatedEntityType بدقة
                val isPasswordRecovery = relatedEntityType == "PASSWORD_RECOVERY" || relatedEntityType == "SUPPORT"
                val channelType = if (isPasswordRecovery) ChannelType.SUPPORT else ChannelType.PRIVATE
                val targetId = if (isPasswordRecovery) ChatRepository.SUPPORT_ADMIN_ID else (fallbackTargetUserId ?: channelId)
                val targetName = if (isPasswordRecovery) ChatRepository.SUPPORT_ADMIN_NAME else (fallbackUserName ?: "مقدم الخدمة")
                val res = repository.getOrCreateChannel(
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    currentUserPhoto = "",
                    otherUserId = targetId,
                    otherUserName = targetName,
                    otherUserPhoto = "",
                    type = channelType,
                    relatedEntityId = relatedEntityId,
                    relatedEntityType = relatedEntityType
                )
                when (res) {
                    is AppResult.Success -> openChannel(res.data, currentUserId)
                    is AppResult.Error -> _eventFlow.emit(ChatEvent.ShowError(res.error.messageArabic))
                }
            }
        }
    }

    fun startDirectChat(
        currentUserId: String,
        currentUserName: String,
        currentUserPhoto: String = "",
        otherUserId: String,
        otherUserName: String,
        otherUserPhoto: String = "",
        relatedEntityId: String? = null,
        relatedEntityType: String? = null
    ) {
        activeUserId = currentUserId
        val isPasswordRecovery = relatedEntityType == "PASSWORD_RECOVERY" || relatedEntityType == "SUPPORT"
        val channelType = if (isPasswordRecovery) ChannelType.SUPPORT else ChannelType.PRIVATE
        val finalOtherId = if (isPasswordRecovery) ChatRepository.SUPPORT_ADMIN_ID else otherUserId.trim()
        val finalOtherName = if (isPasswordRecovery) ChatRepository.SUPPORT_ADMIN_NAME else otherUserName.trim().ifBlank { "مقدم الخدمة" }
        val provId = "direct_${minOf(currentUserId, finalOtherId)}_${maxOf(currentUserId, finalOtherId)}"

        if (_currentChannel.value?.id != provId) {
            val provisional = ChatChannel(
                id = provId,
                participants = listOf(currentUserId, finalOtherId).filter { it.isNotBlank() },
                title = finalOtherName,
                type = channelType,
                relatedEntityId = relatedEntityId,
                relatedEntityType = relatedEntityType
            )
            _currentChannel.value = provisional
            listenToMessages(provId, currentUserId)
        }

        viewModelScope.launch {
            val channelResult = repository.getOrCreateChannel(
                currentUserId = currentUserId,
                currentUserName = currentUserName,
                currentUserPhoto = currentUserPhoto,
                otherUserId = finalOtherId,
                otherUserName = finalOtherName,
                otherUserPhoto = otherUserPhoto,
                type = channelType,
                relatedEntityId = relatedEntityId,
                relatedEntityType = relatedEntityType
            )
            when (channelResult) {
                is AppResult.Success -> openChannel(channelResult.data, currentUserId)
                is AppResult.Error -> _eventFlow.emit(ChatEvent.ShowError(channelResult.error.messageArabic))
            }
        }
    }

    private fun listenToMessages(channelId: String, currentUserId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            // [تعديل] تفعيل الاستماع الفوري للرسائل عند فتح المحادثة لمزامنة فورية ودقيقة
            repository.getChannelMessages(channelId, currentUserId, limit = currentLimit).collect { msgs ->
                // FIXED: Limit messages stored in memory to maximum 150 items
                val cappedMsgs = if (msgs.size > 150) msgs.takeLast(150) else msgs
                messagesManager.updateMessagesList(cappedMsgs)
                markAsRead(channelId, currentUserId)
            }
        }
    }

    fun loadMoreMessages() {
        val channel = _currentChannel.value ?: return
        if (currentLimit >= 150) return
        currentLimit = (currentLimit + 25).coerceAtMost(150)
        listenToMessages(channel.id, activeUserId)
    }

    fun sendMessage(
        senderId: String = activeUserId,
        senderName: String = "أنا",
        text: String,
        mediaType: MediaType = MediaType.TEXT,
        mediaUrl: String = "",
        attachment: ChatAttachment? = null,
        audioWaveform: List<Int> = emptyList()
    ) {
        var channel = _currentChannel.value
        if (channel == null) {
            val fallbackChId = "direct_${activeUserId.ifBlank { senderId }}"
            channel = ChatChannel(
                id = fallbackChId,
                participants = listOf(senderId.ifBlank { activeUserId }),
                title = "محادثة"
            )
            _currentChannel.value = channel
        }
        if (text.isBlank() && mediaUrl.isBlank() && attachment == null) return

        val replyTo = editManager.replyingToMessage.value
        editManager.setReplyingTo(null)

        messagesManager.sendMessage(
            channelId = channel.id,
            senderId = senderId.ifBlank { activeUserId },
            senderName = senderName,
            text = text.trim(),
            mediaType = mediaType,
            mediaUrl = mediaUrl,
            replyTo = replyTo,
            attachment = attachment,
            audioWaveform = audioWaveform,
            onSuccess = { sentMsg ->
                viewModelScope.launch {
                    _eventFlow.emit(ChatEvent.MessageSent(sentMsg.id))
                }
            },
            onError = { errMsg ->
                viewModelScope.launch {
                    _eventFlow.emit(ChatEvent.ShowError(errMsg))
                }
            }
        )
    }

    fun toggleReaction(messageId: String, emoji: String) {
        val channel = _currentChannel.value ?: return
        viewModelScope.launch {
            try {
                repository.toggleReaction(channel.id, messageId, activeUserId, emoji)
            } catch (e: Exception) {
                _eventFlow.emit(ChatEvent.ShowError("تعذر تحديث التفاعل"))
            }
        }
    }

    fun togglePinMessage(messageId: String, isPinned: Boolean) {
        val channel = _currentChannel.value ?: return
        viewModelScope.launch {
            try {
                repository.togglePinMessage(channel.id, messageId, isPinned)
            } catch (e: Exception) {
                _eventFlow.emit(ChatEvent.ShowError("تعذر تحديث حالة التثبيت"))
            }
        }
    }

    fun updateMessageStatus(messageId: String, status: MessageStatus) {
        messagesManager.updateMessageStatus(messageId, status)
    }

    fun resendMessage(messageId: String) {
        val channel = _currentChannel.value ?: return
        val targetMsg = messages.value.find { it.id == messageId } ?: return

        messagesManager.resendMessage(
            channelId = channel.id,
            currentUserId = activeUserId,
            targetMsg = targetMsg,
            onSuccess = { sentMsg ->
                viewModelScope.launch {
                    _eventFlow.emit(ChatEvent.MessageSent(sentMsg.id))
                }
            },
            onError = { errMsg ->
                viewModelScope.launch {
                    _eventFlow.emit(ChatEvent.ShowError(errMsg))
                }
            }
        )
    }

    fun resendMessage(messageId: String, senderId: String, senderName: String) {
        resendMessage(messageId)
    }

    fun setReplyingTo(message: ChatMessage?) {
        editManager.setReplyingTo(message)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onUserTyping(senderId: String, text: String) {
        val channel = _currentChannel.value ?: return
        typingManager.onUserTyping(channel.id, senderId, text)
    }

    fun markAsRead(channelId: String, currentUserId: String) {
        val now = System.currentTimeMillis()
        if (channelId == lastMarkAsReadChannelId && (now - lastMarkAsReadTime) < 5000L) {
            return
        }
        lastMarkAsReadChannelId = channelId
        lastMarkAsReadTime = now
        markAsReadJob?.cancel()
        markAsReadJob = viewModelScope.launch {
            try {
                repository.markChannelAsRead(channelId, currentUserId)
            } catch (_: Exception) {}
        }
    }

    fun editMessage(channelId: String, messageId: String, newText: String) {
        editManager.editMessage(channelId, messageId, newText, currentUserId = activeUserId,
            onError = { viewModelScope.launch { _eventFlow.emit(ChatEvent.ShowError(it)) } }
        )
    }

    fun editMessage(messageId: String, newText: String) {
        val channel = _currentChannel.value ?: return
        editMessage(channel.id, messageId, newText)
    }

    fun deleteMessage(channelId: String, messageId: String) {
        editManager.deleteMessage(channelId, messageId, forEveryone = true, currentUserId = activeUserId,
            onError = { viewModelScope.launch { _eventFlow.emit(ChatEvent.ShowError(it)) } }
        )
    }

    fun deleteMessage(messageId: String, forEveryone: Boolean, currentUserId: String) {
        val channel = _currentChannel.value ?: return
        editManager.deleteMessage(channel.id, messageId, forEveryone, currentUserId,
            onError = { viewModelScope.launch { _eventFlow.emit(ChatEvent.ShowError(it)) } }
        )
    }

    private fun sanitizeErrorMessage(raw: String?, defaultMessage: String): String {
        if (raw.isNullOrBlank()) return defaultMessage
        val lower = raw.lowercase()
        return when {
            lower.contains("permission_denied") || lower.contains("permission-denied") -> "ليس لديك صلاحية لتنفيذ هذا الإجراء"
            lower.contains("unavailable") || lower.contains("network") || lower.contains("timeout") -> "تعذر الاتصال بالخادم، يرجى التحقق من الإنترنت"
            lower.contains("failed_precondition") || lower.contains("not-found") -> "لا يمكن تنفيذ هذه العملية حالياً"
            lower.contains("unauthenticated") -> "انتهت الجلسة، يرجى تسجيل الدخول مجدداً"
            else -> defaultMessage
        }
    }

    fun toggleBlock(otherUserId: String, block: Boolean) {
        val channel = _currentChannel.value ?: return
        viewModelScope.launch {
            try {
                val res = repository.toggleBlockUser(channel.id, otherUserId, block)
                if (res is AppResult.Error) {
                    _eventFlow.emit(ChatEvent.ShowError(res.error.messageArabic))
                }
            } catch (e: Exception) {
                _eventFlow.emit(ChatEvent.ShowError(sanitizeErrorMessage(e.message, "فشل تغيير حالة الحظر، يرجى المحاولة لاحقاً")))
            }
        }
    }

    fun deleteCurrentChannel(onDeleted: () -> Unit) {
        val channel = _currentChannel.value ?: return
        viewModelScope.launch {
            try {
                repository.deleteChannel(channel.id)
                onDeleted()
            } catch (e: Exception) {
                _eventFlow.emit(ChatEvent.ShowError(sanitizeErrorMessage(e.message, "فشل حذف المحادثة، يرجى المحاولة لاحقاً")))
            }
        }
    }

    fun resetState() {
        messagesJob?.cancel()
        markAsReadJob?.cancel()
        presenceManager.clear()
        typingManager.clear()
        editManager.clear()
        _currentChannel.value = null
        messagesManager.updateMessagesList(emptyList())
        _searchQuery.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        resetState()
    }
}

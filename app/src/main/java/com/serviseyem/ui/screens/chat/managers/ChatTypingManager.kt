package com.serviseyem.ui.screens.chat.managers

import com.serviseyem.data.repositories.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatTypingManager(
    private val repository: ChatRepository,
    private val scope: CoroutineScope
) {
    private val _isTypingOther = MutableStateFlow(false)
    val isTypingOther: StateFlow<Boolean> = _isTypingOther.asStateFlow()

    private var typingJob: Job? = null
    private var typingListenerJob: Job? = null

    fun listenToTyping(channelId: String, otherUserId: String) {
        typingListenerJob?.cancel()
        if (channelId.isBlank() || otherUserId.isBlank()) {
            _isTypingOther.value = false
            return
        }
        typingListenerJob = scope.launch {
            repository.getTypingStatus(channelId, otherUserId).collect { isTyping ->
                _isTypingOther.value = isTyping
            }
        }
    }

    fun setOtherUserTyping(isTyping: Boolean) {
        _isTypingOther.value = isTyping
    }

    private var lastTypingSentTime = 0L

    fun onUserTyping(channelId: String, senderId: String, text: String) {
        val now = System.currentTimeMillis()
        if (now - lastTypingSentTime > 2500L) {
            lastTypingSentTime = now
            sendTypingStatus(channelId, senderId, true)
        }
        typingJob?.cancel()
        typingJob = scope.launch {
            delay(3000)
            sendTypingStatus(channelId, senderId, false)
            lastTypingSentTime = 0L
        }
    }

    private fun sendTypingStatus(channelId: String, senderId: String, isTyping: Boolean) {
        scope.launch {
            try {
                repository.setTyping(channelId, senderId, isTyping)
            } catch (_: Exception) {}
        }
    }

    fun clear() {
        typingJob?.cancel()
        typingListenerJob?.cancel()
        typingJob = null
        typingListenerJob = null
        _isTypingOther.value = false
    }
}

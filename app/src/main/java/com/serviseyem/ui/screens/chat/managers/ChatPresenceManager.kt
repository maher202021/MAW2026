package com.serviseyem.ui.screens.chat.managers

import com.serviseyem.data.models.UserPresence
import com.serviseyem.data.repositories.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatPresenceManager(
    private val repository: ChatRepository,
    private val scope: CoroutineScope
) {
    private val _otherUserPresence = MutableStateFlow<UserPresence?>(null)
    val otherUserPresence: StateFlow<UserPresence?> = _otherUserPresence.asStateFlow()

    private var presenceJob: Job? = null

    fun listenToPresence(otherUserId: String) {
        // [تعديل] تتبع حالة الاتصال للمستخدم الآخر بشكل حي ومستمر ودقيق
        presenceJob?.cancel()
        if (otherUserId.isBlank()) return

        presenceJob = scope.launch {
            repository.getUserPresence(otherUserId).collect { presence ->
                _otherUserPresence.value = presence
            }
        }
    }

    fun clear() {
        presenceJob?.cancel()
        _otherUserPresence.value = null
    }
}

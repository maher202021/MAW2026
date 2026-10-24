package com.serviseyem.ui.screens.chat

import androidx.lifecycle.ViewModel
import com.serviseyem.ui.*
import androidx.lifecycle.viewModelScope
import com.serviseyem.data.models.ChatChannel
import com.serviseyem.data.repositories.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {


    private val _channels = MutableStateFlow<List<ChatChannel>>(emptyList())
    val channels: StateFlow<List<ChatChannel>> = _channels.asStateFlow()

    // Filters: ALL, UNREAD, SUPPORT, TECHNICIANS, STORES, RESTAURANTS
    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var channelsJob: Job? = null
    private var lastLoadTime = 0L
    private val CACHE_TTL_MS = 30_000L

    fun loadUserChannels(currentUserId: String, forceRefresh: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!forceRefresh && now - lastLoadTime < CACHE_TTL_MS && _channels.value.isNotEmpty()) {
            _isLoading.value = false
            return
        }
        lastLoadTime = now
        channelsJob?.cancel()
        _isLoading.value = true
        channelsJob = viewModelScope.launch {
            repository.getUserChannels(currentUserId).collect { list ->
                _channels.value = list
                _isLoading.value = false
            }
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setUserPresence(userId: String, isOnline: Boolean) {
        viewModelScope.launch {
            repository.setUserPresence(userId, isOnline)
        }
    }

    fun deleteChannel(channelId: String) {
        _channels.value = _channels.value.filter { it.id != channelId }
        viewModelScope.launch {
            repository.deleteChannel(channelId)
        }
    }

    fun deleteAllChannels(channelsList: List<ChatChannel>) {
        _channels.value = emptyList()
        viewModelScope.launch {
            repository.deleteAllChannels(channelsList)
        }
    }

    override fun onCleared() {
        super.onCleared()
        channelsJob?.cancel()
        channelsJob = null
    }
}


package com.serviseyem.ui

import androidx.lifecycle.viewModelScope
import com.serviseyem.data.ChatChannelEntity
import com.serviseyem.data.models.ChannelType
import com.serviseyem.data.models.MediaType
import com.serviseyem.data.repositories.ChatRepository
import com.serviseyem.utils.AppResult
import kotlinx.coroutines.launch

fun MainViewModel.triggerOpenChatForRequest(requestId: String, customerPhone: String, serviceType: String) {
    val targetPhone = customerPhone.ifBlank { _currentUserPhone.value }
    if (targetPhone.isNotBlank()) {
        getOrCreateChatChannel(
            providerId = "request_$requestId",
            providerName = "صاحب الطلب ($targetPhone)",
            customerId = _currentUserPhone.value.ifBlank { "guest" },
            customerName = _currentUserName.value.ifBlank { "مستخدم الدليل" }
        )
    } else {
        triggerNotification("💬 يمكنك التحدث مع مقدمي العروض عبر شاشة المحادثات")
    }
}

fun MainViewModel.getOrCreateChatChannel(providerId: String, providerName: String, customerId: String, customerName: String) {
    viewModelScope.launch {
        chatRepo.getOrCreateChannel(
            currentUserId = providerId,
            currentUserName = providerName,
            currentUserPhoto = "",
            otherUserId = customerId,
            otherUserName = customerName,
            otherUserPhoto = "",
            type = ChannelType.PRIVATE,
            relatedEntityId = null,
            relatedEntityType = null
        )
    }
}

fun MainViewModel.deleteChatChannel(channelId: String) {
    viewModelScope.launch {
        chatRepo.deleteChannel(channelId)
        triggerToast("تم حذف المحادثة")
    }
}

fun MainViewModel.toggleBlockChatChannel(channelId: String) {
    viewModelScope.launch {
        val ch = _chatChannels.value.find { it.id == channelId } ?: return@launch
        blockChatChannel(channelId, !ch.isBlocked)
    }
}

fun MainViewModel.blockChatChannel(channelId: String, blocked: Boolean) {
    viewModelScope.launch { adminViewModel.crud.updateFields("chat_channels", channelId, mapOf("isBlocked" to blocked)) }
    _activeChatChannel.value = _activeChatChannel.value?.copy(isBlocked = blocked)
}

fun MainViewModel.wipeOldChatChannels(days: Int) {
    triggerToast("🧹 تم تصفية وحذف سجل المحادثات الأقدم من $days أيام بنجاح!")
}

fun MainViewModel.openSupportChat() {
    val currentUserId = authViewModel.getOrGenerateUserId()
    val currentUserName = authViewModel.currentUserName.value.ifBlank { "العميل" }
    val currentUserPhoto = ""
    
    viewModelScope.launch {
        val result = chatRepo.getOrCreateChannel(
            currentUserId = currentUserId,
            currentUserName = currentUserName,
            currentUserPhoto = currentUserPhoto,
            otherUserId = ChatRepository.SUPPORT_ADMIN_ID,
            otherUserName = ChatRepository.SUPPORT_ADMIN_NAME,
            otherUserPhoto = "",
            type = ChannelType.SUPPORT,
            relatedEntityId = null,
            relatedEntityType = null
        )
        if (result is AppResult.Success) {
            targetChatChannelId = result.data.id
            navigateToScreen("CHAT_DIRECT")
        }
    }
}

fun MainViewModel.openChatChannel(channel: ChatChannelEntity?) {
    if (channel == null) return
    val currentUserId = authViewModel.getOrGenerateUserId()
    val currentUserName = authViewModel.currentUserName.value.ifBlank { "العميل" }
    val currentUserPhoto = ""
    
    val otherUserId = channel.participants.firstOrNull { it != currentUserId } ?: ""
    val otherUserName = channel.participantNames[otherUserId] ?: channel.title
    
    viewModelScope.launch {
        val result = chatRepo.getOrCreateChannel(
            currentUserId = currentUserId,
            currentUserName = currentUserName,
            currentUserPhoto = currentUserPhoto,
            otherUserId = otherUserId,
            otherUserName = otherUserName,
            otherUserPhoto = "",
            type = ChannelType.PRIVATE,
            relatedEntityId = null,
            relatedEntityType = null
        )
        if (result is AppResult.Success) {
            targetChatChannelId = result.data.id
        }
    }
}

fun MainViewModel.openOrCreateChatChannel(
    targetId: String,
    targetType: String,
    targetName: String,
    targetPhone: String = "",
    targetCategory: String = "",
    relatedEntityId: String = "",
    relatedEntityType: String = "",
    onCreated: (ChatChannelEntity?) -> Unit
) {
    val myUserId = currentUserId.value
    val isGuest = myUserId.isBlank() || myUserId == "guest" || currentUserPhone.value.isBlank()
    if (isGuest) {
        triggerNotification("⚠️ يجب تسجيل الدخول أو إنشاء حساب لبدء محادثة فورية")
        onCreated(null)
        return
    }

    val isChatDisabled = settings.value.disableChatAll || settings.value.chatRoutingMode == "DISABLED"
    if (isChatDisabled) {
        triggerNotification("⚠️ تم تعطيل خدمة المحادثات الفورية مؤقتاً من قبل الإدارة")
        onCreated(null)
        return
    }

    val curUserName = currentUserName.value.ifBlank { "العميل" }
    val mode = settings.value.chatRoutingMode

    val (finalTargetId, finalTargetName, channelType) = when (mode) {
        "ADMIN_ONLY" -> Triple(
            ChatRepository.SUPPORT_ADMIN_ID,
            ChatRepository.SUPPORT_ADMIN_NAME,
            ChannelType.SUPPORT
        )
        else -> Triple(
            targetId.ifBlank { targetPhone.ifBlank { "provider_$targetName" } },
            targetName.ifBlank { "مقدم الخدمة" },
            if (targetType.equals("SUPPORT", ignoreCase = true)) ChannelType.SUPPORT else ChannelType.PRIVATE
        )
    }
    
    viewModelScope.launch {
        val result = chatRepo.getOrCreateChannel(
            currentUserId = myUserId,
            currentUserName = curUserName,
            currentUserPhoto = "",
            otherUserId = finalTargetId,
            otherUserName = finalTargetName,
            otherUserPhoto = "",
            type = channelType,
            relatedEntityId = relatedEntityId.takeIf { it.isNotBlank() },
            relatedEntityType = relatedEntityType.ifBlank { targetType }.takeIf { it.isNotBlank() }
        )
        
        if (result is AppResult.Success) {
            val ch = result.data
            targetChatChannelId = ch.id
            val existingEntity = _chatChannels.value.find { it.id == ch.id }
            val fullEntity = existingEntity ?: ChatChannelEntity(
                id = ch.id,
                title = ch.title.ifBlank { finalTargetName },
                targetPhone = targetPhone,
                targetCategory = targetCategory,
                participants = ch.participants,
                participantNames = ch.participantNames,
                participantPhotos = ch.participantPhotos,
                lastMessage = ch.lastMessage,
                lastMessageTime = ch.lastMessageTime,
                relatedEntityId = ch.relatedEntityId ?: relatedEntityId,
                relatedEntityType = ch.relatedEntityType ?: relatedEntityType.ifBlank { targetType },
                timestamp = ch.createdAt
            )
            _activeChatChannel.value = fullEntity
            onCreated(fullEntity)
        } else {
            onCreated(null)
        }
    }
}

fun MainViewModel.replyToChatChannel(channelId: String, senderId: String, msgText: String, senderName: String, imageUrl: String = "") {
    if (msgText.trim().isEmpty() && imageUrl.isEmpty()) return
    val resolvedSenderId = if (senderId.isBlank() || senderId == "ADMIN") {
        currentUserId.value.takeIf { it.isNotBlank() && it != "guest" } ?: ChatRepository.SUPPORT_ADMIN_ID
    } else senderId
    viewModelScope.launch {
        chatRepo.sendMessage(channelId, resolvedSenderId, senderName, msgText, if (imageUrl.isNotBlank()) MediaType.IMAGE else MediaType.TEXT, imageUrl, null, null, null)
        triggerToast("تم إرسال الرد بنجاح")
    }
}

fun MainViewModel.sendMessageInChat(msgText: String, imageUrl: String = "") {
    if (msgText.trim().isEmpty() && imageUrl.isEmpty()) return
    val currentUserId = authViewModel.getOrGenerateUserId()
    val currentName = authViewModel.currentUserName.value.ifEmpty { "العميل" }
    viewModelScope.launch {
        val result = chatRepo.getOrCreateChannel(
            currentUserId = currentUserId,
            currentUserName = currentName,
            currentUserPhoto = "",
            otherUserId = ChatRepository.SUPPORT_ADMIN_ID,
            otherUserName = ChatRepository.SUPPORT_ADMIN_NAME,
            otherUserPhoto = "",
            type = ChannelType.SUPPORT
        )
        if (result is AppResult.Success) {
            chatRepo.sendMessage(result.data.id, currentUserId, currentName, msgText, if (imageUrl.isNotBlank()) MediaType.IMAGE else MediaType.TEXT, imageUrl, null, null, null)
            addNotification(
                "💬 رسالة جديدة في الدعم الفني المباشر",
                "من العميل $currentName: ${msgText.ifEmpty { "📷 [صورة]" }}",
                "SUPERVISOR",
                currentUserId
            )
        }
    }
}

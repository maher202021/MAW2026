package com.serviseyem.ui

import com.serviseyem.data.models.ChatChannel
import com.serviseyem.ui.*
import com.serviseyem.data.models.ChannelType

fun MainViewModel.openUnifiedSupportChat() {
    this.openSupportChat()
}

fun MainViewModel.replyToChannelMessage(channelId: String, text: String, senderId: String, senderName: String) {
    this.replyToChatChannel(channelId = channelId, senderId = senderId, msgText = text, senderName = senderName)
}

fun MainViewModel.deleteChatChannelById(channelId: String) {
    this.deleteChatChannel(channelId)
}

fun MainViewModel.blockChatChannelById(channelId: String, isBlocked: Boolean) {
    this.blockChatChannel(channelId, isBlocked)
}

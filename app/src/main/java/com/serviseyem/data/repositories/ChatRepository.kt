package com.serviseyem.data.repositories

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.serviseyem.data.models.ChannelType
import com.serviseyem.data.models.ChatAttachment
import com.serviseyem.data.models.ChatChannel
import com.serviseyem.data.models.ChatMessage
import com.serviseyem.data.models.MediaType
import com.serviseyem.data.models.UserPresence
import com.serviseyem.utils.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * 🚀 ChatRepository
 * الواجهة الموحدة لمستودع المحادثات (Realtime) مع تفويض المهام إلى مكونات متخصصة.
 */
class ChatRepository(
    private val context: Context? = null,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IChatRepository {

    private val channelsCollection = firestore.collection("chat_channels")
    private val presenceCollection = firestore.collection("user_presence")
    private val realtimeDb: com.google.firebase.database.FirebaseDatabase? by lazy {
        try {
            com.google.firebase.database.FirebaseDatabase.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    private val channelDelegate = ChatChannelDelegate(context, firestore, channelsCollection)
    private val messageDelegate = ChatMessageDelegate(firestore, channelsCollection)
    private val presenceDelegate = ChatPresenceDelegate(channelsCollection, presenceCollection)

    companion object {
        const val SUPPORT_ADMIN_ID = "ADMIN"
        const val SUPPORT_ADMIN_NAME = "الدعم الفني"
    }

    override suspend fun getOrCreateChannel(
        currentUserId: String,
        currentUserName: String,
        currentUserPhoto: String,
        otherUserId: String,
        otherUserName: String,
        otherUserPhoto: String,
        type: ChannelType,
        relatedEntityId: String?,
        relatedEntityType: String?
    ): AppResult<ChatChannel> = channelDelegate.getOrCreateChannel(
        currentUserId = currentUserId,
        currentUserName = currentUserName,
        currentUserPhoto = currentUserPhoto,
        otherUserId = otherUserId,
        otherUserName = otherUserName,
        otherUserPhoto = otherUserPhoto,
        type = type,
        relatedEntityId = relatedEntityId,
        relatedEntityType = relatedEntityType
    )

    override suspend fun getChannelById(channelId: String): AppResult<ChatChannel?> =
        channelDelegate.getChannelById(channelId)

    override fun getUserChannels(userId: String): Flow<List<ChatChannel>> =
        channelDelegate.getUserChannels(userId)

    override fun getChannelMessages(channelId: String, currentUserId: String, limit: Int): Flow<List<ChatMessage>> =
        messageDelegate.getChannelMessages(channelId, currentUserId, limit)

    override suspend fun sendMessage(
        channelId: String,
        senderId: String,
        senderName: String,
        messageText: String,
        mediaType: MediaType,
        mediaUrl: String,
        replyToId: String?,
        replyToText: String?,
        attachment: ChatAttachment?,
        audioWaveform: List<Int>
    ): AppResult<ChatMessage> = messageDelegate.sendMessage(
        channelId = channelId,
        senderId = senderId,
        senderName = senderName,
        messageText = messageText,
        mediaType = mediaType,
        mediaUrl = mediaUrl,
        replyToId = replyToId,
        replyToText = replyToText,
        attachment = attachment,
        audioWaveform = audioWaveform
    )

    override suspend fun retryPendingMessages(currentUserId: String): AppResult<Int> = AppResult.Success(0)

    override suspend fun markChannelAsRead(channelId: String, currentUserId: String): AppResult<Unit> =
        messageDelegate.markChannelAsRead(channelId, currentUserId)

    override suspend fun setTyping(channelId: String, userId: String, isTyping: Boolean): AppResult<Unit> =
        presenceDelegate.setTyping(channelId, userId, isTyping)

    override suspend fun toggleBlockUser(channelId: String, userIdToBlock: String, isBlocked: Boolean): AppResult<Unit> =
        presenceDelegate.toggleBlockUser(channelId, userIdToBlock, isBlocked)

    override suspend fun editMessage(
        channelId: String,
        messageId: String,
        newText: String,
        currentUserId: String
    ): AppResult<Unit> = messageDelegate.editMessage(channelId, messageId, newText, currentUserId)

    override suspend fun updateMessageMediaUrl(
        channelId: String,
        messageId: String,
        mediaUrl: String
    ): AppResult<Unit> = messageDelegate.updateMessageMediaUrl(channelId, messageId, mediaUrl)

    override suspend fun deleteMessage(
        channelId: String,
        messageId: String,
        forEveryone: Boolean,
        currentUserId: String
    ): AppResult<Unit> = messageDelegate.deleteMessage(channelId, messageId, forEveryone, currentUserId)

    override suspend fun toggleReaction(
        channelId: String,
        messageId: String,
        userId: String,
        emoji: String
    ): AppResult<Unit> = messageDelegate.toggleReaction(channelId, messageId, userId, emoji)

    override suspend fun togglePinMessage(
        channelId: String,
        messageId: String,
        isPinned: Boolean
    ): AppResult<Unit> = messageDelegate.togglePinMessage(channelId, messageId, isPinned)

    override suspend fun deleteChannel(channelId: String): AppResult<Unit> =
        channelDelegate.deleteChannel(channelId)

    override suspend fun deleteAllChannels(channelsList: List<ChatChannel>): AppResult<Unit> =
        channelDelegate.deleteAllChannels(channelsList)

    override suspend fun setUserPresence(userId: String, isOnline: Boolean): AppResult<Unit> =
        presenceDelegate.setUserPresence(userId, isOnline)

    override fun getUserPresence(userId: String): Flow<UserPresence?> =
        presenceDelegate.getUserPresence(userId)

    override fun getTypingStatus(channelId: String, userId: String): Flow<Boolean> =
        presenceDelegate.getTypingStatus(channelId, userId)

    override suspend fun syncChannelDelta(channelId: String): AppResult<Int> = AppResult.Success(0)
}

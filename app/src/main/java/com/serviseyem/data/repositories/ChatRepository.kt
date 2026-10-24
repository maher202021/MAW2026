package com.serviseyem.data.repositories

import android.content.Context
import android.util.Log
import com.serviseyem.data.models.*
import com.serviseyem.utils.AnalyticsEventsHelper
import com.serviseyem.utils.AppError
import com.serviseyem.utils.AppResult
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

/**
 * 🚀 ChatRepository
 * تطبيق مستودع المحادثات الشامل بنظام (Realtime) والمزامنة الحية مع Firebase Firestore
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
    ): AppResult<ChatChannel> = withContext(Dispatchers.IO) {
        try {
            val cleanCurrent = currentUserId.trim()
            val cleanOther = if (type == ChannelType.SUPPORT) SUPPORT_ADMIN_ID else otherUserId.trim()
            val finalOtherName = if (type == ChannelType.SUPPORT) SUPPORT_ADMIN_NAME else otherUserName
            
            if (cleanCurrent.isBlank()) {
                return@withContext AppResult.Error(AppError.ValidationError("currentUserId", "معرف المستخدم الحالي فارغ"))
            }

            val sortedParticipants = listOf(cleanCurrent, cleanOther).filter { it.isNotBlank() }.sorted()
            
            var customChannelId: String? = null

            if (type == ChannelType.SUPPORT) {
                try {
                    val existingSupportQuery = channelsCollection
                        .whereArrayContains("participants", cleanCurrent)
                        .get().await()
                    
                    val existingSupportDoc = existingSupportQuery.documents.find { doc ->
                        val docType = doc.getString("type") ?: ""
                        val docTitle = doc.getString("title") ?: ""
                        val docId = doc.id
                        docType.equals("SUPPORT", ignoreCase = true) || 
                            docTitle == SUPPORT_ADMIN_NAME || 
                            docId.startsWith("support_") ||
                            docId == "channel_support_${cleanCurrent}"
                    }
                    
                    if (existingSupportDoc != null) {
                        customChannelId = existingSupportDoc.id
                    }
                } catch (e: Exception) {
                    Log.w("ChatRepository", "Error searching existing support channel: ${e.message}")
                }
            }

            val finalChannelId = customChannelId ?: when {
                type == ChannelType.SUPPORT -> "channel_support_${cleanCurrent}"
                sortedParticipants.size == 2 -> "channel_${sortedParticipants[0]}_${sortedParticipants[1]}"
                relatedEntityId != null -> "channel_${type.name.lowercase()}_${relatedEntityId.trim()}_${cleanCurrent}"
                else -> channelsCollection.document().id
            }

            val docRef = channelsCollection.document(finalChannelId)
            val snapshot = docRef.get().await()

            val channelToReturn = if (snapshot.exists()) {
                val existing = snapshot.toObject(ChatChannel::class.java)?.copy(id = snapshot.id) ?: ChatChannel(id = finalChannelId)
                val updatedNames = existing.participantNames.toMutableMap().apply {
                    put(cleanCurrent, currentUserName)
                    if (cleanOther.isNotBlank() && finalOtherName.isNotBlank()) put(cleanOther, finalOtherName)
                }
                val updatedPhotos = existing.participantPhotos.toMutableMap().apply {
                    put(cleanCurrent, currentUserPhoto)
                    if (cleanOther.isNotBlank() && otherUserPhoto.isNotBlank()) put(cleanOther, otherUserPhoto)
                }
                
                val finalChannel = existing.copy(
                    participantNames = updatedNames, 
                    participantPhotos = updatedPhotos,
                    type = if (type == ChannelType.SUPPORT) ChannelType.SUPPORT else existing.type,
                    title = if (type == ChannelType.SUPPORT) "الدعم الفني" else existing.title
                )
                
                if (updatedNames != existing.participantNames || updatedPhotos != existing.participantPhotos) {
                    docRef.update(
                        mapOf(
                            "participantNames" to updatedNames,
                            "participantPhotos" to updatedPhotos,
                            "type" to finalChannel.type.name,
                            "title" to finalChannel.title
                        )
                    ).await()
                }
                finalChannel
            } else {
                val newChannel = ChatChannel(
                    id = finalChannelId,
                    participants = listOf(cleanCurrent, cleanOther).filter { it.isNotBlank() },
                    participantNames = mutableMapOf(cleanCurrent to currentUserName).apply {
                        if (cleanOther.isNotBlank() && finalOtherName.isNotBlank()) put(cleanOther, finalOtherName)
                    },
                    participantPhotos = mutableMapOf(cleanCurrent to currentUserPhoto).apply {
                        if (cleanOther.isNotBlank() && otherUserPhoto.isNotBlank()) put(cleanOther, otherUserPhoto)
                    },
                    type = type,
                    title = if (type == ChannelType.SUPPORT) "الدعم الفني" else finalOtherName.ifBlank { "" },
                    relatedEntityId = relatedEntityId,
                    relatedEntityType = relatedEntityType,
                    lastMessageTime = System.currentTimeMillis()
                )
                docRef.set(newChannel).await()
                newChannel
            }
            AnalyticsEventsHelper.logChatOpened(context, channelToReturn.id, otherUserId)
            AppResult.Success(channelToReturn)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override suspend fun getChannelById(channelId: String): AppResult<ChatChannel?> = withContext(Dispatchers.IO) {
        if (channelId.isBlank()) return@withContext AppResult.Success(null)
        try {
            val snapshot = channelsCollection.document(channelId).get().await()
            if (snapshot.exists()) {
                val channel = snapshot.toObject(ChatChannel::class.java)?.copy(id = snapshot.id)
                AppResult.Success(channel)
            } else {
                AppResult.Success(null)
            }
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override fun getUserChannels(userId: String): Flow<List<ChatChannel>> = callbackFlow {
        val cleanUserId = userId.trim()
        if (cleanUserId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = channelsCollection
            .whereArrayContains("participants", cleanUserId)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Firestore channels listener error: ${error.message}")
                    return@addSnapshotListener
                }

                val allRemoteChannels = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatChannel::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.lastMessageTime } ?: emptyList()

                val remoteChannels = if (cleanUserId.equals("ADMIN", ignoreCase = true)) {
                    allRemoteChannels
                } else {
                    val supportChannels = allRemoteChannels.filter { 
                        it.type == ChannelType.SUPPORT || it.title == "الدعم الفني" || it.id.startsWith("support_") 
                    }
                    val otherChannels = allRemoteChannels.filter { 
                        it.type != ChannelType.SUPPORT && it.title != "الدعم الفني" && !it.id.startsWith("support_") 
                    }
                    val latestSupport = supportChannels.maxByOrNull { it.lastMessageTime }
                    (otherChannels + listOfNotNull(latestSupport)).sortedByDescending { it.lastMessageTime }
                }

                trySend(remoteChannels)
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    override fun getChannelMessages(channelId: String, currentUserId: String, limit: Int): Flow<List<ChatMessage>> = callbackFlow {
        if (channelId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val messagesRef = channelsCollection.document(channelId).collection("messages")
        val query = messagesRef.orderBy("timestamp", Query.Direction.ASCENDING).limitToLast(limit.coerceAtLeast(25).toLong())

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ChatRepository", "Messages listener error: ${error.message}")
                return@addSnapshotListener
            }

            val remoteMessages = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            val visible = remoteMessages.filter { !it.isHiddenFor(currentUserId) }
            trySend(visible)
        }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

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
    ): AppResult<ChatMessage> = withContext(Dispatchers.IO) {
        if (channelId.isBlank() || senderId.isBlank()) {
            return@withContext AppResult.Error(AppError.ValidationError("message", "بيانات الرسالة غير مكتملة"))
        }

        val messageId = channelsCollection.document(channelId).collection("messages").document().id
        val now = System.currentTimeMillis()

        val confirmedMsg = ChatMessage(
            id = messageId,
            channelId = channelId,
            senderId = senderId,
            senderName = senderName,
            message = messageText,
            mediaType = mediaType,
            mediaUrl = mediaUrl,
            attachment = attachment,
            replyToId = replyToId,
            replyToText = replyToText,
            status = MessageStatus.SENT,
            timestamp = now,
            syncStatus = SyncStatus.SYNCED,
            audioWaveform = audioWaveform
        )

        try {
            val channelRef = channelsCollection.document(channelId)
            val channelSnapshot = channelRef.get().await()
            val channel = channelSnapshot.toObject(ChatChannel::class.java)

            if (channel != null && channel.isUserBlocked(senderId)) {
                return@withContext AppResult.Error(AppError.UnauthorizedError("لا يمكنك إرسال الرسالة لأنك محظور في هذه المحادثة."))
            }

            val displayLast = when (mediaType) {
                MediaType.IMAGE -> "📷 صورة"
                MediaType.VIDEO -> "🎥 فيديو"
                MediaType.AUDIO -> "🎤 تسجيل صوتي"
                MediaType.FILE -> "📎 ${attachment?.fileName.orEmpty().ifBlank { "ملف مرفق" }}"
                MediaType.LOCATION -> "📍 موقع جغرافي"
                MediaType.CALL -> "📞 مكالمة صوتية"
                MediaType.TEXT -> messageText
            }

            val updates = mutableMapOf<String, Any>(
                "lastMessage" to displayLast,
                "lastMessageTime" to now,
                "lastMessageSenderId" to senderId,
                "updatedAt" to System.currentTimeMillis()
            )

            channel?.participants?.forEach { pId ->
                if (pId != senderId) {
                    updates["unreadCount.$pId"] = com.google.firebase.firestore.FieldValue.increment(1)
                }
            }

            val batch = firestore.batch()
            batch.set(channelRef.collection("messages").document(messageId), confirmedMsg)
            batch.update(channelRef, updates)
            batch.commit().await()

            AppResult.Success(confirmedMsg)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override suspend fun retryPendingMessages(currentUserId: String): AppResult<Int> = AppResult.Success(0)

    override suspend fun markChannelAsRead(channelId: String, currentUserId: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (channelId.isBlank() || currentUserId.isBlank()) return@withContext AppResult.Success(Unit)
        try {
            val channelRef = channelsCollection.document(channelId)
            channelRef.update("unreadCount.$currentUserId", 0).await()

            val unreadSnapshot = channelRef.collection("messages")
                .whereEqualTo("isRead", false)
                .limit(30)
                .get().await()
            val unreadDocs = unreadSnapshot.documents.filter { doc ->
                val senderId = doc.getString("senderId") ?: ""
                senderId != currentUserId
            }

            if (unreadDocs.isNotEmpty()) {
                val batch = firestore.batch()
                for (doc in unreadDocs) {
                    batch.update(
                        doc.reference,
                        mapOf(
                            "status" to MessageStatus.READ.name,
                            "isRead" to true,
                            "readAt" to System.currentTimeMillis()
                        )
                    )
                }
                batch.commit().await()
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override suspend fun setTyping(channelId: String, userId: String, isTyping: Boolean): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (channelId.isBlank() || userId.isBlank()) return@withContext AppResult.Success(Unit)
        try {
            channelsCollection.document(channelId).update("isTyping.$userId", isTyping).await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override suspend fun toggleBlockUser(channelId: String, userIdToBlock: String, isBlocked: Boolean): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (channelId.isBlank() || userIdToBlock.isBlank()) return@withContext AppResult.Success(Unit)
        try {
            channelsCollection.document(channelId).update("blockedUsers.$userIdToBlock", isBlocked).await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override suspend fun editMessage(
        channelId: String,
        messageId: String,
        newText: String,
        currentUserId: String
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (channelId.isBlank() || messageId.isBlank() || newText.isBlank()) return@withContext AppResult.Success(Unit)
        try {
            val resolvedUserId = currentUserId.trim().ifBlank {
                try {
                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                } catch (_: Exception) {
                    ""
                }
            }
            if (resolvedUserId.isBlank()) {
                return@withContext AppResult.Error(AppError.ValidationError("currentUserId", "غير مصرح بتعديل الرسالة بدون معرف المستخدم"))
            }
            val msgRef = channelsCollection.document(channelId).collection("messages").document(messageId)
            val snapshot = msgRef.get().await()
            val message = snapshot.toObject(ChatMessage::class.java)
            if (message == null || message.senderId != resolvedUserId) {
                return@withContext AppResult.Error(AppError.ValidationError("senderId", "غير مصرح لك بتعديل هذه الرسالة (ليست من إرسالك)"))
            }
            val encryptedText = com.serviseyem.utils.ChatCryptoManager.encrypt(newText.trim())
            msgRef.update(
                mapOf(
                    "message" to encryptedText,
                    "isEdited" to true,
                    "editTimestamp" to System.currentTimeMillis()
                )
            ).await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override suspend fun deleteMessage(
        channelId: String,
        messageId: String,
        forEveryone: Boolean,
        currentUserId: String
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (channelId.isBlank() || messageId.isBlank()) return@withContext AppResult.Success(Unit)
        try {
            val msgRef = channelsCollection.document(channelId).collection("messages").document(messageId)
            if (forEveryone) {
                msgRef.update(
                    mapOf(
                        "isDeleted" to true,
                        "message" to "تم حذف هذه الرسالة"
                    )
                ).await()
            } else {
                msgRef.update("deletedForUsers", FieldValue.arrayUnion(currentUserId)).await()
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override suspend fun toggleReaction(
        channelId: String,
        messageId: String,
        userId: String,
        emoji: String
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        val cleanChannelId = channelId.trim()
        val cleanMessageId = messageId.trim()
        val cleanUserId = userId.trim()
        if (cleanChannelId.isBlank() || cleanMessageId.isBlank() || cleanUserId.isBlank()) {
            return@withContext AppResult.Success(Unit)
        }
        try {
            val msgRef = channelsCollection.document(cleanChannelId).collection("messages").document(cleanMessageId)
            val snapshot = msgRef.get().await()
            val currentReactions = snapshot.toObject(ChatMessage::class.java)?.reactions ?: emptyMap()

            val newValue: Any = if (currentReactions[cleanUserId] == emoji || emoji.isBlank()) {
                FieldValue.delete()
            } else {
                emoji
            }

            msgRef.update("reactions.$cleanUserId", newValue).await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override suspend fun togglePinMessage(
        channelId: String,
        messageId: String,
        isPinned: Boolean
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        val cleanChannelId = channelId.trim()
        val cleanMessageId = messageId.trim()
        if (cleanChannelId.isBlank() || cleanMessageId.isBlank()) return@withContext AppResult.Success(Unit)
        try {
            val msgRef = channelsCollection.document(cleanChannelId).collection("messages").document(cleanMessageId)
            msgRef.update("isPinned", isPinned).await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override suspend fun deleteChannel(channelId: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (channelId.isBlank()) return@withContext AppResult.Success(Unit)
        try {
            val channelDocRef = channelsCollection.document(channelId)
            try {
                com.google.firebase.functions.FirebaseFunctions.getInstance()
                    .getHttpsCallable("deleteChatChannel")
                    .call(mapOf("channelId" to channelId))
                    .await()
            } catch (_: Exception) {
                channelDocRef.delete().await()
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override suspend fun deleteAllChannels(channelsList: List<ChatChannel>): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (channelsList.isEmpty()) return@withContext AppResult.Success(Unit)
        try {
            channelsList.chunked(400).forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { ch ->
                    if (ch.id.isNotBlank()) {
                        batch.delete(channelsCollection.document(ch.id))
                    }
                }
                batch.commit().await()
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override suspend fun setUserPresence(userId: String, isOnline: Boolean): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext AppResult.Success(Unit)
        try {
            val now = System.currentTimeMillis()
            val presence = UserPresence(
                userId = userId,
                isOnline = isOnline,
                lastSeen = now
            )
            presenceCollection.document(userId).set(presence, SetOptions.merge())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    override fun getUserPresence(userId: String): Flow<UserPresence?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = presenceCollection.document(userId).addSnapshotListener { snap, _ ->
            val p = snap?.toObject(UserPresence::class.java)
            trySend(p)
        }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    override fun getTypingStatus(channelId: String, userId: String): Flow<Boolean> = callbackFlow {
        if (channelId.isBlank() || userId.isBlank()) {
            trySend(false)
            close()
            return@callbackFlow
        }

        val listener = channelsCollection.document(channelId).addSnapshotListener { snap, _ ->
            val isTypingMap = snap?.get("isTyping") as? Map<*, *>
            val isTyping = (isTypingMap?.get(userId) as? Boolean) ?: false
            trySend(isTyping)
        }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun syncChannelDelta(channelId: String): AppResult<Int> = AppResult.Success(0)
}

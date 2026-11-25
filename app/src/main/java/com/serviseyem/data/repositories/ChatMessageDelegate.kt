package com.serviseyem.data.repositories

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.serviseyem.data.models.ChatAttachment
import com.serviseyem.data.models.ChatChannel
import com.serviseyem.data.models.ChatMessage
import com.serviseyem.data.models.MediaType
import com.serviseyem.data.models.MessageStatus
import com.serviseyem.data.models.SyncStatus
import com.serviseyem.utils.AppError
import com.serviseyem.utils.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * 💬 ChatMessageDelegate
 * مسؤول عن عمليات الرسائل (جلب، إرسال، تعليم كمقروء، تعديل، حذف، تفاعل، تثبيت).
 */
internal class ChatMessageDelegate(
    private val firestore: FirebaseFirestore,
    private val channelsCollection: CollectionReference
) {

    fun getChannelMessages(channelId: String, currentUserId: String, limit: Int): Flow<List<ChatMessage>> = callbackFlow {
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

    suspend fun sendMessage(
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
                    updates["unreadCount.$pId"] = FieldValue.increment(1)
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

    suspend fun markChannelAsRead(channelId: String, currentUserId: String): AppResult<Unit> = withContext(Dispatchers.IO) {
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

    suspend fun editMessage(
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

    suspend fun updateMessageMediaUrl(
        channelId: String,
        messageId: String,
        mediaUrl: String
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (channelId.isBlank() || messageId.isBlank() || mediaUrl.isBlank()) {
            return@withContext AppResult.Success(Unit)
        }
        try {
            val msgRef = channelsCollection.document(channelId).collection("messages").document(messageId)
            msgRef.update("mediaUrl", mediaUrl).await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    suspend fun deleteMessage(
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

    suspend fun toggleReaction(
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

    suspend fun togglePinMessage(
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
}

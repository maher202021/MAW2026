package com.serviseyem.data.repositories

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.serviseyem.data.models.ChannelType
import com.serviseyem.data.models.ChatChannel
import com.serviseyem.utils.AnalyticsEventsHelper
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
 * 🗂️ ChatChannelDelegate
 * مسؤول عن إدارة قنوات المحادثة (إنشاء/جلب/مراقبة/حذف) لصالح ChatRepository.
 */
internal class ChatChannelDelegate(
    private val context: Context?,
    private val firestore: FirebaseFirestore,
    private val channelsCollection: CollectionReference
) {

    suspend fun getOrCreateChannel(
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
            val cleanOther = if (type == ChannelType.SUPPORT) ChatRepository.SUPPORT_ADMIN_ID else otherUserId.trim()
            val finalOtherName = if (type == ChannelType.SUPPORT) ChatRepository.SUPPORT_ADMIN_NAME else otherUserName

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
                            docTitle == ChatRepository.SUPPORT_ADMIN_NAME ||
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

    suspend fun getChannelById(channelId: String): AppResult<ChatChannel?> = withContext(Dispatchers.IO) {
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

    fun getUserChannels(userId: String): Flow<List<ChatChannel>> = callbackFlow {
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

    suspend fun deleteChannel(channelId: String): AppResult<Unit> = withContext(Dispatchers.IO) {
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

    suspend fun deleteAllChannels(channelsList: List<ChatChannel>): AppResult<Unit> = withContext(Dispatchers.IO) {
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
}

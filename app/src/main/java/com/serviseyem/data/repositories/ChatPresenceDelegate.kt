package com.serviseyem.data.repositories

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import com.serviseyem.data.models.UserPresence
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
 * 🟢 ChatPresenceDelegate
 * مسؤول عن حالة الاتصال (Presence) ومؤشر الكتابة (Typing) وحظر المستخدمين.
 */
internal class ChatPresenceDelegate(
    private val channelsCollection: CollectionReference,
    private val presenceCollection: CollectionReference
) {

    suspend fun setTyping(channelId: String, userId: String, isTyping: Boolean): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (channelId.isBlank() || userId.isBlank()) return@withContext AppResult.Success(Unit)
        try {
            channelsCollection.document(channelId).update("isTyping.$userId", isTyping).await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    suspend fun toggleBlockUser(channelId: String, userIdToBlock: String, isBlocked: Boolean): AppResult<Unit> = withContext(Dispatchers.IO) {
        if (channelId.isBlank() || userIdToBlock.isBlank()) return@withContext AppResult.Success(Unit)
        try {
            channelsCollection.document(channelId).update("blockedUsers.$userIdToBlock", isBlocked).await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(AppError.NetworkError(e))
        }
    }

    suspend fun setUserPresence(userId: String, isOnline: Boolean): AppResult<Unit> = withContext(Dispatchers.IO) {
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

    fun getUserPresence(userId: String): Flow<UserPresence?> = callbackFlow {
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

    fun getTypingStatus(channelId: String, userId: String): Flow<Boolean> = callbackFlow {
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
}

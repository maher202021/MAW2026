package com.serviseyem.data.models

import androidx.annotation.Keep
import java.io.Serializable

/**
 * 🏷️ نوع قناة المحادثة (فردية، دعم فني، مجموعة، متجر، فني)
 */
@Keep
enum class ChannelType {
    PRIVATE,
    SUPPORT,
    GROUP,
    PROVIDER,
    STORE
}

/**
 * 📎 أنواع الوسائط المدعومة في الرسائل
 */
@Keep
enum class MediaType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    LOCATION,
    CALL
}

/**
 * 🚦 حالة إرسال واستلام الرسالة
 */
@Keep
enum class MessageStatus {
    PENDING,
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

/**
 * 🔄 حالة المزامنة المحلية للبيانات
 */
@Keep
enum class SyncStatus {
    SYNCED,
    PENDING_UPLOAD,
    PENDING_DELETE,
    CONFLICT
}

/**
 * 📎 تفاصيل المرفقات للرسائل
 */
@Keep
data class ChatAttachment(
    val url: String = "",
    val localUri: String = "",
    val fileName: String = "",
    val fileSize: Long = 0L,
    val mimeType: String = "",
    val durationSeconds: Int = 0,
    val width: Int = 0,
    val height: Int = 0,
    val thumbnailUrl: String = ""
) : Serializable

/**
 * 👍 تفاعل المستخدمين مع الرسالة (Reactions)
 */
@Keep
data class ChatReaction(
    val userId: String = "",
    val userName: String = "",
    val emoji: String = "👍",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

/**
 * 📝 نموذج قناة المحادثة (ChatChannel)
 *
 * الحقول المعتمدة:
 * - العنوان: title و participantNames
 * - الهاتف: targetPhone
 * - الصورة: groupAvatarUrl و participantPhotos
 *
 * الحقول المهجورة (موجودة للتوافقية فقط ولا يجب استخدامها في المنطق الجديد):
 * - providerName, clientName, targetName, userName, customerName
 * - customerPhone
 * - providerId, clientId
 */
@Keep
data class ChatChannel(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantPhotos: Map<String, String> = emptyMap(),
    val participantRoles: Map<String, String> = emptyMap(), // "ADMIN", "MEMBER"
    val type: ChannelType = ChannelType.PRIVATE,
    val title: String = "",
    val description: String = "",
    val groupAvatarUrl: String = "",
    val relatedEntityId: String? = null,
    val relatedEntityType: String? = null, // BOOKING, URGENT_REQUEST, SUPPORT, STORE
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val lastMessageSenderId: String = "",
    val lastMessageStatus: MessageStatus = MessageStatus.SENT,
    val unreadCount: Map<String, Int> = emptyMap(),
    val isBlocked: Boolean = false,
    val blockedUsers: Map<String, Boolean> = emptyMap(),
    val isTyping: Map<String, Boolean> = emptyMap(),
    val isPinned: Map<String, Boolean> = emptyMap(),
    val isMuted: Map<String, Boolean> = emptyMap(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val channelType: String = "PROVIDER",
    @Deprecated("استخدم title أو participantNames بدلاً منها", ReplaceWith("title"))
    val targetName: String = "",
    val targetPhone: String = "",
    val targetCategory: String = "",
    @Deprecated("استخدم title أو participantNames بدلاً منها", ReplaceWith("title"))
    val userName: String = "",
    @Deprecated("استخدم title أو participantNames بدلاً منها", ReplaceWith("title"))
    val customerName: String = "",
    @Deprecated("استخدم targetPhone بدلاً منها", ReplaceWith("targetPhone"))
    val customerPhone: String = "",
    val isProvider: Boolean = false,
    val timestamp: Long = 0L,
    val unreadCountUser: Int = 0,
    val unreadCountTarget: Int = 0,
    @Deprecated("استخدم title أو participantNames بدلاً منها", ReplaceWith("title"))
    val providerName: String = "",
    val providerPhoto: String = "",
    @Deprecated("استخدم title أو participantNames بدلاً منها", ReplaceWith("title"))
    val clientName: String = "",
    val clientPhoto: String = "",
    val messages: List<ChatMessage> = emptyList()
) : Serializable {
    @Deprecated("استخدم participants بدلاً منها")
    val clientId: String get() = participants.firstOrNull() ?: ""
    val customerId: String get() = participants.firstOrNull() ?: ""
    @Deprecated("استخدم participants بدلاً منها")
    val providerId: String get() = participants.getOrNull(1) ?: ""
    val targetId: String get() = participants.getOrNull(1) ?: ""

    /**
     * يوحّد الحقول المهجورة والقديمة (providerId, clientId, targetName, customerName, timestamp, unreadCountUser...)
     * إلى الحقول القياسية المعتمدة (participants, participantNames, participantPhotos, title, updatedAt, unreadCount).
     */
    fun toCanonicalChannel(): ChatChannel {
        val resolvedTitle = title.ifBlank {
            targetName.ifBlank {
                providerName.ifBlank {
                    customerName.ifBlank {
                        clientName.ifBlank { userName }
                    }
                }
            }
        }
        val resolvedPhone = targetPhone.ifBlank { customerPhone }
        val resolvedAvatar = groupAvatarUrl.ifBlank { providerPhoto.ifBlank { clientPhoto } }
        val resolvedTime = when {
            lastMessageTime > 0L -> lastMessageTime
            timestamp > 0L -> timestamp
            else -> updatedAt
        }
        return copy(
            title = resolvedTitle,
            targetPhone = resolvedPhone,
            groupAvatarUrl = resolvedAvatar,
            lastMessageTime = resolvedTime,
            updatedAt = if (updatedAt > 0L) updatedAt else resolvedTime
        )
    }

    /**
     * الحصول على اسم الطرف الآخر في المحادثات الثنائية
     */
    fun getOtherParticipantName(currentUserId: String): String {
        val otherId = participants.firstOrNull { it != currentUserId }
        if (otherId != null) {
            participantNames[otherId]?.takeIf { it.isNotBlank() }?.let { return it }
        }
        return title.ifBlank {
            targetName.ifBlank { providerName.ifBlank { customerName.ifBlank { clientName.ifBlank { userName.ifBlank { "مستخدم" } } } } }
        }
    }

    /**
     * الحصول على صورة الطرف الآخر في المحادثات الثنائية
     */
    fun getOtherParticipantPhoto(currentUserId: String): String {
        val otherId = participants.firstOrNull { it != currentUserId }
        if (otherId != null) {
            participantPhotos[otherId]?.takeIf { it.isNotBlank() }?.let { return it }
        }
        return groupAvatarUrl.ifBlank { providerPhoto.ifBlank { clientPhoto } }
    }

    /**
     * الحصول على عدد الرسائل غير المقروءة للمستخدم الحالي
     */
    fun getUnreadFor(userId: String): Int {
        unreadCount[userId]?.let { return it }
        if (userId.isNotBlank() && (userId == clientId || userId == customerId)) return unreadCountUser
        if (userId.isNotBlank() && (userId == providerId || userId == targetId)) return unreadCountTarget
        return 0
    }

    /**
     * فحص ما إذا كان المستخدم محظوراً في القناة
     */
    fun isUserBlocked(userId: String): Boolean {
        return isBlocked || blockedUsers[userId] == true
    }
}

/**
 * ✉️ نموذج الرسالة الشامل (ChatMessage)
 */
@Keep
data class ChatMessage(
    val id: String = "",
    val channelId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val senderPhone: String = "",
    val recipientId: String = "",
    val message: String = "",
    val mediaType: MediaType = MediaType.TEXT,
    val mediaUrl: String = "",
    val imageUrl: String = "",
    val audioDurationSec: Int = 0,
    val attachment: ChatAttachment? = null,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val status: MessageStatus = MessageStatus.SENT,
    val statusTime: Long = 0L,
    val reactions: Map<String, String> = emptyMap(), // Map of userId to emoji
    val isEncrypted: Boolean = false,
    val isEdited: Boolean = false,
    val editTimestamp: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedBy: String = "",
    val deletedForUsers: List<String> = emptyList(),
    val fileName: String = "",
    val fileSize: Long = 0L,
    val fileType: String = "",
    val forwardedFrom: String = "",
    val readAt: Long = 0L,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val audioWaveform: List<Int> = emptyList(),
    val isPinned: Boolean = false
) : Serializable {

    fun toCanonicalMessage(chId: String = channelId): ChatMessage {
        val resolvedChannelId = chId.ifBlank { channelId }
        val resolvedMediaUrl = mediaUrl.ifBlank { imageUrl.ifBlank { attachment?.url ?: "" } }
        val resolvedImageUrl = imageUrl.ifBlank { if (mediaType == MediaType.IMAGE) resolvedMediaUrl else "" }
        val resolvedMediaType = if (mediaType == MediaType.TEXT && resolvedImageUrl.isNotBlank()) {
            MediaType.IMAGE
        } else {
            mediaType
        }
        return copy(
            channelId = resolvedChannelId,
            mediaUrl = resolvedMediaUrl,
            imageUrl = resolvedImageUrl,
            mediaType = resolvedMediaType
        )
    }

    /**
     * فحص ما إذا كانت الرسالة مرسلة من المستخدم الحالي
     */
    fun isSentBy(userId: String): Boolean = senderId == userId

    /**
     * التحقق مما إذا تم حذف الرسالة للمستخدم المحدد
     */
    fun isHiddenFor(userId: String): Boolean = isDeleted || deletedForUsers.contains(userId)
}

/**
 * 🟢 حالة تواجد المستخدم (UserPresence)
 */
@Keep
data class UserPresence(
    val userId: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val currentChannelId: String? = null,
    val deviceStatus: String = "ACTIVE"
) : Serializable

/**
 * 🔄 حالة المزامنة التفاضلية (Delta Sync State)
 */
@Keep
data class ChatSyncState(
    val channelId: String = "",
    val lastSyncedTimestamp: Long = 0L,
    val lastMessageCount: Int = 0,
    val isSyncing: Boolean = false
) : Serializable

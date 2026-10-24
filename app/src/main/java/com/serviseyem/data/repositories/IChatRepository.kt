package com.serviseyem.data.repositories

import com.serviseyem.data.models.*
import com.serviseyem.utils.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * 🏛️ IChatRepository
 * واجهة مستودع المحادثات وفق مبادئ Clean Architecture
 */
interface IChatRepository {

    /**
     * جلب أو إنشاء قناة محادثة بين طرفين أو قناة دعم فني
     */
    suspend fun getOrCreateChannel(
        currentUserId: String,
        currentUserName: String,
        currentUserPhoto: String,
        otherUserId: String,
        otherUserName: String,
        otherUserPhoto: String,
        type: ChannelType = ChannelType.PRIVATE,
        relatedEntityId: String? = null,
        relatedEntityType: String? = null
    ): AppResult<ChatChannel>

    /**
     * جلب بيانات القناة بالمعرف
     */
    suspend fun getChannelById(channelId: String): AppResult<ChatChannel?>

    /**
     * مراقبة قنوات المستخدم بنظام Offline-First مع التحديث الفوري
     */
    fun getUserChannels(userId: String): Flow<List<ChatChannel>>

    /**
     * مراقبة رسائل القناة بنظام Offline-First مع التحديث الفوري والصفحات (Pagination)
     */
    fun getChannelMessages(channelId: String, currentUserId: String, limit: Int = 30): Flow<List<ChatMessage>>

    /**
     * إرسال رسالة جديدة مع دعم العمل دون اتصال والمزامنة التلقائية
     */
    suspend fun sendMessage(
        channelId: String,
        senderId: String,
        senderName: String,
        messageText: String,
        mediaType: MediaType = MediaType.TEXT,
        mediaUrl: String = "",
        replyToId: String? = null,
        replyToText: String? = null,
        attachment: ChatAttachment? = null,
        audioWaveform: List<Int> = emptyList()
    ): AppResult<ChatMessage>

    /**
     * إعادة إرسال كافة الرسائل المعلقة في الطابور عند استعادة الاتصال
     */
    suspend fun retryPendingMessages(currentUserId: String): AppResult<Int>

    /**
     * تعيين حالة القراءة للرسائل
     */
    suspend fun markChannelAsRead(channelId: String, currentUserId: String): AppResult<Unit>

    /**
     * تحديث حالة الكتابة للمستخدم
     */
    suspend fun setTyping(channelId: String, userId: String, isTyping: Boolean): AppResult<Unit>

    /**
     * حظر أو إلغاء حظر مستخدم داخل القناة
     */
    suspend fun toggleBlockUser(channelId: String, userIdToBlock: String, isBlocked: Boolean): AppResult<Unit>

    /**
     * تعديل نص رسالة سابقة (مع التحقق من ملكية الرسالة currentUserId == message.senderId)
     */
    suspend fun editMessage(channelId: String, messageId: String, newText: String, currentUserId: String = ""): AppResult<Unit>

    /**
     * حذف رسالة (للجميع أو للمستخدم فقط)
     */
    suspend fun deleteMessage(channelId: String, messageId: String, forEveryone: Boolean, currentUserId: String): AppResult<Unit>

    /**
     * إضافة أو إزالة تفاعل على الرسالة (Reaction)
     */
    suspend fun toggleReaction(channelId: String, messageId: String, userId: String, emoji: String): AppResult<Unit>

    /**
     * تثبيت أو إلغاء تثبيت الرسالة
     */
    suspend fun togglePinMessage(channelId: String, messageId: String, isPinned: Boolean): AppResult<Unit>

    /**
     * حذف قناة المحادثة
     */
    suspend fun deleteChannel(channelId: String): AppResult<Unit>

    /**
     * حذف قائمة قنوات بالكامل
     */
    suspend fun deleteAllChannels(channelsList: List<ChatChannel>): AppResult<Unit>

    /**
     * تحديث حالة التواجد للمستخدم (Online / Offline)
     */
    suspend fun setUserPresence(userId: String, isOnline: Boolean): AppResult<Unit>

    /**
     * مراقبة حالة تواجد مستخدم آخر لحظياً
     */
    fun getUserPresence(userId: String): Flow<UserPresence?>

    /**
     * مراقبة حالة الكتابة لمستخدم في قناة معينة لحظياً
     */
    fun getTypingStatus(channelId: String, userId: String): Flow<Boolean>

    /**
     * المزامنة التفاضلية (Delta Sync) للرسائل الجديدة فقط
     */
    suspend fun syncChannelDelta(channelId: String): AppResult<Int>
}

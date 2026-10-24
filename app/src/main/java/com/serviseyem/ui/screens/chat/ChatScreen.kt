package com.serviseyem.ui.screens.chat

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import com.serviseyem.ui.screens.chat.managers.ChatSmartSuggestionsEngine
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.serviseyem.data.models.ChatChannel
import com.serviseyem.data.models.ChatMessage
import com.serviseyem.data.models.MediaType
import com.serviseyem.data.models.MessageStatus
import com.serviseyem.ui.screens.chat.components.ChatBubbleItem
import com.serviseyem.ui.screens.chat.components.ChatHeaderBar
import com.serviseyem.ui.screens.chat.components.ChatInputBar
import com.serviseyem.ui.screens.chat.components.ChatMediaGalleryDialog
import com.serviseyem.ui.screens.chat.components.TypingIndicator
import com.serviseyem.utils.AudioPlayerManager
import com.serviseyem.utils.DateFormatter
import com.serviseyem.utils.VisualThemePalette
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun ChatScreen(
    currentUserId: String,
    currentUserName: String,
    currentUserPhoto: String = "",
    channel: ChatChannel? = null,
    channelId: String? = null,
    targetUserId: String? = null,
    targetUserName: String? = null,
    targetUserPhoto: String? = null,
    relatedEntityId: String? = null,
    relatedEntityType: String? = null,
    showClientIdentityToProviders: Boolean = true,
    themeColors: VisualThemePalette,
    chatViewModel: ChatViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val effectiveUserId = currentUserId.ifBlank { "guest_user" }
    val effectiveUserName = currentUserName.ifBlank { "أنا" }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val currentChannel by chatViewModel.currentChannel.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val presence by chatViewModel.otherUserPresence.collectAsState()
    val isTypingOther by chatViewModel.isTypingOther.collectAsState()
    val replyingTo by chatViewModel.replyingToMessage.collectAsState()
    val searchQuery by chatViewModel.searchQuery.collectAsState()

    var isSearchOpen by remember { mutableStateOf(false) }
    var selectedMessageForAction by remember { mutableStateOf<ChatMessage?>(null) }
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var showDeleteChannelDialog by remember { mutableStateOf(false) }
    var showMediaGallery by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        if (isSearchOpen) {
            isSearchOpen = false
            chatViewModel.setSearchQuery("")
        } else if (selectedMessageForAction != null) {
            selectedMessageForAction = null
        } else if (editingMessage != null) {
            editingMessage = null
        } else {
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        chatViewModel.eventFlow.collect { event ->
            when (event) {
                is ChatEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ChatEvent.MessageSent -> {
                    // Success
                }
            }
        }
    }

    // Initialize Channel
    LaunchedEffect(channel, channelId, targetUserId, relatedEntityId, relatedEntityType) {
        if (channel != null) {
            chatViewModel.openChannel(channel, effectiveUserId)
        } else if (!channelId.isNullOrBlank()) {
            chatViewModel.openChannelById(
                channelId = channelId,
                currentUserId = effectiveUserId,
                currentUserName = effectiveUserName,
                fallbackTargetUserId = targetUserId,
                fallbackUserName = targetUserName,
                relatedEntityId = relatedEntityId,
                relatedEntityType = relatedEntityType
            )
        } else if (!targetUserId.isNullOrBlank()) {
            chatViewModel.startDirectChat(
                currentUserId = effectiveUserId,
                currentUserName = effectiveUserName,
                currentUserPhoto = currentUserPhoto,
                otherUserId = targetUserId,
                otherUserName = targetUserName ?: "مستخدم",
                otherUserPhoto = targetUserPhoto ?: "",
                relatedEntityId = relatedEntityId,
                relatedEntityType = relatedEntityType
            )
        }
    }

    val activeChannel = currentChannel

    // Mark as read only when opening or closing the chat, not on every single message
    DisposableEffect(activeChannel) {
        val chId = activeChannel?.id ?: channelId
        if (!chId.isNullOrBlank()) {
            chatViewModel.markAsRead(chId, currentUserId)
        }
        onDispose {
            if (!chId.isNullOrBlank()) {
                chatViewModel.markAsRead(chId, currentUserId)
            }
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Stop active audio on screen exit and attach consecutive voice playback
    DisposableEffect(Unit) {
        onDispose {
            AudioPlayerManager.stop()
            AudioPlayerManager.onAudioFinished = null
        }
    }

    LaunchedEffect(messages) {
        AudioPlayerManager.onAudioFinished = { completedId ->
            val audioMsgs = messages.filter { it.mediaType == MediaType.AUDIO && it.mediaUrl.isNotBlank() }
            val idx = audioMsgs.indexOfFirst { it.id == completedId }
            if (idx in 0 until audioMsgs.size - 1) {
                val nextMsg = audioMsgs[idx + 1]
                AudioPlayerManager.play(nextMsg.id, nextMsg.mediaUrl, context)
            }
        }
    }

    val otherUserId = remember(activeChannel, currentUserId, targetUserId) {
        activeChannel?.participants?.firstOrNull { it != currentUserId } ?: targetUserId ?: ""
    }
    val otherUserName = remember(activeChannel, otherUserId, targetUserName, showClientIdentityToProviders) {
        val rawName = activeChannel?.participantNames?.get(otherUserId) 
            ?: targetUserName 
            ?: activeChannel?.title?.takeIf { it.isNotBlank() } 
            ?: "محادثة"
        
        // If client identity is hidden from providers and current user is the provider/owner
        if (!showClientIdentityToProviders && (activeChannel?.relatedEntityType == "PROVIDER" || relatedEntityType == "PROVIDER" || activeChannel?.type == com.serviseyem.data.models.ChannelType.PROVIDER)) {
            val isCurrentViewerProvider = currentUserId == (activeChannel?.relatedEntityId ?: relatedEntityId) || currentUserId.startsWith("provider_")
            if (isCurrentViewerProvider) {
                "العميل"
            } else {
                rawName
            }
        } else {
            rawName
        }
    }
    val otherUserPhoto = remember(activeChannel, otherUserId, targetUserPhoto) {
        activeChannel?.participantPhotos?.get(otherUserId) ?: targetUserPhoto ?: ""
    }

    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) messages
        else messages.filter { it.message.contains(searchQuery, ignoreCase = true) }
    }

    val showScrollToBottom by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 3 && lastVisibleItemIndex < totalItems - 2
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize() // FIXED: Removed root imePadding to prevent screen distortion when keyboard opens
        ) {
            // 1. Fixed Top Header Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                ChatHeaderBar(
                    name = otherUserName,
                    photoUrl = otherUserPhoto,
                    presence = presence,
                    isTyping = isTypingOther,
                    relatedEntityId = activeChannel?.relatedEntityId ?: relatedEntityId,
                    relatedEntityType = activeChannel?.relatedEntityType ?: relatedEntityType,
                    relatedEntityStatus = "قيد التنفيذ والمتابعة",
                    onBackClick = onBackClick,
                    onSearchToggle = {
                        isSearchOpen = !isSearchOpen
                        if (!isSearchOpen) chatViewModel.setSearchQuery("")
                    },
                    onBlockClick = {
                        if (otherUserId.isNotBlank()) {
                            chatViewModel.toggleBlock(otherUserId, true)
                            Toast.makeText(context, "تم حظر المستخدم", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDeleteChannelClick = {
                        showDeleteChannelDialog = true
                    },
                    onMediaGalleryClick = {
                        showMediaGallery = true
                    },
                    onContextClick = {
                        val refId = (activeChannel?.relatedEntityId ?: relatedEntityId)?.takeLast(6) ?: ""
                        Toast.makeText(context, "الخدمة مرتبطة بالسجل #$refId", Toast.LENGTH_SHORT).show()
                    },
                    themeColors = themeColors
                )

                // Pinned Message Banner
                val pinnedMessage = remember(messages) { messages.findLast { it.isPinned } }
                if (pinnedMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.25f))
                            .clickable {
                                val idx = filteredMessages.indexOfFirst { it.id == pinnedMessage.id }
                                if (idx >= 0) {
                                    scope.launch { listState.animateScrollToItem(idx) }
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📌", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "رسالة مثبتة",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                                Text(
                                    text = pinnedMessage.message.ifBlank { "مرفق وسائط" },
                                    fontSize = 12.sp,
                                    color = Color.LightGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = { chatViewModel.togglePinMessage(pinnedMessage.id, false) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إلغاء التثبيت", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                AnimatedVisibility(visible = isSearchOpen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themeColors.surface)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { chatViewModel.setSearchQuery(it) },
                            placeholder = { Text("بحث في المحادثة...", fontSize = 12.sp, color = themeColors.textSecondary) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.textPrimary,
                                unfocusedTextColor = themeColors.textPrimary,
                                focusedBorderColor = themeColors.accent
                            )
                        )
                        IconButton(onClick = {
                            isSearchOpen = false
                            chatViewModel.setSearchQuery("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = themeColors.textPrimary)
                        }
                    }
                }
            }

            // 2. Messages List (takes available vertical space)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (filteredMessages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "لا توجد رسائل مطابقة للبحث" else "لا توجد رسائل سابقة. ابدأ المحادثة الآن!",
                            color = themeColors.textSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item(key = "load_more_header") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { chatViewModel.loadMoreMessages() }) {
                                    Text(
                                        text = "⬆️ تحميل الرسائل السابقة",
                                        fontSize = 12.sp,
                                        color = themeColors.accent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Smart conversation summary card when opening a busy chat
                        val smartSummary = ChatSmartSuggestionsEngine.summarizeMessages(filteredMessages)
                        if (smartSummary != null) {
                            item(key = "conversation_summary_card") {
                                Surface(
                                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("📋", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = smartSummary.summaryTitle,
                                                color = Color(0xFF38BDF8),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = smartSummary.summaryDetails,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(filteredMessages, key = { _, msg -> msg.id }) { index, msg ->
                            val showDateSeparator = index == 0 || !isSameDay(filteredMessages[index - 1].timestamp, msg.timestamp)
                            if (showDateSeparator && msg.timestamp > 0) {
                                DateSeparatorChip(dateMillis = msg.timestamp)
                            }

                            val isMe = msg.senderId == currentUserId
                            val isPreviousSame = index > 0 &&
                                    filteredMessages[index - 1].senderId == msg.senderId &&
                                    !showDateSeparator
                            val isNextSame = index < filteredMessages.size - 1 &&
                                    filteredMessages[index + 1].senderId == msg.senderId &&
                                    isSameDay(filteredMessages[index + 1].timestamp, msg.timestamp)

                            ChatBubbleItem(
                                message = msg,
                                isMe = isMe,
                                isPreviousSameSender = isPreviousSame,
                                isNextSameSender = isNextSame,
                                relatedEntityType = activeChannel?.relatedEntityType ?: relatedEntityType,
                                onReplyClick = { chatViewModel.setReplyingTo(msg) },
                                onLongClick = { selectedMessageForAction = msg },
                                onReactionClick = { emoji -> chatViewModel.toggleReaction(msg.id, emoji) },
                                onRetryClick = { chatViewModel.resendMessage(msg.id) },
                                themeColors = themeColors
                            )
                        }
                    }
                }

                // Floating Scroll to Bottom button
                androidx.compose.animation.AnimatedVisibility(
                    visible = showScrollToBottom,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            scope.launch {
                                if (filteredMessages.isNotEmpty()) {
                                    listState.animateScrollToItem(filteredMessages.size - 1)
                                }
                            }
                        },
                        containerColor = themeColors.accent,
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "الانتقال لأسفل",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 3. Bottom Chat Input (Anchored directly above keyboard)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                if (isTypingOther) {
                    TypingIndicator(
                        userName = activeChannel?.participantNames?.get(otherUserId) ?: otherUserName
                    )
                }

                // 🧠 Smart Quick Reply Suggestions (اقتراحات الردود الذكية فوق شريط الإدخال)
                val lastMsg = messages.lastOrNull()
                val isLastMsgMine = lastMsg?.senderId == currentUserId
                val smartSuggestions = remember(lastMsg?.id, lastMsg?.message, isLastMsgMine) {
                    ChatSmartSuggestionsEngine.generateSuggestions(
                        lastMessage = lastMsg,
                        isLastMessageMine = isLastMsgMine,
                        relatedEntityType = activeChannel?.relatedEntityType ?: relatedEntityType
                    )
                }

                if (smartSuggestions.isNotEmpty() && !isTypingOther && editingMessage == null) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(smartSuggestions) { suggestion ->
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                                shadowElevation = 1.dp,
                                modifier = Modifier.clickable {
                                    chatViewModel.sendMessage(
                                        senderId = currentUserId,
                                        senderName = currentUserName,
                                        text = suggestion,
                                        mediaType = MediaType.TEXT
                                    )
                                }
                            ) {
                                Text(
                                    text = suggestion,
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                ChatInputBar(
                    channelId = activeChannel?.id ?: channelId ?: "direct_chat",
                    replyingTo = replyingTo,
                    editingMessage = editingMessage,
                    onCancelReply = { chatViewModel.setReplyingTo(null) },
                    onCancelEdit = { editingMessage = null },
                    onSendMessage = { text, mediaType, mediaUrl ->
                        chatViewModel.sendMessage(
                            senderId = effectiveUserId,
                            senderName = effectiveUserName,
                            text = text,
                            mediaType = mediaType,
                            mediaUrl = mediaUrl
                        )
                    },
                    onSendAudioMessage = { text, mediaUrl, waveform ->
                        chatViewModel.sendMessage(
                            senderId = effectiveUserId,
                            senderName = effectiveUserName,
                            text = text,
                            mediaType = MediaType.AUDIO,
                            mediaUrl = mediaUrl,
                            audioWaveform = waveform
                        )
                    },
                    onEditMessage = { messageId, newText ->
                        chatViewModel.editMessage(messageId, newText)
                        editingMessage = null
                    },
                    onTyping = { text ->
                        chatViewModel.onUserTyping(effectiveUserId, text)
                    },
                    themeColors = themeColors
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Message options dialog
    val targetMsg = selectedMessageForAction
    if (targetMsg != null) {
        val isMe = targetMsg.senderId == currentUserId

        AlertDialog(
            onDismissRequest = { selectedMessageForAction = null },
            title = { Text("خيارات الرسالة", fontSize = 14.sp, color = themeColors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Quick Reaction Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("❤️", "👍", "😂", "😮", "😢", "🙏").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        chatViewModel.toggleReaction(targetMsg.id, emoji)
                                        selectedMessageForAction = null
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    // Pin / Unpin
                    TextButton(onClick = {
                        chatViewModel.togglePinMessage(targetMsg.id, !targetMsg.isPinned)
                        selectedMessageForAction = null
                    }) {
                        Text(if (targetMsg.isPinned) "📌 إلغاء تثبيت الرسالة" else "📌 تثبيت الرسالة", color = Color(0xFFFFD54F), fontSize = 13.sp)
                    }

                    if (isMe && targetMsg.mediaType == MediaType.TEXT) {
                        TextButton(onClick = {
                            editingMessage = targetMsg
                            selectedMessageForAction = null
                        }) {
                            Text("✏️ تعديل الرسالة", color = themeColors.accent, fontSize = 13.sp)
                        }
                    }
                    TextButton(onClick = {
                        chatViewModel.setReplyingTo(targetMsg)
                        selectedMessageForAction = null
                    }) {
                        Text("↩️ الرد على الرسالة", color = themeColors.textPrimary, fontSize = 13.sp)
                    }
                    if (isMe) {
                        TextButton(onClick = {
                            chatViewModel.deleteMessage(targetMsg.id, true, currentUserId)
                            selectedMessageForAction = null
                        }) {
                            Text("🗑️ حذف لدى الجميع", color = Color(0xFFE53935), fontSize = 13.sp)
                        }
                    }
                    TextButton(onClick = {
                        chatViewModel.deleteMessage(targetMsg.id, false, currentUserId)
                        selectedMessageForAction = null
                    }) {
                        Text("🗑️ حذف لدي فقط", color = themeColors.textSecondary, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {},
            containerColor = themeColors.surface
        )
    }

    // Channel Deletion Confirmation Dialog
    if (showDeleteChannelDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteChannelDialog = false },
            title = { Text("حذف المحادثة بالكامل", color = themeColors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في حذف هذه المحادثة وجميع رسائلها؟ لا يمكن استرجاع البيانات بعد الحذف.", color = themeColors.textSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteChannelDialog = false
                        chatViewModel.deleteCurrentChannel {
                            onBackClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("تأكيد الحذف", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChannelDialog = false }) {
                    Text("إلغاء", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }

    // Media Gallery Dialog
    if (showMediaGallery) {
        ChatMediaGalleryDialog(
            messages = messages,
            onDismiss = { showMediaGallery = false },
            themeColors = themeColors
        )
    }
}

@Composable
private fun DateSeparatorChip(dateMillis: Long) {
    val dateText = remember(dateMillis) {
        formatChatDateHeader(dateMillis)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFF1E293B).copy(alpha = 0.9f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            shadowElevation = 2.dp
        ) {
            Text(
                text = dateText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )
        }
    }
}

private fun isSameDay(time1: Long, time2: Long): Boolean {
    if (time1 <= 0 || time2 <= 0) return false
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun formatChatDateHeader(timeMillis: Long): String {
    val now = Calendar.getInstance()
    val msgCal = Calendar.getInstance().apply { timeInMillis = timeMillis }

    val currentYear = now.get(Calendar.YEAR)
    val msgYear = msgCal.get(Calendar.YEAR)
    val currentDay = now.get(Calendar.DAY_OF_YEAR)
    val msgDay = msgCal.get(Calendar.DAY_OF_YEAR)

    return when {
        currentYear == msgYear && currentDay == msgDay -> "اليوم"
        currentYear == msgYear && (currentDay - msgDay == 1) -> "أمس"
        // ✨ م2-ج3: استخدام DateFormatter
        currentYear == msgYear -> DateFormatter.formatCustom(timeMillis, "EEEE، d MMMM")
        else -> DateFormatter.formatCustom(timeMillis, "d MMMM yyyy")
    }
}

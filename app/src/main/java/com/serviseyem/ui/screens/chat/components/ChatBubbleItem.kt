package com.serviseyem.ui.screens.chat.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.models.ChatMessage
import com.serviseyem.data.models.MediaType
import com.serviseyem.data.models.MessageStatus
import com.serviseyem.utils.AudioPlayerManager
import com.serviseyem.utils.ChatIcons
import com.serviseyem.utils.DateFormatter
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    isMe: Boolean,
    isPreviousSameSender: Boolean = false,
    isNextSameSender: Boolean = false,
    relatedEntityType: String? = null,
    onReplyClick: () -> Unit,
    onLongClick: () -> Unit,
    onReactionClick: ((emoji: String) -> Unit)? = null,
    onRetryClick: ((String) -> Unit)? = null,
    themeColors: VisualThemePalette? = null
) {
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val formattedTime = DateFormatter.formatTime(message.timestamp)

    // =========================================================================
    // 1. SYSTEM / CONTEXTUAL MESSAGES (الرسائل السياقية التلقائية الذكية)
    // =========================================================================
    val isSystem = message.senderId == "system" ||
            message.senderName == "النظام" ||
            message.message.startsWith("[SYSTEM]") ||
            message.message.startsWith("[نظام]")

    if (isSystem) {
        val cleanText = message.message
            .removePrefix("[SYSTEM]")
            .removePrefix("[نظام]")
            .trim()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.25f)),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = cleanText,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedTime,
                        color = Color(0xFF94A3B8),
                        fontSize = 9.5.sp
                    )
                }
            }
        }
        return
    }

    // =========================================================================
    // 2. STYLING & DYNAMIC ENTITY PALETTE
    // =========================================================================
    val isSupport = !isMe && (
            message.senderId.contains("admin", ignoreCase = true) ||
            message.senderId.contains("support", ignoreCase = true) ||
            message.senderName.contains("الدعم", ignoreCase = true) ||
            message.senderName.contains("إدارة", ignoreCase = true) ||
            message.senderName.contains("الادارة", ignoreCase = true)
    )

    val primaryColor = themeColors?.primary ?: Color(0xFF1E88E5)
    val surfaceColor = themeColors?.surface ?: Color(0xFF1E293B)
    val textPrimaryColor = themeColors?.textPrimary ?: Color.White

    // Dynamic gradient by entity type for the sender bubble
    val bubbleBrush = if (isMe) {
        when (relatedEntityType?.uppercase()) {
            "PROVIDER" -> Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)))
            "STORE", "RESTAURANT" -> Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF047857)))
            "MEDICAL" -> Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFBE123C)))
            "PROPERTY" -> Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF6D28D9)))
            else -> Brush.horizontalGradient(listOf(primaryColor, primaryColor.copy(alpha = 0.9f)))
        }
    } else if (isSupport) {
        Brush.horizontalGradient(listOf(Color(0xFF112618), Color(0xFF1B3D28)))
    } else {
        Brush.horizontalGradient(listOf(surfaceColor, surfaceColor))
    }

    // Asymmetric corners with consecutive message grouping (Telegram / Signal style)
    val topStartRadius = if (isMe || !isPreviousSameSender) 16.dp else 4.dp
    val topEndRadius = if (!isMe || !isPreviousSameSender) 16.dp else 4.dp
    val bottomStartRadius = if (isMe) 16.dp else if (!isNextSameSender) 4.dp else 14.dp
    val bottomEndRadius = if (!isMe) 16.dp else if (!isNextSameSender) 4.dp else 14.dp
    val bubbleShape = RoundedCornerShape(
        topStart = topStartRadius,
        topEnd = topEndRadius,
        bottomStart = bottomStartRadius,
        bottomEnd = bottomEndRadius
    )

    // =========================================================================
    // 3. SWIPE TO REPLY GESTURE (السحب السريع للرد)
    // =========================================================================
    val offsetX = remember { Animatable(0f) }
    var showQuickReactionOverlay by remember { mutableStateOf(false) }

    val swipeThresholdPx = 140f
    val isSwipingToReply = abs(offsetX.value) > 70f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 8.dp,
                end = 8.dp,
                top = if (isPreviousSameSender) 1.5.dp else 4.dp,
                bottom = if (isNextSameSender) 1.5.dp else 4.dp
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {},
                    onDragEnd = {
                        if (abs(offsetX.value) >= swipeThresholdPx) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onReplyClick()
                        }
                        scope.launch {
                            offsetX.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 400f))
                        }
                    },
                    onDragCancel = {
                        scope.launch { offsetX.animateTo(0f) }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        val target = offsetX.value + (dragAmount * 0.6f)
                        val clamped = if (isMe) {
                            target.coerceIn(-200f, 0f)
                        } else {
                            target.coerceIn(0f, 200f)
                        }
                        scope.launch { offsetX.snapTo(clamped) }
                    }
                )
            }
    ) {
        // Reply icon indicator revealed during horizontal swipe
        AnimatedVisibility(
            visible = isSwipingToReply,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(if (isMe) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFF38BDF8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = "رد",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Main Bubble Column with horizontal offset
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) },
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 295.dp)
                    .clip(bubbleShape)
                    .then(
                        if (isSupport) {
                            Modifier.border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD700), Color(0xFF10B981))
                                ),
                                shape = bubbleShape
                            )
                        } else Modifier
                    )
                    .background(bubbleBrush)
                    .clickable {
                        showQuickReactionOverlay = !showQuickReactionOverlay
                    }
                    .padding(10.dp)
            ) {
                // Support badge
                if (isSupport) {
                    Text(
                        text = "🛠️ الدعم الفني والإدارة",
                        fontSize = 10.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Pinned indicator
                if (message.isPinned) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "📌 مثبتة",
                            fontSize = 10.sp,
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Replying quote preview
                if (!message.replyToText.isNullOrBlank()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clickable { onReplyClick() }
                    ) {
                        Row(
                            modifier = Modifier.padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(Color(0xFF64B5F6), RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.replyToText,
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Media attachment
                when (message.mediaType) {
                    MediaType.IMAGE -> {
                        if (message.mediaUrl.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.2f))
                            ) {
                                AsyncImage(
                                    model = message.mediaUrl,
                                    contentDescription = "صورة مرفقة",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                    MediaType.AUDIO -> {
                        val context = LocalContext.current
                        val currentPlayingId by AudioPlayerManager.currentPlayingId.collectAsState()
                        val isAudioPlaying by AudioPlayerManager.isPlaying.collectAsState()
                        val liveProgress by AudioPlayerManager.playbackProgress.collectAsState()
                        val isThisAudioActive = currentPlayingId == message.id && isAudioPlaying

                        // Amplitudes for waveform visualization
                        val defaultWaveform = remember {
                            listOf(25, 45, 60, 30, 75, 90, 65, 40, 80, 50, 70, 95, 85, 60, 45, 75, 90, 55, 35, 65, 80, 50, 30, 60)
                        }
                        val waveformBars = remember(message.audioWaveform) {
                            if (message.audioWaveform.isNotEmpty()) {
                                val raw = message.audioWaveform
                                if (raw.size >= 24) raw.take(24)
                                else {
                                    val padded = raw.toMutableList()
                                    while (padded.size < 24) padded.add(40)
                                    padded
                                }
                            } else {
                                defaultWaveform
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (isThisAudioActive) {
                                        AudioPlayerManager.pause()
                                    } else if (currentPlayingId == message.id) {
                                        AudioPlayerManager.resume()
                                    } else {
                                        val source = message.mediaUrl.ifBlank { message.attachment?.url.orEmpty() }
                                        AudioPlayerManager.play(message.id, source, context)
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(if (isThisAudioActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isThisAudioActive) ChatIcons.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isThisAudioActive) "إيقاف مؤقت" else "تشغيل التسجيل",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                val progress = if (currentPlayingId == message.id) liveProgress else 0f
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val barWidth = 3.dp.toPx()
                                        val barSpacing = 2.5.dp.toPx()
                                        val totalWidth = waveformBars.size * (barWidth + barSpacing)
                                        val startX = (size.width - totalWidth).coerceAtLeast(0f) / 2f

                                        waveformBars.forEachIndexed { index, amp ->
                                            val barHeight = ((amp / 100f) * size.height).coerceAtLeast(4.dp.toPx())
                                            val x = startX + index * (barWidth + barSpacing)
                                            val y = (size.height - barHeight) / 2f
                                            val barProgressFraction = index.toFloat() / waveformBars.size.toFloat()
                                            val barColor = if (barProgressFraction <= progress) {
                                                if (isThisAudioActive) Color(0xFF10B981) else Color(0xFF38BDF8)
                                            } else {
                                                Color.White.copy(alpha = 0.3f)
                                            }
                                            drawRoundRect(
                                                color = barColor,
                                                topLeft = Offset(x, y),
                                                size = Size(barWidth, barHeight),
                                                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = message.message.ifBlank { "تسجيل صوتي 🎤" },
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    MediaType.LOCATION -> {
                        val context = LocalContext.current
                        Surface(
                            color = Color.Black.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clickable {
                                    try {
                                        val uri = if (message.mediaUrl.startsWith("geo:") || message.mediaUrl.startsWith("http")) {
                                            Uri.parse(message.mediaUrl)
                                        } else {
                                            Uri.parse("https://maps.google.com/?q=${message.mediaUrl}")
                                        }
                                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "تعذر فتح تطبيق الخرائط", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                                        )
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "موقع جغرافي",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(34.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "موقع جغرافي مباشر 📍",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "اضغط لعرض الموقع وتتبع المسار",
                                            color = Color(0xFF93C5FD),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🗺️ فتح الموقع بالخريطة",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF60A5FA)
                                    )
                                    Text(
                                        text = "Google Maps ↗",
                                        fontSize = 10.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                    MediaType.VIDEO -> {
                        if (message.mediaUrl.isNotBlank()) {
                            val context = LocalContext.current
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(Uri.parse(message.mediaUrl), "video/*")
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "تعذر تشغيل الفيديو: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "تشغيل الفيديو",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color(0xFF2563EB).copy(alpha = 0.9f), CircleShape)
                                            .padding(8.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "اضغط لتشغيل الفيديو 🎥",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                    MediaType.FILE -> {
                        Text("📎 ${message.message.ifBlank { "ملف مرفق" }}", color = Color(0xFF90CAF9), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    else -> {}
                }

                // Message text
                if (message.message.isNotBlank() && message.mediaType != MediaType.FILE && message.mediaType != MediaType.LOCATION && message.mediaType != MediaType.AUDIO) {
                    Text(
                        text = message.message,
                        color = textPrimaryColor,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time & Status Indicators
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (message.isEdited) {
                        Text(
                            text = "معدلة",
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Light
                        )
                    }

                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    if (isMe) {
                        val isRead = message.status == MessageStatus.READ
                        when {
                            message.status == MessageStatus.FAILED -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onRetryClick?.invoke(message.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "إعادة المحاولة",
                                        tint = Color(0xFFFF8A80),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "إعادة الإرسال",
                                        fontSize = 9.sp,
                                        color = Color(0xFFFF8A80),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            message.status == MessageStatus.PENDING || message.status == MessageStatus.SENDING -> {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "قيد الإرسال",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            isRead -> {
                                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "تمت القراءة",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "تمت القراءة",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            message.status == MessageStatus.DELIVERED -> {
                                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "تم الاستلام",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "تم الاستلام",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "تم الإرسال",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Circular Reactions Floating Bar
            AnimatedVisibility(
                visible = showQuickReactionOverlay,
                enter = fadeIn() + scaleIn(spring(dampingRatio = 0.6f)),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    color = Color(0xFF0F172A).copy(alpha = 0.95f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .offset(y = (-4).dp)
                        .padding(horizontal = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("❤️", "👍", "🔧", "✅", "⭐️", "👏").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onReactionClick?.invoke(emoji)
                                        showQuickReactionOverlay = false
                                    }
                                    .padding(4.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                showQuickReactionOverlay = false
                                onLongClick()
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Reactions Pill (if any reacted)
            if (message.reactions.isNotEmpty()) {
                val reactionCounts = remember(message.reactions) {
                    message.reactions.values.groupingBy { it }.eachCount()
                }
                Row(
                    modifier = Modifier
                        .offset(y = (-6).dp)
                        .padding(horizontal = 6.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    reactionCounts.forEach { (emoji, count) ->
                        Text(
                            text = if (count > 1) "$emoji $count" else emoji,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { onReactionClick?.invoke(emoji) }
                        )
                    }
                }
            }
        }
    }
}

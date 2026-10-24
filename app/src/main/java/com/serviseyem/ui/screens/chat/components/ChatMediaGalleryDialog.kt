package com.serviseyem.ui.screens.chat.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.serviseyem.data.models.ChatMessage
import com.serviseyem.data.models.MediaType
import com.serviseyem.utils.AudioPlayerManager
import com.serviseyem.utils.ChatIcons
import com.serviseyem.utils.DateFormatter
import com.serviseyem.utils.VisualThemePalette

@Composable
fun ChatMediaGalleryDialog(
    messages: List<ChatMessage>,
    onDismiss: () -> Unit,
    themeColors: VisualThemePalette
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val imageMessages = remember(messages) {
        messages.filter { it.mediaType == MediaType.IMAGE && it.mediaUrl.isNotBlank() }
    }
    val audioMessages = remember(messages) {
        messages.filter { it.mediaType == MediaType.AUDIO && it.mediaUrl.isNotBlank() }
    }
    val locationMessages = remember(messages) {
        messages.filter { it.mediaType == MediaType.LOCATION && it.mediaUrl.isNotBlank() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeColors.surface)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "معرض وسائط المحادثة 🖼️",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = themeColors.textPrimary)
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = themeColors.surface,
                    contentColor = themeColors.accent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("الصور (${imageMessages.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("الصوتيات (${audioMessages.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("المواقع (${locationMessages.size})", fontSize = 12.sp) }
                    )
                }

                // Tab Content
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        0 -> {
                            if (imageMessages.isEmpty()) {
                                EmptyMediaState(message = "لا توجد صور متبادلة في هذه المحادثة")
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    contentPadding = PaddingValues(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(imageMessages, key = { it.id }) { msg ->
                                        AsyncImage(
                                            model = msg.mediaUrl,
                                            contentDescription = "صورة المحادثة",
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { fullScreenImageUrl = msg.mediaUrl },
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (audioMessages.isEmpty()) {
                                EmptyMediaState(message = "لا توجد تسجيلات صوتية في هذه المحادثة")
                            } else {
                                val currentPlayingId by AudioPlayerManager.currentPlayingId.collectAsState()
                                val isAudioPlaying by AudioPlayerManager.isPlaying.collectAsState()

                                LazyColumn(
                                    contentPadding = PaddingValues(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(audioMessages, key = { it.id }) { msg ->
                                        val isThisActive = currentPlayingId == msg.id && isAudioPlaying
                                        Surface(
                                            color = themeColors.surface,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        if (isThisActive) {
                                                            AudioPlayerManager.pause()
                                                        } else if (currentPlayingId == msg.id) {
                                                            AudioPlayerManager.resume()
                                                        } else {
                                                            AudioPlayerManager.play(msg.id, msg.mediaUrl, context)
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(if (isThisActive) Color(0xFF10B981) else themeColors.primary, CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isThisActive) ChatIcons.Pause else Icons.Default.PlayArrow,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = msg.message.ifBlank { "تسجيل صوتي 🎤" },
                                                        color = themeColors.textPrimary,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "${msg.senderName} • ${DateFormatter.formatDisplay(msg.timestamp)}",
                                                        color = themeColors.textSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            if (locationMessages.isEmpty()) {
                                EmptyMediaState(message = "لا توجد مواقع جغرافية متبادلة في هذه المحادثة")
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(locationMessages, key = { it.id }) { msg ->
                                        Surface(
                                            color = themeColors.surface,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    try {
                                                        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(msg.mediaUrl))
                                                        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        context.startActivity(mapIntent)
                                                    } catch (_: Exception) {
                                                        Toast.makeText(context, "تعذر فتح الخريطة", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "موقع جغرافي 📍",
                                                        color = themeColors.textPrimary,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${msg.senderName} • ${DateFormatter.formatDisplay(msg.timestamp)}",
                                                        color = themeColors.textSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fullscreen image preview modal
            val activeFullUrl = fullScreenImageUrl
            if (activeFullUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                        .clickable { fullScreenImageUrl = null },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = activeFullUrl,
                        contentDescription = "صورة مكبرة",
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { fullScreenImageUrl = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMediaState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}

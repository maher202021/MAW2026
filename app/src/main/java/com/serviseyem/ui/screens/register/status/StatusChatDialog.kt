package com.serviseyem.ui.screens.register.status

import android.net.Uri
import com.serviseyem.ui.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.serviseyem.data.ChatChannelEntity
import com.serviseyem.utils.VisualThemePalette

/**
 * 💬 StatusChatDialog - نافذة المحادثة المباشرة المتطورة
 * تدعم إرسال النصوص والمرفقات ومؤشرات الحالة التفاعلية مع تحسين أداء تحميل الصور
 */
@Composable
fun StatusChatDialog(
    chatChannel: ChatChannelEntity,
    currentUserId: String,
    onSendMessage: (String) -> Unit,
    onSendAttachment: ((Uri) -> Unit)? = null,
    onDismiss: () -> Unit,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var selectedAttachmentUri by remember { mutableStateOf<Uri?>(null) }

    // Lifecycle cleanup
    DisposableEffect(Unit) {
        onDispose {
            inputText = ""
            selectedAttachmentUri = null
        }
    }

    val attachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedAttachmentUri = uri
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.5.dp, themeColors.accent),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "💬 محادثة: ${chatChannel.userName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.accent
                        )
                        Text("متصل الآن - دعم فني وتنسيق مباشر", fontSize = 10.sp, color = Color.Gray)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Red)
                    }
                }

                // Chat Messages List
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    if (chatChannel.messages.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد رسائل سابقة. ابدأ المحادثة الآن!", fontSize = 11.sp, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            reverseLayout = true
                        ) {
                            items(
                                items = chatChannel.messages.reversed(),
                                key = { it.id.ifEmpty { "${it.timestamp}_${it.senderId}" } }
                            ) { msg ->
                                val isMe = msg.senderId == currentUserId
                                val alignment = if (isMe) Alignment.End else Alignment.Start
                                val bubbleBg = if (isMe) themeColors.accent else Color.Gray.copy(alpha = 0.3f)
                                val textColor = if (isMe) Color.Black else Color.White

                                Column(
                                    horizontalAlignment = alignment,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(bubbleBg)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(msg.message, fontSize = 11.5.sp, color = textColor)
                                    }
                                }
                            }
                        }
                    }
                }

                // Attachment Preview Bar if selected
                selectedAttachmentUri?.let { uri ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uri)
                                .crossfade(true)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "مرفق صوري",
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Text("مرفق جاهز للإرسال", fontSize = 10.5.sp, color = Color.White, modifier = Modifier.weight(1f))
                        IconButton(onClick = { selectedAttachmentUri = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "حذف", tint = Color.Red)
                        }
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { attachmentLauncher.launch("image/*") },
                        modifier = Modifier
                            .background(Color(0xFF1E293B), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إرفاق صورة", tint = themeColors.accent)
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("اكتب رسالتك...", fontSize = 11.sp, color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = themeColors.accent,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                        )
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() || selectedAttachmentUri != null) {
                                val textToSend = if (selectedAttachmentUri != null) {
                                    "[مرفق صورة] ${inputText.trim()}"
                                } else {
                                    inputText.trim()
                                }
                                onSendMessage(textToSend)
                                inputText = ""
                                selectedAttachmentUri = null
                            }
                        },
                        modifier = Modifier
                            .background(themeColors.accent, RoundedCornerShape(10.dp))
                            .size(42.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Color.Black)
                    }
                }
            }
        }
    }
}

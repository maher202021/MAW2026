package com.serviseyem.ui.screens.assistant.components

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.VoiceManager
import com.serviseyem.data.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.ProviderCard
import com.serviseyem.ui.screens.assistant.AssistantMessage
import com.serviseyem.ui.screens.entities.PropertyCard
import com.serviseyem.ui.screens.entities.StoreItemCard
import com.serviseyem.utils.VisualThemePalette

/**
 * 💬 AssistantMessageItem
 * Message card rendering user and assistant text, action buttons, TTS button, and suggested provider cards.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AssistantMessageItem(
    msg: AssistantMessage,
    themeColors: VisualThemePalette,
    viewModel: MainViewModel,
    onRequestQuickService: () -> Unit,
    onNavigateToMap: () -> Unit,
    onChatOpen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var reactionState by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Boolean?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (msg.isUser) themeColors.primary else themeColors.surface
                ),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (msg.isUser) 12.dp else 2.dp,
                    bottomEnd = if (msg.isUser) 2.dp else 12.dp
                ),
                border = BorderStroke(
                    1.dp,
                    if (msg.isUser) themeColors.primary else themeColors.border
                ),
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = msg.text,
                        fontSize = 12.sp,
                        color = themeColors.textPrimary,
                        lineHeight = 18.sp
                    )

                    if (!msg.isUser) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = themeColors.border)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Upgraded Interactive Buttons Row under every reply
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 1. Request Now
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                    .border(0.8.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .clickable { onRequestQuickService() }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("⚡ اطلب الآن", fontSize = 8.5.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }

                            // 2. Show on Map
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                    .border(0.8.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .clickable { onNavigateToMap() }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("📍 الخريطة", fontSize = 8.5.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }

                            // 3. Direct Chat (توجيه المحادثة للكيان المطابق أو الدعم الفني كملجأ أخير)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                                    .border(0.8.dp, Color(0xFF3B82F6).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        val firstEntity = msg.matchedEntities.orEmpty()
                                            .filterNotNull()
                                            .firstOrNull()
                                        if (firstEntity != null) {
                                            when (firstEntity) {
                                                is ProviderEntity -> viewModel.openDirectChat(
                                                    targetUserId = firstEntity.id,
                                                    targetUserName = firstEntity.name,
                                                    relatedEntityId = firstEntity.id,
                                                    relatedEntityType = "PROVIDER"
                                                )
                                                is StoreEntity -> viewModel.openDirectChat(
                                                    targetUserId = firstEntity.id,
                                                    targetUserName = firstEntity.name,
                                                    relatedEntityId = firstEntity.id,
                                                    relatedEntityType = "STORE"
                                                )
                                                is PropertyEntity -> viewModel.openDirectChat(
                                                    targetUserId = firstEntity.id,
                                                    targetUserName = firstEntity.title,
                                                    relatedEntityId = firstEntity.id,
                                                    relatedEntityType = "PROPERTY"
                                                )
                                                else -> onChatOpen("support_channel_id")
                                            }
                                        } else {
                                            onChatOpen("support_channel_id")
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("💬 دردش", fontSize = 8.5.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                            }

                            // 4. Copy Reply Text
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                                    .border(0.8.dp, Color(0xFF8B5CF6).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        try {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(msg.text))
                                        } catch (_: Exception) {}
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("📋 نسخ", fontSize = 8.5.sp, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                            }

                            // 5. Thumbs Up
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (reactionState == true) Color(0xFF10B981).copy(alpha = 0.35f) else Color(0xFF334155).copy(alpha = 0.15f))
                                    .clickable { reactionState = if (reactionState == true) null else true }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("👍", fontSize = 9.sp)
                            }

                            // 6. Thumbs Down
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (reactionState == false) Color(0xFFEF4444).copy(alpha = 0.35f) else Color(0xFF334155).copy(alpha = 0.15f))
                                    .clickable { reactionState = if (reactionState == false) null else false }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("👎", fontSize = 9.sp)
                            }

                            // 7. TTS Voice Audio Play Button
                            IconButton(
                                onClick = {
                                    try {
                                        VoiceManager.onSpeak?.invoke(msg.text)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "استماع",
                                    tint = themeColors.accent,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        val safeEntities = msg.matchedEntities.orEmpty().filterNotNull()
        if (safeEntities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "👇 عثرت لك على النتائج التالية لطلبك:",
                fontSize = 11.sp,
                color = themeColors.accent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                safeEntities.forEach { entity ->
                    when (entity) {
                        is ProviderEntity -> {
                            ProviderCard(
                                provider = entity,
                                themeColors = themeColors,
                                viewModel = viewModel,
                                onChatOpen = onChatOpen
                            )
                        }
                        is StoreEntity -> {
                            StoreItemCard(
                                store = entity,
                                themeColors = themeColors,
                                onClick = {
                                    viewModel.selectedStore = entity
                                    viewModel.navigateToScreen(AppScreens.STORE_DETAILS)
                                },
                                onChatClick = { onChatOpen(entity.id) },
                                onRequestServiceClick = {
                                    viewModel.selectedStore = entity
                                    viewModel.navigateToScreen(AppScreens.QUICK_SERVICE_REQUEST)
                                }
                            )
                        }
                        is PropertyEntity -> {
                            PropertyCard(
                                property = entity,
                                themeColors = themeColors,
                                onClick = {
                                    viewModel.selectedProperty = entity
                                    viewModel.navigateToScreen(AppScreens.PROPERTY_DETAILS)
                                },
                                onChatClick = { onChatOpen(entity.id) },
                                onRequestInspectionClick = {
                                    viewModel.selectedProperty = entity
                                    viewModel.navigateToScreen(AppScreens.QUICK_SERVICE_REQUEST)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

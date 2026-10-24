package com.serviseyem.ui.screens.chat.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.models.UserPresence
import com.serviseyem.utils.DateFormatter
import com.serviseyem.utils.VisualThemePalette
import java.util.*

@Composable
fun ChatHeaderBar(
    name: String,
    photoUrl: String,
    presence: UserPresence?,
    isTyping: Boolean,
    relatedEntityId: String? = null,
    relatedEntityType: String? = null,
    relatedEntityStatus: String? = null,
    onBackClick: () -> Unit,
    onSearchToggle: () -> Unit,
    onMediaGalleryClick: () -> Unit = {},
    onBlockClick: () -> Unit,
    onDeleteChannelClick: () -> Unit,
    onContextClick: (() -> Unit)? = null,
    themeColors: VisualThemePalette? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    val entityAccentColor = when (relatedEntityType?.uppercase()) {
        "PROVIDER" -> Color(0xFF3B82F6) // Blue
        "STORE", "RESTAURANT" -> Color(0xFF10B981) // Emerald
        "MEDICAL" -> Color(0xFFEC4899) // Rose
        "PROPERTY" -> Color(0xFF8B5CF6) // Purple
        else -> themeColors?.accent ?: Color(0xFF64FFDA)
    }

    val surfaceColor = themeColors?.surface ?: Color(0xFF142030)
    val primaryColor = themeColors?.primary ?: Color(0xFF1E88E5)
    val accentColor = entityAccentColor
    val textPrimary = themeColors?.textPrimary ?: Color.White
    val textSecondary = themeColors?.textSecondary ?: Color.Gray
    val borderColor = themeColors?.border ?: Color.White.copy(alpha = 0.08f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceColor)
            .border(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = textPrimary)
            }

            Box(modifier = Modifier.size(42.dp)) {
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.take(1).ifBlank { "💬" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                val isOnline = presence?.isOnline == true
                val isAway = !isOnline && presence != null && (System.currentTimeMillis() - presence.lastSeen < 15 * 60 * 1000L)

                // Online/Away indicator dot
                if (isOnline || isAway) {
                    val dotColor = if (isOnline) Color(0xFF00C853) else Color(0xFFFFB300)
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(dotColor, CircleShape)
                            .border(2.dp, surfaceColor, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.ifBlank { "محادثة" },
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val isOnline = presence?.isOnline == true
                val isAway = !isOnline && presence != null && (System.currentTimeMillis() - presence.lastSeen < 15 * 60 * 1000L)

                val statusText = when {
                    isTyping -> "يكتب الآن..."
                    isOnline -> "متصل الآن"
                    isAway -> "نشط مؤخراً"
                    presence != null && presence.lastSeen > 0 -> {
                        // ✨ م2-ج3: استخدام DateFormatter
                        "آخر ظهور ${DateFormatter.formatTime(presence.lastSeen)}"
                    }
                    else -> "غير متصل"
                }

                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = if (isTyping || isOnline) accentColor else if (isAway) Color(0xFFFFB300) else textSecondary
                )
            }

            IconButton(onClick = onSearchToggle) {
                Icon(Icons.Default.Search, contentDescription = "بحث", tint = textPrimary)
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "خيارات", tint = textPrimary)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(surfaceColor)
                ) {
                    DropdownMenuItem(
                        text = { Text("معرض الوسائط 🖼️", color = textPrimary, fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            onMediaGalleryClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = accentColor) }
                    )
                    HorizontalDivider(color = borderColor)
                    DropdownMenuItem(
                        text = { Text("حظر المستخدم", color = Color(0xFFFF8A80), fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            onBlockClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFF8A80)) }
                    )
                    HorizontalDivider(color = borderColor)
                    DropdownMenuItem(
                        text = { Text("حذف المحادثة بالكامل", color = Color(0xFFE53935), fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                        onClick = {
                            showMenu = false
                            onDeleteChannelClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53935)) }
                    )
                }
            }
        }

        // Booking / Urgent Request / Entity Context Strip (Specialized Maintenance/Service Context)
        if (!relatedEntityId.isNullOrBlank() || !relatedEntityType.isNullOrBlank()) {
            val shortId = if (relatedEntityId != null && relatedEntityId.length > 8) {
                relatedEntityId.takeLast(8)
            } else {
                relatedEntityId ?: ""
            }

            val statusSuffix = if (!relatedEntityStatus.isNullOrBlank()) " · $relatedEntityStatus" else ""

            val (contextText, badgeColor) = when (relatedEntityType?.uppercase()) {
                "BOOKING" -> "🗓️ حجز موعد صيانة #$shortId$statusSuffix" to Color(0xFF3B82F6) // Elegant Blue
                "URGENT_REQUEST" -> "🚨 طلب صيانة عاجل #$shortId$statusSuffix" to Color(0xFFF59E0B) // Amber
                "SUPPORT" -> "🛠️ محادثة الدعم الفني #$shortId$statusSuffix" to Color(0xFF10B981) // Emerald
                "STORE", "RESTAURANT" -> "🏪 استفسار حول المتجر / المنتجات #$shortId$statusSuffix" to Color(0xFF8B5CF6) // Purple
                "MEDICAL" -> "🏥 استفسار طبي / موعد عيادة #$shortId$statusSuffix" to Color(0xFFEC4899) // Rose
                "PROPERTY" -> "🏠 استفسار حول العقار #$shortId$statusSuffix" to Color(0xFF06B6D4) // Cyan
                else -> if (!relatedEntityId.isNullOrBlank()) "🔧 طلب خدمة صيانة #$shortId$statusSuffix" to Color(0xFF64748B) else (null to Color.Transparent)
            }

            if (contextText != null) {
                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (onContextClick != null) Modifier.clickable { onContextClick() } else Modifier
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(badgeColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = contextText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (onContextClick != null) {
                            Text(
                                text = "تفاصيل الخدمة ↗",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }
                }
            }
        }
    }
}


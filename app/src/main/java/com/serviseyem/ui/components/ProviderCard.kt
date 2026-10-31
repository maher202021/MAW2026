package com.serviseyem.ui.components

import android.content.Intent
import com.serviseyem.ui.*
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.ProviderEntity
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.dialogs.BookingDialog
import com.serviseyem.ui.screens.register.GuestRegistrationDialog
import com.serviseyem.utils.getStarsString
import com.serviseyem.utils.VisualThemePalette

@Composable
fun ProviderCard(
    provider: ProviderEntity,
    themeColors: VisualThemePalette,
    viewModel: MainViewModel,
    onChatOpen: (String) -> Unit
) {
    val context = LocalContext.current
    val settingsState by viewModel.settings.collectAsState()
    val currentUserIdState by viewModel.currentUserId.collectAsState()

    var showDetailsDialog by remember { mutableStateOf(false) }
    var showReviewsListDialog by remember { mutableStateOf(false) }
    var showGuestRegisterDialogForBooking by remember { mutableStateOf(false) }
    var showBookingDialog by remember { mutableStateOf(false) }

    var isPressed by remember { mutableStateOf(false) }
    val scaleFactor by animateFloatAsState(
        targetValue = if (settingsState.enableScaleAnimation && isPressed) settingsState.clickScaleRatio else 1.0f,
        label = "click_scale"
    )

    val isVerified = provider.isVerified || provider.subscriptionStatus == "APPROVED" || provider.isAvailable
    val coverImg = provider.coverImage.ifBlank { "" }
    val avatarImg = provider.profileImage.ifBlank { "" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .scale(scaleFactor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { showDetailsDialog = true }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111C30)),
        border = BorderStroke(1.dp, Color(0xFF1E2E4E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Cover / Illustration Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(Color(0xFF0A1220))
            ) {
                if (coverImg.isNotBlank()) {
                    SmartAsyncImage(
                        model = coverImg,
                        contentDescription = provider.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0E1A2E), Color(0xFF09111E))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handyman,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8).copy(alpha = 0.5f),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Subtle dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF111C30).copy(alpha = 0.85f)),
                                startY = 30f
                            )
                        )
                )

                // Badges in Header (Rating on left, Verified on right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating Badge (⭐ 5.0 in black capsule with gold star)
                    Surface(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E2E4E)),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable { showReviewsListDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(String.format("%.1f", provider.rating), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                        }
                    }

                    // Verified Badge (موثق ✓ in dark capsule)
                    if (isVerified || provider.isVip) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF1E2E4E))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (provider.isVip) "VIP موثق" else "موثق ✓",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 2. Overlapping Avatar & Content Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                // Header with Overlapping Avatar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-20).dp)
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(2.dp, Color(0xFF2563EB), CircleShape)
                            .shadow(6.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarImg.isNotBlank()) {
                            SmartAsyncImage(
                                model = avatarImg,
                                contentDescription = provider.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = provider.name,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        val profText = if (provider.profession.isBlank()) "صيانة فنية وخدمات متكاملة" else provider.profession
                        Text(
                            text = profText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF38BDF8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Location & Availability Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-10).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        val locText = if (provider.localNeighborhood.isNotBlank()) "${provider.area} - ${provider.localNeighborhood}" else provider.area.ifBlank { "صنعاء - الجمهورية اليمنية" }
                        Text(
                            text = locText,
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF064E3B).copy(alpha = 0.5f),
                        border = BorderStroke(0.5.dp, Color(0xFF10B981))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (provider.isAvailable) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                            )
                            Text(
                                text = if (provider.isAvailable) "متاح الآن" else "مشغول",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (provider.isAvailable) Color(0xFF34D399) else Color(0xFFF87171)
                            )
                        }
                    }
                }

                // 3. Action Buttons Row: [★] [حجز 📅] [التفاصيل 📋] [محادثة 💬] (Screenshot 1 Layout)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-4).dp)
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // زر المفضلة (Star)
                    Surface(
                        onClick = { viewModel.toggleFavorite(provider.id) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E2E4E),
                        border = BorderStroke(1.dp, Color(0xFF283B63)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Star, contentDescription = "المفضلة", tint = Color(0xFFF59E0B), modifier = Modifier.size(17.dp))
                        }
                    }

                    // زر حجز الموعد (Green 📅)
                    Button(
                        onClick = {
                            if (currentUserIdState.isEmpty()) {
                                showGuestRegisterDialogForBooking = true
                            } else {
                                showBookingDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("📅", fontSize = 11.sp)
                            Text("حجز", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }

                    // زر التفاصيل الكاملة (Silver/Light 📋)
                    Button(
                        onClick = { showDetailsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("📋", fontSize = 11.sp)
                            Text("التفاصيل", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }

                    // زر المحادثة الفورية (Blue 💬)
                    Button(
                        onClick = { onChatOpen(provider.id.ifBlank { provider.phone }) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("💬", fontSize = 11.sp)
                            Text("محادثة", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }
    }

    if (showDetailsDialog) {
        ProviderDetailsDialog(
            provider = provider,
            themeColors = themeColors,
            viewModel = viewModel,
            onDismiss = { showDetailsDialog = false },
            onChatOpen = onChatOpen,
            onBookClick = {
                if (currentUserIdState.isEmpty()) {
                    showGuestRegisterDialogForBooking = true
                } else {
                    showBookingDialog = true
                }
            }
        )
    }

    if (showReviewsListDialog) {
        ProviderReviewsListDialog(
            provider = provider,
            themeColors = themeColors,
            viewModel = viewModel,
            onDismiss = { showReviewsListDialog = false }
        )
    }

    if (showGuestRegisterDialogForBooking) {
        GuestRegistrationDialog(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { showGuestRegisterDialogForBooking = false },
            onRegisterCompleted = { _, _, _, _ ->
                showGuestRegisterDialogForBooking = false
                showBookingDialog = true
            }
        )
    }

    if (showBookingDialog) {
        BookingDialog(
            provider = provider,
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { showBookingDialog = false }
        )
    }
}

package com.serviseyem.ui.screens.entities

import android.content.Context
import com.serviseyem.ui.*
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import androidx.compose.runtime.*
import androidx.compose.material3.*

@Composable
fun ProfileActions(
    entityId: String,
    entityName: String,
    entityPhone: String,
    entityType: ProfileEntityType,
    isOwner: Boolean,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onOpenChat: (String) -> Unit,
    onRequestBooking: () -> Unit,
    onOrderProduct: () -> Unit,
    onEditProfile: () -> Unit,
    onEditProducts: () -> Unit,
    onEditGallery: () -> Unit
) {
    val context = LocalContext.current
    val currentUserId by viewModel.currentUserId.collectAsState()
    var showGuestDialog by remember { mutableStateOf(false) }

    if (showGuestDialog) {
        com.serviseyem.ui.screens.register.GuestRegistrationDialog(
            viewModel = viewModel,
            themeColors = themeColors,
            onDismiss = { showGuestDialog = false },
            onRegisterCompleted = { _, _, _, _ -> showGuestDialog = false }
        )
    }

    val handleOpenChat = {
        if (currentUserId.isBlank() || currentUserId == "guest") {
            showGuestDialog = true
        } else {
            viewModel.openDirectChat(
                targetUserId = entityId.ifBlank { entityPhone },
                targetUserName = entityName,
                targetUserPhoto = "",
                relatedEntityId = entityId,
                relatedEntityType = entityType.name
            )
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOwner) {
                // Owner management actions
                Button(
                    onClick = onEditProfile,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعديل الحساب 📝", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                if (entityType == ProfileEntityType.STORE || entityType == ProfileEntityType.RESTAURANT) {
                    Button(
                        onClick = onEditProducts,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إدارة المنتجات 📦", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else if (entityType == ProfileEntityType.TECHNICIAN || entityType == ProfileEntityType.REAL_ESTATE) {
                    Button(
                        onClick = onEditGallery,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("معرض الأعمال 📸", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                // Regular customer actions
                // Dynamic Primary Action
                when (entityType) {
                    ProfileEntityType.STORE, ProfileEntityType.RESTAURANT -> {
                        // Floating action pill replacement for STORE & RESTAURANT
                        Row(
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = onOrderProduct,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (entityType == ProfileEntityType.STORE) Color(0xFF10B981) else Color(0xFFF59E0B)
                                ),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1.35f).fillMaxHeight()
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (entityType == ProfileEntityType.STORE) "طلب بضاعة" else "حجز موعد / طلب",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Button(
                                onClick = handleOpenChat,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) {
                                Icon(Icons.Default.MailOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("محادثة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, softWrap = false)
                            }
                            if (entityPhone.isNotBlank()) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$entityPhone"))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(0.95f).fillMaxHeight()
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("اتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                    ProfileEntityType.TECHNICIAN -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = onRequestBooking,
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1.35f).fillMaxHeight()
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("طلب حجز صيانة 🔧", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1, softWrap = false)
                            }
                            Button(
                                onClick = handleOpenChat,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) {
                                Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("محادثة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, softWrap = false)
                            }
                            if (entityPhone.isNotBlank()) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$entityPhone"))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(0.95f).fillMaxHeight()
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("اتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                    ProfileEntityType.MEDICAL -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = onRequestBooking,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1.35f).fillMaxHeight()
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حجز موعد طبي 🩺", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, softWrap = false)
                            }
                            Button(
                                onClick = handleOpenChat,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) {
                                Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("محادثة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, softWrap = false)
                            }
                            if (entityPhone.isNotBlank()) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$entityPhone"))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(0.95f).fillMaxHeight()
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("اتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                    ProfileEntityType.REAL_ESTATE -> {
                        Button(
                            onClick = handleOpenChat,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("محادثة 💬", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(
                            onClick = onRequestBooking,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f).height(44.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("طلب معاينة العقار 🏡", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    ProfileEntityType.JOB -> {
                        Button(
                            onClick = {
                                if (entityPhone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/967$entityPhone?text=${Uri.encode("مرحباً، أود التقديم على فرصة العمل: $entityName")}"))
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f).height(44.dp)
                        ) {
                            Icon(Icons.Default.AccountBox, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تقديم السيرة الذاتية 📄", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                    ProfileEntityType.GENERAL -> {
                        Button(
                            onClick = handleOpenChat,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("محادثة 💬", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(
                            onClick = onRequestBooking,
                            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f).height(44.dp)
                        ) {
                            Text("طلب الخدمة 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

package com.serviseyem.ui.screens.admin.components

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

/**
 * 🏷️ Reusable Admin Status Badge
 */
@Composable
fun AdminStatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        color = containerColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, containerColor.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = containerColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/**
 * 🎛️ Reusable Admin Switch Row for Settings & Toggles
 */
@Composable
fun AdminSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector? = null,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(themeColors.accent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = themeColors.accent, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 10.5.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = themeColors.accent,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

/**
 * 🔘 Reusable Admin Action Buttons
 */
@Composable
fun AdminActionButtons(
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onApprove: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onToggleBlock: (() -> Unit)? = null,
    isBlocked: Boolean = false,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onApprove != null) {
            Button(
                onClick = onApprove,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Text("موافقة ✓", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        if (onReject != null) {
            Button(
                onClick = onReject,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Text("رفض ❌", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        if (onToggleBlock != null) {
            OutlinedButton(
                onClick = onToggleBlock,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isBlocked) Color(0xFF10B981) else Color(0xFFF59E0B)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isBlocked) "إلغاء الحظر" else "حظر 🚫",
                    fontSize = 10.5.sp,
                    color = if (isBlocked) Color(0xFF10B981) else Color(0xFFF59E0B)
                )
            }
        }
        if (onEdit != null) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = themeColors.accent, modifier = Modifier.size(16.dp))
            }
        }
        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFEF5350).copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * 🃏 Universal Admin Entity Card
 */
@Composable
fun AdminEntityCard(
    title: String,
    subtitle: String,
    details: String? = null,
    imageUrl: String? = null,
    statusText: String? = null,
    statusColor: Color = Color(0xFF10B981),
    isVip: Boolean = false,
    isBlocked: Boolean = false,
    themeColors: VisualThemePalette,
    actions: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (isBlocked) Color(0xFFEF5350).copy(alpha = 0.5f)
            else if (isVip) Color(0xFFF59E0B).copy(alpha = 0.5f)
            else Color.White.copy(alpha = 0.08f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isVip) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⭐ VIP", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = themeColors.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!statusText.isNullOrBlank()) {
                    AdminStatusBadge(text = statusText, containerColor = statusColor)
                }
            }

            if (!details.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = details,
                    fontSize = 10.5.sp,
                    color = Color.LightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            if (actions != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(8.dp))
                actions()
            }
        }
    }
}

/**
 * 🏷️ Reusable Admin Filter Chips Bar
 */
@Composable
fun AdminFilterChips(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) themeColors.accent else Color(0xFF1E293B))
                    .border(
                        1.dp,
                        if (isSelected) themeColors.accent else Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectCategory(category) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = category,
                    fontSize = 11.sp,
                    color = if (isSelected) Color.Black else Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// =========================================================================================
// 1. 🚧 شاشة وضع الصيانة والطوارئ (Maintenance Mode)
// =========================================================================================

/**
 * 🃏 بطاقة مخصصة لإدارة كيانات الأقسام (صور، أغلفة، أسعار، توصية، حذف)
 */
@Composable
fun AdminSectionEntityCard(
    title: String,
    phone: String,
    location: String,
    logoUrl: String,
    coverUrl: String,
    priceOrDetails: String,
    isRecommended: Boolean,
    onToggleRecommend: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isRecommended) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // صورة الغلاف والصورة الشخصية
            Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(8.dp))) {
                if (coverUrl.isNotBlank()) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E293B)))
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(location, fontSize = 9.5.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }

                if (isRecommended) {
                    Surface(
                        color = Color(0xFFF59E0B),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("⭐ موصى به", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (logoUrl.isNotBlank()) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                        )
                    } else {
                        Box(modifier = Modifier.size(36.dp).background(themeColors.accent.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(title.take(1), fontWeight = FontWeight.Bold, color = themeColors.accent)
                        }
                    }
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White, maxLines = 1)
                        Text(phone, fontSize = 10.5.sp, color = themeColors.accent, maxLines = 1)
                    }
                }

                // زر التوصية
                Surface(
                    color = if (isRecommended) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isRecommended) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.clickable { onToggleRecommend() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = if (isRecommended) Color(0xFFF59E0B) else Color.Gray, modifier = Modifier.size(14.dp))
                        Text(if (isRecommended) "موصى به" else "توصية", fontSize = 10.sp, color = if (isRecommended) Color(0xFFF59E0B) else Color.LightGray, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(priceOrDetails, fontSize = 10.5.sp, color = Color.LightGray, maxLines = 1)

            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, themeColors.accent),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = themeColors.accent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعديل الصور والأسعار ✏️", fontSize = 10.5.sp, color = themeColors.accent)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).background(Color(0xFFEF5350).copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}


// Backward compatibility re-exports
@Composable
fun AdminMaintenanceScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = com.serviseyem.ui.screens.admin.AdminMaintenanceScreenContent(viewModel, themeColors, modifier)

@Composable
fun AdminApiKeysScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = com.serviseyem.ui.screens.admin.AdminApiKeysScreenContent(viewModel, themeColors, modifier)

@Composable
fun AdminAutoRoutingScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = com.serviseyem.ui.screens.admin.AdminAutoRoutingScreenContent(viewModel, themeColors, modifier)

@Composable
fun AdminSystemSettingsScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = com.serviseyem.ui.screens.admin.AdminSystemSettingsScreenContent(viewModel, themeColors, modifier)

@Composable
fun AdminStorageAndCacheScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = com.serviseyem.ui.screens.admin.AdminStorageAndCacheScreenContent(viewModel, themeColors, modifier)

@Composable
fun AdminDataManagementScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = com.serviseyem.ui.screens.admin.AdminDataManagementScreenContent(viewModel, themeColors, modifier)

package com.serviseyem.ui

import com.serviseyem.utils.*
import com.serviseyem.ui.*

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 🎨 Unified UI State Components System (Material 3 Compliant)
 * Solves Problem 7: Provides standardized Empty States, Loading States, Error States with Retry,
 * Network Status Indicators, and Page Transitions with full Arabic RTL support.
 */

// ==========================================
// 1. 📡 Network Status Banner Indicator
// ==========================================
@Composable
fun NetworkStatusTopBar(
    isOnline: Boolean,
    themeColors: VisualThemePalette
) {
    AnimatedVisibility(
        visible = !isOnline,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            color = Color(0xFFD97706), // M3 Warm Amber Alert
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Offline Mode",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📡 أنت تعمل دون اتصال - تم تحميل النسخة المحلية المخبأة | سيتم المزامنة عند توفر الشبكة",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ==========================================
// 2. 📭 Standardized M3 Empty State
// ==========================================
@Composable
fun UnifiedEmptyStateComposable(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Info,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    themeColors: VisualThemePalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(themeColors.accent.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themeColors.accent,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            fontSize = 12.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        if (actionButtonText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = actionButtonText,
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// 3. ⏳ Standardized M3 Loading State
// ==========================================
@Composable
fun UnifiedLoadingStateComposable(
    message: String = "جاري جلب وتحديث البيانات السحابية...",
    themeColors: VisualThemePalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = themeColors.accent,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = message,
            fontSize = 12.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// 4. ⚠️ Standardized M3 Error State with Retry
// ==========================================
@Composable
fun UnifiedErrorStateComposable(
    errorMessage: String,
    onRetry: () -> Unit,
    themeColors: VisualThemePalette
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B18)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "حدث خطأ غير متوقع في جلب البيانات",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = errorMessage,
                fontSize = 11.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إعادة المحاولة 🔄", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 6. 💀 Skeleton Shimmer / Placeholder Loading Screens (Services, Restaurants, Jobs)
// ==========================================
@Composable
fun SkeletonPlaceholderCard(
    height: androidx.compose.ui.unit.Dp = 110.dp,
    themeColors: VisualThemePalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B18).copy(alpha = 0.8f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder Image Box
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text placeholders
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                )
                // Subtitle
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(10.dp)
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                )
                // Bottom badge placeholder
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 18.dp)
                        .background(themeColors.accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@Composable
fun UnifiedSkeletonListScreen(
    itemsCount: Int = 4,
    themeColors: VisualThemePalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        repeat(itemsCount) {
            SkeletonPlaceholderCard(themeColors = themeColors)
        }
    }
}


package com.serviseyem.ui.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.utils.VisualThemePalette
import kotlin.math.roundToInt

/**
 * 🌟 High-Craft Floating Action Buttons (FABs)
 * 1. "اطلب خدمتك الآن": Compact, vivid red gradient, instant 3-step wizard.
 * 2. "المساعد الذكي": Draggable Floating Action Button (FAB قابلة للسحب), royal indigo-blue gradient.
 */
@Composable
fun BoxScope.FloatingIconsOverlay(
    settings: AdminSettingsEntity,
    themeColors: VisualThemePalette,
    isClientUser: Boolean = true,
    onAssistantClick: () -> Unit,
    onRequestServiceClick: () -> Unit
) {
    // 1. Primary Action FAB: "اطلب خدمتك الآن" (ثابتة في مكانها حسب إعدادات الأدمن)
    if (!settings.footerMessage.contains("hide_urgent_fab")) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 4.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFE11D48), Color(0xFFBE123C))
                    )
                )
                .clickable { onRequestServiceClick() }
                .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "اطلب خدمتك الآن",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "اطلب خدمتك الآن ⚡",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.sp,
                    softWrap = false
                )
            }
        }
    }

    // 2. Smart Assistant FAB: "المساعد الذكي" (ثابتة في مكانها حسب إعدادات الأدمن)
    if (!settings.assistantHidden) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 4.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                    )
                )
                .clickable { onAssistantClick() }
                .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("🤖", fontSize = 13.sp)
                Text(
                    text = "المساعد الذكي 🤖",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.sp,
                    softWrap = false
                )
            }
        }
    }
}

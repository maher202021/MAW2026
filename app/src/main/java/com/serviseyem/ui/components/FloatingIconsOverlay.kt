package com.serviseyem.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.ui.theme.*
import com.serviseyem.utils.VisualThemePalette

/**
 * 🌟 High-Craft Floating Action Buttons (FABs)
 * - Perfectly circular, glossy, and eye-catching.
 * - "المساعد الذكي" on the left side (BottomStart) in rich teal-blue, name underneath.
 * - "اطلب خدمتك الآن" on the right side (BottomEnd) in strong emerald, name underneath.
 * - Includes smooth scale animation and haptic feedback on click.
 */
@Composable
fun BoxScope.FloatingIconsOverlay(
    settings: AdminSettingsEntity,
    themeColors: VisualThemePalette,
    isClientUser: Boolean = true,
    onAssistantClick: () -> Unit,
    onRequestServiceClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "floating_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Stacked vertically on the left side (BottomStart) above bottom bar with 16dp margin
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. «المساعد الذكي» - 48dp circular button
        if (!settings.assistantHidden) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(48.dp)
                    .shadow(12.dp, CircleShape, ambientColor = Color(0xFF0EA5E9), spotColor = Color(0xFF0284C7))
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF5ED3FF), Color(0xFF0E90FF))
                        )
                    )
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAssistantClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🤖", fontSize = 18.sp)
                    Text(
                        text = "المساعد",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF001B3A),
                        lineHeight = 10.sp
                    )
                }
            }
        }

        // 2. «اطلب خدمتك الآن» - 48dp circular button
        if (!settings.footerMessage.contains("hide_urgent_fab")) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(48.dp)
                    .shadow(12.dp, CircleShape, ambientColor = Color(0xFFFF2D6F), spotColor = Color(0xFFFF7EB3))
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFF7EB3), Color(0xFFFF2D6F))
                        )
                    )
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRequestServiceClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("⚡", fontSize = 18.sp)
                    Text(
                        text = "اطلب الآن",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 9.5.sp
                    )
                }
            }
        }
    }
}

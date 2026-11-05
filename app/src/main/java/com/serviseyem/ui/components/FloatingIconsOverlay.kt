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
 * - "المساعد الذكي" on one side (BottomStart) in rich teal-blue, name underneath.
 * - "اطلب خدمتك الآن" on the opposite side (BottomEnd) in vivid coral-emerald, name underneath.
 * - Includes smooth subtle pulse animation and haptic feedback on click.
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
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // 1. «المساعد الذكي» - BottomStart (Circular Elegant Button)
    if (!settings.assistantHidden) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 14.dp)
                .scale(pulseScale)
                .size(68.dp)
                .shadow(14.dp, CircleShape, ambientColor = Color(0xFFE8B23A), spotColor = Color(0xFF7C3AED))
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF7C3AED), Color(0xFF4F46E5), Color(0xFFE8B23A))
                    )
                )
                .border(BorderStroke(2.dp, Color(0xFFFDE047)), CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAssistantClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Text("🤖", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "المساعد",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }

    // 2. «اطلب خدمتك الآن» - BottomEnd (Circular Elegant Button)
    if (!settings.footerMessage.contains("hide_urgent_fab")) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 14.dp)
                .scale(pulseScale)
                .size(68.dp)
                .shadow(14.dp, CircleShape, ambientColor = Color(0xFF10B981), spotColor = Color(0xFFE11D48))
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF10B981), Color(0xFF0D9488), Color(0xFFE11D48))
                    )
                )
                .border(BorderStroke(2.dp, Color(0xFF6EE7B7)), CircleShape)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRequestServiceClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Text("⚡", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "اطلب خدمتك",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

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
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // 1. «المساعد الذكي» - on the Start side (BottomStart)
    if (!settings.assistantHidden) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .size(48.dp)
                    .shadow(10.dp, CircleShape, ambientColor = Color(0xFF0284C7), spotColor = Color(0xFF0E7490))
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF0E7490))
                        )
                    )
                    .border(BorderStroke(1.2.dp, Color(0xFF7DD3FC)), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAssistantClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 20.sp)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = com.serviseyem.ui.theme.CardSurface,
                border = BorderStroke(0.8.dp, com.serviseyem.ui.theme.CardBorder),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "المساعد الذكي",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = com.serviseyem.ui.theme.TextLightMint,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }

    // 2. «اطلب خدمتك الآن» - on the Opposite side (BottomEnd)
    if (!settings.footerMessage.contains("hide_urgent_fab")) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .size(48.dp)
                    .shadow(10.dp, CircleShape, ambientColor = Color(0xFFE11D48), spotColor = Color(0xFF0D5C3A))
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFE11D48), Color(0xFF0D5C3A))
                        )
                    )
                    .border(BorderStroke(1.2.dp, Color(0xFFFDA4AF)), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRequestServiceClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("⚡", fontSize = 20.sp)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = com.serviseyem.ui.theme.CardSurface,
                border = BorderStroke(0.8.dp, com.serviseyem.ui.theme.CardBorder),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "اطلب خدمتك",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = com.serviseyem.ui.theme.TextLightMint,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

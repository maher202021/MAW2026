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
import androidx.compose.ui.platform.testTag
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
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isCompactScreen = configuration.screenWidthDp < 360

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
        val onAssistantTap = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onAssistantClick()
        }
        if (isCompactScreen) {
            androidx.compose.material3.FloatingActionButton(
                onClick = onAssistantTap,
                shape = CircleShape,
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, bottom = 14.dp)
                    .scale(pulseScale)
                    .size(50.dp)
                    .testTag("fab_smart_assistant")
            ) {
                Text("🤖", fontSize = 20.sp)
            }
        } else {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = onAssistantTap,
                shape = RoundedCornerShape(24.dp),
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 14.dp)
                    .scale(pulseScale)
                    .heightIn(min = 48.dp)
                    .testTag("fab_smart_assistant"),
                icon = { Text("🤖", fontSize = 18.sp) },
                text = {
                    Text(
                        text = "المساعد الذكي",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            )
        }
    }

    // 2. «اطلب خدمتك» - on the Opposite side (BottomEnd)
    if (!settings.footerMessage.contains("hide_urgent_fab")) {
        val onUrgentTap = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onRequestServiceClick()
        }
        if (isCompactScreen) {
            androidx.compose.material3.FloatingActionButton(
                onClick = onUrgentTap,
                shape = CircleShape,
                containerColor = Color(0xFFE11D48),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 14.dp)
                    .scale(pulseScale)
                    .size(50.dp)
                    .testTag("fab_urgent_request")
            ) {
                Text("⚡", fontSize = 20.sp)
            }
        } else {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = onUrgentTap,
                shape = RoundedCornerShape(24.dp),
                containerColor = Color(0xFFE11D48),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 14.dp)
                    .scale(pulseScale)
                    .heightIn(min = 48.dp)
                    .testTag("fab_urgent_request"),
                icon = { Text("⚡", fontSize = 18.sp) },
                text = {
                    Text(
                        text = "اطلب خدمتك",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            )
        }
    }
}

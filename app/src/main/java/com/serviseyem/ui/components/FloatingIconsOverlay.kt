package com.serviseyem.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.utils.VisualThemePalette

/**
 * 🌟 Circular Elegant Floating Action Buttons (FABs)
 * - أزرار دائرية فاخرة بالكامل دون أي شكل مستطيل يعيق رؤية العناوين أو الأقسام.
 * - «المساعد الذكي» في جهة البداية (BottomStart) بشكل دائري متوهج أزرق/سماوي أنيق.
 * - «اطلب خدمتك» في جهة النهاية (BottomEnd) بشكل دائري متوهج وردي/ذهبي أنيق.
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
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val showLabels = maxWidth >= 360.dp

        // 1. «المساعد الذكي» - دائري أنيق في الزاوية اليسرى
        if (!settings.assistantHidden) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 14.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAssistantClick()
                    },
                    shape = CircleShape,
                    containerColor = Color(0xFF0284C7),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(54.dp)
                        .border(2.dp, Color(0xFF38BDF8), CircleShape)
                        .testTag("fab_smart_assistant")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.4f), Color(0xFF0369A1))
                                )
                            )
                    ) {
                        Text("🤖", fontSize = 24.sp)
                    }
                }
                if (showLabels) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        border = BorderStroke(0.5.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "المساعد الذكي",
                            fontSize = 10.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBAE6FD),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // 2. «اطلب خدمتك» - دائري أنيق في الزاوية اليمنى
        if (!settings.footerMessage.contains("hide_urgent_fab")) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 14.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRequestServiceClick()
                    },
                    shape = CircleShape,
                    containerColor = Color(0xFFE11D48),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 10.dp),
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(54.dp)
                        .border(2.dp, Color(0xFFFFD700), CircleShape)
                        .testTag("fab_urgent_request")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFB7185).copy(alpha = 0.4f), Color(0xFFBE123C))
                                )
                            )
                    ) {
                        Text("⚡", fontSize = 24.sp)
                    }
                }
                if (showLabels) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        border = BorderStroke(0.5.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "اطلب خدمتك",
                            fontSize = 10.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFE4E6),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

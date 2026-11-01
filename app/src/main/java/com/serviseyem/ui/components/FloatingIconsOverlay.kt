package com.serviseyem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.utils.VisualThemePalette

/**
 * 🌟 Dual Separated Floating Action Buttons
 * - Each button is positioned on its own side of the screen:
 *   - Start Corner (BottomStart): "المساعد الذكي" with SkyBlue glow & title label badge
 *   - End Corner (BottomEnd): "اطلب خدمتك الآن" with Coral/Amber glow & title label badge
 * - Both buttons feature a high-contrast, professional title badge underneath so users immediately know their purpose.
 */
@Composable
fun BoxScope.FloatingIconsOverlay(
    settings: AdminSettingsEntity,
    themeColors: VisualThemePalette,
    isClientUser: Boolean = true,
    onAssistantClick: () -> Unit,
    onRequestServiceClick: () -> Unit
) {
    // 1. Smart Assistant Floating Button - Side 1 (BottomStart)
    if (!settings.assistantHidden) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 12.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAssistantClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Circular Icon Button (48dp)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(10.dp, CircleShape, spotColor = Color(0xFF38B6FF))
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF38B6FF), Color(0xFF0E90FF))
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✨",
                    fontSize = 21.sp
                )
            }

            // Professional Glassmorphism Title Badge Underneath
            Box(
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(8.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xEE080E1C))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF38B6FF).copy(alpha = 0.8f), Color(0xFF0E90FF).copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 2.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "المساعد الذكي",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE2F3FF)
                )
            }
        }
    }

    // 2. Request Service Floating Button - Side 2 (BottomEnd)
    if (!settings.footerMessage.contains("hide_urgent_fab")) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 12.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRequestServiceClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Circular Icon Button (48dp)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(10.dp, CircleShape, spotColor = Color(0xFFFF5E7A))
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF5E7A), Color(0xFFFF2D6F))
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡",
                    fontSize = 21.sp
                )
            }

            // Professional Glassmorphism Title Badge Underneath
            Box(
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(8.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xEE080E1C))
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFFF5E7A).copy(alpha = 0.8f), Color(0xFFFF2D6F).copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 2.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "اطلب خدمتك الآن",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFEBF0)
                )
            }
        }
    }
}

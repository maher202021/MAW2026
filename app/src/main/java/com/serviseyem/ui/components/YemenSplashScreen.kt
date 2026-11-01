package com.serviseyem.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.R
import com.serviseyem.data.AdminSettingsEntity

/**
 * 👑 YemenSplashScreen - شاشة ترحيب جذابة ومصممة بـ Material 3
 * تعرض شعار التطبيق الـ 3D مع تأثيرات نيون وذهبية متحركة
 */
@Composable
fun YemenSplashScreen(
    settingsState: AdminSettingsEntity,
    modifier: Modifier = Modifier
) {
    // 🌟 Infinite Pulsing Glow Animation
    val infiniteTransition = rememberInfiniteTransition(label = "SplashGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    val logoAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF0B1120),
                        Color(0xFF070A14)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // 💎 Logo Container with 3D Image & Glowing Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .scale(glowScale)
            ) {
                // Background Radial Glow
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.35f),
                                    Color(0xFF10B981).copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Outer Gold/Cyan Border Ring
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFF00E5FF),
                                    Color(0xFF10B981),
                                    Color(0xFFFFD700)
                                )
                            ),
                            shape = CircleShape
                        )
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.serviseyem_logo),
                            contentDescription = "شعار دليل خدمات اليمن",
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .alpha(logoAlpha)
                        )
                    }
                }

                // WAM Badge Accent
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-4).dp)
                ) {
                    Text(
                        text = "WAM",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 👑 App Title & Subtitle in Material 3 Style
            Text(
                text = "دليل خدمات اليمن الذكي",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "دليلك الشامل لكل الخدمات والمهن والخرائط",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ⚡ Material 3 Modern Progress Indicator
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF00E5FF),
                        trackColor = Color(0xFF334155),
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "جاري تهيئة ومزامنة البيانات...",
                        fontSize = 12.5.sp,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 📜 Custom Welcome Message Card
            val splashMessage = if (settingsState.splashWelcomeMessage.isNotBlank()) {
                settingsState.splashWelcomeMessage
            } else {
                "التطبيق الأول في اليمن والوطن العربي الذي يربط مقدمي الخدمات وأصحاب المهن بالمستخدمين فورياً"
            }

            Text(
                text = splashMessage,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

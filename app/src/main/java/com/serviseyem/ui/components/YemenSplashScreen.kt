package com.serviseyem.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.R
import com.serviseyem.data.AdminSettingsDefaults
import com.serviseyem.data.AdminSettingsEntity

/**
 * YemenSplashScreen - شاشة ترحيب مصممة بـ Material 3
 * تعرض شعار التطبيق التراثي المحدّث مع نص ترحيبي واضح ومتوافق مع جميع الأحجام
 */
@Composable
fun YemenSplashScreen(
    settingsState: AdminSettingsEntity,
    modifier: Modifier = Modifier,
    onSkip: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    val logoAlpha by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoAlpha"
    )

    val skipModifier = if (onSkip != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onSkip
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF072B1F),
                        Color(0xFF0A1929),
                        Color(0xFF070A14)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .then(skipModifier)
            .testTag("yemen_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            // Logo Container with WAM Heritage Emblem & Glowing Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(164.dp)
                    .scale(glowScale)
            ) {
                Box(
                    modifier = Modifier
                        .size(156.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFD62828).copy(alpha = 0.28f),
                                    Color(0xFF10B981).copy(alpha = 0.24f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFFD62828),
                                    Color(0xFF10B981),
                                    Color(0xFFE5C158),
                                    Color(0xFFD62828)
                                )
                            ),
                            shape = CircleShape
                        )
                        .background(Color(0xFF07402D))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.wam_3d_icon_foreground),
                            contentDescription = "شعار دليل خدمات اليمن",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(124.dp)
                                .clip(CircleShape)
                                .alpha(logoAlpha)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
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
                        lineHeight = 18.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            val displayTitle = settingsState.customAppName.ifBlank {
                settingsState.appName.ifBlank { "دليل خدمات اليمن" }
            }

            Text(
                text = displayTitle,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                lineHeight = 36.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                softWrap = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "بوابة الخدمات الذكية في اليمن — مهنيون، متاجر، عقارات وخرائط",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFA7F3D0),
                textAlign = TextAlign.Center,
                maxLines = 3,
                softWrap = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F2920).copy(alpha = 0.9f),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFD700),
                        trackColor = Color(0xFF1E3A2F),
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "جاري تجهيز دليلك وتحديث الخدمات...",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        softWrap = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val rawSplashMsg = settingsState.splashWelcomeMessage.trim()
            val isLegacyDefault = rawSplashMsg.isBlank() ||
                rawSplashMsg.contains("التطبيق الأول في اليمن والوطن العربي")
            val splashMessage = if (isLegacyDefault) {
                AdminSettingsDefaults.SPLASH_WELCOME_MESSAGE
            } else {
                rawSplashMsg
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.72f),
                border = BorderStroke(1.dp, Color(0xFFD4A017).copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = splashMessage,
                    fontSize = 13.5.sp,
                    lineHeight = 22.sp,
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                )
            }
        }
    }
}

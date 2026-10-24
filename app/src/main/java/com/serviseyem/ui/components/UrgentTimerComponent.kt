package com.serviseyem.ui.components

import androidx.compose.animation.animateColorAsState
import com.serviseyem.ui.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * ⏱️ UrgentTimerComponent
 * مؤقت تنازلي ذكي مخصص للطلبات العاجلة (30 دقيقة)
 */
@Composable
fun UrgentTimerComponent(
    expiresAt: Long,
    totalDurationMillis: Long = 30 * 60 * 1000L,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    onTimerExpired: () -> Unit = {}
) {
    var timeLeftMs by remember(expiresAt) {
        mutableStateOf(maxOf(0L, expiresAt - System.currentTimeMillis()))
    }

    LaunchedEffect(expiresAt) {
        while (timeLeftMs > 0) {
            delay(1000L)
            timeLeftMs = maxOf(0L, expiresAt - System.currentTimeMillis())
            if (timeLeftMs <= 0) {
                onTimerExpired()
            }
        }
    }

    val remainingSeconds = timeLeftMs / 1000L
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    val isExpired = timeLeftMs <= 0
    val isCritical = timeLeftMs < (5 * 60 * 1000L) && !isExpired // أقل من 5 دقائق
    val isWarning = timeLeftMs < (10 * 60 * 1000L) && !isCritical && !isExpired // أقل من 10 دقائق

    val progress = if (totalDurationMillis > 0) {
        (timeLeftMs.toFloat() / totalDurationMillis.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val alphaAnim = if (isCritical) {
        val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
        val anim by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "CriticalBlink"
        )
        anim
    } else {
        1f
    }

    val targetColor = when {
        isExpired -> Color(0xFF757575)
        isCritical -> Color(0xFFD32F2F)
        isWarning -> Color(0xFFF57C00)
        else -> Color(0xFF388E3C)
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300),
        label = "ColorTransition"
    )

    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    if (isCompact) {
        Row(
            modifier = modifier
                .alpha(if (isCritical) alphaAnim else 1f)
                .clip(RoundedCornerShape(8.dp))
                .background(animatedColor.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Refresh,
                contentDescription = null,
                tint = animatedColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (isExpired) "انتهى الوقت" else timeFormatted,
                color = animatedColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .alpha(if (isCritical) alphaAnim else 1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = animatedColor.copy(alpha = 0.08f)
            ),
            border = BorderStroke(1.5.dp, animatedColor.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Info,
                            contentDescription = null,
                            tint = animatedColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = when {
                                isExpired -> "انتهت مهلة العروض العاجلة"
                                isCritical -> "وقت حرج - متبقي أقل من 5 دقائق!"
                                isWarning -> "مهلة الاستجابة السريعة (30 دقيقة)"
                                else -> "مؤقت الاستجابة السريعة"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = animatedColor
                        )
                    }

                    Text(
                        text = if (isExpired) "00:00" else timeFormatted,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = animatedColor
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = animatedColor,
                    trackColor = animatedColor.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "بداية الطلب",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = if (isExpired) "انتهى الوقت" else "متبقي $minutes دقيقة و $seconds ثانية",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = animatedColor
                    )
                    Text(
                        text = "الحد الأقصى (30 دقيقة)",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

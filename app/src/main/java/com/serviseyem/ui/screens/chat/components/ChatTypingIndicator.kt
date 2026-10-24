package com.serviseyem.ui.screens.chat.components

import androidx.compose.animation.*
import com.serviseyem.ui.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.*

/**
 * ⌨️ TypingPresenceController
 * Manages debounced remote typing emission (500ms delay) with an automatic 4-second timeout.
 */
class TypingPresenceController(
    private val onTypingStateChanged: (Boolean) -> Unit,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    private var debounceJob: Job? = null
    private var timeoutJob: Job? = null
    private var isCurrentlyTyping = false

    fun onUserTyped() {
        if (!isCurrentlyTyping) {
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(500)
                isCurrentlyTyping = true
                onTypingStateChanged(true)
            }
        }

        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(4000)
            if (isCurrentlyTyping) {
                isCurrentlyTyping = false
                onTypingStateChanged(false)
            }
        }
    }

    fun onUserStoppedOrSent() {
        debounceJob?.cancel()
        timeoutJob?.cancel()
        if (isCurrentlyTyping) {
            isCurrentlyTyping = false
            onTypingStateChanged(false)
        }
    }
}

/**
 * 🌊 ChatTypingIndicator
 * Smooth 3-dot staggered wave typing animation for chat messages.
 */
@Composable
fun ChatTypingIndicator(
    isTyping: Boolean,
    userName: String? = null,
    dotColor: Color = Color(0xFF00E5FF),
    dotSize: Dp = 6.dp,
    modifier: Modifier = Modifier,
    themeColors: VisualThemePalette? = null
) {
    val activeDotColor = themeColors?.accent ?: dotColor
    val activeSurfaceColor = themeColors?.surface ?: Color(0xFF1E293B).copy(alpha = 0.95f)
    val textSecondary = themeColors?.textSecondary ?: Color(0xFF94A3B8)

    AnimatedVisibility(
        visible = isTyping,
        enter = fadeIn(animationSpec = tween(200)) + expandVertically(),
        exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = activeSurfaceColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val label = if (!userName.isNullOrBlank()) "$userName يكتب الآن" else "يكتب الآن"
                Text(
                    text = label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondary
                )

                PulsingDotsWave(dotColor = activeDotColor, dotSize = dotSize)
            }
        }
    }
}

@Composable
fun PulsingDotsWave(
    dotColor: Color = Color(0xFF00E5FF),
    dotSize: Dp = 6.dp
) {
    val transition = rememberInfiniteTransition(label = "dots_wave")

    val dot1Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 130, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 260, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .offset(y = dot1Offset.dp)
                .size(dotSize)
                .clip(CircleShape)
                .background(dotColor)
        )
        Box(
            modifier = Modifier
                .offset(y = dot2Offset.dp)
                .size(dotSize)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = 0.85f))
        )
        Box(
            modifier = Modifier
                .offset(y = dot3Offset.dp)
                .size(dotSize)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = 0.7f))
        )
    }
}

// Backwards-compatible overload
@Composable
fun TypingIndicator(userName: String = "", modifier: Modifier = Modifier) {
    ChatTypingIndicator(isTyping = true, userName = userName, modifier = modifier)
}

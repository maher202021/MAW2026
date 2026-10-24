package com.serviseyem.ui.components

import androidx.compose.animation.core.*
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.delay

/**
 * 🎙️ VoiceSearch (واجهة البحث الصوتي الذكي)
 * تدعم التعرف على الصوت الحقيقي باللغة العربية عبر VoiceManager.onHear مع موجات صوتية متحركة واقتراحات فورية.
 */
@Composable
fun VoiceSearchDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSpeechResult: (String) -> Unit,
    themeColors: VisualThemePalette
) {
    if (!isVisible) return

    var isListening by remember { mutableStateOf(true) }
    var recognizedText by remember { mutableStateOf("جاري الاستماع...") }
    var hasValidResult by remember { mutableStateOf(false) }

    val startRealListening = remember {
        {
            isListening = true
            hasValidResult = false
            recognizedText = "جاري الاستماع..."
            val hearHandler = com.serviseyem.VoiceManager.onHear
            if (hearHandler != null) {
                hearHandler.invoke { spokenText ->
                    if (spokenText.isNotBlank()) {
                        recognizedText = spokenText.trim()
                        hasValidResult = true
                    } else {
                        recognizedText = "لم يتم التقاط صوت واضح، اضغط على الميكروفون للمحاولة مجدداً"
                        hasValidResult = false
                    }
                    isListening = false
                }
            } else {
                recognizedText = "يرجى التحدث أو اختيار أحد الاقتراحات السريعة أدناه"
                hasValidResult = false
                isListening = false
            }
        }
    }

    // Pulsing animation for microphone
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Trigger real voice recognition via VoiceManager.onHear when dialog opens
    LaunchedEffect(isVisible) {
        if (isVisible) {
            startRealListening()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = when {
                        isListening -> "تحدث الآن للبحث..."
                        hasValidResult -> "تم التقاط الصوت بنجاح"
                        else -> "تعذر التقاط الصوت"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Pulsing Mic Circle (clickable to retry listening)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { startRealListening() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(if (isListening) pulseScale else 1f)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.2f), CircleShape)
                    )

                    Surface(
                        shape = CircleShape,
                        color = if (isListening) Color(0xFF00E5FF) else if (hasValidResult) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isListening) "🎙️" else if (hasValidResult) "✅" else "🔄",
                                fontSize = 26.sp
                            )
                        }
                    }
                }

                // Recognized text box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF334155),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = recognizedText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(14.dp)
                    )
                }

                // Voice search quick suggestions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("كهربائي منازل", "مطعم شواية", "شقة مفروشة").forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.clickable {
                                recognizedText = suggestion
                                hasValidResult = true
                                isListening = false
                            }
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 10.sp,
                                color = Color(0xFFCBD5E1),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (hasValidResult && recognizedText.isNotBlank()) {
                        onSpeechResult(recognizedText)
                    }
                    onDismiss()
                },
                enabled = hasValidResult && recognizedText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color(0xFF0F172A))
            ) {
                Text("تأكيد البحث")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

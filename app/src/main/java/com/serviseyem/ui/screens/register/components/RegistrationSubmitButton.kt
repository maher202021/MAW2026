package com.serviseyem.ui.screens.register.components

import androidx.compose.foundation.layout.*
import com.serviseyem.ui.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette

/**
 * 🚀 RegistrationSubmitButton - زر الإرسال الموحد المحسن
 * يدعم: مؤشر التقدم الخطي، والتعطيل الفوري ضد النقر المتكرر (Debounce)
 */
@Composable
fun RegistrationSubmitButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    loadingText: String = "جاري الإرسال والتحقق...",
    enabled: Boolean = true,
    progress: Float? = null,
    debounceTimeMs: Long = 1000L,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    var lastClickTime by remember { mutableStateOf(0L) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > debounceTimeMs && !isLoading && enabled) {
                    lastClickTime = currentTime
                    onClick()
                }
            },
            enabled = enabled && !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColors.accent,
                contentColor = Color.Black,
                disabledContainerColor = themeColors.accent.copy(alpha = 0.4f),
                disabledContentColor = Color.Black.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.5.dp
                    )
                    Text(
                        text = loadingText,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            } else {
                Text(
                    text = text,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Optional Linear Progress Bar
        if (isLoading && progress != null) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = themeColors.accent,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}

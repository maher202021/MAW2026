package com.serviseyem.ui.screens.assistant.components

import androidx.compose.foundation.background
import com.serviseyem.ui.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.ChatIcons

/**
 * ✏️ AssistantInputBar
 * Bottom text field, microphone trigger, and send button.
 */
@Composable
fun AssistantInputBar(
    typedText: String,
    isGenerating: Boolean,
    themeColors: VisualThemePalette,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = themeColors.surface,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Direct Microphone button inside the bar
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(themeColors.background)
            ) {
                Icon(
                    imageVector = ChatIcons.Mic,
                    contentDescription = "تسجيل صوتي",
                    tint = themeColors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            OutlinedTextField(
                value = typedText,
                onValueChange = onTextChanged,
                placeholder = { Text("اكتب سؤالك أو اطلب خدمة هنا...", fontSize = 11.sp, color = themeColors.textSecondary) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = themeColors.textPrimary,
                    unfocusedTextColor = themeColors.textPrimary,
                    focusedBorderColor = themeColors.accent,
                    unfocusedBorderColor = themeColors.border,
                    focusedContainerColor = themeColors.background,
                    unfocusedContainerColor = themeColors.background
                )
            )

            IconButton(
                onClick = onSend,
                enabled = typedText.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (typedText.isNotBlank()) themeColors.accent else themeColors.surface)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "إرسال",
                    tint = if (typedText.isNotBlank()) Color.Black else themeColors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

package com.serviseyem.ui.screens.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.VoiceState
import com.serviseyem.utils.VoiceStatus

@Composable
fun VoiceSearchDialog(
    state: VoiceState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onUseKeyboardMic: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "البحث الصوتي",
                    tint = if (state.status == VoiceStatus.LISTENING) Color(0xFF10B981) else Color(0xFFF59E0B)
                )
                Text(
                    text = if (state.status == VoiceStatus.LISTENING) "تحدث الآن…" else "البحث الصوتي",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.status == VoiceStatus.LISTENING) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFF1E293B)
                    )
                    Text(
                        text = state.partial.ifBlank { "أستمع إليك…" },
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.testTag("voice_dialog_partial_text")
                    )
                } else {
                    Text(
                        text = state.message.ifBlank { "تعذر إتمام البحث الصوتي." },
                        color = Color(0xFFF59E0B),
                        fontSize = 13.5.sp,
                        lineHeight = 21.sp,
                        modifier = Modifier.testTag("voice_dialog_error_text")
                    )

                    if (state.canUseKeyboard) {
                        OutlinedButton(
                            onClick = onUseKeyboardMic,
                            border = BorderStroke(1.dp, Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("voice_use_keyboard_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "استعمل ميكروفون لوحة المفاتيح",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "استعمل ميكروفون لوحة المفاتيح",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    if (state.canOpenSettings) {
                        OutlinedButton(
                            onClick = onOpenSettings,
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("voice_open_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "إعدادات الإدخال الصوتي",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إعدادات الإدخال الصوتي",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (state.status == VoiceStatus.ERROR) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("voice_retry_button")
                ) {
                    Text(
                        text = "إعادة المحاولة",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("voice_dismiss_button")
            ) {
                Text(
                    text = "إغلاق",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    )
}

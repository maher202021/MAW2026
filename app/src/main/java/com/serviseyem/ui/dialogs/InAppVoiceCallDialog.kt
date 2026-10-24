package com.serviseyem.ui.dialogs

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun InAppVoiceCallDialog(
    callerName: String,
    callerRole: String,
    onDismiss: () -> Unit,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    var callState by remember { mutableStateOf("RINGING") } // RINGING, CONNECTED, UNANSWERED
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var callSeconds by remember { mutableStateOf(0) }
    var ringingSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(callState) {
        if (callState == "RINGING") {
            ringingSeconds = 0
            while (callState == "RINGING" && ringingSeconds < 8) {
                delay(1000)
                ringingSeconds++
            }
            if (callState == "RINGING" && ringingSeconds >= 8) {
                callState = "UNANSWERED"
            }
        } else if (callState == "CONNECTED") {
            callSeconds = 0
            while (isActive && callState == "CONNECTED") {
                delay(1000)
                callSeconds++
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, themeColors.accent),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🎙️ مكالمة صوتية عالية الجودة (HD)",
                    color = themeColors.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(themeColors.primary.copy(alpha = 0.3f), CircleShape)
                        .border(2.dp, themeColors.accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 42.sp)
                }

                Text(
                    text = callerName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = callerRole,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                when (callState) {
                    "RINGING" -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📲 جاري الاتصال والرنين... 🔔 (${8 - ringingSeconds}ث)", color = Color.Yellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("في انتظار قبول وإجابة $callerName...", color = Color.Gray, fontSize = 11.sp)
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    callState = "CONNECTED"
                                    Toast.makeText(context, "🟢 تم قبول المكالمة وبدء التواصل المباشر", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🟢 إجابة المكالمة (محاكاة التوصيل)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    "UNANSWERED" -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("⚠️ المشترك لا يجيب حالياً أو أن هاتفه مغلق/خارج التغطية.", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text("يرجى إرسال رسالة نصية في المحادثة أو المحاولة لاحقاً.", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { callState = "RINGING" },
                                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                                ) {
                                    Text("إعادة المحاولة 🔄", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                                ) {
                                    Text("إغلاق ❌", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    "CONNECTED" -> {
                        val mins = callSeconds / 60
                        val secs = callSeconds % 60
                        val timeStr = String.format("%02d:%02d", mins, secs)
                        Text("🟢 متصل الآن ($timeStr) - جودة صوتية HD", color = Color.Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (callState == "CONNECTED") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                if (isMuted) {
                                    Toast.makeText(context, "🔇 تم كتم الميكروفون", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "🎙️ الميكروفون يعمل الآن", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .background(if (isMuted) Color.Red.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f), CircleShape)
                                .size(48.dp)
                        ) {
                            Text(if (isMuted) "🔇" else "🎙️", fontSize = 20.sp)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = CircleShape,
                            modifier = Modifier.size(60.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("📞", fontSize = 24.sp, color = Color.White)
                        }

                        IconButton(
                            onClick = {
                                isSpeakerOn = !isSpeakerOn
                                if (isSpeakerOn) {
                                    Toast.makeText(context, "🔊 تم تشغيل مكبر الصوت الخارجي", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "🔈 تم إيقاف مكبر الصوت (السماعة الداخلية)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .background(if (isSpeakerOn) themeColors.accent.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f), CircleShape)
                                .size(48.dp)
                        ) {
                            Text(if (isSpeakerOn) "🔊" else "🔈", fontSize = 20.sp)
                        }
                    }
                } else if (callState == "RINGING") {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = CircleShape,
                        modifier = Modifier.size(54.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("📞", fontSize = 22.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

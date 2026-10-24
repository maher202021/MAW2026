package com.serviseyem.utils

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ⏱️ Session Management & UX Enhancement Engine
 * Solves Problem 10: Manages inactivity timeout auto-lock with warnings, auto-renewal,
 * smooth re-login, operation progress modals, and clean logout memory purging.
 */

// ==========================================
// 1. ⏱️ Session Timeout Warning Dialog
// ==========================================
@Composable
fun SessionTimeoutWarningModal(
    isVisible: Boolean,
    remainingSeconds: Int = 60,
    onExtendSession: () -> Unit,
    onLogoutNow: () -> Unit,
    themeColors: VisualThemePalette
) {
    if (isVisible) {
        var countdownSeconds by remember(isVisible, remainingSeconds) {
            mutableIntStateOf(remainingSeconds.coerceAtLeast(0))
        }

        LaunchedEffect(isVisible, remainingSeconds) {
            countdownSeconds = remainingSeconds.coerceAtLeast(0)
            while (countdownSeconds > 0 && isVisible) {
                delay(1000L)
                countdownSeconds--
            }
            if (countdownSeconds <= 0 && isVisible) {
                onLogoutNow()
            }
        }

        AlertDialog(
            onDismissRequest = { /* Modal force choice */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Session Warning",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تنبيه انتهاء الجلسة ⏱️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "نظراً لعدم وجود نشاط مؤخراً، ستنتهي جلسة تسجليك الحالية آلياً لحماية خصوصيتك خلال:",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "$countdownSeconds ثانية",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onExtendSession,
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تمديد الجلسة الآن 🔄", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onLogoutNow) {
                    Text("تسجيل الخروج الآن 🚪", fontSize = 11.sp, color = Color(0xFFEF4444))
                }
            }
        )
    }
}

// ==========================================
// 2. 📊 Operation Progress Modal Overlay
// ==========================================
@Composable
fun OperationProgressModal(
    isVisible: Boolean,
    title: String, // e.g. "جاري رفع الصور والمرفقات..."
    progress: Float, // 0.0f to 1.0f
    themeColors: VisualThemePalette
) {
    if (isVisible) {
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(32.dp).fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Loading",
                            tint = themeColors.accent,
                            modifier = Modifier.size(32.dp)
                        )

                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = themeColors.accent,
                            trackColor = Color.DarkGray
                        )

                        Text(
                            text = "${(progress * 100).toInt()}% مكتمل",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. 🧼 Clean Logout Manager & Session State Cache
// ==========================================
data class SessionState(
    val isLoggedIn: Boolean = false,
    val adminLogged: Boolean = false
)

object CleanLogoutManager {

    private const val PREFS_NAME = "YS_Local_App_Cache_v2026"

    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    @Volatile
    private var isCacheInitialized = false

    fun getSessionState(context: Context): StateFlow<SessionState> {
        if (!isCacheInitialized) {
            synchronized(this) {
                if (!isCacheInitialized) {
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    _sessionState.value = SessionState(
                        isLoggedIn = prefs.getBoolean("USER_LOGGED", false),
                        adminLogged = prefs.getBoolean("ADMIN_LOGGED", false)
                    )
                    isCacheInitialized = true
                }
            }
        }
        return sessionState
    }

    fun updateSessionState(context: Context, isLoggedIn: Boolean, adminLogged: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("USER_LOGGED", isLoggedIn)
            .putBoolean("ADMIN_LOGGED", adminLogged)
            .apply()
        _sessionState.value = SessionState(
            isLoggedIn = isLoggedIn,
            adminLogged = adminLogged
        )
        isCacheInitialized = true
    }

    fun executeCleanLogout(context: Context, onComplete: () -> Unit) {
        try {
            // 1. Clear encrypted admin session in SecureStorage
            SecureStorage(context).clearAdminSession()

            // 2. Clear SharedPreferences session tokens and update StateFlow cache
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove("USER_TOKEN")
                .remove("USER_LOGGED")
                .remove("ADMIN_LOGGED")
                .remove("KEY_OFFLINE_QUEUE")
                .apply()

            _sessionState.value = SessionState(
                isLoggedIn = false,
                adminLogged = false
            )
            isCacheInitialized = true

            // 3. Invoke callback to reset ViewModel state and navigate to login
            onComplete()
        } catch (e: Exception) {
            _sessionState.value = SessionState(isLoggedIn = false, adminLogged = false)
            onComplete()
        }
    }
}

package com.serviseyem.ui.screens.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 📡 MapErrorOverlay
 * Fallback UI overlay displayed when map tiles fail to load over network (within 3s timeout).
 * Supports single-retry enforcement (retryAvailable).
 */
@Composable
fun MapErrorOverlay(
    onRetry: (() -> Unit)? = null,
    onSwitchToRadar: (() -> Unit)? = null,
    retryAvailable: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "تحذير",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "⚠️ تعذر تحميل الخريطة، تم التفعيل التلقائي للرادار المحلي",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = if (retryAvailable) {
                    "استغرق تحميل خريطة الشبكة أكثر من 3 ثوانٍ. يعمل الرادار المحلي الآن بكفاءة 100% أو يمكنك إعادة المحاولة مرة واحدة."
                } else {
                    "تم استنفاد محاولة الاتصال بالخريطة السحابية. يعمل الآن الرادار المحلي التفاعلي بدون إنترنت."
                },
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                if (onRetry != null && retryAvailable) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إعادة المحاولة", color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (onSwitchToRadar != null) {
                    OutlinedButton(
                        onClick = onSwitchToRadar,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("الرادار المحلي", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

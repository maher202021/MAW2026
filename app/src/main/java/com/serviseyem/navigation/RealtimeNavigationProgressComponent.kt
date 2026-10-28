package com.serviseyem.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.components.BespokePremiumSystem

/**
 * 🧭 RealtimeNavigationProgressComponent
 * مكوّن واجهة مستخدم متقدم مبني بـ Jetpack Compose:
 * 1. يعرض تقدم الملاحة في الوقت الفعلي مع شريط إنجاز انسيابي ونسبة مئوية.
 * 2. يشير بوضوح لا لبس فيه إلى حالة الاتصال: الوضع المتصل (Online) أو غير المتصل (Offline).
 * 3. يعرض التوجيه الصوتي القادم، المسافة والوقت المتبقي، ووقت الوصول المتوقع، وعداد السرعة.
 */
@Composable
fun RealtimeNavigationProgressComponent(
    isOfflineMode: Boolean,
    progressPercentage: Float, // 0.0f .. 1.0f
    remainingDistanceText: String,
    estimatedDurationText: String,
    estimatedArrivalTimeText: String,
    currentSpeedKmh: Int = 42,
    currentInstruction: String,
    nextManeuver: ManeuverType,
    destinationName: String,
    isVoiceMuted: Boolean = false,
    onToggleVoice: () -> Unit = {},
    onRecenterClick: () -> Unit = {},
    onEndNavigationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage.coerceIn(0f, 1f),
        label = "nav_progress"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0D1424).copy(alpha = 0.96f),
        border = BorderStroke(1.dp, BespokePremiumSystem.GlassBorder),
        shadowElevation = 16.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Connection Mode Indicator (Online / Offline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isOfflineMode) Color(0xFF064E3B).copy(alpha = 0.7f) else Color(0xFF0C4A6E).copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, if (isOfflineMode) Color(0xFF10B981) else Color(0xFF0284C7))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOfflineMode) Color(0xFF10B981) else Color(0xFF38BDF8))
                        )
                        Text(
                            text = if (isOfflineMode) "🗺️ وضع غير متصل (Offline Valhalla)" else "🛰️ وضع متصل (Online OSM)",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Voice Mute Quick Control
                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isVoiceMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "الصوت",
                        tint = if (isVoiceMuted) Color.Gray else Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 2. Next Turn Maneuver & Destination Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    if (isOfflineMode) Color(0xFF059669) else Color(0xFF2563EB),
                                    if (isOfflineMode) Color(0xFF10B981) else Color(0xFF00E5FF)
                                )
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (nextManeuver) {
                            ManeuverType.TURN_RIGHT, ManeuverType.SLIGHT_RIGHT, ManeuverType.SHARP_RIGHT -> Icons.Default.TurnRight
                            ManeuverType.TURN_LEFT, ManeuverType.SLIGHT_LEFT, ManeuverType.SHARP_LEFT -> Icons.Default.TurnLeft
                            ManeuverType.DESTINATION -> Icons.Default.Flag
                            ManeuverType.ROUNDABOUT -> Icons.Default.ChangeCircle
                            ManeuverType.U_TURN -> Icons.Default.Refresh
                            else -> Icons.Default.ArrowUpward
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentInstruction,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "الوجهة: $destinationName",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 3. Real-Time Progress Bar & Completion Indicator
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تقدم المسار", color = Color.Gray, fontSize = 10.5.sp)
                    Text(
                        text = "${(animatedProgress * 100).toInt()}% مكتمل",
                        color = if (isOfflineMode) Color(0xFF10B981) else Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = if (isOfflineMode) Color(0xFF10B981) else Color(0xFF00E5FF),
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            // 4. Trip Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("المتبقي", color = Color.Gray, fontSize = 10.5.sp)
                    Text(remainingDistanceText, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("وقت الوصول", color = Color.Gray, fontSize = 10.5.sp)
                    Text(estimatedArrivalTimeText, color = Color(0xFF00E5FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("الوقت المقدر", color = Color.Gray, fontSize = 10.5.sp)
                    Text(estimatedDurationText, color = Color(0xFF10B981), fontSize = 17.sp, fontWeight = FontWeight.Black)
                }
            }

            Divider(color = Color.White.copy(alpha = 0.08f))

            // 5. Actions: Recenter & End Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRecenterClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("موقعي", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onEndNavigationClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.2f).height(44.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إنهاء الرحلة", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

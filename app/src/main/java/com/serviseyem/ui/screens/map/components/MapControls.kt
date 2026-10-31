package com.serviseyem.ui.screens.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.screens.map.MapMode
import com.serviseyem.utils.VisualThemePalette

/**
 * 🎮 MapControls (مطابقة للشاشة والضوابط العائمة للخريطة)
 */
@Composable
fun MapControls(
    isRadarMode: Boolean,
    onToggleRadarMode: () -> Unit,
    isHeatmapActive: Boolean,
    onToggleHeatmap: () -> Unit,
    currentLayer: String = "satellite",
    onSwitchLayer: (String) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onRecenterLocation: () -> Unit,
    onDownloadValhallaClick: () -> Unit = {},
    isGpsActive: Boolean,
    // Vela Modes
    currentMode: com.serviseyem.ui.screens.map.MapMode,
    onModeChange: (com.serviseyem.ui.screens.map.MapMode) -> Unit,
    tiltAngle: Float,
    onToggleTilt: () -> Unit,
    onOpenAleph: () -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shadowElevation = 12.dp,
        modifier = modifier
            .width(54.dp)
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Section 1: Navigation & GPS ---
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onRecenterLocation()
                },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "موقعي",
                    tint = if (isGpsActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }

            val isNavActive = currentLayer == "navigation"
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    if (isNavActive) onSwitchLayer("google_satellite") else onSwitchLayer("navigation")
                },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "ملاحة",
                    tint = if (isNavActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.width(30.dp))

            // --- Section 2: View Controls (Zoom & Layers) ---
            IconButton(onClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onZoomIn()
            }, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.Add, contentDescription = "+", tint = Color.White)
            }

            IconButton(onClick = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onZoomOut()
            }, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "-", tint = Color.White)
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onToggleRadarMode()
                },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (isRadarMode) Icons.Default.Map else Icons.Default.Explore,
                    contentDescription = "رادار",
                    tint = if (isRadarMode) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onToggleTilt,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = "ثلاثي الأبعاد",
                    tint = if (tiltAngle > 0) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = {
                    val nextLayer = when(currentLayer) {
                        "google_satellite" -> "osm"
                        "osm" -> "navigation"
                        "navigation" -> "streets"
                        else -> "google_satellite"
                    }
                    onSwitchLayer(nextLayer)
                },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(Icons.Default.Layers, contentDescription = "طبقات", tint = Color.White)
            }

            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.width(30.dp))

            // --- Section 3: Modes (Vela) ---
            com.serviseyem.ui.screens.map.MapMode.values().forEach { mode ->
                val isSelected = currentMode == mode
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        if (mode == com.serviseyem.ui.screens.map.MapMode.ALEPH_AI) onOpenAleph() else onModeChange(mode)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (isSelected) Color(0xFF10B981).copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Text(text = mode.icon, fontSize = 18.sp)
                }
            }

            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.width(30.dp))

            // --- Section 4: Tools (Heatmap & Download) ---
            IconButton(onClick = onToggleHeatmap, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = if (isHeatmapActive) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                    tint = if (isHeatmapActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.5f),
                    contentDescription = "أوفلاين",
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = onDownloadValhallaClick, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.Download,
                    tint = Color(0xFF10B981),
                    contentDescription = "تحميل",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

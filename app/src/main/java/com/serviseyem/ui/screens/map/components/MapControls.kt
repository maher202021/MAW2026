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

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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
    // New Advanced Map Enhancements
    isDeadZoneActive: Boolean = false,
    onToggleDeadZoneOverlay: () -> Unit = {},
    isReliabilityActive: Boolean = false,
    onToggleReliability: () -> Unit = {},
    onOpenRoutePlanner: () -> Unit = {},
    isRoadSafetyActive: Boolean = false,
    onToggleRoadSafety: () -> Unit = {},
    onOpenAddOsmLandmark: () -> Unit = {},
    onTriggerRuralSos: () -> Unit = {},
    isNightMode: Boolean = false,
    onToggleNightMode: () -> Unit = {},
    isGroupActive: Boolean = false,
    onToggleGroupTracking: () -> Unit = {},
    onOpenGnssDiagnostics: () -> Unit = {},
    onExportGpx: () -> Unit = {},
    onPrintMapPdf: () -> Unit = {},
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shadowElevation = 12.dp,
        modifier = modifier
            .width(54.dp)
            .heightIn(max = 580.dp)
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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

            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.width(30.dp))

            // --- Section 4: Advanced Local Map Tools (GNSS, DeadZone, Night HUD, GPX) ---
            // 1. GNSS Live Satellites Signal Panel
            IconButton(onClick = onOpenGnssDiagnostics, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.SatelliteAlt,
                    tint = themeColors.accent,
                    contentDescription = "تشخيص الأقمار",
                    modifier = Modifier.size(20.dp)
                )
            }

            // 2. Dead Zone Service Heatmap Overlay
            IconButton(onClick = onToggleDeadZoneOverlay, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = if (isDeadZoneActive) Icons.Default.GridOff else Icons.Default.GridOn,
                    tint = if (isDeadZoneActive) Color(0xFFEF4444) else Color.White.copy(alpha = 0.6f),
                    contentDescription = "مناطق التغطية",
                    modifier = Modifier.size(20.dp)
                )
            }

            // 🥇 الفكرة الأولى: "خريطة الموثوقية" (Reliability Layer) - أخضر (متاح)، أصفر (مشغول)، أحمر (غير مغطى)
            IconButton(onClick = onToggleReliability, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    tint = if (isReliabilityActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.6f),
                    contentDescription = "خريطة الموثوقية وتوافر الفنيين",
                    modifier = Modifier.size(20.dp)
                )
            }

            // 🎖️ الفكرة الخامسة: "الخريطة كأداة تخطيط للمسارات والمحطات" (Multi-Stop Route Planner)
            IconButton(onClick = onOpenRoutePlanner, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.Polyline,
                    tint = Color(0xFF00E5FF),
                    contentDescription = "تخطيط مسار مخصص",
                    modifier = Modifier.size(20.dp)
                )
            }

            // طبقة سلامة الطريق وصعوبات الطرق اليمنية (Road Safety Layer)
            IconButton(onClick = onToggleRoadSafety, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    tint = if (isRoadSafetyActive) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.6f),
                    contentDescription = "سلامة وصعوبة الطريق",
                    modifier = Modifier.size(20.dp)
                )
            }

            // 🏅 الفكرة الرابعة: إضافة معلم لتوأمة OpenStreetMap (OSM Node)
            IconButton(onClick = onOpenAddOsmLandmark, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.AddLocationAlt,
                    tint = Color(0xFF10B981),
                    contentDescription = "إضافة معلم OSM",
                    modifier = Modifier.size(20.dp)
                )
            }

            // 🥉 الفكرة الثالثة: زر نجدة طوارئ ريفية بدون إنترنت (Rural Emergency SMS)
            IconButton(onClick = onTriggerRuralSos, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.Sos,
                    tint = Color(0xFFEF4444),
                    contentDescription = "نجدة ريفية SMS",
                    modifier = Modifier.size(20.dp)
                )
            }

            // 3. Night Mode / HUD
            IconButton(onClick = onToggleNightMode, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = if (isNightMode) Icons.Default.Bedtime else Icons.Default.WbSunny,
                    tint = if (isNightMode) Color(0xFF818CF8) else Color.White.copy(alpha = 0.6f),
                    contentDescription = "الوضع الليلي / HUD",
                    modifier = Modifier.size(20.dp)
                )
            }

            // 4. Group Members Tracking
            IconButton(onClick = onToggleGroupTracking, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.Group,
                    tint = if (isGroupActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.6f),
                    contentDescription = "تتبع المجموعة",
                    modifier = Modifier.size(20.dp)
                )
            }

            // 5. GPX Track Export / Import
            IconButton(onClick = onExportGpx, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.AltRoute,
                    tint = Color(0xFF38BDF8),
                    contentDescription = "تصدير GPX",
                    modifier = Modifier.size(20.dp)
                )
            }

            // 6. Printable Map PDF Summary
            IconButton(onClick = onPrintMapPdf, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Default.Print,
                    tint = Color.White.copy(alpha = 0.8f),
                    contentDescription = "طباعة الخريطة",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

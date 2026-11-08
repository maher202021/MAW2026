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
import androidx.compose.ui.text.font.FontWeight
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
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shadowElevation = 12.dp,
        modifier = modifier
            .width(56.dp)
            .heightIn(max = 600.dp)
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(vertical = 6.dp, horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Section 1: Navigation & GPS ---
            MapControlItem(
                icon = Icons.Default.MyLocation,
                label = "موقعي",
                isActive = isGpsActive,
                activeColor = Color(0xFF10B981),
                inactiveColor = Color.White.copy(alpha = 0.8f),
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onRecenterLocation()
                }
            )

            val isNavActive = currentLayer == "navigation"
            MapControlItem(
                icon = Icons.Default.Navigation,
                label = "ملاحة",
                isActive = isNavActive,
                activeColor = Color(0xFF10B981),
                inactiveColor = Color.White.copy(alpha = 0.8f),
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    if (isNavActive) onSwitchLayer("google_satellite") else onSwitchLayer("navigation")
                }
            )

            Divider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.width(28.dp))

            // --- Section 2: View Controls (Zoom & Layers) ---
            MapControlItem(
                icon = Icons.Default.Add,
                label = "تكبير",
                isActive = false,
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onZoomIn()
                }
            )

            MapControlItem(
                icon = Icons.Default.Remove,
                label = "تصغير",
                isActive = false,
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onZoomOut()
                }
            )

            MapControlItem(
                icon = if (isRadarMode) Icons.Default.Map else Icons.Default.Explore,
                label = if (isRadarMode) "خريطة" else "رادار",
                isActive = isRadarMode,
                activeColor = Color(0xFF10B981),
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onToggleRadarMode()
                }
            )

            MapControlItem(
                icon = Icons.Default.ViewInAr,
                label = "3D",
                isActive = tiltAngle > 0,
                activeColor = Color(0xFF10B981),
                onClick = onToggleTilt
            )

            MapControlItem(
                icon = Icons.Default.Layers,
                label = "طبقات",
                isActive = false,
                onClick = {
                    val nextLayer = when(currentLayer) {
                        "google_satellite" -> "osm"
                        "osm" -> "navigation"
                        "navigation" -> "streets"
                        else -> "google_satellite"
                    }
                    onSwitchLayer(nextLayer)
                }
            )

            Divider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.width(28.dp))

            // --- Section 3: Modes (Vela) ---
            com.serviseyem.ui.screens.map.MapMode.values().forEach { mode ->
                val isSelected = currentMode == mode
                val modeLabel = when(mode) {
                    com.serviseyem.ui.screens.map.MapMode.VELA_3D -> "فيلا"
                    com.serviseyem.ui.screens.map.MapMode.AERIAL_OAM -> "أقمار"
                    com.serviseyem.ui.screens.map.MapMode.AR_VPS -> "واقع معزز"
                    com.serviseyem.ui.screens.map.MapMode.NAVIGATION -> "ملاحة"
                    com.serviseyem.ui.screens.map.MapMode.ALEPH_AI -> "ألف"
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 1.dp)
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            if (mode == com.serviseyem.ui.screens.map.MapMode.ALEPH_AI) onOpenAleph() else onModeChange(mode)
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (isSelected) Color(0xFF10B981).copy(alpha = 0.3f) else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        Text(text = mode.icon, fontSize = 16.sp)
                    }
                    Text(
                        text = modeLabel,
                        fontSize = 8.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }

            Divider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.width(28.dp))

            // --- Section 4: Advanced Local Map Tools (GNSS, DeadZone, Night HUD, GPX) ---
            // 1. GNSS Live Satellites Signal Panel
            MapControlItem(
                icon = Icons.Default.SatelliteAlt,
                label = "أقمار",
                isActive = false,
                inactiveColor = themeColors.accent,
                onClick = onOpenGnssDiagnostics
            )

            // 2. Dead Zone Service Heatmap Overlay
            MapControlItem(
                icon = if (isDeadZoneActive) Icons.Default.GridOff else Icons.Default.GridOn,
                label = "تغطية",
                isActive = isDeadZoneActive,
                activeColor = Color(0xFFEF4444),
                onClick = onToggleDeadZoneOverlay
            )

            // 🥇 الفكرة الأولى: "خريطة الموثوقية" (Reliability Layer)
            MapControlItem(
                icon = Icons.Default.VerifiedUser,
                label = "موثوقية",
                isActive = isReliabilityActive,
                activeColor = Color(0xFF10B981),
                onClick = onToggleReliability
            )

            // 🎖️ الفكرة الخامسة: "الخريطة كأداة تخطيط للمسارات والمحطات" (Multi-Stop Route Planner)
            MapControlItem(
                icon = Icons.Default.Polyline,
                label = "مسار",
                isActive = false,
                inactiveColor = Color(0xFF00E5FF),
                onClick = onOpenRoutePlanner
            )

            // طبقة سلامة الطريق وصعوبات الطرق اليمنية (Road Safety Layer)
            MapControlItem(
                icon = Icons.Default.Warning,
                label = "سلامة",
                isActive = isRoadSafetyActive,
                activeColor = Color(0xFFF59E0B),
                onClick = onToggleRoadSafety
            )

            // 🏅 الفكرة الرابعة: إضافة معلم لتوأمة OpenStreetMap (OSM Node)
            MapControlItem(
                icon = Icons.Default.AddLocationAlt,
                label = "معلم",
                isActive = false,
                inactiveColor = Color(0xFF10B981),
                onClick = onOpenAddOsmLandmark
            )

            // 🥉 الفكرة الثالثة: زر نجدة طوارئ ريفية بدون إنترنت (Rural Emergency SMS)
            MapControlItem(
                icon = Icons.Default.Sos,
                label = "طوارئ",
                isActive = false,
                inactiveColor = Color(0xFFEF4444),
                onClick = onTriggerRuralSos
            )

            // 3. Night Mode / HUD
            MapControlItem(
                icon = if (isNightMode) Icons.Default.Bedtime else Icons.Default.WbSunny,
                label = "ليلي",
                isActive = isNightMode,
                activeColor = Color(0xFF818CF8),
                onClick = onToggleNightMode
            )

            // 4. Group Members Tracking
            MapControlItem(
                icon = Icons.Default.Group,
                label = "مجموعة",
                isActive = isGroupActive,
                activeColor = Color(0xFF10B981),
                onClick = onToggleGroupTracking
            )

            // 5. GPX Track Export / Import
            MapControlItem(
                icon = Icons.Default.AltRoute,
                label = "GPX",
                isActive = false,
                inactiveColor = Color(0xFF38BDF8),
                onClick = onExportGpx
            )

            // 6. Printable Map PDF Summary
            MapControlItem(
                icon = Icons.Default.Print,
                label = "طباعة",
                isActive = false,
                inactiveColor = Color.White.copy(alpha = 0.8f),
                onClick = onPrintMapPdf
            )
        }
    }
}

/**
 * عنصر تحكم مصغر وأنيق مع أيقونة واسم توضيحي أسفلها
 */
@Composable
private fun MapControlItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = Color(0xFF10B981),
    inactiveColor: Color = Color.White.copy(alpha = 0.75f),
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(34.dp)
                .background(
                    if (isActive) activeColor.copy(alpha = 0.3f) else Color.Transparent,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else inactiveColor,
                modifier = Modifier.size(19.dp)
            )
        }
        Text(
            text = label,
            fontSize = 8.5.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) activeColor else Color.White.copy(alpha = 0.75f),
            maxLines = 1
        )
    }
}

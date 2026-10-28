package com.serviseyem.ui.screens.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.screens.map.MapMode
import com.serviseyem.utils.VisualThemePalette

/**
 * 🎮 MapControls (مطابقة كاملة للشريط الرأسي العائم للأيقونات في الصورة 1)
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
    // Vela Modes & 3D Controls
    currentMode: MapMode,
    onModeChange: (MapMode) -> Unit,
    tiltAngle: Float,
    onToggleTilt: () -> Unit,
    onCycleTiltAngle: () -> Unit = {},
    onResetNorth: () -> Unit = {},
    onToggleTraffic: () -> Unit = {},
    onOpenStreetView: () -> Unit = {},
    onOpenAleph: () -> Unit = {},
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var showLayersDialog by remember { mutableStateOf(false) }

    if (showLayersDialog) {
        MapLayersDialog(
            currentLayer = currentLayer,
            onSelectLayer = { layerId ->
                onSwitchLayer(layerId)
                if (layerId == "vela_3d") {
                    onModeChange(MapMode.VELA_3D)
                } else {
                    onModeChange(MapMode.AERIAL_OAM)
                }
            },
            onDismiss = { showLayersDialog = false }
        )
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.94f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        shadowElevation = 14.dp,
        modifier = modifier
            .width(52.dp)
            .padding(vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. GPS My Location (موقعي الفعلي)
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onRecenterLocation()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "موقعي",
                    tint = if (isGpsActive) Color(0xFF10B981) else Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
            }

            // 2. Navigation Direction Arrow (سهم الاتجاه والملاحة)
            val isNavActive = currentMode == MapMode.NAVIGATION || currentLayer == "navigation"
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    if (isNavActive) onSwitchLayer("satellite") else onSwitchLayer("navigation")
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "الملاحة",
                    tint = if (isNavActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(19.dp)
                )
            }

            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.width(28.dp))

            // 3. Zoom In (+) التكبير التفاعلي العميق
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onZoomIn()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "تكبير", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // 4. Zoom Out (-) التصغير
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onZoomOut()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "تصغير", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // 5. Compass Needle (إعادة تدوير الخريطة للشمال)
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onResetNorth()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "البوصلة",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(19.dp)
                )
            }

            // 6. HUD / Radar Crosshair (وضع الرادار المتقدم)
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onToggleRadarMode()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = "الرادار",
                    tint = if (isRadarMode) Color(0xFF10B981) else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(19.dp)
                )
            }

            // 7. Map Layers (قائمة اختيار طبقة الخريطة - تفتح المودال)
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    showLayersDialog = true
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "طبقات الخريطة",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(20.dp)
                )
            }

            // 8. Vela 3D Perspective (مباني ثلاثية الأبعاد)
            val is3DActive = currentMode == MapMode.VELA_3D || tiltAngle > 0
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onToggleTilt()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = "Vela 3D",
                    tint = if (is3DActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // 9. Aerial / Satellite Toggle
            val isSat = currentLayer == "satellite" || currentLayer == "google_satellite"
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onSwitchLayer(if (isSat) "hot" else "satellite")
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Satellite,
                    contentDescription = "أقمار صناعية",
                    tint = if (isSat) Color(0xFF10B981) else Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(19.dp)
                )
            }

            // 10. Traffic / Live Street Layers
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onToggleTraffic()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Traffic,
                    contentDescription = "حركة السير والشوارع",
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(19.dp)
                )
            }

            // 11. 360° / Panorama View
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onOpenStreetView()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PanoramaHorizontal,
                    contentDescription = "رؤية بانورامية 360",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(19.dp)
                )
            }

            // 12. Eye / Angle of View (تحكم بزاوية الرؤية والإمالة)
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onCycleTiltAngle()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "زاوية الرؤية",
                    tint = if (tiltAngle > 0) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(19.dp)
                )
            }

            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.width(28.dp))

            // 13. Cloud Sync / Offline Status
            IconButton(
                onClick = onToggleHeatmap,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isHeatmapActive) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                    tint = if (isHeatmapActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.65f),
                    contentDescription = "أوفلاين ومزامنة",
                    modifier = Modifier.size(19.dp)
                )
            }

            // 14. Download Offline Map Tiles (تنزيل الخرائط أوفلاين)
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onDownloadValhallaClick()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    tint = Color(0xFF10B981),
                    contentDescription = "تحميل خريطة أوفلاين",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

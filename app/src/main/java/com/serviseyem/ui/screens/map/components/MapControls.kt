package com.serviseyem.ui.screens.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
    onOpenRegionsDialog: () -> Unit = {},
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
    isNasaGibsActive: Boolean = false,
    onToggleNasaGibs: () -> Unit = {},
    onLongPressNasaGibs: () -> Unit = {},
    isEoxCloudlessActive: Boolean = false,
    onToggleEoxCloudless: () -> Unit = {},
    isEsriImageryActive: Boolean = false,
    onToggleEsriImagery: () -> Unit = {},
    isEumetActive: Boolean = false,
    onToggleEumet: () -> Unit = {},
    onLongPressEumet: () -> Unit = {},
    onOpenMoreLayers: () -> Unit = {},
    isEarthquakeActive: Boolean = false,
    onToggleEarthquake: () -> Unit = {},
    isGroupActive: Boolean = false,
    onToggleGroupTracking: () -> Unit = {},
    onOpenGnssDiagnostics: () -> Unit = {},
    onExportGpx: () -> Unit = {},
    onPrintMapPdf: () -> Unit = {},
    defaultBaseLayer: String = "esri_world_imagery",
    orderedVisibleLayerIds: List<String> = com.serviseyem.data.models.MapLayersCatalog.ALL_LAYERS.map { it.id },
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

            val visibleSet = orderedVisibleLayerIds.toSet()

            if ("NAVIGATION" in visibleSet) {
                val isNavActive = currentLayer == "navigation"
                MapControlItem(
                    icon = Icons.Default.Navigation,
                    label = "ملاحة",
                    isActive = isNavActive,
                    activeColor = Color(0xFF10B981),
                    inactiveColor = Color.White.copy(alpha = 0.8f),
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        if (isNavActive) onSwitchLayer(defaultBaseLayer) else onSwitchLayer("navigation")
                    }
                )
            }

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

            if ("RADAR" in visibleSet) {
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
            }

            if ("VELA_3D" in visibleSet) {
                MapControlItem(
                    icon = Icons.Default.ViewInAr,
                    label = "3D",
                    isActive = tiltAngle > 0,
                    activeColor = Color(0xFF10B981),
                    onClick = onToggleTilt
                )
            }

            MapControlItem(
                icon = Icons.Default.Map,
                label = "الأساس",
                isActive = false,
                onClick = {
                    val nextLayer = when (currentLayer) {
                        "esri_world_imagery" -> "osm_ar"
                        "osm_ar", "osm" -> "opentopomap"
                        "opentopomap" -> "eox_cloudless"
                        else -> "esri_world_imagery"
                    }
                    onSwitchLayer(nextLayer)
                }
            )

            if ("OFFLINE_MAPS" in visibleSet) {
                MapControlItem(
                    icon = Icons.Default.DownloadForOffline,
                    label = "بلا نت",
                    isActive = false,
                    inactiveColor = Color(0xFF10B981),
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onOpenRegionsDialog()
                    }
                )
            }

            Divider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.width(28.dp))

            // --- Section 3 & 4: Ordered Admin-Controlled Modes, Layers & Map Tools ---
            val modeByLayerId = mapOf(
                "VELA_3D" to MapMode.VELA_3D,
                "AERIAL_OAM" to MapMode.AERIAL_OAM,
                "AR_VPS" to MapMode.AR_VPS,
                "NAVIGATION" to MapMode.NAVIGATION,
                "ALEPH_AI" to MapMode.ALEPH_AI
            )

            orderedVisibleLayerIds.forEach { layerId ->
                val mode = modeByLayerId[layerId]
                if (mode != null) {
                    val isSelected = currentMode == mode
                    val modeLabel = when (mode) {
                        MapMode.VELA_3D -> "فيلا"
                        MapMode.AERIAL_OAM -> "أقمار"
                        MapMode.AR_VPS -> "واقع معزز"
                        MapMode.NAVIGATION -> "ملاحة"
                        MapMode.ALEPH_AI -> "ألف"
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                if (mode == MapMode.ALEPH_AI) onOpenAleph() else onModeChange(mode)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (isSelected) Color(0xFF10B981).copy(alpha = 0.3f) else Color.Transparent,
                                        CircleShape
                                    )
                            ) {
                                Text(text = mode.icon, fontSize = 16.sp)
                            }
                        }
                        Text(
                            text = modeLabel,
                            fontSize = 8.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                } else {
                    when (layerId) {
                        "GNSS" -> MapControlItem(
                            icon = Icons.Default.SatelliteAlt,
                            label = "أقمار",
                            isActive = false,
                            inactiveColor = themeColors.accent,
                            onClick = onOpenGnssDiagnostics
                        )
                        "DEAD_ZONES" -> MapControlItem(
                            icon = if (isDeadZoneActive) Icons.Default.GridOff else Icons.Default.GridOn,
                            label = "تغطية",
                            isActive = isDeadZoneActive,
                            activeColor = Color(0xFFEF4444),
                            onClick = onToggleDeadZoneOverlay
                        )
                        "RELIABILITY" -> MapControlItem(
                            icon = Icons.Default.VerifiedUser,
                            label = "موثوقية",
                            isActive = isReliabilityActive,
                            activeColor = Color(0xFF10B981),
                            onClick = onToggleReliability
                        )
                        "HEATMAP" -> MapControlItem(
                            icon = Icons.Default.LocalFireDepartment,
                            label = "حرارية",
                            isActive = isHeatmapActive,
                            activeColor = Color(0xFFF97316),
                            onClick = onToggleHeatmap
                        )
                        "ROAD_SAFETY" -> MapControlItem(
                            icon = Icons.Default.Warning,
                            label = "سلامة",
                            isActive = isRoadSafetyActive,
                            activeColor = Color(0xFFF59E0B),
                            onClick = onToggleRoadSafety
                        )
                        "OSM_CONTRIBUTE" -> MapControlItem(
                            icon = Icons.Default.AddLocationAlt,
                            label = "معلم",
                            isActive = false,
                            inactiveColor = Color(0xFF10B981),
                            onClick = onOpenAddOsmLandmark
                        )
                        "RURAL_SOS" -> MapControlItem(
                            icon = Icons.Default.Sos,
                            label = "طوارئ",
                            isActive = false,
                            inactiveColor = Color(0xFFEF4444),
                            onClick = onTriggerRuralSos
                        )
                        "NIGHT_HUD" -> MapControlItem(
                            icon = if (isNightMode) Icons.Default.Bedtime else Icons.Default.WbSunny,
                            label = "ليلي",
                            isActive = isNightMode,
                            activeColor = Color(0xFF818CF8),
                            onClick = onToggleNightMode
                        )
                        "NASA_GIBS" -> MapControlItem(
                            icon = Icons.Default.Public,
                            label = "ناسا",
                            isActive = isNasaGibsActive,
                            activeColor = Color(0xFF38BDF8),
                            inactiveColor = Color(0xFF38BDF8).copy(alpha = 0.8f),
                            onClick = onToggleNasaGibs,
                            onLongClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onLongPressNasaGibs()
                            }
                        )
                        "EOX_CLOUDLESS" -> MapControlItem(
                            icon = Icons.Default.FilterHdr,
                            label = "EOX دقيقة",
                            isActive = isEoxCloudlessActive,
                            activeColor = Color(0xFF10B981),
                            inactiveColor = Color(0xFF10B981).copy(alpha = 0.8f),
                            onClick = onToggleEoxCloudless
                        )
                        "ESRI_IMAGERY" -> MapControlItem(
                            icon = Icons.Default.PhotoCamera,
                            label = "Esri دقيقة",
                            isActive = isEsriImageryActive,
                            activeColor = Color(0xFFF59E0B),
                            inactiveColor = Color(0xFFF59E0B).copy(alpha = 0.8f),
                            onClick = onToggleEsriImagery
                        )
                        "EUMETSAT" -> MapControlItem(
                            icon = Icons.Default.CloudQueue,
                            label = "ميتيوسات",
                            isActive = isEumetActive,
                            activeColor = Color(0xFF00E5FF),
                            inactiveColor = Color(0xFF00E5FF).copy(alpha = 0.8f),
                            onClick = onToggleEumet,
                            onLongClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onLongPressEumet()
                            }
                        )
                        "EARTHQUAKES" -> MapControlItem(
                            icon = Icons.Default.Sensors,
                            label = "زلازل",
                            isActive = isEarthquakeActive,
                            activeColor = Color(0xFFEF4444),
                            inactiveColor = Color(0xFFF97316),
                            onClick = onToggleEarthquake
                        )
                        "GROUP_TRACK" -> MapControlItem(
                            icon = Icons.Default.Group,
                            label = "مجموعة",
                            isActive = isGroupActive,
                            activeColor = Color(0xFF10B981),
                            onClick = onToggleGroupTracking
                        )
                        "GPX_EXPORT" -> MapControlItem(
                            icon = Icons.Default.AltRoute,
                            label = "GPX",
                            isActive = false,
                            inactiveColor = Color(0xFF38BDF8),
                            onClick = onExportGpx
                        )
                        "PDF_EXPORT" -> MapControlItem(
                            icon = Icons.Default.Print,
                            label = "طباعة",
                            isActive = false,
                            inactiveColor = Color.White.copy(alpha = 0.8f),
                            onClick = onPrintMapPdf
                        )
                    }
                }
            }

            if ("NAVIGATION" in visibleSet) {
                MapControlItem(
                    icon = Icons.Default.Polyline,
                    label = "مسار",
                    isActive = false,
                    inactiveColor = Color(0xFF00E5FF),
                    onClick = onOpenRoutePlanner
                )
            }

            MapControlItem(
                icon = Icons.Default.Layers,
                label = "طبقات",
                isActive = isEumetActive || isNasaGibsActive || isEoxCloudlessActive || isEsriImageryActive,
                activeColor = Color(0xFF38BDF8),
                inactiveColor = Color.White.copy(alpha = 0.75f),
                onClick = onOpenMoreLayers
            )
        }
    }
}

/**
 * عنصر تحكم مصغر وأنيق مع أيقونة واسم توضيحي أسفلها
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MapControlItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = Color(0xFF10B981),
    inactiveColor: Color = Color.White.copy(alpha = 0.75f),
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        if (onLongClick != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
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
            }
        } else {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
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
            }
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

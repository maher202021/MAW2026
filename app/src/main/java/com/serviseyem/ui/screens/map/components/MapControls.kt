package com.serviseyem.ui.screens.map.components

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.serviseyem.ui.screens.map.MapMode
import com.serviseyem.utils.VisualThemePalette

/**
 * Clean Google-Maps-style floating control clusters:
 * - Top-End: Layers/Tools sheet trigger + Radar/Map quick toggle
 * - Bottom-End: Zoom +/- pill + My Location FAB + Directions FAB
 */
@OptIn(ExperimentalFoundationApi::class)
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
    currentMode: MapMode,
    onModeChange: (MapMode) -> Unit,
    tiltAngle: Float,
    onToggleTilt: () -> Unit,
    onOpenAleph: () -> Unit,
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
    hasBottomOverlay: Boolean = false,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val visibleSet = orderedVisibleLayerIds.toSet()

    val anyExtraLayerActive = isEumetActive || isNasaGibsActive || isEoxCloudlessActive ||
        isEsriImageryActive || isHeatmapActive || isNightMode || isDeadZoneActive ||
        isReliabilityActive || isRoadSafetyActive || isEarthquakeActive

    val bottomClearance by animateDpAsState(
        targetValue = if (hasBottomOverlay) 216.dp else 20.dp,
        label = "map_controls_bottom_clearance"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Top-End Cluster: Layers & Tools Sheet + Quick Radar Switch
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 120.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // [نظام خرائط وملاحة عالمي - ملكنا]
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOpenMoreLayers()
                },
                shape = CircleShape,
                color = Color(0xFF0B1220).copy(alpha = 0.92f),
                border = BorderStroke(
                    1.dp,
                    if (anyExtraLayerActive) Color(0xFF5B86FF) else Color.White.copy(alpha = 0.16f)
                ),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("map_layers_fab")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "الطبقات والأدوات",
                        tint = if (anyExtraLayerActive) Color(0xFF00E5FF) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if ("RADAR" in visibleSet) {
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleRadarMode()
                    },
                    shape = CircleShape,
                    color = if (isRadarMode) Color(0xFF3A6BFF) else Color(0xFF0B1220).copy(alpha = 0.92f),
                    border = BorderStroke(
                        1.dp,
                        if (isRadarMode) Color(0xFF93C5FD) else Color.White.copy(alpha = 0.16f)
                    ),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("map_radar_fab")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isRadarMode) Icons.Default.Map else Icons.Default.Explore,
                            contentDescription = if (isRadarMode) "عرض الخريطة" else "وضع الرادار",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Bottom-End Thumb Cluster: Zoom +/-, My Location (long press for GNSS), Directions
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 12.dp, bottom = bottomClearance),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isRadarMode) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0B1220).copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.width(48.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onZoomIn()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("map_zoom_in_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "تكبير",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.width(26.dp)
                        )
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onZoomOut()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("map_zoom_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "تصغير",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // My Location FAB (tap = recenter GPS, long press = GNSS diagnostics)
            Surface(
                shape = CircleShape,
                color = Color(0xFF0B1220).copy(alpha = 0.94f),
                border = BorderStroke(
                    1.5.dp,
                    if (isGpsActive) Color(0xFF5B86FF) else Color.White.copy(alpha = 0.22f)
                ),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRecenterLocation()
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenGnssDiagnostics()
                        }
                    )
                    .testTag("map_my_location_fab")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "موقعي الحالي",
                        tint = if (isGpsActive) Color(0xFF00E5FF) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if ("NAVIGATION" in visibleSet) {
                val isNavActive = currentMode == MapMode.NAVIGATION || currentLayer == "navigation"
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isNavActive) {
                            onSwitchLayer(defaultBaseLayer)
                        } else {
                            onSwitchLayer("navigation")
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (isNavActive) Color(0xFF00E5FF) else Color(0xFF93C5FD).copy(alpha = 0.45f)
                    ),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                if (isNavActive) {
                                    listOf(Color(0xFF22D3EE), Color(0xFF0284C7), Color(0xFF1E3A8A))
                                } else {
                                    listOf(Color(0xFF5B86FF), Color(0xFF3A6BFF), Color(0xFF2851D8))
                                }
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("map_directions_fab")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isNavActive) Icons.Default.Close else Icons.Default.Directions,
                            contentDescription = if (isNavActive) "إغلاق الملاحة" else "الاتجاهات والملاحة",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

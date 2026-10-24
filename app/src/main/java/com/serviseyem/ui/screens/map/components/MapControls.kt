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
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Recenter Location Button (GPS Target)
        Surface(
            shape = CircleShape,
            color = Color(0xFF0F242A),
            border = BorderStroke(1.5.dp, Color(0xFF10B981)),
            shadowElevation = 6.dp,
            modifier = Modifier.size(46.dp)
        ) {
            IconButton(
                onClick = onRecenterLocation,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("recenter_location_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "موقعي الحالي",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 2. Zoom In Button (+)
        Surface(
            shape = CircleShape,
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shadowElevation = 6.dp,
            modifier = Modifier.size(46.dp)
        ) {
            IconButton(
                onClick = onZoomIn,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_zoom_in_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "تكبير",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 3. Zoom Out Button (-)
        Surface(
            shape = CircleShape,
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shadowElevation = 6.dp,
            modifier = Modifier.size(46.dp)
        ) {
            IconButton(
                onClick = onZoomOut,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_zoom_out_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "تصغير",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 4. Layer Switcher Button (تبديل الطبقات)
        Surface(
            shape = CircleShape,
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shadowElevation = 6.dp,
            modifier = Modifier.size(46.dp)
        ) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_layer_switch_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "تبديل طبقة الخريطة",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 4.5 Navigation Layer Direct Button (طبقة الملاحة)
        Surface(
            shape = CircleShape,
            color = if (currentLayer == "navigation") Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
            border = BorderStroke(1.dp, if (currentLayer == "navigation") Color(0xFF00E5FF) else Color(0xFF334155)),
            shadowElevation = 6.dp,
            modifier = Modifier.size(46.dp)
        ) {
            IconButton(
                onClick = { onSwitchLayer("navigation") },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_navigation_layer_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "طبقة الملاحة",
                    tint = if (currentLayer == "navigation") Color(0xFF00E5FF) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 4.6 Download Full Navigation Tiles (تحميل الملاحة الكاملة أوفلاين)
        Surface(
            shape = CircleShape,
            color = Color(0xFF0F242A),
            border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            shadowElevation = 6.dp,
            modifier = Modifier.size(46.dp)
        ) {
            IconButton(
                onClick = onDownloadValhallaClick,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("download_valhalla_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "تحميل الملاحة الكاملة أوفلاين (25MB)",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 5. Cloud Offline Cache Indicator Button (حفظ الأوفلاين)
        Surface(
            shape = CircleShape,
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            shadowElevation = 6.dp,
            modifier = Modifier.size(46.dp)
        ) {
            IconButton(
                onClick = onToggleHeatmap,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("map_offline_cache_btn")
            ) {
                Icon(
                    imageVector = if (isHeatmapActive) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                    contentDescription = "التخزين المحلي للأوفلاين",
                    tint = if (isHeatmapActive) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

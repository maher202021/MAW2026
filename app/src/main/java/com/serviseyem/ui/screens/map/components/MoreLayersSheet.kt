package com.serviseyem.ui.screens.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.screens.map.MapMode
import com.serviseyem.ui.screens.map.MapScreenState
import com.serviseyem.utils.EumetViewCatalog
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.utils.WmsLayerInfo

@Composable
fun Row3(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 16.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00E5FF),
                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.5f),
                uncheckedThumbColor = Color(0xFF64748B),
                uncheckedTrackColor = Color(0xFF334155)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MoreLayersSheet(
    state: MapScreenState,
    onDismiss: () -> Unit,
    disabledLayerIds: Set<String> = emptySet(),
    onSwitchBaseLayer: (String) -> Unit = { state.currentMapLayer = it },
    onToggleLabelsOverlay: (Boolean) -> Unit = {},
    onToggleTilt: () -> Unit = { state.tiltAngle = if (state.tiltAngle > 0f) 0f else 32f },
    onToggleNightMode: () -> Unit = { state.isNightModeActive = !state.isNightModeActive },
    onToggleRadarMode: () -> Unit = { state.isRadarMode = !state.isRadarMode },
    onModeChange: (MapMode) -> Unit = { state.currentMapMode = it },
    onOpenAleph: () -> Unit = { state.isAlephDialogOpen = true },
    onOpenRegionsDialog: () -> Unit = {},
    onOpenRoutePlanner: () -> Unit = { state.isRoutePlannerOpen = true },
    onOpenGnssDiagnostics: () -> Unit = { state.isGnssPanelOpen = true },
    onOpenAddOsmLandmark: () -> Unit = { state.isAddOsmDialogOpen = true },
    onTriggerRuralSos: () -> Unit = {},
    onExportGpx: () -> Unit = {},
    onPrintMapPdf: () -> Unit = {},
    onToggleHeatmap: () -> Unit = { state.isHeatmapActive = !state.isHeatmapActive },
    onToggleDeadZoneOverlay: () -> Unit = { state.isDeadZoneOverlayActive = !state.isDeadZoneOverlayActive },
    onToggleReliability: () -> Unit = { state.isReliabilityLayerActive = !state.isReliabilityLayerActive },
    onToggleRoadSafety: () -> Unit = { state.isRoadSafetyActive = !state.isRoadSafetyActive },
    onToggleEarthquake: () -> Unit = { state.isEarthquakeBottomSheetOpen = true },
    onToggleGroupTracking: () -> Unit = { state.isGroupTrackingActive = !state.isGroupTrackingActive },
    themeColors: VisualThemePalette
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var labelsOverlayEnabled by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B1220),
        contentColor = Color.White,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color.White.copy(alpha = 0.3f),
                width = 40.dp,
                height = 4.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "الخريطة والطبقات والأدوات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            // Section 1: Base Map Style Selector
            Text(
                text = "نوع الخريطة الأساسي",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BaseStyleTile(
                    title = "هجين فضائي",
                    subtitle = "قمر + أسماء",
                    emoji = "🛰️",
                    selected = state.currentMapLayer in setOf("esri_world_imagery", "eox_cloudless", "esri_wayback", "satellite", "imagery"),
                    onClick = { onSwitchBaseLayer("esri_world_imagery") },
                    modifier = Modifier.weight(1f)
                )
                BaseStyleTile(
                    title = "خريطة شوارع",
                    subtitle = "طرق ومعالم",
                    emoji = "🗺️",
                    selected = state.currentMapLayer in setOf("osm_ar", "osm", "navigation"),
                    onClick = { onSwitchBaseLayer("osm_ar") },
                    modifier = Modifier.weight(1f)
                )
                BaseStyleTile(
                    title = "تضاريس وجبال",
                    subtitle = "كنتور وارتفاعات",
                    emoji = "⛰️",
                    selected = state.currentMapLayer == "opentopomap",
                    onClick = { onSwitchBaseLayer("opentopomap") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row3(
                title = "تسميات الشوارع والمدن فوق الأقمار",
                subtitle = "إظهار أسماء الشوارع والأحياء والمعالم بوضوح فوق الصور الفضائية",
                checked = labelsOverlayEnabled
            ) { enabled ->
                labelsOverlayEnabled = enabled
                onToggleLabelsOverlay(enabled)
            }

            // [نظام خرائط وملاحة عالمي - ملكنا]
            Row3(
                title = "توفير البيانات (Data Saver)",
                subtitle = "تقليل دقة البلاطات وإيقاف الطبقات الفضائية الثقيلة لتوفير باقة الإنترنت",
                checked = state.isDataSaverActive
            ) { enabled ->
                state.isDataSaverActive = enabled
                if (enabled) {
                    state.eumetEnabled = false
                    state.isNasaGibsActive = false
                    state.isEoxCloudlessActive = false
                    state.isEsriImageryActive = false
                }
            }

            // Quick View Modes
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if ("VELA_3D" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.ViewInAr,
                        label = "منظور 3D",
                        active = state.tiltAngle > 0f,
                        activeColor = Color(0xFF10B981),
                        onClick = onToggleTilt
                    )
                }
                if ("NIGHT_HUD" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = if (state.isNightModeActive) Icons.Default.Bedtime else Icons.Default.WbSunny,
                        label = "الوضع الليلي",
                        active = state.isNightModeActive,
                        activeColor = Color(0xFF818CF8),
                        onClick = onToggleNightMode
                    )
                }
                if ("RADAR" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.Explore,
                        label = if (state.isRadarMode) "وضع الخريطة" else "الرادار الذكي",
                        active = state.isRadarMode,
                        activeColor = Color(0xFF10B981),
                        onClick = {
                            onToggleRadarMode()
                            onDismiss()
                        }
                    )
                }
                if ("AR_VPS" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.CameraAlt,
                        label = "واقع معزز AR",
                        active = state.currentMapMode == MapMode.AR_VPS,
                        activeColor = Color(0xFF00E5FF),
                        onClick = {
                            onModeChange(if (state.currentMapMode == MapMode.AR_VPS) MapMode.VELA_3D else MapMode.AR_VPS)
                            onDismiss()
                        }
                    )
                }
                if ("ALEPH_AI" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.Visibility,
                        label = "المساعد البصري",
                        active = false,
                        activeColor = Color(0xFF38BDF8),
                        onClick = {
                            onDismiss()
                            onOpenAleph()
                        }
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Section 2: Navigation & Field Tools
            Text(
                text = "أدوات الملاحة والخدمات الميدانية",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if ("OFFLINE_MAPS" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.DownloadForOffline,
                        label = "خرائط بلا نت",
                        active = false,
                        activeColor = Color(0xFF10B981),
                        onClick = {
                            onDismiss()
                            onOpenRegionsDialog()
                        }
                    )
                }
                if ("NAVIGATION" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.Polyline,
                        label = "مخطط المسار",
                        active = false,
                        activeColor = Color(0xFF00E5FF),
                        onClick = {
                            onDismiss()
                            onOpenRoutePlanner()
                        }
                    )
                }
                if ("GNSS" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.SatelliteAlt,
                        label = "إشارة GNSS",
                        active = false,
                        activeColor = themeColors.accent,
                        onClick = {
                            onDismiss()
                            onOpenGnssDiagnostics()
                        }
                    )
                }
                if ("OSM_CONTRIBUTE" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.AddLocationAlt,
                        label = "أضف معلماً",
                        active = false,
                        activeColor = Color(0xFF10B981),
                        onClick = {
                            onDismiss()
                            onOpenAddOsmLandmark()
                        }
                    )
                }
                if ("RURAL_SOS" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.Sos,
                        label = "طوارئ ريفية",
                        active = false,
                        activeColor = Color(0xFFEF4444),
                        onClick = {
                            onDismiss()
                            onTriggerRuralSos()
                        }
                    )
                }
                if ("HEATMAP" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "خريطة حرارية",
                        active = state.isHeatmapActive,
                        activeColor = Color(0xFFF97316),
                        onClick = onToggleHeatmap
                    )
                }
                if ("DEAD_ZONES" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.GridOff,
                        label = "ضعف التغطية",
                        active = state.isDeadZoneOverlayActive,
                        activeColor = Color(0xFFEF4444),
                        onClick = onToggleDeadZoneOverlay
                    )
                }
                if ("RELIABILITY" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.VerifiedUser,
                        label = "موثوقية الفنيين",
                        active = state.isReliabilityLayerActive,
                        activeColor = Color(0xFF10B981),
                        onClick = onToggleReliability
                    )
                }
                if ("ROAD_SAFETY" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.Warning,
                        label = "سلامة الطرق",
                        active = state.isRoadSafetyActive,
                        activeColor = Color(0xFFF59E0B),
                        onClick = onToggleRoadSafety
                    )
                }
                if ("EARTHQUAKES" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.Sensors,
                        label = "الزلازل الحية",
                        active = state.isEarthquakeLayerActive,
                        activeColor = Color(0xFFEF4444),
                        onClick = {
                            onDismiss()
                            onToggleEarthquake()
                        }
                    )
                }
                if ("GROUP_TRACK" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.Group,
                        label = "تتبع المجموعة",
                        active = state.isGroupTrackingActive,
                        activeColor = Color(0xFF10B981),
                        onClick = {
                            onDismiss()
                            onToggleGroupTracking()
                        }
                    )
                }
                if ("GPX_EXPORT" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.AltRoute,
                        label = "تصدير GPX",
                        active = false,
                        activeColor = Color(0xFF38BDF8),
                        onClick = {
                            onDismiss()
                            onExportGpx()
                        }
                    )
                }
                if ("PDF_EXPORT" !in disabledLayerIds) {
                    SheetToolChip(
                        icon = Icons.Default.Print,
                        label = "طباعة PDF",
                        active = false,
                        activeColor = Color.White,
                        onClick = {
                            onDismiss()
                            onPrintMapPdf()
                        }
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Section 3: Satellite & Live Weather Overlays
            Text(
                text = "طبقات الأقمار الصناعية والطقس الحية (مجانية 100%)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )

            if ("EUMETSAT" !in disabledLayerIds) {
                var layers by remember { mutableStateOf<List<WmsLayerInfo>?>(null) }
                var menu by remember { mutableStateOf(false) }

                LaunchedEffect(state.eumetEnabled) {
                    if (state.eumetEnabled && layers == null) {
                        layers = EumetViewCatalog.iodcLayers() ?: emptyList()
                    }
                }

                // [نظام خرائط وملاحة عالمي - ملكنا]
                Row3(
                    title = "قمر Meteosat مباشر (EUMETSAT)",
                    subtitle = "يتجدد كل ~15 دقيقة • سحب وغبار فوق اليمن • دقة ≈ 3 كم",
                    checked = state.eumetEnabled
                ) { active ->
                    state.eumetEnabled = active
                    if (active) {
                        state.isDataSaverActive = false
                        state.isNasaGibsActive = false
                        state.isEoxCloudlessActive = false
                        state.isEsriImageryActive = false
                    }
                }

                if (state.eumetEnabled) {
                    val list = layers
                    when {
                        list == null -> {
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text(
                                    text = "جاري استعلام الطبقات المتوفرة من EUMETView...",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFF00E5FF),
                                    trackColor = Color(0xFF1E293B)
                                )
                            }
                        }
                        list.isEmpty() -> {
                            Text(
                                text = "تعذر جلب قائمة الطبقات (تحتاج إنترنت).",
                                color = Color(0xFFF59E0B),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        else -> {
                            if (state.eumetLayerName.isEmpty()) {
                                state.eumetLayerName = (list.firstOrNull { it.title.contains("dust", true) } ?: list.first()).name
                            }
                            Box(modifier = Modifier.padding(vertical = 4.dp)) {
                                OutlinedButton(
                                    onClick = { menu = true },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF00E5FF),
                                        containerColor = Color(0xFF00E5FF).copy(alpha = 0.08f)
                                    )
                                ) {
                                    Icon(Icons.Default.Satellite, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = list.firstOrNull { it.name == state.eumetLayerName }?.arabicLabel() ?: "اختر طبقة",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = menu,
                                    onDismissRequest = { menu = false },
                                    modifier = Modifier
                                        .background(Color(0xFF1E293B))
                                        .widthIn(min = 220.dp, max = 320.dp)
                                ) {
                                    list.forEach { l ->
                                        val isCurrent = l.name == state.eumetLayerName
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = l.arabicLabel(),
                                                    color = if (isCurrent) Color(0xFF00E5FF) else Color.White,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp
                                                )
                                            },
                                            onClick = {
                                                state.eumetLayerName = l.name
                                                menu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            }

            if ("NASA_GIBS" !in disabledLayerIds) {
                Row3(
                    title = "ناسا اليومية شبه الفورية (NASA GIBS)",
                    subtitle = "تأخير ≈ 3 ساعات • VIIRS NOAA-20 / SNPP / MODIS • دقة 250م",
                    checked = state.isNasaGibsActive
                ) { active ->
                    state.isNasaGibsActive = active
                    if (active) {
                        state.isDataSaverActive = false
                        state.eumetEnabled = false
                        state.isEoxCloudlessActive = false
                        state.isEsriImageryActive = false
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            }

            if ("EOX_CLOUDLESS" !in disabledLayerIds) {
                Row3(
                    title = "EOX Cloudless (Sentinel-2)",
                    subtitle = "صور فضائية فائقة الدقة 10 أمتار بدون غيوم لعموم اليمن",
                    checked = state.isEoxCloudlessActive
                ) { active ->
                    state.isEoxCloudlessActive = active
                    if (active) {
                        state.isDataSaverActive = false
                        state.eumetEnabled = false
                        state.isNasaGibsActive = false
                        state.isEsriImageryActive = false
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            }

            if ("ESRI_IMAGERY" !in disabledLayerIds) {
                Row3(
                    title = "صور Esri الفضائية فائقة الدقة",
                    subtitle = "تغطية عالمية وتفصيلية للمدن والشوارع",
                    checked = state.isEsriImageryActive
                ) { active ->
                    state.isEsriImageryActive = active
                    if (active) {
                        state.isDataSaverActive = false
                        state.eumetEnabled = false
                        state.isNasaGibsActive = false
                        state.isEoxCloudlessActive = false
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Attribution Footer
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.04f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "حقوق البيانات والإسناد: © OpenStreetMap • EUMETSAT • NASA GIBS • Sentinel-2 by EOX • Esri World Imagery",
                    fontSize = 10.5.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun BaseStyleTile(
    title: String,
    subtitle: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color(0xFF3A6BFF).copy(alpha = 0.22f) else Color(0xFF14202E),
        border = BorderStroke(
            1.5.dp,
            if (selected) Color(0xFF5B86FF) else Color.White.copy(alpha = 0.12f)
        ),
        modifier = modifier.defaultMinSize(minHeight = 68.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = title,
                color = if (selected) Color(0xFF93C5FD) else Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 9.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SheetToolChip(
    icon: ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (active) activeColor.copy(alpha = 0.22f) else Color(0xFF1E293B),
        border = BorderStroke(
            1.dp,
            if (active) activeColor else Color.White.copy(alpha = 0.12f)
        ),
        modifier = Modifier.defaultMinSize(minHeight = 44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) activeColor else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = label,
                color = if (active) activeColor else Color.White,
                fontSize = 12.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

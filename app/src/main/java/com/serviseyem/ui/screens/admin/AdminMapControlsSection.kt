package com.serviseyem.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.AdminSettingsEntity
import com.serviseyem.data.models.MapLayerSpec
import com.serviseyem.data.models.MapLayersCatalog
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.updateAdminSettings
import com.serviseyem.ui.screens.admin.components.adminSwitchColors
import com.serviseyem.utils.VisualThemePalette

@Composable
fun AdminMapControlsSection(
    viewModel: MainViewModel,
    settingsState: AdminSettingsEntity,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current

    var isMapFeatureEnabled by remember(settingsState.isMapFeatureEnabled) {
        mutableStateOf(settingsState.isMapFeatureEnabled)
    }
    var showMapToVisitors by remember(settingsState.showMapToVisitors) {
        mutableStateOf(settingsState.showMapToVisitors)
    }
    var defaultBaseLayer by remember(settingsState.defaultMapBaseLayer) {
        mutableStateOf(
            when (settingsState.defaultMapBaseLayer) {
                "esri_world_imagery", "osm_ar", "eox_cloudless", "opentopomap" -> settingsState.defaultMapBaseLayer
                else -> "esri_world_imagery"
            }
        )
    }
    var showProvidersOnMap by remember(settingsState.showProvidersOnMap) {
        mutableStateOf(settingsState.showProvidersOnMap)
    }
    var showStoresOnMap by remember(settingsState.showStoresOnMap) {
        mutableStateOf(settingsState.showStoresOnMap)
    }
    var showRestaurantsOnMap by remember(settingsState.showRestaurantsOnMap) {
        mutableStateOf(settingsState.showRestaurantsOnMap)
    }
    var showMedicalOnMap by remember(settingsState.showMedicalOnMap) {
        mutableStateOf(settingsState.showMedicalOnMap)
    }
    var showPropertiesOnMap by remember(settingsState.showPropertiesOnMap) {
        mutableStateOf(settingsState.showPropertiesOnMap)
    }
    var showSatMenuOnMap by remember(settingsState.showSatMenuOnMap) {
        mutableStateOf(settingsState.showSatMenuOnMap)
    }
    var showMeasureToolOnMap by remember(settingsState.showMeasureToolOnMap) {
        mutableStateOf(settingsState.showMeasureToolOnMap)
    }
    var mapMaxZoom by remember(settingsState.mapMaxZoom) {
        mutableIntStateOf(settingsState.mapMaxZoom.coerceIn(14, 22))
    }
    var mapMaxDistanceKm by remember(settingsState.mapMaxDistanceKm) {
        mutableIntStateOf(settingsState.mapMaxDistanceKm.coerceIn(5, 100))
    }

    var orderedLayers by remember(settingsState.mapLayersOrder) {
        mutableStateOf(MapLayersCatalog.parseFullOrderList(settingsState.mapLayersOrder))
    }
    var disabledLayers by remember(settingsState.disabledMapLayers) {
        mutableStateOf(
            settingsState.disabledMapLayers
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toSet()
        )
    }

    LaunchedEffect(settingsState) {
        isMapFeatureEnabled = settingsState.isMapFeatureEnabled
        showMapToVisitors = settingsState.showMapToVisitors
        defaultBaseLayer = when (settingsState.defaultMapBaseLayer) {
            "esri_world_imagery", "osm_ar", "eox_cloudless", "opentopomap" -> settingsState.defaultMapBaseLayer
            else -> "esri_world_imagery"
        }
        showProvidersOnMap = settingsState.showProvidersOnMap
        showStoresOnMap = settingsState.showStoresOnMap
        showRestaurantsOnMap = settingsState.showRestaurantsOnMap
        showMedicalOnMap = settingsState.showMedicalOnMap
        showPropertiesOnMap = settingsState.showPropertiesOnMap
        showSatMenuOnMap = settingsState.showSatMenuOnMap
        showMeasureToolOnMap = settingsState.showMeasureToolOnMap
        mapMaxZoom = settingsState.mapMaxZoom.coerceIn(14, 22)
        mapMaxDistanceKm = settingsState.mapMaxDistanceKm.coerceIn(5, 100)
        orderedLayers = MapLayersCatalog.parseFullOrderList(settingsState.mapLayersOrder)
        disabledLayers = settingsState.disabledMapLayers
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    fun persistMapSettings(toastMessage: String = "✅ تم حفظ ومزامنة إعدادات وطبقات الخريطة مع Firestore") {
        val updated = settingsState.copy(
            isMapFeatureEnabled = isMapFeatureEnabled,
            mapProvider = "LEAFLET_OSM",
            defaultMapBaseLayer = defaultBaseLayer,
            showMapToVisitors = showMapToVisitors,
            showProvidersOnMap = showProvidersOnMap,
            showStoresOnMap = showStoresOnMap,
            showRestaurantsOnMap = showRestaurantsOnMap,
            showMedicalOnMap = showMedicalOnMap,
            showPropertiesOnMap = showPropertiesOnMap,
            mapLayersOrder = orderedLayers.joinToString(",") { it.id },
            disabledMapLayers = disabledLayers.joinToString(","),
            mapMaxZoom = mapMaxZoom,
            mapMaxDistanceKm = mapMaxDistanceKm,
            showSatMenuOnMap = showSatMenuOnMap,
            showMeasureToolOnMap = showMeasureToolOnMap
        )
        viewModel.updateAdminSettings(updated)
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Base Engine & General Visibility
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = themeColors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "محرك الخرائط المفتوح والطبقة الافتراضية",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "محرك حر 100% (Leaflet + OSM + Esri + EOX + NASA + EUMETSAT) — بدون خرائط جوجل",
                            color = themeColors.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                val baseLayers = listOf(
                    Triple("esri_world_imagery", "أقمار Esri فائقة الدقة", "🛰️"),
                    Triple("osm_ar", "شوارع اليمن OSM", "🌐"),
                    Triple("eox_cloudless", "EOX فضائي صافي", "🌍"),
                    Triple("opentopomap", "تضاريس OpenTopo", "⛰️")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    baseLayers.forEach { (key, title, emoji) ->
                        val isSelected = defaultBaseLayer == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) themeColors.accent else Color(0xFF0F172A)
                                )
                                .clickable { defaultBaseLayer = key }
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                                .testTag("admin_map_base_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(emoji, fontSize = 14.sp)
                                Text(
                                    text = title,
                                    fontSize = 9.5.sp,
                                    lineHeight = 12.sp,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                AdminMapToggleRow(
                    title = "تفعيل ميزة الخرائط في المنصة بالكامل",
                    subtitle = "عند الإيقاف تختفي الخرائط عن غير الإداريين",
                    checked = isMapFeatureEnabled,
                    onCheckedChange = { isMapFeatureEnabled = it },
                    themeColors = themeColors,
                    testTag = "admin_map_feature_switch"
                )

                AdminMapToggleRow(
                    title = "إظهار الخريطة للزوار قبل التسجيل",
                    subtitle = "السماح للزوار غير المسجلين بتصفح الخريطة",
                    checked = showMapToVisitors,
                    onCheckedChange = { showMapToVisitors = it },
                    themeColors = themeColors,
                    testTag = "admin_map_visitors_switch"
                )

                AdminMapToggleRow(
                    title = "إظهار قائمة طبقات الأقمار المنسدلة على الخريطة",
                    subtitle = "التحكم في زر اختيار الطبقات الفضائية السفلي",
                    checked = showSatMenuOnMap,
                    onCheckedChange = { showSatMenuOnMap = it },
                    themeColors = themeColors,
                    testTag = "admin_map_sat_menu_switch"
                )

                AdminMapToggleRow(
                    title = "إظهار مسطرة قياس المسافات الحية",
                    subtitle = "أداة قياس المسافة بين النقاط على الخريطة",
                    checked = showMeasureToolOnMap,
                    onCheckedChange = { showMeasureToolOnMap = it },
                    themeColors = themeColors,
                    testTag = "admin_map_measure_switch"
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("أقصى مستوى تكبير مسموح (Max Zoom):", color = Color.White, fontSize = 12.sp)
                        Text("Z$mapMaxZoom", color = themeColors.accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = mapMaxZoom.toFloat(),
                        onValueChange = { mapMaxZoom = it.toInt().coerceIn(14, 22) },
                        valueRange = 14f..22f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.accent,
                            activeTrackColor = themeColors.accent
                        )
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("نطاق المسح الجغرافي الافتراضي (الرادار):", color = Color.White, fontSize = 12.sp)
                        Text("$mapMaxDistanceKm كم", color = themeColors.accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = mapMaxDistanceKm.toFloat(),
                        onValueChange = { mapMaxDistanceKm = it.toInt().coerceIn(5, 100) },
                        valueRange = 5f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.accent,
                            activeTrackColor = themeColors.accent
                        )
                    )
                }
            }
        }

        // 2. Entity Visibility Switches on Map
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "التحكم بظهور الجهات والأقسام على الخريطة",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                AdminMapToggleRow(
                    title = "👷 إظهار الفنيين ومقدمي الخدمات",
                    subtitle = "علامات الفنيين والمهنيين المتاحين",
                    checked = showProvidersOnMap,
                    onCheckedChange = { showProvidersOnMap = it },
                    themeColors = themeColors,
                    testTag = "admin_map_providers_switch"
                )
                AdminMapToggleRow(
                    title = "🏪 إظهار المحلات والمراكز التجارية",
                    subtitle = "المتاجر والأسواق التجارية على الخريطة",
                    checked = showStoresOnMap,
                    onCheckedChange = { showStoresOnMap = it },
                    themeColors = themeColors,
                    testTag = "admin_map_stores_switch"
                )
                AdminMapToggleRow(
                    title = "🍽️ إظهار المطاعم والكافيهات",
                    subtitle = "المطاعم ونقاط الوجبات السريعة",
                    checked = showRestaurantsOnMap,
                    onCheckedChange = { showRestaurantsOnMap = it },
                    themeColors = themeColors,
                    testTag = "admin_map_restaurants_switch"
                )
                AdminMapToggleRow(
                    title = "🏥 إظهار المراكز الطبية والعيادات",
                    subtitle = "المستشفيات والعيادات والصيدليات",
                    checked = showMedicalOnMap,
                    onCheckedChange = { showMedicalOnMap = it },
                    themeColors = themeColors,
                    testTag = "admin_map_medical_switch"
                )
                AdminMapToggleRow(
                    title = "🏢 إظهار العقارات والأراضي",
                    subtitle = "عروض البيع والإيجار العقارية",
                    checked = showPropertiesOnMap,
                    onCheckedChange = { showPropertiesOnMap = it },
                    themeColors = themeColors,
                    testTag = "admin_map_properties_switch"
                )
            }
        }

        // 3. All 24 Map Layers / Modes / Tools (Toggle + Reorder Up/Down)
        Card(
            colors = CardDefaults.cardColors(containerColor = themeColors.surface),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "إدارة وترتيب كافة طبقات وأدوات الخريطة (${orderedLayers.size} طبقة)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "المفعل: ${orderedLayers.size - disabledLayers.size} | المعطل: ${disabledLayers.size}",
                                color = themeColors.textSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { disabledLayers = emptySet() },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("admin_map_enable_all_layers_btn"),
                        border = BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Text("تفعيل جميع الطبقات", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            orderedLayers = MapLayersCatalog.ALL_LAYERS
                            disabledLayers = emptySet()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("admin_map_reset_layers_btn"),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "الترتيب الافتراضي",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الترتيب الافتراضي", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                orderedLayers.forEachIndexed { index, layer ->
                    val isEnabled = layer.id !in disabledLayers
                    MapLayerControlRow(
                        layer = layer,
                        index = index,
                        total = orderedLayers.size,
                        isEnabled = isEnabled,
                        themeColors = themeColors,
                        onToggleEnabled = { enabled ->
                            disabledLayers = if (enabled) {
                                disabledLayers - layer.id
                            } else {
                                disabledLayers + layer.id
                            }
                        },
                        onMoveUp = {
                            if (index > 0) {
                                val mutable = orderedLayers.toMutableList()
                                val item = mutable.removeAt(index)
                                mutable.add(index - 1, item)
                                orderedLayers = mutable
                            }
                        },
                        onMoveDown = {
                            if (index < orderedLayers.lastIndex) {
                                val mutable = orderedLayers.toMutableList()
                                val item = mutable.removeAt(index)
                                mutable.add(index + 1, item)
                                orderedLayers = mutable
                            }
                        }
                    )
                }
            }
        }

        Button(
            onClick = { persistMapSettings() },
            colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
            contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp)
                .testTag("admin_map_save_button")
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "حفظ ومزامنة جميع إعدادات وطبقات الخرائط سحابياً",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun AdminMapToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    themeColors: VisualThemePalette,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.55f))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFF94A3B8), fontSize = 10.sp, lineHeight = 13.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = adminSwitchColors(themeColors),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun MapLayerControlRow(
    layer: MapLayerSpec,
    index: Int,
    total: Int,
    isEnabled: Boolean,
    themeColors: VisualThemePalette,
    onToggleEnabled: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Surface(
        color = if (isEnabled) Color(0xFF0F172A).copy(alpha = 0.85f) else Color(0xFF0F172A).copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isEnabled) themeColors.accent.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("layer_up_${layer.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "تحريك للأعلى",
                        tint = if (index > 0) Color(0xFF38BDF8) else Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = index < total - 1,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("layer_down_${layer.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "تحريك للأسفل",
                        tint = if (index < total - 1) Color(0xFF38BDF8) else Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(layer.emoji, fontSize = 15.sp)
                    Text(
                        text = "${index + 1}. ${layer.titleAr}",
                        color = if (isEnabled) Color.White else Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = layer.descriptionAr,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggleEnabled,
                colors = adminSwitchColors(themeColors),
                modifier = Modifier.testTag("layer_switch_${layer.id}")
            )
        }
    }
}

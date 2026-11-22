package com.serviseyem.ui.screens.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreLayersSheet(
    state: MapScreenState,
    onDismiss: () -> Unit,
    disabledLayerIds: Set<String> = emptySet(),
    themeColors: VisualThemePalette
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
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
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
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
                        text = "طبقات الأقمار الصناعية والطقس",
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

            Text(
                text = "جميع الطبقات مجانية ومباشرة بدون الحاجة لأي مفاتيح أو اشتراكات 🛰️",
                fontSize = 12.sp,
                color = Color(0xFF38BDF8),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            if ("EUMETSAT" !in disabledLayerIds) {
                // 1. Meteosat Live (EUMETSAT IODC 41.5°E)
                var layers by remember { mutableStateOf<List<WmsLayerInfo>?>(null) }
                var menu by remember { mutableStateOf(false) }

                LaunchedEffect(state.eumetEnabled) {
                    if (state.eumetEnabled && layers == null) {
                        layers = EumetViewCatalog.iodcLayers() ?: emptyList()
                    }
                }

                Row3(
                    title = "قمر Meteosat مباشر (EUMETSAT)",
                    subtitle = "يتجدد كل ~15 دقيقة • سحب وغبار فوق اليمن • دقة ≈ 3 كم",
                    checked = state.eumetEnabled
                ) {
                    state.eumetEnabled = it
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
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
            }

            if ("NASA_GIBS" !in disabledLayerIds) {
                // 2. NASA GIBS Live Satellites
                Row3(
                    title = "ناسا اليومية شبه الفورية (NASA GIBS)",
                    subtitle = "تأخير ≈ 3 ساعات • VIIRS NOAA-20 / SNPP / MODIS • دقة 250م",
                    checked = state.isNasaGibsActive
                ) { active ->
                    state.isNasaGibsActive = active
                    if (active) {
                        state.isEoxCloudlessActive = false
                        state.isEsriImageryActive = false
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
            }

            if ("EOX_CLOUDLESS" !in disabledLayerIds) {
                // 3. EOX Cloudless 2023 Sentinel-2
                Row3(
                    title = "EOX Cloudless (Sentinel-2)",
                    subtitle = "صور فضائية فائقة الدقة 10 أمتار بدون غيوم لعموم اليمن",
                    checked = state.isEoxCloudlessActive
                ) { active ->
                    state.isEoxCloudlessActive = active
                    if (active) {
                        state.isNasaGibsActive = false
                        state.isEsriImageryActive = false
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
            }

            if ("ESRI_IMAGERY" !in disabledLayerIds) {
                // 4. Esri World Imagery
                Row3(
                    title = "صور Esri الفضائية فائقة الدقة",
                    subtitle = "تغطية عالمية وتفصيلية للمدن والشوارع",
                    checked = state.isEsriImageryActive
                ) { active ->
                    state.isEsriImageryActive = active
                    if (active) {
                        state.isNasaGibsActive = false
                        state.isEoxCloudlessActive = false
                    }
                }
            }

            if (listOf("EUMETSAT", "NASA_GIBS", "EOX_CLOUDLESS", "ESRI_IMAGERY").all { it in disabledLayerIds }) {
                Text(
                    text = "تم تعطيل طبقات الأقمار الصناعية الإضافية من لوحة التحكم.",
                    fontSize = 12.sp,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Attribution Footer
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.04f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "حقوق البيانات والإسناد: © EUMETSAT (Meteosat IODC) • NASA EOSDIS GIBS • Sentinel-2 by EOX • Esri World Imagery",
                    fontSize = 10.5.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

package com.serviseyem.ui.screens.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.serviseyem.utils.RegionPack
import com.serviseyem.utils.RegionPackManager

@Composable
fun RegionsDialog(
    onDismiss: () -> Unit,
    onRegionsChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context.applicationContext) }

    var wifiOnly by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var packs by remember { mutableStateOf<List<RegionPack>>(emptyList()) }
    var installedIds by remember { mutableStateOf(RegionPackManager.installedIds(context)) }
    var refreshTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTick) {
        isLoading = true
        errorText = null
        val url = RegionPackManager.configuredManifestUrl(context)
        val result = RegionPackManager.fetchManifest(url)
        isLoading = false
        result.onSuccess {
            packs = it
        }.onFailure { err ->
            errorText = err.localizedMessage ?: "تعذر تحميل قائمة خرائط المناطق"
        }
        installedIds = RegionPackManager.installedIds(context)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(18.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "خرائط بلا إنترنت",
                        tint = Color(0xFF10B981)
                    )
                    Text(
                        text = "خرائط بلا إنترنت",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "© مساهمو OpenStreetMap (MBTiles)",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = wifiOnly,
                        onCheckedChange = { wifiOnly = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF10B981),
                            uncheckedColor = Color(0xFF64748B),
                            checkmarkColor = Color.Black
                        ),
                        modifier = Modifier.testTag("regions_wifi_only_checkbox")
                    )
                    Text(
                        text = "عبر Wi-Fi فقط",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(color = Color(0xFF10B981))
                                Text(
                                    text = "جاري جلب حزم المناطق المتاحة...",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }

                    errorText != null -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = errorText.orEmpty(),
                                    color = Color(0xFFFCD34D),
                                    fontSize = 12.5.sp,
                                    lineHeight = 19.sp
                                )
                                if (installedIds.isNotEmpty()) {
                                    Text(
                                        text = "المناطق المثبتة محلياً: ${installedIds.joinToString("، ")}",
                                        color = Color(0xFFA7F3D0),
                                        fontSize = 11.5.sp
                                    )
                                }
                                OutlinedButton(
                                    onClick = { refreshTick++ },
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                    modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "إعادة المحاولة",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تحديث القائمة", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    packs.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد حزم مناطق متاحة حالياً.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                                .testTag("regions_packs_list"),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(packs, key = { _, pack -> pack.id }) { idx, pack ->
                                val workInfos by workManager
                                    .getWorkInfosForUniqueWorkFlow(RegionPackManager.workName(pack.id))
                                    .collectAsState(initial = emptyList())
                                val activeInfo = workInfos.firstOrNull()
                                val workState = activeInfo?.state
                                val isDownloading = workState == WorkInfo.State.RUNNING || workState == WorkInfo.State.ENQUEUED
                                val progressPct = activeInfo?.progress?.getInt("p", 0) ?: 0

                                LaunchedEffect(workState) {
                                    if (workState == WorkInfo.State.SUCCEEDED) {
                                        installedIds = RegionPackManager.installedIds(context)
                                        onRegionsChanged()
                                    }
                                }

                                val isInstalled = pack.id in installedIds || RegionPackManager.isInstalled(context, pack.id)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pack.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "${pack.sizeMb} م.ب • التكبير Z${pack.minZoom}–Z${pack.maxZoom}",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.5.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        when {
                                            isDownloading -> {
                                                TextButton(
                                                    onClick = { RegionPackManager.cancel(context, pack.id) },
                                                    modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                                                ) {
                                                    Text("إلغاء", color = Color(0xFFEF4444), fontSize = 12.sp)
                                                }
                                            }

                                            isInstalled -> {
                                                OutlinedButton(
                                                    onClick = {
                                                        RegionPackManager.delete(context, pack.id)
                                                        installedIds = RegionPackManager.installedIds(context)
                                                        onRegionsChanged()
                                                    },
                                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f)),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "حذف",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("حذف", color = Color(0xFFEF4444), fontSize = 12.sp)
                                                }
                                            }

                                            else -> {
                                                Button(
                                                    onClick = { RegionPackManager.enqueue(context, pack, wifiOnly) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CloudDownload,
                                                        contentDescription = "تنزيل",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("تنزيل", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }

                                    if (isDownloading) {
                                        LinearProgressIndicator(
                                            progress = { (progressPct / 100f).coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp),
                                            color = Color(0xFF10B981),
                                            trackColor = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = if (workState == WorkInfo.State.ENQUEUED) {
                                                "في الانتظار للتنزيل…"
                                            } else {
                                                "جاري التنزيل… $progressPct%"
                                            },
                                            color = Color(0xFF38BDF8),
                                            fontSize = 11.sp
                                        )
                                    } else if (workState == WorkInfo.State.FAILED) {
                                        Text(
                                            text = activeInfo?.outputData?.getString("error")
                                                ?: "تعذر إكمال التنزيل، أعد المحاولة.",
                                            color = Color(0xFFEF4444),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (idx < packs.lastIndex) {
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("regions_close_button")
            ) {
                Text("إغلاق", color = Color.White.copy(alpha = 0.85f))
            }
        }
    )
}

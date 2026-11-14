package com.serviseyem.ui.screens.map

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.serviseyem.utils.InstalledRegionMarker
import com.serviseyem.utils.RegionPack
import com.serviseyem.utils.RegionPackManager
import kotlinx.coroutines.launch

@Composable
fun RegionsDialog(
    onDismiss: () -> Unit,
    onRegionsChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val workManager = remember { WorkManager.getInstance(context.applicationContext) }

    var wifiOnly by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var packs by remember { mutableStateOf<List<RegionPack>>(emptyList()) }
    var installedRegions by remember { mutableStateOf<List<InstalledRegionMarker>>(RegionPackManager.listInstalled(context)) }
    var installedIds by remember { mutableStateOf(RegionPackManager.installedIds(context)) }
    var siteUrlInput by remember { mutableStateOf(RegionPackManager.configuredManifestUrl(context)) }
    var refreshTick by remember { mutableIntStateOf(0) }

    var isImportingLocal by remember { mutableStateOf(false) }
    var importProgress by remember { mutableFloatStateOf(0f) }
    var importStatus by remember { mutableStateOf("") }

    val allRegionWorkInfos by workManager
        .getWorkInfosByTagFlow("region_pack")
        .collectAsState(initial = emptyList())

    val activeGlobalDownloads = remember(allRegionWorkInfos) {
        allRegionWorkInfos.filter {
            it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
        }
    }

    LaunchedEffect(allRegionWorkInfos) {
        if (allRegionWorkInfos.any { it.state == WorkInfo.State.SUCCEEDED }) {
            installedRegions = RegionPackManager.listInstalled(context)
            installedIds = RegionPackManager.installedIds(context)
            onRegionsChanged()
        }
    }

    fun refreshLocalState() {
        installedRegions = RegionPackManager.listInstalled(context)
        installedIds = RegionPackManager.installedIds(context)
    }

    val mbtilesPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        isImportingLocal = true
        importProgress = 0.05f
        importStatus = "جاري تحضير استيراد الخريطة من ذاكرة الهاتف..."
        errorText = null
        statusMessage = null
        coroutineScope.launch {
            val result = RegionPackManager.importFromUri(context, uri) { prog, msg ->
                importProgress = prog
                importStatus = msg
            }
            isImportingLocal = false
            result.onSuccess { marker ->
                refreshLocalState()
                onRegionsChanged()
                statusMessage = "تم تثبيت خريطة \"${marker.name}\" (${marker.sizeMb} م.ب) بنجاح ✓"
            }.onFailure { err ->
                errorText = err.localizedMessage ?: "تعذر استيراد ملف الخريطة المختار"
            }
        }
    }

    LaunchedEffect(refreshTick) {
        val url = siteUrlInput.trim().ifBlank { RegionPackManager.configuredManifestUrl(context) }
        refreshLocalState()
        if (url.isBlank()) {
            isLoading = false
            packs = emptyList()
            return@LaunchedEffect
        }
        val cleanWithoutQuery = url.lowercase().substringBefore('?')
        if (cleanWithoutQuery.endsWith(".mbtiles")) {
            // Direct .mbtiles link is handled via the Download button, don't auto-fetch as JSON
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        errorText = null
        val result = RegionPackManager.fetchManifest(url)
        isLoading = false
        result.onSuccess {
            packs = it
        }.onFailure { err ->
            errorText = err.localizedMessage ?: "تعذر تحميل قائمة خرائط المناطق"
        }
        refreshLocalState()
    }

    AlertDialog(
        onDismissRequest = { if (!isImportingLocal) onDismiss() },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .testTag("regions_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status / Feedback banner
                statusMessage?.let { msg ->
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF064E3B).copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                color = Color(0xFFA7F3D0),
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                errorText?.let { err ->
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                color = Color(0xFFFCD34D),
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // الخيار 1: الإضافة من ذاكرة الهاتف
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📁 الخيار الأول: إضافة ملف من ذاكرة الهاتف",
                                color = Color(0xFF7DD3FC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = "اختر ملف خريطة (.mbtiles) محفوظ على جهازك وسيتم نسخه وتفعيله فوراً للعمل بلا إنترنت دون استهلاك الباقة.",
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = 11.sp,
                                lineHeight = 17.sp
                            )

                            if (isImportingLocal) {
                                LinearProgressIndicator(
                                    progress = { importProgress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = Color(0xFF38BDF8),
                                    trackColor = Color(0xFF0F172A)
                                )
                                Text(
                                    text = importStatus,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp
                                )
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        mbtilesPickerLauncher.launch(
                                            arrayOf(
                                                "application/octet-stream",
                                                "application/vnd.sqlite3",
                                                "*/*"
                                            )
                                        )
                                    },
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("regions_import_from_storage_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = "اختيار ملف خريطة من ذاكرة الهاتف",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "اختيار ملف .mbtiles من ذاكرة الهاتف",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // الخيار 2: التحميل من الموقع أو الرابط المباشر
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🌐 الخيار الثاني: التحميل من الموقع أو الرابط المباشر",
                                color = Color(0xFFA7F3D0),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = "أدخل رابط قائمة المناطق (regions.json) أو رابط ملف (.mbtiles) مباشر يبدأ بـ https:// لتحميله وتثبيته تلقائياً.",
                                color = Color.White.copy(alpha = 0.72f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            OutlinedTextField(
                                value = siteUrlInput,
                                onValueChange = { siteUrlInput = it },
                                placeholder = {
                                    Text(
                                        text = "https://.../yemen.mbtiles أو regions.json",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("regions_site_url_input")
                            )

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
                                    text = "التنزيل عبر Wi-Fi فقط",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    val url = siteUrlInput.trim()
                                    errorText = null
                                    statusMessage = null
                                    if (url.isBlank()) {
                                        errorText = "أدخل رابط الموقع (regions.json) أو رابط ملف .mbtiles مباشر، أو استخدم زر ذاكرة الهاتف أعلاه."
                                        return@Button
                                    }
                                    RegionPackManager.saveManifestUrl(context, url)
                                    if (url.lowercase().substringBefore('?').endsWith(".mbtiles")) {
                                        RegionPackManager.enqueueDirectMbtilesUrl(
                                            context = context,
                                            url = url,
                                            wifiOnly = wifiOnly
                                        ).onSuccess { pack ->
                                            statusMessage = "بدأ تحميل \"${pack.name}\" في الخلفية..."
                                        }.onFailure { err ->
                                            errorText = err.localizedMessage ?: "تعذر بدء التحميل من الرابط"
                                        }
                                    } else {
                                        refreshTick++
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("regions_fetch_url_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "جلب أو تنزيل من الموقع",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (siteUrlInput.trim().lowercase().substringBefore('?').endsWith(".mbtiles")) {
                                        "تنزيل الخريطة الآن"
                                    } else {
                                        "جلب قائمة الخرائط من الموقع"
                                    },
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }

                // التحميلات الجارية من الروابط المباشرة
                if (activeGlobalDownloads.isNotEmpty()) {
                    itemsIndexed(activeGlobalDownloads, key = { _, info -> info.id }) { _, info ->
                        val pct = info.progress.getInt("p", 0)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (info.state == WorkInfo.State.ENQUEUED) {
                                            "في انتظار الشبكة للتنزيل…"
                                        } else {
                                            "جاري تنزيل حزمة الخريطة… $pct%"
                                        },
                                        color = Color(0xFF38BDF8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(
                                        onClick = { workManager.cancelWorkById(info.id) },
                                        modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                                    ) {
                                        Text("إلغاء", color = Color(0xFFEF4444), fontSize = 12.sp)
                                    }
                                }
                                LinearProgressIndicator(
                                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = Color(0xFF10B981),
                                    trackColor = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }

                // المناطق المثبتة محلياً (من الهاتف أو الإنترنت)
                if (installedRegions.isNotEmpty()) {
                    item {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Text(
                            text = "✅ الخرائط المثبتة على جهازك (${installedRegions.size})",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    itemsIndexed(installedRegions, key = { _, marker -> "installed_${marker.id}" }) { _, marker ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = marker.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${marker.sizeMb} م.ب • التكبير Z${marker.minZoom}–Z${marker.maxZoom}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        RegionPackManager.delete(context, marker.id)
                                        refreshLocalState()
                                        onRegionsChanged()
                                    },
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف ${marker.name}",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("حذف", color = Color(0xFFEF4444), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // قائمة الحزم المتاحة من الموقع
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = Color(0xFF10B981))
                                Text(
                                    text = "جاري جلب حزم المناطق المتاحة...",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else if (packs.isNotEmpty()) {
                    item {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "حزم المناطق المتاحة للتنزيل (${packs.size})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            IconButton(
                                onClick = { refreshTick++ },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحديث القائمة",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

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
                                refreshLocalState()
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
                                                refreshLocalState()
                                                onRegionsChanged()
                                            },
                                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.defaultMinSize(minHeight = 44.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف ${pack.name}",
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
                                                contentDescription = "تنزيل ${pack.name}",
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
        },
        confirmButton = {},
        dismissButton = {
            if (!isImportingLocal) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("regions_close_button")
                ) {
                    Text("إغلاق", color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    )
}

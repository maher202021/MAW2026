package com.serviseyem.ui.screens.map

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.utils.*
import kotlinx.coroutines.launch

/**
 * حوار تنزيل/استيراد خرائط الملاحة الدقيقة أوفلاين (Valhalla) من الإنترنت أو من ذاكرة الهاتف.
 */
@Composable
fun ValhallaOfflineDialog(
    onDismiss: () -> Unit,
    onStartNavigation: () -> Unit,
    onOpenPlacesDownload: (() -> Unit)? = null,
    onStatusMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isBusy by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAlreadyDownloaded by remember { mutableStateOf(ValhallaDownloadManager.isDownloaded(context)) }
    var installedMb by remember { mutableLongStateOf(ValhallaDownloadManager.getInstalledSizeMb(context)) }
    var placesInstalled by remember { mutableStateOf(OfflinePlacesDb.isInstalled(context)) }

    // اختيار ملف خرائط من ذاكرة الهاتف بدون الحاجة لصلاحيات تخزين (Android SAF OpenDocument)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        isBusy = true
        errorMessage = null
        progress = 0.05f
        statusText = "جاري فتح ملف الخرائط من ذاكرة الهاتف..."
        coroutineScope.launch {
            val result = ValhallaDownloadManager.importFromUri(context, uri) { prog, msg ->
                progress = prog
                statusText = msg
            }
            isBusy = false
            isAlreadyDownloaded = ValhallaDownloadManager.isDownloaded(context)
            installedMb = ValhallaDownloadManager.getInstalledSizeMb(context)
            placesInstalled = OfflinePlacesDb.isInstalled(context)
            result.onSuccess { msg ->
                onStatusMessage(msg)
                if (isAlreadyDownloaded && !msg.contains("قاعدة الأماكن")) {
                    onDismiss()
                    onStartNavigation()
                }
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "تعذر استيراد الملف المختار"
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = {
            Text(
                text = if (isAlreadyDownloaded) "🧭 الملاحة الدقيقة (أوفلاين) جاهزة" else "📥 تحميل الملاحة الدقيقة الكاملة (أوفلاين)",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isAlreadyDownloaded && !isBusy) {
                    Text(
                        text = "ملف خرائط ومسارات الملاحة الدقيقة (${installedMb} ميجابايت) محفوظ داخل التطبيق وجاهز للاستخدام أوفلاين دون إنترنت.\n\nيمكنك بدء الملاحة فوراً أو استبداله بملف أحدث من الإنترنت أو ذاكرة الهاتف.",
                        fontSize = 12.5.sp,
                        color = Color(0xFFA7F3D0)
                    )
                } else if (!isBusy) {
                    Text(
                        text = "لتفعيل الملاحة الدقيقة والمسارات التفصيلية داخل شوارع اليمن، يحتاج التطبيق إلى تحميل ملف خرائط أوفلاين (حوالي 25–64 ميجابايت). يُنصح باستخدام Wi-Fi، أو اختيار الملف مباشرة من ذاكرة الهاتف.",
                        fontSize = 12.5.sp,
                        color = Color.LightGray
                    )
                }

                if (isBusy) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF00E5FF),
                        trackColor = Color(0xFF1E293B)
                    )
                    Text(statusText, fontSize = 11.5.sp, color = Color(0xFF00E5FF))
                }

                errorMessage?.let { err ->
                    Surface(
                        color = Color(0xFF7F1D1D).copy(alpha = 0.55f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = err,
                            color = Color(0xFFFECACA),
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                if (!isBusy) {
                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "application/zip",
                                    "application/x-tar",
                                    "application/octet-stream",
                                    "application/vnd.sqlite3",
                                    "*/*"
                                )
                            )
                        },
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("valhalla_import_from_storage_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "إضافة ملف الخرائط من ذاكرة الهاتف",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إضافة ملف الخرائط من ذاكرة الهاتف",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (onOpenPlacesDownload != null) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenPlacesDownload()
                            },
                            border = BorderStroke(1.dp, if (placesInstalled) Color(0xFF10B981) else Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("valhalla_open_places_db_button")
                        ) {
                            Text(
                                text = if (placesInstalled) "✓ قاعدة الأماكن للبحث بلا إنترنت مثبّتة" else "📥 إعداد قاعدة الأماكن للبحث بلا إنترنت",
                                color = if (placesInstalled) Color(0xFFA7F3D0) else Color(0xFFFCD34D),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isBusy) {
                if (isAlreadyDownloaded) {
                    Button(
                        onClick = {
                            onDismiss()
                            onStartNavigation()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("valhalla_start_nav_button")
                    ) {
                        Text("بدء الملاحة الآن 🧭", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            isBusy = true
                            errorMessage = null
                            progress = 0.05f
                            statusText = "جاري بدء التحميل..."
                            coroutineScope.launch {
                                val prefs = context.getSharedPreferences("valhalla_prefs", Context.MODE_PRIVATE)
                                val adminUrl = prefs.getString("valhalla_url", "") ?: ""
                                val success = ValhallaDownloadManager.downloadAndExtractOfflineTiles(context, adminUrl) { prog, msg ->
                                    progress = prog
                                    statusText = msg
                                }
                                isBusy = false
                                isAlreadyDownloaded = ValhallaDownloadManager.isDownloaded(context)
                                installedMb = ValhallaDownloadManager.getInstalledSizeMb(context)
                                if (success) {
                                    onDismiss()
                                    onStartNavigation()
                                    onStatusMessage("تم تحميل وحفظ خرائط الملاحة بنجاح ✓")
                                } else {
                                    errorMessage = "فشل التحميل من الإنترنت. تأكد من الاتصال أو أضف الملف من ذاكرة الهاتف."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("valhalla_download_online_button")
                    ) {
                        Text("تحميل الآن", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            if (!isBusy) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isAlreadyDownloaded) {
                        TextButton(
                            onClick = {
                                isBusy = true
                                errorMessage = null
                                progress = 0.05f
                                statusText = "جاري إعادة التحميل..."
                                coroutineScope.launch {
                                    val prefs = context.getSharedPreferences("valhalla_prefs", Context.MODE_PRIVATE)
                                    val adminUrl = prefs.getString("valhalla_url", "") ?: ""
                                    val success = ValhallaDownloadManager.downloadAndExtractOfflineTiles(context, adminUrl) { prog, msg ->
                                        progress = prog
                                        statusText = msg
                                    }
                                    isBusy = false
                                    isAlreadyDownloaded = ValhallaDownloadManager.isDownloaded(context)
                                    installedMb = ValhallaDownloadManager.getInstalledSizeMb(context)
                                    if (success) {
                                        onDismiss()
                                        onStartNavigation()
                                        onStatusMessage("تم تحديث وحفظ خرائط الملاحة بنجاح ✓")
                                    } else {
                                        errorMessage = "تعذر إعادة التحميل من الشبكة (تم الإبقاء على النسخة الحالية)."
                                    }
                                }
                            },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) {
                            Text("إعادة التنزيل 🔄", color = Color(0xFF00E5FF), fontSize = 12.sp)
                        }
                    }
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("لاحقاً", color = Color.Gray)
                    }
                }
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * حوار تنزيل/استيراد قاعدة الأماكن المحلية (places.db) للبحث عن الشوارع والأحياء بلا إنترنت.
 */
@Composable
fun PlacesDownloadDialog(
    onDismiss: () -> Unit,
    onCompleted: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isBusy by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var installed by remember { mutableStateOf(OfflinePlacesDb.isInstalled(context)) }
    var rowCount by remember { mutableLongStateOf(if (installed) OfflinePlacesDb.rowCount(context) else 0L) }
    val hasDownloadUrl = remember { PlacesDbDownloader.configuredUrl(context).isNotBlank() }

    val placesPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        isBusy = true
        errorMessage = null
        progress = 0.1f
        statusText = "جاري قراءة قاعدة الأماكن..."
        coroutineScope.launch {
            val res = PlacesDbDownloader.importFromUri(context, uri) { prog, msg ->
                progress = prog
                statusText = msg
            }
            isBusy = false
            res.onSuccess { count ->
                installed = true
                rowCount = count
                onDismiss()
                onCompleted("تم استيراد قاعدة الأماكن بنجاح ✓ ($count مكان)")
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "تعذر استيراد قاعدة الأماكن"
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = {
            Text(
                text = if (installed) "📍 قاعدة الأماكن المحلية جاهزة" else "📥 تحميل قاعدة الأماكن (بلا إنترنت)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (installed) {
                        "قاعدة الأماكن المحلية مثبّتة داخل التطبيق ($rowCount شارع وحي ومَعلم في اليمن) وتعمل للبحث بلا إنترنت."
                    } else {
                        "تتيح قاعدة الأماكن (places.db نحو ${NavConfig.PLACES_DB_APPROX_MB} ميجا) البحث بالعربية عن الشوارع والأحياء والمدن والمعالم في اليمن دون إنترنت."
                    },
                    color = if (installed) Color(0xFFA7F3D0) else Color.LightGray,
                    fontSize = 12.5.sp
                )

                if (isBusy) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF00E5FF),
                        trackColor = Color(0xFF1E293B)
                    )
                    Text(statusText, color = Color(0xFF00E5FF), fontSize = 11.5.sp)
                }

                errorMessage?.let { err ->
                    Surface(
                        color = Color(0xFF7F1D1D).copy(alpha = 0.55f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(err, color = Color(0xFFFECACA), fontSize = 11.5.sp, modifier = Modifier.padding(10.dp))
                    }
                }

                if (!isBusy) {
                    OutlinedButton(
                        onClick = {
                            placesPickerLauncher.launch(
                                arrayOf("application/zip", "application/vnd.sqlite3", "application/octet-stream", "*/*")
                            )
                        },
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("places_import_from_storage_button")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "استيراد من ذاكرة الهاتف", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("استيراد places.db / .zip من ذاكرة الهاتف", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (!isBusy && hasDownloadUrl) {
                Button(
                    onClick = {
                        isBusy = true
                        errorMessage = null
                        progress = 0.05f
                        statusText = "جاري تحميل قاعدة الأماكن..."
                        coroutineScope.launch {
                            val res = PlacesDbDownloader.download(context) { p ->
                                progress = p
                                statusText = "جاري تحميل وفك قاعدة الأماكن (${(p * 100).toInt()}%)..."
                            }
                            isBusy = false
                            res.onSuccess {
                                val count = OfflinePlacesDb.rowCount(context)
                                onDismiss()
                                onCompleted("تم تثبيت قاعدة الأماكن بنجاح ✓ ($count مكان)")
                            }.onFailure { err ->
                                errorMessage = err.localizedMessage ?: "فشل تحميل قاعدة الأماكن"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("places_download_online_button")
                ) {
                    Text(if (installed) "تحديث الآن" else "تحميل الآن", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isBusy) {
                TextButton(onClick = onDismiss, modifier = Modifier.defaultMinSize(minHeight = 48.dp)) {
                    Text("إغلاق", color = Color.Gray)
                }
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(16.dp)
    )
}

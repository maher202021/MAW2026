package com.serviseyem.utils

import com.serviseyem.MyApplication
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class ErrorLogItem(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String = "SYSTEM",
    val message: String = "",
    val details: String = "",
    val type: String = "FIRESTORE" // FIRESTORE, API, NETWORK, GENERAL
)

object AppErrorLogManager {
    private val _logs = MutableStateFlow<List<ErrorLogItem>>(emptyList())
    val logs: StateFlow<List<ErrorLogItem>> = _logs.asStateFlow()

    private const val MAX_LOGS = 150

    fun logFirestoreError(tag: String, message: String, throwable: Throwable? = null) {
        val details = throwable?.stackTraceToString() ?: throwable?.localizedMessage ?: ""
        addLog(
            ErrorLogItem(
                tag = if (tag.isBlank()) "Firestore" else tag,
                message = message,
                details = details,
                type = "FIRESTORE"
            )
        )
        try {
            MyApplication.instance?.recordExceptionSafely(
                throwable ?: Exception("$tag: $message")
            )
        } catch (e: Exception) { /* تجاهل */ }
    }

    fun logApiError(tag: String, message: String, throwable: Throwable? = null) {
        val details = throwable?.stackTraceToString() ?: throwable?.localizedMessage ?: ""
        addLog(
            ErrorLogItem(
                tag = if (tag.isBlank()) "API" else tag,
                message = message,
                details = details,
                type = "API"
            )
        )
        try {
            MyApplication.instance?.recordExceptionSafely(
                throwable ?: Exception("$tag: $message")
            )
        } catch (e: Exception) { /* تجاهل */ }
    }

    fun logGenericError(tag: String, message: String, throwable: Throwable? = null) {
        val details = throwable?.stackTraceToString() ?: throwable?.localizedMessage ?: ""
        addLog(
            ErrorLogItem(
                tag = if (tag.isBlank()) "General" else tag,
                message = message,
                details = details,
                type = "GENERAL"
            )
        )
        try {
            MyApplication.instance?.recordExceptionSafely(
                throwable ?: Exception("$tag: $message")
            )
        } catch (e: Exception) { /* تجاهل */ }
    }

    private fun addLog(item: ErrorLogItem) {
        val current = _logs.value.toMutableList()
        current.add(0, item)
        if (current.size > MAX_LOGS) {
            _logs.value = current.take(MAX_LOGS)
        } else {
            _logs.value = current
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun getFormattedLogsText(): String {
        val sb = StringBuilder()
        sb.append("=== سجل أخطاء النظام والتطبيقات (App Error Logs) ===\n")
        // ✨ م2: استخدام DateFormatter
        sb.append("تاريخ التصدير: ${DateFormatter.formatDateTime(System.currentTimeMillis())}\n")
        sb.append("إجمالي السجلات: ${_logs.value.size}\n\n")

        _logs.value.forEachIndexed { index, log ->
            sb.append("[${index + 1}] [${DateFormatter.formatDateTime(log.timestamp)}] [${log.type}] [${log.tag}]\n")
            sb.append("الرسالة: ${log.message}\n")
            if (log.details.isNotBlank()) {
                sb.append("التفاصيل التقنية:\n${log.details}\n")
            }
            sb.append("--------------------------------------------------\n")
        }
        return sb.toString()
    }
}

@Composable
fun ErrorLogsViewerDialog(
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val logs by AppErrorLogManager.logs.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            "FIRESTORE" -> logs.filter { it.type == "FIRESTORE" }
            "API" -> logs.filter { it.type == "API" }
            "GENERAL" -> logs.filter { it.type != "FIRESTORE" && it.type != "API" }
            else -> logs
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "سجل تشخيص أخطاء النظام (Error Logs)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.accent
                            )
                            Text(
                                text = "سجل الأخطاء المؤقتة للـ Firestore والـ API للمالك",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("الكل (${logs.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColors.accent,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "FIRESTORE",
                        onClick = { selectedFilter = "FIRESTORE" },
                        label = { Text("Firestore (${logs.count { it.type == "FIRESTORE" }})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF59E0B),
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "API",
                        onClick = { selectedFilter = "API" },
                        label = { Text("API (${logs.count { it.type == "API" }})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF3B82F6),
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val text = AppErrorLogManager.getFormattedLogsText()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ErrorLogs", text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "📋 تم نسخ سجل الأخطاء إلى الحافظة", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ السجلات 📋", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            AppErrorLogManager.clearLogs()
                            Toast.makeText(context, "🗑️ تم تفريغ سجل الأخطاء بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مسح السجل 🗑️", fontSize = 11.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Logs List
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✨", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لا توجد أخطاء مسجلة حالياً!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "جميع عمليات الـ Firestore والـ API تعمل بشكل سليم.",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredLogs, key = { it.id }) { item ->
                            var isExpanded by remember { mutableStateOf(false) }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    0.5.dp,
                                    when (item.type) {
                                        "FIRESTORE" -> Color(0xFFF59E0B)
                                        "API" -> Color(0xFF3B82F6)
                                        else -> Color(0xFFEF4444)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExpanded = !isExpanded }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = when (item.type) {
                                                "FIRESTORE" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                "API" -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                                else -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "${item.type} • ${item.tag}",
                                                color = when (item.type) {
                                                    "FIRESTORE" -> Color(0xFFF59E0B)
                                                    "API" -> Color(0xFF60A5FA)
                                                    else -> Color(0xFFF87171)
                                                },
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            // ✨ م2: استخدام DateFormatter
                                            text = DateFormatter.formatCustom(item.timestamp, "HH:mm:ss dd/MM"),
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = item.message,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    if (item.details.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (isExpanded) {
                                            Surface(
                                                color = Color(0xFF0F172A),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = item.details,
                                                    color = Color(0xFFFCA5A5),
                                                    fontSize = 9.5.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "اضغط لعرض تفاصيل الخطأ البرمجي...",
                                                color = themeColors.accent,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.AssistantFullReport
import com.serviseyem.utils.AssistantTestRunner
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch

/**
 * 🤖 AssistantTestScreen
 * شاشة الفحص الهندسي التفاعلي الشامل للمساعد الذكي (14 خطوة عملية)
 * مخصصة للمالك فقط في البوابة الخلفية.
 */
@Composable
fun AssistantTestScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val runner = remember { AssistantTestRunner(context) }

    var isRunning by remember { mutableStateOf(false) }
    var progressMessage by remember { mutableStateOf("جاهز لبدء الفحص الشامل للمساعد الذكي (14 خطوة عملية)...") }
    var report by remember { mutableStateOf<AssistantFullReport?>(null) }

    BackHandler(onBack = {
        if (!isRunning) onDismiss()
    })

    Dialog(
        onDismissRequest = { if (!isRunning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            containerColor = Color(0xFF0A0F1D),
            topBar = {
                Surface(
                    color = Color(0xFF111827),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (!isRunning) onDismiss() },
                                modifier = Modifier.testTag("assistant_test_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "🤖 أداة اختبار المساعد الذكي الشامل",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "فحص 14 معياراً: الفهم، الترشيح، التسعيرة، الصوت، Firebase، Offline، وإعدادات الأدمن",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        val currentReportTop = report
                        if (currentReportTop != null) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val text = runner.formatReportText(currentReportTop)
                                    clipboard.setPrimaryClip(ClipData.newPlainText("AssistantTestReport", text))
                                    Toast.makeText(context, "📋 تم نسخ تقرير اختبار المساعد الذكي بالكامل!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("copy_assistant_report_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "نسخ التقرير",
                                    tint = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(14.dp)
            ) {
                // Control Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = progressMessage,
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (isRunning) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                color = Color(0xFF00E5FF),
                                trackColor = Color(0xFF0F172A),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isRunning) {
                                        isRunning = true
                                        scope.launch {
                                            runner.runAssistantTest(
                                                mainViewModel = viewModel,
                                                onProgress = { msg -> progressMessage = msg },
                                                onComplete = { finalReport ->
                                                    report = finalReport
                                                    isRunning = false
                                                    progressMessage = "✅ اكتمل فحص المساعد الذكي: نجح ${finalReport.passed} من ${finalReport.totalSteps}"
                                                }
                                            )
                                        }
                                    }
                                },
                                enabled = !isRunning,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("start_assistant_test_button")
                            ) {
                                if (isRunning) {
                                    CircularProgressIndicator(
                                        color = Color.Black,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("جاري الفحص...", color = Color.Black, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(
                                        imageVector = if (report == null) Icons.Default.PlayArrow else Icons.Default.Refresh,
                                        contentDescription = "بدء الفحص",
                                        tint = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (report == null) "▶️ بدء اختبار المساعد الذكي (14 خطوة)" else "🔄 إعادة تشغيل الاختبار",
                                        color = Color.Black,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }

                            val currentReportBottom = report
                            if (currentReportBottom != null) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val text = runner.formatReportText(currentReportBottom)
                                        clipboard.setPrimaryClip(ClipData.newPlainText("AssistantTestReport", text))
                                        Toast.makeText(context, "📋 تم نسخ التقرير للحافظة!", Toast.LENGTH_SHORT).show()
                                    },
                                    border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "نسخ",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("نسخ التقرير", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary Cards
                report?.let { r ->
                    val passRate = if (r.totalSteps > 0) (r.passed * 100) / r.totalSteps else 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryMetricBox(
                            title = "إجمالي الفحوصات",
                            value = "${r.totalSteps}",
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricBox(
                            title = "ناجحة ✅",
                            value = "${r.passed}",
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricBox(
                            title = "فاشلة ❌",
                            value = "${r.failed}",
                            color = if (r.failed == 0) Color(0xFF64748B) else Color(0xFFEF4444),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricBox(
                            title = "نسبة النجاح",
                            value = "$passRate%",
                            color = if (passRate == 100) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(r.steps) { step ->
                            val isPass = step.status == "SUCCESS"
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPass) Color(0xFF0F291E) else Color(0xFF2D1215)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isPass) Color(0xFF10B981).copy(alpha = 0.45f) else Color(0xFFEF4444).copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (isPass) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = step.status,
                                                tint = if (isPass) Color(0xFF10B981) else Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = step.stepName,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Surface(
                                            color = if (isPass) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (isPass) "ناجح ✓" else "فشل ✗",
                                                color = if (isPass) Color(0xFF34D399) else Color(0xFFF87171),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = step.description,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                    if (step.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "📌 النتيجة: ${step.notes}",
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "اضغط على زر 'بدء اختبار المساعد الذكي' لفحص الـ 14 معياراً على Firebase والمحرك المحلي",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = Color(0xFFCBD5E1),
                fontSize = 10.sp
            )
        }
    }
}

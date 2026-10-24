package com.serviseyem.ui.screens.admin

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TestSystem(val label: String, val icon: String) {
    REGISTRATION("التسجيل (8 أقسام)", "🎫"),
    BOOKINGS("الحجوزات", "📅"),
    INSTANT_REQUESTS("الطلبات العاجلة", "⚡"),
    CHATS("المحادثات", "💬"),
    PAYMENTS("المدفوعات", "💳"),
    REVIEWS("التقييمات", "⭐"),
    NOTIFICATIONS("الإشعارات", "🔔"),
    REPORTS("التقارير", "📢"),
    MAPS("الخريطة GPS", "🗺️"),
    SEARCH("البحث", "🔍"),
    PROFILES("الملفات الشخصية (8)", "👤"),
    PERMISSIONS("صلاحيات الأدمن والمالك", "🛡️"),
    SETTINGS_SYNC("الإعدادات والمزامنة", "⚙️"),
    RUN_ALL("تشغيل الفحص الشامل (13)", "🧪")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationTestScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Initialize all 13 test runners
    val regRunner = remember { RegistrationTestRunner(context) }
    val bookingRunner = remember { BookingTestRunner(context) }
    val instantRunner = remember { InstantRequestTestRunner(context) }
    val chatRunner = remember { ChatTestRunner(context) }
    val paymentRunner = remember { PaymentTestRunner(context) }
    val reviewRunner = remember { ReviewTestRunner(context) }
    val notificationRunner = remember { NotificationTestRunner(context) }
    val reportRunner = remember { ReportTestRunner(context) }
    val mapRunner = remember { MapTestRunner(context) }
    val searchRunner = remember { SearchTestRunner(context) }
    val profileRunner = remember { ProfileDiagnosticRunner(context) }
    val permissionRunner = remember { PermissionsDiagnosticRunner(context) }
    val settingsRunner = remember { SettingsSyncDiagnosticRunner(context) }

    var selectedSystem by remember { mutableStateOf(TestSystem.RUN_ALL) }
    var isRunning by remember { mutableStateOf(false) }
    var currentProgressText by remember {
        mutableStateOf("جاهز لتشغيل الفحص الهندسي الشامل والمفصل (13 نظاماً مع تشخيص السطر والدالة والسبب والحل)...")
    }

    // Reports states
    var regReport by remember { mutableStateOf<FullTestReport?>(null) }
    var bookingReport by remember { mutableStateOf<BookingFullReport?>(null) }
    var instantReport by remember { mutableStateOf<InstantRequestFullReport?>(null) }
    var chatReport by remember { mutableStateOf<ChatFullReport?>(null) }
    var paymentReport by remember { mutableStateOf<PaymentFullReport?>(null) }
    var reviewReport by remember { mutableStateOf<ReviewFullReport?>(null) }
    var notificationReport by remember { mutableStateOf<NotificationFullReport?>(null) }
    var reportReport by remember { mutableStateOf<ReportFullReport?>(null) }
    var mapReport by remember { mutableStateOf<MapFullReport?>(null) }
    var searchReport by remember { mutableStateOf<SearchFullReport?>(null) }
    var profileReport by remember { mutableStateOf<ProfileFullReport?>(null) }
    var permissionReport by remember { mutableStateOf<PermissionFullReport?>(null) }
    var settingsReport by remember { mutableStateOf<SettingsFullReport?>(null) }

    val masterDiagnosticReport = remember(
        regReport, bookingReport, instantReport, chatReport, paymentReport,
        reviewReport, notificationReport, reportReport, mapReport, searchReport,
        profileReport, permissionReport, settingsReport
    ) {
        DeepSystemDiagnosticOrchestrator.buildComprehensiveReport(
            reg = regReport,
            bk = bookingReport,
            inst = instantReport,
            ch = chatReport,
            pay = paymentReport,
            rev = reviewReport,
            noti = notificationReport,
            rep = reportReport,
            mp = mapReport,
            srh = searchReport,
            prof = profileReport,
            perm = permissionReport,
            sett = settingsReport
        )
    }

    BackHandler {
        if (!isRunning) {
            onDismiss()
        } else {
            Toast.makeText(context, "⏳ يرجى الانتظار حتى اكتمال دورة الفحص العملي!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                IconButton(
                    onClick = {
                        if (!isRunning) onDismiss()
                        else Toast.makeText(context, "⏳ الفحص قيد التشغيل حالياً!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔬 أداة الفحص والتشخيص الهندسي الشامل (13 نظاماً)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "تحديد دقيق لمكان الخطأ (File + Line + Function) + السبب والحل المقترح",
                        fontSize = 11.sp,
                        color = themeColors.accent
                    )
                }
            }

            // Tabs selection for the 13 Systems + RUN_ALL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TestSystem.values().forEach { system ->
                    val isSelected = selectedSystem == system
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) themeColors.accent else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable {
                            if (!isRunning) {
                                selectedSystem = system
                                currentProgressText = "جاهز لبدء فحص: ${system.label}"
                            } else {
                                Toast.makeText(context, "⏳ يرجى الانتظار حتى انتهاء الفحص الجاري!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(system.icon, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = system.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            // Live Progress Box
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isRunning) themeColors.accent else Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            color = themeColors.accent,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = currentProgressText,
                        fontSize = 12.sp,
                        color = if (isRunning) Color.White else Color.LightGray,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedSystem) {
                    TestSystem.REGISTRATION -> RenderDetailedSystemSection(
                        title = "1. نظام التسجيل الشامل (8 أقسام)",
                        details = masterDiagnosticReport.systemDetails["1. نظام التسجيل الشامل (8 أقسام)"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.BOOKINGS -> RenderDetailedSystemSection(
                        title = "2. نظام الحجوزات والدورة المستندية",
                        details = masterDiagnosticReport.systemDetails["2. نظام الحجوزات والدورة المستندية"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.INSTANT_REQUESTS -> RenderDetailedSystemSection(
                        title = "3. نظام الطلبات العاجلة وعروض الأسعار",
                        details = masterDiagnosticReport.systemDetails["3. نظام الطلبات العاجلة وعروض الأسعار"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.CHATS -> RenderDetailedSystemSection(
                        title = "4. نظام المحادثات الفورية والوسائط",
                        details = masterDiagnosticReport.systemDetails["4. نظام المحادثات الفورية والوسائط"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.PAYMENTS -> RenderDetailedSystemSection(
                        title = "5. نظام المدفوعات والمحافظ المالية",
                        details = masterDiagnosticReport.systemDetails["5. نظام المدفوعات والمحافظ المالية"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.REVIEWS -> RenderDetailedSystemSection(
                        title = "6. نظام التقييمات ومراجعة الأبعاد",
                        details = masterDiagnosticReport.systemDetails["6. نظام التقييمات ومراجعة الأبعاد"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.NOTIFICATIONS -> RenderDetailedSystemSection(
                        title = "7. نظام الإشعارات والتنبيهات الجغرافية",
                        details = masterDiagnosticReport.systemDetails["7. نظام الإشعارات والتنبيهات الجغرافية"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.REPORTS -> RenderDetailedSystemSection(
                        title = "8. نظام البلاغات والشكاوى الإدارية",
                        details = masterDiagnosticReport.systemDetails["8. نظام البلاغات والشكاوى الإدارية"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.MAPS -> RenderDetailedSystemSection(
                        title = "9. نظام الخريطة التفاعلية GPS و Leaflet",
                        details = masterDiagnosticReport.systemDetails["9. نظام الخريطة التفاعلية GPS و Leaflet"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.SEARCH -> RenderDetailedSystemSection(
                        title = "10. نظام البحث الذكي والفلترة",
                        details = masterDiagnosticReport.systemDetails["10. نظام البحث الذكي والفلترة"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.PROFILES -> RenderDetailedSystemSection(
                        title = "11. نظام الملفات الشخصية (8 أنواع حسابات)",
                        details = masterDiagnosticReport.systemDetails["11. نظام الملفات الشخصية (8 أنواع)"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.PERMISSIONS -> RenderDetailedSystemSection(
                        title = "12. نظام صلاحيات الأدمن والمالك والمشرفين",
                        details = masterDiagnosticReport.systemDetails["12. نظام صلاحيات الأدمن والمالك"],
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.SETTINGS_SYNC -> RenderSettingsDiagnosticSection(
                        report = settingsReport,
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                    TestSystem.RUN_ALL -> RenderMasterDiagnosticSummary(
                        report = masterDiagnosticReport,
                        reg = regReport,
                        bk = bookingReport,
                        inst = instantReport,
                        ch = chatReport,
                        pay = paymentReport,
                        rev = reviewReport,
                        noti = notificationReport,
                        rep = reportReport,
                        mp = mapReport,
                        srh = searchReport,
                        prof = profileReport,
                        perm = permissionReport,
                        sett = settingsReport,
                        onCopyError = { clipboardManager.setText(AnnotatedString(it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (!isRunning) {
                                isRunning = true
                                scope.launch {
                                    try {
                                        when (selectedSystem) {
                                            TestSystem.REGISTRATION -> regRunner.runFullTest(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { regReport = it; isRunning = false }
                                            )
                                            TestSystem.BOOKINGS -> bookingRunner.runBookingTest(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { bookingReport = it; isRunning = false }
                                            )
                                            TestSystem.INSTANT_REQUESTS -> instantRunner.runInstantRequestTest(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { instantReport = it; isRunning = false }
                                            )
                                            TestSystem.CHATS -> chatRunner.runChatTest(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { chatReport = it; isRunning = false }
                                            )
                                            TestSystem.PAYMENTS -> paymentRunner.runPaymentTest(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { paymentReport = it; isRunning = false }
                                            )
                                            TestSystem.REVIEWS -> reviewRunner.runReviewTest(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { reviewReport = it; isRunning = false }
                                            )
                                            TestSystem.NOTIFICATIONS -> notificationRunner.runNotificationTest(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { notificationReport = it; isRunning = false }
                                            )
                                            TestSystem.REPORTS -> reportRunner.runReportTest(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { reportReport = it; isRunning = false }
                                            )
                                            TestSystem.MAPS -> mapRunner.runMapTest(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { mapReport = it; isRunning = false }
                                            )
                                            TestSystem.SEARCH -> searchRunner.runSearchTest(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { searchReport = it; isRunning = false }
                                            )
                                            TestSystem.PROFILES -> profileRunner.runProfileDiagnostics(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { profileReport = it; isRunning = false }
                                            )
                                            TestSystem.PERMISSIONS -> permissionRunner.runPermissionsDiagnostics(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { permissionReport = it; isRunning = false }
                                            )
                                            TestSystem.SETTINGS_SYNC -> settingsRunner.runSettingsDiagnostics(
                                                onProgress = { currentProgressText = it },
                                                onComplete = { settingsReport = it; isRunning = false }
                                            )
                                            TestSystem.RUN_ALL -> {
                                                runAll13SystemsSequentialTests(
                                                    regRunner = regRunner,
                                                    bookingRunner = bookingRunner,
                                                    instantRunner = instantRunner,
                                                    chatRunner = chatRunner,
                                                    paymentRunner = paymentRunner,
                                                    reviewRunner = reviewRunner,
                                                    notificationRunner = notificationRunner,
                                                    reportRunner = reportRunner,
                                                    mapRunner = mapRunner,
                                                    searchRunner = searchRunner,
                                                    profileRunner = profileRunner,
                                                    permissionRunner = permissionRunner,
                                                    settingsRunner = settingsRunner,
                                                    onProgress = { currentProgressText = it },
                                                    onComplete = { reg, bk, inst, ch, pay, rev, noti, rep, mp, srh, prof, perm, sett ->
                                                        regReport = reg
                                                        bookingReport = bk
                                                        instantReport = inst
                                                        chatReport = ch
                                                        paymentReport = pay
                                                        reviewReport = rev
                                                        notificationReport = noti
                                                        reportReport = rep
                                                        mapReport = mp
                                                        searchReport = srh
                                                        profileReport = prof
                                                        permissionReport = perm
                                                        settingsReport = sett
                                                        isRunning = false
                                                        currentProgressText = "✅ اكتمل الفحص الهندسي الشامل للأنظمة الـ 13 بنجاح وتم توليد التقرير المفصل!"
                                                    }
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {
                                        isRunning = false
                                        currentProgressText = "❌ حدث خطأ غير متوقع: ${e.localizedMessage}"
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                        enabled = !isRunning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "تشغيل", tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRunning) "جاري الفحص..." else "بدء الفحص المفصل",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (!isRunning) {
                                isRunning = true
                                scope.launch {
                                    try {
                                        currentProgressText = "🧹 جاري تنظيف وتطهير كافة السجلات الاختبارية من Firestore..."
                                        regRunner.cleanAllTestData()
                                        bookingRunner.cleanAllTestData()
                                        instantRunner.cleanAllTestData()
                                        chatRunner.cleanAllTestData()
                                        paymentRunner.cleanAllTestData()
                                        reviewRunner.cleanAllTestData()
                                        notificationRunner.cleanAllTestData()
                                        reportRunner.cleanAllTestData()
                                        profileRunner.cleanAllTestData()
                                        permissionRunner.cleanAllTestData()
                                        settingsRunner.cleanAllTestData()

                                        regReport = null
                                        bookingReport = null
                                        instantReport = null
                                        chatReport = null
                                        paymentReport = null
                                        reviewReport = null
                                        notificationReport = null
                                        reportReport = null
                                        mapReport = null
                                        searchReport = null
                                        profileReport = null
                                        permissionReport = null
                                        settingsReport = null

                                        currentProgressText = "🧹 تم تنظيف وتطهير كافة السجلات الاختبارية للأنظمة الـ 13 بنجاح!"
                                    } catch (e: Exception) {
                                        currentProgressText = "❌ خطأ أثناء تنظيف البيانات: ${e.localizedMessage}"
                                    } finally {
                                        isRunning = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        enabled = !isRunning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "تنظيف", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تنظيف السجلات", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = {
                        val fullText = DeepSystemDiagnosticOrchestrator.formatComprehensiveDiagnosticReportText(masterDiagnosticReport)
                        clipboardManager.setText(AnnotatedString(fullText))
                        Toast.makeText(context, "📋 تم نسخ التقرير الهندسي الشامل والمفصل (13 نظاماً) للحافظة!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = "نسخ التقرير", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("نسخ التقرير الشامل المفصل (مع السطر والدالة والحل)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RenderDetailedSystemSection(
    title: String,
    details: List<TestDetail>?,
    onCopyError: (String) -> Unit
) {
    val context = LocalContext.current
    if (details.isNullOrEmpty()) {
        RenderEmptyPlaceholder(title)
    } else {
        val passed = details.count { it.status == "SUCCESS" }
        val failed = details.count { it.status == "FAILED" }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(
                            text = "ناجح: $passed | فشل: $failed",
                            color = if (failed == 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            itemsIndexed(details) { idx, detail ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(
                        1.dp,
                        if (detail.status == "SUCCESS") Color(0xFF1E3A2F) else Color(0xFF7F1D1D)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${idx + 1}. ${detail.testName}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (detail.status == "SUCCESS") "✅ ناجح" else "❌ فشل",
                                color = if (detail.status == "SUCCESS") Color(0xFF10B981) else Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📄 ${detail.fileName} : Line ${detail.lineNumber} • ${detail.functionName}",
                            fontSize = 10.sp,
                            color = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = detail.message,
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        if (detail.error != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            RenderErrorDiagnosticCard(
                                err = detail.error,
                                onCopy = {
                                    onCopyError(it)
                                    Toast.makeText(context, "📋 تم نسخ تشخيص الخطأ!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderErrorDiagnosticCard(
    err: TestError,
    onCopy: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1215)),
        border = BorderStroke(1.dp, Color(0xFFEF4444)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "🚨 تشخيص الخطأ المفصل #${err.errorIndex}: ${err.testName}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFFFCA5A5)
            )
            Text("• الخطوة الفاشلة: ${err.failedStep}", fontSize = 11.sp, color = Color.White)
            Text("• الملف والسطر: ${err.fileName} (Line ${err.lineNumber})", fontSize = 11.sp, color = Color(0xFF38BDF8))
            Text("• الدالة المسؤولة: ${err.functionName}", fontSize = 11.sp, color = Color(0xFF38BDF8))
            Text("• نوع الخطأ: ${err.errorType}", fontSize = 11.sp, color = Color(0xFFFBBF24))
            Text("• النص الكامل: ${err.fullMessage}", fontSize = 11.sp, color = Color.LightGray)
            Text("• المتوقع: ${err.expectedOutcome}", fontSize = 10.sp, color = Color(0xFF86EFAC))
            Text("• الفعلي: ${err.actualOutcome}", fontSize = 10.sp, color = Color(0xFFFCA5A5))
            Text("🔍 السبب المحتمل: ${err.probableCause}", fontSize = 11.sp, color = Color(0xFFFDE047))
            Text("🛠️ الحل المقترح:\n${err.suggestedFix}", fontSize = 11.sp, color = Color(0xFF6EE7B7), lineHeight = 15.sp)

            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = {
                    val text = """
                        ❌ خطأ #${err.errorIndex}: ${err.testName}
                        • الخطوة الفاشلة: ${err.failedStep}
                        • الملف المسؤول: ${err.fileName}
                        • السطر: Line ${err.lineNumber}
                        • الدالة: ${err.functionName}
                        • نوع الخطأ: ${err.errorType}
                        • الرسالة: ${err.fullMessage}
                        • السبب المحتمل: ${err.probableCause}
                        • الحل المقترح: ${err.suggestedFix}
                    """.trimIndent()
                    onCopy(text)
                },
                border = BorderStroke(1.dp, Color(0xFFF87171)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("نسخ تفاصيل هذا الخطأ والحل المقترح", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun RenderSettingsDiagnosticSection(
    report: SettingsFullReport?,
    onCopyError: (String) -> Unit
) {
    if (report == null) {
        RenderEmptyPlaceholder("13. نظام الإعدادات والمزامنة (AdminSettingsEntity)")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text(
                    text = "⚙️ أولاً: فحوصات الاتصال الحي والمزامنة اللحظية (${report.passed}/${report.totalChecks} ناجح):",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
            itemsIndexed(report.details) { idx, d ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("${idx + 1}. ${d.testName}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(
                                text = if (d.status == "SUCCESS") "✅ ناجح" else "❌ فشل",
                                color = if (d.status == "SUCCESS") Color(0xFF10B981) else Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Text("📄 ${d.fileName} : Line ${d.lineNumber} • ${d.functionName}", fontSize = 10.sp, color = Color(0xFF38BDF8))
                        Text(d.message, fontSize = 11.sp, color = Color.LightGray)
                        if (d.error != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            RenderErrorDiagnosticCard(d.error, onCopyError)
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📋 ثانياً: جدول فحص وتشخيص حقول AdminSettingsEntity (${report.fieldDiagnostics.size} حقلاً):",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
            itemsIndexed(report.fieldDiagnostics) { idx, f ->
                val isWorking = f.status == "WORKING"
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
                    Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${idx + 1}. ${f.fieldName} (${f.arabicLabel})",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (isWorking) "✅ يعمل" else "⚠️ مهجور أمنياً",
                                color = if (isWorking) Color(0xFF10B981) else Color(0xFFFBBF24),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "الفئة: ${f.category} | الحفظ: ${if (f.isSaved) "✅" else "➖"} | القراءة: ${if (f.isRead) "✅" else "➖"} | الواجهة: ${if (f.isAppliedInUi) "✅" else "➖"} | المزامنة اللحظية: ${if (f.isSyncedRealtime) "✅" else "➖"}",
                            fontSize = 10.sp,
                            color = Color(0xFF93C5FD)
                        )
                        Text("📍 الكود: ${f.whereUsedInCode}", fontSize = 10.sp, color = Color.LightGray)
                        Text("💡 ${f.causeOrNotes}", fontSize = 10.sp, color = Color(0xFFA7F3D0))
                    }
                }
            }
        }
    }
}

@Composable
fun RenderMasterDiagnosticSummary(
    report: ComprehensiveDiagnosticReport,
    reg: FullTestReport?,
    bk: BookingFullReport?,
    inst: InstantRequestFullReport?,
    ch: ChatFullReport?,
    pay: PaymentFullReport?,
    rev: ReviewFullReport?,
    noti: NotificationFullReport?,
    rep: ReportFullReport?,
    mp: MapFullReport?,
    srh: SearchFullReport?,
    prof: ProfileFullReport?,
    perm: PermissionFullReport?,
    sett: SettingsFullReport?,
    onCopyError: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF3B82F6))
            ) {
                Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                    Text(
                        text = "📊 الملخص التنفيذي لأداة الفحص الشامل والمفصل (13 نظاماً)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "إجمالي الفحوصات المشغّلة: ${report.totalChecks} | ناجح: ${report.passedCount} ✅ | فشل: ${report.failedCount} ❌ | تحذيرات: ${report.warningsCount} ⚠️",
                        fontSize = 12.sp,
                        color = if (report.failedCount == 0) Color(0xFF10B981) else Color(0xFFF87171),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RenderSummaryRow("1. نظام التسجيل الشامل (8 أقسام)", reg != null, reg?.passed ?: 0, reg?.failed ?: 0)
                    RenderSummaryRow("2. نظام الحجوزات والتعميد", bk != null, bk?.passed ?: 0, bk?.failed ?: 0)
                    RenderSummaryRow("3. نظام الطلبات العاجلة والتفاوض", inst != null, inst?.passed ?: 0, inst?.failed ?: 0)
                    RenderSummaryRow("4. نظام المحادثات والرسائل والردود", ch != null, ch?.passed ?: 0, ch?.failed ?: 0)
                    RenderSummaryRow("5. نظام المدفوعات والعمليات والمحافظ", pay != null, pay?.passed ?: 0, pay?.failed ?: 0)
                    RenderSummaryRow("6. نظام التقييمات ومراجعة الأبعاد", rev != null, rev?.passed ?: 0, rev?.failed ?: 0)
                    RenderSummaryRow("7. نظام الإشعارات والتنبيهات الجغرافية", noti != null, noti?.passed ?: 0, noti?.failed ?: 0)
                    RenderSummaryRow("8. نظام البلاغات والشكاوى الإدارية", rep != null, rep?.passed ?: 0, rep?.failed ?: 0)
                    RenderSummaryRow("9. نظام الخريطة GPS و Leaflet (9 فحوصات)", mp != null, mp?.passed ?: 0, mp?.failed ?: 0)
                    RenderSummaryRow("10. نظام البحث والفلترة الذكي", srh != null, srh?.passed ?: 0, srh?.failed ?: 0)
                    RenderSummaryRow("11. نظام الملفات الشخصية (8 أنواع حسابات)", prof != null, prof?.passed ?: 0, prof?.failed ?: 0)
                    RenderSummaryRow("12. نظام صلاحيات الأدمن والمالك (38 تبويباً)", perm != null, perm?.passed ?: 0, perm?.failed ?: 0)
                    RenderSummaryRow("13. نظام الإعدادات والمزامنة اللحظية", sett != null, sett?.passed ?: 0, sett?.failed ?: 0)
                }
            }
        }

        if (report.errors.isNotEmpty()) {
            item {
                Text(
                    text = "🚨 الأخطاء المكتشفة مع رقم السطر والدالة والسبب والحل (${report.errors.size}):",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF87171),
                    fontSize = 13.sp
                )
            }
            itemsIndexed(report.errors) { _, err ->
                RenderErrorDiagnosticCard(err = err, onCopy = onCopyError)
            }
        }
    }
}

@Composable
fun RenderSummaryRow(name: String, ran: Boolean, passed: Int, failed: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontSize = 12.sp, color = Color.White, modifier = Modifier.weight(1f))
        if (ran) {
            Text(
                text = "ناجح: $passed | فشل: $failed",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (failed == 0) Color(0xFF10B981) else Color(0xFFEF4444)
            )
        } else {
            Text("لم يُشغّل بعد 💤", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun RenderEmptyPlaceholder(systemName: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text(
            text = "لم يتم تشغيل فحص ($systemName) بعد.\nاضغط على زر 'بدء الفحص المفصل' بالأسفل لتشغيله على Firestore حياً واستخراج تقرير السطر والدالة.",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

suspend fun runAll13SystemsSequentialTests(
    regRunner: RegistrationTestRunner,
    bookingRunner: BookingTestRunner,
    instantRunner: InstantRequestTestRunner,
    chatRunner: ChatTestRunner,
    paymentRunner: PaymentTestRunner,
    reviewRunner: ReviewTestRunner,
    notificationRunner: NotificationTestRunner,
    reportRunner: ReportTestRunner,
    mapRunner: MapTestRunner,
    searchRunner: SearchTestRunner,
    profileRunner: ProfileDiagnosticRunner,
    permissionRunner: PermissionsDiagnosticRunner,
    settingsRunner: SettingsSyncDiagnosticRunner,
    onProgress: (String) -> Unit,
    onComplete: (
        FullTestReport?, BookingFullReport?, InstantRequestFullReport?,
        ChatFullReport?, PaymentFullReport?, ReviewFullReport?,
        NotificationFullReport?, ReportFullReport?, MapFullReport?,
        SearchFullReport?, ProfileFullReport?, PermissionFullReport?, SettingsFullReport?
    ) -> Unit
) {
    var reg: FullTestReport? = null
    var book: BookingFullReport? = null
    var inst: InstantRequestFullReport? = null
    var ch: ChatFullReport? = null
    var pay: PaymentFullReport? = null
    var rev: ReviewFullReport? = null
    var noti: NotificationFullReport? = null
    var rep: ReportFullReport? = null
    var mp: MapFullReport? = null
    var srh: SearchFullReport? = null
    var prof: ProfileFullReport? = null
    var perm: PermissionFullReport? = null
    var sett: SettingsFullReport? = null

    onProgress("🧪 جاري بدء الفحص الهندسي الشامل لكافة أنظمة التطبيق (13 نظاماً)...")
    delay(400)

    regRunner.runFullTest(
        onProgress = { onProgress("🎫 [1/13] التسجيل: $it") },
        onComplete = { reg = it }
    )
    delay(300)

    bookingRunner.runBookingTest(
        onProgress = { onProgress("📅 [2/13] الحجوزات: $it") },
        onComplete = { book = it }
    )
    delay(300)

    instantRunner.runInstantRequestTest(
        onProgress = { onProgress("⚡ [3/13] الطلبات العاجلة: $it") },
        onComplete = { inst = it }
    )
    delay(300)

    chatRunner.runChatTest(
        onProgress = { onProgress("💬 [4/13] المحادثات: $it") },
        onComplete = { ch = it }
    )
    delay(300)

    paymentRunner.runPaymentTest(
        onProgress = { onProgress("💳 [5/13] المدفوعات: $it") },
        onComplete = { pay = it }
    )
    delay(300)

    reviewRunner.runReviewTest(
        onProgress = { onProgress("⭐ [6/13] التقييمات: $it") },
        onComplete = { rev = it }
    )
    delay(300)

    notificationRunner.runNotificationTest(
        onProgress = { onProgress("🔔 [7/13] الإشعارات: $it") },
        onComplete = { noti = it }
    )
    delay(300)

    reportRunner.runReportTest(
        onProgress = { onProgress("📢 [8/13] الشكاوى والتقارير: $it") },
        onComplete = { rep = it }
    )
    delay(300)

    mapRunner.runMapTest(
        onProgress = { onProgress("🗺️ [9/13] الخريطة GPS و Leaflet: $it") },
        onComplete = { mp = it }
    )
    delay(300)

    searchRunner.runSearchTest(
        onProgress = { onProgress("🔍 [10/13] البحث والفلترة: $it") },
        onComplete = { srh = it }
    )
    delay(300)

    profileRunner.runProfileDiagnostics(
        onProgress = { onProgress("👤 [11/13] الملفات الشخصية (8 أنواع): $it") },
        onComplete = { prof = it }
    )
    delay(300)

    permissionRunner.runPermissionsDiagnostics(
        onProgress = { onProgress("🛡️ [12/13] صلاحيات الأدمن والمالك: $it") },
        onComplete = { perm = it }
    )
    delay(300)

    settingsRunner.runSettingsDiagnostics(
        onProgress = { onProgress("⚙️ [13/13] الإعدادات والمزامنة اللحظية: $it") },
        onComplete = { sett = it }
    )
    delay(300)

    onComplete(reg, book, inst, ch, pay, rev, noti, rep, mp, srh, prof, perm, sett)
}

package com.serviseyem.utils

import androidx.annotation.Keep

/**
 * نموذج تفصيلي لأي خطأ يتم اكتشافه أثناء الفحص العملي
 * يحدد الملف، السطر، الدالة، نوع الخطأ، النص الكامل، السبب المحتمل، والحل المقترح.
 */
@Keep
data class TestError(
    val errorIndex: Int = 1,
    val testName: String = "",
    val failedStep: String = "",
    val fileName: String = "",
    val lineNumber: Int = 0,
    val functionName: String = "",
    val timestamp: String = "",
    val errorType: String = "",
    val fullMessage: String = "",
    val probableCause: String = "",
    val suggestedFix: String = "",
    val expectedOutcome: String = "",
    val actualOutcome: String = ""
)

/**
 * نموذج التحذيرات (مثل العمليات البطيئة أو الحقول المهجورة أو غير المطبقة بالكامل)
 */
@Keep
data class TestWarning(
    val warningIndex: Int = 1,
    val testName: String = "",
    val fileName: String = "",
    val lineNumber: Int = 0,
    val functionName: String = "",
    val warningType: String = "",
    val message: String = "",
    val suggestedImprovement: String = "",
    val durationMs: Long = 0L
)

/**
 * نموذج نتيجة فحص خطوة مفردة داخل أي نظام
 */
@Keep
data class TestDetail(
    val id: String = "",
    val systemName: String = "",
    val subCategory: String = "",
    val testName: String = "",
    val fileName: String = "",
    val lineNumber: Int = 0,
    val functionName: String = "",
    val status: String = "SUCCESS", // SUCCESS, FAILED, WARNING
    val message: String = "",
    val durationMs: Long = 0L,
    val expectedOutcome: String = "",
    val actualOutcome: String = "",
    val error: TestError? = null,
    val warning: TestWarning? = null
)

/**
 * نموذج فحص وتشخيص كل حقل في AdminSettingsEntity
 */
@Keep
data class SettingFieldDiagnostic(
    val fieldName: String = "",
    val arabicLabel: String = "",
    val category: String = "",
    val isSaved: Boolean = true,
    val isRead: Boolean = true,
    val isAppliedInUi: Boolean = true,
    val isSyncedRealtime: Boolean = true,
    val whereUsedInCode: String = "",
    val status: String = "WORKING", // WORKING, PARTIAL, NOT_APPLIED, DEPRECATED
    val causeOrNotes: String = ""
)

/**
 * التقرير الشامل والمفصل الذي يجمع كل الفحوصات والأخطاء والتحذيرات وتشخيص الإعدادات
 */
@Keep
data class ComprehensiveDiagnosticReport(
    val timestamp: String = "",
    val totalChecks: Int = 0,
    val passedCount: Int = 0,
    val failedCount: Int = 0,
    val warningsCount: Int = 0,
    val successRatePercent: Float = 100f,
    val totalDurationMs: Long = 0L,
    val errors: List<TestError> = emptyList(),
    val warnings: List<TestWarning> = emptyList(),
    val systemDetails: Map<String, List<TestDetail>> = emptyMap(),
    val settingsDiagnostics: List<SettingFieldDiagnostic> = emptyList()
)

/**
 * أداة مساعدة لتحليل الاستثناءات واستخراج الملف والسطر والدالة والسبب المحتمل والحل المقترح تلقائياً
 */
object DiagnosticErrorAnalyzer {

    fun analyzeException(
        e: Throwable,
        testName: String,
        failedStep: String,
        fallbackFile: String,
        fallbackLine: Int,
        fallbackFunction: String,
        expected: String,
        errorIndex: Int,
        timestamp: String
    ): TestError {
        val appFrame = e.stackTrace.firstOrNull { it.className.startsWith("com.serviseyem") }
        val fileName = appFrame?.fileName ?: fallbackFile
        val lineNumber = appFrame?.lineNumber?.takeIf { it > 0 } ?: fallbackLine
        val functionName = appFrame?.methodName?.let { "$it()" } ?: fallbackFunction
        val errorType = e.javaClass.simpleName.ifBlank { "RuntimeException" }
        val rawMessage = e.localizedMessage ?: e.message ?: e.toString()

        val (cause, fix) = when {
            rawMessage.contains("PERMISSION_DENIED", ignoreCase = true) -> {
                "قواعد أمان Firestore (Security Rules) تمنع الكتابة أو القراءة في هذا المسار من السياق الحالي." to
                    "1. راجع ملف firestore.rules وتأكد من صلاحيات المجموعة.\n2. تأكد من تهيئة جلسة المصادقة أو صلاحية الأدمن.\n3. تحقق من تطابق اسم الكولكشن في الكود."
            }
            rawMessage.contains("NOT_FOUND", ignoreCase = true) || rawMessage.contains("No document to update", ignoreCase = true) -> {
                "محاولة تحديث وثيقة غير موجودة مسبقاً في Firestore باستخدام update() بدلاً من set(merge)." to
                    "1. استخدم set(data, SetOptions.merge()) بدلاً من update().\n2. تحقق من تطابق معرف الوثيقة (Document ID) مثل رقم الهاتف أو المعرف الفريد."
            }
            rawMessage.contains("UNAVAILABLE", ignoreCase = true) || rawMessage.contains("network", ignoreCase = true) -> {
                "انقطاع مؤقت في الاتصال بخوادم Firebase أو بطء في الشبكة." to
                    "1. تحقق من اتصال الإنترنت.\n2. تأكد من تفعيل Offline Persistence في إعدادات Firestore."
            }
            rawMessage.contains("NullPointer", ignoreCase = true) -> {
                "محاولة الوصول إلى حقل فارغ (null) أو وثيقة لم تكتمل قراءتها بعد." to
                    "1. أضف فحص null-safe (?.) وقيمة افتراضية (?:) قبل قراءة الحقل.\n2. تأكد من وجود جميع الحقول الافتراضية في Data Class."
            }
            rawMessage.contains("DEADLINE_EXCEEDED", ignoreCase = true) || rawMessage.contains("timeout", ignoreCase = true) -> {
                "استغرقت العملية وقتاً أطول من الحد المسموح به (Timeout)." to
                    "1. قلل حجم الدفعة (Batch) أو استخدم فهارس Firestore المناسبة.\n2. راجع جودة الاتصال بالشبكة."
            }
            else -> {
                "حدث استثناء أثناء تنفيذ العملية في ($fileName:$lineNumber) داخل الدالة $functionName." to
                    "1. راجع السطر $lineNumber في الملف $fileName.\n2. تحقق من توافق أنواع الحقول المرسلة إلى Firestore مع النموذج البرمجي."
            }
        }

        return TestError(
            errorIndex = errorIndex,
            testName = testName,
            failedStep = failedStep,
            fileName = fileName,
            lineNumber = lineNumber,
            functionName = functionName,
            timestamp = timestamp,
            errorType = errorType,
            fullMessage = rawMessage,
            probableCause = cause,
            suggestedFix = fix,
            expectedOutcome = expected,
            actualOutcome = "فشل العملية: $errorType ($rawMessage)"
        )
    }
}

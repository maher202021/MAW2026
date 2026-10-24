package com.serviseyem.utils

import com.serviseyem.utils.*

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * 🛠️ Centralized Application Error Hierarchy
 */
sealed class AppError(
    open val messageArabic: String,
    open val userActionArabic: String,
    open val cause: Throwable? = null
) {
    data class NetworkError(
        override val cause: Throwable? = null
    ) : AppError(
        messageArabic = "تعذر الاتصال بالسيرفر. يرجى التحقق من اتصال الإنترنت لديك.",
        userActionArabic = "تأكد من تفعيل الواي فاي أو بيانات الهاتف واضغط إعادة المحاولة.",
        cause = cause
    )

    data class TimeoutError(
        override val cause: Throwable? = null
    ) : AppError(
        messageArabic = "استغرقت الاستجابة وقتاً أطول من المتوقع.",
        userActionArabic = "حاول مرة أخرى في وقت لاحق.",
        cause = cause
    )

    data class UnauthorizedError(
        val reason: String = ""
    ) : AppError(
        messageArabic = "ليس لديك صلاحية للوصول إلى هذه البيانات أو القيام بهذا الإجراء.",
        userActionArabic = "يرجى تسجيل الدخول مجدداً أو التواصل مع الإدارة.",
        cause = null
    )

    data class PermissionDenied(
        val permissionName: String
    ) : AppError(
        messageArabic = "تم رفض الإذن المطلوبة ($permissionName).",
        userActionArabic = "يرجى منح الإذن من إعدادات التطبيق بالهاتف.",
        cause = null
    )

    data class FirebaseError(
        val code: String,
        override val cause: Throwable? = null
    ) : AppError(
        messageArabic = "حدث خطأ في مزامنة البيانات السحابية ($code).",
        userActionArabic = "سيتم تحديث البيانات تلقائياً عند استقرار الاتصال.",
        cause = cause
    )

    data class ValidationError(
        val fieldName: String,
        val detail: String
    ) : AppError(
        messageArabic = "البيانات المدخلة في ($fieldName) غير صحيحة: $detail",
        userActionArabic = "يرجى تعديل المدخلات ومحاولة الإرسال مجدداً.",
        cause = null
    )

    data class UnknownError(
        val detailMessage: String = "",
        override val cause: Throwable? = null
    ) : AppError(
        messageArabic = "حدث خطأ غير متوقع أثناء معالجة الطلب.",
        userActionArabic = "يرجى المحاولة مرة أخرى لاحقاً. (التفاصيل الفنية: ${if (detailMessage.isNotBlank()) detailMessage else "غير معروف"})",
        cause = cause
    )
}

/**
 * 📦 Unified Result Wrapper Pattern
 */
sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val error: AppError) : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data

    inline fun onSuccess(action: (T) -> Unit): AppResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (AppError) -> Unit): AppResult<T> {
        if (this is Error) action(error)
        return this
    }

    companion object {
        inline fun <T> runCatchingApp(block: () -> T): AppResult<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                val appError = when {
                    e is java.net.UnknownHostException || e is java.net.SocketException -> AppError.NetworkError(e)
                    e is java.util.concurrent.TimeoutException -> AppError.TimeoutError(e)
                    e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true -> AppError.UnauthorizedError(e.message ?: "")
                    else -> AppError.UnknownError(e.message ?: "خطأ غير معروف", e)
                }
                CrashlyticsDiagnosticLogger.logException(appError, "runCatchingApp block failure")
                Error(appError)
            }
        }
    }
}

/**
 * 🔄 Smart Exponential Backoff Retry Utility
 */
suspend fun <T> withExponentialBackoffRetry(
    maxAttempts: Int = 3,
    initialDelayMs: Long = 1000L,
    factor: Double = 2.0,
    block: suspend () -> T
): AppResult<T> {
    var currentDelay = initialDelayMs
    var lastException: Throwable? = null

    for (attempt in 1..maxAttempts) {
        try {
            return AppResult.Success(block())
        } catch (e: Exception) {
            lastException = e
            if (attempt == maxAttempts) break
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
        }
    }

    val finalError = when (lastException) {
        is java.net.UnknownHostException, is java.net.SocketException -> AppError.NetworkError(lastException)
        is java.util.concurrent.TimeoutException -> AppError.TimeoutError(lastException)
        else -> AppError.UnknownError(lastException?.message ?: "فشلت المحاولات المتكررة", lastException)
    }
    CrashlyticsDiagnosticLogger.logException(finalError, "ExponentialBackoff failed after $maxAttempts attempts")
    return AppResult.Error(finalError)
}

data class CriticalError(
    val error: AppError,
    val context: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 📱 Crashlytics & Diagnostic Event Logging Manager
 */
object CrashlyticsDiagnosticLogger {
    private val criticalErrorsList = mutableListOf<CriticalError>()

    fun logEvent(tag: String, message: String, userRole: String = "USER") {
        try {
            val deviceInfo = "Device: ${Build.MANUFACTURER} ${Build.MODEL}, API: ${Build.VERSION.SDK_INT}"
            android.util.Log.d("AppDiagnostics", "[$tag] Role: $userRole | $message | $deviceInfo")
        } catch (e: Throwable) {}
    }

    fun logException(error: AppError, contextInfo: String = "") {
        try {
            android.util.Log.e(
                "AppDiagnostics",
                "❌ EXCEPTION LOGGED [$contextInfo]: ${error.messageArabic} (Cause: ${error.cause?.message})"
            )
        } catch (e: Throwable) {}
        try {
            val crashlyticsClass = Class.forName("com.google.firebase.crashlytics.FirebaseCrashlytics")
            val getInstanceMethod = crashlyticsClass.getMethod("getInstance")
            val instance = getInstanceMethod.invoke(null)
            val recordExceptionMethod = crashlyticsClass.getMethod("recordException", Throwable::class.java)
            recordExceptionMethod.invoke(instance, error.cause ?: Exception(error.messageArabic))
        } catch (e: Throwable) {
            // Fallback if Crashlytics not available in runtime
        }

        if (error is AppError.NetworkError || error is AppError.UnauthorizedError) {
            synchronized(criticalErrorsList) {
                if (criticalErrorsList.size >= 50) criticalErrorsList.removeAt(0)
                criticalErrorsList.add(CriticalError(error, contextInfo))
            }
        }
    }

    fun getCriticalErrors(): List<CriticalError> = synchronized(criticalErrorsList) { criticalErrorsList.toList() }

    fun toastError(context: Context, error: AppError) {
        Toast.makeText(context, "⚠️ ${error.messageArabic}", Toast.LENGTH_LONG).show()
    }
}

/**
 * 🚨 Standardized Arabic Error Dialog Composable
 */
@Composable
fun AppErrorDialog(
    error: AppError,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFF1F2937),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠️ ", fontSize = 20.sp)
                Text("تنبيه النظام", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = error.messageArabic,
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "💡 النصيحة: ${error.userActionArabic}",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        },
        confirmButton = {
            if (onRetry != null) {
                Button(
                    onClick = {
                        onDismiss()
                        onRetry()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إعادة المحاولة 🔄", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("حسناً إغلاق", color = Color.LightGray, fontSize = 12.sp)
            }
        }
    )
}

/**
 * 🛡️ Screen Error Boundary Composable
 */
@Composable
fun AppErrorBoundary(
    content: @Composable () -> Unit
) {
    var caughtError by remember { mutableStateOf<Throwable?>(null) }

    DisposableEffect(Unit) {
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        val customHandler = Thread.UncaughtExceptionHandler { thread, throwable ->
            caughtError = throwable
            val appError = AppError.UnknownError(
                detailMessage = throwable.message ?: "حدث خطأ أثناء عرض واجهة المستخدم",
                cause = throwable
            )
            CrashlyticsDiagnosticLogger.logException(appError, "AppErrorBoundary")
        }
        Thread.setDefaultUncaughtExceptionHandler(customHandler)
        onDispose {
            Thread.setDefaultUncaughtExceptionHandler(originalHandler)
        }
    }

    if (caughtError != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111827))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("⚠️ حدث تعثر في عرض الشاشة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    Text(
                        text = "تم حماية الشاشة لتفادي توقف التطبيق. يرجى المحاولة مرة أخرى أو تحديث الصفحة.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { caughtError = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("إعادة تحميل الواجهة 🔄", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        content()
    }
}

object ErrorHandler {
    fun getLocalizedMessage(throwable: Throwable, defaultMessage: String = "حدث خطأ غير متوقع"): String {
        return when (throwable) {
            is java.net.UnknownHostException -> "تعذر الاتصال بالشبكة. يرجى التحقق من اتصال الإنترنت لديك."
            is java.net.SocketTimeoutException -> "انتهت مهلة الاتصال بالإنترنت. يرجى المحاولة مرة أخرى."
            is java.io.IOException -> "تعذر الاتصال بالإنترنت أو قراءة البيانات. يرجى إعادة المحاولة."
            else -> throwable.localizedMessage ?: defaultMessage
        }
    }

    fun handleError(
        context: Context,
        throwable: Throwable,
        defaultMessage: String = "حدث خطأ غير متوقع"
    ) {
        val message = getLocalizedMessage(throwable, defaultMessage)
        Toast.makeText(context, "⚠️ $defaultMessage: $message", Toast.LENGTH_SHORT).show()
        AppErrorLogManager.logFirestoreError("ErrorHandler", defaultMessage, throwable)
    }

    fun handleError(
        error: AppError,
        onShowSnackbar: (String) -> Unit,
        onLogError: (String) -> Unit = {}
    ) {
        val message = error.messageArabic
        onShowSnackbar(message)
        onLogError("[$error] $message - ${error.userActionArabic}")
    }
}


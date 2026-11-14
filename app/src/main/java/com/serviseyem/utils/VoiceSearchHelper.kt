package com.serviseyem.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat

enum class VoiceStatus {
    IDLE,
    LISTENING,
    ERROR
}

data class VoiceState(
    val status: VoiceStatus = VoiceStatus.IDLE,
    val partial: String = "",
    val message: String = "",
    val canOpenSettings: Boolean = false,
    val canUseKeyboard: Boolean = false
)

/**
 * 🎙️ VoiceSearchHelper
 * تعرف صوتي داخل التطبيق عبر Android SpeechRecognizer مع سلسلة لغات عربية احتياطية:
 * ar-YE -> ar-SA -> ar-EG -> ar
 * وتجنب فتح شاشة Google الخارجية "تنزيل لغات الكلام".
 */
class VoiceSearchHelper(
    private val context: Context,
    private val onFinal: (String) -> Unit,
    private val onState: (VoiceState) -> Unit
) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val fallbackLanguages = listOf("ar-YE", "ar-SA", "ar-EG", "ar")

    private var recognizer: SpeechRecognizer? = null
    private var langIndex: Int = 0
    private var isDestroyed: Boolean = false

    fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isAvailable(): Boolean {
        return try {
            SpeechRecognizer.isRecognitionAvailable(appContext)
        } catch (_: Exception) {
            false
        }
    }

    fun start() {
        if (isDestroyed) return
        if (!hasMicPermission()) {
            onState(
                VoiceState(
                    status = VoiceStatus.ERROR,
                    message = "يرجى منح صلاحية الميكروفون لاستخدام البحث الصوتي.",
                    canOpenSettings = true,
                    canUseKeyboard = true
                )
            )
            return
        }
        if (!isAvailable()) {
            onState(
                VoiceState(
                    status = VoiceStatus.ERROR,
                    message = "خدمة التعرف الصوتي غير متوفرة على هذا الجهاز. يمكنك استخدام ميكروفون لوحة المفاتيح.",
                    canOpenSettings = true,
                    canUseKeyboard = true
                )
            )
            return
        }
        langIndex = 0
        listen()
    }

    private fun listen() {
        if (isDestroyed) return
        val runOnMain = Runnable {
            if (isDestroyed) return@Runnable
            try {
                try {
                    recognizer?.destroy()
                } catch (_: Exception) {
                }
                recognizer = null

                val sr = SpeechRecognizer.createSpeechRecognizer(appContext)
                recognizer = sr
                val currentLang = fallbackLanguages.getOrElse(langIndex) { "ar" }

                sr.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        onState(
                            VoiceState(
                                status = VoiceStatus.LISTENING,
                                partial = "",
                                message = ""
                            )
                        )
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        handleError(error)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull { it.isNotBlank() }?.trim()
                        if (!text.isNullOrEmpty()) {
                            onState(VoiceState(status = VoiceStatus.IDLE))
                            onFinal(text)
                        } else {
                            handleError(SpeechRecognizer.ERROR_NO_MATCH)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partialText = matches?.firstOrNull()?.trim().orEmpty()
                        if (partialText.isNotEmpty()) {
                            onState(
                                VoiceState(
                                    status = VoiceStatus.LISTENING,
                                    partial = partialText
                                )
                            )
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLang)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLang)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                onState(VoiceState(status = VoiceStatus.LISTENING, partial = ""))
                sr.startListening(intent)
            } catch (_: Exception) {
                onState(
                    VoiceState(
                        status = VoiceStatus.ERROR,
                        message = "تعذر تشغيل محرك التعرف الصوتي. يمكنك استخدام ميكروفون لوحة المفاتيح.",
                        canOpenSettings = true,
                        canUseKeyboard = true
                    )
                )
            }
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            runOnMain.run()
        } else {
            mainHandler.post(runOnMain)
        }
    }

    private fun handleError(error: Int) {
        if (isDestroyed) return

        // Error 12 = ERROR_LANGUAGE_NOT_SUPPORTED, Error 13 = ERROR_LANGUAGE_UNAVAILABLE
        if ((error == 12 || error == 13) && langIndex + 1 < fallbackLanguages.size) {
            langIndex++
            listen()
            return
        }

        val state = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceState(
                status = VoiceStatus.ERROR,
                message = "لم يتم التقاط كلام واضح. حاول التحدث بوضوح أو قريباً من الميكروفون.",
                canOpenSettings = false,
                canUseKeyboard = true
            )

            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
            SpeechRecognizer.ERROR_SERVER -> VoiceState(
                status = VoiceStatus.ERROR,
                message = "تعذر الاتصال بخدمة التعرف الصوتي. تأكد من الإنترنت أو استعمل ميكروفون لوحة المفاتيح.",
                canOpenSettings = false,
                canUseKeyboard = true
            )

            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceState(
                status = VoiceStatus.ERROR,
                message = "صلاحية الميكروفون مطلوبة للبحث الصوتي.",
                canOpenSettings = true,
                canUseKeyboard = true
            )

            SpeechRecognizer.ERROR_AUDIO -> VoiceState(
                status = VoiceStatus.ERROR,
                message = "تعذر تسجيل الصوت من الميكروفون حالياً.",
                canOpenSettings = false,
                canUseKeyboard = true
            )

            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> VoiceState(
                status = VoiceStatus.ERROR,
                message = "خدمة الصوت مشغولة حالياً، أعد المحاولة بعد لحظة.",
                canOpenSettings = false,
                canUseKeyboard = true
            )

            12, 13 -> VoiceState(
                status = VoiceStatus.ERROR,
                message = "حزمة اللغة العربية غير مفعّلة في محرك الصوت. افتح إعدادات الإدخال الصوتي أو استعمل ميكروفون لوحة المفاتيح.",
                canOpenSettings = true,
                canUseKeyboard = true
            )

            else -> VoiceState(
                status = VoiceStatus.ERROR,
                message = "تعذر إتمام البحث الصوتي حالياً. يمكنك استخدام ميكروفون لوحة المفاتيح.",
                canOpenSettings = true,
                canUseKeyboard = true
            )
        }
        onState(state)
    }

    fun stop() {
        mainHandler.post {
            try {
                recognizer?.stopListening()
            } catch (_: Exception) {
            }
        }
    }

    fun cancel() {
        mainHandler.post {
            try {
                recognizer?.cancel()
            } catch (_: Exception) {
            }
            onState(VoiceState(status = VoiceStatus.IDLE))
        }
    }

    fun destroy() {
        isDestroyed = true
        val cleanup = Runnable {
            try {
                recognizer?.cancel()
            } catch (_: Exception) {
            }
            try {
                recognizer?.destroy()
            } catch (_: Exception) {
            }
            recognizer = null
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            cleanup.run()
        } else {
            mainHandler.post(cleanup)
        }
    }

    companion object {
        fun openVoiceSettings(context: Context) {
            val actions = listOf(
                "android.settings.VOICE_INPUT_SETTINGS",
                Settings.ACTION_LOCALE_SETTINGS,
                Settings.ACTION_SETTINGS
            )
            for (action in actions) {
                try {
                    val intent = Intent(action).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return
                } catch (_: Exception) {
                }
            }
        }
    }
}

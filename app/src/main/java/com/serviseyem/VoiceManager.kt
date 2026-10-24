package com.serviseyem

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * 🎙️ VoiceManager
 * محرك الصوت المتكامل: يدعم واجهات الاستماع القديمة والجديدة وتحويل النص إلى كلام (TTS)
 */
object VoiceManager : TextToSpeech.OnInitListener {

    var onSpeak: ((String) -> Unit)? = null
    var onHear: (((String) -> Unit) -> Unit)? = null

    private var tts: TextToSpeech? = null
    @Volatile
    private var isInitialized = false
    @Volatile
    private var isInitializing = false
    @Volatile
    private var isDispatchingOnSpeak = false

    @Volatile
    private var pendingSpeechText: String? = null
    @Volatile
    private var pendingUtteranceId: String = "ai_speech"
    @Volatile
    private var activeUtteranceId: String = "ai_speech"
    private val mainHandler = Handler(Looper.getMainLooper())

    var isSpeakingCallback: ((Boolean) -> Unit)? = null

    @Synchronized
    fun init(context: Context) {
        if (tts == null && !isInitializing) {
            try {
                isInitializing = true
                tts = TextToSpeech(context.applicationContext, this)
            } catch (e: Exception) {
                isInitializing = false
                Log.e("VoiceManager", "Error initializing TTS", e)
            }
        }
    }

    override fun onInit(status: Int) {
        isInitializing = false
        if (status == TextToSpeech.SUCCESS) {
            try {
                val yemeniLocale = Locale.forLanguageTag("ar-YE")
                val arabicLocale = Locale.forLanguageTag("ar")
                var result = tts?.setLanguage(yemeniLocale) ?: TextToSpeech.LANG_MISSING_DATA
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    result = tts?.setLanguage(arabicLocale) ?: TextToSpeech.LANG_MISSING_DATA
                }
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.getDefault())
                }
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(0.95f)
                isInitialized = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        if (utteranceId != null) activeUtteranceId = utteranceId
                        mainHandler.post { isSpeakingCallback?.invoke(true) }
                    }

                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == null || utteranceId == activeUtteranceId) {
                            mainHandler.post { isSpeakingCallback?.invoke(false) }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (utteranceId == null || utteranceId == activeUtteranceId) {
                            mainHandler.post { isSpeakingCallback?.invoke(false) }
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        if (utteranceId == null || utteranceId == activeUtteranceId) {
                            mainHandler.post { isSpeakingCallback?.invoke(false) }
                        }
                    }
                })

                val queuedText = pendingSpeechText
                val queuedId = pendingUtteranceId
                pendingSpeechText = null
                if (!queuedText.isNullOrBlank()) {
                    activeUtteranceId = queuedId
                    val params = Bundle()
                    tts?.speak(queuedText, TextToSpeech.QUEUE_FLUSH, params, queuedId)
                }
            } catch (e: Exception) {
                Log.e("VoiceManager", "Error configuring TTS onInit", e)
                isInitialized = false
                pendingSpeechText = null
                try { tts?.shutdown() } catch (_: Exception) {}
                tts = null
            }
        } else {
            isInitialized = false
            pendingSpeechText = null
            try { tts?.shutdown() } catch (_: Exception) {}
            tts = null
            Log.w("VoiceManager", "TTS initialization failed with status: $status")
        }
    }

    fun speak(text: String, utteranceId: String = "ai_speech") {
        if (text.isBlank()) return
        activeUtteranceId = utteranceId
        if (isInitialized && tts != null) {
            try {
                val params = Bundle()
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            } catch (e: Exception) {
                Log.e("VoiceManager", "Error speaking text", e)
            }
        } else {
            pendingSpeechText = text
            pendingUtteranceId = utteranceId
            if (!isDispatchingOnSpeak && tts == null && !isInitializing) {
                try {
                    isDispatchingOnSpeak = true
                    onSpeak?.invoke(text)
                } finally {
                    isDispatchingOnSpeak = false
                }
            }
        }
    }

    fun stop() {
        try {
            pendingSpeechText = null
            tts?.stop()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error stopping TTS", e)
        }
        mainHandler.post { isSpeakingCallback?.invoke(false) }
    }

    fun shutdown() {
        try {
            pendingSpeechText = null
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error shutting down TTS", e)
        } finally {
            tts = null
            isInitialized = false
            isInitializing = false
            isDispatchingOnSpeak = false
            isSpeakingCallback = null
            onSpeak = null
            onHear = null
        }
    }

    fun getInstance(context: Context): VoiceManager {
        init(context)
        return this
    }
}

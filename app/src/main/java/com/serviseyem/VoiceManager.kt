package com.serviseyem

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * 🎙️ VoiceManager
 * محرك الصوت المتكامل: يدعم التوجيه الصوتي الملاحي وتحويل النص إلى كلام (TTS) مع إدارة AudioFocus.
 */
object VoiceManager : TextToSpeech.OnInitListener {

    var onSpeak: ((String) -> Unit)? = null
    var onHear: (((String) -> Unit) -> Unit)? = null

    private var appContext: Context? = null
    private var tts: TextToSpeech? = null
    private var focusRequest: AudioFocusRequest? = null
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
        appContext = context.applicationContext
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
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts?.setAudioAttributes(attrs)

                val candidateLocales = listOf(
                    Locale.forLanguageTag("ar-YE"),
                    Locale.forLanguageTag("ar-SA"),
                    Locale.forLanguageTag("ar-AE"),
                    Locale.forLanguageTag("ar-EG"),
                    Locale.forLanguageTag("ar")
                )
                var chosen: Locale? = null
                for (loc in candidateLocales) {
                    val avail = tts?.isLanguageAvailable(loc) ?: TextToSpeech.LANG_NOT_SUPPORTED
                    if (avail >= TextToSpeech.LANG_AVAILABLE) {
                        chosen = loc
                        break
                    }
                }
                val result = tts?.setLanguage(chosen ?: Locale.forLanguageTag("ar")) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    isInitialized = false
                    Log.w("VoiceManager", "Arabic TTS not supported on this device; skipping audio to avoid corrupt output")
                    return
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
                        abandonAudioFocus()
                        if (utteranceId == null || utteranceId == activeUtteranceId) {
                            mainHandler.post { isSpeakingCallback?.invoke(false) }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        abandonAudioFocus()
                        if (utteranceId == null || utteranceId == activeUtteranceId) {
                            mainHandler.post { isSpeakingCallback?.invoke(false) }
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        abandonAudioFocus()
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
                    requestAudioFocus()
                    tts?.speak(queuedText, TextToSpeech.QUEUE_FLUSH, Bundle(), queuedId)
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
        val yemeniText = com.serviseyem.utils.YemeniDialectVoiceEngine.toYemeniDialect(text)
        activeUtteranceId = utteranceId
        if (isInitialized && tts != null) {
            try {
                requestAudioFocus()
                tts?.speak(yemeniText, TextToSpeech.QUEUE_FLUSH, Bundle(), utteranceId)
            } catch (e: Exception) {
                Log.e("VoiceManager", "Error speaking text", e)
            }
        } else {
            pendingSpeechText = yemeniText
            pendingUtteranceId = utteranceId
            appContext?.let { ctx ->
                if (tts == null && !isInitializing) {
                    init(ctx)
                }
            }
            if (!isDispatchingOnSpeak && tts == null && !isInitializing) {
                try {
                    isDispatchingOnSpeak = true
                    onSpeak?.invoke(yemeniText)
                } finally {
                    isDispatchingOnSpeak = false
                }
            }
        }
    }

    fun speak(context: Context, text: String, utteranceId: String = "ai_speech") {
        if (appContext == null || (tts == null && !isInitializing)) {
            init(context)
        }
        speak(text, utteranceId)
    }

    fun stop() {
        try {
            pendingSpeechText = null
            tts?.stop()
            abandonAudioFocus()
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
            abandonAudioFocus()
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

    private fun requestAudioFocus() {
        val am = appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (focusRequest == null) {
                    val attrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(attrs)
                        .setAcceptsDelayedFocusGain(false)
                        .build()
                }
                focusRequest?.let { am.requestAudioFocus(it) }
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            }
        } catch (_: Exception) {
        }
    }

    private fun abandonAudioFocus() {
        val am = appContext?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { am.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        } catch (_: Exception) {
        }
    }

    fun getInstance(context: Context): VoiceManager {
        init(context)
        return this
    }
}

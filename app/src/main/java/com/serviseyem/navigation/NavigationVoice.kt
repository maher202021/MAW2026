package com.serviseyem.navigation

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.serviseyem.utils.YemeniDialectVoiceEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * 🔊 NavigationVoice
 * Production-grade spoken voice guidance in Arabic (Yemeni dialect) with:
 * - Pre-init utterance queue so initial "Start Navigation" speech is never dropped.
 * - Speed-scaled announcement intervals (~20s, ~8s, and ~2s before maneuver).
 * - System AudioAttributes & AudioFocus request with ducking support.
 * - Fallback locale chain (ar-YE -> ar-SA -> ar-AE -> ar-EG -> ar).
 */
class NavigationVoice(private val context: Context) : UtteranceProgressListener() {

    private val TAG = "NavigationVoice"
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var focusRequest: AudioFocusRequest? = null

    @Volatile
    private var isReady = false
    private val pendingQueue = ArrayDeque<Pair<String, Int>>()
    private val queueLock = Any()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isArabicSupported = MutableStateFlow(true)
    val isArabicSupported: StateFlow<Boolean> = _isArabicSupported.asStateFlow()

    private var lastAnnouncedKey = ""
    private var lastAnnouncedDistanceBucket = -1

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val engine = tts ?: return@TextToSpeech
                    val attrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    engine.setAudioAttributes(attrs)
                    engine.setPitch(1.0f)
                    engine.setSpeechRate(0.95f)
                    engine.setOnUtteranceProgressListener(this)

                    val candidates = listOf(
                        Locale.forLanguageTag("ar-YE"),
                        Locale.forLanguageTag("ar-SA"),
                        Locale.forLanguageTag("ar-AE"),
                        Locale.forLanguageTag("ar-EG"),
                        Locale.forLanguageTag("ar")
                    )
                    var foundLocale: Locale? = null
                    for (loc in candidates) {
                        val avail = engine.isLanguageAvailable(loc)
                        if (avail >= TextToSpeech.LANG_AVAILABLE) {
                            foundLocale = loc
                            break
                        }
                    }

                    if (foundLocale != null) {
                        engine.language = foundLocale
                        _isArabicSupported.value = true
                    } else {
                        Log.w(TAG, "Arabic TTS voice not installed; attempting best-effort locale.")
                        val fallbackRes = engine.setLanguage(Locale.forLanguageTag("ar"))
                        if (fallbackRes < TextToSpeech.LANG_AVAILABLE) {
                            engine.language = Locale.getDefault()
                            _isArabicSupported.value = false
                        } else {
                            _isArabicSupported.value = true
                        }
                    }

                    val toSpeak: List<Pair<String, Int>>
                    synchronized(queueLock) {
                        isReady = true
                        toSpeak = pendingQueue.toList()
                        pendingQueue.clear()
                    }
                    toSpeak.forEach { (text, mode) ->
                        if (!_isMuted.value) {
                            dispatchSpeak(text, mode)
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to initialize TextToSpeech engine: status=$status")
                    _isArabicSupported.value = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating TextToSpeech", e)
            _isArabicSupported.value = false
        }
    }

    fun setMuted(muted: Boolean) {
        _isMuted.value = muted
        if (muted) {
            stop()
        }
    }

    fun toggleMute(): Boolean {
        val newState = !_isMuted.value
        setMuted(newState)
        return newState
    }

    fun processTurnAnnouncement(
        instructionKey: String,
        instructionText: String,
        distanceToTurnMeters: Double,
        currentSpeedKmh: Float
    ) {
        if (_isMuted.value) return

        val speedMps = (currentSpeedKmh.coerceAtLeast(20f) * 1000.0) / 3600.0

        val farDistance = (speedMps * 20.0).coerceIn(200.0, 800.0)
        val midDistance = (speedMps * 8.0).coerceIn(80.0, 250.0)
        val finalDistance = (speedMps * 2.5).coerceIn(25.0, 50.0)

        val bucket = when {
            distanceToTurnMeters <= finalDistance -> 3
            distanceToTurnMeters <= midDistance -> 2
            distanceToTurnMeters <= farDistance -> 1
            else -> 0
        }

        if (bucket == 0) return
        if (instructionKey == lastAnnouncedKey && bucket == lastAnnouncedDistanceBucket) return

        lastAnnouncedKey = instructionKey
        lastAnnouncedDistanceBucket = bucket

        val speechText = when (bucket) {
            3 -> "الحين، ${toYemeniDialect(instructionText)}"
            2 -> "بعد ${formatYemeniDistance(distanceToTurnMeters)}، ${toYemeniDialect(instructionText)}"
            1 -> "بعد ${formatYemeniDistance(distanceToTurnMeters)}، ${toYemeniDialect(instructionText)}"
            else -> toYemeniDialect(instructionText)
        }

        speak(speechText, queueMode = TextToSpeech.QUEUE_FLUSH)
    }

    private fun toYemeniDialect(text: String): String {
        var res = YemeniDialectVoiceEngine.toYemeniDialect(text)
        val dialectMap = mapOf(
            "انعطف يساراً" to "لف يسار",
            "انعطف يميناً" to "لف يمين",
            "اتجه إلى اليسار" to "خلك على اليسار",
            "اتجه إلى اليمين" to "خلك على اليمين",
            "تابع السير" to "واصل على طول",
            "استمر في السير" to "واصل سيدة",
            "استمر إلى الأمام" to "واصل قبالك",
            "عند الدوار" to "عند الجولة",
            "الدوار" to "الجولة",
            "كيلومتر" to "كيلو"
        )
        for ((fusha, ammiya) in dialectMap) {
            res = res.replace(fusha, ammiya)
        }
        return res
    }

    private fun formatYemeniDistance(meters: Double): String {
        return when {
            meters in 180.0..220.0 -> "ميتين متر"
            meters in 90.0..115.0 -> "مية متر"
            meters in 400.0..600.0 -> "نص كيلو"
            else -> HaversinePrecisionNavigator.formatDistanceArabic(meters)
        }
    }

    fun announceArrival(destinationName: String) {
        if (_isMuted.value) return
        val text = "وصلت بالسلامة يا غالي إلى وجهتك: $destinationName."
        speak(text, queueMode = TextToSpeech.QUEUE_FLUSH)
    }

    fun announceReroute() {
        if (_isMuted.value) return
        lastAnnouncedKey = ""
        lastAnnouncedDistanceBucket = -1
        speak("بنعدل المسار الحين...", queueMode = TextToSpeech.QUEUE_FLUSH)
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_ADD) {
        if (_isMuted.value || text.isBlank()) return
        val formatted = toYemeniDialect(text)
        if (!isReady) {
            synchronized(queueLock) {
                if (!isReady) {
                    if (queueMode == TextToSpeech.QUEUE_FLUSH) {
                        pendingQueue.clear()
                    } else if (pendingQueue.size >= 3) {
                        pendingQueue.removeFirst()
                    }
                    pendingQueue.addLast(formatted to queueMode)
                    return
                }
            }
        }
        dispatchSpeak(formatted, queueMode)
    }

    private fun dispatchSpeak(text: String, queueMode: Int) {
        requestAudioFocus()
        val utteranceId = "NAV_VOICE_${System.currentTimeMillis()}"
        try {
            tts?.speak(text, queueMode, Bundle(), utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error dispatching speech: ${e.message}")
            abandonAudioFocus()
        }
    }

    fun stop() {
        synchronized(queueLock) {
            pendingQueue.clear()
        }
        try {
            tts?.stop()
        } catch (_: Exception) {}
        abandonAudioFocus()
    }

    fun destroy() {
        isReady = false
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }

    private fun requestAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (focusRequest == null) {
                    val attributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()

                    focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(attributes)
                        .setAcceptsDelayedFocusGain(false)
                        .build()
                }
                focusRequest?.let { audioManager?.requestAudioFocus(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(
                    null,
                    AudioManager.STREAM_NOTIFICATION,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
            }
        } catch (_: Exception) {}
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(null)
            }
        } catch (_: Exception) {}
    }

    override fun onStart(utteranceId: String?) {}

    override fun onDone(utteranceId: String?) {
        abandonAudioFocus()
    }

    @Deprecated("Deprecated in Java")
    override fun onError(utteranceId: String?) {
        abandonAudioFocus()
    }

    override fun onError(utteranceId: String?, errorCode: Int) {
        abandonAudioFocus()
    }
}

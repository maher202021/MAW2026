package com.serviseyem.navigation

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * 🔊 NavigationVoice
 * Production-grade spoken voice guidance in Arabic with:
 * - Speed-scaled announcement intervals (~20s, ~8s, and ~2s before maneuver).
 * - System AudioFocus request with ducking support.
 * - Queue flushing on reroute and duplicate announcement prevention.
 * - Arabic TTS language support detection.
 */
class NavigationVoice(private val context: Context) : UtteranceProgressListener() {

    private val TAG = "NavigationVoice"
    private var tts: TextToSpeech? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var focusRequest: AudioFocusRequest? = null

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
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val arLocale = Locale("ar")
                val langResult = tts?.isLanguageAvailable(arLocale)
                if (langResult == TextToSpeech.LANG_AVAILABLE || langResult == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    tts?.language = arLocale
                    _isArabicSupported.value = true
                } else {
                    Log.w(TAG, "Arabic TTS voice not installed on device.")
                    _isArabicSupported.value = false
                }
                tts?.setOnUtteranceProgressListener(this)
            } else {
                Log.e(TAG, "Failed to initialize TextToSpeech engine.")
            }
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

    /**
     * Checks if an announcement should be made based on vehicle speed and distance to turn.
     * Announcement buckets:
     * - Far call: ~20 seconds before turn (clamped between 200m and 800m).
     * - Mid call: ~8 seconds before turn (clamped between 80m and 250m).
     * - Final call: ~2 seconds before turn (clamped between 25m and 50m).
     */
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

        // Prevent repeating the same bucket for the same maneuver
        if (instructionKey == lastAnnouncedKey && bucket == lastAnnouncedDistanceBucket) {
            return
        }

        lastAnnouncedKey = instructionKey
        lastAnnouncedDistanceBucket = bucket

        val speechText = when (bucket) {
            3 -> "الآن، $instructionText"
            2 -> "بعد ${HaversinePrecisionNavigator.formatDistanceArabic(distanceToTurnMeters)}، $instructionText"
            1 -> "بعد ${HaversinePrecisionNavigator.formatDistanceArabic(distanceToTurnMeters)}، $instructionText"
            else -> instructionText
        }

        speak(speechText, queueMode = TextToSpeech.QUEUE_FLUSH)
    }

    fun announceArrival(destinationName: String) {
        if (_isMuted.value) return
        val text = "لقد وصلت إلى وجهتك: $destinationName."
        speak(text, queueMode = TextToSpeech.QUEUE_FLUSH)
    }

    fun announceReroute() {
        if (_isMuted.value) return
        lastAnnouncedKey = ""
        lastAnnouncedDistanceBucket = -1
        speak("جارٍ إعادة حساب المسار...", queueMode = TextToSpeech.QUEUE_FLUSH)
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_ADD) {
        if (_isMuted.value || text.isBlank()) return

        requestAudioFocus()
        val utteranceId = "NAV_VOICE_${System.currentTimeMillis()}"
        tts?.speak(text, queueMode, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        abandonAudioFocus()
    }

    fun destroy() {
        stop()
        tts?.shutdown()
        tts = null
    }

    private fun requestAudioFocus() {
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
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }

    override fun onStart(utteranceId: String?) {}

    override fun onDone(utteranceId: String?) {
        abandonAudioFocus()
    }

    override fun onError(utteranceId: String?) {
        abandonAudioFocus()
    }
}

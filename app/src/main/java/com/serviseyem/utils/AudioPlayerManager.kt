package com.serviseyem.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

/**
 * 🎵 AudioPlayerManager
 * مشغل صوتيات احترافي لإدارة تشغيل الرسائل الصوتية الحقيقية مع متابعة التقدم لحظياً
 */
object AudioPlayerManager {

    private const val TAG = "AudioPlayerManager"

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private val _currentPlayingId = MutableStateFlow<String?>(null)
    val currentPlayingId: StateFlow<String?> = _currentPlayingId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private var lastContext: Context? = null

    var onAudioFinished: ((completedMessageId: String) -> Unit)? = null

    fun play(messageId: String, audioSource: String, context: Context) {
        lastContext = context.applicationContext
        if (_currentPlayingId.value == messageId && _isPlaying.value) {
            pause()
            return
        }

        stop()

        if (audioSource.isBlank()) {
            Log.e(TAG, "Cannot play audio: source is blank")
            return
        }

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }

            if (audioSource.startsWith("data:audio") || isLikelyBase64(audioSource)) {
                val base64Data = if (audioSource.contains(",")) {
                    audioSource.substringAfter(",")
                } else {
                    audioSource
                }
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                val tempFile = File(context.cacheDir, "temp_voice_play.m4a")
                FileOutputStream(tempFile).use { it.write(decodedBytes) }
                player.setDataSource(tempFile.absolutePath)
            } else if (audioSource.startsWith("http://") || audioSource.startsWith("https://")) {
                player.setDataSource(audioSource)
            } else if (audioSource.startsWith("content://") || audioSource.startsWith("file://")) {
                player.setDataSource(context, Uri.parse(audioSource))
            } else {
                val localFile = File(audioSource)
                if (localFile.exists()) {
                    player.setDataSource(localFile.absolutePath)
                } else {
                    val decodedBytes = Base64.decode(audioSource, Base64.DEFAULT)
                    val tempFile = File(context.cacheDir, "temp_voice_play.m4a")
                    FileOutputStream(tempFile).use { it.write(decodedBytes) }
                    player.setDataSource(tempFile.absolutePath)
                }
            }

            player.prepare()
            player.start()

            mediaPlayer = player
            _currentPlayingId.value = messageId
            _isPlaying.value = true
            _playbackProgress.value = 0f

            player.setOnCompletionListener {
                val finishedId = _currentPlayingId.value
                stop()
                if (finishedId != null) {
                    onAudioFinished?.invoke(finishedId)
                }
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                stop()
                true
            }

            startProgressTicker()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio playback: ${e.message}", e)
            stop()
        }
    }

    private fun isLikelyBase64(str: String): Boolean {
        return str.length > 50 && !str.contains("/") && !str.contains(" ")
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing audio: ${e.message}")
        }
        progressJob?.cancel()
    }

    fun resume() {
        try {
            if (mediaPlayer != null && !_isPlaying.value) {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressTicker()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming audio: ${e.message}")
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer: ${e.message}")
        } finally {
            mediaPlayer = null
            _currentPlayingId.value = null
            _isPlaying.value = false
            _playbackProgress.value = 0f

            // 🧹 حذف الملفات المؤقتة
            try {
                val cacheDir = lastContext?.cacheDir
                cacheDir?.listFiles()?.forEach { file ->
                    if (file.name.startsWith("temp_voice_") && file.exists()) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                // تجاهل
            }
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (_isPlaying.value && mediaPlayer != null) {
                try {
                    val current = mediaPlayer?.currentPosition ?: 0
                    val total = mediaPlayer?.duration ?: 1
                    if (total > 0) {
                        _playbackProgress.value = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    }
                } catch (e: Exception) {
                    break
                }
                delay(100)
            }
        }
    }

    fun release() {
        stop()
    }
}

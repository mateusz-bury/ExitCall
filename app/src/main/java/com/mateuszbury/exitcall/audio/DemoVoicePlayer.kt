package com.mateuszbury.exitcall.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.mateuszbury.exitcall.R

class DemoVoicePlayer(
    context: Context
) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private var mediaPlayer: MediaPlayer? = null
    private var muted = false
    private var focusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        // Do not resume the demo unexpectedly after another call or app interrupts it.
        if (change != AudioManager.AUDIOFOCUS_GAIN) {
            stop()
        }
    }

    fun play() {
        stop()
        if (!requestAudioFocus()) {
            Log.w(TAG, "Demo voice skipped: audio focus not granted")
            return
        }

        try {
            // Supply attributes at creation: create() prepares the player immediately.
            val player = MediaPlayer.create(
                appContext,
                R.raw.exitcall_demo_voice,
                audioAttributes,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            if (player == null) {
                Log.w(TAG, "Demo voice MediaPlayer could not be created")
                stop()
                return
            }
            mediaPlayer = player
            player.setOnCompletionListener {
                stop()
            }
            player.setOnErrorListener { _, what, extra ->
                Log.w(TAG, "Demo voice playback failed: what=$what extra=$extra")
                stop()
                true
            }
            val volume = if (muted) 0f else 1f
            player.setVolume(volume, volume)
            player.start()
            Log.d(TAG, "Demo voice playback started on communication audio")
        } catch (error: Exception) {
            Log.w(TAG, "Demo voice playback could not start", error)
            stop()
        }
    }

    fun setMuted(enabled: Boolean) {
        // Keep this state even before the delayed player is created.
        muted = enabled
        val volume = if (enabled) 0f else 1f
        mediaPlayer?.setVolume(volume, volume)
    }

    @Suppress("DEPRECATION")
    private fun requestAudioFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(focusListener, Handler(Looper.getMainLooper()))
                .build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            audioManager.requestAudioFocus(
                focusListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return hasAudioFocus
    }

    @Suppress("DEPRECATION")
    fun stop() {
        // release() stops playback as well, including completion/error cleanup.
        mediaPlayer?.release()
        mediaPlayer = null
        if (hasAudioFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let(audioManager::abandonAudioFocusRequest)
            } else {
                audioManager.abandonAudioFocus(focusListener)
            }
        }
        focusRequest = null
        hasAudioFocus = false
    }

    companion object {
        private const val TAG = "ExitCall"
    }
}

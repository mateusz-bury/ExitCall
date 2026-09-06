package com.mateuszbury.exitcall.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

class DemoVoicePlayer(
    context: Context
) {
    private val appContext = context.applicationContext
    private var mediaPlayer: MediaPlayer? = null

    fun play() {
        stop()

        val rawResourceId = appContext.resources.getIdentifier(
            DEMO_VOICE_RESOURCE_NAME,
            "raw",
            appContext.packageName
        )

        if (rawResourceId == 0) {
            Log.w(
                TAG,
                "Demo voice asset missing: res/raw/$DEMO_VOICE_RESOURCE_NAME.mp3"
            )
            return
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        mediaPlayer = MediaPlayer.create(
            appContext,
            rawResourceId,
            audioAttributes,
            0
        )?.apply {
            setOnCompletionListener {
                releasePlayer()
            }
            setOnErrorListener { _, what, extra ->
                Log.w(
                    TAG,
                    "Demo voice playback failed: what=$what extra=$extra"
                )
                releasePlayer()
                true
            }
            start()
        }
    }

    fun stop() {
        mediaPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) {
                    player.stop()
                }
            }
        }
        releasePlayer()
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val TAG = "ExitCall"
        private const val DEMO_VOICE_RESOURCE_NAME = "exitcall_demo_voice"
    }
}

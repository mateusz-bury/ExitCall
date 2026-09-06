package com.mateuszbury.exitcall.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.mateuszbury.exitcall.R

class DemoVoicePlayer(
    context: Context
) {
    private val appContext = context.applicationContext
    private var mediaPlayer: MediaPlayer? = null

    fun play() {
        stop()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        mediaPlayer = MediaPlayer.create(
            appContext,
            R.raw.exitcall_demo_voice,
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
    }
}

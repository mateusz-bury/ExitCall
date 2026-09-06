package com.mateuszbury.exitcall.audio

import android.content.Context
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

        mediaPlayer = MediaPlayer.create(
            appContext,
            R.raw.exitcall_demo_voice
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

            Log.d(TAG, "Demo voice playback started")
        }

        if (mediaPlayer == null) {
            Log.w(TAG, "Demo voice MediaPlayer could not be created")
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

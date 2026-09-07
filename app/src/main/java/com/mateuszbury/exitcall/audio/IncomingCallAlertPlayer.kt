package com.mateuszbury.exitcall.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class IncomingCallAlertPlayer(
    context: Context
) {
    private val appContext = context.applicationContext
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    fun start(
        ringtoneUri: String?,
        vibrate: Boolean
    ) {
        stop()
        startRingtone(ringtoneUri)
        startVibration(vibrate)
    }

    fun stop() {
        ringtone?.stop()
        ringtone = null

        vibrator?.cancel()
        vibrator = null
    }

    private fun startRingtone(configuredRingtoneUri: String?) {
        val defaultRingtoneUri = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_RINGTONE
        )

        val selectedRingtoneUri = configuredRingtoneUri
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)
            ?: defaultRingtoneUri

        ringtone = RingtoneManager.getRingtone(
            appContext,
            selectedRingtoneUri
        ) ?: RingtoneManager.getRingtone(
            appContext,
            defaultRingtoneUri
        )

        ringtone?.audioAttributes =
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.isLooping = true
        }

        ringtone?.play()

        Log.d(TAG, "Incoming-call ringtone started")
    }

    private fun startVibration(enabled: Boolean) {
        if (!enabled) {
            Log.d(TAG, "Incoming-call vibration disabled")
            return
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appContext.getSystemService(
                Context.VIBRATOR_MANAGER_SERVICE
            ) as VibratorManager

            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(
                Context.VIBRATOR_SERVICE
            ) as Vibrator
        }

        val pattern = longArrayOf(
            0,
            700,
            800
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(
                pattern,
                0
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val attributes = VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_RINGTONE)
                    .build()

                vibrator?.vibrate(
                    effect,
                    attributes
                )
            } else {
                vibrator?.vibrate(effect)
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(
                pattern,
                0
            )
        }

        Log.d(TAG, "Incoming-call vibration started")
    }

    companion object {
        private const val TAG = "ExitCall"
    }
}

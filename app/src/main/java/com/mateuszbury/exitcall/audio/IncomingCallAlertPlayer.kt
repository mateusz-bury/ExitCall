package com.mateuszbury.exitcall.audio

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class IncomingCallAlertPlayer(
    context: Context
) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private var bluetoothDeviceId: Int? = null
    private var deviceCallbackRegistered = false
    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            if (removedDevices?.any { it.id == bluetoothDeviceId } == true) {
                // Do not continue a private alert on the phone after headset disconnect.
                ringtone?.stop()
                ringtone = null
                bluetoothDeviceId = null
            }
        }
    }
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
        if (deviceCallbackRegistered) {
            audioManager.unregisterAudioDeviceCallback(deviceCallback)
            deviceCallbackRegistered = false
        }
        bluetoothDeviceId = null
        ringtone?.stop()
        ringtone = null

        vibrator?.cancel()
        vibrator = null
    }

    private fun startRingtone(configuredRingtoneUri: String?) {
        val bluetoothOutput = audioManager
            .getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .firstOrNull(::isBluetoothMediaOutput)

        // RINGTONE policy can duplicate alerts on the speaker and a BT headset.
        // Use the media path with BT, retaining ringtone semantics without accessories.
        val usage = if (bluetoothOutput != null) {
            val notificationManager = appContext.getSystemService(NotificationManager::class.java)
            if (
                audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL ||
                audioManager.getStreamVolume(AudioManager.STREAM_RING) == 0 ||
                notificationManager.currentInterruptionFilter !=
                    NotificationManager.INTERRUPTION_FILTER_ALL
            ) {
                Log.d(TAG, "Bluetooth ringtone suppressed by silent/vibrate/DND settings")
                return
            }
            bluetoothDeviceId = bluetoothOutput.id
            audioManager.registerAudioDeviceCallback(
                deviceCallback,
                Handler(Looper.getMainLooper())
            )
            deviceCallbackRegistered = true
            AudioAttributes.USAGE_MEDIA
        } else {
            AudioAttributes.USAGE_NOTIFICATION_RINGTONE
        }

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
                .setUsage(usage)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.isLooping = true
        }

        ringtone?.play()

        Log.d(TAG, "Incoming-call ringtone started: usage=$usage bluetooth=${bluetoothOutput != null}")
    }

    private fun isBluetoothMediaOutput(device: AudioDeviceInfo): Boolean {
        if (device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) {
            return true
        }
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            device.type == AudioDeviceInfo.TYPE_HEARING_AID
        ) {
            return true
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            device.type == AudioDeviceInfo.TYPE_BLE_HEADSET
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

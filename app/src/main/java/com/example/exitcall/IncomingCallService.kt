package com.example.exitcall

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationAttributes
import android.app.PendingIntent

class IncomingCallService : Service() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    companion object {
        const val CHANNEL_ID = "incoming_call_channel"
        const val NOTIFICATION_ID = 1002
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val notificationManager =
            getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {

            val canUseFullScreenIntent =
                notificationManager.canUseFullScreenIntent()

            Log.d(
                "ExitCall",
                "Can use full screen intent: $canUseFullScreenIntent"
            )
        }

        // 1. Mówimy Androidowi, jaki ekran chcemy otworzyć
        val fullScreenIntent = Intent(
            this,
            IncomingCallActivity::class.java
        )

        // 2. Pakujemy Intent w PendingIntent,
        // żeby Android mógł uruchomić Activity z notification
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            2001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Budujemy notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Incoming call")
            .setContentText("ExitCall")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)

            // 4. TO jest najważniejsza linia:
            // notification ma spróbować otworzyć IncomingCallActivity
            .setFullScreenIntent(
                fullScreenPendingIntent,
                true
            )
            .build()

        // 5. Service staje się foreground service
        startForeground(
            NOTIFICATION_ID,
            notification
        )

        // 6. Dopiero potem uruchamiamy dzwonek i wibrację
        startRingtone()
        startVibration()
    }

    private fun startRingtone() {

        val ringtoneUri = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_RINGTONE
        )

        ringtone = RingtoneManager.getRingtone(
            applicationContext,
            ringtoneUri
        )

        ringtone?.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.isLooping = true
        }

        ringtone?.play()

        Log.d("ExitCall", "Ringtone started from IncomingCallService")
    }

    private fun startVibration() {

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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

        Log.d("ExitCall", "Vibration started")
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Incoming calls",
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {

        ringtone?.stop()
        ringtone = null

        vibrator?.cancel()
        vibrator = null

        Log.d("ExitCall", "Ringtone stopped")

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
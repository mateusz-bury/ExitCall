package com.mateuszbury.exitcall

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.mateuszbury.exitcall.model.CallConfigStore

class IncomingCallService : Service() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var isEnding = false

    private val timeoutHandler = Handler(Looper.getMainLooper())

    private val timeoutRunnable = Runnable {
        Log.d(
            "ExitCall",
            "Incoming call timed out after ${UNANSWERED_TIMEOUT_MS / 1000} seconds"
        )

        sendBroadcast(
            Intent(ACTION_TIMEOUT).setPackage(packageName)
        )

        finishIncomingCall("timeout")
    }

    companion object {
        const val CHANNEL_ID = "incoming_call_channel"
        const val NOTIFICATION_ID = 1002

        const val ACTION_DECLINE =
            "com.mateuszbury.exitcall.ACTION_DECLINE"

        const val ACTION_TIMEOUT =
            "com.mateuszbury.exitcall.ACTION_TIMEOUT"

        const val UNANSWERED_TIMEOUT_MS = 20_000L
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val config = CallConfigStore(this).load()

        Log.d(
            "ExitCall",
            "Loaded call config for service: caller=${config.callerName}, ringtone=${config.ringtoneName}, vibrate=${config.vibrate}"
        )

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

        val activityFlags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP

        val fullScreenIntent = Intent(
            this,
            IncomingCallActivity::class.java
        ).apply {
            addFlags(activityFlags)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            2001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val answerIntent = Intent(
            this,
            IncomingCallActivity::class.java
        ).apply {
            addFlags(activityFlags)
            putExtra(
                IncomingCallActivity.EXTRA_START_ACTIVE,
                true
            )
        }

        val answerPendingIntent = PendingIntent.getActivity(
            this,
            2002,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val declineIntent = Intent(
            this,
            IncomingCallService::class.java
        ).apply {
            action = ACTION_DECLINE
        }

        val declinePendingIntent = PendingIntent.getService(
            this,
            2003,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val caller = Person.Builder()
            .setName(
                config.callerName.ifBlank {
                    "Unknown"
                }
            )
            .setImportant(true)
            .build()

        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setStyle(
                NotificationCompat.CallStyle.forIncomingCall(
                    caller,
                    declinePendingIntent,
                    answerPendingIntent
                )
            )
            .setFullScreenIntent(
                fullScreenPendingIntent,
                true
            )
            .build()

        startForeground(
            NOTIFICATION_ID,
            notification
        )

        startRingtone(config.ringtoneUri)
        startVibration(config.vibrate)

        timeoutHandler.postDelayed(
            timeoutRunnable,
            UNANSWERED_TIMEOUT_MS
        )
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        if (intent?.action == ACTION_DECLINE) {
            Log.d(
                "ExitCall",
                "Decline pressed from notification"
            )

            finishIncomingCall("decline")
        }

        return START_NOT_STICKY
    }

    private fun finishIncomingCall(reason: String) {
        if (isEnding) return

        isEnding = true
        timeoutHandler.removeCallbacks(timeoutRunnable)

        stopAlerting()

        stopForeground(STOP_FOREGROUND_REMOVE)
        getSystemService(NotificationManager::class.java)
            .cancel(NOTIFICATION_ID)

        Log.d(
            "ExitCall",
            "Incoming call finished: $reason"
        )

        stopSelf()
    }

    private fun stopAlerting() {
        ringtone?.stop()
        ringtone = null

        vibrator?.cancel()
        vibrator = null
    }

    private fun startRingtone(configuredRingtoneUri: String?) {
        val defaultRingtoneUri = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_RINGTONE
        )

        val ringtoneUri = configuredRingtoneUri
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::parse)
            ?: defaultRingtoneUri

        ringtone = RingtoneManager.getRingtone(
            applicationContext,
            ringtoneUri
        ) ?: RingtoneManager.getRingtone(
            applicationContext,
            defaultRingtoneUri
        )

        ringtone?.audioAttributes =
            AudioAttributes.Builder()
                .setUsage(
                    AudioAttributes.USAGE_NOTIFICATION_RINGTONE
                )
                .setContentType(
                    AudioAttributes.CONTENT_TYPE_SONIFICATION
                )
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.isLooping = true
        }

        ringtone?.play()

        Log.d(
            "ExitCall",
            "Ringtone started from IncomingCallService"
        )
    }

    private fun startVibration(enabled: Boolean) {
        if (!enabled) {
            Log.d(
                "ExitCall",
                "Vibration disabled by call config"
            )
            return
        }

        vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    getSystemService(
                        Context.VIBRATOR_MANAGER_SERVICE
                    ) as VibratorManager

                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(
                    Context.VIBRATOR_SERVICE
                ) as Vibrator
            }

        val pattern = longArrayOf(
            0,
            700,
            800
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect =
                VibrationEffect.createWaveform(
                    pattern,
                    0
                )

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            ) {
                val attributes =
                    VibrationAttributes.Builder()
                        .setUsage(
                            VibrationAttributes.USAGE_RINGTONE
                        )
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

        Log.d(
            "ExitCall",
            "Vibration started"
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Incoming calls",
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager =
                getSystemService(
                    NotificationManager::class.java
                )

            notificationManager.createNotificationChannel(
                channel
            )
        }
    }

    override fun onDestroy() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        stopAlerting()

        stopForeground(STOP_FOREGROUND_REMOVE)
        getSystemService(NotificationManager::class.java)
            .cancel(NOTIFICATION_ID)

        Log.d(
            "ExitCall",
            "IncomingCallService destroyed"
        )

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null
}

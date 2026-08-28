package com.example.exitcall

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person

class IncomingCallService : Service() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    companion object {
        const val CHANNEL_ID = "incoming_call_channel"
        const val NOTIFICATION_ID = 1002

        const val ACTION_DECLINE =
            "com.example.exitcall.ACTION_DECLINE"
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

        /*
         * Full Screen Intent
         *
         * Używany głównie wtedy, kiedy telefon jest zablokowany.
         */
        val fullScreenIntent = Intent(
            this,
            IncomingCallActivity::class.java
        )

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            2001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        /*
         * ANSWER
         *
         * Otwieramy IncomingCallActivity, ale przekazujemy informację,
         * że użytkownik już odebrał połączenie.
         */
        val answerIntent = Intent(
            this,
            IncomingCallActivity::class.java
        ).apply {

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

        /*
         * DECLINE
         *
         * Nie otwieramy żadnego Activity.
         * Wysyłamy akcję bezpośrednio do tego Service.
         */
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

        /*
         * Osoba dzwoniąca dla Android CallStyle.
         *
         * Na razie ExitCall na sztywno.
         * Później weźmiemy dane z CallConfig.
         */
        val caller = Person.Builder()
            .setName("ExitCall")
            .setImportant(true)
            .build()

        /*
         * Call-style notification.
         */
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

        /*
         * Service przechodzi w foreground.
         */
        startForeground(
            NOTIFICATION_ID,
            notification
        )

        /*
         * Uruchamiamy dzwonek i wibrację.
         */
        startRingtone()
        startVibration()
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

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startRingtone() {

        val ringtoneUri = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_RINGTONE
        )

        ringtone = RingtoneManager.getRingtone(
            applicationContext,
            ringtoneUri
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

    private fun startVibration() {

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

        ringtone?.stop()
        ringtone = null

        vibrator?.cancel()
        vibrator = null

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
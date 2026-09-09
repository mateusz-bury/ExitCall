package com.mateuszbury.exitcall.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.mateuszbury.exitcall.MainActivity
import com.mateuszbury.exitcall.R

class MissedCallNotifier(
    context: Context
) {
    private val appContext = context.applicationContext
    private val notificationManager =
        appContext.getSystemService(NotificationManager::class.java)

    fun show(
        callerName: String,
        phoneNumber: String,
        vibrate: Boolean
    ) {
        createChannel()

        val displayName = callerName.ifBlank {
            appContext.getString(R.string.missed_call_unknown_caller)
        }

        val contentIntent = Intent(
            appContext,
            MainActivity::class.java
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }

        val contentPendingIntent = PendingIntent.getActivity(
            appContext,
            CONTENT_REQUEST_CODE,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val body = phoneNumber
            .takeIf(String::isNotBlank)
            ?.let { number ->
                appContext.getString(
                    R.string.missed_call_body_with_number,
                    number
                )
            }
            ?: appContext.getString(R.string.missed_call_body)

        val notification = NotificationCompat.Builder(
            appContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(
                appContext.getString(
                    R.string.missed_call_title,
                    displayName
                )
            )
            .setContentText(body)
            .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setVibrate(null)
            .build()

        notificationManager.cancel(NOTIFICATION_ID)
        notificationManager.notify(NOTIFICATION_ID, notification)

        if (vibrate) {
            vibrateOnce()
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val notificationSound = RingtoneManager.getDefaultUri(
            RingtoneManager.TYPE_NOTIFICATION
        )
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channel = NotificationChannel(
            CHANNEL_ID,
            appContext.getString(R.string.missed_call_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = appContext.getString(
                R.string.missed_call_channel_description
            )
            setSound(notificationSound, audioAttributes)
            enableVibration(false)
        }

        notificationManager.createNotificationChannel(channel)
    }

    @Suppress("DEPRECATION")
    private fun vibrateOnce() {
        val vibrator = appContext.getSystemService(Vibrator::class.java)

        if (!vibrator.hasVibrator()) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    VIBRATION_DURATION_MS,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(VIBRATION_DURATION_MS)
        }
    }

    companion object {
        private const val CHANNEL_ID = "missed_call_channel_v1"
        private const val NOTIFICATION_ID = 1003
        private const val CONTENT_REQUEST_CODE = 3001
        private const val VIBRATION_DURATION_MS = 180L
    }
}

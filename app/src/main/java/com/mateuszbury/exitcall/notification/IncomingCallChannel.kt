package com.mateuszbury.exitcall.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object IncomingCallChannel {
    const val ID = "incoming_call_channel_v2"

    fun hasControls(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled() && isReady(context)

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(
                    NotificationChannel(ID, "Incoming calls", NotificationManager.IMPORTANCE_HIGH).apply {
                        setSound(null, null)
                        enableVibration(false)
                    }
                )
        }
    }

    fun isReady(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = manager.getNotificationChannel(ID) ?: return false
        val groupBlocked = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            channel.group?.let { manager.getNotificationChannelGroup(it)?.isBlocked } == true
        return channel.importance >= NotificationManager.IMPORTANCE_HIGH && !groupBlocked
    }
}

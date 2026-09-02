package com.mateuszbury.exitcall.capability

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mateuszbury.exitcall.scheduler.ScheduledCallScheduler

class CallCapabilityResolver(
    context: Context,
    private val scheduledCallScheduler: ScheduledCallScheduler
) {
    private val appContext = context.applicationContext

    fun firstMissingCapability(
        requireExactAlarm: Boolean
    ): CallCapability? {
        if (!hasNotificationCapability()) {
            return CallCapability.NOTIFICATIONS
        }

        if (!hasFullScreenIntentCapability()) {
            return CallCapability.FULL_SCREEN_INTENT
        }

        if (
            requireExactAlarm &&
            !scheduledCallScheduler.canScheduleExactAlarms()
        ) {
            return CallCapability.EXACT_ALARM
        }

        return null
    }

    fun canRequestNotificationRuntimePermission(
        requestedThisSession: Boolean
    ): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED &&
            !requestedThisSession
    }

    fun settingsIntent(
        capability: CallCapability
    ): Intent? {
        return when (capability) {
            CallCapability.NOTIFICATIONS -> {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(
                        Settings.EXTRA_APP_PACKAGE,
                        appContext.packageName
                    )
                }
            }

            CallCapability.FULL_SCREEN_INTENT -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    null
                } else {
                    Intent(
                        Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        Uri.parse("package:${appContext.packageName}")
                    )
                }
            }

            CallCapability.EXACT_ALARM -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    null
                } else {
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:${appContext.packageName}")
                    )
                }
            }
        }
    }

    fun appDetailsSettingsIntent(): Intent {
        return Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${appContext.packageName}")
        )
    }

    private fun hasNotificationCapability(): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return NotificationManagerCompat
            .from(appContext)
            .areNotificationsEnabled()
    }

    private fun hasFullScreenIntentCapability(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return true
        }

        return appContext
            .getSystemService(NotificationManager::class.java)
            .canUseFullScreenIntent()
    }
}

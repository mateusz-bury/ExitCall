package com.mateuszbury.exitcall.power

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

enum class BatteryOptimizationStatus {
    UNRESTRICTED,
    BACKGROUND_RESTRICTED,
    OPTIMIZED,
    UNKNOWN
}

class BatteryOptimizationResolver(
    context: Context
) {
    private val appContext = context.applicationContext
    private val activityManager =
        appContext.getSystemService(ActivityManager::class.java)
    private val powerManager =
        appContext.getSystemService(PowerManager::class.java)

    fun currentStatus(): BatteryOptimizationStatus {
        return runCatching {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                activityManager.isBackgroundRestricted
            ) {
                BatteryOptimizationStatus.BACKGROUND_RESTRICTED
            } else if (
                powerManager.isIgnoringBatteryOptimizations(
                    appContext.packageName
                )
            ) {
                BatteryOptimizationStatus.UNRESTRICTED
            } else {
                BatteryOptimizationStatus.OPTIMIZED
            }
        }.getOrDefault(BatteryOptimizationStatus.UNKNOWN)
    }

    fun settingsIntent(): Intent {
        val packageUri = Uri.parse("package:${appContext.packageName}")
        val appDetailsSettings = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            packageUri
        )
        val candidates = listOf(
            Intent(APP_BATTERY_SETTINGS_ACTION, packageUri),
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        )

        return candidates.firstOrNull { candidate ->
            runCatching {
                candidate.resolveActivity(appContext.packageManager) != null
            }.getOrDefault(false)
        } ?: appDetailsSettings
    }

    private companion object {
        const val APP_BATTERY_SETTINGS_ACTION =
            "android.settings.APP_BATTERY_SETTINGS"
    }
}

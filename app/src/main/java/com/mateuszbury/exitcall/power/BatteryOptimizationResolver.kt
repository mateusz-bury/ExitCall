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

    // Doze allowlisting is NOT permission to run in the background.
    // null means the OS query failed, not that access is granted.
    fun isBackgroundExecutionAllowed(): Boolean? = runCatching {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.P ||
            !activityManager.isBackgroundRestricted
    }.getOrNull()

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

    fun settingsIntents(): List<Intent> {
        val packageUri = Uri.parse("package:${appContext.packageName}")
        val appDetailsSettings = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            packageUri
        )
        return listOf(
            Intent(APP_BATTERY_SETTINGS_ACTION, packageUri),
            appDetailsSettings,
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        )
    }

    private companion object {
        const val APP_BATTERY_SETTINGS_ACTION =
            "android.settings.APP_BATTERY_SETTINGS"
    }
}

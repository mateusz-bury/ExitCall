package com.mateuszbury.exitcall.power

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

enum class BatteryOptimizationStatus {
    UNRESTRICTED,
    OPTIMIZED,
    UNKNOWN
}

class BatteryOptimizationResolver(
    context: Context
) {
    private val appContext = context.applicationContext
    private val powerManager =
        appContext.getSystemService(PowerManager::class.java)

    fun currentStatus(): BatteryOptimizationStatus {
        return runCatching {
            if (
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
        val batteryOptimizationSettings = Intent(
            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        )

        return batteryOptimizationSettings.takeIf {
            it.resolveActivity(appContext.packageManager) != null
        } ?: Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${appContext.packageName}")
        )
    }
}

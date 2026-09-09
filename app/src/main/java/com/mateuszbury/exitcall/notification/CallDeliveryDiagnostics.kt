package com.mateuszbury.exitcall.notification

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.mateuszbury.exitcall.power.BatteryOptimizationResolver

/** Diagnostic state only: never includes caller identity or audio file paths. */
object CallDeliveryDiagnostics {
    fun log(context: Context, stage: String) {
        runCatching {
            val manager = context.getSystemService(NotificationManager::class.java)
            val power = context.getSystemService(PowerManager::class.java)
            val locked = context.getSystemService(KeyguardManager::class.java).isKeyguardLocked
            val fullScreen = if (Build.VERSION.SDK_INT >= 34) manager.canUseFullScreenIntent() else true
            val importance = if (Build.VERSION.SDK_INT >= 26) manager.getNotificationChannel(IncomingCallChannel.ID)?.importance else null
            val background = BatteryOptimizationResolver(context).isBackgroundExecutionAllowed()
            Log.i("ExitCallDelivery", "stage=$stage sdk=${Build.VERSION.SDK_INT} manufacturer=${Build.MANUFACTURER} model=${Build.MODEL} backgroundAllowed=$background dozeExempt=${power.isIgnoringBatteryOptimizations(context.packageName)} notifications=${NotificationManagerCompat.from(context).areNotificationsEnabled()} channelImportance=$importance fullScreenAllowed=$fullScreen interactive=${power.isInteractive} locked=$locked")
        }.onFailure {
            Log.w("ExitCallDelivery", "Unable to read diagnostic state at $stage", it)
        }
    }
}

package com.mateuszbury.exitcall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.mateuszbury.exitcall.model.ScheduledCallStore
import com.mateuszbury.exitcall.notification.CallDeliveryDiagnostics

class CallAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ExitCall", "Scheduled call triggered!")
        CallDeliveryDiagnostics.log(context, "alarm-received")

        val scheduledCallStore = ScheduledCallStore(context)
        val settingsSnapshot = scheduledCallStore.loadSettingsSnapshot()
        scheduledCallStore.clear()

        val serviceIntent = Intent(
            context,
            IncomingCallService::class.java
        ).also { incomingCallIntent ->
            settingsSnapshot?.let { settings ->
                IncomingCallService.putSettingsSnapshot(
                    intent = incomingCallIntent,
                    settings = settings
                )
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (exception: RuntimeException) {
            // Includes OEM/background service restrictions and revoked access.
            // The elapsed call is consumed, not replayed unexpectedly later.
            Log.e("ExitCallDelivery", "Scheduled service launch failed", exception)
        }
    }
}

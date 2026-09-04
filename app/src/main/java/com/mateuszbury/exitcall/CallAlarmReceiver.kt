package com.mateuszbury.exitcall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.mateuszbury.exitcall.model.ScheduledCallStore

class CallAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ExitCall", "Scheduled call triggered!")

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}

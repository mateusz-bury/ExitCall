package com.example.exitcall.scheduler

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScheduledCallRecoveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        ) {
            return
        }

        val result = ScheduledCallScheduler(context)
            .restorePersistedCall()

        Log.i(
            TAG,
            "Scheduled call recovery action=$action result=$result"
        )
    }

    companion object {
        private const val TAG = "ExitCallRecovery"
    }
}

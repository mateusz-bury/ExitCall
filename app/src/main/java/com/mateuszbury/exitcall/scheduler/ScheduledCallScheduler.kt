package com.mateuszbury.exitcall.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.mateuszbury.exitcall.CallAlarmReceiver
import com.mateuszbury.exitcall.model.ScheduledCallStore

class ScheduledCallScheduler(
    context: Context,
    private val scheduledCallStore: ScheduledCallStore =
        ScheduledCallStore(context.applicationContext)
) {
    enum class RegistrationResult {
        SCHEDULED,
        NO_PENDING_CALL,
        TRIGGER_TIME_PASSED,
        MISSING_EXACT_ALARM_PERMISSION,
        FAILED
    }

    private val appContext = context.applicationContext
    private val alarmManager =
        appContext.getSystemService(AlarmManager::class.java)

    fun schedule(
        triggerTimeMillis: Long
    ): RegistrationResult {
        return register(
            triggerTimeMillis = triggerTimeMillis,
            persistOnSuccess = true
        )
    }

    fun restorePersistedCall(): RegistrationResult {
        val triggerTimeMillis = scheduledCallStore.load()
            ?: return RegistrationResult.NO_PENDING_CALL

        return register(
            triggerTimeMillis = triggerTimeMillis,
            persistOnSuccess = false
        )
    }

    fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        return alarmManager.canScheduleExactAlarms()
    }

    private fun register(
        triggerTimeMillis: Long,
        persistOnSuccess: Boolean
    ): RegistrationResult {
        if (triggerTimeMillis <= System.currentTimeMillis()) {
            if (!persistOnSuccess) {
                scheduledCallStore.clear()
            }

            Log.i(
                TAG,
                "Skipping scheduled call because trigger time has already passed: $triggerTimeMillis"
            )

            return RegistrationResult.TRIGGER_TIME_PASSED
        }

        if (!canScheduleExactAlarms()) {
            Log.w(
                TAG,
                "Exact alarm capability is missing; scheduled call was not registered"
            )

            return RegistrationResult.MISSING_EXACT_ALARM_PERMISSION
        }

        val alarmIntent = Intent(
            appContext,
            CallAlarmReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            SCHEDULED_CALL_REQUEST_CODE,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )

            if (persistOnSuccess) {
                scheduledCallStore.save(triggerTimeMillis)
            }

            Log.i(
                TAG,
                "Scheduled call registered for: $triggerTimeMillis"
            )

            RegistrationResult.SCHEDULED
        } catch (securityException: SecurityException) {
            Log.e(
                TAG,
                "Exact alarm capability was unavailable while registering scheduled call",
                securityException
            )

            RegistrationResult.MISSING_EXACT_ALARM_PERMISSION
        } catch (runtimeException: RuntimeException) {
            Log.e(
                TAG,
                "Scheduled call registration failed",
                runtimeException
            )

            RegistrationResult.FAILED
        }
    }

    companion object {
        private const val TAG = "ExitCallScheduler"
        private const val SCHEDULED_CALL_REQUEST_CODE = 4001
    }
}

package com.example.exitcall.model

import android.content.Context

class ScheduledCallStore(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    fun save(triggerTimeMillis: Long) {
        preferences.edit()
            .putLong(KEY_TRIGGER_TIME, triggerTimeMillis)
            .apply()
    }

    fun load(): Long? {
        val triggerTimeMillis = preferences.getLong(
            KEY_TRIGGER_TIME,
            NO_SCHEDULED_CALL
        )

        if (triggerTimeMillis == NO_SCHEDULED_CALL) {
            return null
        }

        if (triggerTimeMillis <= System.currentTimeMillis()) {
            clear()
            return null
        }

        return triggerTimeMillis
    }

    fun clear() {
        preferences.edit()
            .remove(KEY_TRIGGER_TIME)
            .apply()
    }

    companion object {
        private const val PREFS_NAME =
            "exitcall_scheduled_call"

        private const val KEY_TRIGGER_TIME =
            "trigger_time_millis"

        private const val NO_SCHEDULED_CALL = -1L
    }
}

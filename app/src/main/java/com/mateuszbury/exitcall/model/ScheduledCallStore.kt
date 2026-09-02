package com.mateuszbury.exitcall.model

import android.content.Context
import android.os.UserManager

class ScheduledCallStore(
    context: Context
) {

    private val appContext = context.applicationContext
    private val deviceProtectedContext =
        appContext.createDeviceProtectedStorageContext()

    private val preferences =
        deviceProtectedContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    init {
        migrateLegacyScheduleIfNeeded()
    }

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

    private fun migrateLegacyScheduleIfNeeded() {
        if (preferences.contains(KEY_TRIGGER_TIME)) {
            return
        }

        val userManager = appContext.getSystemService(UserManager::class.java)
        if (!userManager.isUserUnlocked) {
            return
        }

        val legacyPreferences = appContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        if (!legacyPreferences.contains(KEY_TRIGGER_TIME)) {
            return
        }

        val triggerTimeMillis = legacyPreferences.getLong(
            KEY_TRIGGER_TIME,
            NO_SCHEDULED_CALL
        )

        if (triggerTimeMillis != NO_SCHEDULED_CALL) {
            preferences.edit()
                .putLong(KEY_TRIGGER_TIME, triggerTimeMillis)
                .apply()
        }

        legacyPreferences.edit()
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

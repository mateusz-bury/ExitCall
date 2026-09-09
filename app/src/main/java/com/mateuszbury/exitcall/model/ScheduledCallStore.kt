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

    fun save(
        triggerTimeMillis: Long,
        settingsSnapshot: AppSettings
    ) {
        preferences.edit()
            .putLong(KEY_TRIGGER_TIME, triggerTimeMillis)
            .putBoolean(KEY_HAS_SETTINGS_SNAPSHOT, true)
            .putString(KEY_RINGTONE_URI, settingsSnapshot.ringtoneUri)
            .putString(KEY_RINGTONE_NAME, settingsSnapshot.ringtoneName)
            .putBoolean(KEY_VIBRATE, settingsSnapshot.vibrate)
            .putInt(
                KEY_RING_DURATION_SECONDS,
                settingsSnapshot.ringDurationSeconds
            )
            .putBoolean(
                KEY_MISSED_CALL_NOTIFICATION_ENABLED,
                settingsSnapshot.missedCallNotificationEnabled
            )
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

    fun loadSettingsSnapshot(): AppSettings? {
        if (!preferences.getBoolean(KEY_HAS_SETTINGS_SNAPSHOT, false)) {
            return null
        }

        val defaults = AppSettings()

        return AppSettings(
            ringtoneUri = preferences.getString(
                KEY_RINGTONE_URI,
                defaults.ringtoneUri
            ),
            ringtoneName = preferences.getString(
                KEY_RINGTONE_NAME,
                defaults.ringtoneName
            ) ?: defaults.ringtoneName,
            vibrate = preferences.getBoolean(
                KEY_VIBRATE,
                defaults.vibrate
            ),
            ringDurationSeconds = RingDuration.validatedOrDefault(
                preferences.getInt(
                    KEY_RING_DURATION_SECONDS,
                    defaults.ringDurationSeconds
                )
            ),
            missedCallNotificationEnabled = preferences.getBoolean(
                KEY_MISSED_CALL_NOTIFICATION_ENABLED,
                defaults.missedCallNotificationEnabled
            )
        )
    }

    fun clear() {
        preferences.edit()
            .remove(KEY_TRIGGER_TIME)
            .remove(KEY_HAS_SETTINGS_SNAPSHOT)
            .remove(KEY_RINGTONE_URI)
            .remove(KEY_RINGTONE_NAME)
            .remove(KEY_VIBRATE)
            .remove(KEY_RING_DURATION_SECONDS)
            .remove(KEY_MISSED_CALL_NOTIFICATION_ENABLED)
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

        private const val KEY_HAS_SETTINGS_SNAPSHOT =
            "has_settings_snapshot"
        private const val KEY_RINGTONE_URI = "settings_ringtone_uri"
        private const val KEY_RINGTONE_NAME = "settings_ringtone_name"
        private const val KEY_VIBRATE = "settings_vibrate"
        private const val KEY_RING_DURATION_SECONDS =
            "settings_ring_duration_seconds"
        private const val KEY_MISSED_CALL_NOTIFICATION_ENABLED =
            "settings_missed_call_notification_enabled"

        private const val NO_SCHEDULED_CALL = -1L
    }
}

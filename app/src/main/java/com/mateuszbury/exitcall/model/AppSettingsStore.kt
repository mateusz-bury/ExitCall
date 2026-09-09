package com.mateuszbury.exitcall.model

import android.content.Context

class AppSettingsStore(
    context: Context
) {
    private val appContext = context.applicationContext

    private val preferences = appContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    init {
        migrateCallConfigSettingsIfNeeded()
    }

    fun save(settings: AppSettings) {
        require(RingDuration.isValid(settings.ringDurationSeconds)) {
            "Ring duration must be between ${RingDuration.MIN_SECONDS} and ${RingDuration.MAX_SECONDS} seconds."
        }

        preferences.edit()
            .putString(KEY_RINGTONE_URI, settings.ringtoneUri)
            .putString(KEY_RINGTONE_NAME, settings.ringtoneName)
            .putBoolean(KEY_VIBRATE, settings.vibrate)
            .putInt(KEY_RING_DURATION_SECONDS, settings.ringDurationSeconds)
            .putBoolean(
                KEY_MISSED_CALL_NOTIFICATION_ENABLED,
                settings.missedCallNotificationEnabled
            )
            .apply()
    }

    fun load(): AppSettings {
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

    private fun migrateCallConfigSettingsIfNeeded() {
        if (preferences.getBoolean(KEY_LEGACY_MIGRATION_COMPLETE, false)) {
            return
        }

        val legacyConfig = CallConfigStore(appContext).load()

        preferences.edit()
            .putString(KEY_RINGTONE_URI, legacyConfig.ringtoneUri)
            .putString(KEY_RINGTONE_NAME, legacyConfig.ringtoneName)
            .putBoolean(KEY_VIBRATE, legacyConfig.vibrate)
            .putInt(KEY_RING_DURATION_SECONDS, RingDuration.DEFAULT_SECONDS)
            .putBoolean(KEY_MISSED_CALL_NOTIFICATION_ENABLED, true)
            .putBoolean(KEY_LEGACY_MIGRATION_COMPLETE, true)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "exitcall_app_settings"

        private const val KEY_RINGTONE_URI = "ringtone_uri"
        private const val KEY_RINGTONE_NAME = "ringtone_name"
        private const val KEY_VIBRATE = "vibrate"
        private const val KEY_RING_DURATION_SECONDS = "ring_duration_seconds"
        private const val KEY_MISSED_CALL_NOTIFICATION_ENABLED =
            "missed_call_notification_enabled"
        private const val KEY_LEGACY_MIGRATION_COMPLETE =
            "legacy_call_config_migration_complete"
    }
}

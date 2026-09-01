package com.mateuszbury.exitcall.model

import android.content.Context

class CallConfigStore(
    context: Context
) {

    private val preferences =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    fun save(config: CallConfig) {
        preferences.edit()
            .putString(
                KEY_CALLER_NAME,
                config.callerName
            )
            .putString(
                KEY_PHONE_NUMBER,
                config.phoneNumber
            )
            .putString(
                KEY_RINGTONE_URI,
                config.ringtoneUri
            )
            .putString(
                KEY_RINGTONE_NAME,
                config.ringtoneName
            )
            .putBoolean(
                KEY_VIBRATE,
                config.vibrate
            )
            .apply()
    }

    fun load(): CallConfig {
        val defaults = CallConfig()

        return CallConfig(
            callerName = preferences.getString(
                KEY_CALLER_NAME,
                defaults.callerName
            ) ?: defaults.callerName,

            phoneNumber = preferences.getString(
                KEY_PHONE_NUMBER,
                defaults.phoneNumber
            ) ?: defaults.phoneNumber,

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
            )
        )
    }

    companion object {
        private const val PREFS_NAME =
            "exitcall_call_config"

        private const val KEY_CALLER_NAME =
            "caller_name"

        private const val KEY_PHONE_NUMBER =
            "phone_number"

        private const val KEY_RINGTONE_URI =
            "ringtone_uri"

        private const val KEY_RINGTONE_NAME =
            "ringtone_name"

        private const val KEY_VIBRATE =
            "vibrate"
    }
}
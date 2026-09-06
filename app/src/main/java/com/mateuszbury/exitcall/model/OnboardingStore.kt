package com.mateuszbury.exitcall.model

import android.content.Context

class OnboardingStore(
    context: Context
) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun isCompleted(): Boolean {
        return preferences.getInt(
            KEY_COMPLETED_VERSION,
            0
        ) >= CURRENT_VERSION
    }

    fun markCompleted() {
        preferences.edit()
            .putInt(KEY_COMPLETED_VERSION, CURRENT_VERSION)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "exitcall_onboarding"
        private const val KEY_COMPLETED_VERSION = "completed_version"

        // Bump when a mandatory onboarding slice is added so earlier test builds
        // cannot silently skip newly required education/demo steps.
        private const val CURRENT_VERSION = 1
    }
}

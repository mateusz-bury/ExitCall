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
        return preferences.getBoolean(KEY_COMPLETED, false)
    }

    fun markCompleted() {
        preferences.edit()
            .putBoolean(KEY_COMPLETED, true)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "exitcall_onboarding"
        private const val KEY_COMPLETED = "completed"
    }
}

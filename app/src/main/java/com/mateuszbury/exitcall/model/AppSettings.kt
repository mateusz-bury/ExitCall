package com.mateuszbury.exitcall.model

data class AppSettings(
    val ringtoneUri: String? = null,
    val ringtoneName: String = "System default",
    val vibrate: Boolean = true,
    val ringDurationSeconds: Int = RingDuration.DEFAULT_SECONDS
)

object RingDuration {
    const val MIN_SECONDS = 5
    const val MAX_SECONDS = 60
    const val DEFAULT_SECONDS = 20

    val presets = listOf(5, 10, 20)

    fun isValid(seconds: Int): Boolean {
        return seconds in MIN_SECONDS..MAX_SECONDS
    }

    fun validatedOrDefault(seconds: Int): Int {
        return seconds.takeIf(::isValid) ?: DEFAULT_SECONDS
    }
}

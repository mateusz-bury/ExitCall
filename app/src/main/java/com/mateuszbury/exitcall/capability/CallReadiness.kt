package com.mateuszbury.exitcall.capability

/** Shared policy for first-run setup and user-initiated calls. FSI remains optional. */
object CallReadiness {
    fun firstMissing(
        available: Map<CallCapability, Boolean>,
        requireExactAlarm: Boolean = false
    ): CallCapability? = when {
        available[CallCapability.NOTIFICATIONS] != true -> CallCapability.NOTIFICATIONS
        available[CallCapability.BACKGROUND_EXECUTION] != true -> CallCapability.BACKGROUND_EXECUTION
        requireExactAlarm && available[CallCapability.EXACT_ALARM] != true -> CallCapability.EXACT_ALARM
        else -> null
    }
}

package com.mateuszbury.exitcall.capability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallReadinessTest {
    private fun state(notifications: Boolean, background: Boolean, exact: Boolean = false) = mapOf(
        CallCapability.NOTIFICATIONS to notifications,
        CallCapability.BACKGROUND_EXECUTION to background,
        CallCapability.EXACT_ALARM to exact,
        CallCapability.FULL_SCREEN_INTENT to false
    )

    @Test fun missingNotificationsTakePrecedence() {
        assertEquals(CallCapability.NOTIFICATIONS, CallReadiness.firstMissing(state(false, false)))
    }

    @Test fun restrictedBackgroundBlocksBothImmediateAndScheduledCalls() {
        for (scheduled in listOf(false, true)) {
            assertEquals(CallCapability.BACKGROUND_EXECUTION,
                CallReadiness.firstMissing(state(true, false, true), scheduled))
        }
    }

    @Test fun unknownBackgroundDoesNotGrantAccess() {
        assertEquals(CallCapability.BACKGROUND_EXECUTION,
            CallReadiness.firstMissing(mapOf(CallCapability.NOTIFICATIONS to true)))
    }

    @Test fun fullScreenAndExactAlarmAreNotRequiredForOnboardingOrImmediateCalls() {
        assertNull(CallReadiness.firstMissing(state(true, true)))
    }

    @Test fun schedulingRequiresExactAlarm() {
        assertEquals(CallCapability.EXACT_ALARM, CallReadiness.firstMissing(state(true, true), true))
        assertNull(CallReadiness.firstMissing(state(true, true, true), true))
    }
}

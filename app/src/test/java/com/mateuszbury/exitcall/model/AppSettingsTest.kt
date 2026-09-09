package com.mateuszbury.exitcall.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {

    @Test
    fun missedCallNotificationIsEnabledByDefault() {
        assertTrue(AppSettings().missedCallNotificationEnabled)
    }

    @Test
    fun timeoutBoundariesAreValid() {
        assertTrue(RingDuration.isValid(5))
        assertTrue(RingDuration.isValid(60))
    }

    @Test
    fun timeoutOutsideBoundariesIsInvalid() {
        assertFalse(RingDuration.isValid(4))
        assertFalse(RingDuration.isValid(61))
    }

    @Test
    fun invalidPersistedTimeoutFallsBackToDefault() {
        assertEquals(20, RingDuration.validatedOrDefault(0))
        assertEquals(20, RingDuration.validatedOrDefault(120))
    }

    @Test
    fun validCustomTimeoutIsPreserved() {
        assertEquals(37, RingDuration.validatedOrDefault(37))
    }
}

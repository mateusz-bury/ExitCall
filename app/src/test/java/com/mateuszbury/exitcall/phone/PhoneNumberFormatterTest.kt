package com.mateuszbury.exitcall.phone

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberFormatterTest {

    @Test
    fun blankNumberRemainsValidAndEmpty() {
        val result = PhoneNumberFormatter.validate("   ")

        assertTrue(result.isValid)
        assertEquals("", result.normalized)
    }

    @Test
    fun polishInternationalNumberIsNormalizedAndFormatted() {
        val result = PhoneNumberFormatter.validate("+48 123 456 789")

        assertTrue(result.isValid)
        assertEquals("+48123456789", result.normalized)
        assertEquals(
            "+48 123 456 789",
            PhoneNumberFormatter.formatForDisplay(result.normalized)
        )
    }

    @Test
    fun doubleZeroInternationalPrefixBecomesPlusPrefix() {
        val result = PhoneNumberFormatter.validate("0048 123 456 789")

        assertTrue(result.isValid)
        assertEquals("+48123456789", result.normalized)
    }

    @Test
    fun genericInternationalNumberKeepsCountryPrefix() {
        val result = PhoneNumberFormatter.validate("+1 (415) 555-2671")

        assertTrue(result.isValid)
        assertEquals("+14155552671", result.normalized)
    }

    @Test
    fun nineDigitNationalNumberUsesReadableGrouping() {
        val result = PhoneNumberFormatter.validate("123-456-789")

        assertTrue(result.isValid)
        assertEquals("123456789", result.normalized)
        assertEquals(
            "123 456 789",
            PhoneNumberFormatter.formatForDisplay(result.normalized)
        )
    }

    @Test
    fun lettersAreRejected() {
        assertFalse(
            PhoneNumberFormatter.validate("+48 CALL-ME").isValid
        )
    }

    @Test
    fun plusOutsideBeginningIsRejected() {
        assertFalse(
            PhoneNumberFormatter.validate("48+123456789").isValid
        )
    }

    @Test
    fun tooShortNumberIsRejected() {
        assertFalse(
            PhoneNumberFormatter.validate("12345").isValid
        )
    }

    @Test
    fun e164MaximumLengthIsEnforced() {
        val valid = PhoneNumberFormatter.validate("+123456789012345")
        val invalid = PhoneNumberFormatter.validate("+1234567890123456")

        assertTrue(valid.isValid)
        assertFalse(invalid.isValid)
    }
}

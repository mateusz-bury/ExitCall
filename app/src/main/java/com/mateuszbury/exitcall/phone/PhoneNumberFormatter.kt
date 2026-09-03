package com.mateuszbury.exitcall.phone

object PhoneNumberFormatter {

    data class ValidationResult(
        val isValid: Boolean,
        val normalized: String,
        val errorMessage: String? = null
    )

    private const val MIN_DIGITS = 7
    private const val MAX_DIGITS = 15

    private val allowedFormattingCharacters = setOf(
        ' ',
        '-',
        '(',
        ')',
        '.',
        '/'
    )

    fun validate(input: String): ValidationResult {
        val trimmed = input.trim()

        if (trimmed.isBlank()) {
            return ValidationResult(
                isValid = true,
                normalized = ""
            )
        }

        val invalidCharacter = trimmed.firstOrNull { character ->
            !character.isDigit() &&
                character != '+' &&
                character !in allowedFormattingCharacters
        }

        if (invalidCharacter != null) {
            return invalid(
                "Use digits, +, spaces or standard phone-number separators."
            )
        }

        val plusCount = trimmed.count { it == '+' }

        if (plusCount > 1 || (plusCount == 1 && !trimmed.startsWith('+'))) {
            return invalid(
                "The + country prefix can appear only at the beginning."
            )
        }

        val digitsOnly = trimmed.filter(Char::isDigit)
        val usesInternationalPrefix =
            trimmed.startsWith('+') || trimmed.startsWith("00")

        val internationalDigits = if (
            trimmed.startsWith("00") && !trimmed.startsWith('+')
        ) {
            digitsOnly.drop(2)
        } else {
            digitsOnly
        }

        if (internationalDigits.length < MIN_DIGITS) {
            return invalid("Enter at least $MIN_DIGITS digits.")
        }

        if (internationalDigits.length > MAX_DIGITS) {
            return invalid(
                "Phone numbers can contain at most $MAX_DIGITS digits."
            )
        }

        if (usesInternationalPrefix && internationalDigits.startsWith('0')) {
            return invalid("Enter a valid international country prefix.")
        }

        val normalized = if (usesInternationalPrefix) {
            "+$internationalDigits"
        } else {
            internationalDigits
        }

        return ValidationResult(
            isValid = true,
            normalized = normalized
        )
    }

    fun formatForDisplay(input: String): String {
        val validation = validate(input)

        if (!validation.isValid) {
            return input.trim()
        }

        val normalized = validation.normalized

        if (normalized.isBlank()) {
            return ""
        }

        if (normalized.startsWith("+48")) {
            val nationalNumber = normalized.removePrefix("+48")

            if (nationalNumber.length == 9) {
                return "+48 ${nationalNumber.chunked(3).joinToString(" ")}"
            }
        }

        if (!normalized.startsWith('+') && normalized.length == 9) {
            return normalized.chunked(3).joinToString(" ")
        }

        return normalized
    }

    private fun invalid(message: String): ValidationResult {
        return ValidationResult(
            isValid = false,
            normalized = "",
            errorMessage = message
        )
    }
}

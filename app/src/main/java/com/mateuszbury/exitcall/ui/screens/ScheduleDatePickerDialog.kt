package com.mateuszbury.exitcall.ui.screens

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleDatePickerDialog(
    scheduledAtMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val initialSelectedDateMillis = remember(scheduledAtMillis) {
        localDateAsUtcMillis(scheduledAtMillis)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDateUtcMillis =
                        datePickerState.selectedDateMillis

                    if (selectedDateUtcMillis == null) {
                        onDismiss()
                    } else {
                        onConfirm(
                            replaceDate(
                                baseMillis = scheduledAtMillis,
                                selectedDateUtcMillis =
                                    selectedDateUtcMillis
                            )
                        )
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false
        )
    }
}

private fun replaceDate(
    baseMillis: Long,
    selectedDateUtcMillis: Long
): Long {
    val selectedDate = Calendar.getInstance(
        TimeZone.getTimeZone("UTC")
    ).apply {
        timeInMillis = selectedDateUtcMillis
    }

    return Calendar.getInstance().apply {
        timeInMillis = baseMillis

        set(
            Calendar.YEAR,
            selectedDate.get(Calendar.YEAR)
        )

        set(
            Calendar.MONTH,
            selectedDate.get(Calendar.MONTH)
        )

        set(
            Calendar.DAY_OF_MONTH,
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
    }.timeInMillis
}

private fun localDateAsUtcMillis(
    localMillis: Long
): Long {
    val localCalendar =
        Calendar.getInstance().apply {
            timeInMillis = localMillis
        }

    return Calendar.getInstance(
        TimeZone.getTimeZone("UTC")
    ).apply {
        clear()

        set(
            localCalendar.get(Calendar.YEAR),
            localCalendar.get(Calendar.MONTH),
            localCalendar.get(Calendar.DAY_OF_MONTH)
        )
    }.timeInMillis
}

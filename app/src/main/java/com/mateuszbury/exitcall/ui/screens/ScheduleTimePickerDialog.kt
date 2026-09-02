package com.mateuszbury.exitcall.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleTimePickerDialog(
    scheduledAtMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val initialTime = remember(scheduledAtMillis) {
        Calendar.getInstance().apply {
            timeInMillis = scheduledAtMillis
        }.let { calendar ->
            calendar.get(Calendar.HOUR_OF_DAY) to
                calendar.get(Calendar.MINUTE)
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.first,
        initialMinute = initialTime.second,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Choose time")
        },
        text = {
            TimePicker(
                state = timePickerState
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        replaceTime(
                            baseMillis = scheduledAtMillis,
                            hour = timePickerState.hour,
                            minute = timePickerState.minute
                        )
                    )
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
    )
}

private fun replaceTime(
    baseMillis: Long,
    hour: Int,
    minute: Int
): Long {
    return Calendar.getInstance().apply {
        timeInMillis = baseMillis

        set(
            Calendar.HOUR_OF_DAY,
            hour
        )

        set(
            Calendar.MINUTE,
            minute
        )

        set(
            Calendar.SECOND,
            0
        )

        set(
            Calendar.MILLISECOND,
            0
        )
    }.timeInMillis
}

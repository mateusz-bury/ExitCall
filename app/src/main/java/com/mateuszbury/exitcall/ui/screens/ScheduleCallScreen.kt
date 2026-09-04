package com.mateuszbury.exitcall.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mateuszbury.exitcall.ui.components.ExitCallTopBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class SchedulePicker {
    DATE,
    TIME
}

@Composable
private fun ScheduleOption(
    label: String,
    value: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .width(145.dp)
                .height(92.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 19.sp
                )
            }
        }

        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ScheduleCallScreen(
    initialScheduledAtMillis: Long?,
    onBack: () -> Unit,
    onSchedule: (Long) -> Unit,
    onCancelScheduledCall: () -> Unit
) {
    val isEditingScheduledCall = initialScheduledAtMillis != null

    val initialTime = remember(initialScheduledAtMillis) {
        initialScheduledAtMillis
            ?.takeIf { it > System.currentTimeMillis() }
            ?: Calendar.getInstance().apply {
                add(Calendar.MINUTE, 5)
            }.timeInMillis
    }

    var scheduledAtMillis by remember(initialTime) {
        mutableLongStateOf(initialTime)
    }

    var activePicker by remember {
        mutableStateOf<SchedulePicker?>(null)
    }

    var validationError by remember {
        mutableStateOf<String?>(null)
    }

    val dateFormatter = remember {
        SimpleDateFormat(
            "MMM d, yyyy",
            Locale.ENGLISH
        )
    }

    val timeFormatter = remember {
        SimpleDateFormat(
            "HH:mm",
            Locale.ENGLISH
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ExitCallTopBar(
                title = if (isEditingScheduledCall) {
                    "Edit scheduled call"
                } else {
                    "Schedule a call"
                },
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = if (isEditingScheduledCall) {
                    "Change when your ExitCall should start."
                } else {
                    "Choose when your ExitCall should start."
                }
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            ScheduleOption(
                label = "DATE",
                value = dateFormatter.format(scheduledAtMillis),
                description = "Choose the day when your ExitCall should start.",
                onClick = {
                    activePicker = SchedulePicker.DATE
                }
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            ScheduleOption(
                label = "TIME",
                value = timeFormatter.format(scheduledAtMillis),
                description = "Choose the exact time for the call to begin.",
                onClick = {
                    activePicker = SchedulePicker.TIME
                }
            )

            validationError?.let { error ->
                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (scheduledAtMillis <= System.currentTimeMillis()) {
                        validationError =
                            "Choose a date and time in the future."
                    } else {
                        validationError = null
                        onSchedule(scheduledAtMillis)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (isEditingScheduledCall) {
                        "Save changes"
                    } else {
                        "Schedule a call"
                    },
                    fontSize = 18.sp
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = onCancelScheduledCall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(
                    text = "Cancel scheduled call",
                    fontSize = 18.sp
                )
            }
        }
    }

    when (activePicker) {
        SchedulePicker.DATE -> {
            ScheduleDatePickerDialog(
                scheduledAtMillis = scheduledAtMillis,
                onConfirm = { updatedScheduledAtMillis ->
                    scheduledAtMillis = updatedScheduledAtMillis
                    validationError = null
                    activePicker = null
                },
                onDismiss = {
                    activePicker = null
                }
            )
        }

        SchedulePicker.TIME -> {
            ScheduleTimePickerDialog(
                scheduledAtMillis = scheduledAtMillis,
                onConfirm = { updatedScheduledAtMillis ->
                    scheduledAtMillis = updatedScheduledAtMillis
                    validationError = null
                    activePicker = null
                },
                onDismiss = {
                    activePicker = null
                }
            )
        }

        null -> Unit
    }
}

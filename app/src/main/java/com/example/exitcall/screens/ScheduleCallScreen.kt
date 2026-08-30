package com.example.exitcall.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp


private val ExitCallBackground = Color(0xFF101311)
private val ExitCallSurface = Color(0xFF191D1A)
private val ExitCallGreen = Color(0xFFA8D5BA)
private val ExitCallText = Color(0xFFF2F4F2)
private val ExitCallMuted = Color(0xFF9BA39E)

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCallScreen(
    onBack: () -> Unit,
    onSchedule: (Long) -> Unit
) {
    val initialTime = remember {
        Calendar.getInstance().apply {
            add(Calendar.MINUTE, 5)
        }.timeInMillis
    }

    var scheduledAtMillis by remember {
        mutableLongStateOf(initialTime)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showTimePicker by remember {
        mutableStateOf(false)
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
            TopAppBar(
                title = {
                    Text("Schedule a call")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
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
                text = "Choose when your ExitCall should start."
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            ScheduleOption(
                label = "DATE",
                value = dateFormatter.format(scheduledAtMillis),
                description = "Choose the day when your ExitCall should start.",
                onClick = {
                    showDatePicker = true
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
                    showTimePicker = true
                }
            )

            validationError?.let { error ->
                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = error
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
                colors = buttonColors(
                    containerColor = ExitCallGreen,
                    contentColor = ExitCallBackground
                )
            ) {
                Text(
                    text = "Shedule a call",
                    fontSize = 18.sp
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis =
                localDateAsUtcMillis(scheduledAtMillis)
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            scheduledAtMillis =
                                replaceDate(
                                    baseMillis = scheduledAtMillis,
                                    selectedDateUtcMillis = it
                                )
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }

    if (showTimePicker) {
        val calendar = remember(scheduledAtMillis) {
            Calendar.getInstance().apply {
                timeInMillis = scheduledAtMillis
            }
        }

        val timePickerState = rememberTimePickerState(
            initialHour =
                calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute =
                calendar.get(Calendar.MINUTE),
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = {
                showTimePicker = false
            },
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
                        scheduledAtMillis =
                            replaceTime(
                                baseMillis = scheduledAtMillis,
                                hour = timePickerState.hour,
                                minute = timePickerState.minute
                            )

                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
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
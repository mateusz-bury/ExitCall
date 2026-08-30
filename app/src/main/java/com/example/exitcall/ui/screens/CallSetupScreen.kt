package com.example.exitcall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exitcall.model.CallConfig

@Composable
fun CallSetupScreen(
    initialConfig: CallConfig,
    onChooseContact: () -> Unit,
    onChooseRingtone: () -> Unit,
    onSave: (CallConfig) -> Unit
) {

    var callerName by remember {
        mutableStateOf(initialConfig.callerName)
    }

    var phoneNumber by remember {
        mutableStateOf(initialConfig.phoneNumber)
    }

    var vibrate by remember {
        mutableStateOf(initialConfig.vibrate)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(20.dp)
        ) {

            Text(
                text = "Set up a call",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Choose how your incoming call should look and feel.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            SetupCard {

                Text(
                    text = "Caller",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = callerName,
                    onValueChange = {
                        callerName = it
                    },
                    label = {
                        Text("Caller name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                    },
                    label = {
                        Text("Phone number")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedButton(
                    onClick = onChooseContact,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Choose contact")
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            SetupCard {

                Text(
                    text = "Call behavior",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {

                        Text(
                            text = "Ringtone",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )

                        Text(
                            text = initialConfig.ringtoneName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    TextButton(
                        onClick = onChooseRingtone
                    ) {
                        Text(
                            text = "Change",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {

                        Text(
                            text = "Vibrate",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )

                        Text(
                            text = "Use vibration during incoming call",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    Switch(
                        checked = vibrate,
                        onCheckedChange = {
                            vibrate = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedBorderColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {

                    onSave(
                        CallConfig(
                            callerName = callerName,
                            phoneNumber = phoneNumber,
                            ringtoneUri = initialConfig.ringtoneUri,
                            ringtoneName = initialConfig.ringtoneName,
                            vibrate = vibrate
                        )
                    )
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
                    text = "Save call setup",
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun SetupCard(
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp),
        content = content
    )
}

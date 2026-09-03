package com.mateuszbury.exitcall.ui.screens

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mateuszbury.exitcall.model.CallConfig

@Composable
fun CallSetupScreen(
    initialConfig: CallConfig,
    onChooseContact: () -> Unit,
    onSave: (CallConfig) -> Unit
) {
    val context = LocalContext.current

    var callerName by remember {
        mutableStateOf(initialConfig.callerName)
    }

    var phoneNumber by remember {
        mutableStateOf(initialConfig.phoneNumber)
    }

    var ringtoneUri by remember {
        mutableStateOf(initialConfig.ringtoneUri)
    }

    var ringtoneName by remember {
        mutableStateOf(initialConfig.ringtoneName)
    }

    var vibrate by remember {
        mutableStateOf(initialConfig.vibrate)
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            return@rememberLauncherForActivityResult
        }

        val pickedUri = getPickedRingtoneUri(result.data)

        if (pickedUri == null) {
            ringtoneUri = null
            ringtoneName = "System default"
            return@rememberLauncherForActivityResult
        }

        ringtoneUri = pickedUri.toString()
        ringtoneName = RingtoneManager
            .getRingtone(context, pickedUri)
            ?.getTitle(context)
            ?: "Selected ringtone"
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
                            text = ringtoneName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            val existingUri = ringtoneUri
                                ?.takeIf { it.isNotBlank() }
                                ?.let(Uri::parse)
                                ?: RingtoneManager.getDefaultUri(
                                    RingtoneManager.TYPE_RINGTONE
                                )

                            val pickerIntent = Intent(
                                RingtoneManager.ACTION_RINGTONE_PICKER
                            ).apply {
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_TYPE,
                                    RingtoneManager.TYPE_RINGTONE
                                )
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_TITLE,
                                    "Choose ringtone"
                                )
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    existingUri
                                )
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
                                    true
                                )
                                putExtra(
                                    RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
                                    false
                                )
                            }

                            ringtonePickerLauncher.launch(pickerIntent)
                        }
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
                            ringtoneUri = ringtoneUri,
                            ringtoneName = ringtoneName,
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

@Suppress("DEPRECATION")
private fun getPickedRingtoneUri(intent: Intent?): Uri? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent?.getParcelableExtra(
            RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
            Uri::class.java
        )
    } else {
        intent?.getParcelableExtra(
            RingtoneManager.EXTRA_RINGTONE_PICKED_URI
        )
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

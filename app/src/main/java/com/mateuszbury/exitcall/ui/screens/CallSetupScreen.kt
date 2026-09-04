package com.mateuszbury.exitcall.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mateuszbury.exitcall.model.AppSettings
import com.mateuszbury.exitcall.model.CallConfig
import com.mateuszbury.exitcall.phone.PhoneNumberFormatter

private data class PickedContact(
    val displayName: String,
    val phoneNumber: String
)

@Composable
fun CallSetupScreen(
    initialConfig: CallConfig,
    appSettings: AppSettings,
    onSave: (CallConfig) -> Unit
) {
    val context = LocalContext.current

    var callerName by remember {
        mutableStateOf(initialConfig.callerName)
    }

    var phoneNumber by remember {
        mutableStateOf(
            PhoneNumberFormatter.formatForDisplay(
                initialConfig.phoneNumber
            )
        )
    }

    var contactPickerError by remember {
        mutableStateOf<String?>(null)
    }

    val phoneValidation = PhoneNumberFormatter.validate(phoneNumber)

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            return@rememberLauncherForActivityResult
        }

        val selectedUri = result.data?.data

        if (selectedUri == null) {
            contactPickerError = "Unable to read the selected contact."
            return@rememberLauncherForActivityResult
        }

        val pickedContact = readPickedPhoneContact(
            context = context,
            uri = selectedUri
        )

        if (pickedContact == null) {
            contactPickerError = "Unable to read the selected phone number."
            return@rememberLauncherForActivityResult
        }

        if (pickedContact.displayName.isNotBlank()) {
            callerName = pickedContact.displayName
        }

        val pickedPhoneValidation = PhoneNumberFormatter.validate(
            pickedContact.phoneNumber
        )

        phoneNumber = if (pickedPhoneValidation.isValid) {
            PhoneNumberFormatter.formatForDisplay(
                pickedPhoneValidation.normalized
            )
        } else {
            pickedContact.phoneNumber
        }

        contactPickerError = null
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose who should call you.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            SetupCard {
                Text(
                    text = "Caller",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = callerName,
                    onValueChange = { callerName = it },
                    label = { Text("Caller name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        contactPickerError = null
                    },
                    label = { Text("Phone number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !phoneValidation.isValid,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    )
                )

                if (!phoneValidation.isValid) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = phoneValidation.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                contactPickerError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        contactPickerError = null

                        val pickerIntent = Intent(Intent.ACTION_PICK).apply {
                            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                        }

                        runCatching {
                            contactPickerLauncher.launch(pickerIntent)
                        }.onFailure {
                            contactPickerError =
                                "Contact picker is not available on this device."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Choose contact")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SetupCard {
                Text(
                    text = "App defaults",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = appSettings.ringtoneName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )

                Text(
                    text = buildString {
                        append(
                            if (appSettings.vibrate) {
                                "Vibration on"
                            } else {
                                "Vibration off"
                            }
                        )
                        append(" • ${appSettings.ringDurationSeconds}s ring duration")
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Change call defaults in Settings.",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (!phoneValidation.isValid) {
                        return@Button
                    }

                    onSave(
                        initialConfig.copy(
                            callerName = callerName,
                            phoneNumber = phoneValidation.normalized
                        )
                    )
                },
                enabled = phoneValidation.isValid,
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

private fun readPickedPhoneContact(
    context: Context,
    uri: Uri
): PickedContact? {
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    return runCatching {
        context.contentResolver
            .query(
                uri,
                projection,
                null,
                null,
                null
            )
            ?.use { cursor ->
                if (!cursor.moveToFirst()) {
                    return@use null
                }

                val nameIndex = cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )
                val numberIndex = cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )

                if (numberIndex < 0) {
                    return@use null
                }

                val number = cursor
                    .getString(numberIndex)
                    ?.trim()
                    .orEmpty()

                if (number.isBlank()) {
                    return@use null
                }

                val displayName = if (nameIndex >= 0) {
                    cursor
                        .getString(nameIndex)
                        ?.trim()
                        .orEmpty()
                } else {
                    ""
                }

                PickedContact(
                    displayName = displayName,
                    phoneNumber = number
                )
            }
    }.getOrNull()
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

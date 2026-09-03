package com.mateuszbury.exitcall.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
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

private data class PickedPhone(
    val number: String,
    val label: String
)

private data class PickedContact(
    val displayName: String,
    val phoneNumbers: List<PickedPhone>
)

@Composable
fun CallSetupScreen(
    initialConfig: CallConfig,
    onSave: (CallConfig) -> Unit
) {
    val context = LocalContext.current

    var callerName by remember {
        mutableStateOf(initialConfig.callerName)
    }

    var phoneNumber by remember {
        mutableStateOf(initialConfig.phoneNumber)
    }

    var contactPickerError by remember {
        mutableStateOf<String?>(null)
    }

    var pendingPhoneChoice by remember {
        mutableStateOf<PickedContact?>(null)
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

    fun applyPickedPhone(
        contact: PickedContact,
        phone: PickedPhone
    ) {
        if (contact.displayName.isNotBlank()) {
            callerName = contact.displayName
        }

        phoneNumber = phone.number
        contactPickerError = null
        pendingPhoneChoice = null
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { selectedUri ->
        if (selectedUri == null) {
            return@rememberLauncherForActivityResult
        }

        val pickedContact = readPickedContact(
            context = context,
            uri = selectedUri
        )

        if (pickedContact == null || pickedContact.phoneNumbers.isEmpty()) {
            contactPickerError = "This contact does not have a usable phone number."
            return@rememberLauncherForActivityResult
        }

        if (pickedContact.phoneNumbers.size == 1) {
            applyPickedPhone(
                contact = pickedContact,
                phone = pickedContact.phoneNumbers.first()
            )
        } else {
            pendingPhoneChoice = pickedContact
            contactPickerError = null
        }
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

                contactPickerError?.let { error ->
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedButton(
                    onClick = {
                        contactPickerError = null

                        runCatching {
                            contactPickerLauncher.launch(null)
                        }.onFailure {
                            contactPickerError = "Contact picker is not available on this device."
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

    pendingPhoneChoice?.let { contact ->
        AlertDialog(
            onDismissRequest = {
                pendingPhoneChoice = null
            },
            title = {
                Text("Choose phone number")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    contact.phoneNumbers.forEach { phone ->
                        TextButton(
                            onClick = {
                                applyPickedPhone(
                                    contact = contact,
                                    phone = phone
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = phone.label,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = phone.number,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingPhoneChoice = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun readPickedContact(
    context: Context,
    uri: Uri
): PickedContact? {
    return runCatching {
        val displayName = context.contentResolver
            .query(
                uri,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
                null,
                null,
                null
            )
            ?.use { cursor ->
                if (!cursor.moveToFirst()) {
                    return@use ""
                }

                val nameIndex = cursor.getColumnIndex(
                    ContactsContract.Contacts.DISPLAY_NAME
                )

                if (nameIndex >= 0) {
                    cursor.getString(nameIndex)?.trim().orEmpty()
                } else {
                    ""
                }
            }
            .orEmpty()

        val entityUri = Uri.withAppendedPath(
            uri,
            ContactsContract.Contacts.Entity.CONTENT_DIRECTORY
        )

        val phoneNumbers = linkedMapOf<String, PickedPhone>()

        context.contentResolver
            .query(
                entityUri,
                arrayOf(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.LABEL
                ),
                null,
                null,
                null
            )
            ?.use { cursor ->
                val mimeIndex = cursor.getColumnIndex(
                    ContactsContract.Data.MIMETYPE
                )
                val numberIndex = cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                val typeIndex = cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.TYPE
                )
                val labelIndex = cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.LABEL
                )

                while (cursor.moveToNext()) {
                    if (
                        mimeIndex < 0 ||
                        numberIndex < 0 ||
                        cursor.getString(mimeIndex) !=
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    ) {
                        continue
                    }

                    val number = cursor
                        .getString(numberIndex)
                        ?.trim()
                        .orEmpty()

                    if (number.isBlank()) {
                        continue
                    }

                    val type = if (typeIndex >= 0) {
                        cursor.getInt(typeIndex)
                    } else {
                        ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
                    }

                    val customLabel = if (labelIndex >= 0) {
                        cursor.getString(labelIndex)
                    } else {
                        null
                    }

                    val label = ContactsContract.CommonDataKinds.Phone
                        .getTypeLabel(
                            context.resources,
                            type,
                            customLabel
                        )
                        .toString()

                    val deduplicationKey = number.filter(Char::isDigit)

                    if (deduplicationKey.isNotBlank()) {
                        phoneNumbers.putIfAbsent(
                            deduplicationKey,
                            PickedPhone(
                                number = number,
                                label = label
                            )
                        )
                    }
                }
            }

        PickedContact(
            displayName = displayName,
            phoneNumbers = phoneNumbers.values.toList()
        )
    }.getOrNull()
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

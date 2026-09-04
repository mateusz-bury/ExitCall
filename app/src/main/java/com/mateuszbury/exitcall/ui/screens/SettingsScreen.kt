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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mateuszbury.exitcall.capability.CallCapability
import com.mateuszbury.exitcall.model.AppSettings
import com.mateuszbury.exitcall.model.RingDuration

@Composable
fun SettingsScreen(
    settings: AppSettings,
    capabilityAvailability: Map<CallCapability, Boolean>,
    appVersionName: String,
    onSettingsChange: (AppSettings) -> Unit,
    onOpenCapabilitySettings: (CallCapability) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var customDuration by rememberSaveable(settings.ringDurationSeconds) {
        mutableStateOf(settings.ringDurationSeconds.toString())
    }

    var customSelected by rememberSaveable(settings.ringDurationSeconds) {
        mutableStateOf(settings.ringDurationSeconds !in RingDuration.presets)
    }

    val parsedCustomDuration = customDuration.toIntOrNull()
    val customDurationValid = parsedCustomDuration?.let(RingDuration::isValid) == true

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            return@rememberLauncherForActivityResult
        }

        val pickedUri = getSettingsRingtoneUri(result.data)

        if (pickedUri == null) {
            onSettingsChange(
                settings.copy(
                    ringtoneUri = null,
                    ringtoneName = "System default"
                )
            )
            return@rememberLauncherForActivityResult
        }

        val ringtoneName = runCatching {
            RingtoneManager
                .getRingtone(context, pickedUri)
                ?.getTitle(context)
        }.getOrNull() ?: "Selected ringtone"

        onSettingsChange(
            settings.copy(
                ringtoneUri = pickedUri.toString(),
                ringtoneName = ringtoneName
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 16.dp,
            end = 20.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Settings",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "Set your defaults and check device readiness.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }

        item {
            SettingsSection(title = "CALL SETTINGS") {
                SettingsActionRow(
                    title = "Ringtone",
                    subtitle = settings.ringtoneName,
                    actionLabel = "Change",
                    onClick = {
                        val existingUri = settings.ringtoneUri
                            ?.takeIf(String::isNotBlank)
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
                )

                SettingsDivider()

                SettingsSwitchRow(
                    title = "Vibrate",
                    subtitle = "Use vibration during incoming calls",
                    checked = settings.vibrate,
                    onCheckedChange = { enabled ->
                        onSettingsChange(settings.copy(vibrate = enabled))
                    }
                )

                SettingsDivider()

                Text(
                    text = "Ring duration",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Text(
                    text = "End an unanswered call automatically",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                RingDuration.presets.forEach { duration ->
                    DurationOption(
                        label = "$duration seconds",
                        selected = !customSelected &&
                            settings.ringDurationSeconds == duration,
                        onClick = {
                            customSelected = false
                            customDuration = duration.toString()
                            onSettingsChange(
                                settings.copy(ringDurationSeconds = duration)
                            )
                        }
                    )
                }

                DurationOption(
                    label = "Custom",
                    selected = customSelected,
                    onClick = {
                        customSelected = true

                        if (customDurationValid) {
                            onSettingsChange(
                                settings.copy(
                                    ringDurationSeconds = parsedCustomDuration!!
                                )
                            )
                        }
                    }
                )

                if (customSelected) {
                    OutlinedTextField(
                        value = customDuration,
                        onValueChange = { value ->
                            if (value.length <= 2 && value.all(Char::isDigit)) {
                                customDuration = value

                                value.toIntOrNull()
                                    ?.takeIf(RingDuration::isValid)
                                    ?.let { duration ->
                                        onSettingsChange(
                                            settings.copy(
                                                ringDurationSeconds = duration
                                            )
                                        )
                                    }
                            }
                        },
                        label = { Text("Seconds") },
                        supportingText = {
                            Text("Enter a whole number from 5 to 60")
                        },
                        isError = customDuration.isNotEmpty() &&
                            !customDurationValid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                SettingsDivider()

                ComingSoonRow(
                    title = "Voice",
                    subtitle = "Default voice and audio scenario"
                )

                SettingsDivider()

                ComingSoonRow(
                    title = "Audio output",
                    subtitle = "System, earpiece, speaker or Bluetooth"
                )
            }
        }

        item {
            SettingsSection(title = "PERMISSIONS & DEVICE") {
                CapabilityRow(
                    title = "Notifications",
                    available = capabilityAvailability[
                        CallCapability.NOTIFICATIONS
                    ] == true,
                    onManage = {
                        onOpenCapabilitySettings(CallCapability.NOTIFICATIONS)
                    }
                )

                SettingsDivider()

                CapabilityRow(
                    title = "Exact alarms",
                    available = capabilityAvailability[
                        CallCapability.EXACT_ALARM
                    ] == true,
                    onManage = {
                        onOpenCapabilitySettings(CallCapability.EXACT_ALARM)
                    }
                )

                SettingsDivider()

                CapabilityRow(
                    title = "Full-screen calls",
                    available = capabilityAvailability[
                        CallCapability.FULL_SCREEN_INTENT
                    ] == true,
                    onManage = {
                        onOpenCapabilitySettings(
                            CallCapability.FULL_SCREEN_INTENT
                        )
                    }
                )

                SettingsDivider()

                ComingSoonRow(
                    title = "Microphone",
                    subtitle = "Required only for custom recordings"
                )

                SettingsDivider()

                ComingSoonRow(
                    title = "Battery optimization",
                    subtitle = "Device-specific reliability guidance"
                )
            }
        }

        item {
            SettingsSection(title = "APPEARANCE & BEHAVIOR") {
                ComingSoonRow(
                    title = "Language",
                    subtitle = "System default"
                )
                SettingsDivider()
                ComingSoonRow(
                    title = "Theme",
                    subtitle = "System, light or dark"
                )
                SettingsDivider()
                ComingSoonRow(
                    title = "After call",
                    subtitle = "Choose where ExitCall returns"
                )
            }
        }

        item {
            SettingsSection(title = "HELP & INFORMATION") {
                ComingSoonRow(
                    title = "View tutorial again",
                    subtitle = "The one-time demo will not reset"
                )
                SettingsDivider()
                ComingSoonRow(
                    title = "Privacy, safe use & support",
                    subtitle = "Product and support information"
                )
                SettingsDivider()
                ComingSoonRow(
                    title = "Open-source licenses",
                    subtitle = "Third-party software notices"
                )
                SettingsDivider()
                ReadOnlyRow(
                    title = "App version",
                    value = appVersionName
                )
            }
        }

        item {
            SettingsSection(title = "ACCOUNT & DATA") {
                ComingSoonRow(
                    title = "Restore purchase",
                    subtitle = "Restore a previous store purchase"
                )
                SettingsDivider()
                ComingSoonRow(
                    title = "Manage local data",
                    subtitle = "History, profiles and custom recordings"
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

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
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsRowText(
            title = title,
            subtitle = subtitle,
            modifier = Modifier.weight(1f)
        )

        TextButton(onClick = onClick) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsRowText(
            title = title,
            subtitle = subtitle,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun DurationOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun CapabilityRow(
    title: String,
    available: Boolean,
    onManage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsRowText(
            title = title,
            subtitle = if (available) "Ready" else "Action required",
            modifier = Modifier.weight(1f)
        )

        TextButton(onClick = onManage) {
            Text("Manage")
        }
    }
}

@Composable
private fun ComingSoonRow(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsRowText(
            title = title,
            subtitle = subtitle,
            modifier = Modifier.weight(1f),
            enabled = false
        )

        Text(
            text = "Coming soon",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ReadOnlyRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SettingsRowText(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontSize = 16.sp
        )
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 14.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Suppress("DEPRECATION")
private fun getSettingsRingtoneUri(intent: Intent?): Uri? {
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

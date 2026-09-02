package com.mateuszbury.exitcall.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.mateuszbury.exitcall.capability.CallCapability

@Composable
fun CallCapabilityDialog(
    capability: CallCapability,
    canRequestNotificationPermission: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val title: String
    val message: String
    val confirmLabel: String

    when (capability) {
        CallCapability.NOTIFICATIONS -> {
            title = "Notifications required"
            message =
                "ExitCall needs notifications to show incoming call controls reliably. No call will be started or scheduled until this is enabled."
            confirmLabel = if (canRequestNotificationPermission) {
                "Allow notifications"
            } else {
                "Open settings"
            }
        }

        CallCapability.FULL_SCREEN_INTENT -> {
            title = "Full-screen calls required"
            message =
                "Allow full-screen notifications so an ExitCall can appear over the lock screen when you need it."
            confirmLabel = "Open settings"
        }

        CallCapability.EXACT_ALARM -> {
            title = "Exact alarms required"
            message =
                "Allow exact alarms so scheduled calls can start at the time you choose."
            confirmLabel = "Open settings"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Not now")
            }
        }
    )
}

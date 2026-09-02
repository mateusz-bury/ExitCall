package com.mateuszbury.exitcall.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mateuszbury.exitcall.capability.CallCapability
import com.mateuszbury.exitcall.model.CallConfig
import com.mateuszbury.exitcall.ui.components.CallCapabilityDialog
import com.mateuszbury.exitcall.ui.screens.CallSetupScreen
import com.mateuszbury.exitcall.ui.screens.HomeScreen
import com.mateuszbury.exitcall.ui.screens.ScheduleCallScreen

private enum class MainScreen {
    HOME,
    CALL_SETUP,
    SCHEDULE_CALL
}

@Composable
fun MainApp(
    initialCallConfig: CallConfig,
    scheduledCallAtMillis: Long?,
    capabilityIssue: CallCapability?,
    canRequestNotificationPermission: Boolean,
    onRequireCallCapabilities: (
        requireExactAlarm: Boolean,
        onReady: () -> Unit
    ) -> Unit,
    onStartImmediateCall: () -> Unit,
    onScheduleCall: (Long) -> Boolean,
    onCancelScheduledCall: () -> Unit,
    onSaveCallConfig: (CallConfig) -> Unit,
    onCapabilityConfirm: (CallCapability) -> Unit,
    onCapabilityDismiss: () -> Unit
) {
    var currentScreen by remember {
        mutableStateOf(MainScreen.HOME)
    }

    var callConfig by remember {
        mutableStateOf(initialCallConfig)
    }

    BackHandler(
        enabled = currentScreen != MainScreen.HOME
    ) {
        currentScreen = MainScreen.HOME
    }

    when (currentScreen) {
        MainScreen.HOME -> {
            HomeScreen(
                scheduledCallAtMillis = scheduledCallAtMillis,
                onCallMeNow = {
                    onRequireCallCapabilities(
                        false,
                        onStartImmediateCall
                    )
                },
                onScheduleCall = {
                    onRequireCallCapabilities(true) {
                        currentScreen = MainScreen.SCHEDULE_CALL
                    }
                },
                onCancelScheduledCall = onCancelScheduledCall,
                onCallSetup = {
                    currentScreen = MainScreen.CALL_SETUP
                },
                onSettings = {
                    // TODO settings
                }
            )
        }

        MainScreen.CALL_SETUP -> {
            CallSetupScreen(
                initialConfig = callConfig,
                onChooseContact = {
                    // TODO contact picker
                },
                onChooseRingtone = {
                    // TODO system ringtone picker
                },
                onSave = { newConfig ->
                    onSaveCallConfig(newConfig)
                    callConfig = newConfig
                    currentScreen = MainScreen.HOME
                }
            )
        }

        MainScreen.SCHEDULE_CALL -> {
            ScheduleCallScreen(
                initialScheduledAtMillis = scheduledCallAtMillis,
                onBack = {
                    currentScreen = MainScreen.HOME
                },
                onSchedule = { triggerTimeMillis ->
                    onRequireCallCapabilities(true) {
                        if (onScheduleCall(triggerTimeMillis)) {
                            currentScreen = MainScreen.HOME
                        }
                    }
                }
            )
        }
    }

    capabilityIssue?.let { capability ->
        CallCapabilityDialog(
            capability = capability,
            canRequestNotificationPermission =
                canRequestNotificationPermission,
            onConfirm = {
                onCapabilityConfirm(capability)
            },
            onDismiss = onCapabilityDismiss
        )
    }
}

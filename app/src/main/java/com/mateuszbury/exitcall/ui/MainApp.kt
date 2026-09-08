package com.mateuszbury.exitcall.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mateuszbury.exitcall.capability.CallCapability
import com.mateuszbury.exitcall.model.AppSettings
import com.mateuszbury.exitcall.model.CallConfig
import com.mateuszbury.exitcall.ui.components.CallCapabilityDialog
import com.mateuszbury.exitcall.ui.screens.CallSetupScreen
import com.mateuszbury.exitcall.ui.screens.DemoCallFlow
import com.mateuszbury.exitcall.ui.screens.HomeScreen
import com.mateuszbury.exitcall.ui.screens.OnboardingScreen
import com.mateuszbury.exitcall.ui.screens.ScheduleCallScreen
import com.mateuszbury.exitcall.ui.screens.SettingsScreen

private enum class MainScreen {
    HOME,
    CALL_SETUP,
    SCHEDULE_CALL,
    SETTINGS,
    TUTORIAL,
    TUTORIAL_DEMO,
    DEMO_REPLAY
}

@Composable
fun MainApp(
    initialCallConfig: CallConfig,
    initialAppSettings: AppSettings,
    scheduledCallAtMillis: Long?,
    capabilityIssue: CallCapability?,
    capabilityAvailability: Map<CallCapability, Boolean>,
    canRequestNotificationPermission: Boolean,
    appVersionName: String,
    onRequireCallCapabilities: (
        requireExactAlarm: Boolean,
        onReady: () -> Unit
    ) -> Unit,
    onStartImmediateCall: () -> Unit,
    onScheduleCall: (Long) -> Boolean,
    onCancelScheduledCall: () -> Unit,
    onSaveCallConfig: (CallConfig) -> Unit,
    onSaveAppSettings: (AppSettings) -> Unit,
    onOpenCapabilitySettings: (CallCapability) -> Unit,
    onCapabilityConfirm: (CallCapability) -> Unit,
    onCapabilityDismiss: () -> Unit
) {
    var currentScreen by remember {
        mutableStateOf(MainScreen.HOME)
    }

    var callConfig by remember {
        mutableStateOf(initialCallConfig)
    }

    var appSettings by remember {
        mutableStateOf(initialAppSettings)
    }

    BackHandler(
        enabled = currentScreen != MainScreen.HOME &&
            currentScreen != MainScreen.TUTORIAL_DEMO &&
            currentScreen != MainScreen.DEMO_REPLAY
    ) {
        currentScreen = if (currentScreen == MainScreen.TUTORIAL) {
            MainScreen.SETTINGS
        } else {
            MainScreen.HOME
        }
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
                onCallSetup = {
                    currentScreen = MainScreen.CALL_SETUP
                },
                onSettings = {
                    currentScreen = MainScreen.SETTINGS
                }
            )
        }

        MainScreen.CALL_SETUP -> {
            CallSetupScreen(
                initialConfig = callConfig,
                appSettings = appSettings,
                onBack = {
                    currentScreen = MainScreen.HOME
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
                },
                onCancelScheduledCall = {
                    onCancelScheduledCall()
                    currentScreen = MainScreen.HOME
                }
            )
        }

        MainScreen.SETTINGS -> {
            SettingsScreen(
                settings = appSettings,
                capabilityAvailability = capabilityAvailability,
                appVersionName = appVersionName,
                onSettingsChange = { newSettings ->
                    onSaveAppSettings(newSettings)
                    appSettings = newSettings
                },
                onOpenCapabilitySettings = onOpenCapabilitySettings,
                onReplayDemo = {
                    onRequireCallCapabilities(false) {
                        currentScreen = MainScreen.DEMO_REPLAY
                    }
                },
                onViewTutorial = {
                    currentScreen = MainScreen.TUTORIAL
                },
                onBack = {
                    currentScreen = MainScreen.HOME
                }
            )
        }

        MainScreen.TUTORIAL -> {
            OnboardingScreen(
                capabilityAvailability = capabilityAvailability,
                onRequestNotifications = {
                    onCapabilityConfirm(CallCapability.NOTIFICATIONS)
                },
                onOpenFullScreenSettings = {
                    onOpenCapabilitySettings(CallCapability.FULL_SCREEN_INTENT)
                },
                onComplete = {
                    onRequireCallCapabilities(false) {
                        currentScreen = MainScreen.TUTORIAL_DEMO
                    }
                }
            )
        }

        MainScreen.TUTORIAL_DEMO, MainScreen.DEMO_REPLAY -> {
            DemoCallFlow(
                onCompleted = {
                    currentScreen = MainScreen.SETTINGS
                },
                onCancelled = {
                    currentScreen = if (currentScreen == MainScreen.DEMO_REPLAY) {
                        MainScreen.SETTINGS
                    } else {
                        MainScreen.TUTORIAL
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


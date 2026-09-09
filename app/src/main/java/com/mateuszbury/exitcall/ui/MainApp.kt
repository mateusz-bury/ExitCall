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
import com.mateuszbury.exitcall.power.BatteryOptimizationStatus
import com.mateuszbury.exitcall.ui.components.CallCapabilityDialog
import com.mateuszbury.exitcall.ui.screens.CallSetupScreen
import com.mateuszbury.exitcall.ui.screens.HomeScreen
import com.mateuszbury.exitcall.ui.screens.InformationScreen
import com.mateuszbury.exitcall.ui.screens.OnboardingScreen
import com.mateuszbury.exitcall.ui.screens.OpenSourceLicensesScreen
import com.mateuszbury.exitcall.ui.screens.ScheduleCallScreen
import com.mateuszbury.exitcall.ui.screens.SettingsScreen

private enum class MainScreen {
    HOME,
    CALL_SETUP,
    SCHEDULE_CALL,
    SETTINGS,
    TUTORIAL,
    INFORMATION,
    OPEN_SOURCE_LICENSES
}

@Composable
fun MainApp(
    initialCallConfig: CallConfig,
    initialAppSettings: AppSettings,
    scheduledCallAtMillis: Long?,
    capabilityIssue: CallCapability?,
    capabilityAvailability: Map<CallCapability, Boolean>,
    batteryOptimizationStatus: BatteryOptimizationStatus,
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
    onOpenBatteryOptimizationSettings: () -> Unit,
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
        enabled = currentScreen != MainScreen.HOME
    ) {
        currentScreen = when (currentScreen) {
            MainScreen.TUTORIAL,
            MainScreen.INFORMATION,
            MainScreen.OPEN_SOURCE_LICENSES -> MainScreen.SETTINGS
            else -> MainScreen.HOME
        }
    }

    when (currentScreen) {
        MainScreen.HOME -> {
            HomeScreen(
                callConfig = callConfig,
                appSettings = appSettings,
                scheduledCallAtMillis = scheduledCallAtMillis,
                capabilityAvailability = capabilityAvailability,
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
                },
                onOpenCapabilitySettings = onOpenCapabilitySettings
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
                batteryOptimizationStatus = batteryOptimizationStatus,
                appVersionName = appVersionName,
                onSettingsChange = { newSettings ->
                    onSaveAppSettings(newSettings)
                    appSettings = newSettings
                },
                onOpenCapabilitySettings = onOpenCapabilitySettings,
                onOpenBatteryOptimizationSettings =
                    onOpenBatteryOptimizationSettings,
                onViewTutorial = {
                    currentScreen = MainScreen.TUTORIAL
                },
                onOpenInformation = {
                    currentScreen = MainScreen.INFORMATION
                },
                onOpenLicenses = {
                    currentScreen = MainScreen.OPEN_SOURCE_LICENSES
                },
                onBack = {
                    currentScreen = MainScreen.HOME
                }
            )
        }

        MainScreen.INFORMATION -> {
            InformationScreen(
                appVersionName = appVersionName,
                onBack = {
                    currentScreen = MainScreen.SETTINGS
                }
            )
        }

        MainScreen.OPEN_SOURCE_LICENSES -> {
            OpenSourceLicensesScreen(
                onBack = {
                    currentScreen = MainScreen.SETTINGS
                }
            )
        }

        MainScreen.TUTORIAL -> {
            OnboardingScreen(
                capabilityAvailability = capabilityAvailability,
                launchesDemoOnComplete = false,
                onRequestNotifications = {
                    onCapabilityConfirm(CallCapability.NOTIFICATIONS)
                },
                onOpenFullScreenSettings = {
                    onOpenCapabilitySettings(CallCapability.FULL_SCREEN_INTENT)
                },
                onComplete = {
                    currentScreen = MainScreen.SETTINGS
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


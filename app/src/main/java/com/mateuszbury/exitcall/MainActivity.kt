package com.mateuszbury.exitcall

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mateuszbury.exitcall.model.CallConfigStore
import com.mateuszbury.exitcall.model.ScheduledCallStore
import com.mateuszbury.exitcall.scheduler.ScheduledCallScheduler
import com.mateuszbury.exitcall.ui.screens.CallSetupScreen
import com.mateuszbury.exitcall.ui.screens.HomeScreen
import com.mateuszbury.exitcall.ui.screens.ScheduleCallScreen
import com.mateuszbury.exitcall.ui.theme.ExitCallTheme
import java.util.Locale

private enum class CallCapability {
    NOTIFICATIONS,
    FULL_SCREEN_INTENT,
    EXACT_ALARM
}

class MainActivity : ComponentActivity() {
    private enum class MainScreen {
        HOME,
        CALL_SETUP,
        SCHEDULE_CALL
    }

    private lateinit var scheduledCallStore: ScheduledCallStore
    private lateinit var scheduledCallScheduler: ScheduledCallScheduler

    private var scheduledCallAtMillis by mutableStateOf<Long?>(null)
    private var capabilityIssue by mutableStateOf<CallCapability?>(null)

    private var pendingCapabilityAction: (() -> Unit)? = null
    private var pendingRequiresExactAlarm = false
    private var waitingForCapabilitySettings = false
    private var notificationPermissionRequestedThisSession = false

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                continuePendingCapabilityAction()
            } else {
                notificationPermissionRequestedThisSession = true
                capabilityIssue = CallCapability.NOTIFICATIONS
            }
        }

    private fun startImmediateCall() {
        val serviceIntent = Intent(
            this,
            IncomingCallService::class.java
        )

        ContextCompat.startForegroundService(
            this,
            serviceIntent
        )
    }

    private fun scheduleCall(triggerTimeMillis: Long): Boolean {
        return when (
            scheduledCallScheduler.schedule(triggerTimeMillis)
        ) {
            ScheduledCallScheduler.RegistrationResult.SCHEDULED -> {
                scheduledCallAtMillis = triggerTimeMillis
                true
            }

            ScheduledCallScheduler.RegistrationResult.MISSING_EXACT_ALARM_PERMISSION -> {
                capabilityIssue = CallCapability.EXACT_ALARM
                false
            }

            ScheduledCallScheduler.RegistrationResult.NO_PENDING_CALL,
            ScheduledCallScheduler.RegistrationResult.TRIGGER_TIME_PASSED,
            ScheduledCallScheduler.RegistrationResult.FAILED -> {
                false
            }
        }
    }

    private fun ensureCallCapabilities(
        requireExactAlarm: Boolean,
        onReady: () -> Unit
    ) {
        pendingCapabilityAction = onReady
        pendingRequiresExactAlarm = requireExactAlarm

        continuePendingCapabilityAction()
    }

    private fun continuePendingCapabilityAction() {
        val missingCapability = firstMissingCapability(
            requireExactAlarm = pendingRequiresExactAlarm
        )

        if (missingCapability != null) {
            capabilityIssue = missingCapability
            return
        }

        val action = pendingCapabilityAction

        pendingCapabilityAction = null
        pendingRequiresExactAlarm = false
        capabilityIssue = null

        action?.invoke()
    }

    private fun firstMissingCapability(
        requireExactAlarm: Boolean
    ): CallCapability? {
        if (!hasNotificationCapability()) {
            return CallCapability.NOTIFICATIONS
        }

        if (!hasFullScreenIntentCapability()) {
            return CallCapability.FULL_SCREEN_INTENT
        }

        if (
            requireExactAlarm &&
            !hasExactAlarmCapability()
        ) {
            return CallCapability.EXACT_ALARM
        }

        return null
    }

    private fun hasNotificationCapability(): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return NotificationManagerCompat
            .from(this)
            .areNotificationsEnabled()
    }

    private fun canRequestNotificationRuntimePermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED &&
            !notificationPermissionRequestedThisSession
    }

    private fun hasFullScreenIntentCapability(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return true
        }

        return getSystemService(NotificationManager::class.java)
            .canUseFullScreenIntent()
    }

    private fun hasExactAlarmCapability(): Boolean {
        return scheduledCallScheduler.canScheduleExactAlarms()
    }

    private fun handleCapabilityAction(
        capability: CallCapability
    ) {
        when (capability) {
            CallCapability.NOTIFICATIONS -> {
                if (canRequestNotificationRuntimePermission()) {
                    capabilityIssue = null
                    notificationPermissionRequestedThisSession = true

                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    openNotificationSettings()
                }
            }

            CallCapability.FULL_SCREEN_INTENT -> {
                openFullScreenIntentSettings()
            }

            CallCapability.EXACT_ALARM -> {
                openExactAlarmSettings()
            }
        }
    }

    private fun openNotificationSettings() {
        openCapabilitySettings(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(
                    Settings.EXTRA_APP_PACKAGE,
                    packageName
                )
            }
        )
    }

    private fun openFullScreenIntentSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            continuePendingCapabilityAction()
            return
        }

        openCapabilitySettings(
            Intent(
                Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                Uri.parse("package:$packageName")
            )
        )
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            continuePendingCapabilityAction()
            return
        }

        openCapabilitySettings(
            Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:$packageName")
            )
        )
    }

    private fun openCapabilitySettings(intent: Intent) {
        capabilityIssue = null
        waitingForCapabilitySettings = true

        runCatching {
            startActivity(intent)
        }.onFailure {
            waitingForCapabilitySettings = true

            runCatching {
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:$packageName")
                    )
                )
            }.onFailure {
                waitingForCapabilitySettings = false
                capabilityIssue = firstMissingCapability(
                    requireExactAlarm = pendingRequiresExactAlarm
                )
            }
        }
    }

    private fun cancelPendingCapabilityAction() {
        capabilityIssue = null
        pendingCapabilityAction = null
        pendingRequiresExactAlarm = false
    }

    override fun attachBaseContext(newBase: Context) {
        val locale = Locale.ENGLISH

        Locale.setDefault(locale)

        val configuration = Configuration(
            newBase.resources.configuration
        ).apply {
            setLocale(locale)
        }

        super.attachBaseContext(
            newBase.createConfigurationContext(configuration)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val callConfigStore = CallConfigStore(this)
        scheduledCallStore = ScheduledCallStore(this)
        scheduledCallScheduler = ScheduledCallScheduler(this)
        scheduledCallScheduler.restorePersistedCall()
        scheduledCallAtMillis = scheduledCallStore.load()

        setContent {
            ExitCallTheme {
                var currentScreen by remember {
                    mutableStateOf(MainScreen.HOME)
                }

                var callConfig by remember {
                    mutableStateOf(
                        callConfigStore.load()
                    )
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
                                ensureCallCapabilities(
                                    requireExactAlarm = false
                                ) {
                                    startImmediateCall()
                                }
                            },
                            onScheduleCall = {
                                ensureCallCapabilities(
                                    requireExactAlarm = true
                                ) {
                                    currentScreen = MainScreen.SCHEDULE_CALL
                                }
                            },
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
                                callConfigStore.save(newConfig)
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
                                ensureCallCapabilities(
                                    requireExactAlarm = true
                                ) {
                                    if (scheduleCall(triggerTimeMillis)) {
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
                            canRequestNotificationRuntimePermission(),
                        onConfirm = {
                            handleCapabilityAction(capability)
                        },
                        onDismiss = {
                            cancelPendingCapabilityAction()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (::scheduledCallStore.isInitialized) {
            scheduledCallAtMillis = scheduledCallStore.load()
        }

        if (waitingForCapabilitySettings) {
            waitingForCapabilitySettings = false

            if (pendingCapabilityAction != null) {
                continuePendingCapabilityAction()
            }
        }
    }
}

@Composable
private fun CallCapabilityDialog(
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

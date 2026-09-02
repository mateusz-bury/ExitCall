package com.mateuszbury.exitcall

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mateuszbury.exitcall.capability.CallCapability
import com.mateuszbury.exitcall.capability.CallCapabilityResolver
import com.mateuszbury.exitcall.model.CallConfigStore
import com.mateuszbury.exitcall.model.ScheduledCallStore
import com.mateuszbury.exitcall.scheduler.ScheduledCallScheduler
import com.mateuszbury.exitcall.ui.components.CallCapabilityDialog
import com.mateuszbury.exitcall.ui.screens.CallSetupScreen
import com.mateuszbury.exitcall.ui.screens.HomeScreen
import com.mateuszbury.exitcall.ui.screens.ScheduleCallScreen
import com.mateuszbury.exitcall.ui.theme.ExitCallTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private enum class MainScreen {
        HOME,
        CALL_SETUP,
        SCHEDULE_CALL
    }

    private lateinit var scheduledCallStore: ScheduledCallStore
    private lateinit var scheduledCallScheduler: ScheduledCallScheduler
    private lateinit var capabilityResolver: CallCapabilityResolver

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
        val missingCapability =
            capabilityResolver.firstMissingCapability(
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

    private fun canRequestNotificationRuntimePermission(): Boolean {
        return capabilityResolver.canRequestNotificationRuntimePermission(
            requestedThisSession = notificationPermissionRequestedThisSession
        )
    }

    private fun handleCapabilityAction(
        capability: CallCapability
    ) {
        if (
            capability == CallCapability.NOTIFICATIONS &&
            canRequestNotificationRuntimePermission()
        ) {
            capabilityIssue = null
            notificationPermissionRequestedThisSession = true

            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
            return
        }

        val settingsIntent = capabilityResolver.settingsIntent(capability)

        if (settingsIntent == null) {
            continuePendingCapabilityAction()
            return
        }

        openCapabilitySettings(settingsIntent)
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
                    capabilityResolver.appDetailsSettingsIntent()
                )
            }.onFailure {
                waitingForCapabilitySettings = false
                capabilityIssue =
                    capabilityResolver.firstMissingCapability(
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
        capabilityResolver = CallCapabilityResolver(
            context = this,
            scheduledCallScheduler = scheduledCallScheduler
        )

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

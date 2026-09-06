package com.mateuszbury.exitcall

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mateuszbury.exitcall.capability.CallCapability
import com.mateuszbury.exitcall.capability.CallCapabilityResolver
import com.mateuszbury.exitcall.model.AppSettings
import com.mateuszbury.exitcall.model.AppSettingsStore
import com.mateuszbury.exitcall.model.CallConfigStore
import com.mateuszbury.exitcall.model.OnboardingStore
import com.mateuszbury.exitcall.model.ScheduledCallStore
import com.mateuszbury.exitcall.scheduler.ScheduledCallScheduler
import com.mateuszbury.exitcall.ui.MainApp
import com.mateuszbury.exitcall.ui.screens.OnboardingScreen
import com.mateuszbury.exitcall.ui.theme.ExitCallTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var scheduledCallStore: ScheduledCallStore
    private lateinit var scheduledCallScheduler: ScheduledCallScheduler
    private lateinit var capabilityResolver: CallCapabilityResolver
    private lateinit var appSettingsStore: AppSettingsStore

    private var scheduledCallAtMillis by mutableStateOf<Long?>(null)
    private var capabilityIssue by mutableStateOf<CallCapability?>(null)
    private var appSettings by mutableStateOf(AppSettings())
    private var onboardingCompleted by mutableStateOf(false)
    private var onboardingDemoCompleted by mutableStateOf(false)
    private var capabilityAvailability by mutableStateOf(
        emptyMap<CallCapability, Boolean>()
    )

    private var pendingCapabilityAction: (() -> Unit)? = null
    private var pendingRequiresExactAlarm = false
    private var waitingForCapabilitySettings = false
    private var notificationPermissionRequestedThisSession = false
    private var onboardingNotificationRequestInFlight = false

    private val demoCallLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onboardingDemoCompleted = true
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            refreshCapabilityAvailability()

            if (onboardingNotificationRequestInFlight) {
                onboardingNotificationRequestInFlight = false
                notificationPermissionRequestedThisSession = true
            } else if (granted) {
                continuePendingCapabilityAction()
            } else {
                notificationPermissionRequestedThisSession = true
                capabilityIssue = CallCapability.NOTIFICATIONS
            }
        }

    private fun startDemoCall() {
        demoCallLauncher.launch(
            Intent(
                this,
                DemoCallActivity::class.java
            )
        )
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
            scheduledCallScheduler.schedule(
                triggerTimeMillis = triggerTimeMillis,
                settingsSnapshot = appSettings
            )
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

    private fun cancelScheduledCall() {
        scheduledCallScheduler.cancel()
        scheduledCallAtMillis = null
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

        refreshCapabilityAvailability()

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

    private fun requestNotificationsFromOnboarding() {
        if (
            capabilityResolver.isCapabilityAvailable(
                CallCapability.NOTIFICATIONS
            )
        ) {
            refreshCapabilityAvailability()
            return
        }

        if (canRequestNotificationRuntimePermission()) {
            onboardingNotificationRequestInFlight = true
            notificationPermissionRequestedThisSession = true
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
            return
        }

        openCapabilitySettings(CallCapability.NOTIFICATIONS)
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

        openCapabilitySettings(capability)
    }

    private fun openCapabilitySettings(
        capability: CallCapability
    ) {
        val settingsIntent = capabilityResolver.settingsIntent(capability)

        if (settingsIntent == null) {
            refreshCapabilityAvailability()
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
                refreshCapabilityAvailability()

                if (pendingCapabilityAction != null) {
                    capabilityIssue =
                        capabilityResolver.firstMissingCapability(
                            requireExactAlarm = pendingRequiresExactAlarm
                        )
                }
            }
        }
    }

    private fun refreshCapabilityAvailability() {
        if (!::capabilityResolver.isInitialized) {
            return
        }

        capabilityAvailability = CallCapability.values().associateWith(
            capabilityResolver::isCapabilityAvailable
        )
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
        val initialCallConfig = callConfigStore.load()
        val onboardingStore = OnboardingStore(this)
        onboardingCompleted = onboardingStore.isCompleted()

        appSettingsStore = AppSettingsStore(this)
        appSettings = appSettingsStore.load()

        scheduledCallStore = ScheduledCallStore(this)
        scheduledCallScheduler = ScheduledCallScheduler(
            context = this,
            scheduledCallStore = scheduledCallStore
        )
        capabilityResolver = CallCapabilityResolver(
            context = this,
            scheduledCallScheduler = scheduledCallScheduler
        )

        refreshCapabilityAvailability()
        scheduledCallScheduler.restorePersistedCall()
        scheduledCallAtMillis = scheduledCallStore.load()

        setContent {
            ExitCallTheme {
                if (!onboardingCompleted) {
                    OnboardingScreen(
                        capabilityAvailability = capabilityAvailability,
                        onRequestNotifications = {
                            requestNotificationsFromOnboarding()
                        },
                        onOpenFullScreenSettings = {
                            openCapabilitySettings(
                                CallCapability.FULL_SCREEN_INTENT
                            )
                        },
                        onComplete = {
                            onboardingStore.markCompleted()
                            onboardingCompleted = true
                        },
                        requireDemo = true,
                        demoCompleted = onboardingDemoCompleted,
                        onStartDemo = {
                            startDemoCall()
                        }
                    )
                } else {
                    MainApp(
                        initialCallConfig = initialCallConfig,
                        initialAppSettings = appSettings,
                        scheduledCallAtMillis = scheduledCallAtMillis,
                        capabilityIssue = capabilityIssue,
                        capabilityAvailability = capabilityAvailability,
                        canRequestNotificationPermission =
                            canRequestNotificationRuntimePermission(),
                        appVersionName = BuildConfig.VERSION_NAME,
                        onRequireCallCapabilities = { requireExactAlarm, onReady ->
                            ensureCallCapabilities(
                                requireExactAlarm = requireExactAlarm,
                                onReady = onReady
                            )
                        },
                        onStartImmediateCall = {
                            startImmediateCall()
                        },
                        onScheduleCall = { triggerTimeMillis ->
                            scheduleCall(triggerTimeMillis)
                        },
                        onCancelScheduledCall = {
                            cancelScheduledCall()
                        },
                        onSaveCallConfig = { newConfig ->
                            callConfigStore.save(newConfig)
                        },
                        onSaveAppSettings = { newSettings ->
                            appSettingsStore.save(newSettings)
                            appSettings = newSettings
                        },
                        onOpenCapabilitySettings = { capability ->
                            openCapabilitySettings(capability)
                        },
                        onCapabilityConfirm = { capability ->
                            handleCapabilityAction(capability)
                        },
                        onCapabilityDismiss = {
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

        refreshCapabilityAvailability()

        if (waitingForCapabilitySettings) {
            waitingForCapabilitySettings = false

            if (pendingCapabilityAction != null) {
                continuePendingCapabilityAction()
            }
        }
    }
}

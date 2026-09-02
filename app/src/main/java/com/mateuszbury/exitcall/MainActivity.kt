package com.mateuszbury.exitcall

import android.Manifest
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
import com.mateuszbury.exitcall.model.CallConfigStore
import com.mateuszbury.exitcall.model.ScheduledCallStore
import com.mateuszbury.exitcall.scheduler.ScheduledCallScheduler
import com.mateuszbury.exitcall.ui.MainApp
import com.mateuszbury.exitcall.ui.theme.ExitCallTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
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
        val initialCallConfig = callConfigStore.load()

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
                MainApp(
                    initialCallConfig = initialCallConfig,
                    scheduledCallAtMillis = scheduledCallAtMillis,
                    capabilityIssue = capabilityIssue,
                    canRequestNotificationPermission =
                        canRequestNotificationRuntimePermission(),
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
                    onSaveCallConfig = { newConfig ->
                        callConfigStore.save(newConfig)
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

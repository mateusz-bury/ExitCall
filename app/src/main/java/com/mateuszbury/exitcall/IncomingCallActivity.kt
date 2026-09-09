package com.mateuszbury.exitcall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.mateuszbury.exitcall.audio.ActiveCallAudioRouter
import com.mateuszbury.exitcall.audio.AudioRouteSnapshot
import com.mateuszbury.exitcall.model.CallConfigStore
import com.mateuszbury.exitcall.notification.CallDeliveryDiagnostics
import com.mateuszbury.exitcall.screens.CallScreen
import com.mateuszbury.exitcall.ui.screens.ActiveCallScreen
import com.mateuszbury.exitcall.ui.screens.IncomingCallScreen
import com.mateuszbury.exitcall.ui.theme.ExitCallBackground
import com.mateuszbury.exitcall.ui.theme.ExitCallTheme

class IncomingCallActivity : ComponentActivity() {

    companion object {
        const val EXTRA_START_ACTIVE = "extra_start_active"
    }

    private var currentScreen by mutableStateOf(CallScreen.INCOMING_CALL)
    private var audioRouteSnapshot by mutableStateOf(AudioRouteSnapshot())
    private var callFinishedReceiverRegistered = false

    private val activeCallAudioRouter by lazy {
        ActiveCallAudioRouter(this)
    }

    private val callFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (
                intent?.action == IncomingCallService.ACTION_CALL_FINISHED &&
                currentScreen == CallScreen.INCOMING_CALL
            ) {
                Log.d(
                    "ExitCall",
                    "IncomingCallActivity closing after incoming call ended"
                )

                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CallDeliveryDiagnostics.log(this, "activity-created")

        configureSystemBars()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        ContextCompat.registerReceiver(
            this,
            callFinishedReceiver,
            IntentFilter(IncomingCallService.ACTION_CALL_FINISHED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        callFinishedReceiverRegistered = true

        val startActive = intent.getBooleanExtra(
            EXTRA_START_ACTIVE,
            false
        )

        currentScreen = if (startActive) {
            CallScreen.ACTIVE_CALL
        } else {
            CallScreen.INCOMING_CALL
        }

        if (startActive) {
            stopIncomingCallService()
            startActiveCallAudio()
        }

        val config = CallConfigStore(this).load()

        Log.d(
            "ExitCall",
            "Loaded call config for UI: caller=${config.callerName}, vibrate=${config.vibrate}"
        )

        setContent {
            ExitCallTheme {
                BackHandler(
                    enabled = currentScreen == CallScreen.INCOMING_CALL
                ) {
                    declineIncomingCall()
                }

                when (currentScreen) {
                    CallScreen.INCOMING_CALL -> {
                        IncomingCallScreen(
                            config = config,
                            onDecline = {
                                declineIncomingCall()
                            },
                            onAnswer = {
                                stopIncomingCallService()
                                currentScreen = CallScreen.ACTIVE_CALL
                                startActiveCallAudio()
                            }
                        )
                    }

                    CallScreen.ACTIVE_CALL -> {
                        ActiveCallScreen(
                            config = config,
                            availableAudioRoutes = audioRouteSnapshot.availableRoutes,
                            selectedAudioRoute = audioRouteSnapshot.selectedRoute,
                            onAudioRouteSelected = { route ->
                                activeCallAudioRouter.selectRoute(route)
                            },
                            onHangUp = {
                                activeCallAudioRouter.stop()
                                finish()
                            }
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun configureSystemBars() {
        window.statusBarColor = ExitCallBackground.toArgb()
        window.navigationBarColor = ExitCallBackground.toArgb()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        CallDeliveryDiagnostics.log(this, "activity-new-intent")
        setIntent(intent)

        if (
            intent.getBooleanExtra(
                EXTRA_START_ACTIVE,
                false
            )
        ) {
            stopIncomingCallService()
            currentScreen = CallScreen.ACTIVE_CALL
            startActiveCallAudio()
        }
    }

    override fun onDestroy() {
        activeCallAudioRouter.stop()

        if (callFinishedReceiverRegistered) {
            unregisterReceiver(callFinishedReceiver)
            callFinishedReceiverRegistered = false
        }

        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        CallDeliveryDiagnostics.log(this, "activity-resumed")
    }

    private fun declineIncomingCall() {
        stopIncomingCallService()
        finish()
    }

    private fun startActiveCallAudio() {
        activeCallAudioRouter.start { snapshot ->
            audioRouteSnapshot = snapshot
        }
    }

    private fun stopIncomingCallService() {
        stopService(
            Intent(
                this,
                IncomingCallService::class.java
            )
        )
    }
}

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
import androidx.core.content.ContextCompat
import com.mateuszbury.exitcall.model.CallConfigStore
import com.mateuszbury.exitcall.screens.CallScreen
import com.mateuszbury.exitcall.ui.screens.ActiveCallScreen
import com.mateuszbury.exitcall.ui.screens.IncomingCallScreen
import com.mateuszbury.exitcall.ui.theme.ExitCallTheme

class IncomingCallActivity : ComponentActivity() {

    companion object {
        const val EXTRA_START_ACTIVE = "extra_start_active"
    }

    private var currentScreen by mutableStateOf(CallScreen.INCOMING_CALL)
    private var callFinishedReceiverRegistered = false

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
                            }
                        )
                    }

                    CallScreen.ACTIVE_CALL -> {
                        ActiveCallScreen(
                            config = config,
                            onHangUp = {
                                finish()
                            }
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (
            intent.getBooleanExtra(
                EXTRA_START_ACTIVE,
                false
            )
        ) {
            stopIncomingCallService()
            currentScreen = CallScreen.ACTIVE_CALL
        }
    }

    override fun onDestroy() {
        if (callFinishedReceiverRegistered) {
            unregisterReceiver(callFinishedReceiver)
            callFinishedReceiverRegistered = false
        }

        super.onDestroy()
    }

    private fun declineIncomingCall() {
        stopIncomingCallService()
        finish()
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

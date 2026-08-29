package com.example.exitcall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.exitcall.model.CallConfig
import com.example.exitcall.screens.CallScreen
import com.example.exitcall.ui.screens.ActiveCallScreen
import com.example.exitcall.ui.screens.IncomingCallScreen
import com.example.exitcall.ui.theme.ExitCallTheme

class IncomingCallActivity : ComponentActivity() {

    companion object {
        const val EXTRA_START_ACTIVE = "extra_start_active"
    }

    private var currentScreen by mutableStateOf(CallScreen.INCOMING_CALL)
    private var timeoutReceiverRegistered = false

    private val timeoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (
                intent?.action == IncomingCallService.ACTION_TIMEOUT &&
                currentScreen == CallScreen.INCOMING_CALL
            ) {
                Log.d(
                    "ExitCall",
                    "IncomingCallActivity closing after unanswered timeout"
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
            timeoutReceiver,
            IntentFilter(IncomingCallService.ACTION_TIMEOUT),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        timeoutReceiverRegistered = true

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

        setContent {
            ExitCallTheme {
                val config = CallConfig()

                when (currentScreen) {
                    CallScreen.INCOMING_CALL -> {
                        IncomingCallScreen(
                            config = config,
                            onDecline = {
                                stopIncomingCallService()
                                finish()
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
        if (timeoutReceiverRegistered) {
            unregisterReceiver(timeoutReceiver)
            timeoutReceiverRegistered = false
        }

        super.onDestroy()
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

package com.example.exitcall

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.exitcall.model.CallConfig
import com.example.exitcall.screens.AppScreen
import com.example.exitcall.ui.screens.ActiveCallScreen
import com.example.exitcall.ui.screens.IncomingCallScreen
import com.example.exitcall.ui.theme.ExitCallTheme

class IncomingCallActivity : ComponentActivity() {

    companion object {
        const val EXTRA_START_ACTIVE = "extra_start_active"
    }

    private var currentScreen by mutableStateOf(AppScreen.INCOMING_CALL)

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

        val startActive = intent.getBooleanExtra(
            EXTRA_START_ACTIVE,
            false
        )

        currentScreen = if (startActive) {
            AppScreen.ACTIVE_CALL
        } else {
            AppScreen.INCOMING_CALL
        }

        if (startActive) {
            stopIncomingCallService()
        }

        setContent {
            ExitCallTheme {
                val config = CallConfig()

                when (currentScreen) {
                    AppScreen.INCOMING_CALL -> {
                        IncomingCallScreen(
                            config = config,
                            onDecline = {
                                stopIncomingCallService()
                                finish()
                            },
                            onAnswer = {
                                stopIncomingCallService()
                                currentScreen = AppScreen.ACTIVE_CALL
                            }
                        )
                    }

                    AppScreen.ACTIVE_CALL -> {
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
            currentScreen = AppScreen.ACTIVE_CALL
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

package com.example.exitcall

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.exitcall.model.CallConfig
import com.example.exitcall.screens.AppScreen
import com.example.exitcall.ui.screens.IncomingCallScreen
import com.example.exitcall.ui.theme.ExitCallTheme
import androidx.compose.runtime.getValue
import com.example.exitcall.ui.screens.ActiveCallScreen

class IncomingCallActivity : ComponentActivity() {

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

        setContent {

            ExitCallTheme {

                val config = CallConfig()
                var currentScreen by remember {
                    mutableStateOf(AppScreen.INCOMING_CALL)
                }


                when(currentScreen){

                AppScreen.INCOMING_CALL -> {


                IncomingCallScreen(
                    config = config,

                    onDecline = {
                        stopService(
                            Intent(
                                this,
                                IncomingCallService::class.java
                            )
                        )

                        finish()
                    },

                    onAnswer = {
                        stopService(
                            Intent(
                                this,
                                IncomingCallService::class.java
                            )
                        )

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
}
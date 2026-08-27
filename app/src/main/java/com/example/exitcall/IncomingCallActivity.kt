package com.example.exitcall

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.exitcall.model.CallConfig
import com.example.exitcall.ui.screens.IncomingCallScreen
import com.example.exitcall.ui.theme.ExitCallTheme

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

                        finish()
                    }
                )
            }
        }
    }
}
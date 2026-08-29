package com.example.exitcall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.exitcall.model.CallConfig
import com.example.exitcall.ui.screens.CallSetupScreen
import com.example.exitcall.ui.screens.HomeScreen
import com.example.exitcall.ui.theme.ExitCallTheme

class MainActivity : ComponentActivity() {

    private enum class MainScreen {
        HOME,
        CALL_SETUP
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

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    3001
                )
            }
        }

        enableEdgeToEdge()

        setContent {
            ExitCallTheme {
                var currentScreen by remember {
                    mutableStateOf(MainScreen.HOME)
                }

                var callConfig by remember {
                    mutableStateOf(CallConfig())
                }

                BackHandler(
                    enabled = currentScreen == MainScreen.CALL_SETUP
                ) {
                    currentScreen = MainScreen.HOME
                }

                when (currentScreen) {
                    MainScreen.HOME -> {
                        HomeScreen(
                            onCallMeNow = {
                                startImmediateCall()
                            },
                            onScheduleCall = {
                                // TODO scheduler
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
                                callConfig = newConfig
                                currentScreen = MainScreen.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}

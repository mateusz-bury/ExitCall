package com.example.exitcall

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import com.example.exitcall.ui.screens.ScheduleCallScreen
import com.example.exitcall.ui.theme.ExitCallTheme
import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import com.example.exitcall.model.CallConfigStore

class MainActivity : ComponentActivity() {
    private enum class MainScreen {
        HOME,
        CALL_SETUP,
        SCHEDULE_CALL
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

        val callConfigStore = CallConfigStore(this)

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
                            onCallMeNow = {
                                startImmediateCall()
                            },
                            onScheduleCall = {
                                currentScreen = MainScreen.SCHEDULE_CALL
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
                            onBack = {
                                currentScreen = MainScreen.HOME
                            },
                            onSchedule = { triggerTimeMillis ->

                                Log.d(
                                    "ExitCall",
                                    "Selected schedule time: $triggerTimeMillis"
                                )

                                currentScreen = MainScreen.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}

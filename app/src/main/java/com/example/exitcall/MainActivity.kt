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

    /**
     * DEBUG ONLY.
     * Temporary manual trigger for lock-screen testing until the real scheduler (#4) exists.
     * Unlike the old prototype, this is never called automatically on app startup.
     */
    private fun scheduleDebugCall() {
        val alarmManager =
            getSystemService(ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(
            this,
            CallAlarmReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            1001,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 5_000

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.e("ExitCall", "Exact alarm permission NOT granted")

            startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )

        Log.d("ExitCall", "DEBUG call scheduled for +5 seconds")
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
                                // Temporary DEBUG trigger until the real scheduler (#4) lands.
                                scheduleDebugCall()
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

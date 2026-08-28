package com.example.exitcall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.exitcall.screens.AppScreen
import com.example.exitcall.ui.screens.ActiveCallScreen
import com.example.exitcall.ui.screens.CallSetupScreen
import com.example.exitcall.ui.screens.HomeScreen
import com.example.exitcall.ui.screens.IncomingCallScreen
import com.example.exitcall.ui.theme.ExitCallTheme
import com.example.exitcall.model.CallConfig
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.net.Uri
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    fun scheduleCall(context: Context) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, CallAlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 10_000

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            val canSchedule = alarmManager.canScheduleExactAlarms()

            Log.d("ExitCall", "Can schedule exact alarms: $canSchedule")

            if (canSchedule) {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )

                Log.d("ExitCall", "Scheduled call set for +60 seconds")

            } else {

                Log.e("ExitCall", "Exact alarm permission NOT granted")

                val permissionIntent =
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }

                context.startActivity(permissionIntent)
            }

        } else {

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )

            Log.d("ExitCall", "Scheduled call set for +60 seconds")
        }
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

        scheduleCall(this)

        enableEdgeToEdge()

        setContent {

            ExitCallTheme {
                var currentScreen by remember {
                    mutableStateOf(AppScreen.HOME)
                }
                var callConfig by remember {
                    mutableStateOf(CallConfig())
                }
                BackHandler(
                    enabled = currentScreen != AppScreen.HOME
                ) {
                    when (currentScreen) {

                        AppScreen.CALL_SETUP -> {
                            currentScreen = AppScreen.HOME
                        }

                        AppScreen.INCOMING_CALL,
                        AppScreen.ACTIVE_CALL -> {
                        }

                        else -> Unit
                    }
                }

                when (currentScreen) {
                    AppScreen.HOME -> {

                        HomeScreen(

                            onCallMeNow = {
                                currentScreen = AppScreen.INCOMING_CALL
                            },

                            onScheduleCall = {
                                // TODO scheduler
                            },

                            onCallSetup = {
                                currentScreen = AppScreen.CALL_SETUP
                            },

                            onSettings = {
                                // TODO settings
                            }
                        )
                    }

                    AppScreen.INCOMING_CALL -> {

                        IncomingCallScreen(
                            config = callConfig,
                            onDecline = {
                                currentScreen = AppScreen.HOME
                            },

                            onAnswer = {
                                currentScreen = AppScreen.ACTIVE_CALL
                            }
                        )
                    }

                    AppScreen.ACTIVE_CALL -> {

                        ActiveCallScreen(
                            config = callConfig,
                            onHangUp = {
                                finish()
                            }
                        )

                    }

                    AppScreen.CALL_SETUP -> {
                        CallSetupScreen(
                            initialConfig = callConfig,
                            onChooseContact = {
                                // zaraz podepniemy picker kontaktu
                            },
                            onChooseRingtone = {
                                // zaraz podepniemy systemowy ringtone picker
                            },
                            onSave = { newConfig ->
                                callConfig = newConfig
                                currentScreen = AppScreen.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}
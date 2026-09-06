package com.mateuszbury.exitcall.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.mateuszbury.exitcall.audio.DemoVoicePlayer
import com.mateuszbury.exitcall.audio.IncomingCallAlertPlayer
import com.mateuszbury.exitcall.model.AppSettings
import com.mateuszbury.exitcall.model.CallConfig
import com.mateuszbury.exitcall.screens.CallScreen
import kotlinx.coroutines.delay

private const val DEMO_VOICE_START_DELAY_MS = 1_000L

@Composable
fun DemoCallFlow(
    onCompleted: () -> Unit,
    onCancelled: () -> Unit
) {
    var currentScreen by remember {
        mutableStateOf(CallScreen.INCOMING_CALL)
    }

    val context = LocalContext.current
    val alertPlayer = remember(context) {
        IncomingCallAlertPlayer(context)
    }
    val voicePlayer = remember(context) {
        DemoVoicePlayer(context)
    }
    val demoConfig = remember {
        CallConfig(
            callerName = "ExitCall",
            phoneNumber = "",
            vibrate = true
        )
    }
    val demoSettings = remember {
        AppSettings()
    }

    DisposableEffect(alertPlayer, voicePlayer) {
        onDispose {
            alertPlayer.stop()
            voicePlayer.stop()
        }
    }

    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            CallScreen.INCOMING_CALL -> {
                voicePlayer.stop()
                alertPlayer.start(
                    ringtoneUri = demoSettings.ringtoneUri,
                    vibrate = demoSettings.vibrate
                )
            }

            CallScreen.ACTIVE_CALL -> {
                alertPlayer.stop()
                delay(DEMO_VOICE_START_DELAY_MS)
                voicePlayer.play()
            }
        }
    }

    BackHandler(onBack = onCancelled)

    when (currentScreen) {
        CallScreen.INCOMING_CALL -> {
            IncomingCallScreen(
                config = demoConfig,
                onDecline = onCancelled,
                onAnswer = {
                    currentScreen = CallScreen.ACTIVE_CALL
                }
            )
        }

        CallScreen.ACTIVE_CALL -> {
            ActiveCallScreen(
                config = demoConfig,
                onHangUp = onCompleted
            )
        }
    }
}

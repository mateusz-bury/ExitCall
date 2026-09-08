package com.mateuszbury.exitcall.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.mateuszbury.exitcall.audio.ActiveCallAudioRouter
import com.mateuszbury.exitcall.audio.AudioRouteSnapshot
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
    val audioRouter = remember(context) {
        ActiveCallAudioRouter(context)
    }
    var audioRouteSnapshot by remember {
        mutableStateOf(AudioRouteSnapshot())
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

    var demoEnded by remember { mutableStateOf(false) }
    val latestOnCompleted by rememberUpdatedState(onCompleted)
    val latestOnCancelled by rememberUpdatedState(onCancelled)
    val stopAudio = remember(alertPlayer, voicePlayer, audioRouter) {
        {
            alertPlayer.stop()
            voicePlayer.stop()
            audioRouter.stop()
        }
    }
    val finishDemo: (Boolean) -> Unit = { completed ->
        if (!demoEnded) {
            demoEnded = true
            stopAudio()
            if (completed) latestOnCompleted() else latestOnCancelled()
        }
    }
    val latestFinishDemo by rememberUpdatedState(finishDemo)
    val lifecycle = LocalView.current.findViewTreeLifecycleOwner()?.lifecycle

    DisposableEffect(lifecycle, stopAudio) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // A tutorial/demo must not keep ringing or start voice in the background.
                latestFinishDemo(false)
            }
        }
        lifecycle?.addObserver(observer)
        onDispose {
            lifecycle?.removeObserver(observer)
            stopAudio()
        }
    }

    LaunchedEffect(currentScreen, demoEnded) {
        if (demoEnded) return@LaunchedEffect
        when (currentScreen) {
            CallScreen.INCOMING_CALL -> {
                voicePlayer.stop()
                audioRouter.stop()
                alertPlayer.start(
                    ringtoneUri = demoSettings.ringtoneUri,
                    vibrate = demoSettings.vibrate
                )
                delay(demoSettings.ringDurationSeconds * 1_000L)
                // Answer can arrive before recomposition cancels this effect.
                if (currentScreen == CallScreen.INCOMING_CALL && !demoEnded) {
                    latestFinishDemo(false)
                }
            }

            CallScreen.ACTIVE_CALL -> {
                alertPlayer.stop()
                audioRouter.start { snapshot ->
                    audioRouteSnapshot = snapshot
                }
                audioRouter.selectDefaultRoute()
                delay(DEMO_VOICE_START_DELAY_MS)
                if (!demoEnded) voicePlayer.play()
            }
        }
    }

    BackHandler { finishDemo(false) }

    when (currentScreen) {
        CallScreen.INCOMING_CALL -> {
            IncomingCallScreen(
                config = demoConfig,
                onDecline = { finishDemo(false) },
                onAnswer = {
                    if (!demoEnded) currentScreen = CallScreen.ACTIVE_CALL
                }
            )
        }

        CallScreen.ACTIVE_CALL -> {
            ActiveCallScreen(
                config = demoConfig,
                availableAudioRoutes = audioRouteSnapshot.availableRoutes,
                selectedAudioRoute = audioRouteSnapshot.selectedRoute,
                onAudioRouteSelected = audioRouter::selectRoute,
                onMuteChanged = voicePlayer::setMuted,
                onHangUp = { finishDemo(true) }
            )
        }
    }
}

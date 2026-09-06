package com.mateuszbury.exitcall

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mateuszbury.exitcall.audio.DemoVoicePlayer
import com.mateuszbury.exitcall.model.CallConfig
import com.mateuszbury.exitcall.screens.CallScreen
import com.mateuszbury.exitcall.ui.screens.ActiveCallScreen
import com.mateuszbury.exitcall.ui.screens.IncomingCallScreen
import com.mateuszbury.exitcall.ui.theme.ExitCallTheme

class DemoCallActivity : ComponentActivity() {

    private val demoConfig = CallConfig(
        callerName = "ExitCall",
        phoneNumber = "",
        vibrate = false
    )

    private var currentScreen by mutableStateOf(CallScreen.INCOMING_CALL)
    private lateinit var demoVoicePlayer: DemoVoicePlayer
    private val voiceHandler = Handler(Looper.getMainLooper())

    private val playVoiceRunnable = Runnable {
        demoVoicePlayer.play()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        demoVoicePlayer = DemoVoicePlayer(this)
        setResult(Activity.RESULT_CANCELED)

        setContent {
            ExitCallTheme {
                BackHandler {
                    when (currentScreen) {
                        CallScreen.INCOMING_CALL -> cancelDemo()
                        CallScreen.ACTIVE_CALL -> finishDemo()
                        else -> cancelDemo()
                    }
                }

                when (currentScreen) {
                    CallScreen.INCOMING_CALL -> {
                        IncomingCallScreen(
                            config = demoConfig,
                            onDecline = {
                                cancelDemo()
                            },
                            onAnswer = {
                                currentScreen = CallScreen.ACTIVE_CALL
                                voiceHandler.removeCallbacks(playVoiceRunnable)
                                voiceHandler.postDelayed(
                                    playVoiceRunnable,
                                    VOICE_START_DELAY_MS
                                )
                            }
                        )
                    }

                    CallScreen.ACTIVE_CALL -> {
                        ActiveCallScreen(
                            config = demoConfig,
                            onHangUp = {
                                finishDemo()
                            }
                        )
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun onDestroy() {
        voiceHandler.removeCallbacks(playVoiceRunnable)
        demoVoicePlayer.stop()
        super.onDestroy()
    }

    private fun cancelDemo() {
        voiceHandler.removeCallbacks(playVoiceRunnable)
        demoVoicePlayer.stop()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun finishDemo() {
        voiceHandler.removeCallbacks(playVoiceRunnable)
        demoVoicePlayer.stop()
        setResult(Activity.RESULT_OK)
        finish()
    }

    companion object {
        private const val VOICE_START_DELAY_MS = 1_000L
    }
}

package com.mateuszbury.exitcall

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.mateuszbury.exitcall.capability.CallCapability
import com.mateuszbury.exitcall.ui.screens.OnboardingScreen
import com.mateuszbury.exitcall.ui.theme.ExitCallTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BackgroundOnboardingGateTest {
    @get:Rule val compose = createComposeRule()

    @Test fun backgroundDeniedBlocksAndGrantUnlocksDemo() {
        val allowed = mutableStateOf(false)
        var completed = 0
        compose.setContent {
            ExitCallTheme {
                OnboardingScreen(
                    capabilityAvailability = mapOf(
                        CallCapability.NOTIFICATIONS to true,
                        CallCapability.BACKGROUND_EXECUTION to allowed.value,
                        CallCapability.FULL_SCREEN_INTENT to false
                    ),
                    onRequestNotifications = {},
                    onOpenFullScreenSettings = {},
                    onOpenBackgroundSettings = {},
                    onComplete = { completed++ }
                )
            }
        }
        repeat(2) {
            compose.onNodeWithText("Continue").performClick()
            compose.waitForIdle()
        }
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.onNodeWithText("Make sure ExitCall can reach you").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.runOnIdle { allowed.value = true }
        repeat(2) {
            compose.onNodeWithText("Continue").performClick()
            compose.waitForIdle()
        }
        compose.onNodeWithText("Start demo call").assertIsEnabled()
        compose.runOnIdle { allowed.value = false }
        compose.waitForIdle()
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals(0, completed)
            allowed.value = true
        }
        repeat(2) {
            compose.onNodeWithText("Continue").performClick()
            compose.waitForIdle()
        }
        compose.onNodeWithText("Start demo call").performClick()
        compose.runOnIdle { assertEquals(1, completed) }
    }

    @Test fun tutorialReplayRemainsNonBlockingWithBackgroundDenied() {
        var completed = 0
        compose.setContent {
            ExitCallTheme {
                OnboardingScreen(
                    capabilityAvailability = emptyMap(),
                    launchesDemoOnComplete = false,
                    onRequestNotifications = {},
                    onOpenFullScreenSettings = {},
                    onOpenBackgroundSettings = {},
                    onComplete = { completed++ }
                )
            }
        }
        repeat(4) {
            compose.onNodeWithText("Continue").performClick()
            compose.waitForIdle()
        }
        compose.onNodeWithText("Finish").performClick()
        compose.runOnIdle { assertEquals(1, completed) }
    }
}

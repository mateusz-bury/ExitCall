package com.mateuszbury.exitcall

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mateuszbury.exitcall.capability.CallCapability
import com.mateuszbury.exitcall.ui.screens.OnboardingScreen
import com.mateuszbury.exitcall.ui.theme.ExitCallTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OnboardingGateTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun notificationsDenied_blocksProgressAndDemo() {
        var completed = 0
        compose.setContent {
            ExitCallTheme {
                OnboardingScreen(
                    capabilityAvailability = mapOf(CallCapability.NOTIFICATIONS to false),
                    onRequestNotifications = {},
                    onOpenFullScreenSettings = {},
                    onComplete = { completed++ }
                )
            }
        }
        repeat(2) {
            compose.onNodeWithText("Continue").performClick()
            compose.waitForIdle()
        }
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(0, completed) }
    }

    @Test
    fun notificationRevocation_blocksDemoUntilPermissionReturns() {
        val notifications = mutableStateOf(true)
        var completed = 0
        compose.setContent {
            ExitCallTheme {
                OnboardingScreen(
                    capabilityAvailability = mapOf(
                        CallCapability.NOTIFICATIONS to notifications.value
                    ),
                    onRequestNotifications = {},
                    onOpenFullScreenSettings = {},
                    onComplete = { completed++ }
                )
            }
        }
        repeat(4) {
            compose.onNodeWithText("Continue").performClick()
            compose.waitForIdle()
        }
        compose.onNodeWithText("Start demo call").assertIsEnabled()
        compose.runOnIdle { notifications.value = false }
        compose.waitForIdle()
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.runOnIdle {
            assertEquals(0, completed)
            notifications.value = true
        }
        repeat(2) {
            compose.onNodeWithText("Continue").performClick()
            compose.waitForIdle()
        }
        compose.onNodeWithText("Start demo call").performClick()
        compose.runOnIdle { assertEquals(1, completed) }
    }
}

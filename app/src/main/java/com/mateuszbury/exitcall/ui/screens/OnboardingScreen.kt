package com.mateuszbury.exitcall.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mateuszbury.exitcall.R
import com.mateuszbury.exitcall.capability.CallCapability

private const val PAGE_COUNT = 5
private const val PERMISSIONS_PAGE = 2

@Composable
fun OnboardingScreen(
    capabilityAvailability: Map<CallCapability, Boolean>,
    onRequestNotifications: () -> Unit,
    onOpenFullScreenSettings: () -> Unit,
    onComplete: () -> Unit
) {
    var page by remember { mutableIntStateOf(0) }
    val notificationsEnabled =
        capabilityAvailability[CallCapability.NOTIFICATIONS] == true

    BackHandler(enabled = page > 0) {
        page--
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.onboarding_brand),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (page) {
                0 -> WelcomePage()
                1 -> EcMarkerPage()
                2 -> CapabilityPage(
                    capabilityAvailability = capabilityAvailability,
                    onRequestNotifications = onRequestNotifications,
                    onOpenFullScreenSettings = onOpenFullScreenSettings
                )
                3 -> TrialPage()
                else -> ReadyPage()
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        ProgressDots(currentPage = page)
        Spacer(modifier = Modifier.height(20.dp))

        val lastPage = page == PAGE_COUNT - 1
        val canContinue = page != PERMISSIONS_PAGE || notificationsEnabled

        Button(
            onClick = {
                if (lastPage) onComplete() else page++
            },
            enabled = canContinue,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (lastPage) {
                    stringResource(R.string.onboarding_get_started)
                } else {
                    stringResource(R.string.onboarding_continue)
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (page == PERMISSIONS_PAGE && !notificationsEnabled) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.onboarding_notifications_blocking_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
private fun WelcomePage() {
    PageScaffold(
        title = stringResource(R.string.onboarding_welcome_title),
        body = stringResource(R.string.onboarding_welcome_body)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(132.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "EC",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        FeatureLine(stringResource(R.string.onboarding_welcome_feature_schedule))
        FeatureLine(stringResource(R.string.onboarding_welcome_feature_call))
        FeatureLine(stringResource(R.string.onboarding_welcome_feature_safety))
    }
}

@Composable
private fun EcMarkerPage() {
    PageScaffold(
        title = stringResource(R.string.onboarding_marker_title),
        body = stringResource(R.string.onboarding_marker_body)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
            ) {
                Text(
                    text = "EC",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(82.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "E",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.onboarding_marker_demo_caller),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.onboarding_marker_incoming),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        RuleCard(
            title = stringResource(R.string.onboarding_marker_rule_exitcall),
            body = stringResource(R.string.onboarding_marker_rule_exitcall_body),
            emphasized = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        RuleCard(
            title = stringResource(R.string.onboarding_marker_rule_real),
            body = stringResource(R.string.onboarding_marker_rule_real_body),
            emphasized = false
        )
    }
}

@Composable
private fun CapabilityPage(
    capabilityAvailability: Map<CallCapability, Boolean>,
    onRequestNotifications: () -> Unit,
    onOpenFullScreenSettings: () -> Unit
) {
    val notificationsEnabled =
        capabilityAvailability[CallCapability.NOTIFICATIONS] == true
    val fullScreenEnabled =
        capabilityAvailability[CallCapability.FULL_SCREEN_INTENT] == true
    val exactAlarmEnabled =
        capabilityAvailability[CallCapability.EXACT_ALARM] == true

    PageScaffold(
        title = stringResource(R.string.onboarding_permissions_title),
        body = stringResource(R.string.onboarding_permissions_body)
    ) {
        CapabilityCard(
            title = stringResource(R.string.onboarding_notifications_title),
            description = stringResource(R.string.onboarding_notifications_body),
            status = if (notificationsEnabled) {
                stringResource(R.string.onboarding_enabled)
            } else {
                stringResource(R.string.onboarding_required)
            },
            enabled = notificationsEnabled,
            actionLabel = if (notificationsEnabled) null else {
                stringResource(R.string.onboarding_enable)
            },
            onAction = onRequestNotifications
        )

        Spacer(modifier = Modifier.height(12.dp))

        CapabilityCard(
            title = stringResource(R.string.onboarding_fsi_title),
            description = stringResource(R.string.onboarding_fsi_body),
            status = if (fullScreenEnabled) {
                stringResource(R.string.onboarding_enabled)
            } else {
                stringResource(R.string.onboarding_recommended)
            },
            enabled = fullScreenEnabled,
            actionLabel = if (fullScreenEnabled) null else {
                stringResource(R.string.onboarding_open_settings)
            },
            onAction = onOpenFullScreenSettings
        )

        Spacer(modifier = Modifier.height(12.dp))

        CapabilityCard(
            title = stringResource(R.string.onboarding_exact_alarm_title),
            description = stringResource(R.string.onboarding_exact_alarm_body),
            status = if (exactAlarmEnabled) {
                stringResource(R.string.onboarding_enabled)
            } else {
                stringResource(R.string.onboarding_requested_later)
            },
            enabled = exactAlarmEnabled,
            actionLabel = null,
            onAction = {}
        )
    }
}

@Composable
private fun TrialPage() {
    PageScaffold(
        title = stringResource(R.string.onboarding_trial_title),
        body = stringResource(R.string.onboarding_trial_body)
    ) {
        TrialCard(
            value = "1",
            title = stringResource(R.string.onboarding_trial_demo_title),
            body = stringResource(R.string.onboarding_trial_demo_body)
        )
        Spacer(modifier = Modifier.height(12.dp))
        TrialCard(
            value = "2",
            title = stringResource(R.string.onboarding_trial_free_title),
            body = stringResource(R.string.onboarding_trial_free_body)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.onboarding_trial_rules),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReadyPage() {
    PageScaffold(
        title = stringResource(R.string.onboarding_ready_title),
        body = stringResource(R.string.onboarding_ready_body)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            modifier = Modifier.size(126.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "✓",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.onboarding_ready_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PageScaffold(
    title: String,
    body: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 31.sp,
            lineHeight = 37.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            lineHeight = 23.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(30.dp))
        content()
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun FeatureLine(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusBubble(text = "✓")
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun RuleCard(
    title: String,
    body: String,
    emphasized: Boolean
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (emphasized) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = if (emphasized) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun CapabilityCard(
    title: String,
    description: String,
    status: String,
    enabled: Boolean,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = status,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (actionLabel != null) {
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = onAction,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun TrialCard(
    value: String,
    title: String,
    body: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBubble(text = value)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun StatusBubble(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
        modifier = Modifier.size(42.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProgressDots(currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(PAGE_COUNT) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (index == currentPage) 10.dp else 7.dp)
                    .background(
                        color = if (index == currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

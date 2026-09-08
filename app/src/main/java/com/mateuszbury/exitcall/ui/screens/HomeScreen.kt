package com.mateuszbury.exitcall.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mateuszbury.exitcall.R
import com.mateuszbury.exitcall.capability.CallCapability
import com.mateuszbury.exitcall.model.AppSettings
import com.mateuszbury.exitcall.model.CallConfig
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    callConfig: CallConfig,
    appSettings: AppSettings,
    scheduledCallAtMillis: Long?,
    capabilityAvailability: Map<CallCapability, Boolean>,
    onCallMeNow: () -> Unit,
    onScheduleCall: () -> Unit,
    onCallSetup: () -> Unit,
    onSettings: () -> Unit,
    onOpenCapabilitySettings: (CallCapability) -> Unit
) {
    val nextCallText = scheduledCallAtMillis?.let { triggerTimeMillis ->
        SimpleDateFormat("MMM d, HH:mm", Locale.ENGLISH)
            .format(triggerTimeMillis)
    } ?: stringResource(R.string.home_not_scheduled)

    val notificationsReady =
        capabilityAvailability[CallCapability.NOTIFICATIONS] == true
    val exactAlarmReady =
        capabilityAvailability[CallCapability.EXACT_ALARM] == true
    val fullScreenReady =
        capabilityAvailability[CallCapability.FULL_SCREEN_INTENT] == true

    val readinessCapability = when {
        !notificationsReady -> CallCapability.NOTIFICATIONS
        scheduledCallAtMillis != null && !exactAlarmReady ->
            CallCapability.EXACT_ALARM
        !fullScreenReady -> CallCapability.FULL_SCREEN_INTENT
        else -> null
    }
    val readinessTitle = if (
        !notificationsReady ||
        (scheduledCallAtMillis != null && !exactAlarmReady)
    ) {
        stringResource(R.string.home_status_action_required)
    } else {
        stringResource(R.string.home_status_ready)
    }
    val readinessDetail = when {
        !notificationsReady ->
            stringResource(R.string.home_status_notifications_required)
        scheduledCallAtMillis != null && !exactAlarmReady ->
            stringResource(R.string.home_status_exact_alarm_required)
        !fullScreenReady ->
            stringResource(R.string.home_status_notification_fallback)
        else -> stringResource(R.string.home_status_all_ready)
    }
    val readinessActionLabel = when {
        !notificationsReady ||
            (scheduledCallAtMillis != null && !exactAlarmReady) ->
            stringResource(R.string.home_status_manage)
        !fullScreenReady -> stringResource(R.string.home_status_improve)
        else -> null
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 16.dp,
            end = 20.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.home_title),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp
                    )
                    Text(
                        text = stringResource(R.string.home_tagline),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = onSettings) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = stringResource(
                            R.string.home_settings_content_description
                        ),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = stringResource(R.string.home_next_call),
                    value = nextCallText,
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    title = stringResource(R.string.home_caller),
                    value = callConfig.callerName.ifBlank {
                        stringResource(R.string.app_name)
                    },
                    supporting = callConfig.phoneNumber.ifBlank {
                        stringResource(R.string.home_no_phone_number)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            DashboardCard(
                title = stringResource(R.string.home_sound),
                value = appSettings.ringtoneName,
                supporting = stringResource(
                    if (appSettings.vibrate) {
                        R.string.home_vibrate_on
                    } else {
                        R.string.home_vibrate_off
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            ReadinessCard(
                title = readinessTitle,
                detail = readinessDetail,
                actionLabel = readinessActionLabel,
                onAction = readinessCapability?.let { capability ->
                    { onOpenCapabilitySettings(capability) }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_need_an_exit),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 23.sp
            )
        }

        item {
            Button(
                onClick = onCallMeNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.home_start_call),
                    fontSize = 18.sp
                )
            }
        }

        item {
            OutlinedButton(
                onClick = onScheduleCall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(
                        if (scheduledCallAtMillis == null) {
                            R.string.home_schedule_call
                        } else {
                            R.string.home_edit_scheduled_call
                        }
                    ),
                    fontSize = 18.sp
                )
            }
        }

        item {
            OutlinedButton(
                onClick = onCallSetup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.home_set_up_call),
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: String? = null
) {
    DashboardSurface(modifier = modifier.heightIn(min = 106.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        supporting?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReadinessCard(
    title: String,
    detail: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    DashboardSurface(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_status),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = detail,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun DashboardSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp),
        content = content
    )
}

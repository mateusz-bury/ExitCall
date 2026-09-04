package com.mateuszbury.exitcall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mateuszbury.exitcall.R
import com.mateuszbury.exitcall.model.CallConfig
import com.mateuszbury.exitcall.phone.PhoneNumberFormatter
import com.mateuszbury.exitcall.ui.theme.ExitCallActiveBackground
import com.mateuszbury.exitcall.ui.theme.ExitCallCallActionSurface
import com.mateuszbury.exitcall.ui.theme.ExitCallCallControlSurface
import com.mateuszbury.exitcall.ui.theme.ExitCallHangUp
import kotlinx.coroutines.delay

@Composable
fun ActiveCallScreen(
    config: CallConfig,
    onHangUp: () -> Unit
) {
    var elapsedSeconds by remember {
        mutableStateOf(0)
    }
    var keypadVisible by remember {
        mutableStateOf(false)
    }
    var moreVisible by remember {
        mutableStateOf(false)
    }
    var muteEnabled by remember {
        mutableStateOf(false)
    }
    var speakerEnabled by remember {
        mutableStateOf(false)
    }
    var keypadInput by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val callTime = "%02d:%02d".format(minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ExitCallActiveBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text = callTime,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = config.callerName.ifBlank { "Unknown" },
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 40.sp,
                lineHeight = 48.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (config.phoneNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Mobile  ${PhoneNumberFormatter.formatForDisplay(config.phoneNumber)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            Box(
                modifier = Modifier
                    .size(156.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = config.callerName
                        .firstOrNull()
                        ?.uppercase()
                        ?: "?",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 68.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 32.dp,
                        topEnd = 32.dp
                    )
                )
                .background(ExitCallCallControlSurface)
                .navigationBarsPadding()
                .padding(
                    start = 18.dp,
                    top = 24.dp,
                    end = 18.dp,
                    bottom = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                keypadVisible -> {
                    KeypadPanel(
                        input = keypadInput,
                        onDigit = { digit ->
                            keypadInput += digit
                        },
                        onDelete = {
                            keypadInput = keypadInput.dropLast(1)
                        },
                        onClose = {
                            keypadVisible = false
                        }
                    )
                }

                moreVisible -> {
                    MorePanel(
                        onClose = {
                            moreVisible = false
                        }
                    )
                }

                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CallActionItem(
                            label = "Keypad",
                            iconRes = R.drawable.ic_dialpad,
                            selected = false,
                            onClick = {
                                keypadVisible = true
                                moreVisible = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CallActionItem(
                            label = "Mute",
                            iconRes = if (muteEnabled) {
                                R.drawable.ic_mic_off
                            } else {
                                R.drawable.ic_mic
                            },
                            selected = muteEnabled,
                            onClick = {
                                muteEnabled = !muteEnabled
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CallActionItem(
                            label = "Speaker",
                            iconRes = R.drawable.ic_volume_up,
                            selected = speakerEnabled,
                            onClick = {
                                speakerEnabled = !speakerEnabled
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CallActionItem(
                            label = "More",
                            iconRes = R.drawable.ic_more_vert,
                            selected = false,
                            onClick = {
                                moreVisible = true
                                keypadVisible = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onHangUp,
                shape = RoundedCornerShape(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExitCallHangUp,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .width(190.dp)
                    .height(76.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_call),
                    contentDescription = "Hang up",
                    modifier = Modifier
                        .size(30.dp)
                        .rotate(135f)
                )
            }
        }

        Text(
            text = "EC",
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(
                    top = 12.dp,
                    end = 16.dp
                )
        )
    }
}

@Composable
private fun KeypadPanel(
    input: String,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("*", "0", "#")
    )

    Text(
        text = input.ifBlank { "Keypad" },
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = if (input.isBlank()) 18.sp else 28.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(18.dp))

    keys.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row.forEach { key ->
                KeypadKey(
                    label = key,
                    onClick = {
                        onDigit(key)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onDelete,
            enabled = input.isNotEmpty()
        ) {
            Text("Delete")
        }

        Spacer(modifier = Modifier.width(18.dp))

        TextButton(
            onClick = onClose
        ) {
            Text("Hide keypad")
        }
    }
}

@Composable
private fun KeypadKey(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = ExitCallCallActionSurface,
        modifier = Modifier
            .size(68.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MorePanel(
    onClose: () -> Unit
) {
    Text(
        text = "More",
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(18.dp))

    MoreActionRow(
        title = "Audio route",
        subtitle = "Bluetooth and headset routing coming in #20"
    )

    Spacer(modifier = Modifier.height(10.dp))

    MoreActionRow(
        title = "Post-call behavior",
        subtitle = "Additional destinations coming later"
    )

    Spacer(modifier = Modifier.height(12.dp))

    TextButton(
        onClick = onClose
    ) {
        Text("Close")
    }
}

@Composable
private fun MoreActionRow(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ExitCallCallActionSurface)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            fontSize = 13.sp
        )
    }
}

@Composable
fun CallActionItem(
    label: String,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        ExitCallCallActionSurface
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            fontSize = 13.sp
        )
    }
}

package com.example.exitcall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exitcall.model.CallConfig
import com.example.exitcall.ui.theme.ExitCallActiveBackground
import com.example.exitcall.ui.theme.ExitCallCallActionSurface
import com.example.exitcall.ui.theme.ExitCallCallControlSurface
import com.example.exitcall.ui.theme.ExitCallHangUp
import kotlinx.coroutines.delay

@Composable
fun ActiveCallScreen(
    config: CallConfig,
    onHangUp: () -> Unit
) {
    var elapsedSeconds by remember {
        mutableStateOf(0)
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
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(
                    top = 40.dp,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
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
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = callTime,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = config.callerName,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 34.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedButton(
                onClick = { }
            ) {
                Text("Audio Emoji")
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp
                        )
                    )
                    .background(ExitCallCallControlSurface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CallActionItem("Keypad")
                    CallActionItem("Mute")
                    CallActionItem("Speaker")
                    CallActionItem("More")
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onHangUp,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExitCallHangUp
                    ),
                    modifier = Modifier.size(72.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("📞")
                }
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
fun CallActionItem(label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    color = ExitCallCallActionSurface,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "•",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp
        )
    }
}

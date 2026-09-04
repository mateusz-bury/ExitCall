package com.mateuszbury.exitcall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
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
                    top = 28.dp,
                    end = 18.dp,
                    bottom = 20.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CallActionItem(
                    label = "Keypad",
                    iconRes = R.drawable.ic_dialpad,
                    modifier = Modifier.weight(1f)
                )
                CallActionItem(
                    label = "Mute",
                    iconRes = R.drawable.ic_mic,
                    modifier = Modifier.weight(1f)
                )
                CallActionItem(
                    label = "Speaker",
                    iconRes = R.drawable.ic_volume_up,
                    modifier = Modifier.weight(1f)
                )
                CallActionItem(
                    label = "More",
                    iconRes = R.drawable.ic_more_vert,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

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
fun CallActionItem(
    label: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    color = ExitCallCallActionSurface,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp
        )
    }
}

package com.example.exitcall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exitcall.model.CallConfig
import kotlinx.coroutines.delay

@Composable
fun ActiveCallScreen(
    config: CallConfig,
    onHangUp: () -> Unit
) {
    var elapsedSeconds by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val formattedTime = "%02d:%02d".format(minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050706))
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(92.dp))

        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    color = Color(0xFFA8D5BA),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = config.callerName
                    .firstOrNull()
                    ?.uppercase()
                    ?: "?",
                color = Color(0xFF050706),
                fontSize = 40.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = config.callerName,
            color = Color.White,
            fontSize = 36.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = formattedTime,
            color = Color.LightGray,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(34.dp))

        Button(
            onClick = {},
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF272B28)
            )
        ) {
            Text(
                text = "Audio Emoji",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CallAction("Keypad")
            CallAction("Mute")
            CallAction("Speaker")
            CallAction("More")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onHangUp,
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B6B)
            )
        ) {
            Text(
                text = "📞",
                fontSize = 30.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
private fun CallAction(
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(
                    color = Color(0xFF272B28),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "•",
                color = Color.White,
                fontSize = 28.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
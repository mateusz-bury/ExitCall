package com.example.exitcall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exitcall.model.CallConfig
import kotlin.math.roundToInt

@Composable
fun IncomingCallScreen(
    config: CallConfig,
    onDecline: () -> Unit,
    onAnswer: () -> Unit
) {
    val background = Color(0xFF050706)
    val green = Color(0xFFA8D5BA)
    val text = Color(0xFFF2F4F2)
    val muted = Color(0xFF9BA39E)

    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(130.dp))

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color(0xFF222824), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = config.callerName.take(1).uppercase(),
                    color = green,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = config.callerName,
                color = text,
                fontSize = 42.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = config.phoneNumber,
                color = muted,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            IncomingCallSlider(
                dragOffset = dragOffset,
                onDragOffsetChange = { dragOffset = it },
                onDecline = onDecline,
                onAnswer = onAnswer,
                green = green,
                text = text,
                muted = muted
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun IncomingCallSlider(
    dragOffset: Float,
    onDragOffsetChange: (Float) -> Unit,
    onDecline: () -> Unit,
    onAnswer: () -> Unit,
    green: Color,
    text: Color,
    muted: Color
) {
    val density = LocalDensity.current
    val handleSize = 68.dp
    val horizontalPadding = 12.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .background(
                color = Color(0xFF171B18),
                shape = RoundedCornerShape(46.dp)
            )
    ) {
        val maxDrag = with(density) {
            ((maxWidth - handleSize - horizontalPadding * 2).toPx() / 2f)
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Decline",
                    color = Color(0xFFE87474),
                    fontSize = 13.sp
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Answer",
                    color = green,
                    fontSize = 13.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset { IntOffset(dragOffset.roundToInt(), 0) }
                .size(handleSize)
                .background(Color.White, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDragOffsetChange(
                                (dragOffset + dragAmount.x)
                                    .coerceIn(-maxDrag, maxDrag)
                            )
                        },
                        onDragEnd = {
                            when {
                                dragOffset >= maxDrag * 0.55f -> onAnswer()
                                dragOffset <= -maxDrag * 0.55f -> onDecline()
                                else -> onDragOffsetChange(0f)
                            }
                        },
                        onDragCancel = {
                            onDragOffsetChange(0f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "☎",
                color = green,
                fontSize = 30.sp
            )
        }
    }
}
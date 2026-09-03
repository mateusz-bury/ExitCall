package com.mateuszbury.exitcall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mateuszbury.exitcall.model.CallConfig
import com.mateuszbury.exitcall.ui.theme.ExitCallAnswerGreen
import com.mateuszbury.exitcall.ui.theme.ExitCallIncomingBackground
import com.mateuszbury.exitcall.ui.theme.ExitCallSliderBackground
import kotlin.math.roundToInt

@Composable
fun IncomingCallScreen(
    config: CallConfig,
    onDecline: () -> Unit,
    onAnswer: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ExitCallIncomingBackground)
                .safeDrawingPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier.height(130.dp)
            )

            Box(
                modifier = Modifier
                    .size(96.dp)
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
                    fontSize = 40.sp
                )
            }

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Text(
                text = config.callerName.ifBlank {
                    "Unknown"
                },
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 42.sp,
                lineHeight = 50.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (config.phoneNumber.isNotBlank()) {
                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = config.phoneNumber,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            IncomingCallSlider(
                onDecline = onDecline,
                onAnswer = onAnswer
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )
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
fun IncomingCallSlider(
    onDecline: () -> Unit,
    onAnswer: () -> Unit
) {
    var dragOffset by remember {
        mutableFloatStateOf(0f)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(
                RoundedCornerShape(46.dp)
            )
            .background(ExitCallSliderBackground)
    ) {
        val maxDragPx = with(
            LocalDensity.current
        ) {
            ((maxWidth - 76.dp) / 2 - 8.dp)
                .toPx()
        }

        val actionThreshold = maxDragPx * 0.55f

        Text(
            text = "Decline",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        )

        Text(
            text = "Answer",
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset {
                    IntOffset(
                        x = dragOffset.roundToInt(),
                        y = 0
                    )
                }
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onBackground)
                .pointerInput(maxDragPx) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffset =
                                (dragOffset + dragAmount)
                                    .coerceIn(
                                        -maxDragPx,
                                        maxDragPx
                                    )
                        },
                        onDragEnd = {
                            when {
                                dragOffset >= actionThreshold -> {
                                    dragOffset = 0f
                                    onAnswer()
                                }

                                dragOffset <= -actionThreshold -> {
                                    dragOffset = 0f
                                    onDecline()
                                }

                                else -> {
                                    dragOffset = 0f
                                }
                            }
                        },
                        onDragCancel = {
                            dragOffset = 0f
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "☎",
                color = ExitCallAnswerGreen,
                fontSize = 32.sp
            )
        }
    }
}

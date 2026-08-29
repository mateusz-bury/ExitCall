package com.example.exitcall.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ExitCallBackground = Color(0xFF101311)
private val ExitCallSurface = Color(0xFF191D1A)
private val ExitCallGreen = Color(0xFFA8D5BA)
private val ExitCallText = Color(0xFFF2F4F2)
private val ExitCallMuted = Color(0xFF9BA39E)

private enum class MainScreen {
    HOME,
    CALL_SETUP,
    SCHEDULE_CALL
}

@Composable
fun HomeScreen(
    onCallMeNow: () -> Unit,
    onScheduleCall: () -> Unit,
    onCallSetup: () -> Unit,
    onSettings: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ExitCallBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            // HEADER

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = "ExitCall",
                        color = ExitCallText,
                        fontSize = 28.sp
                    )

                    Text(
                        text = "Leave when you need to.",
                        color = ExitCallMuted,
                        fontSize = 14.sp
                    )
                }

                TextButton(
                    onClick = onSettings
                ) {
                    Text(
                        text = "Settings",
                        color = ExitCallGreen
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            // DASHBOARD PLACEHOLDERS

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                DashboardBox(
                    title = "Calls",
                    value = "—",
                    modifier = Modifier.weight(1f)
                )

                DashboardBox(
                    title = "Next call",
                    value = "Not scheduled",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            DashboardBox(
                title = "Status",
                value = "Ready",
                modifier = Modifier.fillMaxWidth()
            )

            // wypychamy akcje mniej więcej na środek ekranu

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Need an exit?",
                color = ExitCallText,
                fontSize = 22.sp
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = onCallMeNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExitCallGreen,
                    contentColor = Color(0xFF101311)
                )
            ) {
                Text(
                    text = "Start call",
                    fontSize = 17.sp
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedButton(
                onClick = onScheduleCall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ExitCallText
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        Color(0xFF3B443F)
                    )
                )
            ) {
                Text(
                    text = "Schedule a call"
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            OutlinedButton(
                onClick = onCallSetup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ExitCallGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        ExitCallGreen
                    )
                )
            ) {
                Text(
                    text = "Set up a call"
                )
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

@Composable
private fun DashboardBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .background(
                color = ExitCallSurface,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF252B27),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {

        Text(
            text = title,
            color = ExitCallMuted,
            fontSize = 13.sp
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = value,
            color = ExitCallText,
            fontSize = 18.sp
        )
    }
}
package com.mateuszbury.exitcall.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ExitCallColorScheme = darkColorScheme(
    primary = ExitCallGreen,
    onPrimary = ExitCallBackground,

    secondary = ExitCallGreen,
    onSecondary = ExitCallBackground,

    background = ExitCallBackground,
    onBackground = ExitCallText,

    surface = ExitCallSurface,
    onSurface = ExitCallText,

    surfaceVariant = ExitCallSurfaceVariant,
    onSurfaceVariant = ExitCallMuted,

    outline = ExitCallOutline,
    outlineVariant = ExitCallDivider,

    error = ExitCallError
)

@Composable
fun ExitCallTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ExitCallColorScheme,
        typography = Typography,
        content = content
    )
}

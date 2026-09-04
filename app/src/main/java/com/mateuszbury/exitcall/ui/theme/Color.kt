package com.mateuszbury.exitcall.ui.theme

import androidx.compose.ui.graphics.Color

val ExitCallBackground = Color(0xFF101311)
val ExitCallSurface = Color(0xFF191D1A)
val ExitCallSurfaceVariant = Color(0xFF252B27)

val ExitCallGreen = Color(0xFFA8D5BA)
val ExitCallText = Color(0xFFF2F4F2)
val ExitCallMuted = Color(0xFF9BA39E)

val ExitCallError = Color(0xFFFFB4AB)
val ExitCallOutline = Color(0xFF3B443F)
val ExitCallDivider = Color(0xFF2A302C)

// Call-flow aliases intentionally reuse the main ExitCall palette so the
// call surfaces feel like part of the same product rather than a separate UI.
val ExitCallIncomingBackground = ExitCallBackground
val ExitCallActiveBackground = ExitCallBackground
val ExitCallSliderBackground = ExitCallSurfaceVariant
val ExitCallCallControlSurface = ExitCallSurface
val ExitCallCallActionSurface = ExitCallSurfaceVariant
val ExitCallAnswerGreen = ExitCallGreen
val ExitCallHangUp = ExitCallError

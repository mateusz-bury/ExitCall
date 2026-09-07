package com.mateuszbury.exitcall.audio

enum class AudioRoute {
    PHONE,
    SPEAKER,
    BLUETOOTH
}

data class AudioRouteSnapshot(
    val availableRoutes: Set<AudioRoute> = setOf(
        AudioRoute.PHONE,
        AudioRoute.SPEAKER
    ),
    val selectedRoute: AudioRoute = AudioRoute.PHONE
)

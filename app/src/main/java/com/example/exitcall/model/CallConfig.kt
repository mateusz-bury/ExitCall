package com.example.exitcall.model

data class CallConfig(
    val callerName: String = "ExitCall",
    val phoneNumber: String = "123 456 789",
    val ringtoneUri: String? = null,
    val ringtoneName: String = "System default",
    val vibrate: Boolean = true
)
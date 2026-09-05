package com.mateuszbury.exitcall.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

class ActiveCallAudioRouter(
    context: Context
) {
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())

    private var listener: ((AudioRouteSnapshot) -> Unit)? = null
    private var started = false
    private var previousMode = AudioManager.MODE_NORMAL
    private var previousSpeakerphoneOn = false
    private var previousBluetoothScoOn = false
    private var previousCommunicationDeviceId: Int? = null

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            publishSnapshot()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            publishSnapshot()
        }
    }

    fun start(onRouteChanged: (AudioRouteSnapshot) -> Unit) {
        listener = onRouteChanged

        if (started) {
            publishSnapshot()
            return
        }

        started = true
        previousMode = audioManager.mode
        previousSpeakerphoneOn = audioManager.isSpeakerphoneOn
        previousBluetoothScoOn = audioManager.isBluetoothScoOn

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            previousCommunicationDeviceId = audioManager.communicationDevice?.id
        }

        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.registerAudioDeviceCallback(deviceCallback, mainHandler)
        publishSnapshot()
    }

    fun selectRoute(route: AudioRoute) {
        if (!started) {
            return
        }

        if (route !in availableRoutes()) {
            publishSnapshot()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            selectModernRoute(route)
        } else {
            selectLegacyRoute(route)
        }

        publishSnapshot()
    }

    fun stop() {
        if (!started) {
            listener = null
            return
        }

        runCatching {
            audioManager.unregisterAudioDeviceCallback(deviceCallback)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            restoreModernRoute()
        } else {
            restoreLegacyRoute()
        }

        audioManager.mode = previousMode
        listener = null
        started = false
    }

    private fun selectModernRoute(route: AudioRoute) {
        val device = when (route) {
            AudioRoute.PHONE -> preferredPhoneDevice()
            AudioRoute.SPEAKER -> communicationDevices()
                .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
            AudioRoute.BLUETOOTH -> communicationDevices()
                .firstOrNull(::isBluetoothDevice)
        }

        if (device == null) {
            Log.w("ExitCall", "Audio route $route is not currently available")
            return
        }

        val accepted = audioManager.setCommunicationDevice(device)
        Log.d(
            "ExitCall",
            "Audio route request route=$route deviceType=${device.type} accepted=$accepted"
        )
    }

    @Suppress("DEPRECATION")
    private fun selectLegacyRoute(route: AudioRoute) {
        when (route) {
            AudioRoute.PHONE -> {
                audioManager.stopBluetoothSco()
                audioManager.isBluetoothScoOn = false
                audioManager.isSpeakerphoneOn = false
            }

            AudioRoute.SPEAKER -> {
                audioManager.stopBluetoothSco()
                audioManager.isBluetoothScoOn = false
                audioManager.isSpeakerphoneOn = true
            }

            AudioRoute.BLUETOOTH -> {
                audioManager.isSpeakerphoneOn = false
                audioManager.startBluetoothSco()
                audioManager.isBluetoothScoOn = true
            }
        }
    }

    private fun restoreModernRoute() {
        val previousDevice = previousCommunicationDeviceId
            ?.let { previousId ->
                communicationDevices().firstOrNull { it.id == previousId }
            }

        audioManager.clearCommunicationDevice()

        if (previousDevice != null) {
            audioManager.setCommunicationDevice(previousDevice)
        }
    }

    @Suppress("DEPRECATION")
    private fun restoreLegacyRoute() {
        if (!previousBluetoothScoOn) {
            audioManager.stopBluetoothSco()
        }

        audioManager.isBluetoothScoOn = previousBluetoothScoOn
        audioManager.isSpeakerphoneOn = previousSpeakerphoneOn
    }

    private fun publishSnapshot() {
        if (!started) {
            return
        }

        val snapshot = AudioRouteSnapshot(
            availableRoutes = availableRoutes(),
            selectedRoute = selectedRoute()
        )

        mainHandler.post {
            listener?.invoke(snapshot)
        }
    }

    private fun availableRoutes(): Set<AudioRoute> {
        val routes = linkedSetOf(
            AudioRoute.PHONE,
            AudioRoute.SPEAKER
        )

        val bluetoothAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            communicationDevices().any(::isBluetoothDevice)
        } else {
            audioManager
                .getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .any(::isBluetoothDevice)
        }

        if (bluetoothAvailable) {
            routes += AudioRoute.BLUETOOTH
        }

        return routes
    }

    @Suppress("DEPRECATION")
    private fun selectedRoute(): AudioRoute {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val current = audioManager.communicationDevice

            return when {
                current == null -> AudioRoute.PHONE
                current.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> AudioRoute.SPEAKER
                isBluetoothDevice(current) -> AudioRoute.BLUETOOTH
                else -> AudioRoute.PHONE
            }
        }

        return when {
            audioManager.isBluetoothScoOn && AudioRoute.BLUETOOTH in availableRoutes() -> {
                AudioRoute.BLUETOOTH
            }

            audioManager.isSpeakerphoneOn -> AudioRoute.SPEAKER
            else -> AudioRoute.PHONE
        }
    }

    private fun preferredPhoneDevice(): AudioDeviceInfo? {
        val devices = communicationDevices()

        return devices.firstOrNull(::isWiredPhoneDevice)
            ?: devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
            }
            ?: devices.firstOrNull {
                !isBluetoothDevice(it) &&
                    it.type != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            }
            ?: devices.firstOrNull {
                it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
            }
    }

    private fun communicationDevices(): List<AudioDeviceInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.availableCommunicationDevices
        } else {
            audioManager
                .getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .toList()
        }
    }

    private fun isWiredPhoneDevice(device: AudioDeviceInfo): Boolean {
        return when (device.type) {
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_USB_DEVICE -> true

            else -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                device.type == AudioDeviceInfo.TYPE_USB_HEADSET
        }
    }

    private fun isBluetoothDevice(device: AudioDeviceInfo): Boolean {
        if (
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
            device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
        ) {
            return true
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            device.type == AudioDeviceInfo.TYPE_HEARING_AID
        ) {
            return true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (
                device.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                device.type == AudioDeviceInfo.TYPE_BLE_SPEAKER
            ) {
                return true
            }
        }

        return Build.VERSION.SDK_INT >= 37 &&
            device.type == AudioDeviceInfo.TYPE_BLE_HEARING_AID
    }
}

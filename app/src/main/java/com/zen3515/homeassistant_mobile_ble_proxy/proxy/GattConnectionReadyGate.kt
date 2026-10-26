package com.zen3515.homeassistant_mobile_ble_proxy.proxy

internal data class GattConnectionSetupDecision(
    val emitConnectionReady: Boolean,
    val discoverServices: Boolean,
)

internal fun decideGattConnectionSetup(
    acceptsPlatformCache: Boolean,
    platformCacheReady: Boolean,
): GattConnectionSetupDecision {
    val localServiceTableReady = acceptsPlatformCache && platformCacheReady
    return GattConnectionSetupDecision(
        emitConnectionReady = localServiceTableReady,
        discoverServices = !localServiceTableReady,
    )
}

/**
 * Tracks when an ESPHome Bluetooth connection may be reported as ready.
 *
 * Home Assistant's remote cache only stores the exported handle numbers. Every
 * new Android [android.bluetooth.BluetoothGatt] still needs a local service
 * table so those handles can be resolved to the current session's characteristic
 * objects. A cached connection may therefore become ready at link-up only when
 * Android has already supplied that local table. Otherwise it waits for service
 * discovery, just like a connection that explicitly requested fresh services.
 */
internal class GattConnectionReadyGate(
    private val acceptsPlatformCache: Boolean,
) {
    var isReady: Boolean = false
        private set

    val isWaitingForServices: Boolean
        get() = !isReady

    fun onLinkConnected(platformCacheReady: Boolean): GattConnectionSetupDecision {
        val decision = decideGattConnectionSetup(
            acceptsPlatformCache = acceptsPlatformCache,
            platformCacheReady = platformCacheReady,
        )
        return if (decision.emitConnectionReady) {
            decision.copy(emitConnectionReady = markReady())
        } else {
            decision
        }
    }

    fun onServicesDiscovered(): Boolean = markReady()

    private fun markReady(): Boolean {
        if (isReady) {
            return false
        }
        isReady = true
        return true
    }
}

package com.zen3515.homeassistant_mobile_ble_proxy.proxy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GattConnectionReadyGateTest {
    @Test
    fun cachedConnectionIsReadyAtLinkUpWhenAndroidCacheWasLoaded() {
        val gate = GattConnectionReadyGate(acceptsPlatformCache = true)

        assertTrue(gate.onLinkConnected(platformCacheReady = true).emitConnectionReady)
        assertTrue(gate.isReady)
        assertFalse(gate.isWaitingForServices)
        assertFalse(gate.onServicesDiscovered())
        assertFalse(gate.onLinkConnected(platformCacheReady = true).emitConnectionReady)
    }

    @Test
    fun cachedConnectionWaitsWhenNewAndroidSessionHasNoLocalServiceTable() {
        val gate = GattConnectionReadyGate(acceptsPlatformCache = true)

        assertFalse(gate.onLinkConnected(platformCacheReady = false).emitConnectionReady)
        assertFalse(gate.isReady)
        assertTrue(gate.isWaitingForServices)

        assertTrue(gate.onServicesDiscovered())
        assertTrue(gate.isReady)
        assertFalse(gate.isWaitingForServices)
        assertFalse(gate.onServicesDiscovered())
    }

    @Test
    fun noCacheConnectionIgnoresPlatformCacheAndWaitsForDiscovery() {
        val gate = GattConnectionReadyGate(acceptsPlatformCache = false)

        assertFalse(gate.onLinkConnected(platformCacheReady = true).emitConnectionReady)
        assertTrue(gate.isWaitingForServices)

        assertTrue(gate.onServicesDiscovered())
        assertTrue(gate.isReady)
    }
}

package com.zen3515.homeassistant_mobile_ble_proxy.proxy

import org.junit.Assert.assertEquals
import org.junit.Test

class GattConnectionSetupDecisionTest {
    @Test
    fun preferCacheHitReportsReadyWithoutDiscovery() {
        assertEquals(
            GattConnectionSetupDecision(
                emitConnectionReady = true,
                discoverServices = false,
            ),
            decideGattConnectionSetup(
                acceptsPlatformCache = true,
                platformCacheReady = true,
            ),
        )
    }

    @Test
    fun preferCacheMissDiscoversBeforeReportingReady() {
        assertEquals(
            GattConnectionSetupDecision(
                emitConnectionReady = false,
                discoverServices = true,
            ),
            decideGattConnectionSetup(
                acceptsPlatformCache = true,
                platformCacheReady = false,
            ),
        )
    }

    @Test
    fun forceDiscoveryDoesNotTreatPlatformCacheAsReady() {
        assertEquals(
            GattConnectionSetupDecision(
                emitConnectionReady = false,
                discoverServices = true,
            ),
            decideGattConnectionSetup(
                acceptsPlatformCache = false,
                platformCacheReady = true,
            ),
        )
    }
}

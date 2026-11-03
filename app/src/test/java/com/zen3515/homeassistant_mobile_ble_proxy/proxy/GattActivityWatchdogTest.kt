package com.zen3515.homeassistant_mobile_ble_proxy.proxy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GattActivityWatchdogTest {
    // @scenario:cmw0nqdsg0uo3pq6y1ztzazzm
    @Test
    fun `idle connection is expired and cannot be revived by a late callback`() {
        val scheduled = mutableListOf<Runnable>()
        var expired = 0
        val watchdog = GattActivityWatchdog(
            timeoutMs = 15 * 60 * 1000L,
            postDelayed = { callback, _ -> scheduled += callback },
            removeCallback = { scheduled -= it },
        )

        watchdog.start { expired += 1 }
        val timeout = scheduled.single()

        timeout.run()
        watchdog.recordActivity()
        timeout.run()

        assertEquals(1, expired)
        assertFalse(watchdog.isArmed)
    }

    @Test
    fun `real GATT activity renews the lease and stale timeout is ignored`() {
        val scheduled = mutableListOf<Runnable>()
        var expired = 0
        val watchdog = GattActivityWatchdog(
            timeoutMs = 15 * 60 * 1000L,
            postDelayed = { callback, _ -> scheduled += callback },
            removeCallback = { scheduled -= it },
        )

        watchdog.start { expired += 1 }
        val staleTimeout = scheduled.single()
        watchdog.recordActivity()
        val currentTimeout = scheduled.single()

        staleTimeout.run()
        assertEquals(0, expired)
        assertTrue(watchdog.isArmed)

        currentTimeout.run()
        assertEquals(1, expired)
        assertFalse(watchdog.isArmed)
    }

    @Test
    fun `cancel removes the lease and ignores its queued callback`() {
        val scheduled = mutableListOf<Runnable>()
        var expired = 0
        val watchdog = GattActivityWatchdog(
            timeoutMs = 15 * 60 * 1000L,
            postDelayed = { callback, _ -> scheduled += callback },
            removeCallback = { scheduled -= it },
        )

        watchdog.start { expired += 1 }
        val cancelledTimeout = scheduled.single()
        watchdog.cancel()
        cancelledTimeout.run()

        assertEquals(0, expired)
        assertFalse(watchdog.isArmed)
    }
}

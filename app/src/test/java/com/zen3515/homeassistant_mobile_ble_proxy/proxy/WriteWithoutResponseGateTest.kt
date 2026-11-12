package com.zen3515.homeassistant_mobile_ble_proxy.proxy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Executable steps of @scenario:cmwdh5u95019u3hp5ldc47ebz (docs/bdd/paced-writes.feature).
 *
 * HATAB-63: Android keeps the GATT client busy after a write-without-response until onCharacteristicWrite,
 * so the next queued write must wait for that callback; tuya_ble's fragmented handshake lost every second
 * fragment when the proxy started the next write straight away.
 */
class WriteWithoutResponseGateTest {
    private val scheduled = mutableListOf<Runnable>()
    private val gate = WriteWithoutResponseGate(
        timeoutMs = 1_000,
        postDelayed = { callback, _ -> scheduled += callback },
        removeCallback = { scheduled -= it },
    )

    @Test
    fun `the queue waits for Android's write callback before the next fragment`() {
        var released = 0
        gate.begin { released += 1 }

        assertTrue(gate.isWaiting)
        assertEquals(0, released)
        assertTrue(gate.acknowledge())
        assertFalse(gate.isWaiting)
        assertTrue("the fallback timer is cancelled", scheduled.isEmpty())
        assertEquals("acknowledging hands the release to the caller", 0, released)
    }

    @Test
    fun `a write callback that never comes releases the queue once after the fallback`() {
        var released = 0
        gate.begin { released += 1 }

        scheduled.single().run()

        assertEquals(1, released)
        assertFalse(gate.isWaiting)
        assertFalse("a late callback must not complete the next operation", gate.acknowledge())
    }

    @Test
    fun `a callback for a write with response is not mistaken for the paced write`() {
        assertFalse(gate.acknowledge())
    }

    @Test
    fun `closing the connection drops the pending fallback without releasing`() {
        var released = 0
        gate.begin { released += 1 }

        gate.reset()

        assertFalse(gate.isWaiting)
        assertTrue(scheduled.isEmpty())
        assertEquals(0, released)
    }
}

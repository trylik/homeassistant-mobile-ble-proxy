package com.zen3515.homeassistant_mobile_ble_proxy.proxy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Executable steps of @scenario:cmwddyhfw014s3hp5538nlxz0 (docs/bdd/gatt-lease-release.feature), HATAB-63:
 * a GATT lease is released when the API session that opened it closes, unless a newer session re-took it.
 */
class GattLeaseOwnershipTest {
    private val meter = 0xDC234F35FE4CL
    private val other = 0xA09208715C46L

    @Test
    fun `a closed client releases every lease it connected`() {
        val ownership = GattLeaseOwnership<String>()
        ownership.recordConnect(meter, "old-session")
        ownership.recordConnect(other, "old-session")

        assertEquals(setOf(meter, other), ownership.releaseOwner("old-session").toSet())
        assertTrue(ownership.releaseOwner("old-session").isEmpty())
    }

    @Test
    fun `a lease taken over by a reconnected client survives the old client closing`() {
        val ownership = GattLeaseOwnership<String>()
        ownership.recordConnect(meter, "old-session")
        ownership.recordConnect(meter, "new-session")

        assertTrue(ownership.releaseOwner("old-session").isEmpty())
        assertEquals(listOf(meter), ownership.releaseOwner("new-session"))
    }

    @Test
    fun `a lease that already ended is not released again`() {
        val ownership = GattLeaseOwnership<String>()
        ownership.recordConnect(meter, "session")
        ownership.recordEnded(meter)

        assertTrue(ownership.releaseOwner("session").isEmpty())
    }

    @Test
    fun `releasing one client leaves another client's leases alone`() {
        val ownership = GattLeaseOwnership<String>()
        ownership.recordConnect(meter, "home-assistant")
        ownership.recordConnect(other, "second-client")

        assertEquals(listOf(meter), ownership.releaseOwner("home-assistant"))
        assertEquals(listOf(other), ownership.releaseOwner("second-client"))
    }
}

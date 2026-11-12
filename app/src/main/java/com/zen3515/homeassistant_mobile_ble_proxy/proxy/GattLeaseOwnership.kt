package com.zen3515.homeassistant_mobile_ble_proxy.proxy

/**
 * Remembers which API client asked for each GATT connection, so the connections a client opened can be
 * released when that client goes away (HATAB-63).
 *
 * ESPHome firmware drops a client's BLE connections when its API session ends. Without that, a lease opened
 * by a Home Assistant session that has since reconnected stays CONNECTED on Android: the device stops
 * advertising, the new session never sees it, and its notifications keep the idle watchdog from firing.
 *
 * The most recent connect request owns a lease, so a reconnected client that re-took a device keeps it when
 * the old session closes. Thread-safe: requests arrive on client threads, connection events on the GATT
 * dispatcher.
 */
class GattLeaseOwnership<S : Any> {
    private val lock = Any()
    private val owners = HashMap<Long, S>()

    /** [owner] asked to connect [address]; it now owns that lease. */
    fun recordConnect(address: Long, owner: S) {
        synchronized(lock) { owners[address] = owner }
    }

    /** The lease on [address] ended (disconnect request or link loss); nobody owns it any more. */
    fun recordEnded(address: Long) {
        synchronized(lock) { owners.remove(address) }
    }

    /** Forgets every lease [owner] holds and returns their addresses, for the caller to disconnect. */
    fun releaseOwner(owner: S): List<Long> = synchronized(lock) {
        val released = owners.filterValues { it == owner }.keys.toList()
        released.forEach(owners::remove)
        released
    }
}

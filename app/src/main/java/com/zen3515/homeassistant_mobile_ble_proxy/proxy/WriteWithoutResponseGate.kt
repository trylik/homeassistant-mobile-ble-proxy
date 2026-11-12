package com.zen3515.homeassistant_mobile_ble_proxy.proxy

/**
 * Holds a connection's GATT queue after a write-without-response until Android reports it with
 * onCharacteristicWrite (HATAB-63).
 *
 * Android keeps the GATT client busy after every characteristic write, including WRITE_TYPE_NO_RESPONSE, so a
 * second write issued before that callback returns false. Starting the next queued write straight away made
 * every second fragment of tuya_ble's handshake fail with ERROR_OPERATION_FAILED, and the Tuya meter never
 * answered a reconnect. The fallback timer releases the queue if a callback never arrives, so a missing
 * callback cannot stall the connection.
 */
internal class WriteWithoutResponseGate(
    timeoutMs: Long,
    postDelayed: (Runnable, Long) -> Unit,
    removeCallback: (Runnable) -> Unit,
) {
    private val fallback = DisconnectWatchdog(timeoutMs, postDelayed, removeCallback)

    val isWaiting: Boolean
        get() = fallback.isArmed

    /** A write-without-response started; [onFallback] releases the queue if Android never calls back. */
    fun begin(onFallback: () -> Unit) {
        fallback.arm(onFallback)
    }

    /**
     * Android called onCharacteristicWrite. Returns true when it acknowledged the paced write, in which case
     * the caller completes the operation; false when no write-without-response was waiting.
     */
    fun acknowledge(): Boolean {
        if (!fallback.isArmed) return false
        fallback.cancel()
        return true
    }

    /** The connection is going away; drop the fallback without releasing anything. */
    fun reset() {
        fallback.cancel()
    }
}

package com.zen3515.homeassistant_mobile_ble_proxy.proxy

/**
 * Expires a connected GATT client when Android stops delivering any real GATT activity.
 *
 * A repeated ESPHome connect request is deliberately not activity: Home Assistant can retry a
 * connection whose Android object is still marked connected after the peripheral has vanished.
 */
internal class GattActivityWatchdog(
    private val timeoutMs: Long,
    private val postDelayed: (Runnable, Long) -> Unit,
    private val removeCallback: (Runnable) -> Unit,
) {
    private var pending: Runnable? = null
    private var onTimeout: (() -> Unit)? = null

    val isArmed: Boolean
        get() = pending != null

    fun start(onTimeout: () -> Unit) {
        this.onTimeout = onTimeout
        schedule()
    }

    fun recordActivity() {
        if (onTimeout == null) return
        schedule()
    }

    fun cancel() {
        onTimeout = null
        pending?.let(removeCallback)
        pending = null
    }

    private fun schedule() {
        pending?.let(removeCallback)
        lateinit var scheduled: Runnable
        scheduled = Runnable {
            if (pending !== scheduled) return@Runnable
            pending = null
            val callback = onTimeout ?: return@Runnable
            onTimeout = null
            callback()
        }
        pending = scheduled
        try {
            postDelayed(scheduled, timeoutMs)
        } catch (throwable: Throwable) {
            pending = null
            onTimeout = null
            throw throwable
        }
    }
}

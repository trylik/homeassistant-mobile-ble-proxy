package com.zen3515.homeassistant_mobile_ble_proxy.proxy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Hands events to one coroutine so callback order is preserved across suspend points.
 *
 * Android GATT notifications are already serialized onto the main thread by the manager.
 * Launching a new IO coroutine for every callback loses that ordering before the bytes reach
 * Home Assistant, which corrupts protocols whose messages span several notifications.
 */
class OrderedEventDispatcher<T>(
    scope: CoroutineScope,
    private val handler: suspend (T) -> Unit,
) {
    private val events = Channel<T>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (event in events) {
                handler(event)
            }
        }
    }

    fun dispatch(event: T): Boolean = events.trySend(event).isSuccess

    fun close() {
        events.close()
    }
}

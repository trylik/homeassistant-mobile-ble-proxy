package com.zen3515.homeassistant_mobile_ble_proxy.proxy

import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderedEventDispatcherTest {
    @Test
    fun `dispatches burst events in submission order`() = runBlocking {
        val received = Collections.synchronizedList(mutableListOf<Int>())
        val completed = CountDownLatch(100)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val dispatcher = OrderedEventDispatcher<Int>(scope) { event ->
            withContext(Dispatchers.Default) {
                delay((event % 7).toLong())
            }
            received += event
            completed.countDown()
        }

        repeat(100, dispatcher::dispatch)

        assertTrue(completed.await(5, TimeUnit.SECONDS))
        assertEquals((0 until 100).toList(), received)
        dispatcher.close()
        scope.cancel()
    }
}

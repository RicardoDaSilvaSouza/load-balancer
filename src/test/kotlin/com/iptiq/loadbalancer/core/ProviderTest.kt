package com.iptiq.loadbalancer.core

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProviderTest {

    @Test
    fun `given a provider, when invoking get function, it should return an unique ID`() {
        // setup
        val firstProvider = DefaultProvider()
        val secondProvider = DefaultProvider()

        runBlocking {
            // act
            val firstId = firstProvider.get()
            val secondId = secondProvider.get()

            // assert
            assertNotEquals(firstId, secondId)
            assertEquals(firstProvider.id.toString(), firstId)
            assertEquals(secondProvider.id.toString(), secondId)
        }
    }

    @Test
    fun `given a provider, when invoking check function, it should return the provider status`() {
        // setup
        val firstProvider = DefaultProvider()
        val secondProvider = DefaultProvider(status = false)

        // act
        val firstStatus = firstProvider.check()
        val secondStatus = secondProvider.check()

        // assert
        assertTrue(firstStatus)
        assertFalse(secondStatus)
    }
}
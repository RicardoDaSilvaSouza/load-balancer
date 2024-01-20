package com.iptiq.loadbalancer.core

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class LoadBalancerTest {

    @Test
    fun `given a load balancer, when it is instantiated, it should have a list of 10 providers`() {
        // setup
        val loadBalancer = RandomLoadBalancer()

        // act + assert
        assertEquals(10, loadBalancer.providers.size)
    }

    @Test
    fun `given an instance of RandomLoadBalancer, when invoking get function, it should return random providers' ids`() {
        // setup
        val loadBalancer = RandomLoadBalancer()

        runBlocking {
            // act
            val firstResponse = loadBalancer.get()
            val secondResponse = loadBalancer.get()
            val thirdResponse = loadBalancer.get()

            // assert
            assertNotNull(loadBalancer.providers[firstResponse])
            assertNotNull(loadBalancer.providers[secondResponse])
            assertNotNull(loadBalancer.providers[thirdResponse])
        }
    }

    @Test
    fun `given an instance of RoundRobinLoadBalancer, when invoking get function, it should return providers' ids in sequence`() {
        // setup
        val loadBalancer = RoundRobinLoadBalancer()
        val providers = loadBalancer.providers.toList()
        val expectedFirstResponse = providers[0].first
        val expectedSecondResponse = providers[1].first
        val expectedThirdResponse = providers[2].first

        runBlocking {
            // act
            val firstResponse = loadBalancer.get()
            val secondResponse = loadBalancer.get()
            val thirdResponse = loadBalancer.get()

            // assert
            assertEquals(expectedFirstResponse, firstResponse)
            assertEquals(expectedSecondResponse, secondResponse)
            assertEquals(expectedThirdResponse, thirdResponse)
        }
    }

    @Test
    fun `given an instance of RoundRobinLoadBalancer with 2 providers, when invoking get function, it should use the providers in sequential order`() {
        // setup
        val loadBalancer = RoundRobinLoadBalancer(providersSize = 2)
        val providers = loadBalancer.providers.toList()
        val expectedFirstResponse = providers[0].first
        val expectedSecondResponse = providers[1].first
        val expectedThirdResponse = providers[0].first
        val expectedFourthResponse = providers[1].first

        runBlocking {
            // act
            val firstResponse = loadBalancer.get()
            val secondResponse = loadBalancer.get()
            val thirdResponse = loadBalancer.get()
            val fourthResponse = loadBalancer.get()

            // assert
            assertEquals(expectedFirstResponse, firstResponse)
            assertEquals(expectedSecondResponse, secondResponse)
            assertEquals(expectedThirdResponse, thirdResponse)
            assertEquals(expectedFourthResponse, fourthResponse)
        }
    }

    @Test
    fun `given an instance of any LoadBalancer, when invoking removeProvider function, it should remove the given provider from the list`() {
        // setup
        val loadBalancer = RandomLoadBalancer()
        val providers = loadBalancer.providers.toList()
        val expectedProvider = providers[0].second

        // act
        loadBalancer.removeProvider(provider = expectedProvider)

        // assert
        runBlocking {
            assertNull(loadBalancer.providers[expectedProvider.get()])
        }
    }

    @Test
    fun `given an instance of any LoadBalancer, when invoking removeProvider function, it should do nothing if the provider is not in the list`() {
        // setup
        val loadBalancer = RandomLoadBalancer()
        val expectedProvider = DefaultProvider()

        // act
        loadBalancer.removeProvider(provider = expectedProvider)

        // assert
        assertEquals(10, loadBalancer.providers.size)
    }

    @Test
    fun `given an instance of any LoadBalancer, when invoking addProvider function, it should add a provider to the list of providers`() {
        // setup
        val loadBalancer = RandomLoadBalancer()
        val providers = loadBalancer.providers.toList()
        val expectedProvider = DefaultProvider()

        // act
        loadBalancer.removeProvider(provider = providers[0].second)
        loadBalancer.addProvider(provider = expectedProvider)

        // assert
        runBlocking {
            assertContains(loadBalancer.providers, expectedProvider.get())
        }
    }

    @Test
    fun `given an instance of any LoadBalancer, when invoking addProvider function, it should throw error in case the list is full`() {
        // setup
        val loadBalancer = RandomLoadBalancer()
        val expectedProvider = DefaultProvider()

        // act + assert
        assertThrows<IllegalStateException> {
            loadBalancer.addProvider(provider = expectedProvider)
        }
    }

    @Test
    fun `given an instance of any LoadBalancer, when heartBeatRoutine starts, it should remove providers`() {
        // setup
        val loadBalancer = RandomLoadBalancer(heartBeatDelayInSec = 1)
        val providers = loadBalancer.providers.toList()
        val expectedProvider = providers[0].second

        runBlocking {
            // act
            val job = launch {
                loadBalancer.heartBeatRoutine()
            }
            expectedProvider.running = false
            delay(2)
            job.cancel()

            // assert
            assertNull(loadBalancer.providers[expectedProvider.get()])
        }
    }

    @Test
    fun `given an instance of any LoadBalancer, when heartBeatRoutine starts, it should recover providers after 2 successful checks`() {
        // setup
        val loadBalancer = RandomLoadBalancer(
            providersSize = 2,
            heartBeatDelayInSec = 1
        )
        val providers = loadBalancer.providers.toList()
        val expectedProvider = providers[0].second

        runBlocking {
            // act
            val job = launch {
                loadBalancer.heartBeatRoutine()
            }
            expectedProvider.running = false
            delay(2.seconds)
            expectedProvider.running = true
            delay(2.seconds)
            job.cancel()

            // assert
            assertContains(loadBalancer.providers, expectedProvider.get())
        }
    }

    @Test
    fun `given an instance of any LoadBalancer, when reaching the maximum of requests, it should throw exception`() {
        // setup
        val loadBalancer = RandomLoadBalancer(
            providersSize = 2,
            maxRequestsPerProvider = 1,
        )

        assertThrows<IllegalStateException> {
            runBlocking {
                // act + assert
                val firstJob = launch {
                    loadBalancer.get()
                }
                val secondJob = launch {
                    loadBalancer.get()
                }
                val thirdJob = launch {
                    loadBalancer.get()
                }
                firstJob.join()
                secondJob.join()
                thirdJob.join()
            }
        }
    }
}
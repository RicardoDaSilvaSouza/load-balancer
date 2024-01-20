package com.iptiq.loadbalancer.core

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

abstract class LoadBalancer<T, P : Provider<T>>(
    providerType: Class<P>,
    protected val providersSize: Int,
    private val maxRequestsPerProvider: Int,
    protected val heartBeatDelayInSec: Int,
    private val amountBeatsToRecover: Int,
    private var requests: AtomicInteger = AtomicInteger(0),
) {
    val providers = mutableMapOf<String, Provider<T>>().also {
        for (providerIndex in 1..providersSize) {
            val provider = providerType.getConstructor().newInstance()
            it[provider.id.toString()] = provider
        }
    }
    private val removedProviders = mutableMapOf<String, Provider<T>>()
    private val heartBeatTable = mutableMapOf<String, Int>()

    abstract suspend fun get(): T

    fun addProvider(provider: Provider<T>) {
        if (providers.size >= providersSize)
            throw IllegalStateException("Max provider amount reached: $providersSize")

        providers.put(provider = provider)
    }

    fun removeProvider(provider: Provider<T>): Provider<T>? =
        providers.remove(provider.id.toString())

    suspend fun heartBeatRoutine() {
        while (true) {
            checkActiveProviders()
            checkRemovedProviders()

            delay(heartBeatDelayInSec.seconds)
        }
    }

    protected suspend fun handleRequest(action: suspend () -> T): T {
        val currentRequests = requests.incrementAndGet()
        val maxRequests = getMaxRequests()
        if (currentRequests >= maxRequests) throw IllegalStateException("Reached max requests allowed: $maxRequests")
        return action().also {
            requests.decrementAndGet()
        }
    }

    private fun getMaxRequests() = maxRequestsPerProvider * providers.size

    private fun recoverProvider(providerKey: String) {
        removedProviders.remove(providerKey)?.let { recoveredProvider ->
            addProvider(provider = recoveredProvider)
        }
    }

    private fun checkActiveProviders() {
        providers.filter {
            !it.value.check()
        }.values.forEach {
            removeProvider(provider = it)?.also { removedProvider ->
                removedProviders.put(provider = removedProvider)
                heartBeatTable[removedProvider.id.toString()] = 0
            }
        }
    }

    private fun checkRemovedProviders() {
        removedProviders.filter {
            it.value.check()
        }.values.forEach {
            val id = it.id.toString()
            var beats = heartBeatTable[id] ?: 0
            beats++
            if (beats >= amountBeatsToRecover) {
                recoverProvider(providerKey = id)
                heartBeatTable.remove(id)
            } else {
                heartBeatTable[id] = beats
            }
        }
    }

    private fun MutableMap<String, Provider<T>>.put(provider: Provider<T>) {
        this[provider.id.toString()] = provider
    }
}
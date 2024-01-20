package com.iptiq.loadbalancer.core

class RoundRobinLoadBalancer(
    providersSize: Int = 10,
    maxRequestsPerProvider: Int = 2,
    amountBeatsToRecover: Int = 2,
    heartBeatDelayInSec: Int = 5,
) : LoadBalancer<String, DefaultProvider>(
    providersSize = providersSize,
    maxRequestsPerProvider = maxRequestsPerProvider,
    amountBeatsToRecover = amountBeatsToRecover,
    heartBeatDelayInSec = heartBeatDelayInSec,
    providerType = DefaultProvider::class.java
) {
    private var providerIndex = 0
    override suspend fun get(): String =
        handleRequest {
            providers.toList()[providerIndex].second.get().also {
                providerIndex++
                if (providerIndex >= providersSize) providerIndex = 0
            }
        }
}
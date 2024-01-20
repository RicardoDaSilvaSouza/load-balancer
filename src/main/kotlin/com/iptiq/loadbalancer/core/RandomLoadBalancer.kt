package com.iptiq.loadbalancer.core

class RandomLoadBalancer(
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
    override suspend fun get(): String =
        handleRequest {
            providers.toList().random().second.get()
        }
}
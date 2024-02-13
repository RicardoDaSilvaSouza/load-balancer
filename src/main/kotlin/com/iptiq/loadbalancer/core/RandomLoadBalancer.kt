package com.iptiq.loadbalancer.core

import com.iptiq.loadbalancer.core.model.Result

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
    override suspend fun get(): Result<String> =
        handleRequest {
            Result.Success(providers.toList().random().second.get())
        }
}
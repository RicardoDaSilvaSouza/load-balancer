package com.iptiq.loadbalancer.core

import com.iptiq.loadbalancer.core.model.Result

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
    override suspend fun get(): Result<String> =
        handleRequest {
            if (providers.isEmpty()) {
                Result.Failure(err = IllegalStateException("There is no providers"))
            } else {
                val providersAsList = providers.toList()
                if (providerIndex >= providersAsList.size) providerIndex = 0

                val provider = providersAsList[providerIndex].second
                val value = provider.get()
                providerIndex++

                Result.Success(content = value)
            }
        }
}
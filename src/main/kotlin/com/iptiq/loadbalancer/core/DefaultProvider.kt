package com.iptiq.loadbalancer.core

import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class DefaultProvider(
    id: UUID = UUID.randomUUID(),
    status: Boolean = true,
) : Provider<String>(
    id = id,
    running = status,
) {
    override suspend fun get(): String {
        delay(1.seconds)
        return id.toString()
    }
}
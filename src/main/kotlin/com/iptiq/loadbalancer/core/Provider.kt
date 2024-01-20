package com.iptiq.loadbalancer.core

import java.util.UUID

abstract class Provider<T>(
    val id: UUID,
    var running: Boolean,
) {
    abstract suspend fun get(): T

    fun check(): Boolean = running
}
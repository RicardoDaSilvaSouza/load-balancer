package com.iptiq.loadbalancer.core.model

sealed class Result<T> {
    val value: T 
        get() = when (this) {
            is Success<T> -> this.content
            is Failure -> throw this.err
        }
    
    data class Success<T>(val content: T): Result<T>()
    data class Failure<T>(val err: Throwable): Result<T>()
}
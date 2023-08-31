package com.matteblack.di

import kotlin.reflect.KClass

annotation class Inject

object DIContainer {
    @PublishedApi
    internal val registry = mutableMapOf<KClass<*>, Any>()


    fun <T : Any> register(instance: T) {
        registry[instance::class] = instance
    }

    fun unregister(klass: KClass<*>) {
        registry -= klass
    }

    inline fun <reified T : Any> resolve(): T {
        return (registry[T::class] ?: error("No factory registered for ${T::class}"))as T
    }
}

inline fun <reified T : Any> injected() = lazy { DIContainer.resolve<T>() }


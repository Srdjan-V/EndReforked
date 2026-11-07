package io.github.srdjanv.endreforked.common

/** Small eager registry helper used by the mod registries. */
class Registry<T> {
    private val entries = mutableListOf<T>()

    fun register(value: T): T = value.also(entries::add)

    fun all(): List<T> = entries
}


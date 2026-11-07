package io.github.srdjanv.endreforked.common

import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Registry entries are declared as `val X by REGISTRY.register(::Thing)` and created, in declaration order, by [registerAll]
 * inside the matching `RegistryEvent.Register`. Reading an entry before that, or while it is being created, throws.
 * That makes a construction time cross reference between two entries of the same registry a clear error instead of a null.
 * Entries of an earlier Forge registry (blocks while items are created) are safe to read.
 */
class DeferredRegistry<T : IForgeRegistryEntry<T>>(private val owner: Any, private val type: Class<T>) {
    private val entries = mutableListOf<Entry<out T>>()

    fun <V : T> register(factory: () -> V): Entry<V> = Entry(factory).also(entries::add)

    /** Creates every entry in declaration order and adds it to [registry]. */
    fun registerAll(registry: IForgeRegistry<T>) = entries.forEach { registry.register(it.create()) }

    /** Every entry, in declaration order. Only valid after [registerAll]. */
    fun all(): List<T> = entries.map { it.get("<unknown>") }

    inner class Entry<V : T> internal constructor(private val factory: () -> V) : ReadOnlyProperty<Any?, V> {
        private var value: V? = null
        private var creating = false

        internal fun create(): V {
            check(value == null) { "${owner::class.simpleName} entry is created twice" }
            creating = true
            try { return factory().also { value = it } } finally { creating = false }
        }

        internal fun get(name: String): V = value ?: error(
            "${owner::class.simpleName}.$name is read " +
                if (creating) "while it is being created (cross reference inside its own constructor)"
                else "before RegistryEvent.Register<${type.simpleName}>",
        )

        override fun getValue(thisRef: Any?, property: KProperty<*>): V = get(property.name)
    }
}

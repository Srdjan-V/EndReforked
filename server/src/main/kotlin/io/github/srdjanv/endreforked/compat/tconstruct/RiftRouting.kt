package io.github.srdjanv.endreforked.compat.tconstruct

/** The Rift routing, without Minecraft types: the item goes through the sinks in order. Each sink returns what it could not take, or null. */
object RiftRouting {
    /** What is left after all [sinks]. The caller drops it at the feet of the player, so nothing falls into the void. */
    fun <T : Any> route(item: T, sinks: List<(T) -> T?>): T? {
        var rest = item
        for (sink in sinks) rest = sink(rest) ?: return null
        return rest
    }
}

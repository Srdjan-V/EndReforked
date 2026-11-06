package io.github.srdjanv.endreforked.client

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

object ParticleHandler {
    private val particles = HashMap<String, RenderInfo>()

    fun addToRender(info: RenderInfo) = synchronized(particles) { particles[info.id] = info }

    fun removeFromRender(id: String) = synchronized(particles) { particles.remove(id) }

    @JvmStatic
    @SubscribeEvent
    fun playerTick(event: TickEvent.PlayerTickEvent) {
        if (event.phase == TickEvent.Phase.END) return
        synchronized(particles) {
            val iterator = particles.values.iterator()
            while (iterator.hasNext()) {
                val info = iterator.next()
                info.render()
                if (--info.remakingTicks < 0) iterator.remove()
            }
        }
    }

    @JvmStatic
    @SubscribeEvent
    fun onPlayerDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) = synchronized(particles) { particles.clear() }

    abstract class RenderInfo(val id: String, var remakingTicks: Int) {
        abstract fun render()
    }
}

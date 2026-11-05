package io.github.srdjanv.endreforked.common.network

import io.github.srdjanv.endreforked.wire.s2c.S2CPacket
import io.netty.buffer.Unpooled
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.common.network.FMLEventChannel
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket

/**
 * The one custom-payload channel of the mod. Every message is a serialized protobuf envelope.
 * The client registers its listener on [channel]. There are no client to server messages yet, so the server has no listener.
 */
class Network(val channel: FMLEventChannel, private val name: String) {
    fun sendToAll(packet: S2CPacket) {
        channel.sendToAll(FMLProxyPacket(PacketBuffer(Unpooled.wrappedBuffer(S2CPacket.ADAPTER.encode(packet))), name))
    }
}

package io.github.srdjanv.endreforked.client.di

import io.github.srdjanv.endreforked.ModServices
import io.github.srdjanv.endreforked.client.ClientProxy
import io.github.srdjanv.endreforked.client.network.ClientPacketHandler
import io.github.srdjanv.endreforked.common.CommonProxy
import io.github.srdjanv.endreforked.common.registry.Registries
import io.github.srdjanv.endreforked.wire.s2c.ChunkEntropy
import net.minecraftforge.fml.relauncher.Side

/** Client part of the composition root. */
class ClientServices(common: ModServices) {
    /** The registries of the client. Separate from the server ones, also in single player. The server snapshots replace their content. */
    val registries: Registries = Registries.create(false)
    /** The entropy of the chunk of the local player, for the HUD. Null until the server sent it. */
    var chunkEntropy: ChunkEntropy? = null
        private set

    val packetHandler = ClientPacketHandler(common.net, registries) { chunkEntropy = it }
    val proxy: CommonProxy = ClientProxy(Side.CLIENT, common.registrySync, registries)

    companion object {
        /** The one entry point that the server module calls by name, see Tags.CLIENTSERVICES. */
        @JvmStatic
        fun create(common: ModServices): CommonProxy = ClientServices(common).proxy
    }
}

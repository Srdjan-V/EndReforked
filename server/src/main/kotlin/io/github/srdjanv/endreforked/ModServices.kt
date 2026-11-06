package io.github.srdjanv.endreforked

import io.github.srdjanv.endreforked.common.CommonProxy
import io.github.srdjanv.endreforked.common.network.Network
import io.github.srdjanv.endreforked.common.registry.Registries
import io.github.srdjanv.endreforked.common.registry.RegistrySync
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.Logger

/** Composition root, built once in the mod constructor. The client adds its own parts on top, see ClientServices. */
class ModServices(val side: Side, val logger: Logger) {
    val net = Network(NetworkRegistry.INSTANCE.newEventDrivenChannel(Tags.MODID), Tags.MODID)

    /** The registries of the logical server. The client has its own set. */
    val registries: Registries = Registries.create()
    val registrySync = RegistrySync(registries, net, logger)

    /** The proxy of the dedicated server. Not used on the client, so it is created on first access. */
    val serverProxy: CommonProxy by lazy { CommonProxy(side, registrySync) }
}

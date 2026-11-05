package io.github.srdjanv.endreforked.client.di

import dagger.Component
import dagger.Module
import dagger.Provides
import io.github.srdjanv.endreforked.client.ClientProxy
import io.github.srdjanv.endreforked.client.network.ClientPacketHandler
import io.github.srdjanv.endreforked.common.network.Network
import io.github.srdjanv.endreforked.common.CommonProxy
import io.github.srdjanv.endreforked.common.registry.Registries
import io.github.srdjanv.endreforked.common.registry.RegistrySync
import io.github.srdjanv.endreforked.di.CommonComponent
import net.minecraftforge.fml.relauncher.Side
import jakarta.inject.Named
import jakarta.inject.Scope

@Scope
annotation class ClientScope

@Module
object ClientModule {
    /** The registries of the client. Separate from the server ones, also in single player. The server snapshots replace their content. */
    @ClientScope
    @Provides
    @Named("client")
    fun clientRegistries(): Registries = Registries.create()

    @ClientScope
    @Provides
    fun proxy(net: Network, registrySync: RegistrySync, @Named("client") client: Registries): CommonProxy {
        ClientPacketHandler(net, client)
        return ClientProxy(Side.CLIENT, registrySync, client)
    }
}

/** Client component. Provides the ClientProxy as the CommonProxy. */
@ClientScope
@Component(modules = [ClientModule::class], dependencies = [CommonComponent::class])
interface ClientComponent {
    fun proxy(): CommonProxy

    @Component.Factory
    interface Factory {
        fun create(parent: CommonComponent): ClientComponent
    }
}

/** The one entry point that the server module calls by name, see Tags.CLIENTCOMPONENT. */
object ClientComponentFactory {
    @JvmStatic
    fun create(parent: CommonComponent): CommonProxy = DaggerClientComponent.factory().create(parent).proxy()
}

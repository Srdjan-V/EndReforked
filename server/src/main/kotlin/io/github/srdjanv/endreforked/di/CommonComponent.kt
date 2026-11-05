package io.github.srdjanv.endreforked.di

import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.CommonProxy
import io.github.srdjanv.endreforked.common.network.Network
import io.github.srdjanv.endreforked.common.registry.Registries
import io.github.srdjanv.endreforked.common.registry.RegistrySync
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.Logger
import jakarta.inject.Scope

/** Lives as long as the mod. */
@Scope
annotation class CommonScope

@Module
object CommonModule {
    @Provides
    fun logger(): Logger = EndReforked.LOGGER

    @CommonScope
    @Provides
    fun net(): Network = Network(NetworkRegistry.INSTANCE.newEventDrivenChannel(Tags.MODID), Tags.MODID)

    /** The registries of the logical server. The client has its own set, see ClientModule. */
    @CommonScope
    @Provides
    fun serverRegistries(): Registries = Registries.create()

    @CommonScope
    @Provides
    fun registrySync(registries: Registries, net: Network, logger: Logger): RegistrySync =
        RegistrySync(registries, net, logger)
}

/** Composition root, shared by both sides. ServerComponent and ClientComponent add the proxy on top. */
@CommonScope
@Component(modules = [CommonModule::class])
interface CommonComponent {
    fun side(): Side
    fun logger(): Logger
    fun net(): Network
    fun registrySync(): RegistrySync

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance side: Side): CommonComponent
    }
}

@Scope
annotation class ServerScope

@Module
object ServerModule {
    @ServerScope
    @Provides
    fun proxy(side: Side, registrySync: RegistrySync): CommonProxy = CommonProxy(side, registrySync)
}

/** Component for the dedicated server. Provides the plain CommonProxy. */
@ServerScope
@Component(modules = [ServerModule::class], dependencies = [CommonComponent::class])
interface ServerComponent {
    fun proxy(): CommonProxy

    @Component.Factory
    interface Factory {
        fun create(parent: CommonComponent): ServerComponent
    }
}

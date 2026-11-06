package io.github.srdjanv.endreforked.compat.groovyscript

import com.cleanroommc.groovyscript.api.GroovyPlugin
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer
import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.registry.RegistryEntry
import io.github.srdjanv.endreforked.common.registry.MachineRecipeDefaults
import io.github.srdjanv.endreforked.compat.scripting.ScriptLayer
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry

/**
 * GroovyScript finds this class by itself (it implements [GroovyPlugin]) and makes it the `mods.endreborn` container.
 * Nothing in the mod loads this class, so without GroovyScript it never loads.
 * Scripts: use the `postInit` loader. The registries are built after it, and `/gs reload` runs only this loader again.
 */
class EndRebornGroovyPlugin : GroovyPlugin {
    override fun getModId() = Tags.MODID

    override fun getContainerName() = Tags.MODNAME

    override fun onCompatLoaded(container: GroovyContainer<*>) {}

    override fun createGroovyPropertyContainer(): GroovyPropertyContainer = EndRebornGroovy()
}

/** The script API: one property per registry. Created when GroovyScript has loaded all mods, so the registries exist. */
class EndRebornGroovy : GroovyPropertyContainer() {
    override fun initialize(owner: GroovyContainer<*>) {
        val sync = EndReforked.getProxy().registrySync
        val registries = sync.registries
        fun <E : RegistryEntry> layer(registry: ManagedRegistry<E, *>) =
            ScriptLayer(registry, NAMESPACE, sync::report)

        val machines = layer(registries.machineRecipes)
        addProperty(MachineGroovy("EntropyChamber", MachineRecipeDefaults.ENTROPY_CHAMBER, machines))
        addProperty(MachineGroovy("EndForge", MachineRecipeDefaults.END_FORGE, machines))
        addProperty(MachineGroovy("Materializer", MachineRecipeDefaults.MATERIALIZER, machines))
        addProperty(EntropyWandGroovy(layer(registries.wandConversions)))
        addProperty(FluidBlockInteractionGroovy(layer(registries.fluidBlockInteractions)))
        addProperty(FluidEntityInteractionGroovy(layer(registries.fluidEntityInteractions)))
        addProperty(WorldFeatureGroovy(layer(registries.worldFeatures)))
        addProperty(EndBiomeGroovy(layer(registries.endBiomes)))
    }

    private companion object {
        /** The namespace of the ids that scripts get without a name. */
        const val NAMESPACE = "groovyscript"
    }
}

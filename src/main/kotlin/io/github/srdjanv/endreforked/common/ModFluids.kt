package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.common.fluids.FluidEndMagma
import io.github.srdjanv.endreforked.common.fluids.FluidEntropy
import io.github.srdjanv.endreforked.common.fluids.FluidOrgana
import io.github.srdjanv.endreforked.utils.models.IAsset
import net.minecraft.item.Item
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Forge fluids are not a Forge registry, so there is no [DeferredRegistry]. [register] runs in pre init, which is before
 * every `RegistryEvent`: the fluid blocks read these fluids when the block registry event creates them.
 */
object ModFluids {
    lateinit var END_MAGMA: FluidEndMagma private set
    lateinit var ENTROPY: FluidEntropy private set
    lateinit var ORGANA: FluidOrgana private set

    private val fluids get() = listOf<Fluid>(END_MAGMA, ENTROPY, ORGANA)

    fun register() {
        END_MAGMA = FluidEndMagma().also(FluidRegistry::registerFluid)
        ENTROPY = FluidEntropy().also(FluidRegistry::registerFluid)
        ORGANA = FluidOrgana().also(FluidRegistry::registerFluid)
    }

    @SubscribeEvent
    @JvmStatic
    fun registerModels(event: ModelRegistryEvent) {
        fluids.filterIsInstance<IAsset>().forEach { it.handleAssets() }
    }

    @SubscribeEvent
    @JvmStatic
    fun registerItems(event: RegistryEvent.Register<Item>) {
        fluids.forEach { FluidRegistry.addBucketForFluid(it) }
    }
}

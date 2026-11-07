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

object ModFluids {
    private val FLUIDS = Registry<Fluid>()

    @JvmField val END_MAGMA: FluidEndMagma = register(::FluidEndMagma)
    @JvmField val ENTROPY: FluidEntropy = register(::FluidEntropy)
    @JvmField val ORGANA: FluidOrgana = register(::FluidOrgana)

    private fun <F : Fluid> register(factory: () -> F): F =
        factory().also { FluidRegistry.registerFluid(it); FLUIDS.register(it) }

    @SubscribeEvent
    @JvmStatic
    fun registerModels(event: ModelRegistryEvent) {
        FLUIDS.all().filterIsInstance<IAsset>().forEach { it.handleAssets() }
    }

    @SubscribeEvent
    @JvmStatic
    fun registerItems(event: RegistryEvent.Register<Item>) {
        FLUIDS.all().forEach { FluidRegistry.addBucketForFluid(it) }
    }
}

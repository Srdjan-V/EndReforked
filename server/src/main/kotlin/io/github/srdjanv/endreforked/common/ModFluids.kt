package io.github.srdjanv.endreforked.common

import com.google.common.base.Suppliers
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
import java.util.function.Supplier

object ModFluids {
    private val FLUIDS = mutableListOf<Supplier<out Fluid>>()

    @JvmField val END_MAGMA: Supplier<FluidEndMagma> = register(::FluidEndMagma)
    @JvmField val ENTROPY: Supplier<FluidEntropy> = register(::FluidEntropy)
    @JvmField val ORGANA: Supplier<FluidOrgana> = register(::FluidOrgana)

    private fun <F : Fluid> register(factory: () -> F): Supplier<F> =
        Suppliers.memoize { factory().also { FluidRegistry.registerFluid(it) } }.also { FLUIDS += it }

    @SubscribeEvent
    @JvmStatic
    fun registerModels(event: ModelRegistryEvent) {
        FLUIDS.map { it.get() }.filterIsInstance<IAsset>().forEach { it.handleAssets() }
    }

    @SubscribeEvent
    @JvmStatic
    fun registerItems(event: RegistryEvent.Register<Item>) {
        FLUIDS.forEach { FluidRegistry.addBucketForFluid(it.get()) }
    }
}

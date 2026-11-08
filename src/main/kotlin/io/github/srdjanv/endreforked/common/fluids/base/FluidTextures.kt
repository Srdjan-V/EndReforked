package io.github.srdjanv.endreforked.common.fluids.base

import io.github.srdjanv.endreforked.Tags
import net.minecraft.util.ResourceLocation

object FluidTextures {
    @JvmField val STILL = ResourceLocation(Tags.MODID, "blocks/fluids/fluid_still")
    @JvmField val FLOWING = ResourceLocation(Tags.MODID, "blocks/fluids/fluid_flow")
    @JvmField val MOLTEN_STILL = ResourceLocation(Tags.MODID, "blocks/fluids/molten_fluid_still")
    @JvmField val MOLTEN_FLOWING = ResourceLocation(Tags.MODID, "blocks/fluids/molten_fluid_flow")
}

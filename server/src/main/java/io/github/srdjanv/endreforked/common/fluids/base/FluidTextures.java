package io.github.srdjanv.endreforked.common.fluids.base;

import net.minecraft.util.ResourceLocation;

import io.github.srdjanv.endreforked.Tags;

public class FluidTextures {

    public static ResourceLocation STILL = new ResourceLocation(Tags.MODID, "blocks/fluids/fluid_still");
    public static ResourceLocation FLOWING = new ResourceLocation(Tags.MODID, "blocks/fluids/fluid_flow");

    public static ResourceLocation MOLTEN_STILL = new ResourceLocation(Tags.MODID, "blocks/fluids/molten_fluid_still");
    public static ResourceLocation MOLTEN_FLOWING = new ResourceLocation(Tags.MODID, "blocks/fluids/molten_fluid_flow");

    /*
     * public static ResourceLocation getStill(String fluidName) {
     * return new ResourceLocation(Tags.MODID, String.format("blocks/fluids/%s", fluidName));
     * }
     * 
     * public static ResourceLocation getFlowing(String fluidName) {
     * return new ResourceLocation(Tags.MODID, String.format("blocks/fluids/%s_flow", fluidName));
     * }
     */
}

package io.github.srdjanv.endreforked.common.fluids.base;

import java.awt.*;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.IAsset;

public class BaseFluid extends Fluid implements IAsset {

    public BaseFluid(String fluidName, ResourceLocation still, ResourceLocation flowing, Color color) {
        super(fluidName, still, flowing, color);
    }

    public BaseFluid(String fluidName, ResourceLocation still, ResourceLocation flowing,
                     @Nullable ResourceLocation overlay, Color color) {
        super(fluidName, still, flowing, overlay, color);
    }

    public BaseFluid(String fluidName, ResourceLocation still, ResourceLocation flowing, int color) {
        super(fluidName, still, flowing, color);
    }

    public BaseFluid(String fluidName, ResourceLocation still, ResourceLocation flowing,
                     @Nullable ResourceLocation overlay, int color) {
        super(fluidName, still, flowing, overlay, color);
    }

    public BaseFluid(String fluidName, ResourceLocation still, ResourceLocation flowing) {
        super(fluidName, still, flowing);
    }

    public BaseFluid(String fluidName, ResourceLocation still, ResourceLocation flowing,
                     @Nullable ResourceLocation overlay) {
        super(fluidName, still, flowing, overlay);
    }

    @Override
    public void handleAssets() {
        EndReforked.getProxy().registerToTextureAtlas(getFlowing());
        EndReforked.getProxy().registerToTextureAtlas(getStill());
        var overlay = getOverlay();
        if (overlay != null) {
            EndReforked.getProxy().registerToTextureAtlas(overlay);
        }
    }
}

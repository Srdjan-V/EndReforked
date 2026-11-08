package io.github.srdjanv.endreforked.core.devmixin;

import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.srdjanv.endreforked.Tags;

@Mixin(AnvilChunkLoader.class)
public abstract class AnvilChunkLoaderMixin {

    @Inject(method = "loadChunk__Async", at = @At("HEAD"), cancellable = true, remap = false)
    public void loadChunk(World worldIn, int x, int z, CallbackInfoReturnable<Object[]> info) {
        if (FMLCommonHandler
                .instance()
                .getMinecraftServerInstance()
                .getWorld(0)
                .getGameRules()
                .getBoolean(Tags.MODID + "_dev_disable_region_loading"))
            info.setReturnValue(null);
    }
}

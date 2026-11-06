package io.github.srdjanv.endreforked.common.fluids.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.IAsset
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid

open class BaseFluid(name: String, still: ResourceLocation, flowing: ResourceLocation, color: Int) :
    Fluid(name, still, flowing, color), IAsset {

    override fun handleAssets() {
        EndReforked.getProxy().registerToTextureAtlas(flowing)
        EndReforked.getProxy().registerToTextureAtlas(still)
        overlay?.let { EndReforked.getProxy().registerToTextureAtlas(it) }
    }
}

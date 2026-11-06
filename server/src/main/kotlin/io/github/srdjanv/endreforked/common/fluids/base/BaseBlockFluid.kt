package io.github.srdjanv.endreforked.common.fluids.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.IAsset
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraftforge.fluids.BlockFluidClassic
import net.minecraftforge.fluids.Fluid

open class BaseBlockFluid(name: String, fluid: Fluid, material: Material, mapColor: MapColor) :
    BlockFluidClassic(fluid, material, mapColor), IAsset {

    init {
        setRegistryName(name)
        setTranslationKey(name)
    }

    override fun handleAssets() {
        EndReforked.getProxy().registerFileStateMapper(this, "fluids", registryName!!.path)
    }
}

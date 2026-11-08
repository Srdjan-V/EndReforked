package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.block.BlockRotatedPillar
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material

class BlockPillar(name: String, material: Material) : BlockRotatedPillar(material), InventoryBlockModel {

    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
        setSoundType(SoundType.STONE)
        setHardness(3.0f)
        setHarvestLevel("pickaxe", 2)
    }
}

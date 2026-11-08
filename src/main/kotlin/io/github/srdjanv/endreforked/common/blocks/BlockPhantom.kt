package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.block.BlockBreakable
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import java.util.Random

class BlockPhantom(name: String, material: Material, ignoreSimilarity: Boolean) :
    BlockBreakable(material, ignoreSimilarity), InventoryBlockModel {

    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }

    override fun isFullCube(state: IBlockState) = true

    override fun canSilkHarvest() = true

    override fun quantityDropped(random: Random) = 0

    override fun isReplaceable(worldIn: IBlockAccess, pos: BlockPos) = true
}

package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.items.base.BaseSeeds
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.EnumPlantType

class ItemOrganaFlowerSeed : BaseSeeds(
    "organa_flower_seed", ModBlocks.ORGANA_FLOWER_STEM_BLOCK, ModBlocks.ORGANA_WEED_BLOCK,
) {
    private val crop = ModBlocks.ORGANA_FLOWER_STEM_BLOCK

    override fun onItemUse(player: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing,
                           hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        val stack = player.getHeldItem(hand)
        val state = worldIn.getBlockState(pos)
        if (facing == EnumFacing.UP && player.canPlayerEdit(pos.offset(facing), facing, stack) &&
            state.block.canSustainPlant(state, worldIn, pos, EnumFacing.UP, crop) && worldIn.isAirBlock(pos.up())
        ) {
            worldIn.setBlockState(pos.up(), getPlant(worldIn, pos), 11)
            stack.shrink(1)
            return EnumActionResult.SUCCESS
        }
        return EnumActionResult.FAIL
    }

    override fun getPlantType(world: IBlockAccess, pos: BlockPos) = ORGANA_FLOWER

    private companion object {
        val ORGANA_FLOWER: EnumPlantType = EnumPlantType.getPlantType("organa_flower")
    }
}

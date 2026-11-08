package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.items.base.ItemSeedFoodBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.EnumPlantType

class ItemDragoniteBerries : ItemSeedFoodBase(
    "dragonite_berries", 1, 0.8f, ModBlocks.DRAGONITE_CROP, ModBlocks.ENTROPY_END_STONE,
) {
    private val crop = ModBlocks.DRAGONITE_CROP

    init {
        setAlwaysEdible()
    }

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

    override fun getPlantType(world: IBlockAccess, pos: BlockPos) = DRAGONITE

    private companion object {
        val DRAGONITE: EnumPlantType = EnumPlantType.getPlantType("dragonite")
    }
}

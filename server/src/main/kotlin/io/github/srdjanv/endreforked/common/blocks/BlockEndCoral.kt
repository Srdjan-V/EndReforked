package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockBush
import net.minecraft.block.IGrowable
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.IShearable
import java.util.Random

class BlockEndCoral : BaseBlockBush("end_coral", Material.VINE), IShearable, IGrowable {
    init {
        addSustainableBlock(Blocks.END_BRICKS)
        addSustainableBlock(Blocks.END_STONE)
        addSustainableBlock(ModBlocks.ENTROPY_END_STONE)
        addSustainableBlock(ModBlocks.ENTROPY_CROP_BLOCK)
    }

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos) = END_BUSH_AABB

    override fun getMapColor(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): MapColor = MapColor.YELLOW

    override fun quantityDropped(random: Random) = 0

    override fun isReplaceable(worldIn: IBlockAccess, pos: BlockPos) = true

    override fun harvestBlock(
        worldIn: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, te: TileEntity?, stack: ItemStack,
    ) {
    }

    override fun isShearable(item: ItemStack, world: IBlockAccess, pos: BlockPos) = true

    override fun onSheared(item: ItemStack, world: IBlockAccess, pos: BlockPos, fortune: Int): MutableList<ItemStack> =
        mutableListOf(ItemStack(ModBlocks.END_CORAL))

    override fun canGrow(worldIn: World, pos: BlockPos, state: IBlockState, isClient: Boolean) = true

    override fun canUseBonemeal(worldIn: World, rand: Random, pos: BlockPos, state: IBlockState) = true

    override fun grow(worldIn: World, rand: Random, startPos: BlockPos, state: IBlockState) {
        val upPos = startPos.up()
        for (i in 0 until 64) {
            var pos = upPos
            var j = 0
            while (true) {
                if (j >= i / 16) {
                    if (worldIn.isAirBlock(pos) && canPlaceBlockAt(worldIn, pos)) {
                        worldIn.setBlockState(pos, defaultState)
                    }
                    break
                }
                pos = pos.add(
                    rand.nextInt(3) - 1,
                    (rand.nextInt(3) - 1) * rand.nextInt(3) / 2,
                    rand.nextInt(3) - 1,
                )
                if (worldIn.getBlockState(pos.down()).block != Blocks.END_STONE ||
                    worldIn.getBlockState(pos).isNormalCube
                ) break
                ++j
            }
        }
    }

    companion object {
        @JvmField
        val END_BUSH_AABB = AxisAlignedBB(
            0.09999999403953552, 0.0, 0.09999999403953552,
            0.8999999761581421, 0.400000011920929, 0.8999999761581421,
        )
    }
}

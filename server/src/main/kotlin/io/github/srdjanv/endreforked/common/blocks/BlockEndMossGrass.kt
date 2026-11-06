package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import net.minecraft.block.IGrowable
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class BlockEndMossGrass : BlockBase("end_moss_grass", Material.GRASS, MapColor.PURPLE), IGrowable {
    init {
        setSoundType(SoundType.GLASS)
        setHarvestLevel("pickaxe", 2)
        setHardness(1F)
        setTickRandomly(true)
    }

    override fun onFallenUpon(worldIn: World, pos: BlockPos, entityIn: Entity, fallDistance: Float) {
        if (entityIn.isSneaking) super.onFallenUpon(worldIn, pos, entityIn, fallDistance)
        else entityIn.fall(fallDistance, 0.0F)
    }

    override fun onLanded(worldIn: World, entityIn: Entity) {
        if (entityIn.isSneaking) {
            super.onLanded(worldIn, entityIn)
        } else if (entityIn.motionY < 0.0) {
            if (entityIn.motionY > -0.6) {
                super.onLanded(worldIn, entityIn)
                return
            }
            entityIn.motionY = -entityIn.motionY
            if (entityIn !is EntityLivingBase) entityIn.motionY *= 0.8
        }
    }

    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        if (worldIn.isRemote) return
        // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
        if (!worldIn.isAreaLoaded(pos, 3)) return
        spreadMoss(worldIn, pos, rand)
    }

    override fun canGrow(worldIn: World, pos: BlockPos, state: IBlockState, isClient: Boolean) = true

    override fun canUseBonemeal(worldIn: World, rand: Random, pos: BlockPos, state: IBlockState) = true

    override fun grow(worldIn: World, rand: Random, startPos: BlockPos, state: IBlockState) {
        spreadMoss(worldIn, startPos, rand)
        val upPos = startPos.up()
        for (i in 0 until 64) {
            var pos = upPos
            var j = 0
            while (true) {
                if (j >= i / 16) {
                    if (worldIn.isAirBlock(pos)) {
                        val weed = ModBlocks.ORGANA_WEED_BLOCK.get()
                        if (weed.canPlaceBlockAt(worldIn, pos)) {
                            worldIn.setBlockState(pos, weed.defaultState)
                        } else {
                            val downPos = pos.down()
                            if (worldIn.getBlockState(downPos).block == Blocks.END_STONE) {
                                worldIn.setBlockState(downPos, defaultState)
                            }
                        }
                    }
                    break
                }
                pos = pos.add(
                    rand.nextInt(3) - 1,
                    (rand.nextInt(3) - 1) * rand.nextInt(3) / 2,
                    rand.nextInt(3) - 1,
                )
                if (worldIn.getBlockState(pos.down()).block == this || !worldIn.getBlockState(pos).isNormalCube) ++j
                else break
            }
        }
    }

    private fun spreadMoss(worldIn: World, pos: BlockPos, rand: Random) {
        if (worldIn.getLightFromNeighbors(pos.up()) < 4 &&
            worldIn.getBlockState(pos.up()).getLightOpacity(worldIn, pos.up()) > 2
        ) {
            worldIn.setBlockState(pos, ModBlocks.END_MOSS_BLOCK.get().defaultState)
        }
        for (i in 0 until 4) {
            val randPos = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1)
            if (randPos.y in 0..255 && !worldIn.isBlockLoaded(randPos)) return
            val up = worldIn.getBlockState(randPos.up())
            if (worldIn.getBlockState(randPos).block == ModBlocks.END_MOSS_BLOCK.get() &&
                worldIn.getLightFromNeighbors(randPos.up()) >= 4 &&
                up.getLightOpacity(worldIn, pos.up()) <= 2
            ) {
                worldIn.setBlockState(randPos, defaultState)
            }
        }
    }
}

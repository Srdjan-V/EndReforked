package io.github.srdjanv.endreforked.common.blocks

import com.cleanroommc.modularui.factory.TileEntityGuiFactory
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import io.github.srdjanv.endreforked.common.tiles.EndForgeTile
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class BlockEndForge(name: String) : BlockBase(name, Material.ROCK) {
    init {
        setSoundType(SoundType.STONE)
        setHardness(3.0F)
        setHarvestLevel("pickaxe", 2)
    }

    override fun getMapColor(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): MapColor = MapColor.PURPLE

    override fun onEntityWalk(worldIn: World, pos: BlockPos, entityIn: Entity) {
        if (!entityIn.isImmuneToFire && entityIn is EntityLivingBase &&
            !EnchantmentHelper.hasFrostWalkerEnchantment(entityIn)
        ) {
            entityIn.attackEntityFrom(DamageSource.HOT_FLOOR, 1.0F)
        }
        super.onEntityWalk(worldIn, pos, entityIn)
    }

    @SideOnly(Side.CLIENT)
    override fun getRenderLayer() = BlockRenderLayer.CUTOUT

    override fun isOpaqueCube(state: IBlockState) = false

    override fun isFullCube(state: IBlockState) = false

    override fun onBlockActivated(
        worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer,
        hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float,
    ): Boolean {
        if (!worldIn.isRemote) TileEntityGuiFactory.open(playerIn, pos)
        return true
    }

    override fun hasTileEntity(state: IBlockState) = true

    override fun createTileEntity(world: World, state: IBlockState): TileEntity? = EndForgeTile()

    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        val above = pos.up()
        val block = worldIn.getBlockState(above).block
        if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
            worldIn.setBlockToAir(above)
            worldIn.playSound(
                null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
                2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F,
            )
            (worldIn as? WorldServer)?.spawnParticle(
                EnumParticleTypes.SMOKE_LARGE, above.x + 0.5, above.y + 0.25, above.z + 0.5,
                8, 0.5, 0.25, 0.5, 0.0,
            )
        }
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        tooltip.add(I18n.format("tile.block_forge.tooltip"))
    }

    @SideOnly(Side.CLIENT)
    override fun randomDisplayTick(stateIn: IBlockState, worldIn: World, pos: BlockPos, rand: Random) {
        val x = pos.x.toDouble()
        val y = pos.y.toDouble()
        val z = pos.z.toDouble()
        if (worldIn.getBlockState(pos.up()).material == Material.AIR) {
            if (rand.nextInt(50) == 0) {
                val px = x + rand.nextFloat()
                val py = y + stateIn.getBoundingBox(worldIn, pos).maxY
                val pz = z + rand.nextFloat()
                worldIn.spawnParticle(EnumParticleTypes.LAVA, px, py, pz, 0.0, 0.0, 0.0)
                worldIn.playSound(
                    px, py, pz, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS,
                    0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false,
                )
            }
            if (rand.nextInt(100) == 0) {
                worldIn.playSound(
                    x, y, z, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS,
                    0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false,
                )
            }
        }
    }
}

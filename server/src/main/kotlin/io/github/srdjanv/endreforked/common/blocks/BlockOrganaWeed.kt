package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockBush
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.OrganaWeedTile
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.IShearable
import java.util.Random

class BlockOrganaWeed : BaseBlockBush("organa_weed", Material.VINE), IShearable {

    init {
        sustainableBlocks.add(ModBlocks.END_MOSS_GRASS_BLOCK.get())
    }

    override fun hasTileEntity(state: IBlockState) = true

    override fun createTileEntity(world: World, state: IBlockState): TileEntity? = OrganaWeedTile()

    override fun onEntityCollision(worldIn: World, pos: BlockPos, state: IBlockState, entityIn: Entity) {
        if (worldIn.isRemote) return
        entityIn.fallDistance = 0f
        if (entityIn is EntityPlayer) {
            if (entityIn.isSneaking) return
            entityIn.addPotionEffect(PotionEffect(MobEffects.LEVITATION, 20, 2))
            entityIn.addPotionEffect(PotionEffect(MobEffects.SPEED, 80, 2))
        } else if (entityIn is EntityLivingBase) {
            entityIn.addPotionEffect(PotionEffect(MobEffects.LEVITATION, 10))
            entityIn.addPotionEffect(PotionEffect(MobEffects.SPEED, 20))
        }
    }

    override fun getMapColor(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): MapColor = MapColor.PURPLE

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
        BlockEndCoral.END_BUSH_AABB

    override fun isReplaceable(worldIn: IBlockAccess, pos: BlockPos) = true

    override fun quantityDropped(random: Random) = if (random.nextInt(100) < 20) 1 else 0

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ModItems.ORGANA_FLOWER_SEED.get()

    override fun isShearable(item: ItemStack, world: IBlockAccess, pos: BlockPos) = true

    override fun onSheared(item: ItemStack, world: IBlockAccess, pos: BlockPos, fortune: Int): List<ItemStack> =
        listOf(ItemStack(ModBlocks.ORGANA_WEED_BLOCK.get()))
}

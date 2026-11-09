package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import io.github.srdjanv.endreforked.common.tiles.LormyteStabilizerTile
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

/**
 * The Lormyte Stabilizer. Its [TIER] (0 to 2, also the item meta) sets the area: its chunk, 3x3 or 5x5 chunks.
 * While it is on, the chunk entropy of the area stays below the cap. A redstone signal turns it off ([POWERED]).
 * It has no GUI. A small tile entity only lets the entropy world find it when its chunk loads.
 */
class BlockLormyteStabilizer : BlockBase("lormyte_stabilizer", Material.ROCK) {
    init {
        setSoundType(SoundType.GLASS)
        setHardness(3.0F)
        setHarvestLevel("pickaxe", 2)
        setLightLevel(0.5F)
        defaultState = blockState.baseState.withProperty(TIER, 0).withProperty(POWERED, false)
    }

    override fun createBlockState() = BlockStateContainer(this, TIER, POWERED)

    // Meta: bits 0 and 1 are the tier, bit 2 is the redstone state.
    override fun getStateFromMeta(meta: Int): IBlockState =
        defaultState.withProperty(TIER, (meta and 3).coerceAtMost(MAX_TIER)).withProperty(POWERED, meta and 4 != 0)

    override fun getMetaFromState(state: IBlockState): Int = state.getValue(TIER) or (if (state.getValue(POWERED)) 4 else 0)

    override fun getStateForPlacement(
        world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase,
    ): IBlockState = defaultState.withProperty(TIER, meta.coerceIn(0, MAX_TIER)).withProperty(POWERED, world.isBlockPowered(pos))

    /** The block drops itself with its tier. */
    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ModItems.LORMYTE_STABILIZER

    override fun damageDropped(state: IBlockState) = state.getValue(TIER)

    override fun getItem(worldIn: World, pos: BlockPos, state: IBlockState) = ItemStack(this, 1, state.getValue(TIER))

    override fun getSubBlocks(itemIn: CreativeTabs, items: NonNullList<ItemStack>) {
        for (tier in 0..MAX_TIER) items.add(ItemStack(this, 1, tier))
    }

    override fun handleAssets() {} // the item (BaseMetaItemBlock) registers the model of each tier

    override fun hasTileEntity(state: IBlockState) = true

    override fun createTileEntity(world: World, state: IBlockState): TileEntity = LormyteStabilizerTile()

    override fun onBlockAdded(worldIn: World, pos: BlockPos, state: IBlockState) = updatePowered(worldIn, pos, state)

    override fun neighborChanged(state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos) =
        updatePowered(worldIn, pos, state)

    private fun updatePowered(world: World, pos: BlockPos, state: IBlockState) {
        if (world.isRemote) return
        val powered = world.isBlockPowered(pos)
        if (powered != state.getValue(POWERED)) world.setBlockState(pos, state.withProperty(POWERED, powered), 2)
    }

    companion object {
        const val MAX_TIER = 2

        @JvmField
        val TIER: PropertyInteger = PropertyInteger.create("tier", 0, MAX_TIER)

        /** True while a redstone signal turns the crystal off. An off crystal does not cap the chunk entropy, and it can absorb (see Stabilizers). */
        @JvmField
        val POWERED: PropertyBool = PropertyBool.create("powered")
    }
}

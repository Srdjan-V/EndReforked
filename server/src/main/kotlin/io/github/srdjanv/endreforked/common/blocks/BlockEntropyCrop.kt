package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockCrops
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.EntropyCropTile
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Enchantments
import net.minecraft.init.SoundEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.NonNullList
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.EnumPlantType
import net.minecraftforge.common.IPlantable
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class BlockEntropyCrop : BaseBlockCrops("entropy_crop") {
    init {
        setSoundType(SoundType.STONE)
        setHardness(3.0F)
        setHarvestLevel("pickaxe", 2)
    }

    override fun getLightValue(state: IBlockState, world: IBlockAccess, pos: BlockPos) = when (state.getValue(AGE)) {
        2 -> 1
        3 -> 2
        else -> 0
    }

    override fun hasTileEntity(state: IBlockState) = state.getValue(AGE) == 3

    override fun createTileEntity(world: World, state: IBlockState): TileEntity? =
        if (state.getValue(AGE) == 3) EntropyCropTile() else null

    override fun canSilkHarvest(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer) = true

    override fun getSilkTouchDrop(state: IBlockState) = ItemStack(ModItems.ENTROPY_CROP_BLOCK, 1, 0)

    override fun getSubBlocks(itemIn: CreativeTabs, items: NonNullList<ItemStack>) {
        items.add(ItemStack(ModItems.ENTROPY_CROP_BLOCK, 1, 0))
        items.add(ItemStack(ModItems.ENTROPY_CROP_BLOCK, 1, 3))
    }

    override fun getSeed(): Item = ModItems.ENTROPY_CROP_BLOCK

    override fun getCrop(): Item = ModItems.ENTROPY_SHARD

    override fun getMaxAge() = 3

    override fun createBlockState() = BlockStateContainer(this, AGE)

    override fun getAgeProperty() = AGE

    override fun canUseBonemeal(worldIn: World, rand: Random, pos: BlockPos, state: IBlockState) = false

    override fun getPlantType(world: IBlockAccess, pos: BlockPos) = XORCITE

    override fun getPlant(world: IBlockAccess?, pos: BlockPos?): IBlockState {
        if (world == null || pos == null) return defaultState
        val state = world.getBlockState(pos)
        return if (state.block != this) defaultState else state
    }

    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        if (isMaxAge(state)) return
        if (!worldIn.isAreaLoaded(pos, 1)) return

        // TODO: 17/11/2023 add config options
        val existing = BlockPos.getAllInBox(pos.add(2, 2, 2), pos.add(-2, -2, -2)).asSequence()
            .filter { worldIn.getBlockState(it).block == ModBlocks.ENTROPY_CROP_BLOCK }
            .take(6).count()
        if (existing < 6) {
            BlockPos.getAllInBox(pos.add(1, 1, 1), pos.add(-1, -1, -1))
                .filter { worldIn.getBlockState(it).block == Blocks.END_STONE }
                .shuffled().take(3)
                .forEach { worldIn.setBlockState(it, defaultState) }
        }

        if (rand.nextInt(if (worldIn.provider.dimension == 1) 2 else 10) != 0) return
        grow(worldIn, pos, state)
    }

    override fun grow(worldIn: World, pos: BlockPos, state: IBlockState) {
        val age = getAge(state)
        if (age >= 3) return
        worldIn.setBlockState(pos, withAge(age + 1), 2)
    }

    override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int) {
        if (isMaxAge(state)) addAdditionalDrops(fortune, drops, if (world is World) world.rand else RANDOM)
    }

    override fun dropBlockAsItemWithChance(worldIn: World, pos: BlockPos, state: IBlockState, chance: Float, fortune: Int) {
        // do not drop items while restoring blockstates, prevents item dupe
        if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) {
            val drops = getDrops(worldIn, pos, state, fortune)
            val finalChance = ForgeEventFactory.fireBlockHarvesting(drops, worldIn, pos, state, fortune, chance, false, harvesters.get())
            for (drop in drops) if (worldIn.rand.nextFloat() <= finalChance) spawnAsEntity(worldIn, pos, drop)
        }
    }

    override fun onBlockActivated(
        worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer,
        hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float,
    ): Boolean {
        if (!isMaxAge(state)) return false
        if (worldIn.isRemote) return true
        worldIn.setBlockState(pos, defaultState)
        val fortune = EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FORTUNE, playerIn)
        for (drop in addAdditionalDrops(fortune, ArrayList(), worldIn.rand)) spawnAsEntity(worldIn, pos, drop)
        return true
    }

    private fun <T : MutableList<ItemStack>> addAdditionalDrops(fortune: Int, drops: T, random: Random): T {
        drops.add(ItemStack(ModItems.ENTROPY_SHARD, 1))
        if (random.nextInt(20) <= 1 + fortune) drops.add(ItemStack(ModItems.ENTROPY_SHARD, 1))
        return drops
    }

    override fun canSustainPlant(
        state: IBlockState, world: IBlockAccess, pos: BlockPos, direction: EnumFacing, plantable: IPlantable,
    ) = state.block == ModBlocks.ENTROPY_CROP_BLOCK || super.canSustainPlant(state, world, pos, direction, plantable)

    override fun canSustainBush(state: IBlockState) = true

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos) = ESSENCE_AABB

    override fun getRenderLayer() = BlockRenderLayer.CUTOUT_MIPPED

    override fun getCollisionBoundingBox(blockState: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB? =
        ESSENCE_AABB

    override fun isOpaqueCube(state: IBlockState) = true

    override fun isFullCube(state: IBlockState) = true

    override fun canPlaceBlockAt(worldIn: World, pos: BlockPos) = true

    override fun canBlockStay(worldIn: World, pos: BlockPos, state: IBlockState) = true

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
                worldIn.spawnParticle(EnumParticleTypes.PORTAL, px, py, pz, 0.0, 0.5, 0.0)
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

    override fun handleAssets() {
        val item = Item.getItemFromBlock(this)
        EndReforked.getProxy().registerItemRenderer(item, 0, "inventory")
        EndReforked.getProxy().registerItemRenderer(item, 3, "inventory")
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, player: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        tooltip.add(I18n.format("tile.dragon_essence.tooltip"))
    }

    companion object {
        @JvmField
        val XORCITE: EnumPlantType = EnumPlantType.getPlantType("xorcite")

        @JvmField
        val AGE: PropertyInteger = PropertyInteger.create("age", 0, 3)

        private val ESSENCE_AABB = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
    }
}

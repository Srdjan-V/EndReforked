package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.entity.EntityLord
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockWorldState
import net.minecraft.block.state.IBlockState
import net.minecraft.block.state.pattern.BlockMaterialMatcher
import net.minecraft.block.state.pattern.BlockPattern
import net.minecraft.block.state.pattern.BlockStateMatcher
import net.minecraft.block.state.pattern.FactoryBlockPattern
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class BlockRune(name: String, material: Material) : Block(material), InventoryBlockModel {

    private val pattern: BlockPattern by lazy {
        FactoryBlockPattern.start().aisle("###", "#^#", "###")
            .where('^', BlockWorldState.hasState(BlockStateMatcher.forBlock(ModBlocks.RUNE_BLOCK.get())))
            .where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.PURPUR_BLOCK)))
            .where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build()
    }

    init {
        setTranslationKey(name)
        setRegistryName(name)
        setHardness(5.0f)
        setCreativeTab(EndReforked.ENDERTAB)
    }

    override fun onBlockAdded(world: World, pos: BlockPos, state: IBlockState) {
        super.onBlockAdded(world, pos, state)
        world.playSound(
            null, pos, SoundEvents.ENTITY_ENDERMEN_AMBIENT, SoundCategory.BLOCKS, 0.5f,
            2.6f + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8f,
        )
        trySpawnBoss(world, pos)
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        tooltip.add(I18n.format("tile.block_rune.tooltip"))
    }

    private fun trySpawnBoss(world: World, pos: BlockPos) {
        val helper = pattern.match(world, pos) ?: return
        val cells = (0 until pattern.palmLength).flatMap { j -> (0 until pattern.thumbLength).map { k -> j to k } }
        for ((j, k) in cells) world.setBlockState(helper.translateOffset(j, k, 0).pos, Blocks.AIR.defaultState, 2)

        val blockPos = helper.translateOffset(1, 2, 0).pos
        val construct = EntityLord(world)
        construct.setLocationAndAngles(blockPos.x + 0.5, blockPos.y + 1.0, blockPos.z + 0.5, 0.0f, 0.0f)
        world.spawnEntity(construct)

        for (player in world.getEntitiesWithinAABB(EntityPlayerMP::class.java, construct.entityBoundingBox.grow(5.0))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(player, construct)
        }
        repeat(120) {
            world.spawnParticle(
                EnumParticleTypes.SNOWBALL, blockPos.x + world.rand.nextDouble(),
                blockPos.y + world.rand.nextDouble() * 3.9, blockPos.z + world.rand.nextDouble(), 0.0, 0.0, 0.0,
            )
        }
        for ((j, k) in cells) world.notifyNeighborsRespectDebug(helper.translateOffset(j, k, 0).pos, Blocks.AIR, false)
    }
}

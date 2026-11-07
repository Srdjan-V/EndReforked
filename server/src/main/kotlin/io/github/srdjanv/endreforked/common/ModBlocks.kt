package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.common.blocks.*
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockBush
import io.github.srdjanv.endreforked.common.blocks.base.BlockEndBase
import io.github.srdjanv.endreforked.common.blocks.base.BlockStairsBase
import io.github.srdjanv.endreforked.common.blocks.base.BlockWallBase
import io.github.srdjanv.endreforked.common.fluids.blocks.BlockFluidEndMagma
import io.github.srdjanv.endreforked.common.fluids.blocks.BlockFluidEntropy
import io.github.srdjanv.endreforked.common.fluids.blocks.BlockFluidOrgana
import io.github.srdjanv.endreforked.utils.models.IAsset
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ModBlocks {
    private val BLOCKS = Registry<Block>()

    @JvmField val END_STONE_SMOOTH_BLOCK: Block = register { BlockEndBase("end_stone_smooth_block", Material.ROCK) }
    @JvmField val END_STONE_SMOOTH_STAIRS: Block =
        register { BlockStairsBase("end_stone_smooth_stairs", END_STONE_SMOOTH_BLOCK) }
    @JvmField val END_STONE_SMOOTH_WALL: Block =
        register { BlockWallBase("end_stone_smooth_wall", END_STONE_SMOOTH_BLOCK) }

    @JvmField val END_STONE_PILLAR: Block = register { BlockPillar("end_stone_pillar", Material.ROCK) }
    @JvmField val END_BRICKS_CHISELED: Block = register { BlockEndBase("end_bricks_chiseled", Material.ROCK) }

    @JvmField val END_BRICKS_STAIRS: Block = register { BlockStairsBase("end_bricks_stairs", Blocks.END_BRICKS) }
    @JvmField val END_BRICKS_WALL: Block = register { BlockWallBase("end_bricks_wall", Blocks.END_BRICKS) }

    @JvmField val PURPUR_WALL: Block = register { BlockWallBase("purpur_wall", Blocks.PURPUR_BLOCK) }
    @JvmField val PURPUR_LAMP: Block = register { BlockPurpurLamp("purpur_lamp_block") }

    @JvmField val OBSIDIAN_ESSENCE_ORE: Block = register(::BlockObsidianEssence)
    @JvmField val PHANTOM_BLOCK: Block = register { BlockPhantom("phantom_block", Material.BARRIER, false) }

    @JvmField val ENTROPY_END_STONE: BlockEntropyEndStone = register(::BlockEntropyEndStone)
    @JvmField val ENTROPY_FIRE: BlockEntropyFire = register(::BlockEntropyFire)
    @JvmField val END_CORAL: BaseBlockBush = register(::BlockEndCoral)
    @JvmField val END_MAGMA_BLOCK: Block = register(::BlockEndMagma)

    @JvmField val LORMYTE_CRYSTAL_BLOCK: Block = register { BlockEndBase("lormyte_crystal_block", Material.ROCK) }
    @JvmField val DECORATIVE_LORMYTE_BLOCK: Block =
        register { BlockEndBase("decorative_lormyte_block", Material.ROCK) }

    @JvmField val ENDORIUM_BLOCK: Block = register { BlockEndBase("endorium_block", Material.IRON) }

    // tungsten
    @JvmField val TUNGSTEN_BLOCK: Block = register { BlockEndBase("tungsten_block", Material.IRON) }
    @JvmField val TUNGSTEN_ORE: Block = register { TungstenOre("tungsten_ore", Material.ROCK) }
    @JvmField val TUNGSTEN_END_ORE: Block = register { TungstenOre("tungsten_end_ore", Material.ROCK) }

    // tiles
    @JvmField val BLOCK_END_FORGE: Block = register { BlockEndForge("end_forge_block") }
    @JvmField val MATERIALIZER_BLOCK: Block = register { BlockMaterializer("materializer_block") }
    @JvmField val SMALL_ENTROPY_BATTERY_BLOCK: BlockSmallEntropyBattery = register(::BlockSmallEntropyBattery)
    @JvmField val ENTROPY_CHAMBER: BlockEntropyChamber = register(::BlockEntropyChamber)

    @JvmField val RUNE_BLOCK: Block = register { BlockRune("rune_block", Material.ROCK) }

    @JvmField val DRAGONITE_CROP: BlockDragoniteCrop = register(::BlockDragoniteCrop)
    // todo data fix old:xorcite_block
    @JvmField val ENTROPY_CROP_BLOCK: Block = register(::BlockEntropyCrop)

    @JvmField val ENDER_FLOWER_CROP: BlockEnderCrop = register(::BlockEnderCrop)
    @JvmField val ENDER_FLOWER_CROP_DEAD: BlockEnderCropDead = register(::BlockEnderCropDead)

    // todo fixup
    @JvmField val ORGANA_WEED_BLOCK: BlockOrganaWeed = register(::BlockOrganaWeed)

    // fluids
    @JvmField val FLUID_END_MAGMA_BLOCK: BlockFluidEndMagma = register(::BlockFluidEndMagma)
    @JvmField val FLUID_ENTROPY_BLOCK: BlockFluidEntropy = register(::BlockFluidEntropy)
    @JvmField val FLUID_ORGANA_BLOCK: BlockFluidOrgana = register(::BlockFluidOrgana)

    @JvmField val END_MOSS_GRASS_BLOCK: BlockEndMossGrass = register(::BlockEndMossGrass)
    @JvmField val END_MOSS_BLOCK: BlockEndMoss = register(::BlockEndMoss)

    @JvmField val ORGANA_FLOWER_BLOCK: BlockOrganaFlower = register(::BlockOrganaFlower)
    @JvmField val ORGANA_PLANT_BLOCK: BlockOrganaPlant = register(::BlockOrganaPlant)

    @JvmField val ORGANA_FLOWER_STEM_BLOCK: BlockOrganaFlowerStem = register(::BlockOrganaFlowerStem)
    @JvmField val ORGANA_FLOWER_STEM_DEAD_BLOCK: BlockOrganaFlowerStemDead = register(::BlockOrganaFlowerStemDead)

    private fun <B : Block> register(factory: () -> B): B = factory().also { BLOCKS.register(it) }
    @SubscribeEvent
    @JvmStatic
    fun onBlockRegister(event: RegistryEvent.Register<Block>) {
        BLOCKS.all().forEach(event.registry::register)
    }

    @SubscribeEvent
    @JvmStatic
    fun registerModels(event: ModelRegistryEvent) {
        BLOCKS.all().filterIsInstance<IAsset>().forEach { it.handleAssets() }
    }
}

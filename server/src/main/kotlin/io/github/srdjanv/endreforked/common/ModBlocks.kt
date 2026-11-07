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
    private val BLOCKS = DeferredRegistry(this, Block::class.java)

    @get:JvmStatic val END_STONE_SMOOTH_BLOCK by BLOCKS.register { BlockEndBase("end_stone_smooth_block", Material.ROCK) }
    @get:JvmStatic val END_STONE_SMOOTH_STAIRS by BLOCKS.register { BlockStairsBase("end_stone_smooth_stairs", END_STONE_SMOOTH_BLOCK) }
    @get:JvmStatic val END_STONE_SMOOTH_WALL by BLOCKS.register { BlockWallBase("end_stone_smooth_wall", END_STONE_SMOOTH_BLOCK) }

    @get:JvmStatic val END_STONE_PILLAR by BLOCKS.register { BlockPillar("end_stone_pillar", Material.ROCK) }
    @get:JvmStatic val END_BRICKS_CHISELED by BLOCKS.register { BlockEndBase("end_bricks_chiseled", Material.ROCK) }

    @get:JvmStatic val END_BRICKS_STAIRS by BLOCKS.register { BlockStairsBase("end_bricks_stairs", Blocks.END_BRICKS) }
    @get:JvmStatic val END_BRICKS_WALL by BLOCKS.register { BlockWallBase("end_bricks_wall", Blocks.END_BRICKS) }

    @get:JvmStatic val PURPUR_WALL by BLOCKS.register { BlockWallBase("purpur_wall", Blocks.PURPUR_BLOCK) }
    @get:JvmStatic val PURPUR_LAMP by BLOCKS.register { BlockPurpurLamp("purpur_lamp_block") }

    @get:JvmStatic val OBSIDIAN_ESSENCE_ORE by BLOCKS.register(::BlockObsidianEssence)
    @get:JvmStatic val PHANTOM_BLOCK by BLOCKS.register { BlockPhantom("phantom_block", Material.BARRIER, false) }

    @get:JvmStatic val ENTROPY_END_STONE by BLOCKS.register(::BlockEntropyEndStone)
    @get:JvmStatic val ENTROPY_FIRE by BLOCKS.register(::BlockEntropyFire)
    @get:JvmStatic val END_CORAL by BLOCKS.register(::BlockEndCoral)
    @get:JvmStatic val END_MAGMA_BLOCK by BLOCKS.register(::BlockEndMagma)

    @get:JvmStatic val LORMYTE_CRYSTAL_BLOCK by BLOCKS.register { BlockEndBase("lormyte_crystal_block", Material.ROCK) }
    @get:JvmStatic val DECORATIVE_LORMYTE_BLOCK by BLOCKS.register { BlockEndBase("decorative_lormyte_block", Material.ROCK) }

    @get:JvmStatic val ENDORIUM_BLOCK by BLOCKS.register { BlockEndBase("endorium_block", Material.IRON) }

    // tungsten
    @get:JvmStatic val TUNGSTEN_BLOCK by BLOCKS.register { BlockEndBase("tungsten_block", Material.IRON) }
    @get:JvmStatic val TUNGSTEN_ORE by BLOCKS.register { TungstenOre("tungsten_ore", Material.ROCK) }
    @get:JvmStatic val TUNGSTEN_END_ORE by BLOCKS.register { TungstenOre("tungsten_end_ore", Material.ROCK) }

    // tiles
    @get:JvmStatic val BLOCK_END_FORGE by BLOCKS.register { BlockEndForge("end_forge_block") }
    @get:JvmStatic val MATERIALIZER_BLOCK by BLOCKS.register { BlockMaterializer("materializer_block") }
    @get:JvmStatic val SMALL_ENTROPY_BATTERY_BLOCK by BLOCKS.register(::BlockSmallEntropyBattery)
    @get:JvmStatic val ENTROPY_CHAMBER by BLOCKS.register(::BlockEntropyChamber)

    @get:JvmStatic val RUNE_BLOCK by BLOCKS.register { BlockRune("rune_block", Material.ROCK) }

    @get:JvmStatic val DRAGONITE_CROP by BLOCKS.register(::BlockDragoniteCrop)
    // todo data fix old:xorcite_block
    @get:JvmStatic val ENTROPY_CROP_BLOCK by BLOCKS.register(::BlockEntropyCrop)

    @get:JvmStatic val ENDER_FLOWER_CROP by BLOCKS.register(::BlockEnderCrop)
    @get:JvmStatic val ENDER_FLOWER_CROP_DEAD by BLOCKS.register(::BlockEnderCropDead)

    // todo fixup
    @get:JvmStatic val ORGANA_WEED_BLOCK by BLOCKS.register(::BlockOrganaWeed)

    // fluids
    @get:JvmStatic val FLUID_END_MAGMA_BLOCK by BLOCKS.register(::BlockFluidEndMagma)
    @get:JvmStatic val FLUID_ENTROPY_BLOCK by BLOCKS.register(::BlockFluidEntropy)
    @get:JvmStatic val FLUID_ORGANA_BLOCK by BLOCKS.register(::BlockFluidOrgana)

    @get:JvmStatic val END_MOSS_GRASS_BLOCK by BLOCKS.register(::BlockEndMossGrass)
    @get:JvmStatic val END_MOSS_BLOCK by BLOCKS.register(::BlockEndMoss)

    @get:JvmStatic val ORGANA_FLOWER_BLOCK by BLOCKS.register(::BlockOrganaFlower)
    @get:JvmStatic val ORGANA_PLANT_BLOCK by BLOCKS.register(::BlockOrganaPlant)

    @get:JvmStatic val ORGANA_FLOWER_STEM_BLOCK by BLOCKS.register(::BlockOrganaFlowerStem)
    @get:JvmStatic val ORGANA_FLOWER_STEM_DEAD_BLOCK by BLOCKS.register(::BlockOrganaFlowerStemDead)

    @SubscribeEvent
    @JvmStatic
    fun onBlockRegister(event: RegistryEvent.Register<Block>) {
        BLOCKS.registerAll(event.registry)
    }

    @SubscribeEvent
    @JvmStatic
    fun registerModels(event: ModelRegistryEvent) {
        BLOCKS.all().filterIsInstance<IAsset>().forEach { it.handleAssets() }
    }
}

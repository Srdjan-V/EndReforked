package io.github.srdjanv.endreforked.common

import com.google.common.base.Suppliers
import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.blocks.*
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockBush
import io.github.srdjanv.endreforked.common.blocks.base.BlockEndBase
import io.github.srdjanv.endreforked.common.blocks.base.BlockStairsBase
import io.github.srdjanv.endreforked.common.blocks.base.BlockWallBase
import io.github.srdjanv.endreforked.common.configs.Configs
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
import java.util.function.Supplier

object ModBlocks {
    private val BLOCKS = mutableListOf<Supplier<out Block?>>()

    @JvmField val END_STONE_SMOOTH_BLOCK: Supplier<Block> = maybeRegister { BlockEndBase("end_stone_smooth_block", Material.ROCK) }
    @JvmField val END_STONE_SMOOTH_STAIRS: Supplier<Block> =
        maybeRegister { BlockStairsBase("end_stone_smooth_stairs", END_STONE_SMOOTH_BLOCK.get()) }
    @JvmField val END_STONE_SMOOTH_WALL: Supplier<Block> =
        maybeRegister { BlockWallBase("end_stone_smooth_wall", END_STONE_SMOOTH_BLOCK.get()) }

    @JvmField val END_STONE_PILLAR: Supplier<Block> = maybeRegister { BlockPillar("end_stone_pillar", Material.ROCK) }
    @JvmField val END_BRICKS_CHISELED: Supplier<Block> = maybeRegister { BlockEndBase("end_bricks_chiseled", Material.ROCK) }

    @JvmField val END_BRICKS_STAIRS: Supplier<Block> = maybeRegister { BlockStairsBase("end_bricks_stairs", Blocks.END_BRICKS) }
    @JvmField val END_BRICKS_WALL: Supplier<Block> = maybeRegister { BlockWallBase("end_bricks_wall", Blocks.END_BRICKS) }

    @JvmField val PURPUR_WALL: Supplier<Block> = maybeRegister { BlockWallBase("purpur_wall", Blocks.PURPUR_BLOCK) }
    @JvmField val PURPUR_LAMP: Supplier<Block> = maybeRegister { BlockPurpurLamp("purpur_lamp_block") }

    @JvmField val OBSIDIAN_ESSENCE_ORE: Supplier<Block> = register(::BlockObsidianEssence)
    @JvmField val PHANTOM_BLOCK: Supplier<Block> = maybeRegister { BlockPhantom("phantom_block", Material.BARRIER, false) }

    @JvmField val ENTROPY_END_STONE: Supplier<BlockEntropyEndStone> = register(::BlockEntropyEndStone)
    @JvmField val ENTROPY_FIRE: Supplier<BlockEntropyFire> = register(::BlockEntropyFire)
    @JvmField val END_CORAL: Supplier<BaseBlockBush> = register(::BlockEndCoral)
    @JvmField val END_MAGMA_BLOCK: Supplier<Block> = register(::BlockEndMagma)

    @JvmField val LORMYTE_CRYSTAL_BLOCK: Supplier<Block> = maybeRegister { BlockEndBase("lormyte_crystal_block", Material.ROCK) }
    @JvmField val DECORATIVE_LORMYTE_BLOCK: Supplier<Block> =
        maybeRegister { BlockEndBase("decorative_lormyte_block", Material.ROCK) }

    @JvmField val ENDORIUM_BLOCK: Supplier<Block> = maybeRegister { BlockEndBase("endorium_block", Material.IRON) }

    // tungsten
    @JvmField val TUNGSTEN_BLOCK: Supplier<Block> = maybeRegister { BlockEndBase("tungsten_block", Material.IRON) }
    @JvmField val TUNGSTEN_ORE: Supplier<Block> = maybeRegister { TungstenOre("tungsten_ore", Material.ROCK) }
    @JvmField val TUNGSTEN_END_ORE: Supplier<Block> = maybeRegister { TungstenOre("tungsten_end_ore", Material.ROCK) }

    // tiles
    @JvmField val BLOCK_END_FORGE: Supplier<Block> = maybeRegister { BlockEndForge("end_forge_block") }
    @JvmField val MATERIALIZER_BLOCK: Supplier<Block> = maybeRegister { BlockMaterializer("materializer_block") }
    @JvmField val SMALL_ENTROPY_BATTERY_BLOCK: Supplier<BlockSmallEntropyBattery> = register(::BlockSmallEntropyBattery)
    @JvmField val ENTROPY_CHAMBER: Supplier<BlockEntropyChamber> = register(::BlockEntropyChamber)

    @JvmField val RUNE_BLOCK: Supplier<Block> = maybeRegister { BlockRune("rune_block", Material.ROCK) }

    @JvmField val DRAGONITE_CROP: Supplier<BlockDragoniteCrop> = maybeRegister(::BlockDragoniteCrop)
    // todo data fix old:xorcite_block
    @JvmField val ENTROPY_CROP_BLOCK: Supplier<Block> = register(::BlockEntropyCrop)

    @JvmField val ENDER_FLOWER_CROP: Supplier<BlockEnderCrop> = register(::BlockEnderCrop)
    @JvmField val ENDER_FLOWER_CROP_DEAD: Supplier<BlockEnderCropDead> = register(::BlockEnderCropDead)

    // todo fixup
    @JvmField val ORGANA_WEED_BLOCK: Supplier<BlockOrganaWeed> = register(::BlockOrganaWeed)

    // fluids
    @JvmField val FLUID_END_MAGMA_BLOCK: Supplier<BlockFluidEndMagma> = register(::BlockFluidEndMagma)
    @JvmField val FLUID_ENTROPY_BLOCK: Supplier<BlockFluidEntropy> = register(::BlockFluidEntropy)
    @JvmField val FLUID_ORGANA_BLOCK: Supplier<BlockFluidOrgana> = register(::BlockFluidOrgana)

    @JvmField val END_MOSS_GRASS_BLOCK: Supplier<BlockEndMossGrass> = register(::BlockEndMossGrass)
    @JvmField val END_MOSS_BLOCK: Supplier<BlockEndMoss> = register(::BlockEndMoss)

    @JvmField val ORGANA_FLOWER_BLOCK: Supplier<BlockOrganaFlower> = register(::BlockOrganaFlower)
    @JvmField val ORGANA_PLANT_BLOCK: Supplier<BlockOrganaPlant> = register(::BlockOrganaPlant)

    @JvmField val ORGANA_FLOWER_STEM_BLOCK: Supplier<BlockOrganaFlowerStem> = register(::BlockOrganaFlowerStem)
    @JvmField val ORGANA_FLOWER_STEM_DEAD_BLOCK: Supplier<BlockOrganaFlowerStemDead> = register(::BlockOrganaFlowerStemDead)

    private fun <B : Block> register(factory: () -> B): Supplier<B> =
        Suppliers.memoize { factory() }.also { BLOCKS += it }

    // The supplier yields null for a block disabled in the config.
    @Suppress("UNCHECKED_CAST")
    private fun <B : Block> maybeRegister(factory: () -> B): Supplier<B> =
        Suppliers.memoize<B?> {
            val block = factory()
            if (block.registryName!!.path in Configs.SERVER_SIDE_CONFIGS.disabledBlocks) {
                EndReforked.LOGGER.info("Skipping registration of Block: '{}'", block.registryName)
                null
            } else block
        }.also { BLOCKS += it } as Supplier<B>

    @SubscribeEvent
    @JvmStatic
    fun onBlockRegister(event: RegistryEvent.Register<Block>) {
        BLOCKS.mapNotNull { it.get() }.forEach(event.registry::register)
    }

    @SubscribeEvent
    @JvmStatic
    fun registerModels(event: ModelRegistryEvent) {
        BLOCKS.mapNotNull { it.get() }.filterIsInstance<IAsset>().forEach { it.handleAssets() }
    }
}

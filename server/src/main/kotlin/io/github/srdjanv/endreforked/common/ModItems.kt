package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.common.configs.Configs
import io.github.srdjanv.endreforked.common.items.*
import io.github.srdjanv.endreforked.common.items.base.*
import io.github.srdjanv.endreforked.common.items.tools.*
import io.github.srdjanv.endreforked.utils.models.IAsset
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.Item.ToolMaterial
import net.minecraft.item.ItemBlock
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.common.util.EnumHelper
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ModItems {
    private val ITEMS = Registry<Item>()

    // Materials
    @JvmField val WAND_MATERIAL: ToolMaterial =
        EnumHelper.addToolMaterial("entropy_wand", 4, 256, 5.5F, 1.0F, 13)!!


    // Items
    @JvmField val INGOT_ENDORIUM: Item = register { ItemBase("item_ingot_endorium") }
    @JvmField val SHARD_OBSIDIAN: Item = register { ItemBase("item_shard_obsidian") }
    @JvmField val RAW_ENDORIUM: Item = register { ItemBase("item_raw_endorium") }
    @JvmField val END_ESSENCE: Item = register { ItemBase("item_end_essence") }
    @JvmField val END_SHARD: Item = register { ItemBase("item_end_shard") }
    @JvmField val END_RUNE: Item = register { ItemLegendary("item_end_rune") }
    @JvmField val ITEM_LORMYTE_CRYSTAL: Item = register { ItemBase("item_lormyte_crystal") }
    @JvmField val TUNGSTEN_INGOT: Item = register { ItemBase("tungsten_ingot") }
    @JvmField val ENDER_STRING: Item = register { ItemLegendary("item_ender_string") }

    @JvmField val ANGEL_FEATHER: Item = register { ItemBase("item_angel_feather") }
    @JvmField val DRAGON_SCALES: Item = register { ItemBase("dragon_scales") }
    // todo data fix old:xorcite_shard
    @JvmField val ENTROPY_SHARD: Item = register { ItemBase("entropy_shard") }
    @JvmField val INFUSED_METALL: Item = register { ItemBase("ingot_infused") }
    @JvmField val SWORD_SHARD: Item = register { ItemBase("sword_shard") }
    @JvmField val CATALYST: Item = register { ItemCatalyst("catalyst") }
    @JvmField val TUNGSTEN_NUGGET: Item = register { ItemBase("tungsten_nugget") }

    @JvmField val ENTROPY_RANGE_UPGRADE_TWO: ItemEntropyRadiusUpgrade =
        register { ItemEntropyRadiusUpgrade(EntropyRadius.TWO) }
    @JvmField val ENTROPY_RANGE_UPGRADE_THREE: ItemEntropyRadiusUpgrade =
        register { ItemEntropyRadiusUpgrade(EntropyRadius.THREE) }

    // Tools
    @JvmField val ENTROPY_WAND: Item = register { ToolEntropyWand("entropy_wand", WAND_MATERIAL) }

    @JvmField val ENTROPY_READER: ItemEntropyReader = register(::ItemEntropyReader)
    @JvmField val LORMYTE_CHARM: ItemLormyteCharm = register(::ItemLormyteCharm)
    @JvmField val ENTROPY_CELL: ItemEntropyCell = register(::ItemEntropyCell)
    @JvmField val ENTROPY_WINGS: ItemEntropyWings = register(::ItemEntropyWings)

    // Food
    @JvmField val ENDER_FLESH: ItemEnderFlesh = register(::ItemEnderFlesh)
    @JvmField val DRAGONITE_BERRIES: ItemDragoniteBerries = register(::ItemDragoniteBerries)
    @JvmField val DRAGONITE_TEA: ItemDragoniteTea = register(::ItemDragoniteTea)
    @JvmField val ORGANA_FRUIT: ItemOrganaFruit = register(::ItemOrganaFruit)
    @JvmField val CHORUS_SOUP: ItemChorusSoup = register(::ItemChorusSoup)

    // TODO: 22/10/2023 rename itemBlocks
    @JvmField val BLOCK_ENDORIUM: ItemBlock = registerItemBlock(ModBlocks.ENDORIUM_BLOCK)
    @JvmField val END_STONE_SMOOTH: ItemBlock = registerItemBlock(ModBlocks.END_STONE_SMOOTH_BLOCK)
    @JvmField val END_STONE_PILLAR: ItemBlock = registerItemBlock(ModBlocks.END_STONE_PILLAR)
    @JvmField val PURPUR_LAMP: ItemBlock = registerItemBlock(ModBlocks.PURPUR_LAMP)
    @JvmField val ENDER_FLOWER_CROP: ItemBlock = registerItemBlock(ModBlocks.ENDER_FLOWER_CROP)
    @JvmField val DRAGON_BUSH: ItemBlock = registerItemBlock(ModBlocks.DRAGONITE_CROP)
    @JvmField val OBSIDIAN_ESSENCE_ORE: ItemBlock = registerItemBlock(ModBlocks.OBSIDIAN_ESSENCE_ORE)
    @JvmField val PHANTOM_BLOCK: ItemBlock = registerItemBlock(ModBlocks.PHANTOM_BLOCK)
    @JvmField val ENTROPY_END_STONE: ItemBlock = registerItemBlock(ModBlocks.ENTROPY_END_STONE)
    @JvmField val LORMYTE_CRYSTAL: ItemBlock = registerItemBlock(ModBlocks.LORMYTE_CRYSTAL_BLOCK)
    @JvmField val DECORATIVE_LORMYTE: ItemBlock = registerItemBlock(ModBlocks.DECORATIVE_LORMYTE_BLOCK)
    @JvmField val TUNGSTEN_BLOCK: ItemBlock = registerItemBlock(ModBlocks.TUNGSTEN_BLOCK)
    @JvmField val TUNGSTEN_ORE: ItemBlock = registerItemBlock(ModBlocks.TUNGSTEN_ORE)
    @JvmField val TUNGSTEN_END_ORE: ItemBlock = registerItemBlock(ModBlocks.TUNGSTEN_END_ORE)
    @JvmField val BLOCK_RUNE: ItemBlock = registerItemBlock(ModBlocks.RUNE_BLOCK)
    @JvmField val BLOCK_END_MAGMA: ItemBlock = registerItemBlock(ModBlocks.END_MAGMA_BLOCK)
    @JvmField val BLOCK_END_FORGE: ItemBlock = registerItemBlock(ModBlocks.BLOCK_END_FORGE)
    @JvmField val ENTROPY_CHAMBER: ItemBlock = registerItemBlock(ModBlocks.ENTROPY_CHAMBER)
    @JvmField val ENTROPY_CROP_BLOCK: BaseMetaItemBlock =
        registerItemBlock(ModBlocks.ENTROPY_CROP_BLOCK) { block ->
            BaseMetaItemBlock(block) { meta -> if (meta == 3) "mature" else null }
        }
    @JvmField val BLOCK_E_USER: ItemBlock = registerItemBlock(ModBlocks.MATERIALIZER_BLOCK)
    @JvmField val SMALL_ENTROPY_BATTERY_BLOCK: ItemBlock = registerItemBlock(ModBlocks.SMALL_ENTROPY_BATTERY_BLOCK)

    @JvmField val END_STONE_CHISELED: ItemBlock = registerItemBlock(ModBlocks.END_BRICKS_CHISELED)
    @JvmField val COLD_FIRE: ItemBlock = registerItemBlock(ModBlocks.ENTROPY_FIRE)
    @JvmField val STAIRS_END_BRICKS: ItemBlock = registerItemBlock(ModBlocks.END_BRICKS_STAIRS)
    @JvmField val STAIRS_SMOOTH_END_STONE: ItemBlock = registerItemBlock(ModBlocks.END_STONE_SMOOTH_STAIRS)
    @JvmField val WALL_END_BRICKS: ItemBlock = registerItemBlock(ModBlocks.END_BRICKS_WALL)
    @JvmField val WALL_PURPUR: ItemBlock = registerItemBlock(ModBlocks.PURPUR_WALL)
    @JvmField val WALL_SMOOTH_END_STONE: ItemBlock = registerItemBlock(ModBlocks.END_STONE_SMOOTH_WALL)
    @JvmField val END_CORAL: ItemBlock = registerItemBlock(ModBlocks.END_CORAL)

    @JvmField val ORGANA_WEED_BLOCK: ItemBlock = registerItemBlock(ModBlocks.ORGANA_WEED_BLOCK)
    @JvmField val END_MOSS_GRASS_BLOCK: ItemBlock = registerItemBlock(ModBlocks.END_MOSS_GRASS_BLOCK)
    @JvmField val END_MOSS_BLOCK: ItemBlock = registerItemBlock(ModBlocks.END_MOSS_BLOCK)

    @JvmField val ORGANA_FLOWER_SEED: Item = register(::ItemOrganaFlowerSeed)
    @JvmField val ORGANA_PLANT_BLOCK: ItemBlock = registerItemBlock(ModBlocks.ORGANA_PLANT_BLOCK)
    @JvmField val ORGANA_FLOWER: BaseMetaItemBlock =
        registerItemBlock(ModBlocks.ORGANA_FLOWER_BLOCK) { block ->
            BaseMetaItemBlock(block) { meta ->
                when (meta) {
                    10 -> $$"mature$big"
                    8 -> $$"mature$small"
                    else -> null
                }
            }
        }

    private fun <I : Item> register(factory: () -> I): I = factory().also { ITEMS.register(it) }

    private fun <B : Block> registerItemBlock(block: B): ItemBlock =
        registerItemBlock(block) { ItemBlock(it) }

    private fun <I : ItemBlock, B : Block> registerItemBlock(block: B, factory: (B) -> I): I =
        factory(block).apply { registryName = block.registryName; ITEMS.register(this) }

    @SubscribeEvent
    @JvmStatic
    fun onItemRegister(event: RegistryEvent.Register<Item>) {
        ITEMS.all().forEach(event.registry::register)
    }

    @SubscribeEvent
    @JvmStatic
    fun registerModels(event: ModelRegistryEvent) {
        ITEMS.all().filterIsInstance<IAsset>().forEach { it.handleAssets() }
    }
}

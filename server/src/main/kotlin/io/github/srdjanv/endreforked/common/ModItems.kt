package io.github.srdjanv.endreforked.common

import com.google.common.base.Suppliers
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
import java.util.function.Supplier

object ModItems {
    private val ITEMS = mutableListOf<Supplier<out Item?>>()

    // Materials
    @JvmField val WAND_MATERIAL: Supplier<ToolMaterial> =
        Suppliers.memoize { EnumHelper.addToolMaterial("entropy_wand", 4, 256, 5.5F, 1.0F, 13)!! }


    // Items
    @JvmField val INGOT_ENDORIUM: Supplier<Item> = maybeRegister { ItemBase("item_ingot_endorium") }
    @JvmField val SHARD_OBSIDIAN: Supplier<Item> = maybeRegister { ItemBase("item_shard_obsidian") }
    @JvmField val RAW_ENDORIUM: Supplier<Item> = maybeRegister { ItemBase("item_raw_endorium") }
    @JvmField val END_ESSENCE: Supplier<Item> = maybeRegister { ItemBase("item_end_essence") }
    @JvmField val END_SHARD: Supplier<Item> = maybeRegister { ItemBase("item_end_shard") }
    @JvmField val END_RUNE: Supplier<Item> = maybeRegister { ItemLegendary("item_end_rune") }
    @JvmField val ITEM_LORMYTE_CRYSTAL: Supplier<Item> = maybeRegister { ItemBase("item_lormyte_crystal") }
    @JvmField val TUNGSTEN_INGOT: Supplier<Item> = maybeRegister { ItemBase("tungsten_ingot") }
    @JvmField val ENDER_STRING: Supplier<Item> = maybeRegister { ItemLegendary("item_ender_string") }

    @JvmField val ANGEL_FEATHER: Supplier<Item> = maybeRegister { ItemBase("item_angel_feather") }
    @JvmField val DRAGON_SCALES: Supplier<Item> = maybeRegister { ItemBase("dragon_scales") }
    // todo data fix old:xorcite_shard
    @JvmField val ENTROPY_SHARD: Supplier<Item> = register { ItemBase("entropy_shard") }
    @JvmField val INFUSED_METALL: Supplier<Item> = maybeRegister { ItemBase("ingot_infused") }
    @JvmField val SWORD_SHARD: Supplier<Item> = maybeRegister { ItemBase("sword_shard") }
    @JvmField val CATALYST: Supplier<Item> = maybeRegister { ItemCatalyst("catalyst") }
    @JvmField val TUNGSTEN_NUGGET: Supplier<Item> = maybeRegister { ItemBase("tungsten_nugget") }

    @JvmField val ENTROPY_RANGE_UPGRADE_TWO: Supplier<ItemEntropyRadiusUpgrade> =
        register { ItemEntropyRadiusUpgrade(EntropyRadius.TWO) }
    @JvmField val ENTROPY_RANGE_UPGRADE_THREE: Supplier<ItemEntropyRadiusUpgrade> =
        register { ItemEntropyRadiusUpgrade(EntropyRadius.THREE) }

    // Tools
    @JvmField val ENTROPY_WAND: Supplier<Item> = maybeRegister { ToolEntropyWand("entropy_wand", WAND_MATERIAL.get()) }

    @JvmField val ENTROPY_READER: Supplier<ItemEntropyReader> = register(::ItemEntropyReader)
    @JvmField val LORMYTE_CHARM: Supplier<ItemLormyteCharm> = register(::ItemLormyteCharm)
    @JvmField val ENTROPY_WINGS: Supplier<ItemEntropyWings> = register(::ItemEntropyWings)

    // Food
    @JvmField val ENDER_FLESH: Supplier<ItemEnderFlesh> = register(::ItemEnderFlesh)
    @JvmField val DRAGONITE_BERRIES: Supplier<ItemDragoniteBerries> = register(::ItemDragoniteBerries)
    @JvmField val DRAGONITE_TEA: Supplier<ItemDragoniteTea> = register(::ItemDragoniteTea)
    @JvmField val ORGANA_FRUIT: Supplier<ItemOrganaFruit> = register(::ItemOrganaFruit)
    @JvmField val CHORUS_SOUP: Supplier<ItemChorusSoup> = register(::ItemChorusSoup)

    // TODO: 22/10/2023 rename itemBlocks
    @JvmField val BLOCK_ENDORIUM: Supplier<ItemBlock> = registerItemBlock(ModBlocks.ENDORIUM_BLOCK)
    @JvmField val END_STONE_SMOOTH: Supplier<ItemBlock> = registerItemBlock(ModBlocks.END_STONE_SMOOTH_BLOCK)
    @JvmField val END_STONE_PILLAR: Supplier<ItemBlock> = registerItemBlock(ModBlocks.END_STONE_PILLAR)
    @JvmField val PURPUR_LAMP: Supplier<ItemBlock> = registerItemBlock(ModBlocks.PURPUR_LAMP)
    @JvmField val ENDER_FLOWER_CROP: Supplier<ItemBlock> = registerItemBlock(ModBlocks.ENDER_FLOWER_CROP)
    @JvmField val DRAGON_BUSH: Supplier<ItemBlock> = registerItemBlock(ModBlocks.DRAGONITE_CROP)
    @JvmField val OBSIDIAN_ESSENCE_ORE: Supplier<ItemBlock> = registerItemBlock(ModBlocks.OBSIDIAN_ESSENCE_ORE)
    @JvmField val PHANTOM_BLOCK: Supplier<ItemBlock> = registerItemBlock(ModBlocks.PHANTOM_BLOCK)
    @JvmField val ENTROPY_END_STONE: Supplier<ItemBlock> = registerItemBlock(ModBlocks.ENTROPY_END_STONE)
    @JvmField val LORMYTE_CRYSTAL: Supplier<ItemBlock> = registerItemBlock(ModBlocks.LORMYTE_CRYSTAL_BLOCK)
    @JvmField val DECORATIVE_LORMYTE: Supplier<ItemBlock> = registerItemBlock(ModBlocks.DECORATIVE_LORMYTE_BLOCK)
    @JvmField val TUNGSTEN_BLOCK: Supplier<ItemBlock> = registerItemBlock(ModBlocks.TUNGSTEN_BLOCK)
    @JvmField val TUNGSTEN_ORE: Supplier<ItemBlock> = registerItemBlock(ModBlocks.TUNGSTEN_ORE)
    @JvmField val TUNGSTEN_END_ORE: Supplier<ItemBlock> = registerItemBlock(ModBlocks.TUNGSTEN_END_ORE)
    @JvmField val BLOCK_RUNE: Supplier<ItemBlock> = registerItemBlock(ModBlocks.RUNE_BLOCK)
    @JvmField val BLOCK_END_MAGMA: Supplier<ItemBlock> = registerItemBlock(ModBlocks.END_MAGMA_BLOCK)
    @JvmField val BLOCK_END_FORGE: Supplier<ItemBlock> = registerItemBlock(ModBlocks.BLOCK_END_FORGE)
    @JvmField val ENTROPY_CHAMBER: Supplier<ItemBlock> = registerItemBlock(ModBlocks.ENTROPY_CHAMBER)
    @JvmField val ENTROPY_CROP_BLOCK: Supplier<BaseMetaItemBlock> =
        registerItemBlock(ModBlocks.ENTROPY_CROP_BLOCK) { block ->
            BaseMetaItemBlock(block) { meta -> if (meta == 3) "mature" else null }
        }
    @JvmField val BLOCK_E_USER: Supplier<ItemBlock> = registerItemBlock(ModBlocks.MATERIALIZER_BLOCK)
    @JvmField val SMALL_ENTROPY_BATTERY_BLOCK: Supplier<ItemBlock> = registerItemBlock(ModBlocks.SMALL_ENTROPY_BATTERY_BLOCK)

    @JvmField val END_STONE_CHISELED: Supplier<ItemBlock> = registerItemBlock(ModBlocks.END_BRICKS_CHISELED)
    @JvmField val COLD_FIRE: Supplier<ItemBlock> = registerItemBlock(ModBlocks.ENTROPY_FIRE)
    @JvmField val STAIRS_END_BRICKS: Supplier<ItemBlock> = registerItemBlock(ModBlocks.END_BRICKS_STAIRS)
    @JvmField val STAIRS_SMOOTH_END_STONE: Supplier<ItemBlock> = registerItemBlock(ModBlocks.END_STONE_SMOOTH_STAIRS)
    @JvmField val WALL_END_BRICKS: Supplier<ItemBlock> = registerItemBlock(ModBlocks.END_BRICKS_WALL)
    @JvmField val WALL_PURPUR: Supplier<ItemBlock> = registerItemBlock(ModBlocks.PURPUR_WALL)
    @JvmField val WALL_SMOOTH_END_STONE: Supplier<ItemBlock> = registerItemBlock(ModBlocks.END_STONE_SMOOTH_WALL)
    @JvmField val END_CORAL: Supplier<ItemBlock> = registerItemBlock(ModBlocks.END_CORAL)

    @JvmField val ORGANA_WEED_BLOCK: Supplier<ItemBlock> = registerItemBlock(ModBlocks.ORGANA_WEED_BLOCK)
    @JvmField val END_MOSS_GRASS_BLOCK: Supplier<ItemBlock> = registerItemBlock(ModBlocks.END_MOSS_GRASS_BLOCK)
    @JvmField val END_MOSS_BLOCK: Supplier<ItemBlock> = registerItemBlock(ModBlocks.END_MOSS_BLOCK)

    @JvmField val ORGANA_FLOWER_SEED: Supplier<Item> = register(::ItemOrganaFlowerSeed)
    @JvmField val ORGANA_PLANT_BLOCK: Supplier<ItemBlock> = registerItemBlock(ModBlocks.ORGANA_PLANT_BLOCK)
    @JvmField val ORGANA_FLOWER: Supplier<BaseMetaItemBlock> =
        registerItemBlock(ModBlocks.ORGANA_FLOWER_BLOCK) { block ->
            BaseMetaItemBlock(block) { meta ->
                when (meta) {
                    10 -> "mature\$big"
                    8 -> "mature\$small"
                    else -> null
                }
            }
        }

    private fun <I : Item> register(factory: () -> I): Supplier<I> =
        Suppliers.memoize { factory() }.also { ITEMS += it }

    // The supplier yields null for an item disabled in the config.
    @Suppress("UNCHECKED_CAST")
    private fun <I : Item> maybeRegister(factory: () -> I): Supplier<I> =
        Suppliers.memoize<I?> {
            val item = factory()
            if (item.registryName!!.path in Configs.SERVER_SIDE_CONFIGS.disabledItems) {
                EndReforked.LOGGER.info("Skipping registration of Item: '{}'", item.registryName)
                null
            } else item
        }.also { ITEMS += it } as Supplier<I>

    private fun <B : Block> registerItemBlock(block: Supplier<B>): Supplier<ItemBlock> =
        registerItemBlock(block) { ItemBlock(it) }

    // The supplier yields null when the block is disabled.
    @Suppress("UNCHECKED_CAST")
    private fun <I : ItemBlock, B : Block> registerItemBlock(block: Supplier<B>, factory: (B) -> I): Supplier<I> =
        Suppliers.memoize<I?> {
            val b: B? = block.get()
            b?.let { factory(it).apply { setRegistryName(it.registryName) } }
        }.also { ITEMS += it } as Supplier<I>

    @SubscribeEvent
    @JvmStatic
    fun onItemRegister(event: RegistryEvent.Register<Item>) {
        ITEMS.mapNotNull { it.get() }.forEach(event.registry::register)
    }

    @SubscribeEvent
    @JvmStatic
    fun registerModels(event: ModelRegistryEvent) {
        ITEMS.mapNotNull { it.get() }.filterIsInstance<IAsset>().forEach { it.handleAssets() }
    }
}

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
    private val ITEMS = DeferredRegistry(this, Item::class.java)

    // Materials
    @JvmField val WAND_MATERIAL: ToolMaterial =
        EnumHelper.addToolMaterial("entropy_wand", 4, 256, 5.5F, 1.0F, 13)!!


    // Items
    @get:JvmStatic val INGOT_ENDORIUM by ITEMS.register { ItemBase("item_ingot_endorium") }
    @get:JvmStatic val SHARD_OBSIDIAN by ITEMS.register { ItemBase("item_shard_obsidian") }
    @get:JvmStatic val RAW_ENDORIUM by ITEMS.register { ItemBase("item_raw_endorium") }
    @get:JvmStatic val END_ESSENCE by ITEMS.register { ItemBase("item_end_essence") }
    @get:JvmStatic val END_SHARD by ITEMS.register { ItemBase("item_end_shard") }
    @get:JvmStatic val END_RUNE by ITEMS.register { ItemLegendary("item_end_rune") }
    @get:JvmStatic val ITEM_LORMYTE_CRYSTAL by ITEMS.register { ItemBase("item_lormyte_crystal") }
    @get:JvmStatic val TUNGSTEN_INGOT by ITEMS.register { ItemBase("tungsten_ingot") }
    @get:JvmStatic val ENDER_STRING by ITEMS.register { ItemLegendary("item_ender_string") }

    @get:JvmStatic val ANGEL_FEATHER by ITEMS.register { ItemBase("item_angel_feather") }
    @get:JvmStatic val DRAGON_SCALES by ITEMS.register { ItemBase("dragon_scales") }
    // todo data fix old:xorcite_shard
    @get:JvmStatic val ENTROPY_SHARD by ITEMS.register { ItemBase("entropy_shard") }
    @get:JvmStatic val INFUSED_METALL by ITEMS.register { ItemBase("ingot_infused") }
    @get:JvmStatic val SWORD_SHARD by ITEMS.register { ItemBase("sword_shard") }
    @get:JvmStatic val CATALYST by ITEMS.register { ItemCatalyst("catalyst") }
    @get:JvmStatic val TUNGSTEN_NUGGET by ITEMS.register { ItemBase("tungsten_nugget") }

    @get:JvmStatic val ENTROPY_RANGE_UPGRADE_TWO by ITEMS.register { ItemEntropyRadiusUpgrade(EntropyRadius.TWO) }
    @get:JvmStatic val ENTROPY_RANGE_UPGRADE_THREE by ITEMS.register { ItemEntropyRadiusUpgrade(EntropyRadius.THREE) }

    // Tools
    @get:JvmStatic val ENTROPY_WAND by ITEMS.register { ToolEntropyWand("entropy_wand", WAND_MATERIAL) }

    @get:JvmStatic val ENTROPY_READER by ITEMS.register(::ItemEntropyReader)
    @get:JvmStatic val LORMYTE_CHARM by ITEMS.register(::ItemLormyteCharm)
    @get:JvmStatic val ENTROPY_CELL by ITEMS.register(::ItemEntropyCell)
    @get:JvmStatic val ENTROPY_WINGS by ITEMS.register(::ItemEntropyWings)

    // Food
    @get:JvmStatic val ENDER_FLESH by ITEMS.register(::ItemEnderFlesh)
    @get:JvmStatic val DRAGONITE_BERRIES by ITEMS.register(::ItemDragoniteBerries)
    @get:JvmStatic val DRAGONITE_TEA by ITEMS.register(::ItemDragoniteTea)
    @get:JvmStatic val ORGANA_FRUIT by ITEMS.register(::ItemOrganaFruit)
    @get:JvmStatic val CHORUS_SOUP by ITEMS.register(::ItemChorusSoup)

    // TODO: 22/10/2023 rename itemBlocks
    @get:JvmStatic val BLOCK_ENDORIUM by itemBlock(ModBlocks::ENDORIUM_BLOCK)
    @get:JvmStatic val PURPUR_LAMP by itemBlock(ModBlocks::PURPUR_LAMP)
    @get:JvmStatic val ENDER_FLOWER_CROP by itemBlock(ModBlocks::ENDER_FLOWER_CROP)
    @get:JvmStatic val DRAGON_BUSH by itemBlock(ModBlocks::DRAGONITE_CROP)
    @get:JvmStatic val OBSIDIAN_ESSENCE_ORE by itemBlock(ModBlocks::OBSIDIAN_ESSENCE_ORE)
    @get:JvmStatic val ENTROPY_END_STONE by itemBlock(ModBlocks::ENTROPY_END_STONE)
    @get:JvmStatic val LORMYTE_CRYSTAL by itemBlock(ModBlocks::LORMYTE_CRYSTAL_BLOCK)
    @get:JvmStatic val DECORATIVE_LORMYTE by itemBlock(ModBlocks::DECORATIVE_LORMYTE_BLOCK)
    @get:JvmStatic val TUNGSTEN_BLOCK by itemBlock(ModBlocks::TUNGSTEN_BLOCK)
    @get:JvmStatic val TUNGSTEN_ORE by itemBlock(ModBlocks::TUNGSTEN_ORE)
    @get:JvmStatic val TUNGSTEN_END_ORE by itemBlock(ModBlocks::TUNGSTEN_END_ORE)
    @get:JvmStatic val BLOCK_RUNE by itemBlock(ModBlocks::RUNE_BLOCK)
    @get:JvmStatic val BLOCK_END_MAGMA by itemBlock(ModBlocks::END_MAGMA_BLOCK)
    @get:JvmStatic val BLOCK_END_FORGE by itemBlock(ModBlocks::BLOCK_END_FORGE)
    @get:JvmStatic val ENTROPY_CHAMBER by itemBlock(ModBlocks::ENTROPY_CHAMBER)
    @get:JvmStatic val ENTROPY_CROP_BLOCK by itemBlock(ModBlocks::ENTROPY_CROP_BLOCK) { block ->
            BaseMetaItemBlock(block) { meta -> if (meta == 3) "mature" else null }
        }
    @get:JvmStatic val BLOCK_E_USER by itemBlock(ModBlocks::MATERIALIZER_BLOCK)
    @get:JvmStatic val SMALL_ENTROPY_BATTERY_BLOCK by itemBlock(ModBlocks::SMALL_ENTROPY_BATTERY_BLOCK)
    @get:JvmStatic val END_CORAL by itemBlock(ModBlocks::END_CORAL)

    @get:JvmStatic val ORGANA_WEED_BLOCK by itemBlock(ModBlocks::ORGANA_WEED_BLOCK)
    @get:JvmStatic val END_MOSS_GRASS_BLOCK by itemBlock(ModBlocks::END_MOSS_GRASS_BLOCK)
    @get:JvmStatic val END_MOSS_BLOCK by itemBlock(ModBlocks::END_MOSS_BLOCK)

    @get:JvmStatic val ORGANA_FLOWER_SEED by ITEMS.register(::ItemOrganaFlowerSeed)
    @get:JvmStatic val ORGANA_PLANT_BLOCK by itemBlock(ModBlocks::ORGANA_PLANT_BLOCK)
    @get:JvmStatic val ORGANA_FLOWER by itemBlock(ModBlocks::ORGANA_FLOWER_BLOCK) { block ->
            BaseMetaItemBlock(block) { meta ->
                when (meta) {
                    10 -> $$"mature$big"
                    8 -> $$"mature$small"
                    else -> null
                }
            }
        }

    private fun <B : Block> itemBlock(block: () -> B) = itemBlock(block, ::ItemBlock)

    private fun <B : Block, I : ItemBlock> itemBlock(block: () -> B, factory: (B) -> I) =
        ITEMS.register { block().let { b -> factory(b).apply { registryName = b.registryName } } }

    @SubscribeEvent
    @JvmStatic
    fun onItemRegister(event: RegistryEvent.Register<Item>) {
        ITEMS.registerAll(event.registry)
    }

    @SubscribeEvent
    @JvmStatic
    fun registerModels(event: ModelRegistryEvent) {
        ITEMS.all().filterIsInstance<IAsset>().forEach { it.handleAssets() }
    }
}

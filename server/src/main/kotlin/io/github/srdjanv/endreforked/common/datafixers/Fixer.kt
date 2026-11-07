package io.github.srdjanv.endreforked.common.datafixers

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.utils.Initializer
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry

/** Remaps the old registry names of blocks and items when a world is loaded. */
object Fixer : Initializer {
    private fun mappings(vararg pairs: Pair<String, String>): Map<ResourceLocation, ResourceLocation> =
        pairs.associate { ResourceLocation(Tags.MODID, it.first) to ResourceLocation(Tags.MODID, it.second) }

    // Old names that were both a block and an item
    private val common = mappings(
        "block_end_forge" to "end_forge_block",
        "entropy_user" to "materializer_block",
        "block_wolframium" to "tungsten_block",
        "block_wolframium_ore" to "tungsten_ore",
        "block_end_stone_smooth" to "end_stone_smooth_block",
        "smooth_end_stone_wall" to "end_stone_smooth_wall",
        "smooth_end_stone_stairs" to "end_stone_smooth_stairs",
        "block_end_stone_pillar" to "end_stone_pillar",
        "chiseled_end_bricks" to "end_bricks_chiseled",
        "e_end_bricks_stairs" to "end_bricks_stairs",
        "e_end_bricks_wall" to "end_bricks_wall",
        "block_entropy_end_stone" to "entropy_end_stone",
        "block_endorium" to "endorium_block",
        "block_purpur_lamp" to "purpur_lamp_block",
        "e_purpur_wall" to "purpur_wall",
        "crop_ender_flower" to "voidbloom_crop",
        "ender_flower_crop" to "voidbloom_crop",
        "broken_ender_flower" to "voidbloom_crop_withered",
        "ender_flower_broken" to "voidbloom_crop_withered",
        "crop_dragonite" to "dragonite_crop",
        "block_essence_ore" to "essence_ore",
        "block_phantom" to "phantom_block",
        "block_lormyte_crystal" to "lormyte_crystal_block",
        "block_decorative_lormyte" to "decorative_lormyte_block",
        "block_end_magma" to "end_magma_block",
        "block_rune" to "rune_block",
        "ogana_weed" to "organa_weed",
    )

    private val blocks = common + mappings("dragon_essence" to "xorcite_block")

    private val items = common + mappings(
        "item_ingot_wolframium" to "tungsten_ingot",
        "wolframium_nugget" to "tungsten_nugget",
        "death_essence" to "xorcite_shard",
        "item_dragonite_seeds" to "dragonite_berries",
        "food_dragonite_berries" to "dragonite_berries",
        "item_dragonite_tea" to "dragonite_tea",
        "food_chorus_soup" to "chorus_soup",
    )

    init {
        // Registers the data version of the mod. There are no NBT fixes, only the registry remaps below.
        FMLCommonHandler.instance().dataFixer.init(Tags.MODID, Tags.DATAFIXER_VERSION)
    }

    override fun registerEventBus() = registerThisToEventBus()

    @SubscribeEvent
    fun missingBlockMapping(event: RegistryEvent.MissingMappings<Block>) = remap(event, blocks, ForgeRegistries.BLOCKS, "block")

    @SubscribeEvent
    fun missingItemMapping(event: RegistryEvent.MissingMappings<Item>) = remap(event, items, ForgeRegistries.ITEMS, "item")

    private fun <T : IForgeRegistryEntry<T>> remap(
        event: RegistryEvent.MissingMappings<T>, mappings: Map<ResourceLocation, ResourceLocation>,
        registry: IForgeRegistry<T>, kind: String,
    ) {
        for (entry in event.allMappings) {
            val newName = mappings[entry.key] ?: continue
            val new = registry.getValue(newName)
            if (new != null) {
                entry.remap(new)
            } else {
                EndReforked.LOGGER.error("Unable to locate {}: {}", kind, newName)
                EndReforked.LOGGER.error("Report it at: {}", Tags.ISSUES_URL)
                entry.warn()
            }
        }
    }
}

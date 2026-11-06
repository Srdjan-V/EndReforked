package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.configs.Configs
import io.github.srdjanv.endreforked.utils.Initializer
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.*
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraftforge.event.LootTableLoadEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class LootHandler : Initializer {
    companion object {
        @JvmField val END_GUARD = ResourceLocation(Tags.MODID, "entities/endguard")
        @JvmField val WATCHER = ResourceLocation(Tags.MODID, "entities/watcher")
        @JvmField val LORD = ResourceLocation(Tags.MODID, "entities/lord")

        // TODO: 08/02/2024 add shipWreck loot table
        private val INJECTED_CHEST_TABLES = listOf(
            "abandoned_mineshaft", "desert_pyramid", "end_city_treasure", "jungle_temple", "nether_bridge",
            "village_blacksmith",
        )
        private val INJECTED_ENTITIES_TABLES = listOf("ender_dragon")

        private fun inject(event: LootTableLoadEvent, category: String, injectors: List<String>) {
            val name = event.name.toString()
            if (name.startsWith(category) && name.substring(category.length) in injectors) {
                val file = name.removePrefix("minecraft:")
                val entry = LootEntryTable(ResourceLocation(Tags.MODID, "inject/$file"), 1, 0, emptyArray<LootCondition>(),
                    "endreborn_inject_entry")
                event.table.addPool(LootPool(arrayOf(entry), emptyArray<LootCondition>(), RandomValueRange(1f),
                    RandomValueRange(0f, 1f), "endreborn_inject_pool"))
            }
        }
    }

    override fun registerEventBus() {
        registerThisToEventBus()
    }

    override fun preInit(event: FMLPreInitializationEvent) {
        listOf(END_GUARD, WATCHER, LORD).forEach(LootTableList::register)
    }

    @SubscribeEvent
    fun lootLoad(evt: LootTableLoadEvent) {
        if (Configs.SERVER_SIDE_CONFIGS.chestLoot) inject(evt, "minecraft:chests/", INJECTED_CHEST_TABLES)
        if (Configs.SERVER_SIDE_CONFIGS.entityLoot) inject(evt, "minecraft:entities/", INJECTED_ENTITIES_TABLES)
    }

    override fun dispose() = true
}

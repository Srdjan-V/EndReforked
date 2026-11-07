package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler
import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler
import io.github.srdjanv.endreforked.common.configs.Configs
import io.github.srdjanv.endreforked.common.tiles.*
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.EntropyCropTile
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.FluidEntropyTile
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.FluidOrganaTile
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.OrganaFlowerTile
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.OrganaWeedTile
import io.github.srdjanv.endreforked.common.village.EndVillagerHandler
import io.github.srdjanv.endreforked.utils.Initializer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.crafting.ShapelessRecipes
import net.minecraft.tileentity.BannerPattern
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.util.EnumHelper
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary

class Registration : Initializer {
    override fun registerEventBus() {
        registerThisToEventBus()
    }

    override fun preInit(event: FMLPreInitializationEvent) {
        CapabilityTimedFlightHandler.register()
        CapabilityEntropyHandler.register()

        fun tile(cls: Class<out net.minecraft.tileentity.TileEntity>, name: String) =
            GameRegistry.registerTileEntity(cls, ResourceLocation(Tags.MODID, name))
        tile(MaterializerTile::class.java, "materializer_tile")
        tile(EndForgeTile::class.java, "endforge_Tile")
        tile(SmallEntropyBatteryTile::class.java, "small_entropy_battery_tile")
        tile(EntropyChamberTile::class.java, "entropy_chamber_tile")

        tile(OrganaFlowerTile::class.java, "organa_flower_tile")
        tile(OrganaWeedTile::class.java, "organa_weed_tile")
        tile(EntropyCropTile::class.java, "entropy_crop_tile")
        tile(FluidEntropyTile::class.java, "fluid_entropy_tile")
        tile(FluidOrganaTile::class.java, "fluid_organa_tile")
    }

    override fun init(event: FMLInitializationEvent) {
        registerBannerPatterns()

        // todo remove or improve
        if (Configs.SERVER_SIDE_CONFIGS.spawnNewVillagers) {
            EndVillagerHandler.initVillagerTrades()
            EndVillagerHandler.initVillagerHouse()
        }

        GameRegistry.addSmelting(ModBlocks.TUNGSTEN_ORE, ItemStack(ModItems.TUNGSTEN_INGOT, 1), 1.5f)
        handleOreDictionary()
    }

    override fun dispose() = true

    @SubscribeEvent
    fun registerRecipes(event: RegistryEvent.Register<IRecipe>) {
        val tea = ModItems.DRAGONITE_TEA
        val path = tea.registryName!!.path
        val ingredients = NonNullList.from(
            Ingredient.EMPTY,
            Ingredient.fromStacks(ItemStack(Items.GLASS_BOTTLE, 2)),
            Ingredient.fromStacks(ItemStack(ModItems.DRAGONITE_BERRIES)),
            Ingredient.fromStacks(ItemStack(ModItems.ORGANA_FRUIT)),
        )
        event.registry.register(
            ShapelessRecipes(path, ItemStack(tea, 2), ingredients).setRegistryName(ResourceLocation(Tags.MODID, path)),
        )
    }

    private fun registerBannerPatterns() {
        addPattern("rune", "run", ItemStack(ModItems.END_RUNE))
        addPattern("end", "end", ItemStack(Items.CHORUS_FRUIT_POPPED))
        addPattern("pearl", "prl", ItemStack(Items.ENDER_PEARL))
    }

    private fun addPattern(name: String, id: String, craftingItem: ItemStack) {
        val enumName = "endreborn_$name"
        EnumHelper.addEnum(
            BannerPattern::class.java, enumName.uppercase(),
            arrayOf(String::class.java, String::class.java, ItemStack::class.java),
            enumName, "er_$id", craftingItem,
        )
    }

    private fun handleOreDictionary() {
        OreDictionary.registerOre("ingotEndorium", ModItems.INGOT_ENDORIUM)
        OreDictionary.registerOre("blockEndorium", ModItems.BLOCK_ENDORIUM)
        OreDictionary.registerOre("ingotInfused", ModItems.INFUSED_METALL)

        OreDictionary.registerOre("ingotTungsten", ModItems.TUNGSTEN_INGOT)
        OreDictionary.registerOre("nuggetTungsten", ModItems.TUNGSTEN_NUGGET)
        OreDictionary.registerOre("blockTungsten", ModBlocks.TUNGSTEN_BLOCK)
        OreDictionary.registerOre("oreTungsten", ModBlocks.TUNGSTEN_ORE)
        OreDictionary.registerOre("oreTungsten", ModBlocks.TUNGSTEN_END_ORE)

        OreDictionary.registerOre("dustObsidian", ModItems.CATALYST)
        OreDictionary.registerOre("shardObsidian", ModItems.SHARD_OBSIDIAN)
        OreDictionary.registerOre("shardLormyte", ModItems.LORMYTE_CRYSTAL)
        OreDictionary.registerOre("essence", ModItems.END_ESSENCE)
    }
}

package io.github.srdjanv.endreforked.compat.tconstruct

import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.compat.CompatManger
import io.github.srdjanv.endreforked.EndReforked
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import slimeknights.tconstruct.library.TinkerRegistry
import slimeknights.tconstruct.library.fluid.FluidMolten
import slimeknights.tconstruct.library.materials.*
import slimeknights.tconstruct.library.traits.ITrait
import slimeknights.tconstruct.smeltery.block.BlockMolten
import slimeknights.tconstruct.library.utils.HarvestLevels

/** Tinkers' materials of the mod. Loaded only when `tconstruct` is loaded, see [CompatManger]. */
class TConstruct : CompatManger.ModCompat {
    private val endorium = Material("endreborn_endorium", 0x1b7b6b)
    private val tungsten = Material("endreborn_tungsten", 0x6b6f78)
    private val infused = Material("endreborn_infused", 0xb060e0)
    private val dragonScale = Material("endreborn_dragonscale", 0xa020f0)

    /**
     * The fluid blocks. Tinkers only creates a block for a fluid without one, and maps its model to its own `fluid_block` blockstate,
     * which has no variant for our fluids. So the mod makes them: same ids Tinkers would use, our own blockstate (`fluids.json`).
     */
    private val blocks = mutableListOf<BlockMolten>()

    /** A castable material: the fluid, the stats (head, handle, extra), the ore dictionary suffix of the smeltery. */
    private fun metal(m: Material, color: Int, temperature: Int, suffix: String, head: HeadMaterialStats, handle: HandleMaterialStats, extra: ExtraMaterialStats) {
        val fluid = FluidMolten(m.identifier, color).setTemperature(temperature).setLuminosity(10)
        FluidRegistry.registerFluid(fluid)
        // Before the block registry event, so Tinkers sees a block and skips its own
        blocks += BlockMolten(fluid).also { fluid.block = it; it.registryName = ResourceLocation("tconstruct", "molten_${fluid.name}") }
        TinkerRegistry.addMaterialStats(m, head, handle, extra)
        TinkerRegistry.integrate(m, fluid, suffix).preInit()
    }

    override fun registerEventBus() = registerThisToEventBus()

    @SubscribeEvent
    fun onBlockRegister(event: RegistryEvent.Register<Block>) = blocks.forEach(event.registry::register)

    @SubscribeEvent
    fun registerModels(event: ModelRegistryEvent) =
        blocks.forEach { EndReforked.getProxy().registerFileStateMapper(it, null, "fluids", it.fluid.name) }

    override fun preInit(event: FMLPreInitializationEvent) {
        // Material ids are new (the mod is in alpha), the prefix avoids clashes with other mods
        metal(
            endorium, 0xff1b7b6b.toInt(), 900, "Endorium",
            HeadMaterialStats(700, 7.0f, 4.0f, HarvestLevels.DIAMOND), HandleMaterialStats(1.0f, 60), ExtraMaterialStats(150),
        )
        metal(
            tungsten, 0xff6b6f78.toInt(), 1200, "Tungsten",
            HeadMaterialStats(330, 6.5f, 5.5f, HarvestLevels.IRON), HandleMaterialStats(1.1f, 40), ExtraMaterialStats(110),
        )
        metal(
            infused, 0xffb060e0.toInt(), 1100, "Infused",
            HeadMaterialStats(1000, 8.0f, 5.5f, HarvestLevels.OBSIDIAN), HandleMaterialStats(1.2f, 100), ExtraMaterialStats(200),
        )
        TinkerRegistry.addMaterialStats(dragonScale, BowStringMaterialStats(1.1f))
        TinkerRegistry.integrate(dragonScale).preInit()
    }

    override fun init(event: FMLInitializationEvent) {
        fun Material.ingots(item: Item, trait: ITrait, partType: String? = null) {
            addItem(item, 1, Material.VALUE_Ingot)
            if (partType == null) addTrait(trait) else addTrait(trait, partType)
            setRepresentativeItem(item)
        }
        endorium.ingots(ModItems.INGOT_ENDORIUM, TraitEnderPocket)
        endorium.addItem(ModItems.BLOCK_ENDORIUM, 1, Material.VALUE_Block)
        tungsten.ingots(ModItems.TUNGSTEN_INGOT, TraitGrounding)
        tungsten.addItem(ModItems.TUNGSTEN_NUGGET, 1, Material.VALUE_Nugget)
        tungsten.addItem(ModItems.TUNGSTEN_BLOCK, 1, Material.VALUE_Block)
        infused.ingots(ModItems.INFUSED_METALL, TraitSiphon)
        dragonScale.ingots(ModItems.DRAGON_SCALES, TraitBreath, MaterialTypes.BOWSTRING)
    }

    override fun postInit(event: FMLPostInitializationEvent) {
        val ids = listOf(endorium, tungsten, infused, dragonScale).map { it.identifier }
        val ok = ids.filter { TinkerRegistry.getMaterial(it) !== Material.UNKNOWN }
        val melts = listOf(ModItems.INGOT_ENDORIUM, ModItems.TUNGSTEN_INGOT, ModItems.INFUSED_METALL)
            .count { TinkerRegistry.getMelting(ItemStack(it)) != null }
        EndReforked.LOGGER.info("Tinkers' Construct: registered materials {} of {}, {} of 3 ingots melt in the smeltery", ok, ids, melts)
    }
}

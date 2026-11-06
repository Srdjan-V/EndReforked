package io.github.srdjanv.endreforked.compat.tconstruct

import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.compat.CompatManger
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import slimeknights.tconstruct.library.TinkerRegistry
import slimeknights.tconstruct.library.materials.*
import slimeknights.tconstruct.library.utils.HarvestLevels

class TConstruct : CompatManger.ModCompat {
    override fun preInit(event: FMLPreInitializationEvent) {
        TinkerRegistry.addMaterialStats(
            Endorium.material,
            HeadMaterialStats(1024, 5.5f, 8.0f, HarvestLevels.OBSIDIAN),
            BowMaterialStats(1.2f, 2f, 5f),
            HandleMaterialStats(1.4f, 1024),
            ArrowShaftMaterialStats(1.4f, 5),
        )
        TinkerRegistry.integrate(Endorium.material).preInit()

        TinkerRegistry.addMaterialStats(
            Wolframium.material,
            HeadMaterialStats(512, 5.5f, 6.5f, HarvestLevels.DIAMOND),
            BowMaterialStats(0.5f, 1f, 0f),
            HandleMaterialStats(1.4f, 512),
            ArrowShaftMaterialStats(1.4f, 1),
        )
        TinkerRegistry.integrate(Wolframium.material).preInit()
    }

    override fun init(event: FMLInitializationEvent) {
        Endorium.material.addItem(ModItems.INGOT_ENDORIUM.get(), 1, Material.VALUE_Ingot)
        Endorium.material.addTrait(Endorium.trait).setCraftable(true).setCastable(false)
            .setRepresentativeItem(ModItems.INGOT_ENDORIUM.get())

        Wolframium.material.addItem(ModItems.TUNGSTEN_INGOT.get(), 1, Material.VALUE_Ingot)
        Wolframium.material.addTrait(Wolframium.trait).setCraftable(true).setCastable(false)
            .setRepresentativeItem(ModItems.TUNGSTEN_INGOT.get())
    }
}

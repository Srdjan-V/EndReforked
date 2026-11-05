package io.github.srdjanv.endreforked.compat.tconstruct;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.compat.CompatManger;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.utils.HarvestLevels;

public class TConstruct implements CompatManger.ModCompat {

    @Override
    public String modID() {
        return "tconstruct";
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        TinkerRegistry.addMaterialStats(Endorium.material,
                new HeadMaterialStats(1024, 5.5f, 8.0f, HarvestLevels.OBSIDIAN),
                new BowMaterialStats(1.2f, 2f, 5),
                new HandleMaterialStats(1.4f, 1024),
                new ArrowShaftMaterialStats(1.4f, 5));
        TinkerRegistry.integrate(Endorium.material).preInit();

        TinkerRegistry.addMaterialStats(Wolframium.material,
                new HeadMaterialStats(512, 5.5f, 6.5f, HarvestLevels.DIAMOND),
                new BowMaterialStats(0.5f, 1f, 0),
                new HandleMaterialStats(1.4f, 512),
                new ArrowShaftMaterialStats(1.4f, 1));
        TinkerRegistry.integrate(Wolframium.material).preInit();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        Endorium.material.addItem(ModItems.INGOT_ENDORIUM.get(), 1, Material.VALUE_Ingot);
        Endorium.material
                .addTrait(Endorium.trait)
                .setCraftable(true).setCastable(false)
                .setRepresentativeItem(ModItems.INGOT_ENDORIUM.get());

        Wolframium.material.addItem(ModItems.TUNGSTEN_INGOT.get(), 1, Material.VALUE_Ingot);
        Wolframium.material
                .addTrait(Wolframium.trait)
                .setCraftable(true).setCastable(false)
                .setRepresentativeItem(ModItems.TUNGSTEN_INGOT.get());
    }
}

package io.github.srdjanv.endreforked.compat.conarm

import c4.conarm.lib.materials.ArmorMaterialType
import c4.conarm.lib.materials.ArmorMaterials
import c4.conarm.lib.materials.CoreMaterialStats
import c4.conarm.lib.materials.PlatesMaterialStats
import c4.conarm.lib.materials.TrimMaterialStats
import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.entropy.CriticalEvents
import io.github.srdjanv.endreforked.compat.CompatManger
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import slimeknights.tconstruct.library.TinkerRegistry
import slimeknights.tconstruct.library.materials.Material

/**
 * Construct's Armory stats and armor traits for the Tinkers' materials of [io.github.srdjanv.endreforked.compat.tconstruct.TConstruct].
 * Loaded only when `conarm` and `tconstruct` are loaded, see [CompatManger]. The traits are in [ArmorTraits].
 */
class ConArm : CompatManger.ModCompat {
    private fun mat(id: String): Material = TinkerRegistry.getMaterial(id)

    override fun preInit(event: FMLPreInitializationEvent) {
        // Stats: core(durability, defense), plates(modifier, durability, toughness), trim(extra durability). Iron is core 12/15, cobalt 19.5/15.6.
        TinkerRegistry.addMaterialStats(mat("endreborn_endorium"), CoreMaterialStats(18f, 14f), PlatesMaterialStats(0.9f, 4f, 1f), TrimMaterialStats(9f))
        TinkerRegistry.addMaterialStats(mat("endreborn_tungsten"), CoreMaterialStats(16f, 17f), PlatesMaterialStats(0.85f, 6f, 3f), TrimMaterialStats(5f))
        TinkerRegistry.addMaterialStats(mat("endreborn_infused"), CoreMaterialStats(20f, 18f), PlatesMaterialStats(1.0f, 8f, 2.5f), TrimMaterialStats(10f))
        // Dragon scales are a decoration: trim only (they stay a bow string for Tinkers' tools).
        TinkerRegistry.addMaterialStats(mat("endreborn_dragonscale"), TrimMaterialStats(12f))
    }

    override fun init(event: FMLInitializationEvent) {
        ArmorMaterials.addArmorTrait(mat("endreborn_endorium"), ArmorTraits.Blink)
        ArmorMaterials.addArmorTrait(mat("endreborn_tungsten"), ArmorTraits.Anchored)
        ArmorMaterials.addArmorTrait(mat("endreborn_infused"), ArmorTraits.Absorb)
        ArmorMaterials.addArmorTrait(mat("endreborn_dragonscale"), ArmorTraits.VoidRescue, ArmorMaterialType.TRIM)
        CriticalEvents.protectors.add { ArmorTraits.wearing(it, ArmorTraits.Anchored) }
    }

    override fun postInit(event: FMLPostInitializationEvent) {
        val ids = listOf("endorium", "tungsten", "infused", "dragonscale").map { "endreborn_$it" }
        val ok = ids.filter { mat(it).hasStats(ArmorMaterialType.CORE) || mat(it).hasStats(ArmorMaterialType.TRIM) }
        EndReforked.LOGGER.info("Construct's Armory: registered armor materials {} of {}", ok, ids)
    }
}

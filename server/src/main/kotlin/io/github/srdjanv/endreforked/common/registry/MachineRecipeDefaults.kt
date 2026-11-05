package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.machine.FluidIngredient
import io.github.srdjanv.endreforked.api.machine.FluidOut
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.common.ModFluids
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.registry.GameAdapter.id
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraftforge.fluids.FluidRegistry

/** The machine recipes that ship with the mod. Called when the registries are built, after the items exist. */
object MachineRecipeDefaults {
    val ENTROPY_CHAMBER = Id(Tags.MODID, "entropy_chamber")

    fun all(): List<MachineRecipe> = listOf(
        MachineRecipe(
            id = Id(Tags.MODID, "entropy_chamber/end_magma"),
            machine = ENTROPY_CHAMBER,
            fluidInputs = listOf(FluidIngredient(FluidRegistry.LAVA.id(), 2_000)),
            fluidOutputs = listOf(FluidOut(ModFluids.END_MAGMA.get().id(), 1_000)),
            ticks = 25 * 20,
            entropy = 150,
        ),
        MachineRecipe(
            id = Id(Tags.MODID, "entropy_chamber/organa_weed"),
            machine = ENTROPY_CHAMBER,
            itemInputs = listOf(ItemIngredient.Item(Item.getItemFromBlock(Blocks.END_STONE).id(), 0, 4)),
            itemOutputs = listOf(ItemOut(ModItems.ORGANA_WEED_BLOCK.get().id(), 0, 2)),
            ticks = 5 * 20,
            entropy = 2,
        ),
        MachineRecipe(
            id = Id(Tags.MODID, "entropy_chamber/end_moss_grass"),
            machine = ENTROPY_CHAMBER,
            itemInputs = listOf(ItemIngredient.Item(Item.getItemFromBlock(Blocks.GRASS).id(), 0, 20)),
            itemOutputs = listOf(ItemOut(ModItems.END_MOSS_GRASS_BLOCK.get().id(), 0, 2)),
            ticks = 5 * 20,
            entropy = 2,
        ),
    )
}

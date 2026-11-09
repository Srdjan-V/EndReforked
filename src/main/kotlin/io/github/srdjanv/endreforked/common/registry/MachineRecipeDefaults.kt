package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModFluids
import io.github.srdjanv.endreforked.common.ModItems
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack

/** The machine recipes that ship with the mod (DESIGN 4.4). Called when the registries are built, after the items exist. */
object MachineRecipeDefaults {
    val ENTROPY_CHAMBER = ResourceLocation(Tags.MODID, "entropy_chamber")
    val END_FORGE = ResourceLocation(Tags.MODID, "end_forge")
    val MATERIALIZER = ResourceLocation(Tags.MODID, "materializer")

    fun all(): List<MachineRecipe> = entropyChamber() + endForge() + materializer()

    private fun item(item: Item, count: Int = 1) = ItemIngredient.Item(ItemStack(item, count))

    private fun item(block: Block, count: Int = 1) = ItemIngredient.Item(ItemStack(block, count))

    private fun entropyChamber(): List<MachineRecipe> {
        fun id(name: String) = ResourceLocation(Tags.MODID, "entropy_chamber/$name")
        return listOf(
            MachineRecipe(
                id = id("entropy_fluid"),
                machine = ENTROPY_CHAMBER,
                itemInputs = listOf(item(ModItems.ENTROPY_SHARD)),
                fluidOutputs = listOf(FluidStack(ModFluids.ENTROPY, 1_000)),
                ticks = 100,
            ),
            MachineRecipe(
                id = id("organa_fluid"),
                machine = ENTROPY_CHAMBER,
                itemInputs = listOf(item(ModItems.ORGANA_FRUIT, 4)),
                fluidOutputs = listOf(FluidStack(ModFluids.ORGANA, 1_000)),
                ticks = 100,
                entropy = 20,
            ),
            MachineRecipe(
                id = id("ichor"),
                machine = ENTROPY_CHAMBER,
                fluidInputs = listOf(FluidStack(FluidRegistry.LAVA, 1_000), FluidStack(ModFluids.ORGANA, 1_000)),
                fluidOutputs = listOf(FluidStack(ModFluids.ICHOR, 1_000)),
                ticks = 200,
                entropy = 100,
            ),
        )
    }

    /** Endorium in the End Forge starts with lava (no entropy). Ichor makes more of it and the infused ingot. */
    private fun endForge(): List<MachineRecipe> {
        fun id(name: String) = ResourceLocation(Tags.MODID, "end_forge/$name")
        fun fluid(fluid: Fluid) = listOf(FluidStack(fluid, 250))
        val pearlsAndStone = listOf(item(Items.ENDER_PEARL, 2), item(Blocks.END_STONE, 4))
        return listOf(
            MachineRecipe(
                id = id("endorium"), machine = END_FORGE, itemInputs = pearlsAndStone, fluidInputs = fluid(FluidRegistry.LAVA),
                itemOutputs = listOf(ItemStack(ModItems.INGOT_ENDORIUM)), ticks = 200,
            ),
            MachineRecipe(
                id = id("infused"), machine = END_FORGE, itemInputs = listOf(item(ModItems.INGOT_ENDORIUM)), fluidInputs = fluid(ModFluids.ICHOR),
                itemOutputs = listOf(ItemStack(ModItems.INFUSED_METALL)), ticks = 150, entropy = 40,
            ),
            MachineRecipe(
                id = id("endorium_ichor"), machine = END_FORGE, itemInputs = pearlsAndStone, fluidInputs = fluid(ModFluids.ICHOR),
                itemOutputs = listOf(ItemStack(ModItems.INGOT_ENDORIUM, 4)), ticks = 150, entropy = 40,
            ),
        )
    }

    /**
     * Three items (the catalyst is one) and one world event at 80 %: a structure of rune blocks that the event turns into purpur blocks.
     * R is the rune block, P the purpur pillar. Dragon breath needs a charged chunk, the others a critical one.
     */
    private fun materializer(): List<MachineRecipe> {
        val catalyst = item(ModItems.CATALYST)
        val rune = ModBlocks.RUNE_BLOCK.defaultState
        val symbols = mapOf('R' to rune, 'P' to Blocks.PURPUR_PILLAR.defaultState)
        val used = WorldAction.SetBlock(Blocks.PURPUR_BLOCK.defaultState)
        fun id(name: String) = ResourceLocation(Tags.MODID, "materializer/$name")
        fun event(chance: Int, aisles: List<List<String>>) = listOf(WorldEvent(80, chance, StructureSpec(aisles, symbols), used))
        return listOf(
            MachineRecipe(
                id = id("dragon_breath"), machine = MATERIALIZER,
                itemInputs = listOf(item(Items.GLASS_BOTTLE, 4), item(ModItems.ENTROPY_SHARD), catalyst),
                itemOutputs = listOf(ItemStack(Items.DRAGON_BREATH, 4)),
                ticks = 400, entropy = 150, worldEvents = event(50, listOf(listOf("R"))), minLevel = EntropyLevel.CHARGED,
            ),
            MachineRecipe(
                id = id("shulker_shell"), machine = MATERIALIZER,
                itemInputs = listOf(item(Blocks.PURPUR_BLOCK, 4), item(Items.ENDER_PEARL), catalyst),
                itemOutputs = listOf(ItemStack(Items.SHULKER_SHELL)),
                ticks = 600, entropy = 300, worldEvents = event(75, listOf(listOf("P"), listOf("R"))), minLevel = EntropyLevel.CRITICAL,
            ),
            MachineRecipe(
                id = id("dragon_scales"), machine = MATERIALIZER,
                itemInputs = listOf(item(ModItems.INFUSED_METALL), item(Items.DRAGON_BREATH, 4), catalyst),
                itemOutputs = listOf(ItemStack(ModItems.DRAGON_SCALES, 2)),
                ticks = 600, entropy = 300, worldEvents = event(100, listOf(listOf("RRR"))), minLevel = EntropyLevel.CRITICAL,
            ),
        )
    }
}

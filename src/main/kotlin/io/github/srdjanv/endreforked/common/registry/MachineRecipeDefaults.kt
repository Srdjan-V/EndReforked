package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModFluids
import io.github.srdjanv.endreforked.common.ModItems
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidRegistry

/** The machine recipes that ship with the mod. Called when the registries are built, after the items exist. */
object MachineRecipeDefaults {
    val ENTROPY_CHAMBER = ResourceLocation(Tags.MODID, "entropy_chamber")
    val END_FORGE = ResourceLocation(Tags.MODID, "end_forge")
    val MATERIALIZER = ResourceLocation(Tags.MODID, "materializer")

    fun all(): List<MachineRecipe> = entropyChamber() + endForge() + materializer()

    private fun entropyChamber(): List<MachineRecipe> = listOf(
        MachineRecipe(
            id = ResourceLocation(Tags.MODID, "entropy_chamber/end_magma"),
            machine = ENTROPY_CHAMBER,
            fluidInputs = listOf(FluidStack(FluidRegistry.LAVA, 2_000)),
            fluidOutputs = listOf(FluidStack(ModFluids.END_MAGMA, 1_000)),
            ticks = 25 * 20,
            entropy = 150,
        ),
        MachineRecipe(
            id = ResourceLocation(Tags.MODID, "entropy_chamber/organa_weed"),
            machine = ENTROPY_CHAMBER,
            itemInputs = listOf(ItemIngredient.Item(ItemStack(Blocks.END_STONE, 4))),
            itemOutputs = listOf(ItemStack(ModItems.ORGANA_WEED_BLOCK, 2)),
            ticks = 5 * 20,
            entropy = 2,
        ),
        MachineRecipe(
            id = ResourceLocation(Tags.MODID, "entropy_chamber/end_moss_grass"),
            machine = ENTROPY_CHAMBER,
            itemInputs = listOf(ItemIngredient.Item(ItemStack(Blocks.GRASS, 20))),
            itemOutputs = listOf(ItemStack(ModItems.ORGANA_BLOCK, 2)),
            ticks = 5 * 20,
            entropy = 2,
        ),
    )

    /** Lava and End Magma, 1,000 mB each, plus one item. End Magma gives 4 times the output and is faster, and costs 40 chunk entropy. */
    private fun endForge(): List<MachineRecipe> {
        val lava = FluidRegistry.LAVA
        val endMagma = ModFluids.END_MAGMA
        fun recipe(name: String, fluid: Fluid, input: Item, ticks: Int, output: Item, count: Int, entropy: Int = 0) = MachineRecipe(
            id = ResourceLocation(Tags.MODID, "end_forge/$name"),
            machine = END_FORGE,
            itemInputs = listOf(ItemIngredient.Item(ItemStack(input))),
            fluidInputs = listOf(FluidStack(fluid, 1_000)),
            itemOutputs = listOf(ItemStack(output, count)),
            ticks = ticks,
            entropy = entropy,
        )
        val endorium = ModItems.INGOT_ENDORIUM
        val essence = ModItems.END_ESSENCE
        val entropyStone = ModItems.ENTROPY_END_STONE
        val infused = ModItems.INFUSED_METALL
        val magmaBlock = Item.getItemFromBlock(ModBlocks.END_MAGMA_BLOCK)
        return listOf(
            recipe("lava_infused_metall", lava, endorium, 120, infused, 1),
            recipe("lava_magma_cream", lava, Items.SLIME_BALL, 200, Items.MAGMA_CREAM, 1),
            recipe("lava_blaze_powder", lava, essence, 200, Items.BLAZE_POWDER, 1),
            recipe("lava_end_magma_block", lava, entropyStone, 200, magmaBlock, 1),
            recipe("end_magma_infused_metall", endMagma, endorium, 100, infused, 4, entropy = 40),
            recipe("end_magma_magma_cream", endMagma, Items.SLIME_BALL, 150, Items.MAGMA_CREAM, 4, entropy = 40),
            recipe("end_magma_blaze_powder", endMagma, essence, 150, Items.BLAZE_POWDER, 4, entropy = 40),
            recipe("end_magma_end_magma_block", endMagma, entropyStone, 150, magmaBlock, 4, entropy = 40),
        )
    }

    /** Item plus catalyst. The world events replace blocks near the machine, and a failed event ruins the run. */
    private fun materializer(): List<MachineRecipe> {
        val catalyst = ItemIngredient.Item(ItemStack(ModItems.CATALYST))
        val stone = Blocks.STONE.defaultState
        val iron = Blocks.IRON_BLOCK.defaultState
        val gold = Blocks.GOLD_BLOCK.defaultState
        fun event(percent: Int, chance: Int, symbol: IBlockState, aisles: List<List<String>>, action: WorldAction) =
            WorldEvent(percent, chance, StructureSpec(aisles, mapOf('X' to symbol)), action)
        return listOf(
            MachineRecipe(
                id = ResourceLocation(Tags.MODID, "materializer/iron_to_iron"),
                machine = MATERIALIZER,
                itemInputs = listOf(ItemIngredient.Item(ItemStack(Items.IRON_INGOT)), catalyst),
                itemOutputs = listOf(ItemStack(Items.IRON_INGOT, 2)),
                ticks = 200,
                entropy = 100,
                worldEvents = listOf(
                    event(30, 15, stone, listOf(listOf("X")), WorldAction.SetBlock(iron)),
                    event(60, 15, iron, listOf(listOf("X")), WorldAction.SetBlock(gold)),
                    event(90, 15, gold, listOf(listOf("X")), WorldAction.SetBlock(stone)),
                ),
            ),
            MachineRecipe(
                id = ResourceLocation(Tags.MODID, "materializer/diamond_to_diamond"),
                machine = MATERIALIZER,
                itemInputs = listOf(ItemIngredient.Item(ItemStack(Items.DIAMOND)), catalyst),
                itemOutputs = listOf(ItemStack(Items.DIAMOND)),
                ticks = 600,
                entropy = 300,
                worldEvents = listOf(
                    event(30, 2, stone, List(6) { listOf("X") }, WorldAction.SetBlock(iron)),
                    event(60, 2, iron, listOf(listOf("XXXXXXX")) + List(6) { listOf("X      ") }, WorldAction.SetBlock(gold)),
                    event(90, 2, gold, listOf(listOf("XXXXXXX")), WorldAction.DropItem(ItemStack(Items.DIAMOND))),
                ),
            ),
        )
    }
}

package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.machine.BlockSpec
import io.github.srdjanv.endreforked.api.machine.FluidIngredient
import io.github.srdjanv.endreforked.api.machine.FluidOut
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModFluids
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.registry.GameAdapter.id
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraftforge.fluids.FluidRegistry

/** The machine recipes that ship with the mod. Called when the registries are built, after the items exist. */
object MachineRecipeDefaults {
    val ENTROPY_CHAMBER = Id(Tags.MODID, "entropy_chamber")
    val END_FORGE = Id(Tags.MODID, "end_forge")
    val MATERIALIZER = Id(Tags.MODID, "materializer")

    fun all(): List<MachineRecipe> = entropyChamber() + endForge() + materializer()

    private fun entropyChamber(): List<MachineRecipe> = listOf(
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

    /** Lava and End Magma, 1,000 mB each, plus one item. End Magma gives 4 times the output and is faster. */
    private fun endForge(): List<MachineRecipe> {
        val lava = FluidRegistry.LAVA.id()
        val endMagma = ModFluids.END_MAGMA.get().id()
        fun recipe(name: String, fluid: Id, input: Item, ticks: Int, output: Item, count: Int) = MachineRecipe(
            id = Id(Tags.MODID, "end_forge/$name"),
            machine = END_FORGE,
            itemInputs = listOf(ItemIngredient.Item(input.id(), 0)),
            fluidInputs = listOf(FluidIngredient(fluid, 1_000)),
            itemOutputs = listOf(ItemOut(output.id(), 0, count)),
            ticks = ticks,
        )
        val endorium = ModItems.INGOT_ENDORIUM.get()
        val essence = ModItems.END_ESSENCE.get()
        val entropyStone = ModItems.ENTROPY_END_STONE.get()
        val infused = ModItems.INFUSED_METALL.get()
        val magmaBlock = Item.getItemFromBlock(ModBlocks.END_MAGMA_BLOCK.get())
        return listOf(
            recipe("lava_infused_metall", lava, endorium, 120, infused, 1),
            recipe("lava_magma_cream", lava, Items.SLIME_BALL, 200, Items.MAGMA_CREAM, 1),
            recipe("lava_blaze_powder", lava, essence, 200, Items.BLAZE_POWDER, 1),
            recipe("lava_end_magma_block", lava, entropyStone, 200, magmaBlock, 1),
            recipe("end_magma_infused_metall", endMagma, endorium, 100, infused, 4),
            recipe("end_magma_magma_cream", endMagma, Items.SLIME_BALL, 150, Items.MAGMA_CREAM, 4),
            recipe("end_magma_blaze_powder", endMagma, essence, 150, Items.BLAZE_POWDER, 4),
            recipe("end_magma_end_magma_block", endMagma, entropyStone, 150, magmaBlock, 4),
        )
    }

    /** Item plus catalyst. The world events replace blocks near the machine, and a failed event ruins the run. */
    private fun materializer(): List<MachineRecipe> {
        val catalyst = ItemIngredient.Item(ModItems.CATALYST.get().id(), 0)
        fun block(b: Block) = BlockSpec(b.id())
        val stone = block(Blocks.STONE)
        val iron = block(Blocks.IRON_BLOCK)
        val gold = block(Blocks.GOLD_BLOCK)
        fun event(percent: Int, chance: Int, symbol: BlockSpec, aisles: List<List<String>>, action: WorldAction) =
            WorldEvent(percent, chance, StructureSpec(aisles, mapOf('X' to symbol)), action)
        return listOf(
            MachineRecipe(
                id = Id(Tags.MODID, "materializer/iron_to_iron"),
                machine = MATERIALIZER,
                itemInputs = listOf(ItemIngredient.Item(Items.IRON_INGOT.id(), 0), catalyst),
                itemOutputs = listOf(ItemOut(Items.IRON_INGOT.id(), 0, 2)),
                ticks = 200,
                worldEvents = listOf(
                    event(30, 15, stone, listOf(listOf("X")), WorldAction.SetBlock(iron)),
                    event(60, 15, iron, listOf(listOf("X")), WorldAction.SetBlock(gold)),
                    event(90, 15, gold, listOf(listOf("X")), WorldAction.SetBlock(stone)),
                ),
            ),
            MachineRecipe(
                id = Id(Tags.MODID, "materializer/diamond_to_diamond"),
                machine = MATERIALIZER,
                itemInputs = listOf(ItemIngredient.Item(Items.DIAMOND.id(), 0), catalyst),
                itemOutputs = listOf(ItemOut(Items.DIAMOND.id(), 0, 1)),
                ticks = 600,
                worldEvents = listOf(
                    event(30, 2, stone, List(6) { listOf("X") }, WorldAction.SetBlock(iron)),
                    event(60, 2, iron, listOf(listOf("XXXXXXX")) + List(6) { listOf("X      ") }, WorldAction.SetBlock(gold)),
                    event(90, 2, gold, listOf(listOf("XXXXXXX")), WorldAction.DropItem(ItemOut(Items.DIAMOND.id(), 0, 1))),
                ),
            ),
        )
    }
}

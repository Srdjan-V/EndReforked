package io.github.srdjanv.endreforked.common.machine

import io.github.srdjanv.endreforked.api.base.crafting.TileStatus
import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.impl.machine.MachineRecipes
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import net.minecraft.init.Blocks
import net.minecraft.init.Bootstrap
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MachineProcessTest {
    init {
        Bootstrap.register()
    }

    @AfterEach
    fun reset() = EntropyTuning.reset()

    private val machine = ResourceLocation("endreborn", "end_forge")
    private fun recipe(minLevel: EntropyLevel = EntropyLevel.STABLE) = MachineRecipe(
        ResourceLocation("test", "r"), machine, listOf(ItemIngredient.Item(ItemStack(Blocks.END_STONE, 2))),
        itemOutputs = listOf(ItemStack(Blocks.DIRT)), ticks = 1, minLevel = minLevel,
    )

    private fun process(recipe: MachineRecipe, absorb: Boolean = false): MachineProcess {
        val io = MachineIO(InputHandler(1, { true }) {}, OutputHandler(1) {}, emptyList(), emptyList())
        io.itemInputs.setStackInSlot(0, ItemStack(Blocks.END_STONE, 2))
        val hooks = object : ProcessHooks {
            override fun absorbOverload() = absorb
        }
        return MachineProcess(machine, io, { MachineRecipes(listOf(recipe)) }, hooks)
    }

    private fun run(p: MachineProcess, level: EntropyLevel, roll: Double) = repeat(2) { p.tick(null, level) { roll } }

    @Test
    fun anOverloadTakesTheInputsAndGivesNoOutput() {
        val p = process(recipe())
        run(p, EntropyLevel.CRITICAL, 0.19)
        assertEquals(TileStatus.FAILED, p.status)
        assertTrue(p.io.itemInputs.getStackInSlot(0).isEmpty)
        assertTrue(p.io.itemOutputs.getStackInSlot(0).isEmpty)
    }

    @Test
    fun aRollAtOrAboveTheChanceFinishesNormally() {
        val p = process(recipe())
        run(p, EntropyLevel.CRITICAL, 0.2)
        assertTrue(p.io.itemInputs.getStackInSlot(0).isEmpty)
        assertEquals(1, p.io.itemOutputs.getStackInSlot(0).count)
    }

    @Test
    fun onlyACriticalChunkOverloads() {
        val p = process(recipe())
        run(p, EntropyLevel.CHARGED, 0.0) // 0.0 is a bonus roll too, so the output is 2
        assertEquals(2, p.io.itemOutputs.getStackInSlot(0).count)
    }

    @Test
    fun anAbsorbedOverloadEndsAsNormal() {
        val p = process(recipe(), absorb = true)
        run(p, EntropyLevel.CRITICAL, 0.0)
        assertTrue(p.io.itemInputs.getStackInSlot(0).isEmpty)
        assertTrue(p.io.itemOutputs.getStackInSlot(0).count >= 1)
    }

    @Test
    fun aRecipePausesBelowItsMinimumLevel() {
        val p = process(recipe(EntropyLevel.CRITICAL))
        run(p, EntropyLevel.CHARGED, 0.9)
        assertEquals(TileStatus.NOT_ENOUGH_ENTROPY, p.status)
        assertEquals(2, p.io.itemInputs.getStackInSlot(0).count)
        run(p, EntropyLevel.CRITICAL, 0.9)
        assertEquals(1, p.io.itemOutputs.getStackInSlot(0).count)
    }
}

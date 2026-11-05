package io.github.srdjanv.endreforked.common.machine

import io.github.srdjanv.endreforked.api.base.crafting.TileStatus
import io.github.srdjanv.endreforked.api.entropy.world.ChunkEntropyView
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.common.registry.GameAdapter
import io.github.srdjanv.endreforked.impl.machine.MachineRecipes
import io.github.srdjanv.endreforked.impl.machine.Match
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

/**
 * One recipe pipeline of a machine: finds the recipe for the current [io], runs it for the recipe's ticks,
 * then takes the inputs and gives the outputs. [recipes] is the registry snapshot to read.
 * It differs per side: a client world reads the client registry. The server reads the server one.
 *
 * The found recipe is cached. It is looked up again only when the inputs changed ([inputChanged])
 * or when the registry has a new snapshot.
 */
class MachineProcess(
    val machine: Id,
    val io: MachineIO,
    private val recipes: () -> MachineRecipes,
) : INBTSerializable<NBTTagCompound> {
    var status = TileStatus.IDLE
    var ticksRun = 0
        private set

    // The input version stands in for a timestamp: it grows on every input change.
    private var inputVersion = 0
    private var cachedVersion = -1
    private var cachedSnapshot: MachineRecipes? = null
    private var cachedMatch: Match? = null
    private var activeRecipe: Id? = null

    fun inputChanged() {
        inputVersion++
    }

    val match: Match?
        get() {
            val snapshot = recipes()
            if (snapshot !== cachedSnapshot || inputVersion != cachedVersion) {
                cachedSnapshot = snapshot
                cachedVersion = inputVersion
                cachedMatch = snapshot.find(machine, io.itemInput(), io.fluidInput(), GameAdapter.ores)
            }
            return cachedMatch
        }

    val recipe: MachineRecipe? get() = match?.recipe

    fun progress(): Double = recipe?.let { ticksRun.toDouble() / it.ticks } ?: 0.0

    /** One server tick. [entropy] is the entropy around the machine. */
    fun tick(entropy: ChunkEntropyView) {
        val match = match
        if (match == null) {
            ticksRun = 0
            activeRecipe = null
            status = if (io.isInputEmpty()) TileStatus.IDLE else TileStatus.INVALID
            return
        }
        val recipe = match.recipe
        if (recipe.id != activeRecipe) {
            activeRecipe = recipe.id
            ticksRun = 0
        }
        if (entropy.currentEntropy < recipe.entropy) {
            status = TileStatus.NOT_ENOUGH_ENTROPY
            return
        }
        status = TileStatus.RUNNING
        if (ticksRun < recipe.ticks) {
            ticksRun++
            return
        }

        // finished: check everything, then change everything
        if (entropy.drainEntropy(recipe.entropy, true) != recipe.entropy) {
            status = TileStatus.NOT_ENOUGH_ENTROPY
        } else if (!io.extract(match, true)) {
            status = TileStatus.INVALID
        } else if (!io.fits(recipe)) {
            status = TileStatus.OUT_FULL
        } else {
            entropy.drainEntropy(recipe.entropy, false)
            io.extract(match, false)
            io.insertOutputs(recipe)
            ticksRun = 0
        }
    }

    override fun serializeNBT() = io.serializeNBT().apply {
        setInteger("ticks_run", ticksRun)
        activeRecipe?.let { setString("recipe", it.toString()) }
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        io.deserializeNBT(nbt)
        ticksRun = nbt.getInteger("ticks_run")
        activeRecipe = if (nbt.hasKey("recipe")) Id.parseOrNull(nbt.getString("recipe")) else null
        inputChanged()
    }
}

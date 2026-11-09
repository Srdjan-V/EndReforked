package io.github.srdjanv.endreforked.common.machine

import io.github.srdjanv.endreforked.api.base.crafting.TileStatus
import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import io.github.srdjanv.endreforked.api.entropy.world.ChunkEntropyView
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.impl.machine.MachineRecipes
import io.github.srdjanv.endreforked.impl.machine.Match
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.util.INBTSerializable

/** Machine specific behaviour of a [MachineProcess]. Both calls are optional. */
interface ProcessHooks {
    /**
     * Called on every tick that the recipe runs, with the ticks run so far.
     * The last call is at ticksRun == recipe.ticks, also while the output is full.
     * Return false to abort: the inputs are taken and lost, and the status is FAILED.
     */
    fun onRunning(recipe: MachineRecipe, ticksRun: Int): Boolean = true

    /** Called once after a recipe finished and its outputs were given. */
    fun onFinished(recipe: MachineRecipe) {}

    /** Called when a run overloads (critical chunk). Return true to absorb it: the run then ends as normal. Chunk 27 uses it for the Organa absorption. */
    fun absorbOverload(): Boolean = false
}

/**
 * One recipe pipeline of a machine: finds the recipe for the current [io], runs it for the recipe's ticks,
 * then takes the inputs and gives the outputs. [recipes] is the registry snapshot to read.
 * It differs per side: a client world reads the client registry. The server reads the server one.
 *
 * The found recipe is cached. It is looked up again only when the inputs changed ([inputChanged])
 * or when the registry has a new snapshot.
 */
class MachineProcess(
    val machine: ResourceLocation,
    val io: MachineIO,
    private val recipes: () -> MachineRecipes,
    private val hooks: ProcessHooks = object : ProcessHooks {},
) : INBTSerializable<NBTTagCompound> {
    var status = TileStatus.IDLE
    var ticksRun = 0
        private set

    // The input version stands in for a timestamp: it grows on every input change.
    private var inputVersion = 0
    private var cachedVersion = -1
    private var cachedSnapshot: MachineRecipes? = null
    private var cachedMatch: Match? = null
    private var activeRecipe: ResourceLocation? = null
    private val accumulator = ProgressAccumulator()

    fun inputChanged() {
        inputVersion++
    }

    val match: Match?
        get() {
            val snapshot = recipes()
            if (snapshot !== cachedSnapshot || inputVersion != cachedVersion) {
                cachedSnapshot = snapshot
                cachedVersion = inputVersion
                cachedMatch = snapshot.find(machine, io.itemInput(), io.fluidInput())
            }
            return cachedMatch
        }

    val recipe: MachineRecipe? get() = match?.recipe

    fun progress(): Double = recipe?.let { ticksRun.toDouble() / it.ticks } ?: 0.0

    /**
     * One server tick. [entropy] is the entropy around the machine. A machine without entropy passes null, and only recipes with 0 entropy run.
     * [level] is the level of the chunk of the machine: it sets the speed, the chance of a bonus output and the overload.
     * A recipe is paused while [level] is below its minimum level.
     * [roll] gives random numbers in [0, 1) for the overload and the bonus.
     * A run in a critical chunk overloads with the chance [EntropyTuning.overloadChance]: it takes the inputs and the entropy and gives no output.
     */
    fun tick(entropy: ChunkEntropyView? = null, level: EntropyLevel = EntropyLevel.STABLE, roll: () -> Double = Math::random) {
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
        if ((entropy?.currentEntropy ?: 0) < recipe.entropy) {
            status = TileStatus.NOT_ENOUGH_ENTROPY
            return
        }
        if (level < recipe.minLevel) {
            status = TileStatus.NOT_ENOUGH_ENTROPY
            return
        }
        status = TileStatus.RUNNING
        if (!hooks.onRunning(recipe, ticksRun)) {
            io.extract(match, false)
            ticksRun = 0
            activeRecipe = null
            status = TileStatus.FAILED
            return
        }
        if (ticksRun < recipe.ticks) {
            ticksRun = minOf(recipe.ticks, ticksRun + accumulator.step(EntropyTuning.speed(level)))
            return
        }

        // finished: check everything, then change everything
        if (recipe.entropy > 0 && (entropy?.drainEntropy(recipe.entropy, true) ?: 0) != recipe.entropy) {
            status = TileStatus.NOT_ENOUGH_ENTROPY
        } else if (!io.extract(match, true)) {
            status = TileStatus.INVALID
        } else if (!io.fits(recipe)) {
            status = TileStatus.OUT_FULL
        } else if (EntropyTuning.overload(level, roll()) && !hooks.absorbOverload()) {
            if (recipe.entropy > 0) entropy?.drainEntropy(recipe.entropy, false)
            io.extract(match, false)
            ticksRun = 0
            activeRecipe = null
            status = TileStatus.FAILED
        } else {
            if (recipe.entropy > 0) entropy?.drainEntropy(recipe.entropy, false)
            io.extract(match, false)
            io.insertOutputs(recipe)
            if (EntropyTuning.bonus(level, roll())) {
                recipe.itemOutputs.firstOrNull()?.let { io.itemOutputs.insertInternal(it.copy().apply { count = 1 }, false) }
            }
            ticksRun = 0
            hooks.onFinished(recipe)
        }
    }

    override fun serializeNBT() = io.serializeNBT().apply {
        setInteger("ticks_run", ticksRun)
        activeRecipe?.let { setString("recipe", it.toString()) }
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        io.deserializeNBT(nbt)
        ticksRun = nbt.getInteger("ticks_run")
        activeRecipe = if (nbt.hasKey("recipe")) ResourceLocation(nbt.getString("recipe")) else null
        inputChanged()
    }
}

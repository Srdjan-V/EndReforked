package io.github.srdjanv.endreforked.compat.groovyscript

import com.cleanroommc.groovyscript.api.GroovyBlacklist
import com.cleanroommc.groovyscript.api.GroovyLog
import com.cleanroommc.groovyscript.api.IScriptReloadable
import com.cleanroommc.groovyscript.helper.Alias
import com.cleanroommc.groovyscript.registry.NamedRegistry
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning

/**
 * `mods.endreborn.entropy`: the entropy numbers, see [EntropyTuning]. A `/gs reload` puts back the defaults before the scripts run again.
 * Inducer names: rift, organaFlower.
 */
class EntropyGroovy : NamedRegistry(Alias.generateOf("Entropy")), IScriptReloadable {
    /** The chunk is charged from this percent of its capacity (default 33). */
    fun chargedPercent(percent: Int) = run { EntropyTuning.chargedPercent = percent }

    /** The chunk is critical above this percent (default 80). */
    fun criticalPercent(percent: Int) = run { EntropyTuning.criticalPercent = percent }

    /** The percent of its capacity that a chunk of the Entropy biome moves to by itself (default 85). */
    fun entropyBiomePercent(percent: Int) = run { EntropyTuning.entropyBiomePercent = percent }

    /** The chance in percent that a finished run in a critical chunk loses its output (default 20). */
    fun overloadChance(percent: Int) = run { EntropyTuning.overloadChance = percent.coerceIn(0, 100) }

    /** The critical events: a roll every [interval] ticks (default 600), with [baseChance] percent at the critical threshold (default 85). */
    fun criticalEvents(interval: Int, baseChance: Int) = run {
        EntropyTuning.criticalInterval = interval.coerceAtLeast(1)
        EntropyTuning.criticalBaseChance = baseChance.coerceIn(0, 100)
    }

    /** The chunk entropy that a shard crop uses for each growth stage (default 33) and for each block it spreads to (default 25). */
    fun shardCrop(stage: Int, spread: Int) = run {
        EntropyTuning.shardCropStage = stage.coerceAtLeast(0)
        EntropyTuning.shardCropSpread = spread.coerceAtLeast(0)
    }

    /** The cap of a Lormyte Stabilizer in percent of the chunk capacity. Below 0 (default) it is the critical level. */
    fun stabilizerCap(percent: Int) = run { EntropyTuning.stabilizerCapPercent = percent.coerceAtMost(100) }

    /** Organa absorption on or off (default on). */
    fun absorption(enabled: Boolean) = run { EntropyTuning.absorptionEnabled = enabled }

    /** The capacity of an entropy cell (default 1000). */
    fun cellCapacity(capacity: Int) = run { EntropyTuning.cellCapacity = capacity.coerceAtLeast(1) }

    /** The timed flight factor in a charged chunk (default 1.5) and in a critical chunk (default 2). */
    fun flightFactors(charged: Double, critical: Double) = run {
        EntropyTuning.flightChargedFactor = charged
        EntropyTuning.flightCriticalFactor = critical
    }

    /** A carried cell pays [cost] entropy (default 100) for [ticks] more flight ticks (default 400) when the flight ends. */
    fun flightCell(cost: Int, ticks: Int) = run {
        EntropyTuning.flightCellCost = cost.coerceAtLeast(1)
        EntropyTuning.flightCellExtension = ticks
    }

    /** An inducer adds [amount] to its chunk every [interval] ticks. */
    fun inducer(name: String, amount: Int, interval: Int) {
        val inducer = EntropyTuning.inducer(name)
        if (inducer == null) GroovyLog.msg("Unknown entropy inducer {}", name).error().post()
        else if (interval < 1) GroovyLog.msg("The interval of an entropy inducer must be 1 or more").error().post()
        else {
            inducer.amount = amount
            inducer.interval = interval
        }
    }

    @GroovyBlacklist
    override fun onReload() = EntropyTuning.reset()

    @GroovyBlacklist
    override fun afterScriptLoad() {}
}

package io.github.srdjanv.endreforked.compat.crafttweaker

import crafttweaker.annotations.ZenRegister
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import stanhebben.zenscript.annotations.ZenClass
import stanhebben.zenscript.annotations.ZenMethod

/**
 * `mods.endreborn.Entropy`: the entropy numbers, see [EntropyTuning]. Startup only: CraftTweaker has no reload.
 * Inducer names: organaWeed, organaFlower, entropyCrop, fluidEntropy, fluidOrgana.
 */
@ZenRegister
@ZenClass("mods.endreborn.Entropy")
object EntropyZen {
    @JvmStatic @ZenMethod fun chargedPercent(percent: Int) = act("Setting the charged level to $percent %") { EntropyTuning.chargedPercent = percent }

    @JvmStatic @ZenMethod fun criticalPercent(percent: Int) = act("Setting the critical level to $percent %") { EntropyTuning.criticalPercent = percent }

    @JvmStatic @ZenMethod fun entropyBiomePercent(percent: Int) = act("Setting the Entropy biome base level to $percent %") { EntropyTuning.entropyBiomePercent = percent }

    @JvmStatic @ZenMethod fun chamberDrain(amount: Int) = act("Setting the Entropy Chamber drain to $amount") { EntropyTuning.chamberDrain = amount }

    @JvmStatic
    @ZenMethod
    fun criticalEvents(interval: Int, baseChance: Int) = act("Setting the critical events to every $interval ticks, base chance $baseChance %") {
        EntropyTuning.criticalInterval = interval.coerceAtLeast(1)
        EntropyTuning.criticalBaseChance = baseChance.coerceIn(0, 100)
    }

    @JvmStatic @ZenMethod fun cellCapacity(capacity: Int) = act("Setting the entropy cell capacity to $capacity") { EntropyTuning.cellCapacity = capacity.coerceAtLeast(1) }

    @JvmStatic
    @ZenMethod
    fun flightFactors(charged: Double, critical: Double) = act("Setting the timed flight factors to x$charged and x$critical") {
        EntropyTuning.flightChargedFactor = charged
        EntropyTuning.flightCriticalFactor = critical
    }

    @JvmStatic
    @ZenMethod
    fun flightCell(cost: Int, ticks: Int) = act("Setting the entropy cell flight extension to $ticks ticks for $cost entropy") {
        EntropyTuning.flightCellCost = cost.coerceAtLeast(1)
        EntropyTuning.flightCellExtension = ticks
    }

    @JvmStatic
    @ZenMethod
    fun inducer(name: String, amount: Int, interval: Int) {
        val inducer = EntropyTuning.inducer(name) ?: return fail("Entropy: unknown inducer $name")
        if (interval < 1) return fail("Entropy: the interval must be 1 or more")
        act("Setting the entropy inducer $name to $amount every $interval ticks") {
            inducer.amount = amount
            inducer.interval = interval
        }
    }
}

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
    fun inducer(name: String, amount: Int, interval: Int) {
        val inducer = EntropyTuning.inducer(name) ?: return fail("Entropy: unknown inducer $name")
        if (interval < 1) return fail("Entropy: the interval must be 1 or more")
        act("Setting the entropy inducer $name to $amount every $interval ticks") {
            inducer.amount = amount
            inducer.interval = interval
        }
    }
}

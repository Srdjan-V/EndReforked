package io.github.srdjanv.endreforked.compat.groovyscript

import com.cleanroommc.groovyscript.api.GroovyBlacklist
import com.cleanroommc.groovyscript.api.GroovyLog
import com.cleanroommc.groovyscript.api.IScriptReloadable
import com.cleanroommc.groovyscript.helper.Alias
import com.cleanroommc.groovyscript.registry.NamedRegistry
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning

/**
 * `mods.endreborn.entropy`: the entropy numbers, see [EntropyTuning]. A `/gs reload` puts back the defaults before the scripts run again.
 * Inducer names: organaWeed, organaFlower, entropyCrop, fluidEntropy, fluidOrgana.
 */
class EntropyGroovy : NamedRegistry(Alias.generateOf("Entropy")), IScriptReloadable {
    /** The chunk is charged from this percent of its capacity (default 33). */
    fun chargedPercent(percent: Int) = run { EntropyTuning.chargedPercent = percent }

    /** The chunk is critical above this percent (default 80). */
    fun criticalPercent(percent: Int) = run { EntropyTuning.criticalPercent = percent }

    /** The percent of its capacity that a chunk of the Entropy biome moves to by itself (default 50). */
    fun entropyBiomePercent(percent: Int) = run { EntropyTuning.entropyBiomePercent = percent }

    /** The entropy that an Entropy Chamber drains per chunk of its radius, every 10 s (default 150). */
    fun chamberDrain(amount: Int) = run { EntropyTuning.chamberDrain = amount }

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

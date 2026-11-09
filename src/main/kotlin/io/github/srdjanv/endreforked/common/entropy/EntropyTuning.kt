package io.github.srdjanv.endreforked.common.entropy

import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import kotlin.math.min

/**
 * Every number of the entropy rules that scripts can change (`mods.endreborn.entropy`), with the defaults of the mod.
 * Scripts write on the server thread at load time. GroovyScript calls [reset] before it runs the scripts again.
 */
object EntropyTuning {
    /** The amount [amount] that an inducer adds to its chunk every [interval] ticks. */
    class Inducer(private val defaultAmount: Int, private val defaultInterval: Int = 5 * 20) {
        @Volatile var amount = defaultAmount
        @Volatile var interval = defaultInterval

        fun reset() {
            amount = defaultAmount
            interval = defaultInterval
        }
    }

    @Volatile var chargedPercent = 33
    @Volatile var criticalPercent = 80

    /** The level that the chunks of the Entropy biome move to, in percent of their capacity. Other biomes move to 0. */
    @Volatile var entropyBiomePercent = 85

    /** The chance in percent that a finished run in a critical chunk loses its output (overload). */
    @Volatile var overloadChance = 20

    /** Ticks between two critical event rolls of a world, and the chance in percent at the critical threshold. It rises to 100 % at a full chunk. */
    @Volatile var criticalInterval = 30 * 20
    @Volatile var criticalBaseChance = 50

    /** Shard crop: chunk entropy for each growth stage, and for each block it spreads to. */
    @Volatile var shardCropStage = 33
    @Volatile var shardCropSpread = 25

    /** Lormyte Stabilizer: the cap in percent of the capacity of a chunk in its area. Below 0 it is [criticalPercent]. */
    @Volatile var stabilizerCapPercent = -1

    /** Organa absorption: a powered-off Stabilizer with Organa fluid next to it absorbs critical events and overloads. */
    @Volatile var absorptionEnabled = true

    /** Entropy cell capacity. */
    @Volatile var cellCapacity = 1000

    /** Timed flight: duration factor in a charged and in a critical chunk (stable is x1). */
    @Volatile var flightChargedFactor = 1.5
    @Volatile var flightCriticalFactor = 2.0

    /** When the flight ends, a carried cell pays [flightCellCost] entropy for [flightCellExtension] more ticks. */
    @Volatile var flightCellCost = 100
    @Volatile var flightCellExtension = 20 * 20

    /** Siphon trait: entropy drained per broken block, ticks of the speed bonus, speed bonus per drained point, and entropy per repaired durability. */
    @Volatile var siphonDrain = 10
    @Volatile var siphonBonusTicks = 3 * 20
    @Volatile var siphonSpeedPerPoint = 0.05
    @Volatile var siphonEntropyPerDurability = 2

    /** Blink trait (armor): ticks of cooldown per player. */
    @Volatile var blinkCooldown = 5 * 20

    /** Absorb trait (armor): ticks between two drains, entropy per step, absorption points per step, absorption cap in points per armor piece with the trait. */
    @Volatile var absorbInterval = 5 * 20
    @Volatile var absorbEntropy = 50
    @Volatile var absorbPoints = 2f
    @Volatile var absorbCapPerPiece = 8f

    /** Void rescue trait (armor): durability lost by the piece, in percent of its maximum. */
    @Volatile var voidRescueWearPercent = 25

    /** The Entropy Rift: a passive inducer in the Entropy biome. */
    val rift = Inducer(30, 5 * 20)

    /** The Organa flower: the amount it induces each time it grows. Its interval is not used. */
    val organaFlower = Inducer(20)

    private val inducers = mapOf(
        "rift" to rift,
        "organaFlower" to organaFlower,
    )

    /** The inducer of a script by name: [inducers] keys. Null for an unknown name. */
    fun inducer(name: String): Inducer? = inducers[name]

    /** Ticks between two decay steps of a world. */
    const val DECAY_INTERVAL = 10 * 20
    const val BONUS_CHANCE = 0.1

    fun reset() {
        chargedPercent = 33
        criticalPercent = 80
        entropyBiomePercent = 85
        overloadChance = 20
        criticalInterval = 30 * 20
        criticalBaseChance = 50
        shardCropStage = 33
        shardCropSpread = 25
        stabilizerCapPercent = -1
        absorptionEnabled = true
        cellCapacity = 1000
        flightChargedFactor = 1.5
        flightCriticalFactor = 2.0
        flightCellCost = 100
        flightCellExtension = 20 * 20
        siphonDrain = 10
        siphonBonusTicks = 3 * 20
        siphonSpeedPerPoint = 0.05
        siphonEntropyPerDurability = 2
        blinkCooldown = 5 * 20
        absorbInterval = 5 * 20
        absorbEntropy = 50
        absorbPoints = 2f
        absorbCapPerPiece = 8f
        voidRescueWearPercent = 25
        inducers.values.forEach(Inducer::reset)
    }

    /** The most entropy that a chunk of [capacity] may hold under a Stabilizer. */
    fun stabilizerCap(capacity: Int): Int =
        (capacity.toLong() * (if (stabilizerCapPercent < 0) criticalPercent else stabilizerCapPercent) / 100).toInt()

    fun level(entropy: Int, capacity: Int) = EntropyLevel.of(entropy, capacity, chargedPercent, criticalPercent)

    fun speed(level: EntropyLevel) = when (level) {
        EntropyLevel.STABLE -> 1.0
        EntropyLevel.CHARGED -> 1.5
        EntropyLevel.CRITICAL -> 2.0
    }

    /** The chance (0 to 1) of a critical event: the base chance at the critical threshold, rising linearly to 1 at a full chunk. */
    fun criticalChance(entropy: Int, capacity: Int): Double {
        if (capacity <= 0) return 0.0
        val base = criticalBaseChance / 100.0
        val span = 100.0 - criticalPercent
        val t = if (span <= 0) 1.0 else ((entropy * 100.0 / capacity - criticalPercent) / span).coerceIn(0.0, 1.0)
        return base + (1 - base) * t
    }

    /** The timed flight duration of [base] ticks, scaled by the chunk [level]. */
    fun flightDuration(base: Int, level: EntropyLevel) = (base * when (level) {
        EntropyLevel.STABLE -> 1.0
        EntropyLevel.CHARGED -> flightChargedFactor
        EntropyLevel.CRITICAL -> flightCriticalFactor
    }).toInt()

    fun spawnFactor(level: EntropyLevel) = level.ordinal + 1

    /** True when a finished run overloads: critical level and a [roll] (a random number in [0, 1)) under [overloadChance]. */
    fun overload(level: EntropyLevel, roll: Double) = level == EntropyLevel.CRITICAL && roll < overloadChance / 100.0

    /** [roll] is a random number in [0, 1). */
    fun bonus(level: EntropyLevel, roll: Double) = level != EntropyLevel.STABLE && roll < BONUS_CHANCE

    /** [current] moved by at most [step] towards [base], never past it. */
    fun decayStep(current: Int, base: Int, step: Int): Int =
        if (current > base) current - min(step, current - base) else current + min(step, base - current)
}

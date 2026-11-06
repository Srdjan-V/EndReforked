package io.github.srdjanv.endreforked.impl.world

import io.github.srdjanv.endreforked.api.machine.BlockSpec
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.ITEM_ENTITY
import io.github.srdjanv.endreforked.api.world.WandConversion
import io.github.srdjanv.endreforked.api.world.WorldEffect
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WorldInteractionTest {
    private fun id(path: String) = Id("test", path)
    private fun block(name: String, vararg props: Pair<String, String>) = BlockSpec(Id("minecraft", name), mapOf(*props))
    private val entropy = Id("endreborn", "entropy")

    private val stoneToCobble = WandConversion(id("a"), block("stone", "variant" to "stone"), block("cobblestone"), effects = listOf(WorldEffect.FLINT_SOUND))
    private val graniteToDirt = WandConversion(id("b"), block("stone", "variant" to "granite"), block("dirt"), itemDamage = 7, priority = 1)
    private val endStone = FluidBlockInteraction(id("c"), entropy, block("end_stone"), block("dirt"), chance = 5)
    private val chorus = FluidEntityInteraction(
        id("d"), entropy, ITEM_ENTITY, ItemIngredient.Item(Id("minecraft", "chorus_flower"), 0, 4),
        result = EntityResult.SpawnItem(ItemOut(Id("minecraft", "dirt"), 0, 4)), chance = 10, consumeSource = true,
    )
    private val enderman = FluidEntityInteraction(
        id("e"), entropy, Id("minecraft", "enderman"), removeEntity = true,
        result = EntityResult.SpawnEntity(Id("endreborn", "watcher")), consumeSource = true, consumeChance = 2, priority = 2,
    )
    private val fruit = FluidEntityInteraction(
        id("f"), entropy, ITEM_ENTITY, ItemIngredient.Item(Id("minecraft", "apple")),
        result = EntityResult.SetFluidBlock(block("water")), chance = 20, consumeSource = true,
    )

    @Test
    fun lookupsAreOrderedByPriorityThenId() {
        val wand = WandConversions(listOf(stoneToCobble, graniteToDirt, stoneToCobble.copy(id = id("0"))))
        assertEquals(listOf(id("b"), id("0"), id("a")), wand.forBlock(Id("minecraft", "stone")).map { it.id })
        assertEquals(emptyList<WandConversion>(), wand.forBlock(Id("minecraft", "dirt")))
        val blocks = FluidBlockInteractions(listOf(endStone))
        assertEquals(listOf(endStone), blocks.find(entropy, Id("minecraft", "end_stone")))
        assertEquals(emptyList<FluidBlockInteraction>(), blocks.find(Id("endreborn", "organa"), Id("minecraft", "end_stone")))
        val entities = FluidEntityInteractions(listOf(fruit, chorus, enderman))
        assertEquals(listOf(chorus, fruit), entities.find(entropy, ITEM_ENTITY))
        assertEquals(listOf(enderman), entities.find(entropy, Id("minecraft", "enderman")))
    }

    @Test
    fun wireRoundTrip() {
        val wand = WandConversions(listOf(stoneToCobble, graniteToDirt)).toSnapshot()
        assertEquals(listOf(stoneToCobble, graniteToDirt), RegistrySnapshot.ADAPTER.decode(RegistrySnapshot.ADAPTER.encode(wand)).toWandConversions())
        val blocks = FluidBlockInteractions(listOf(endStone)).toSnapshot()
        assertEquals(listOf(endStone), RegistrySnapshot.ADAPTER.decode(RegistrySnapshot.ADAPTER.encode(blocks)).toFluidBlockInteractions())
        val entities = FluidEntityInteractions(listOf(chorus, enderman, fruit)).toSnapshot()
        assertEquals(
            listOf(chorus, enderman, fruit),
            RegistrySnapshot.ADAPTER.decode(RegistrySnapshot.ADAPTER.encode(entities)).toFluidEntityInteractions(),
        )
        assertNull(wand.toFluidBlockInteractions())
        assertNull(blocks.toWandConversions())
    }

    @Test
    fun scriptsOverrideRemoveAndInvalidEntriesAreSkipped() {
        val wand = WandConversions.managed({ listOf(stoneToCobble, graniteToDirt) })
        wand.add(stoneToCobble.copy(result = block("gravel"), itemDamage = 9))
        wand.remove(id("b"))
        wand.add(graniteToDirt.copy(id = id("neg"), itemDamage = -1))
        val wandReport = wand.rebuild()
        assertEquals(listOf("scripts: test:neg: itemDamage must not be negative"), wandReport.errors)
        assertEquals(2, wandReport.applied["scripts"])
        assertEquals(listOf(stoneToCobble.copy(result = block("gravel"), itemDamage = 9)), wand.current.entries.toList())

        val blocks = FluidBlockInteractions.managed({ listOf(endStone) })
        blocks.add(endStone.copy(id = id("c0"), chance = 0))
        blocks.add(endStone.copy(id = id("c2"), consumeChance = 3))
        val blockReport = blocks.rebuild()
        assertEquals(2, blockReport.errors.size, blockReport.errors.toString())
        assertTrue(blockReport.errors.any { it.contains("chance must be at least 1") })
        assertTrue(blockReport.errors.any { it.contains("consumeChance needs consumeSource") })
        assertEquals(listOf(endStone), blocks.current.entries.toList())

        val entities = FluidEntityInteractions.managed({ listOf(chorus, enderman) })
        entities.removeIf { it.entity == ITEM_ENTITY }
        entities.add(fruit)
        entities.add(chorus.copy(id = id("noitem"), item = null))
        entities.add(enderman.copy(id = id("extra"), item = ItemIngredient.Item(Id("minecraft", "apple"))))
        val report = entities.rebuild()
        assertEquals(2, report.errors.size, report.errors.toString())
        assertTrue(report.errors.any { it.contains("test:noitem: an interaction with minecraft:item needs an item") })
        assertTrue(report.errors.any { it.contains("test:extra: only an interaction with minecraft:item has an item") })
        assertEquals(listOf(enderman, fruit), entities.current.entries.toList())
    }
}

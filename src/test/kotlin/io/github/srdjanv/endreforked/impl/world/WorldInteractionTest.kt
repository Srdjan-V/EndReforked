package io.github.srdjanv.endreforked.impl.world

import io.github.srdjanv.endreforked.api.world.BlockMatch
import io.github.srdjanv.endreforked.api.world.CorruptionConversion
import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.ITEM_ENTITY
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockStone
import net.minecraft.init.Blocks
import net.minecraft.init.Bootstrap
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.FluidRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WorldInteractionTest {
    init {
        Bootstrap.register()
    }

    private fun id(path: String) = ResourceLocation("test", path)
    private fun mc(path: String) = ResourceLocation("minecraft", path)
    private fun stone(variant: BlockStone.EnumType) = BlockMatch(Blocks.STONE, mapOf(BlockStone.VARIANT to variant))
    private val granite = BlockStone.EnumType.GRANITE
    private val entropy = FluidRegistry.LAVA // any registered fluid

    private val stoneToCobble = CorruptionConversion(id("a"), stone(BlockStone.EnumType.STONE), Blocks.COBBLESTONE.defaultState)
    private val graniteToDirt = CorruptionConversion(id("b"), stone(granite), Blocks.DIRT.defaultState, priority = 1)
    private val endStone = FluidBlockInteraction(id("c"), entropy, BlockMatch(Blocks.END_STONE), Blocks.DIRT.defaultState, chance = 5)
    private val chorus = FluidEntityInteraction(
        id("d"), entropy, ITEM_ENTITY, ItemStack(Blocks.CHORUS_FLOWER, 4),
        result = EntityResult.SpawnItem(ItemStack(Blocks.DIRT, 4)), chance = 10, consumeSource = true,
    )
    private val enderman = FluidEntityInteraction(
        id("e"), entropy, mc("enderman"), removeEntity = true,
        result = EntityResult.SpawnEntity(mc("zombie")), consumeSource = true, consumeChance = 2, priority = 2,
    )
    private val fruit = FluidEntityInteraction(
        id("f"), entropy, ITEM_ENTITY, ItemStack(Blocks.GRASS),
        result = EntityResult.SetFluidBlock(Blocks.WATER.defaultState), chance = 20, consumeSource = true,
    )

    @Test
    fun aBlockMatchWithoutPropertiesMatchesEveryState() {
        val state = Blocks.STONE.defaultState.withProperty(BlockStone.VARIANT, granite)
        assertTrue(BlockMatch(Blocks.STONE).matches(state))
        assertTrue(stone(granite).matches(state))
        assertFalse(stone(BlockStone.EnumType.STONE).matches(state))
        assertFalse(BlockMatch(Blocks.DIRT).matches(state))
        assertEquals(state, stone(granite).toState())
    }

    @Test
    fun aMatchOnTheFluidLevelAcceptsOnlySources() {
        val level = BlockLiquid.LEVEL
        val sourceOnly = BlockMatch(Blocks.WATER, mapOf(level to 0))
        assertTrue(sourceOnly.matches(Blocks.WATER.defaultState))
        assertFalse(sourceOnly.matches(Blocks.WATER.defaultState.withProperty(level, 3)))
        val soil = FluidBlockInteraction(id("soil"), entropy, sourceOnly, Blocks.DIRT.defaultState)
        assertEquals(listOf(soil), FluidBlockInteractions(listOf(soil)).find(entropy, Blocks.WATER))
    }

    @Test
    fun lookupsAreOrderedByPriorityThenId() {
        val corruption = CorruptionConversions(listOf(stoneToCobble, graniteToDirt, stoneToCobble.copy(id = id("0"))))
        assertEquals(listOf(id("b"), id("0"), id("a")), corruption.forBlock(Blocks.STONE).map { it.id })
        assertEquals(emptyList<CorruptionConversion>(), corruption.forBlock(Blocks.DIRT))
        val blocks = FluidBlockInteractions(listOf(endStone))
        assertEquals(listOf(endStone), blocks.find(entropy, Blocks.END_STONE))
        assertEquals(emptyList<FluidBlockInteraction>(), blocks.find(FluidRegistry.WATER, Blocks.END_STONE))
        val entities = FluidEntityInteractions(listOf(fruit, chorus, enderman))
        assertEquals(listOf(chorus, fruit), entities.find(entropy, ITEM_ENTITY))
        assertEquals(listOf(enderman), entities.find(entropy, mc("enderman")))
    }

    // Stacks have no equals, so a round trip is compared by encoding the decoded entries again.
    private fun roundTrip(snapshot: RegistrySnapshot) = RegistrySnapshot.ADAPTER.decode(RegistrySnapshot.ADAPTER.encode(snapshot))

    @Test
    fun wireRoundTrip() {
        val blocks = FluidBlockInteractions(listOf(endStone)).toSnapshot()
        assertEquals(listOf(endStone), roundTrip(blocks).toFluidBlockInteractions())
        val entities = FluidEntityInteractions(listOf(chorus, enderman, fruit)).toSnapshot()
        val back = roundTrip(entities).toFluidEntityInteractions()!!
        assertEquals(entities, FluidEntityInteractions(back).toSnapshot())
        val chorusBack = back.first { it.id == id("d") }
        assertEquals(4, chorusBack.item!!.count)
        assertEquals(4, (chorusBack.result as EntityResult.SpawnItem).item.count)
        assertEquals(Blocks.WATER.defaultState, (back.first { it.id == id("f") }.result as EntityResult.SetFluidBlock).block)
        assertNull(blocks.toFluidEntityInteractions())
    }

    @Test
    fun scriptsOverrideRemoveAndInvalidEntriesAreSkipped() {
        val corruption = CorruptionConversions.managed { listOf(stoneToCobble, graniteToDirt) }
        corruption.add(stoneToCobble.copy(result = Blocks.GRAVEL.defaultState))
        corruption.remove(id("b"))
        assertEquals(0, corruption.rebuild().errors.size)
        assertEquals(listOf(stoneToCobble.copy(result = Blocks.GRAVEL.defaultState)), corruption.current.entries.toList())

        val blocks = FluidBlockInteractions.managed { listOf(endStone) }
        blocks.add(endStone.copy(id = id("c0"), chance = 0))
        blocks.add(endStone.copy(id = id("c2"), consumeChance = 3))
        val blockReport = blocks.rebuild()
        assertEquals(2, blockReport.errors.size, blockReport.errors.toString())
        assertTrue(blockReport.errors.any { it.contains("chance must be at least 1") })
        assertTrue(blockReport.errors.any { it.contains("consumeChance needs consumeSource") })
        assertEquals(listOf(endStone), blocks.current.entries.toList())

        val entities = FluidEntityInteractions.managed { listOf(chorus, enderman) }
        entities.removeIf { it.entity == ITEM_ENTITY }
        entities.add(fruit)
        entities.add(chorus.copy(id = id("noitem"), item = null))
        entities.add(enderman.copy(id = id("extra"), item = ItemStack(Blocks.GRASS)))
        entities.add(enderman.copy(id = id("noentity"), entity = ResourceLocation("nomod", "thing")))
        val report = entities.rebuild()
        assertEquals(3, report.errors.size, report.errors.toString())
        assertTrue(report.errors.any { it.contains("test:noitem: an interaction with minecraft:item needs an item") })
        assertTrue(report.errors.any { it.contains("test:extra: only an interaction with minecraft:item has an item") })
        assertTrue(report.errors.any { it.contains("test:noentity: unknown entity nomod:thing") })
        assertEquals(listOf(enderman, fruit), entities.current.entries.toList())
    }
}

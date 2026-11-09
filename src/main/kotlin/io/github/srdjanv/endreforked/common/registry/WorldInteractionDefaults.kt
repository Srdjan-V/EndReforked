package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.world.BlockMatch
import io.github.srdjanv.endreforked.api.world.CorruptionConversion
import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModFluids
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.init.Blocks
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.BlockFluidBase

/** The corruption conversions and fluid interactions that ship with the mod. Called when the registries are built, after the blocks and items exist. */
object WorldInteractionDefaults {
    private fun match(b: Block, vararg properties: Pair<IProperty<*>, Comparable<*>>) = BlockMatch(b, mapOf(*properties))

    /** Only end stone corrupts. The conversions match the default state of the block only. */
    fun corruptionConversions(): List<CorruptionConversion> = listOf(
        CorruptionConversion(ResourceLocation(Tags.MODID, "corruption/end_stone"), match(Blocks.END_STONE), ModBlocks.ENTROPY_END_STONE.defaultState),
    )

    /** Entropy fluid next to an Organa fluid source makes Organa soil, like lava and water make obsidian. */
    fun fluidBlockInteractions(): List<FluidBlockInteraction> = listOf(
        FluidBlockInteraction(
            id = ResourceLocation(Tags.MODID, "entropy/organa_soil"),
            fluid = ModFluids.ENTROPY,
            match = match(ModBlocks.FLUID_ORGANA_BLOCK, BlockFluidBase.LEVEL to 0),
            result = ModBlocks.ORGANA_BLOCK.defaultState,
        ),
        FluidBlockInteraction(
            id = ResourceLocation(Tags.MODID, "entropy/entropy_end_stone"),
            fluid = ModFluids.ENTROPY,
            match = match(Blocks.END_STONE),
            result = ModBlocks.ENTROPY_END_STONE.defaultState,
            chance = 5,
        ),
    )

    fun fluidEntityInteractions(): List<FluidEntityInteraction> {
        val entropy = ModFluids.ENTROPY
        return listOf(
            FluidEntityInteraction(
                id = ResourceLocation(Tags.MODID, "entropy/entity_watcher"),
                fluid = entropy,
                entity = ResourceLocation("minecraft", "enderman"),
                removeEntity = true,
                result = EntityResult.SpawnEntity(ResourceLocation(Tags.MODID, "watcher")),
                consumeSource = true,
            ),
        )
    }
}

package io.github.srdjanv.endreforked.compat.crafttweaker

import crafttweaker.annotations.ZenRegister
import crafttweaker.api.block.IBlockState
import crafttweaker.api.block.IBlockStateMatcher
import crafttweaker.api.item.IItemStack
import crafttweaker.api.liquid.ILiquidStack
import crafttweaker.api.minecraft.CraftTweakerMC
import io.github.srdjanv.endreforked.api.world.CorruptionConversion
import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.ITEM_ENTITY
import io.github.srdjanv.endreforked.compat.scripting.removeByEntity
import io.github.srdjanv.endreforked.compat.scripting.removeByFluid
import io.github.srdjanv.endreforked.compat.scripting.removeByInput
import io.github.srdjanv.endreforked.compat.scripting.removeByOutput
import stanhebben.zenscript.annotations.Optional
import stanhebben.zenscript.annotations.ZenClass
import stanhebben.zenscript.annotations.ZenMethod

// The script API of the world interaction registries. An input block is `<blockstate:minecraft:log:variant=oak>` (the properties must match)
// or `<blockstate:minecraft:log>.matchBlock()` (every state matches). An output block is a block state. An id argument is `namespace:path`.

/** `mods.endreborn.CorruptionConversion`: what the Corruption critical event does to a block. */
@ZenRegister
@ZenClass("mods.endreborn.CorruptionConversion")
object CorruptionConversionZen {
    private val layer get() = CtLayers.corruption

    @JvmStatic
    @ZenMethod
    fun addRecipe(input: IBlockStateMatcher, output: IBlockState, @Optional priority: Int, @Optional name: String?) {
        val match = matchOf(input) ?: return fail("CorruptionConversion: the input must match the states of one block")
        val result = CraftTweakerMC.getBlockState(output)
        act("Adding a corruption conversion") {
            layer.add(name?.let(layer::parse), "corruption") { CorruptionConversion(it, match, result, priority) }
        }
    }

    @JvmStatic @ZenMethod fun remove(id: String) = act("Removing the corruption conversion $id") { layer.remove(layer.parse(id)) }

    @JvmStatic @ZenMethod fun removeAll() = act("Removing all corruption conversions") { layer.removeAll() }

    @JvmStatic @ZenMethod fun removeByInput(input: IBlockState) =
        act("Removing the corruption conversions of $input") { layer.removeByInput(CraftTweakerMC.getBlockState(input)) }

    @JvmStatic @ZenMethod fun removeByOutput(output: IBlockState) =
        act("Removing the corruption conversions to $output") { layer.removeByOutput(CraftTweakerMC.getBlockState(output)) }
}

/** `mods.endreborn.FluidBlockInteraction`: what a fluid block does to a block next to it. */
@ZenRegister
@ZenClass("mods.endreborn.FluidBlockInteraction")
object FluidBlockInteractionZen {
    private val layer get() = CtLayers.fluidBlocks

    /**
     * [input] is the block next to the fluid. It happens in 1 of [chance] tries. With [consumeSource] it only happens at a source block,
     * and the source is removed in 1 of [consumeChance] cases (0 never).
     */
    @JvmStatic
    @ZenMethod
    fun addRecipe(
        fluid: ILiquidStack, input: IBlockStateMatcher, output: IBlockState,
        @Optional(valueLong = 1) chance: Int, @Optional consumeSource: Boolean, @Optional consumeChance: Int,
        @Optional priority: Int, @Optional name: String?,
    ) {
        val liquid = fluidOf(fluid) ?: return fail("FluidBlockInteraction: needs a fluid")
        val match = matchOf(input) ?: return fail("FluidBlockInteraction: the input must match the states of one block")
        val result = CraftTweakerMC.getBlockState(output)
        act("Adding a fluid block interaction") {
            layer.add(name?.let(layer::parse), "fluid_block") {
                FluidBlockInteraction(it, liquid, match, result, chance, consumeSource, consumeChance, priority)
            }
        }
    }

    @JvmStatic @ZenMethod fun remove(id: String) = act("Removing the fluid block interaction $id") { layer.remove(layer.parse(id)) }

    @JvmStatic @ZenMethod fun removeAll() = act("Removing all fluid block interactions") { layer.removeAll() }

    @JvmStatic @ZenMethod fun removeByFluid(fluid: ILiquidStack) =
        act("Removing the fluid block interactions of $fluid") { fluidOf(fluid)?.let(layer::removeByFluid) }

    @JvmStatic @ZenMethod fun removeByInput(input: IBlockState) =
        act("Removing the fluid block interactions of $input") { layer.removeByInput(CraftTweakerMC.getBlockState(input)) }

    @JvmStatic @ZenMethod fun removeByOutput(output: IBlockState) =
        act("Removing the fluid block interactions to $output") { layer.removeByOutput(CraftTweakerMC.getBlockState(output)) }
}

/** `mods.endreborn.FluidEntityInteraction`: what a fluid block does to an entity that touches it. */
@ZenRegister
@ZenClass("mods.endreborn.FluidEntityInteraction")
object FluidEntityInteractionZen {
    private val layer get() = CtLayers.fluidEntities

    /**
     * The entity is an entity type (`minecraft:cow`), or null for item entities that hold [input] (the count is taken from the stack).
     * With [removeEntity] the entity is removed. Then exactly one result happens: [outputItem] is spawned at the entity,
     * an entity of type [outputEntity] is spawned, or the fluid block is replaced with [outputBlock].
     * [chance], [consumeSource] and [consumeChance] work like in FluidBlockInteraction.
     */
    @JvmStatic
    @ZenMethod
    fun addRecipe(
        fluid: ILiquidStack, @Optional entity: String?, @Optional input: IItemStack?, @Optional removeEntity: Boolean,
        @Optional outputItem: IItemStack?, @Optional outputEntity: String?, @Optional outputBlock: IBlockState?,
        @Optional(valueLong = 1) chance: Int, @Optional consumeSource: Boolean, @Optional consumeChance: Int,
        @Optional priority: Int, @Optional name: String?,
    ) {
        val liquid = fluidOf(fluid) ?: return fail("FluidEntityInteraction: needs a fluid")
        val results = listOfNotNull<EntityResult>(
            outputItem?.let { EntityResult.SpawnItem(CraftTweakerMC.getItemStack(it).copy()) },
            outputEntity?.let { EntityResult.SpawnEntity(layer.parse(it)) },
            outputBlock?.let { EntityResult.SetFluidBlock(CraftTweakerMC.getBlockState(it)) },
        )
        val result = results.singleOrNull() ?: return fail("FluidEntityInteraction: needs exactly one of outputItem, outputEntity, outputBlock")
        val item = input?.let { CraftTweakerMC.getItemStack(it).copy() }
        if (entity == null && item == null) return fail("FluidEntityInteraction: an item entity needs an input")
        act("Adding a fluid entity interaction") {
            layer.add(name?.let(layer::parse), "fluid_entity") {
                FluidEntityInteraction(
                    it, liquid, entity?.let(layer::parse) ?: ITEM_ENTITY, item, removeEntity, result,
                    chance, consumeSource, consumeChance, priority,
                )
            }
        }
    }

    @JvmStatic @ZenMethod fun remove(id: String) = act("Removing the fluid entity interaction $id") { layer.remove(layer.parse(id)) }

    @JvmStatic @ZenMethod fun removeAll() = act("Removing all fluid entity interactions") { layer.removeAll() }

    @JvmStatic @ZenMethod fun removeByFluid(fluid: ILiquidStack) =
        act("Removing the fluid entity interactions of $fluid") { fluidOf(fluid)?.let(layer::removeByFluid) }

    @JvmStatic @ZenMethod fun removeByEntity(entity: String) =
        act("Removing the fluid entity interactions of $entity") { layer.removeByEntity(layer.parse(entity)) }
}

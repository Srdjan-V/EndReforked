package io.github.srdjanv.endreforked.compat.groovyscript

import com.cleanroommc.groovyscript.api.GroovyLog
import com.cleanroommc.groovyscript.helper.Alias
import io.github.srdjanv.endreforked.api.world.BlockMatch
import io.github.srdjanv.endreforked.api.world.CorruptionConversion
import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.ITEM_ENTITY
import io.github.srdjanv.endreforked.compat.scripting.ScriptLayer
import io.github.srdjanv.endreforked.compat.scripting.removeByEntity
import io.github.srdjanv.endreforked.compat.scripting.removeByFluid
import io.github.srdjanv.endreforked.compat.scripting.removeByInput
import io.github.srdjanv.endreforked.compat.scripting.removeByOutput
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack

// The script API of the world interaction registries. A block argument is a block state (`blockstate('minecraft:log:1')`,
// the properties must match) or a block (`block('minecraft:log')`, every state matches). An id argument is `namespace:path`.

/** `mods.endreborn.corruption_conversion`: what the Corruption critical event does to a block. */
class CorruptionConversionGroovy(layer: ScriptLayer<CorruptionConversion>) :
    ReloadableGroovy<CorruptionConversion>(Alias.generateOf("CorruptionConversion"), layer) {
    fun recipeBuilder() = CorruptionBuilder(this)

    fun remove(id: String) = layer.remove(layer.parse(id))

    fun removeAll() = layer.removeAll()

    fun removeByInput(input: IBlockState) = layer.removeByInput(input)

    fun removeByOutput(output: IBlockState) = layer.removeByOutput(output)
}

class CorruptionBuilder(private val registry: CorruptionConversionGroovy) {
    private var name: ResourceLocation? = null
    private var match: BlockMatch? = null
    private var result: IBlockState? = null
    private var priority = 0

    fun name(name: String) = apply { this.name = registry.layer.parse(name) }

    fun input(state: IBlockState) = apply { match = BlockMatch(state) }

    fun input(block: Block) = apply { match = BlockMatch(block) }

    fun output(state: IBlockState) = apply { result = state }

    fun output(block: Block) = apply { result = block.defaultState }

    fun priority(priority: Int) = apply { this.priority = priority }

    fun register(): CorruptionConversion? {
        val msg = GroovyLog.msg("Error adding a corruption conversion").error()
        msg.add(match == null, "needs an input")
        msg.add(result == null, "needs an output")
        if (msg.postIfNotEmpty()) return null
        return registry.layer.add(name, "corruption") { CorruptionConversion(it, match!!, result!!, priority) }
    }
}

/** The common part of the builders of the fluid interactions. */
abstract class ReactionBuilder {
    protected var name: ResourceLocation? = null
    protected var fluid: Fluid? = null
    protected var chance = 1
    protected var consumeSource = false
    protected var consumeChance = 0
    protected var priority = 0

    /** The fluid, as `fluid('lava')`. */
    fun fluid(fluid: FluidStack) = apply { this.fluid = fluid.fluid }

    fun fluid(fluid: Fluid) = apply { this.fluid = fluid }

    /** It happens in 1 of this many tries. */
    fun chance(chance: Int) = apply { this.chance = chance }

    /** It only happens at a source block. */
    fun consumeSource() = apply { consumeSource = true }

    /** With consumeSource: the source is removed in 1 of this many cases. */
    fun consumeChance(chance: Int) = apply { consumeChance = chance }

    fun priority(priority: Int) = apply { this.priority = priority }
}

/** `mods.endreborn.fluid_block_interaction`: what a fluid block does to a block next to it. */
class FluidBlockInteractionGroovy(layer: ScriptLayer<FluidBlockInteraction>) :
    ReloadableGroovy<FluidBlockInteraction>(Alias.generateOf("FluidBlockInteraction"), layer) {
    fun recipeBuilder() = FluidBlockBuilder(this)

    fun remove(id: String) = layer.remove(layer.parse(id))

    fun removeAll() = layer.removeAll()

    fun removeByFluid(fluid: FluidStack) = layer.removeByFluid(fluid.fluid)

    fun removeByInput(input: IBlockState) = layer.removeByInput(input)

    fun removeByOutput(output: IBlockState) = layer.removeByOutput(output)
}

class FluidBlockBuilder(private val registry: FluidBlockInteractionGroovy) : ReactionBuilder() {
    private var match: BlockMatch? = null
    private var result: IBlockState? = null

    fun name(name: String) = apply { this.name = registry.layer.parse(name) }

    /** The block next to the fluid. */
    fun input(state: IBlockState) = apply { match = BlockMatch(state) }

    fun input(block: Block) = apply { match = BlockMatch(block) }

    fun output(state: IBlockState) = apply { result = state }

    fun output(block: Block) = apply { result = block.defaultState }

    fun register(): FluidBlockInteraction? {
        val msg = GroovyLog.msg("Error adding a fluid block interaction").error()
        msg.add(fluid == null, "needs a fluid")
        msg.add(match == null, "needs an input")
        msg.add(result == null, "needs an output")
        if (msg.postIfNotEmpty()) return null
        return registry.layer.add(name, "fluid_block") {
            FluidBlockInteraction(it, fluid!!, match!!, result!!, chance, consumeSource, consumeChance, priority)
        }
    }
}

/** `mods.endreborn.fluid_entity_interaction`: what a fluid block does to an entity that touches it. */
class FluidEntityInteractionGroovy(layer: ScriptLayer<FluidEntityInteraction>) :
    ReloadableGroovy<FluidEntityInteraction>(Alias.generateOf("FluidEntityInteraction"), layer) {
    fun recipeBuilder() = FluidEntityBuilder(this)

    fun remove(id: String) = layer.remove(layer.parse(id))

    fun removeAll() = layer.removeAll()

    fun removeByFluid(fluid: FluidStack) = layer.removeByFluid(fluid.fluid)

    fun removeByEntity(entity: String) = layer.removeByEntity(layer.parse(entity))
}

class FluidEntityBuilder(private val registry: FluidEntityInteractionGroovy) : ReactionBuilder() {
    private var entity: ResourceLocation? = null
    private var item: ItemStack? = null
    private var removeEntity = false
    private var result: EntityResult? = null

    fun name(name: String) = apply { this.name = registry.layer.parse(name) }

    /** The entity type. Without it the interaction is for item entities, and needs [input]. */
    fun entity(entity: String) = apply { this.entity = registry.layer.parse(entity) }

    /** For item entities: the item that must be in the stack. Its count is taken from the stack. */
    fun input(item: ItemStack) = apply { this.item = item.copy() }

    /** The entity is removed. */
    fun removeEntity() = apply { removeEntity = true }

    /** Spawns the item at the entity. */
    fun outputItem(item: ItemStack) = apply { result = EntityResult.SpawnItem(item.copy()) }

    /** Spawns an entity of this type at the entity. */
    fun outputEntity(entity: String) = apply { result = EntityResult.SpawnEntity(registry.layer.parse(entity)) }

    /** Replaces the fluid block. */
    fun outputBlock(state: IBlockState) = apply { result = EntityResult.SetFluidBlock(state) }

    fun register(): FluidEntityInteraction? {
        val msg = GroovyLog.msg("Error adding a fluid entity interaction").error()
        msg.add(fluid == null, "needs a fluid")
        msg.add(result == null, "needs an output")
        if (msg.postIfNotEmpty()) return null
        return registry.layer.add(name, "fluid_entity") {
            FluidEntityInteraction(it, fluid!!, entity ?: ITEM_ENTITY, item, removeEntity, result!!, chance, consumeSource, consumeChance, priority)
        }
    }
}

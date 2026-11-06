package io.github.srdjanv.endreforked.common.world

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.WILDCARD_META
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.common.registry.GameAdapter
import io.github.srdjanv.endreforked.common.registry.GameAdapter.id
import io.github.srdjanv.endreforked.common.registry.GameAdapter.matches
import io.github.srdjanv.endreforked.common.registry.GameAdapter.toStack
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.fluids.BlockFluidBase
import net.minecraftforge.fluids.Fluid
import java.util.Random
import java.util.concurrent.ConcurrentHashMap

/** Runs the fluid interaction registries for the fluid blocks. A fluid block calls it from its random tick and its entity collision. */
object FluidInteractions {
    private val fluidIds = ConcurrentHashMap<Fluid, Id>()

    private fun id(fluid: Fluid) = fluidIds.getOrPut(fluid) { fluid.id() }

    /** One try of an interaction. [chance] is 1 in N, and with [consumeSource] only a source block (level 0) reacts. */
    private fun passes(chance: Int, consumeSource: Boolean, fluidState: IBlockState, random: Random) =
        (chance <= 1 || random.nextInt(chance) == 0) && (!consumeSource || fluidState.getValue(BlockFluidBase.LEVEL) == 0)

    /** After a reaction a source block disappears in 1 of [consumeChance] cases. */
    private fun consume(world: WorldServer, fluidPos: BlockPos, consumeSource: Boolean, consumeChance: Int, random: Random) {
        if (consumeSource && consumeChance > 0 && random.nextInt(consumeChance) == 0) world.setBlockToAir(fluidPos)
    }

    private fun fits(item: ItemIngredient.Item, stack: ItemStack) =
        !stack.isEmpty && stack.item.id() == item.id && stack.count >= item.count && (item.meta == WILDCARD_META || item.meta == stack.metadata)

    /** Reacts with the six neighbours of the fluid block at [fluidPos]. */
    @JvmStatic
    fun randomTick(world: World, fluidPos: BlockPos, fluidState: IBlockState, random: Random, fluid: Fluid) {
        if (world.isRemote) return
        val server = world as WorldServer
        val interactions = EndReforked.getProxy().registries(world).fluidBlockInteractions.current
        val fluidId = id(fluid)
        for (side in EnumFacing.VALUES) {
            val pos = fluidPos.offset(side)
            val state = server.getBlockState(pos)
            if (state.block.isAir(state, server, pos) || state.block === fluid.block) continue
            for (i in interactions.find(fluidId, state.block.id())) {
                if (!state.matches(i.match) || !passes(i.chance, i.consumeSource, fluidState, random)) continue
                val result = GameAdapter.state(i.result) ?: continue
                server.setBlockState(pos, result, 3)
                consume(server, fluidPos, i.consumeSource, i.consumeChance, random)
                break
            }
        }
    }

    /** Reacts with an entity that touches the fluid block at [fluidPos]. The first interaction that fits happens. */
    @JvmStatic
    fun entityCollision(world: World, fluidPos: BlockPos, fluidState: IBlockState, entity: Entity, fluid: Fluid) {
        if (world.isRemote) return
        val server = world as WorldServer
        val entityId = GameAdapter.entityId(entity) ?: return
        val random = world.rand
        for (i in EndReforked.getProxy().registries(world).fluidEntityInteractions.current.find(id(fluid), entityId)) {
            val input = i.item
            val stack = (entity as? EntityItem)?.item
            if (input != null && (stack == null || !fits(input, stack))) continue
            if (!passes(i.chance, i.consumeSource, fluidState, random)) continue

            // Build the result first, so a result that cannot be built changes nothing.
            val spawned: Entity? = when (val r = i.result) {
                is EntityResult.SpawnItem -> EntityItem(world).also { it.item = r.item.toStack() }
                is EntityResult.SpawnEntity -> GameAdapter.createEntity(r.entity, world) ?: continue
                is EntityResult.SetFluidBlock -> null
            }
            val fluidResult = (i.result as? EntityResult.SetFluidBlock)?.let { GameAdapter.state(it.block) ?: continue }

            if (input != null) stack!!.shrink(input.count)
            if (i.removeEntity) server.removeEntity(entity)
            if (spawned != null) {
                spawned.setPosition(entity.posX, entity.posY, entity.posZ)
                server.spawnEntity(spawned)
                consume(server, fluidPos, i.consumeSource, i.consumeChance, random)
            } else {
                server.setBlockState(fluidPos, fluidResult!!)
            }
            break
        }
    }
}

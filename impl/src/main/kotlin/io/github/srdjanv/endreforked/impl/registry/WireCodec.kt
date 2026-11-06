package io.github.srdjanv.endreforked.impl.registry

import io.github.srdjanv.endreforked.api.world.BlockMatch
import io.github.srdjanv.endreforked.wire.s2c.FluidAmount
import io.github.srdjanv.endreforked.wire.s2c.BlockSpec as WireBlock
import io.github.srdjanv.endreforked.wire.s2c.Stack as WireStack
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import org.apache.logging.log4j.LogManager

/*
 * The only place where registry data turns into ids and back. A registry entry holds Minecraft objects, a snapshot holds ids.
 * A decode function throws IllegalArgumentException for an id this client does not have.
 */

private val LOGGER = LogManager.getLogger("endreforked")

/** Decodes the entries of a snapshot. An entry that names something this client does not have is logged and skipped. */
internal fun <W, T> List<W>.decodeEntries(id: (W) -> String, decode: (W) -> T): List<T> = mapNotNull {
    try {
        decode(it)
    } catch (e: IllegalArgumentException) {
        LOGGER.warn("Skipped registry entry {}: {}", id(it), e.message)
        null
    }
}

internal fun ItemStack.toWire() = WireStack(item.registryName.toString(), metadata, count)

internal fun WireStack.toItemStack() =
    ItemStack(Item.REGISTRY.getObject(ResourceLocation(id)) ?: throw IllegalArgumentException("unknown item $id"), count, meta)

internal fun FluidStack.toWire() = FluidAmount(fluid.name, amount)

internal fun Fluid.toWire() = name

internal fun fluidOf(name: String): Fluid = FluidRegistry.getFluid(name) ?: throw IllegalArgumentException("unknown fluid $name")

internal fun FluidAmount.toFluidStack() = FluidStack(fluidOf(id), amount)

@Suppress("UNCHECKED_CAST")
private fun IProperty<*>.nameOf(value: Comparable<*>) = (this as IProperty<Comparable<Any>>).getName(value as Comparable<Any>)

private fun wireBlock(block: Block, properties: Map<IProperty<*>, Comparable<*>>) =
    WireBlock(block.registryName.toString(), properties.entries.associate { (p, v) -> p.name to p.nameOf(v) })

internal fun IBlockState.toWire() = wireBlock(block, properties)

internal fun BlockMatch.toWire() = wireBlock(block, properties)

internal fun WireBlock.toMatch(): BlockMatch {
    val location = ResourceLocation(block)
    require(Block.REGISTRY.containsKey(location)) { "unknown block $block" }
    val b = Block.REGISTRY.getObject(location)
    return BlockMatch(b, properties.entries.associate { (name, value) ->
        val property = b.blockState.properties.firstOrNull { it.name == name } ?: throw IllegalArgumentException("unknown property $name of $block")
        property to (property.parseValue(value).orNull() ?: throw IllegalArgumentException("unknown value $value of $name of $block"))
    })
}

internal fun WireBlock.toState() = toMatch().toState()

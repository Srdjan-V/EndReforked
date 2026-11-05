package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.api.machine.BlockSpec
import io.github.srdjanv.endreforked.api.machine.FluidOut
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.impl.machine.FluidInput
import io.github.srdjanv.endreforked.impl.machine.ItemInput
import io.github.srdjanv.endreforked.impl.machine.ItemKey
import io.github.srdjanv.endreforked.impl.machine.OreResolver
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.oredict.OreDictionary

/** The edge between the id based registries and Minecraft: ItemStack, FluidStack and the ore dictionary. */
object GameAdapter {
    fun ResourceLocation.toId() = Id(namespace, path)

    fun Id.toLocation() = ResourceLocation(namespace, path)

    fun Item.id(): Id = registryName!!.toId()

    fun Block.id(): Id = registryName!!.toId()

    /** Fluids are named without a namespace in 1.12. Forge's default name is `modid:name`. */
    fun Fluid.id(): Id = Id.parseOrNull(FluidRegistry.getDefaultFluidName(this) ?: "") ?: Id("minecraft", name)

    fun block(id: Id): Block? = Block.REGISTRY.getObject(id.toLocation()).takeIf { Block.REGISTRY.containsKey(id.toLocation()) }

    /** The default state of the block with the properties set. Null when the block, a property or a value does not exist. */
    fun state(spec: BlockSpec): IBlockState? {
        var state = block(spec.block)?.defaultState ?: return null
        for ((name, value) in spec.properties) {
            val property = state.propertyKeys.firstOrNull { it.name == name } ?: return null
            state = with(state, property, value) ?: return null
        }
        return state
    }

    private fun <T : Comparable<T>> with(state: IBlockState, property: IProperty<T>, value: String): IBlockState? =
        property.parseValue(value).orNull()?.let { state.withProperty(property, it) }

    fun item(id: Id): Item? = Item.REGISTRY.getObject(id.toLocation())

    fun fluid(id: Id): Fluid? = FluidRegistry.getFluid(id.path)?.takeIf { it.id() == id }

    fun ItemStack.toKey() = ItemKey(item.id(), metadata)

    fun ItemStack.toInput(): ItemInput? = if (isEmpty) null else ItemInput(toKey(), count)

    fun FluidStack?.toInput(): FluidInput? = if (this == null || amount <= 0) null else FluidInput(fluid.id(), amount)

    fun ItemOut.toStack(): ItemStack = item(id)?.let { ItemStack(it, count, meta) } ?: ItemStack.EMPTY

    fun FluidOut.toStack(): FluidStack? = fluid(id)?.let { FluidStack(it, amount) }

    val ores = OreResolver { key, ore ->
        val item = item(key.id) ?: return@OreResolver false
        OreDictionary.getOreIDs(ItemStack(item, 1, key.meta)).any { OreDictionary.getOreName(it) == ore }
    }

    /** Checks that every item and fluid of the recipe exists. Needs the registries to be filled. */
    fun validate(r: MachineRecipe): String? {
        for (i in r.itemInputs) if (i is ItemIngredient.Item && item(i.id) == null) return "unknown item ${i.id}"
        for (i in r.itemOutputs) if (item(i.id) == null) return "unknown item ${i.id}"
        for (f in r.fluidInputs) if (fluid(f.id) == null) return "unknown fluid ${f.id}"
        for (f in r.fluidOutputs) if (fluid(f.id) == null) return "unknown fluid ${f.id}"
        for (event in r.worldEvents) {
            for (block in event.structure.symbols.values) if (state(block) == null) return "unknown block or property in $block"
            when (val a = event.action) {
                is WorldAction.SetBlock -> if (state(a.block) == null) return "unknown block or property in ${a.block}"
                is WorldAction.DropItem -> if (item(a.item.id) == null) return "unknown item ${a.item.id}"
            }
        }
        return null
    }
}

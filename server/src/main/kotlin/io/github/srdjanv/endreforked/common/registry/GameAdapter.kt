package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.api.machine.FluidOut
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.impl.machine.FluidInput
import io.github.srdjanv.endreforked.impl.machine.ItemInput
import io.github.srdjanv.endreforked.impl.machine.ItemKey
import io.github.srdjanv.endreforked.impl.machine.OreResolver
import net.minecraft.block.Block
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
        return null
    }
}

package io.github.srdjanv.endreforked.common.machine

import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.common.registry.GameAdapter
import io.github.srdjanv.endreforked.common.registry.GameAdapter.toInput
import io.github.srdjanv.endreforked.common.registry.GameAdapter.toStack
import io.github.srdjanv.endreforked.impl.machine.FluidInput
import io.github.srdjanv.endreforked.impl.machine.ItemInput
import io.github.srdjanv.endreforked.impl.machine.Match
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.util.NonNullList
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidTank
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.items.ItemStackHandler

/** Item input slots. They only take items that [accepts]. [changed] runs on every change. */
class InputHandler(size: Int, private val accepts: (ItemStack) -> Boolean, private val changed: () -> Unit) :
    ItemStackHandler(size) {
    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack =
        if (accepts(stack)) super.insertItem(slot, stack, simulate) else stack

    override fun onContentsChanged(slot: Int) = changed()
}

/** Item output slots. Other mods and the GUI cannot insert, only the machine can with [insertInternal]. */
class OutputHandler(size: Int, private val changed: () -> Unit) : ItemStackHandler(size) {
    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack = stack

    /** Inserts into any slot. Returns what did not fit. */
    fun insertInternal(stack: ItemStack, simulate: Boolean): ItemStack {
        var rest = stack
        for (slot in 0 until slots) {
            if (rest.isEmpty) break
            rest = super.insertItem(slot, rest, simulate)
        }
        return rest
    }

    override fun onContentsChanged(slot: Int) = changed()
}

/** A fluid input tank. It only takes fluids that [accepts]. */
class InputTank(capacity: Int, private val accepts: (FluidStack) -> Boolean, private val changed: () -> Unit) :
    FluidTank(capacity) {
    override fun fill(resource: FluidStack, doFill: Boolean): Int = if (accepts(resource)) super.fill(resource, doFill) else 0

    override fun setFluid(fluid: FluidStack?) {
        super.setFluid(fluid)
        changed() // the GUI sync sets the fluid without onContentsChanged
    }

    override fun onContentsChanged() = changed()
}

/** A fluid output tank. Only the machine can fill it, with fillInternal. */
class OutputTank(capacity: Int, private val changed: () -> Unit) : FluidTank(capacity) {
    override fun fill(resource: FluidStack, doFill: Boolean): Int = 0

    override fun setFluid(fluid: FluidStack?) {
        super.setFluid(fluid)
        changed()
    }

    override fun onContentsChanged() = changed()
}

/**
 * The inventories of one machine pipeline, as lists. Recipes see them by position: input slot 0, tank 1, and so on.
 * The layout is generic, so a later machine with more slots or tanks needs no new code here.
 */
class MachineIO(
    val itemInputs: InputHandler,
    val itemOutputs: OutputHandler,
    val fluidInputs: List<InputTank>,
    val fluidOutputs: List<OutputTank>,
) : INBTSerializable<NBTTagCompound> {
    fun itemInput(): List<ItemInput?> = List(itemInputs.slots) { itemInputs.getStackInSlot(it).toInput() }

    fun fluidInput(): List<FluidInput?> = fluidInputs.map { it.fluid.toInput() }

    fun isInputEmpty() = itemInput().all { it == null } && fluidInput().all { it == null }

    /**
     * Takes what the match says from the inputs. All or nothing: it first checks every slot and tank
     * and removes only if all of them pass. With [simulate] it only checks.
     */
    fun extract(match: Match, simulate: Boolean): Boolean {
        val actions = ArrayList<() -> Unit>()
        for ((slot, n) in match.itemsTaken.withIndex()) {
            if (n == 0) continue
            if (itemInputs.extractItem(slot, n, true).count != n) return false
            actions += { itemInputs.extractItem(slot, n, false) }
        }
        for ((tank, n) in match.fluidsTaken.withIndex()) {
            if (n == 0) continue
            if (fluidInputs[tank].drain(n, false)?.amount != n) return false
            actions += { fluidInputs[tank].drain(n, true) }
        }
        if (!simulate) actions.forEach { it() }
        return true
    }

    /** True if all outputs of [recipe] fit together. It tries them on copies of the output slots and tanks. */
    fun fits(recipe: MachineRecipe): Boolean {
        val items = ItemStackHandler(NonNullList.from(ItemStack.EMPTY, *Array(itemOutputs.slots) { itemOutputs.getStackInSlot(it).copy() }))
        for (out in recipe.itemOutputs) if (!ItemHandlerHelper.insertItem(items, out.toStack(), false).isEmpty) return false
        val tanks = fluidOutputs.map { FluidTank(it.fluid?.copy(), it.capacity) }
        for (out in recipe.fluidOutputs) {
            val stack = out.toStack() ?: return false
            if (tanks.none { it.fill(stack, true) == stack.amount }) return false
        }
        return true
    }

    /** Puts the outputs of [recipe] into the output slots and tanks. Call [fits] first. */
    fun insertOutputs(recipe: MachineRecipe) {
        for (out in recipe.itemOutputs) itemOutputs.insertInternal(out.toStack(), false)
        for (out in recipe.fluidOutputs) {
            val stack = out.toStack() ?: continue
            fluidOutputs.firstOrNull { it.fillInternal(stack, false) == stack.amount }?.fillInternal(stack, true)
        }
    }

    override fun serializeNBT() = NBTTagCompound().apply {
        setTag("item_inputs", itemInputs.serializeNBT())
        setTag("item_outputs", itemOutputs.serializeNBT())
        setTag("fluid_inputs", tanksToNbt(fluidInputs))
        setTag("fluid_outputs", tanksToNbt(fluidOutputs))
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        itemInputs.deserializeNBT(nbt.getCompoundTag("item_inputs"))
        itemOutputs.deserializeNBT(nbt.getCompoundTag("item_outputs"))
        tanksFromNbt(fluidInputs, nbt.getTagList("fluid_inputs", Constants.NBT.TAG_COMPOUND))
        tanksFromNbt(fluidOutputs, nbt.getTagList("fluid_outputs", Constants.NBT.TAG_COMPOUND))
    }

    private fun tanksToNbt(tanks: List<FluidTank>) = NBTTagList().apply { tanks.forEach { appendTag(it.writeToNBT(NBTTagCompound())) } }

    private fun tanksFromNbt(tanks: List<FluidTank>, list: NBTTagList) {
        for ((i, tank) in tanks.withIndex()) tank.readFromNBT(if (i < list.tagCount()) list.getCompoundTagAt(i) else NBTTagCompound())
    }
}

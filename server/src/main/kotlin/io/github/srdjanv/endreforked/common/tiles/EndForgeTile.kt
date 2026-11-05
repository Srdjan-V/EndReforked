package io.github.srdjanv.endreforked.common.tiles

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.factory.PosGuiData
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.DoubleSyncValue
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler
import com.cleanroommc.modularui.value.sync.GuiSyncManager
import com.cleanroommc.modularui.value.sync.SyncHandlers
import com.cleanroommc.modularui.widgets.FluidSlot
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.ProgressWidget
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import io.github.srdjanv.endreforked.api.base.crafting.TileStatus
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.common.machine.InputHandler
import io.github.srdjanv.endreforked.common.machine.InputTank
import io.github.srdjanv.endreforked.common.machine.MachineIO
import io.github.srdjanv.endreforked.common.machine.MachineProcess
import io.github.srdjanv.endreforked.common.machine.OutputHandler
import io.github.srdjanv.endreforked.common.machine.ProcessHooks
import io.github.srdjanv.endreforked.common.machine.recipes
import io.github.srdjanv.endreforked.common.registry.GameAdapter
import io.github.srdjanv.endreforked.common.registry.GameAdapter.id
import io.github.srdjanv.endreforked.common.registry.GameAdapter.toKey
import io.github.srdjanv.endreforked.common.registry.MachineRecipeDefaults
import io.github.srdjanv.endreforked.common.tiles.base.BaseTileEntity
import io.github.srdjanv.endreforked.common.widgets.BasicTextWidget
import net.minecraft.init.SoundEvents
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.SoundCategory
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.items.CapabilityItemHandler

/**
 * The End Forge: one item slot, one 1,600 mB tank and one output slot, on the machine recipe registry.
 * The inputs stay in their slot and tank until the recipe is done, then the recipe takes its amounts.
 */
class EndForgeTile : BaseTileEntity(), ITickable, IGuiHolder<PosGuiData> {
    private val process: MachineProcess = MachineProcess(
        MachineRecipeDefaults.END_FORGE,
        MachineIO(
            InputHandler(
                1,
                { recipes().acceptsItem(MachineRecipeDefaults.END_FORGE, it.toKey(), GameAdapter.ores) },
                { markDirty(); process.inputChanged() },
            ),
            OutputHandler(1) { markDirty() },
            listOf(
                InputTank(
                    1_600,
                    { recipes().acceptsFluid(MachineRecipeDefaults.END_FORGE, it.fluid.id()) },
                    { markDirty(); process.inputChanged() },
                ),
            ),
            emptyList(),
        ),
        this::recipes,
        object : ProcessHooks {
            override fun onFinished(recipe: MachineRecipe) {
                world.playSound(null, pos, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0f, 1.0f)
            }
        },
    )

    private val itemIn get() = process.io.itemInputs
    private val itemOut get() = process.io.itemOutputs
    private val fluidIn get() = process.io.fluidInputs[0]

    // The last value from the server, for the GUI on the client
    private var percentage = 0.0

    init {
        inventoriesToDrop.add(itemIn)
        inventoriesToDrop.add(itemOut)
    }

    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)
        process.deserializeNBT(compound.getCompoundTag("process"))
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setTag("process", process.serializeNBT())
        return super.writeToNBT(compound)
    }

    override fun update() {
        if (!world.isRemote) process.tick()
    }

    override fun buildUI(data: PosGuiData, syncManager: GuiSyncManager): ModularPanel {
        val panel = ModularPanel.defaultPanel("end_forge_gui").bindPlayerInventory()

        syncManager.syncValue(
            "status",
            SyncHandlers.enumValue(TileStatus::class.java, { process.status }, { process.status = it }),
        )
        syncManager.syncValue("percentage", DoubleSyncValue({ process.progress() }, { percentage = it }))

        if (data.isClient) {
            val textBox = BasicTextWidget().left(20).top(3).right(20).background(GuiTextures.MC_BACKGROUND)
            textBox.setKey { process.status.langKey }
            panel.child(textBox)
            panel.child(
                ProgressWidget().size(18).left(95).top(45)
                    .texture(GuiTextures.PROGRESS_ARROW, 25)
                    .progress { if (process.recipe != null) percentage else 0.0 },
            )
        }

        panel.child(FluidSlot().syncHandler(FluidSlotSyncHandler(fluidIn)).left(25).top(45))
        panel.child(ItemSlot().slot(ModularSlot(itemIn, 0)).left(50).top(45))
        panel.child(ItemSlot().slot(ModularSlot(itemOut, 0).accessibility(false, true)).left(120).top(45))
        return panel
    }

    // Every side but the top. The sides take input, the bottom gives the output.
    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        (capability === CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability === CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) &&
            facing != null && facing != EnumFacing.UP || super.hasCapability(capability, facing)

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? = when {
        facing == null || facing == EnumFacing.UP -> super.getCapability(capability, facing)
        capability === CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY -> fluidIn as T
        capability === CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> (if (facing == EnumFacing.DOWN) itemOut else itemIn) as T
        else -> super.getCapability(capability, facing)
    }
}

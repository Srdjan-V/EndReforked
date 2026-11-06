package io.github.srdjanv.endreforked.common.tiles

import com.cleanroommc.modularui.api.IGuiHolder
import com.cleanroommc.modularui.drawable.GuiTextures
import com.cleanroommc.modularui.drawable.keys.LangKey
import com.cleanroommc.modularui.factory.PosGuiData
import com.cleanroommc.modularui.screen.ModularPanel
import com.cleanroommc.modularui.value.sync.DoubleSyncValue
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler
import com.cleanroommc.modularui.value.sync.GuiSyncManager
import com.cleanroommc.modularui.value.sync.IntSyncValue
import com.cleanroommc.modularui.value.sync.SyncHandler
import com.cleanroommc.modularui.value.sync.SyncHandlers
import com.cleanroommc.modularui.widget.SingleChildWidget
import com.cleanroommc.modularui.widgets.FluidSlot
import com.cleanroommc.modularui.widgets.ItemSlot
import com.cleanroommc.modularui.widgets.ProgressWidget
import com.cleanroommc.modularui.widgets.slot.ModularSlot
import com.cleanroommc.modularui.widgets.slot.SlotGroup
import io.github.srdjanv.endreforked.api.base.crafting.TileStatus
import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.api.entropy.EntropyRadiusUpgrade
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import io.github.srdjanv.endreforked.api.util.Ticker
import io.github.srdjanv.endreforked.common.entropy.chunks.PassiveEntropyChunkDrainer
import io.github.srdjanv.endreforked.common.machine.InputHandler
import io.github.srdjanv.endreforked.common.machine.InputTank
import io.github.srdjanv.endreforked.common.machine.MachineIO
import io.github.srdjanv.endreforked.common.machine.MachineProcess
import io.github.srdjanv.endreforked.common.machine.OutputHandler
import io.github.srdjanv.endreforked.common.machine.OutputTank
import io.github.srdjanv.endreforked.common.machine.recipes
import io.github.srdjanv.endreforked.common.registry.MachineRecipeDefaults
import io.github.srdjanv.endreforked.common.tiles.base.BaseTileEntity
import io.github.srdjanv.endreforked.common.widgets.BasicTextWidget
import io.github.srdjanv.endreforked.utils.LangUtil
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.wrapper.CombinedInvWrapper
import java.util.Optional

/**
 * The entropy chamber has two independent pipelines on the machine recipe registry:
 * items (one input slot, one output slot) and fluids (one 20,000 mB tank each way).
 * A recipe with only item inputs runs in the item pipeline, a recipe with only fluid inputs in the fluid pipeline.
 */
class EntropyChamberTile : BaseTileEntity(), ITickable, EntropyDataProvider, IGuiHolder<PosGuiData> {
    private val reader = EntropyChunkReader.ofTileEntity(this, EntropyRadius.ONE)
    private val drainer = PassiveEntropyChunkDrainer(reader, Ticker(10 * 20), 150) { reader.radius }

    private val upgradeSlot = object : InternalItemStackHandler(1) {
        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack =
            if (stack.item is EntropyRadiusUpgrade) super.insertItem(slot, stack, simulate) else stack
    }

    private val itemProcess: MachineProcess = MachineProcess(
        MachineRecipeDefaults.ENTROPY_CHAMBER,
        MachineIO(
            InputHandler(
                1,
                { recipes().acceptsItem(MachineRecipeDefaults.ENTROPY_CHAMBER, it) },
                { markDirty(); itemProcess.inputChanged() },
            ),
            OutputHandler(1) { markDirty() },
            emptyList(),
            emptyList(),
        ),
        this::recipes,
    )

    private val fluidProcess: MachineProcess = MachineProcess(
        MachineRecipeDefaults.ENTROPY_CHAMBER,
        MachineIO(
            InputHandler(0, { false }) { },
            OutputHandler(0) { },
            listOf(
                InputTank(
                    20_000,
                    { recipes().acceptsFluid(MachineRecipeDefaults.ENTROPY_CHAMBER, it.fluid) },
                    { markDirty(); fluidProcess.inputChanged() },
                ),
            ),
            listOf(OutputTank(20_000) { markDirty() }),
        ),
        this::recipes,
    )

    private val itemIn get() = itemProcess.io.itemInputs
    private val itemOut get() = itemProcess.io.itemOutputs
    private val fluidIn get() = fluidProcess.io.fluidInputs[0]
    private val fluidOut get() = fluidProcess.io.fluidOutputs[0]

    // The last values from the server, for the GUI on the client
    private var itemPercentage = 0.0
    private var fluidPercentage = 0.0
    private var availableEntropy = 0
    private var passiveEntropyDrain = 0

    // New layout: each pipeline writes its own inventories and progress, so the two cannot mix up.
    override fun readFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)
        itemProcess.deserializeNBT(compound.getCompoundTag("item_pipeline"))
        fluidProcess.deserializeNBT(compound.getCompoundTag("fluid_pipeline"))
        if (compound.hasKey("upgrade_slot")) upgradeSlot.deserializeNBT(compound.getCompoundTag("upgrade_slot"))
    }

    override fun writeToNBT(compound: NBTTagCompound): NBTTagCompound {
        compound.setTag("item_pipeline", itemProcess.serializeNBT())
        compound.setTag("fluid_pipeline", fluidProcess.serializeNBT())
        compound.setTag("upgrade_slot", upgradeSlot.serializeNBT())
        return super.writeToNBT(compound)
    }

    override fun getEntropyRadius(): Optional<EntropyRadius> = Optional.of(reader.radius)

    override fun getPassiveDrainer(): Optional<EntropyDataProvider.PassiveDrainer> = Optional.of(drainer.passiveDrainer)

    override fun getActiveDrainer(): Optional<EntropyDataProvider.ActiveDrainer> {
        if (itemProcess.recipe == null && fluidProcess.recipe == null) return Optional.empty()
        return Optional.of(
            EntropyDataProvider.ActiveDrainer { (itemProcess.recipe?.entropy ?: 0) + (fluidProcess.recipe?.entropy ?: 0) },
        )
    }

    override fun update() {
        if (world.isRemote) return
        drainer.drain()
        updateRange()
        itemProcess.tick(reader.entropyView)
        fluidProcess.tick(reader.entropyView)
    }

    private fun updateRange() {
        val stack = upgradeSlot.getStackInSlot(0)
        if (stack.isEmpty) {
            reader.updateRadius(EntropyRadius.ONE)
            return
        }
        val upgrade = stack.item
        if (upgrade is EntropyRadiusUpgrade) reader.updateRadius(upgrade.entropyRadius)
    }

    override fun buildUI(data: PosGuiData, syncManager: GuiSyncManager): ModularPanel {
        val panel = ModularPanel.defaultPanel("entropy_chamber_gui", 176, 190).bindPlayerInventory()

        syncManager.syncValue("item_inv_sync", object : SyncHandler() {
            private var lastIn = ItemStack.EMPTY
            private var lastOut = ItemStack.EMPTY

            override fun readOnClient(id: Int, buf: PacketBuffer) {
                when (id) {
                    0 -> itemIn.setStackInSlot(0, buf.readItemStack())
                    1 -> itemOut.setStackInSlot(0, buf.readItemStack())
                }
            }

            override fun readOnServer(id: Int, buf: PacketBuffer) {}

            override fun detectAndSendChanges(init: Boolean) {
                val inStack = itemIn.getStackInSlot(0)
                val outStack = itemOut.getStackInSlot(0)
                if (init || !ItemStack.areItemStacksEqual(inStack, lastIn) || !ItemStack.areItemStacksEqual(outStack, lastOut)) {
                    lastIn = inStack.copy()
                    lastOut = outStack.copy()
                    sync(0) { it.writeItemStack(inStack) }
                    sync(1) { it.writeItemStack(outStack) }
                }
            }
        })
        syncManager.syncValue("fluid_inv_sync", object : SyncHandler() {
            private var lastIn: FluidStack? = null
            private var lastOut: FluidStack? = null

            override fun readOnClient(id: Int, buf: PacketBuffer) {
                when (id) {
                    0 -> fluidIn.readFromNBT(buf.readCompoundTag()!!)
                    1 -> fluidOut.readFromNBT(buf.readCompoundTag()!!)
                }
            }

            override fun readOnServer(id: Int, buf: PacketBuffer) {}

            override fun detectAndSendChanges(init: Boolean) {
                val inFluid = fluidIn.fluid
                val outFluid = fluidOut.fluid
                if (init || !same(inFluid, lastIn) || !same(outFluid, lastOut)) {
                    lastIn = inFluid?.copy()
                    lastOut = outFluid?.copy()
                    sync(0) { it.writeCompoundTag(fluidIn.writeToNBT(NBTTagCompound())) }
                    sync(1) { it.writeCompoundTag(fluidOut.writeToNBT(NBTTagCompound())) }
                }
            }

            private fun same(a: FluidStack?, b: FluidStack?) = if (a == null || b == null) a === b else a.isFluidStackIdentical(b)
        })

        syncManager.syncValue(
            "item_status",
            SyncHandlers.enumValue(TileStatus::class.java, { itemProcess.status }, { itemProcess.status = it }),
        )
        syncManager.syncValue("item_percentage", DoubleSyncValue({ itemProcess.progress() }, { itemPercentage = it }))
        syncManager.syncValue(
            "fluid_status",
            SyncHandlers.enumValue(TileStatus::class.java, { fluidProcess.status }, { fluidProcess.status = it }),
        )
        syncManager.syncValue("fluid_percentage", DoubleSyncValue({ fluidProcess.progress() }, { fluidPercentage = it }))

        syncManager.syncValue(
            "available_entropy",
            IntSyncValue({ reader.entropyView.currentEntropy }, { availableEntropy = it }),
        )
        syncManager.syncValue(
            "passive_entropy_drain",
            IntSyncValue({ drainer.passiveDrainer.drained }, { passiveEntropyDrain = it }),
        )

        if (data.isClient) {
            val itemTextBox = BasicTextWidget().left(20).top(3).right(20).background(GuiTextures.MC_BACKGROUND)
            itemTextBox.setKey { itemProcess.status.langKey }
            panel.child(itemTextBox)
            panel.child(
                ProgressWidget().size(18).left(77).top(23)
                    .texture(GuiTextures.PROGRESS_ARROW, 25)
                    .progress { if (itemProcess.recipe != null) itemPercentage else 0.0 },
            )
        }

        val itemInSlot = ItemSlot().slot(ModularSlot(itemIn, 0).slotGroup(SlotGroup.singleton("item_in", 100))).left(55).top(23)
        itemInSlot.tooltip().setAutoUpdate(true).tooltipBuilder { tooltip ->
            if (itemProcess.recipe != null) {
                tooltip.addLine(
                    "${LangUtil.translateToLocal("status.item.matching")} ${LangUtil.translateToLocal("status.item.item")}, " +
                        LangUtil.translateToLocal("status.item.damage"),
                )
            }
        }
        panel.child(itemInSlot)
        panel.child(ItemSlot().slot(ModularSlot(itemOut, 0)).left(100).top(23))

        if (data.isClient) {
            val fluidTextBox = BasicTextWidget().left(20).top(44).right(20).background(GuiTextures.MC_BACKGROUND)
            fluidTextBox.setKey { fluidProcess.status.langKey }
            panel.child(fluidTextBox)
            panel.child(
                ProgressWidget().size(18).left(77).top(64)
                    .texture(GuiTextures.PROGRESS_ARROW, 25)
                    .progress { if (fluidProcess.recipe != null) fluidPercentage else 0.0 },
            )
        }
        val fluidInSlot = FluidSlot().syncHandler(FluidSlotSyncHandler(fluidIn)).left(55).top(64)
        fluidInSlot.tooltip().setAutoUpdate(true).tooltipBuilder { tooltip ->
            if (fluidProcess.recipe != null) {
                tooltip.addLine("${LangUtil.translateToLocal("status.fluid.matching")} ${LangUtil.translateToLocal("status.fluid.fluid")}")
            }
        }
        panel.child(fluidInSlot)
        panel.child(FluidSlot().syncHandler(FluidSlotSyncHandler(fluidOut)).left(100).top(64))

        if (data.isClient) {
            val entropyLevel = BasicTextWidget().left(20).right(20).top(85).background(GuiTextures.MC_BACKGROUND)
            entropyLevel.tooltip().tooltipBuilder { tooltip ->
                tooltip.addLine(LangKey("entropy.drainer.passive").get() + ": " + passiveEntropyDrain)
            }
            entropyLevel.setKey { availableEntropy.toString() }
            panel.child(entropyLevel)
        }

        val upgradeSlotWidget = UpgradeBox().size(25).leftRel(1f).background(GuiTextures.MC_BACKGROUND)
        panel.child(upgradeSlotWidget)
        val upgrade = ItemSlot().slot(
            ModularSlot(upgradeSlot, 0).slotGroup(SlotGroup.singleton("upgrade_item_in", 110)),
        ).top(3).left(3)
        upgrade.tooltip().setAutoUpdate(true).tooltipBuilder { tooltip ->
            val item = upgrade.ingredient
            if (item is EntropyRadiusUpgrade) {
                val range = item.entropyRadius
                tooltip.addLine(LangKey("entropy.range.chunks").get() + ": " + range.chunks)
                tooltip.addLine(LangKey("entropy.range.chunks").get() + ": " + range.radius)
            }
        }
        data.jeiSettings.addJeiExclusionArea(upgradeSlotWidget)
        upgradeSlotWidget.child(upgrade)

        return panel
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean =
        capability === CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ||
            capability === CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
            super.hasCapability(capability, facing)

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? = when {
        capability === CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ->
            FluidHandlerConcatenate(fluidOut, fluidIn) as T
        capability === CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ->
            CombinedInvWrapper(itemIn, itemOut) as T
        else -> super.getCapability(capability, facing)
    }

    private class UpgradeBox : SingleChildWidget<UpgradeBox>()
}

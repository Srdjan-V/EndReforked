package io.github.srdjanv.endreforked.common.tiles;

import java.io.IOException;
import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.keys.LangKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import io.github.srdjanv.endreforked.api.base.crafting.TileStatus;
import io.github.srdjanv.endreforked.api.base.crafting.processors.BaseRecipeProcessor;
import io.github.srdjanv.endreforked.api.base.crafting.processors.ProcessorExecutor;
import io.github.srdjanv.endreforked.api.base.crafting.processors.ProcessorValidator;
import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider;
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius;
import io.github.srdjanv.endreforked.api.entropy.EntropyRadiusUpgrade;
import io.github.srdjanv.endreforked.api.entropy.chamber.*;
import io.github.srdjanv.endreforked.api.entropy.world.EntropyChunkReader;
import io.github.srdjanv.endreforked.api.util.Ticker;
import io.github.srdjanv.endreforked.common.entropy.chunks.PassiveEntropyChunkDrainer;
import io.github.srdjanv.endreforked.common.tiles.base.BaseTileEntity;
import io.github.srdjanv.endreforked.common.widgets.BasicTextWidget;

public class EntropyChamberTile extends BaseTileEntity
                                implements ITickable, EntropyDataProvider, IGuiHolder<PosGuiData> {

    private final EntropyChunkReader reader;
    private final PassiveEntropyChunkDrainer drainer;
    private int availableEntropy;
    private int passiveEntropyDrain;

    private final InternalItemStackHandler upgradeSlot = new InternalItemStackHandler(1) {

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.getItem() instanceof EntropyRadiusUpgrade) return super.insertItem(slot, stack, simulate);
            return stack;
        }
    };

    private TileStatus itemStatus = TileStatus.IDLE;
    private double itemPercentage;
    private final InternalItemStackHandler itemIn = new InternalItemStackHandler(1) {

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot == 0) {
                if (itemProcessor.validateRecipe(stack))
                    return super.insertItem(slot, stack, simulate);
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };
    private final InternalItemStackHandler itemOut = new InternalItemStackHandler(1) {

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot == 0) return stack;
            if (slot == -1) return super.insertItem(0, stack, simulate);
            return super.insertItem(slot, stack, simulate);
        }
    };
    private final BaseRecipeProcessor<ItemStack, ItemStack, ItemChamberRecipe> itemProcessor;
    private final ProcessorValidator<ItemStack, ItemStack, ItemChamberRecipe> itemProcessorValidator;
    private final ProcessorExecutor<ItemStack, ItemStack, ItemChamberRecipe> itemProcessorExecutor;

    private TileStatus fluidStatus = TileStatus.IDLE;
    private double fluidPercentage;
    private final InternalFluidTank fluidIn = new InternalFluidTank(20_000) {

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (fluidProcessor.validateRecipe(resource))
                return super.fill(resource, doFill);
            return 0;
        }
    };
    private final InternalFluidTank fluidOut = new InternalFluidTank(20_000) {

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }
    };
    private final BaseRecipeProcessor<FluidStack, FluidStack, FluidChamberRecipe> fluidProcessor;
    private final ProcessorValidator<FluidStack, FluidStack, FluidChamberRecipe> fluidProcessorValidator;
    private final ProcessorExecutor<FluidStack, FluidStack, FluidChamberRecipe> fluidProcessorExecutor;

    public EntropyChamberTile() {
        reader = EntropyChunkReader.ofTileEntity(this, EntropyRadius.ONE);
        drainer = new PassiveEntropyChunkDrainer(
                reader, new Ticker(10 * 20),
                150,
                reader::getRadius);

        itemProcessor = new BaseRecipeProcessor<>(EntropyItemChamberHandler.INSTANCE);
        itemProcessorValidator = ProcessorValidator.item2ItemOf(
                itemProcessor,
                this::updateItemStatus,
                () -> reader,
                () -> itemIn.getStackInSlot(0));

        itemProcessorExecutor = new ProcessorExecutor<>(
                itemProcessor,
                this::updateItemStatus,
                () -> reader,
                () -> itemIn.getStackInSlot(0),
                stack -> itemOut.insertItem(-1, stack, true).isEmpty(),
                stack -> itemIn.extractItem(0, stack.getCount(), true).getCount() == stack.getCount(),
                (itemInput, outputStack) -> {
                    itemIn.extractItem(0, itemInput.getCount(), false);
                    itemOut.insertItem(-1, outputStack, false);
                });

        fluidProcessor = new BaseRecipeProcessor<>(EntropyFluidChamberHandler.INSTANCE);
        fluidProcessorValidator = ProcessorValidator.fluid2FluidOf(
                fluidProcessor,
                this::updateFluidStatus,
                () -> reader,
                fluidIn::getFluid);

        fluidProcessorExecutor = new ProcessorExecutor<>(
                fluidProcessor,
                this::updateFluidStatus,
                () -> reader,
                fluidIn::getFluid,
                fluid -> fluidOut.fillTileInternal(fluid, false) == fluid.amount,
                fluid -> {
                    var drainIn = fluidIn.drain(fluid.amount, false);
                    return drainIn != null && drainIn.amount == fluid.amount;
                },
                (fluidInput, fluidOut) -> {
                    fluidIn.drain(fluidInput.amount, true);
                    this.fluidOut.fillTileInternal(fluidOut, true);
                });
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        if (compound.hasKey("item_in"))
            itemIn.deserializeNBT(compound.getCompoundTag("item_in"));
        if (compound.hasKey("item_out"))
            itemOut.deserializeNBT(compound.getCompoundTag("item_out"));
        if (compound.hasKey("item_executor"))
            itemProcessorExecutor.deserializeNBT(compound.getCompoundTag("item_executor"));

        if (compound.hasKey("fluid_in"))
            fluidIn.readFromNBT(compound.getCompoundTag("fluid_in"));
        if (compound.hasKey("fluid_out"))
            fluidOut.readFromNBT(compound.getCompoundTag("fluid_out"));
        if (compound.hasKey("fluid_executor"))
            itemProcessorExecutor.deserializeNBT(compound.getCompoundTag("fluid_executor"));

        if (compound.hasKey("upgrade_slot"))
            upgradeSlot.deserializeNBT(compound.getCompoundTag("upgrade_slot"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("item_in", itemIn.serializeNBT());
        compound.setTag("item_out", itemOut.serializeNBT());
        compound.setTag("item_executor", itemProcessorExecutor.serializeNBT());

        compound.setTag("fluid_in", fluidIn.writeToNBT(new NBTTagCompound()));
        compound.setTag("fluid_out", fluidOut.writeToNBT(new NBTTagCompound()));
        compound.setTag("fluid_executor", fluidProcessorExecutor.serializeNBT());

        compound.setTag("upgrade_slot", upgradeSlot.serializeNBT());

        return super.writeToNBT(compound);
    }

    @Override
    public Optional<EntropyRadius> getEntropyRadius() {
        return Optional.of(reader.getRadius());
    }

    @Override
    public Optional<PassiveDrainer> getPassiveDrainer() {
        return Optional.of(drainer.getPassiveDrainer());
    }

    @Override
    public Optional<ActiveDrainer> getActiveDrainer() {
        if (!itemProcessor.hasRecipe() && !fluidProcessor.hasRecipe()) return Optional.empty();
        return Optional.of(() -> {
            int cost = 0;
            if (itemProcessor.hasRecipe()) cost = itemProcessor.getRecipe().getEntropyCost();
            if (fluidProcessor.hasRecipe()) cost += fluidProcessor.getRecipe().getEntropyCost();
            return cost;
        });
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, GuiSyncManager syncManager) {
        ModularPanel panel = ModularPanel.defaultPanel("entropy_chamber_gui", 176, 190).bindPlayerInventory();

        syncManager.syncValue("item_inv_sync", new SyncHandler() {

            @Override
            public void readOnClient(int id, PacketBuffer buf) throws IOException {
                switch (id) {
                    case 0 -> itemIn.setStackInSlot(0, buf.readItemStack());
                    case 1 -> itemOut.setStackInSlot(0, buf.readItemStack());
                }
            }

            @Override
            public void readOnServer(int id, PacketBuffer buf) {}

            private ItemStack itemStackIn = ItemStack.EMPTY;
            private boolean valid;

            @Override
            public void detectAndSendChanges(boolean init) {
                if (init || invChanged()) {
                    sync(0, buffer -> buffer.writeItemStack(itemIn.getStackInSlot(0)));
                    sync(1, buffer -> buffer.writeItemStack(itemOut.getStackInSlot(0)));
                }
            }

            private boolean invChanged() {
                var changed = false;
                var valid = itemProcessor.hasRecipe();
                if (this.valid != valid) {
                    this.valid = valid;
                    changed = true;
                }
                if (valid) {
                    if (!itemProcessor.getHandlerRegistry().getHashStrategy()
                            .equals(itemIn.getStackInSlot(0), itemStackIn)) {
                        itemStackIn = itemIn.getStackInSlot(0);
                        changed = true;
                    }
                }
                return changed;
            }
        });
        syncManager.syncValue("fluid_inv_sync", new SyncHandler() {

            @Override
            public void readOnClient(int id, PacketBuffer buf) throws IOException {
                switch (id) {
                    case 0 -> fluidIn.readFromNBT(buf.readCompoundTag());
                    case 1 -> fluidOut.readFromNBT(buf.readCompoundTag());
                }
            }

            @Override
            public void readOnServer(int id, PacketBuffer buf) {}

            private FluidStack fluidStack;
            private boolean valid;

            @Override
            public void detectAndSendChanges(boolean init) {
                if (init || tanksChanged()) {
                    sync(0, buffer -> buffer.writeCompoundTag(fluidIn.writeToNBT(new NBTTagCompound())));
                    sync(1, buffer -> buffer.writeCompoundTag(fluidOut.writeToNBT(new NBTTagCompound())));
                }
            }

            private boolean tanksChanged() {
                var changed = false;
                var valid = fluidProcessor.hasRecipe();
                if (this.valid != valid) {
                    this.valid = valid;
                    changed = true;
                }
                if (valid) {
                    if (!fluidProcessor.getHandlerRegistry().getHashStrategy().equals(fluidIn.getFluid(), fluidStack)) {
                        fluidStack = fluidIn.getFluid();
                        changed = true;
                    }
                }
                return changed;
            }
        });

        syncManager.syncValue("item_status",
                SyncHandlers.enumValue(TileStatus.class, () -> itemStatus, status -> this.itemStatus = status));
        syncManager.syncValue("item_percentage",
                new DoubleSyncValue(itemProcessorExecutor::getPercentage, percentage -> itemPercentage = percentage));

        syncManager.syncValue("fluid_status",
                SyncHandlers.enumValue(TileStatus.class, () -> fluidStatus, status -> this.fluidStatus = status));
        syncManager.syncValue("fluid_percentage",
                new DoubleSyncValue(fluidProcessorExecutor::getPercentage, percentage -> fluidPercentage = percentage));

        syncManager.syncValue("available_entropy",
                new IntSyncValue(
                        () -> reader.getEntropyView().getCurrentEntropy(),
                        availableEntropy -> this.availableEntropy = availableEntropy));
        syncManager.syncValue("passive_entropy_drain",
                new IntSyncValue(
                        () -> drainer.getPassiveDrainer().getDrained(),
                        passiveEntropyDrain -> this.passiveEntropyDrain = passiveEntropyDrain));

        if (data.isClient()) {
            var itemTextBox = new BasicTextWidget().left(20).top(3).right(20).background(GuiTextures.MC_BACKGROUND);
            itemTextBox.setKey(() -> itemStatus.getLangKey());
            panel.child(itemTextBox);
            panel.child(new ProgressWidget()
                    .size(18).left(77).top(23)
                    .texture(GuiTextures.PROGRESS_ARROW, 25)
                    .progress(() -> {
                        if (itemProcessor.validateRecipe(this.itemIn.getStackInSlot(0)))
                            return itemPercentage;
                        return 0;
                    }));
        }

        var itemIn = new ItemSlot().slot(
                new ModularSlot(this.itemIn, 0)
                        .slotGroup(SlotGroup.singleton("item_in", 100)))
                .left(55).top(23);
        itemIn.tooltip().setAutoUpdate(true).tooltipBuilder(tooltip -> {
            if (itemProcessor.validateRecipe(this.itemIn.getStackInSlot(0)))
                tooltip.addLine(itemProcessor.getHandlerRegistry().translateHashStrategy());
        });
        panel.child(itemIn);

        var itemOut = new ItemSlot().slot(new ModularSlot(this.itemOut, 0)).left(100).top(23);
        panel.child(itemOut);

        if (data.isClient()) {
            var fluidTextBox = new BasicTextWidget().left(20).top(44).right(20).background(GuiTextures.MC_BACKGROUND);
            fluidTextBox.setKey(() -> fluidStatus.getLangKey());
            panel.child(fluidTextBox);
            panel.child(new ProgressWidget()
                    .size(18).left(77).top(64)
                    .texture(GuiTextures.PROGRESS_ARROW, 25)
                    .progress(() -> {
                        if (fluidProcessor.validateRecipe(fluidIn.getFluid()))
                            return fluidPercentage;
                        return 0;
                    }));
        }
        var fluidIn = new FluidSlot().syncHandler(new FluidSlotSyncHandler(this.fluidIn)).left(55).top(64);
        fluidIn.tooltip().setAutoUpdate(true).tooltipBuilder(tooltip -> {
            if (fluidProcessor.validateRecipe(fluidIn.getFluidStack()))
                tooltip.addLine(fluidProcessor.getHandlerRegistry().translateHashStrategy());
        });
        panel.child(fluidIn);

        var fluidOut = new FluidSlot().syncHandler(new FluidSlotSyncHandler(this.fluidOut)).left(100).top(64);
        panel.child(fluidOut);

        if (data.isClient()) {
            var entropyLevel = new BasicTextWidget().left(20).right(20).top(85).background(GuiTextures.MC_BACKGROUND);
            entropyLevel.tooltip().tooltipBuilder(tooltip -> tooltip
                    .addLine(new LangKey("entropy.drainer.passive").get() + ": " + passiveEntropyDrain));
            entropyLevel.setKey(() -> String.valueOf(availableEntropy));
            panel.child(entropyLevel);
        }

        var upgradeSlotWidget = new SingleChildWidget<>()
                .size(25).leftRel(1)
                .background(GuiTextures.MC_BACKGROUND);
        panel.child(upgradeSlotWidget);
        final var upgradeSlot = new ItemSlot().slot(
                new ModularSlot(this.upgradeSlot, 0)
                        .slotGroup(SlotGroup.singleton("upgrade_item_in", 110)))
                .top(3).left(3);
        upgradeSlot.tooltip().setAutoUpdate(true).tooltipBuilder(tooltip -> {
            if (upgradeSlot.getIngredient() instanceof EntropyRadiusUpgrade upgrade) {
                var range = upgrade.getEntropyRadius();
                var rangeChunksKey = new LangKey("entropy.range.chunks");
                tooltip.addLine(rangeChunksKey.get() + ": " + range.getChunks());
                var rangeRadiusKey = new LangKey("entropy.range.chunks");
                tooltip.addLine(rangeRadiusKey.get() + ": " + range.getRadius());
            }
        });
        data.getJeiSettings().addJeiExclusionArea(upgradeSlotWidget);
        upgradeSlotWidget.child(upgradeSlot);

        return panel;
    }

    @Override
    public void update() {
        if (world.isRemote) return;
        drainer.drain();
        updateRange();
        if (itemProcessorValidator.prepare()) itemProcessorExecutor.execute();
        if (fluidProcessorValidator.prepare()) fluidProcessorExecutor.execute();
    }

    private void updateRange() {
        ItemStack stackInSlot = upgradeSlot.getStackInSlot(0);
        if (stackInSlot.isEmpty()) {
            reader.updateRadius(EntropyRadius.ONE);
            return;
        }
        if (stackInSlot.getItem() instanceof EntropyRadiusUpgrade upgrade) {
            reader.updateRadius(upgrade.getEntropyRadius());
        }
    }

    private void updateItemStatus(TileStatus status) {
        if (this.itemStatus != status) {
            this.itemStatus = status;
        }
    }

    private void updateFluidStatus(TileStatus status) {
        if (this.fluidStatus != status) {
            this.fluidStatus = status;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY == capability) {
            return true;
        } else if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY == capability) {
            return true;
        }

        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY == capability) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandlerConcatenate(fluidOut, fluidIn));
        } else if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY == capability) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new CombinedInvWrapper(itemIn, itemOut));
        }

        return super.getCapability(capability, facing);
    }
}

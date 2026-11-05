package io.github.srdjanv.endreforked.common.tiles;

import java.util.Objects;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.api.base.crafting.TileStatus;
import io.github.srdjanv.endreforked.api.base.crafting.groupings.Fluid2ItemGrouping;
import io.github.srdjanv.endreforked.api.base.crafting.processors.FluidItemRecipeProcessor;
import io.github.srdjanv.endreforked.api.endforge.EndForgeHandler;
import io.github.srdjanv.endreforked.api.endforge.EndForgeRecipe;
import io.github.srdjanv.endreforked.common.tiles.base.BaseTileEntity;

public class EndForgeTile extends BaseTileEntity implements ITickable, IGuiHolder<PosGuiData> {

    private final ItemStackHandler intput = new InternalItemStackHandler(1);

    private final ItemStackHandler processingInventory = new InternalItemStackHandler(1);

    private final ItemStackHandler output = new InternalItemStackHandler(1) {

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return switch (slot) {
                case 0 -> stack;
                case -1 -> super.insertItem(0, stack, simulate);
                default -> super.insertItem(slot, stack, simulate);
            };
        }
    };

    private final FluidTank inputTank = new InternalFluidTank(1600);
    private final FluidTank processingFluid = new InternalFluidTank(1600);

    private TileStatus status = TileStatus.IDLE;
    private final FluidItemRecipeProcessor<ItemStack, Fluid2ItemGrouping<EndForgeRecipe>, EndForgeRecipe> recipeProcessor;
    private int ticksRun;

    public EndForgeTile() {
        recipeProcessor = new FluidItemRecipeProcessor<>(EndForgeHandler.getInstance());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        ticksRun = compound.getInteger("ticksRun");

        if (compound.hasKey("inputInv"))
            intput.deserializeNBT(compound.getCompoundTag("inputInv"));
        if (compound.hasKey("outInventory"))
            output.deserializeNBT(compound.getCompoundTag("outInv"));
        if (compound.hasKey("processInv"))
            processingInventory.deserializeNBT(compound.getCompoundTag("processInv"));

        if (compound.hasKey("inputFluid")) {
            var stack = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("inputFluid"));
            if (Objects.nonNull(stack)) inputTank.setFluid(stack);
        }
        if (compound.hasKey("processFluid")) {
            var stack = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("processFluid"));
            if (Objects.nonNull(stack)) processingFluid.setFluid(stack);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("ticksRun", ticksRun);
        compound.setTag("inputInv", intput.serializeNBT());
        compound.setTag("outInventory", output.serializeNBT());
        compound.setTag("processInv", processingInventory.serializeNBT());

        if (Objects.nonNull(inputTank.getFluid()))
            compound.setTag("inputFluid", inputTank.getFluid().writeToNBT(new NBTTagCompound()));
        if (Objects.nonNull(processingFluid.getFluid()))
            compound.setTag("processFluid", processingFluid.getFluid().writeToNBT(new NBTTagCompound()));
        return super.writeToNBT(compound);
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (prepare()) progress();
        }
    }

    private boolean prepare() {
        var processingItemStack = processingInventory.getStackInSlot(0);
        var processingFluidStack = this.processingFluid.getFluid();
        boolean validProcessingRecipe = true;
        if (processingItemStack.isEmpty() || Objects.isNull(processingFluidStack)) {
            if (!(processingItemStack.isEmpty() && Objects.isNull(processingFluidStack)))
                EndReforked.LOGGER.warn("Clearing processing items in {} at {}, items {}, {}",
                        this.getClass(), this.pos, processingItemStack, processingFluidStack);
            updateStatus(TileStatus.IDLE);
            resetProcessingInvAndFluid();
            validProcessingRecipe = false;
        }

        if (validProcessingRecipe) {
            if (!recipeProcessor.validateGrouping(processingFluidStack)) return false;
            if (!recipeProcessor.validateRecipe(processingItemStack)) return false;

        } else {
            boolean invalid = false;

            var inputFluidStack = inputTank.getFluid();
            if (!recipeProcessor.validateGrouping(inputFluidStack)) invalid = true;

            var inputItem = intput.getStackInSlot(0);
            if (!recipeProcessor.validateRecipe(inputItem)) invalid = true;

            if (invalid) {
                if ((Objects.nonNull(inputFluidStack) && inputFluidStack.amount == 0) && inputItem.isEmpty()) {
                    updateStatus(TileStatus.IDLE);
                } else updateStatus(TileStatus.INVALID);
                return false;
            }

            var fluidStackToMatch = recipeProcessor.getRecipeGrouping().getGrouping();
            var drainedFluidStack = inputTank.drain(fluidStackToMatch, false);

            if (!intput.extractItem(0, recipeProcessor.getRecipe().getInput2().getCount(), true).isEmpty() &&
                    Objects.nonNull(drainedFluidStack) && drainedFluidStack.amount == fluidStackToMatch.amount) {
                processingInventory.insertItem(0,
                        intput.extractItem(0, recipeProcessor.getRecipe().getInput2().getCount(), false),
                        false);

                processingFluid.fill(inputTank.drain(fluidStackToMatch, true), true);
            } else {
                updateStatus(TileStatus.INVALID);
                return false;
            }
        }
        return true;
    }

    private void progress() {
        // finished
        if (ticksRun >= recipeProcessor.getRecipe().getTicksToComplete()) {
            var outputStack = recipeProcessor.getRecipe().getRecipeFunction().apply(
                    processingFluid.getFluid(),
                    processingInventory.getStackInSlot(0));
            if (output.insertItem(-1, outputStack, true).isEmpty()) {
                output.insertItem(-1, outputStack, false);
                world.playSound(null, pos, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                resetProcessingInvAndFluid();
                reset();
            } else updateStatus(TileStatus.OUT_FULL);
        } else {
            updateStatus(TileStatus.RUNNING);
            ticksRun++;
        }
    }

    private void resetProcessingInvAndFluid() {
        processingInventory.setStackInSlot(0, ItemStack.EMPTY);
        processingFluid.setFluid(null);
    }

    private void reset() {
        ticksRun = 0;
        markDirty();
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, GuiSyncManager syncManager) {
        ModularPanel panel = ModularPanel.defaultPanel("materializer_gui").bindPlayerInventory();

        class BasicTextWidget extends Widget<BasicTextWidget> {

            @Override
            public void setValue(IValue<?> value) {
                super.setValue(value);
            }

            @Override
            public void draw(GuiContext context, WidgetTheme widgetTheme) {
                var newStatus = (String) getValue().getValue();
                var trueStatus = TileStatus.valueOf(newStatus);;
                IKey.lang(trueStatus.getLangKey()).drawAtZero(context, getArea());
            }
        }

        var textBox = new BasicTextWidget().left(20).top(3).right(20).background(GuiTextures.MC_BACKGROUND);
        textBox.setValue(new StringSyncValue(() -> status.name(), null));
        panel.child(textBox);

        class BasicTextSyncingWidget extends Widget<BasicTextSyncingWidget> {

            @Override
            public void setValue(IValue<?> value) {
                super.setValue(value);
            }
        }

        // TODO: 28/10/2023 center
        panel.child(new FluidSlot().syncHandler(new FluidSlotSyncHandler(inputTank)).left(25).top(45));
        panel.child(new ItemSlot().slot(new ModularSlot(intput, 0)).left(50).top(45));
        panel.child(new ItemSlot().slot(new ModularSlot(output, 0).accessibility(false, true)).left(120).top(45));

        var process = new ProgressWidget()
                .size(18).left(95).top(45)
                .texture(GuiTextures.PROGRESS_ARROW, 25)
                .value(new DoubleSyncValue(() -> {
                    if (recipeProcessor.getRecipe() != null)
                        return (double) ticksRun / recipeProcessor.getRecipe().getTicksToComplete();
                    return 0;
                }, null));
        panel.child(process);

        return panel;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            switch (facing) {
                case NORTH, SOUTH, WEST, EAST, DOWN -> {
                    return true;
                }
                case UP -> {
                    return false;
                }
            }
        }

        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            switch (facing) {
                case NORTH, SOUTH, WEST, EAST, DOWN -> {
                    return true;
                }
                case UP -> {
                    return false;
                }
            }
        }

        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            switch (facing) {
                case NORTH, SOUTH, WEST, EAST, DOWN -> {
                    return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(inputTank);
                }
            }
        }

        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            switch (facing) {
                case NORTH, SOUTH, WEST, EAST -> {
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(intput);
                }
                case DOWN -> {
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(output);
                }
            }
        }

        return super.getCapability(capability, facing);
    }

    private void updateStatus(TileStatus status) {
        if (this.status != status) {
            this.status = status;
        }
    }
}

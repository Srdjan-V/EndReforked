package io.github.srdjanv.endreforked.common.tiles.base;

import java.util.List;

import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BaseTileEntity extends TileEntity {

    protected final List<ItemStackHandler> inventoriesToDrop = new ObjectArrayList<>();

    public BaseTileEntity() {}

    protected class InternalItemStackHandler extends ItemStackHandler {

        public InternalItemStackHandler() {}

        public InternalItemStackHandler(int size) {
            super(size);
        }

        public InternalItemStackHandler(NonNullList<ItemStack> stacks) {
            super(stacks);
        }

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    }

    protected class InternalFluidTank extends FluidTank {

        public InternalFluidTank(int capacity) {
            super(capacity);
        }

        public InternalFluidTank(@Nullable FluidStack fluidStack, int capacity) {
            super(fluidStack, capacity);
        }

        public InternalFluidTank(Fluid fluid, int amount, int capacity) {
            super(fluid, amount, capacity);
        }

        public int fillTileInternal(FluidStack resource, boolean doFill) {
            return super.fill(resource, doFill);
        }

        @Override
        protected void onContentsChanged() {
            markDirty();
        }
    }

    public void dopItems() {
        for (ItemStackHandler itemStackHandler : inventoriesToDrop) {
            for (int i = 0; i < itemStackHandler.getSlots(); ++i) {
                ItemStack itemstack = itemStackHandler.getStackInSlot(i);

                if (!itemstack.isEmpty()) {
                    InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemstack);
                }
            }
        }
    }
}

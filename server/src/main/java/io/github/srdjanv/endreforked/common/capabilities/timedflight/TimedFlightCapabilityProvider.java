package io.github.srdjanv.endreforked.common.capabilities.timedflight;

import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.capabilities.timedflight.ITimedFlight;

public class TimedFlightCapabilityProvider implements ICapabilitySerializable<NBTTagInt> {

    private final ITimedFlight timedFlight = CapabilityTimedFlightHandler.INSTANCE.getDefaultInstance();

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityTimedFlightHandler.INSTANCE;
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityTimedFlightHandler.INSTANCE ?
                CapabilityTimedFlightHandler.INSTANCE.cast(timedFlight) : null;
    }

    @Override
    public NBTTagInt serializeNBT() {
        return (NBTTagInt) CapabilityTimedFlightHandler.INSTANCE.writeNBT(timedFlight, null);
    }

    @Override
    public void deserializeNBT(NBTTagInt nbt) {
        CapabilityTimedFlightHandler.INSTANCE.readNBT(timedFlight, null, nbt);
    }
}

package io.github.srdjanv.endreforked.api.capabilities.timedflight;

import net.minecraft.entity.player.EntityPlayerMP;

public interface ITimedFlight {

    int getFlightDuration();

    void setFlightDuration(int flightDuration);

    void tickPlayer(EntityPlayerMP playerMP);
}

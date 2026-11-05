package io.github.srdjanv.endreforked.common.capabilities.timedflight;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;

import io.github.srdjanv.endreforked.api.capabilities.timedflight.ITimedFlight;
import io.github.srdjanv.endreforked.common.ModPotions;

public class TimedFlight implements ITimedFlight {

    private boolean sendStartUpdate;
    private boolean sendStopUpdate;
    private int flightDuration;

    @Override
    public int getFlightDuration() {
        return flightDuration;
    }

    @Override
    public void setFlightDuration(int flightDuration) {
        if (flightDuration <= 0) return;
        this.flightDuration = flightDuration;
        sendStopUpdate = true;
        sendStartUpdate = true;
    }

    @Override
    public void tickPlayer(EntityPlayerMP playerMP) {
        if (flightDuration > 0) {
            if (sendStartUpdate) {
                sendStartUpdate = false;
                sendUpdate(playerMP, true);
            }
            flightDuration--;
        } else if (sendStopUpdate) {
            sendStopUpdate = false;
            sendUpdate(playerMP, false);
        }
    }

    public void sendUpdate(EntityPlayerMP playerMP, boolean flight) {
        if (playerMP.capabilities.isCreativeMode) return;
        handleParticle(playerMP, flight);
        if (flight) {
            playerMP.capabilities.allowFlying = true;
        } else {
            playerMP.capabilities.allowFlying = false;
            playerMP.capabilities.isFlying = false;
        }
        playerMP.sendPlayerAbilities();
    }

    public void handleParticle(EntityPlayerMP playerMP, boolean flight) {
        if (flight) {
            playerMP.addPotionEffect(new PotionEffect(ModPotions.TIMED_FLIGHT.get(), flightDuration));
        } else playerMP.removePotionEffect(ModPotions.TIMED_FLIGHT.get());
    }

    /*
     * public void handleParticle(EntityPlayerMP playerMP, boolean flight) {
     * final String id = "TimedFlight";
     * 
     * if (flight) {
     * EndReforked.NET.sendTo(PlayerParticlePacket.newParticle(id, flightDuration, EnumParticleTypes.SPELL_MOB),
     * playerMP);
     * } else {
     * EndReforked.NET.sendTo(PlayerParticlePacket.remove(id), playerMP);
     * }
     * }
     */
}

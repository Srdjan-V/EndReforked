package io.github.srdjanv.endreforked.client;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import it.unimi.dsi.fastutil.objects.*;

public class ParticleHandler {

    private final static Lock lock = new ReentrantLock();
    private final static Object2ObjectOpenHashMap<String, RenderInfo> PARTICLES = new Object2ObjectOpenHashMap<>();

    public static void addToRender(RenderInfo info) {
        lock.lock();
        try {
            PARTICLES.put(info.id, info);
        } finally {
            lock.unlock();
        }
    }

    public static void removeFromRender(String id) {
        lock.lock();
        try {
            PARTICLES.remove(id);
        } finally {
            lock.unlock();
        }
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        lock.lock();
        try {
            var ITERATOR = PARTICLES.values().iterator();
            while (ITERATOR.hasNext()) {
                var renderInfo = ITERATOR.next();
                renderInfo.render();
                if (--renderInfo.remakingTicks < 0) {
                    ITERATOR.remove();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        lock.lock();
        try {
            PARTICLES.clear();
        } finally {
            lock.unlock();
        }
    }

    public static abstract class RenderInfo {

        private final String id;
        protected int remakingTicks;

        protected RenderInfo(String id, int remakingTicks) {
            this.id = id;
            this.remakingTicks = remakingTicks;
        }

        public abstract void render();

        public String getId() {
            return id;
        }

        public int getRemakingTicks() {
            return remakingTicks;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            RenderInfo renderInfo = (RenderInfo) object;
            return id.equals(renderInfo.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    /*
     * public static class StaticInfo extends Info {
     * private final EnumParticleTypes particleType;
     * private final double xCoord;
     * private final double yCoord;
     * private final double zCoord;
     * private final double xSpeed;
     * private final double ySpeed;
     * private final double zSpeed;
     * private final int[] parameters;
     * 
     * 
     * public StaticInfo(int remakingTicks,
     * EnumParticleTypes particleType,
     * double xCoord, double yCoord, double zCoord,
     * double xSpeed, double ySpeed, double zSpeed,
     * int[] parameters) {
     * 
     * this.particleType = particleType;
     * this.xCoord = xCoord;
     * this.yCoord = yCoord;
     * this.zCoord = zCoord;
     * this.xSpeed = xSpeed;
     * this.ySpeed = ySpeed;
     * this.zSpeed = zSpeed;
     * this.parameters = parameters;
     * }
     * 
     * public StaticInfo() {super();}
     * 
     * 
     * public void render() {
     * Minecraft.getMinecraft().world.spawnParticle(particleType, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed,
     * parameters);
     * }
     * 
     * @Override public boolean equals(Object object) {
     * if (this == object) return true;
     * if (object == null || getClass() != object.getClass()) return false;
     * Info info = (Info) object;
     * return particleType == info.particleType;
     * }
     * 
     * @Override public int hashCode() {
     * return Objects.hashCode(particleType);
     * }
     * }
     */
}

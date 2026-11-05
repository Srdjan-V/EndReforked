package io.github.srdjanv.endreforked.common.network.servertoclient;

import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.EndReforked;

public class PlayerParticlePacket extends ParticlePacket {

    public PlayerParticlePacket() {}

    public PlayerParticlePacket(boolean remove, String id, int renderIterations,
                                @Nullable EnumParticleTypes particleType) {
        super(remove, id, renderIterations, particleType);
    }

    public static PlayerParticlePacket newParticle(String id, int renderIterations, EnumParticleTypes particleType) {
        return new PlayerParticlePacket(id, renderIterations, particleType);
    }

    public PlayerParticlePacket(String id, int renderIterations, EnumParticleTypes particleType) {
        this(false, id, renderIterations, particleType);
    }

    public static PlayerParticlePacket remove(String id) {
        return new PlayerParticlePacket(id);
    }

    public PlayerParticlePacket(String id) {
        this(true, id, 0, null);
    }

    public enum Handler implements IMessageHandler<PlayerParticlePacket, IMessage> {

        INSTANCE;

        @Override
        public IMessage onMessage(PlayerParticlePacket message, MessageContext ctx) {
            if (ctx.side.isServer()) return null;

            EndReforked.getProxy().handlePlayerParticle(message);
            return null;
        }
    }
}

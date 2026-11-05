package io.github.srdjanv.endreforked.common.network.servertoclient;

import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;

public class ParticlePacket implements IMessage {

    private boolean remove;
    private String id;
    private int renderIterations;
    private EnumParticleTypes particleType;

    public ParticlePacket() {}

    public ParticlePacket(boolean remove, String id, int renderIterations, @Nullable EnumParticleTypes particleType) {
        this.remove = remove;
        this.id = id;
        this.renderIterations = renderIterations;
        this.particleType = particleType;
    }

    public ParticlePacket(String id, int renderIterations, EnumParticleTypes particleType) {
        this(false, id, renderIterations, particleType);
    }

    public ParticlePacket(String id) {
        this(true, id, 0, null);
    }

    public boolean remove() {
        return remove;
    }

    public String id() {
        return id;
    }

    public int renderIterations() {
        return renderIterations;
    }

    @Nullable
    public EnumParticleTypes particleType() {
        return particleType;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        remove = buf.readBoolean();
        id = ByteBufUtils.readUTF8String(buf);
        if (remove) return;
        renderIterations = buf.readInt();
        particleType = EnumParticleTypes.getParticleFromId(buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(remove);
        ByteBufUtils.writeUTF8String(buf, id);
        if (remove) return;
        buf.writeInt(renderIterations);
        buf.writeInt(particleType.getParticleID());
    }
}

package io.github.srdjanv.endreforked.common.network.servertoclient;

import net.minecraft.util.EnumParticleTypes;

import io.netty.buffer.ByteBuf;

public class FixedParticlePacket extends ParticlePacket {

    private double xCoord;
    private double yCoord;
    private double zCoord;
    private double xSpeed;
    private double ySpeed;
    private double zSpeed;
    private int[] parameters;

    public FixedParticlePacket() {}

    public FixedParticlePacket(boolean remove, String id, int renderIterations, EnumParticleTypes particleType,
                               double xCoord, double yCoord, double zCoord,
                               double xSpeed, double ySpeed, double zSpeed,
                               int... parameters) {
        super(remove, id, renderIterations, particleType);
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.zCoord = zCoord;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.zSpeed = zSpeed;
        this.parameters = parameters;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        xCoord = buf.readDouble();
        yCoord = buf.readDouble();
        zCoord = buf.readDouble();
        xSpeed = buf.readDouble();
        ySpeed = buf.readDouble();
        zSpeed = buf.readDouble();
        parameters = new int[buf.readInt()];
        for (int i = 0; i < parameters.length; i++) parameters[i] = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeDouble(xCoord);
        buf.writeDouble(yCoord);
        buf.writeDouble(zCoord);
        buf.writeDouble(xSpeed);
        buf.writeDouble(ySpeed);
        buf.writeDouble(zSpeed);
        buf.writeInt(parameters.length);
        for (int parameter : parameters) buf.writeInt(parameter);
    }
}

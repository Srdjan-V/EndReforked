package io.github.srdjanv.endreforked.common.network.servertoclient;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import io.github.srdjanv.endreforked.EndReforked;
import io.netty.buffer.ByteBuf;

public class TpParticlePacket implements IMessage {

    private double posX;
    private double posY;
    private double posZ;

    private double newPosX;
    private double newPosY;
    private double newPosZ;

    private double width;
    private double height;

    public TpParticlePacket(boolean livingOldPos, EntityLivingBase livingBase, double posX, double posY, double posZ) {
        if (livingOldPos) {
            this.posX = livingBase.posX;
            this.posY = livingBase.posY;
            this.posZ = livingBase.posZ;

            this.newPosX = posX;
            this.newPosY = posY;
            this.newPosZ = posZ;
        } else {
            this.newPosX = livingBase.posX;
            this.newPosY = livingBase.posY;
            this.newPosZ = livingBase.posZ;

            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }

        this.width = livingBase.width;
        this.height = livingBase.height;
    }

    public TpParticlePacket(double posX, double posY, double posZ, double newPosX, double newPosY, double newPosZ,
                            double width, double height) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.newPosX = newPosX;
        this.newPosY = newPosY;
        this.newPosZ = newPosZ;
        this.width = width;
        this.height = height;
    }

    public TpParticlePacket() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();

        newPosX = buf.readDouble();
        newPosY = buf.readDouble();
        newPosZ = buf.readDouble();

        width = buf.readDouble();
        height = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);

        buf.writeDouble(newPosX);
        buf.writeDouble(newPosY);
        buf.writeDouble(newPosZ);

        buf.writeDouble(width);
        buf.writeDouble(height);
    }

    public enum Handler implements IMessageHandler<TpParticlePacket, IMessage> {

        INSTANCE;

        @Override
        public IMessage onMessage(TpParticlePacket message, MessageContext ctx) {
            if (ctx.side.isServer()) return null;
            EndReforked.getProxy().handleTpParticle(message.posX, message.posY, message.posZ, message.newPosX,
                    message.newPosY, message.newPosZ, message.width, message.height);
            return null;
        }
    }
}

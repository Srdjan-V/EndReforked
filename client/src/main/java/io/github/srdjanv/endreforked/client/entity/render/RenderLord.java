package io.github.srdjanv.endreforked.client.entity.render;

import java.time.LocalDate;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.entity.EntityLord;

public class RenderLord extends RenderLiving<EntityLord> {

    public static final ResourceLocation TEXTURES = new ResourceLocation(
            Tags.MODID + ":textures/entity/endlord.png");
    public static final ResourceLocation TEXTURES_NEW = new ResourceLocation(
            Tags.MODID + ":textures/entity/endlord_new.png");

    public static final ResourceLocation USED_TEXTURE;

    static {
        if (isNewYear()) {
            USED_TEXTURE = TEXTURES_NEW;
        } else USED_TEXTURE = TEXTURES;
    }

    public static boolean isNewYear() {
        int day = LocalDate.now().getDayOfYear();
        return day == 366 || day == 365 || day == 364 || day == 1;
    }

    public static final Factory FACTORY = new Factory();

    public RenderLord(RenderManager manager) {
        super(manager, new ModelLord(), 0.4F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLord entity) {
        return USED_TEXTURE;
    }

    public static class Factory implements IRenderFactory<EntityLord> {

        @Override
        public Render<? super EntityLord> createRenderFor(RenderManager manager) {
            return new RenderLord(manager);
        }
    }
}

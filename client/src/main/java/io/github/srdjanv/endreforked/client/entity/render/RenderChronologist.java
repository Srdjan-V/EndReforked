package io.github.srdjanv.endreforked.client.entity.render;

import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.client.entity.layer.LayerChronologist;
import io.github.srdjanv.endreforked.common.entity.EntityChronologist;

public class RenderChronologist extends RenderLiving<EntityChronologist> {

    public static final ResourceLocation TEXTURES = new ResourceLocation(
            Tags.MODID, "textures/entity/chronologist.png");

    public static final RenderChronologist.Factory FACTORY = new RenderChronologist.Factory();

    public RenderChronologist(RenderManager manager) {
        super(manager, new ModelEnderman(0), 0.5F);
        this.addLayer(new LayerChronologist(this));
    }

    @Override
    public void doRender(EntityChronologist entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.isScreaming()) {
            x += entity.getRNG().nextGaussian() * 0.02D;
            z += entity.getRNG().nextGaussian() * 0.02D;
        }
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityChronologist entity) {
        return TEXTURES;
    }

    public static class Factory implements IRenderFactory<EntityChronologist> {

        @Override
        public Render<? super EntityChronologist> createRenderFor(RenderManager manager) {
            return new RenderChronologist(manager);
        }
    }
}

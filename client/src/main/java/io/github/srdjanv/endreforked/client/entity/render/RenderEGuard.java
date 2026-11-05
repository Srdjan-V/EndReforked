package io.github.srdjanv.endreforked.client.entity.render;

import net.minecraft.client.model.ModelBlaze;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.entity.EntityEndGuard;

public class RenderEGuard extends RenderLiving<EntityEndGuard> {

    public static final ResourceLocation TEXTURES = new ResourceLocation(Tags.MODID, "textures/entity/endguard.png");

    public static final Factory FACTORY = new Factory();

    public RenderEGuard(RenderManager manager) {
        super(manager, new ModelBlaze(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEndGuard entity) {
        return TEXTURES;
    }

    public static class Factory implements IRenderFactory<EntityEndGuard> {

        @Override
        public Render<? super EntityEndGuard> createRenderFor(RenderManager manager) {
            return new RenderEGuard(manager);
        }
    }
}

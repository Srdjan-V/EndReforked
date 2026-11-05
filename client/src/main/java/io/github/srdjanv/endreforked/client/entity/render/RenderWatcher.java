package io.github.srdjanv.endreforked.client.entity.render;

import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.client.entity.layer.LayerWatcherEyes;
import io.github.srdjanv.endreforked.common.entity.EntityWatcher;

public class RenderWatcher extends RenderLiving<EntityWatcher> {

    public static final ResourceLocation TEXTURES = new ResourceLocation(
            Tags.MODID, "textures/entity/watcher.png");

    public static final Factory FACTORY = new Factory();

    public RenderWatcher(RenderManager manager) {
        super(manager, new ModelEnderman(0), 0.5F);
        this.addLayer(new LayerWatcherEyes(this));
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWatcher entity) {
        return TEXTURES;
    }

    @Override
    public ModelEnderman getMainModel() {
        return (ModelEnderman) super.getMainModel();
    }

    @Override
    public void doRender(EntityWatcher entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ModelEnderman modelenderman = this.getMainModel();
        modelenderman.isAttacking = entity.isScreaming();

        if (entity.isScreaming()) {
            x += entity.getRNG().nextGaussian() * 0.02D;
            z += entity.getRNG().nextGaussian() * 0.02D;
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    public static class Factory implements IRenderFactory<EntityWatcher> {

        @Override
        public Render<? super EntityWatcher> createRenderFor(RenderManager manager) {
            return new RenderWatcher(manager);
        }
    }
}

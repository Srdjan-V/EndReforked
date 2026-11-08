package io.github.srdjanv.endreforked.client.entity.render

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.client.entity.layer.LayerWatcherEyes
import io.github.srdjanv.endreforked.common.entity.EntityWatcher
import net.minecraft.client.model.ModelEnderman
import net.minecraft.client.renderer.entity.RenderLiving
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation

class RenderWatcher(manager: RenderManager) : RenderLiving<EntityWatcher>(manager, ModelEnderman(0.0f), 0.5f) {
    init {
        addLayer(LayerWatcherEyes(this))
    }

    override fun getEntityTexture(entity: EntityWatcher) = TEXTURE

    override fun getMainModel() = super.getMainModel() as ModelEnderman

    override fun doRender(entity: EntityWatcher, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float) {
        getMainModel().isAttacking = entity.isScreaming
        val shaking = entity.isScreaming
        val dx = if (shaking) entity.rng.nextGaussian() * 0.02 else 0.0
        val dz = if (shaking) entity.rng.nextGaussian() * 0.02 else 0.0
        super.doRender(entity, x + dx, y, z + dz, entityYaw, partialTicks)
    }

    private companion object {
        val TEXTURE = ResourceLocation(Tags.MODID, "textures/entity/watcher.png")
    }
}

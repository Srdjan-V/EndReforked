package io.github.srdjanv.endreforked.client.entity.render

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.client.entity.layer.LayerChronologist
import io.github.srdjanv.endreforked.common.entity.EntityChronologist
import net.minecraft.client.model.ModelEnderman
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderLiving
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation

class RenderChronologist(manager: RenderManager) : RenderLiving<EntityChronologist>(manager, ModelEnderman(0.0f), 0.5f) {
    init {
        addLayer(LayerChronologist(this))
    }

    override fun doRender(entity: EntityChronologist, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float) {
        val shaking = entity.isScreaming
        val dx = if (shaking) entity.rng.nextGaussian() * 0.02 else 0.0
        val dz = if (shaking) entity.rng.nextGaussian() * 0.02 else 0.0
        GlStateManager.pushMatrix()
        GlStateManager.disableLighting()
        super.doRender(entity, x + dx, y, z + dz, entityYaw, partialTicks)
        GlStateManager.popMatrix()
    }

    override fun getEntityTexture(entity: EntityChronologist) = TEXTURE

    private companion object {
        val TEXTURE = ResourceLocation(Tags.MODID, "textures/entity/chronologist.png")
    }
}

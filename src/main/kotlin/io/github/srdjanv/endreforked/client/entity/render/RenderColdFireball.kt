package io.github.srdjanv.endreforked.client.entity.render

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.entity.EntityEntropyFireball
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation

class RenderColdFireball(renderManager: RenderManager) : Render<EntityEntropyFireball>(renderManager) {
    override fun doRender(entity: EntityEntropyFireball, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float) {
        GlStateManager.pushMatrix()
        bindEntityTexture(entity)
        GlStateManager.translate(x.toFloat(), y.toFloat(), z.toFloat())
        GlStateManager.enableRescaleNormal()
        GlStateManager.scale(0.8f, 0.8f, 0.8f)
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer
        GlStateManager.rotate(180.0f - renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate((if (renderManager.options.thirdPersonView == 2) -1 else 1) * -renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        if (renderOutlines) {
            GlStateManager.enableColorMaterial()
            GlStateManager.enableOutlineMode(getTeamColor(entity))
        }
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL)
        buffer.pos(-0.5, -0.25, 0.0).tex(0.0, 1.0).normal(0.0f, 1.0f, 0.0f).endVertex()
        buffer.pos(0.5, -0.25, 0.0).tex(1.0, 1.0).normal(0.0f, 1.0f, 0.0f).endVertex()
        buffer.pos(0.5, 0.75, 0.0).tex(1.0, 0.0).normal(0.0f, 1.0f, 0.0f).endVertex()
        buffer.pos(-0.5, 0.75, 0.0).tex(0.0, 0.0).normal(0.0f, 1.0f, 0.0f).endVertex()
        tessellator.draw()
        if (renderOutlines) {
            GlStateManager.disableOutlineMode()
            GlStateManager.disableColorMaterial()
        }
        GlStateManager.disableRescaleNormal()
        GlStateManager.popMatrix()
        super.doRender(entity, x, y, z, entityYaw, partialTicks)
    }

    override fun getEntityTexture(entity: EntityEntropyFireball) = TEXTURE

    private companion object {
        val TEXTURE = ResourceLocation(Tags.MODID, "textures/entity/cold_fireball.png")
    }
}

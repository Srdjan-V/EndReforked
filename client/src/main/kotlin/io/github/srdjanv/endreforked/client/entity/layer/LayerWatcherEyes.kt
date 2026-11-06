package io.github.srdjanv.endreforked.client.entity.layer

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.client.entity.render.RenderWatcher
import io.github.srdjanv.endreforked.common.entity.EntityWatcher
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class LayerWatcherEyes(private val renderer: RenderWatcher) : LayerRenderer<EntityWatcher> {
    override fun doRenderLayer(
        entity: EntityWatcher, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float,
        ageInTicks: Float, netHeadYaw: Float, headPitch: Float, scale: Float,
    ) {
        val entityRenderer = Minecraft.getMinecraft().entityRenderer
        renderer.bindTexture(TEXTURE)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE)
        GlStateManager.disableLighting()
        GlStateManager.depthMask(!entity.isInvisible)
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0f, 0.0f)
        GlStateManager.enableLighting()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        entityRenderer.setupFogColor(true)
        renderer.mainModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale)
        entityRenderer.setupFogColor(false)
        GlStateManager.depthMask(true)
        GlStateManager.disableBlend()
    }

    override fun shouldCombineTextures() = true

    private companion object {
        val TEXTURE = ResourceLocation(Tags.MODID, "textures/entity/watcher_layer.png")
    }
}

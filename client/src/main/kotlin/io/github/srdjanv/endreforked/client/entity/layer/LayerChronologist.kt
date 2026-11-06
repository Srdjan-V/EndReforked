package io.github.srdjanv.endreforked.client.entity.layer

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.entity.EntityChronologist
import net.minecraft.client.model.ModelEnderman
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderLivingBase
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class LayerChronologist(private val renderer: RenderLivingBase<*>) : LayerRenderer<EntityChronologist> {
    private val layerModel = ModelEnderman(0.25f)

    override fun doRenderLayer(
        entity: EntityChronologist, limbSwing: Float, limbSwingAmount: Float, partialTicks: Float,
        ageInTicks: Float, netHeadYaw: Float, headPitch: Float, scale: Float,
    ) {
        layerModel.setModelAttributes(renderer.mainModel)
        layerModel.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        renderer.bindTexture(TEXTURE)
        layerModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale)
    }

    override fun shouldCombineTextures() = true

    private companion object {
        val TEXTURE = ResourceLocation(Tags.MODID, "textures/entity/chronologist_layer.png")
    }
}

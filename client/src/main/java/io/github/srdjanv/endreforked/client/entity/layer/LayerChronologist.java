package io.github.srdjanv.endreforked.client.entity.layer;

import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.entity.EntityChronologist;

@SideOnly(Side.CLIENT)
public class LayerChronologist implements LayerRenderer<EntityChronologist> {

    private static final ResourceLocation LAYER_TEXTURES = new ResourceLocation(
            Tags.MODID, "textures/entity/chronologist_layer.png");
    private final RenderLivingBase<?> renderer;
    private final ModelEnderman layerModel = new ModelEnderman(0.25F);

    public LayerChronologist(RenderLivingBase<?> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityChronologist entitylivingbaseIn, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        layerModel.setModelAttributes(renderer.getMainModel());
        layerModel.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        renderer.bindTexture(LAYER_TEXTURES);
        layerModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return true;
    }
}

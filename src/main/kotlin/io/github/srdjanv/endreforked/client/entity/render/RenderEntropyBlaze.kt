package io.github.srdjanv.endreforked.client.entity.render

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.entity.EntityEntropyBlaze
import net.minecraft.client.model.ModelBlaze
import net.minecraft.client.renderer.entity.RenderLiving
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation

class RenderEntropyBlaze(manager: RenderManager) : RenderLiving<EntityEntropyBlaze>(manager, ModelBlaze(), 0.5f) {
    override fun getEntityTexture(entity: EntityEntropyBlaze) = TEXTURE

    private companion object {
        val TEXTURE = ResourceLocation(Tags.MODID, "textures/entity/entropy_blaze.png")
    }
}

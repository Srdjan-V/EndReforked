package io.github.srdjanv.endreforked.client.entity.render

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.entity.EntityEndGuard
import net.minecraft.client.model.ModelBlaze
import net.minecraft.client.renderer.entity.RenderLiving
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation

class RenderEGuard(manager: RenderManager) : RenderLiving<EntityEndGuard>(manager, ModelBlaze(), 0.5f) {
    override fun getEntityTexture(entity: EntityEndGuard) = TEXTURE

    private companion object {
        val TEXTURE = ResourceLocation(Tags.MODID, "textures/entity/endguard.png")
    }
}

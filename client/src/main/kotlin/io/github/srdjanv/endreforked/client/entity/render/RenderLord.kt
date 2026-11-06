package io.github.srdjanv.endreforked.client.entity.render

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.entity.EntityLord
import net.minecraft.client.renderer.entity.RenderLiving
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.util.ResourceLocation
import java.time.LocalDate

class RenderLord(manager: RenderManager) : RenderLiving<EntityLord>(manager, ModelLord(), 0.4f) {
    override fun getEntityTexture(entity: EntityLord) = TEXTURE

    private companion object {
        val TEXTURE = ResourceLocation(
            Tags.MODID + if (LocalDate.now().dayOfYear in intArrayOf(364, 365, 366, 1)) ":textures/entity/endlord_new.png" else ":textures/entity/endlord.png",
        )
    }
}

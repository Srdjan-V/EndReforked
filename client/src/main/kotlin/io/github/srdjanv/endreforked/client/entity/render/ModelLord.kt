package io.github.srdjanv.endreforked.client.entity.render

import io.github.srdjanv.endreforked.common.entity.EntityLord
import net.minecraft.client.model.ModelBiped
import net.minecraft.entity.Entity
import net.minecraft.util.math.MathHelper
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.PI

@SideOnly(Side.CLIENT)
class ModelLord : ModelBiped(0.0f, 0.0f, 64, 64) {
    override fun setRotationAngles(
        limbSwing: Float, limbSwingAmount: Float, ageInTicks: Float, netHeadYaw: Float, headPitch: Float, scaleFactor: Float,
        entityIn: Entity,
    ) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn)
        val armsRaised = entityIn is EntityLord && entityIn.isArmsRaised
        val f = MathHelper.sin(swingProgress * PI.toFloat())
        val f1 = MathHelper.sin((1.0f - (1.0f - swingProgress) * (1.0f - swingProgress)) * PI.toFloat())
        val sway = MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f
        val bob = MathHelper.sin(ageInTicks * 0.067f) * 0.05f

        bipedRightArm.rotateAngleY = -(0.1f - f * 0.6f)
        bipedLeftArm.rotateAngleY = 0.1f - f * 0.6f
        bipedRightArm.rotateAngleX = -PI.toFloat() / (if (armsRaised) 2.2f else 0.1f) + (f * 1.2f - f1 * 0.4f) + bob
        bipedLeftArm.rotateAngleX += f * 1.2f - f1 * 0.4f
        bipedLeftArm.rotateAngleX -= bob
        bipedRightArm.rotateAngleZ = sway
        bipedLeftArm.rotateAngleZ = -sway
    }
}

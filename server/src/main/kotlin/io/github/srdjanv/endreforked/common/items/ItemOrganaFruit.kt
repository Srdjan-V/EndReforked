package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler
import io.github.srdjanv.endreforked.common.items.base.ItemFoodBase
import io.github.srdjanv.endreforked.wire.s2c.S2CPacket
import io.github.srdjanv.endreforked.wire.s2c.TpParticle
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import okio.ByteString

class ItemOrganaFruit : ItemFoodBase("organa_fruit", 4, 0.3f, false) {
    init {
        setAlwaysEdible()
    }

    override fun onItemUseFinish(stack: ItemStack, worldIn: World, entityLiving: EntityLivingBase): ItemStack {
        val result = super.onItemUseFinish(stack, worldIn, entityLiving)
        if (worldIn.isRemote) return result
        val x = entityLiving.posX
        val y = entityLiving.posY
        val z = entityLiving.posZ
        val rng = entityLiving.rng
        for (i in 0 until 16) {
            val randX = entityLiving.posX + (rng.nextDouble() - 0.5) * 16.0
            val randY = MathHelper.clamp(entityLiving.posY + (rng.nextInt(16) - 8).toDouble(), 0.0,
                (worldIn.actualHeight - 1).toDouble())
            val randZ = entityLiving.posZ + (rng.nextDouble() - 0.5) * 16.0
            if (entityLiving.isRiding) entityLiving.dismountRidingEntity()
            val didTP = if (entityLiving is EntityPlayerMP) attemptTeleport(entityLiving, randX, randY, randZ)
            else entityLiving.attemptTeleport(randX, randY, randZ)
            if (didTP) {
                worldIn.playSound(null, x, y, z, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1f, 1f)
                entityLiving.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1f, 1f)
                break
            }
        }
        if (entityLiving is EntityPlayerMP) {
            val flight = 25 * 20
            entityLiving.cooldownTracker.setCooldown(this, flight - 5 * 20)
            entityLiving.getCapability(CapabilityTimedFlightHandler.INSTANCE, null)!!.flightDuration = flight
            entityLiving.capabilities.isFlying = true
            entityLiving.sendPlayerAbilities()
        }
        return result
    }

    private fun attemptTeleport(player: EntityPlayerMP, targetX: Double, targetY: Double, targetZ: Double): Boolean {
        val orgX = player.posX
        val orgY = player.posY
        val orgZ = player.posZ
        var validSpace = false
        val tpPos = BlockPos.MutableBlockPos()
        tpPos.setPos(targetX, targetY, targetZ)
        val world = player.world
        if (world.isBlockLoaded(tpPos)) {
            var validPos = false
            while (!validPos && tpPos.y > 0) {
                tpPos.move(EnumFacing.DOWN)
                if (world.canSeeSky(tpPos)) validPos = true
            }
            if (!validPos) tpPos.setPos(targetX, targetY, targetZ)
            while (!validPos && tpPos.y > 0) {
                tpPos.move(EnumFacing.DOWN)
                if (world.getBlockState(tpPos).material.blocksMovement()) validPos = true
            }
            if (validPos) {
                player.setPositionAndUpdate(tpPos.x.toDouble(), tpPos.y.toDouble(), tpPos.z.toDouble())
                if (world.getCollisionBoxes(player, player.entityBoundingBox).isEmpty() &&
                    !world.containsAnyLiquid(player.entityBoundingBox)
                ) validSpace = true
            }
        }
        if (!validSpace) {
            player.setPositionAndUpdate(orgX, orgY, orgZ)
            return false
        }
        EndReforked.NET.sendToAll(S2CPacket(null, TpParticle(orgX, orgY, orgZ, player.posX, player.posY, player.posZ,
            player.width.toDouble(), player.height.toDouble(), ByteString.EMPTY), null, ByteString.EMPTY))
        return true
    }
}

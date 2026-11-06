package io.github.srdjanv.endreforked.common.handlers

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.configs.Configs
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.living.LivingHurtEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EventHandler {
    @SubscribeEvent
    @JvmStatic
    fun onDragonTick(event: LivingEvent.LivingUpdateEvent) {
        val dragon = event.entityLiving as? EntityDragon ?: return
        val world = dragon.world
        if (world.isRemote || dragon.deathTicks < 150 || dragon.deathTicks % 10 != 0) return

        repeat(6) {
            val x = world.rand.nextInt(256) - 128
            val z = world.rand.nextInt(256) - 128
            val pos = BlockPos(x, world.getHeight(x, z) - 1, z)
            if (world.isBlockLoaded(pos) && world.getBlockState(pos.down()).block == Blocks.END_STONE) {
                world.setBlockState(pos, ModBlocks.ENTROPY_CROP_BLOCK.get().defaultState)
            }
        }
    }

    @SubscribeEvent
    @JvmStatic
    fun onPlayerPosition(event: LivingHurtEvent) {
        val player = event.entityLiving as? EntityPlayerMP ?: return
        if (player.dimension == 1 && Configs.SERVER_SIDE_CONFIGS.teleporterEnd && player.position.y <= -6) {
            event.isCanceled = true
            player.entityWorld.minecraftServer!!.playerList.changePlayerDimension(player, 0)
        }
    }
}

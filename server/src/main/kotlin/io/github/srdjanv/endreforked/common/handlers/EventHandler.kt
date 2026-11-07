package io.github.srdjanv.endreforked.common.handlers

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.configs.Configs
import io.github.srdjanv.endreforked.common.entity.EntityEndGuard
import io.github.srdjanv.endreforked.common.entity.EntityWatcher
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import net.minecraft.world.biome.Biome
import net.minecraftforge.event.world.WorldEvent
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.living.LivingHurtEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EventHandler {
    // ponytail: the list of a spawn query is the biome's own list, so the weight is set in place just before the game reads it.
    private val baseWeights = java.util.IdentityHashMap<Biome.SpawnListEntry, Int>()

    /** The weight of the spawn entries of Watcher and Endguard is x1, x2 or x3 by the level of the spawn chunk. */
    @SubscribeEvent
    @JvmStatic
    fun onPotentialSpawns(event: WorldEvent.PotentialSpawns) {
        val level = EntropyWorldHandler.levelAt(event.world.provider.dimension, event.pos)
        for (entry in event.list) {
            if (entry.entityClass != EntityWatcher::class.java && entry.entityClass != EntityEndGuard::class.java) continue
            entry.itemWeight = baseWeights.getOrPut(entry) { entry.itemWeight } * EntropyTuning.spawnFactor(level)
        }
    }

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
                world.setBlockState(pos, ModBlocks.ENTROPY_CROP_BLOCK.defaultState)
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

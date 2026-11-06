package io.github.srdjanv.endreforked.common.world

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.api.world.WorldEffect
import io.github.srdjanv.endreforked.common.configs.Configs
import io.github.srdjanv.endreforked.common.registry.GameAdapter
import io.github.srdjanv.endreforked.common.registry.GameAdapter.id
import io.github.srdjanv.endreforked.common.registry.GameAdapter.matches
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer

/** Runs the wand conversion registry for the Entropy Wand. */
object WandInteractions {
    /** Converts the block at [pos] with the first conversion that fits. Returns false when none does. */
    @JvmStatic
    fun use(player: EntityPlayer, world: WorldServer, pos: BlockPos, hand: EnumHand): Boolean {
        val state = world.getBlockState(pos)
        val conversions = EndReforked.getProxy().registries(world).wandConversions.current
        for (conversion in conversions.forBlock(state.block.id())) {
            if (!state.matches(conversion.match)) continue
            val result = GameAdapter.state(conversion.result) ?: continue
            world.setBlockState(pos, result)
            player.getHeldItem(hand).damageItem(conversion.itemDamage ?: Configs.SERVER_SIDE_CONFIGS.entropyWandUseDamage, player)
            for (effect in conversion.effects) {
                when (effect) {
                    WorldEffect.FLINT_SOUND -> world.playSound(
                        null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1f, world.rand.nextFloat() * 0.4f + 0.8f,
                    )
                }
            }
            return true
        }
        return false
    }
}

package io.github.srdjanv.endreforked.compat.conarm

import c4.conarm.common.armor.utils.ArmorHelper
import c4.conarm.lib.traits.AbstractArmorTrait
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import net.minecraft.entity.IProjectile
import net.minecraft.entity.EntityAreaEffectCloud
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import net.minecraftforge.event.entity.living.LivingAttackEvent
import net.minecraftforge.event.entity.living.PotionEvent
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import slimeknights.tconstruct.library.utils.TagUtil
import slimeknights.tconstruct.library.utils.TinkerUtil

/**
 * The armor traits of the mod. Armory ids get the suffix `_armor` (lang keys `modifier.endreborn_<name>_armor.name`).
 * Each trait works from forge events and checks the armor of the player with [wearing] (unbroken pieces only).
 */
object ArmorTraits {
    fun pieces(player: EntityPlayer, trait: AbstractArmorTrait): List<ItemStack> = player.armorInventoryList.filter {
        !it.isEmpty && ArmorHelper.isUnbrokenTinkersArmor(it) && TinkerUtil.hasTrait(TagUtil.getTagSafe(it), trait.identifier)
    }

    fun wearing(player: EntityPlayer, trait: AbstractArmorTrait) = pieces(player, trait).isNotEmpty()

    private fun now(p: EntityPlayer) = p.world.totalWorldTime

    /**
     * Blink: a projectile that would hit the wearer is cancelled (LivingAttackEvent, so arrows, snowballs and fireballs are covered)
     * and the wearer teleports like an enderman: random spot within 8 blocks, safe landing by [EntityPlayer.attemptTeleport].
     * The cooldown ([EntropyTuning.blinkCooldown]) is in the entity data. During the cooldown the projectile hits normally.
     * Overlap: Armory's Enderport is a 25 % chance per hurt (any damage, up to 64 blocks); Blink is sure, only for projectiles, short, with a cooldown.
     */
    object Blink : AbstractArmorTrait("endreborn_blink", TextFormatting.DARK_AQUA) {
        private const val UNTIL = "endreborn_blink_until"

        /** True while Blink teleports the wearer: [Anchored] must not cancel that. */
        @JvmField internal var blinking = false

        init {
            MinecraftForge.EVENT_BUS.register(this)
        }

        @SubscribeEvent
        fun onAttack(event: LivingAttackEvent) {
            val p = event.entityLiving as? EntityPlayer ?: return
            if (p.world.isRemote || p.isDead || !isProjectile(event.source) || !wearing(p, this)) return
            if (p.entityData.getLong(UNTIL) > now(p)) return
            if (!blink(p)) return
            p.entityData.setLong(UNTIL, now(p) + EntropyTuning.blinkCooldown)
            event.isCanceled = true
        }

        private fun isProjectile(s: DamageSource) = s.isProjectile || s.immediateSource is IProjectile

        private fun blink(p: EntityPlayer): Boolean {
            blinking = true
            try {
                repeat(16) {
                    val x = p.posX + (p.rng().nextDouble() - 0.5) * 16
                    val y = p.posY + p.rng().nextInt(9) - 4
                    val z = p.posZ + (p.rng().nextDouble() - 0.5) * 16
                    if (p.attemptTeleport(x, y, z)) return true
                }
                return false
            } finally {
                blinking = false
            }
        }

        private fun EntityPlayer.rng() = world.rand
    }

    /**
     * Anchored: levitation is denied (PotionApplicableEvent), forced teleports are cancelled (EnderTeleportEvent of the wearer),
     * and the chunk of the wearer is safe from critical events (see [io.github.srdjanv.endreforked.common.entropy.CriticalEvents.protectors]).
     * Rule: a teleport is the own action of the player when he uses an item (chorus fruit, organa fruit: his hand is active) or his own ender pearl
     * is in flight (it impacts, then teleports him). Every other teleport of the wearer is forced. The Blink trait teleport is allowed too.
     */
    object Anchored : AbstractArmorTrait("endreborn_anchored", TextFormatting.GRAY) {
        init {
            MinecraftForge.EVENT_BUS.register(this)
        }

        @SubscribeEvent
        fun onPotion(event: PotionEvent.PotionApplicableEvent) {
            val p = event.entityLiving as? EntityPlayer ?: return
            if (event.potionEffect.potion == MobEffects.LEVITATION && !p.world.isRemote && wearing(p, this)) event.result = Event.Result.DENY
        }

        @SubscribeEvent
        fun onTeleport(event: EnderTeleportEvent) {
            val p = event.entityLiving as? EntityPlayer ?: return
            if (p.world.isRemote || Blink.blinking || !wearing(p, this)) return
            if (p.isHandActive || p.world.loadedEntityList.any { it is EntityEnderPearl && it.thrower === p }) return
            event.isCanceled = true
        }
    }

    /**
     * Absorb: every [EntropyTuning.absorbInterval] ticks the chunk of the wearer (radius one) is drained by [EntropyTuning.absorbEntropy]
     * and gives [EntropyTuning.absorbPoints] absorption points (2 = one heart). The cap is [EntropyTuning.absorbCapPerPiece] points per piece with the trait.
     * A smaller drain gives a proportional amount. Nothing is drained at the cap.
     */
    object Absorb : AbstractArmorTrait("endreborn_absorb", TextFormatting.LIGHT_PURPLE) {
        init {
            MinecraftForge.EVENT_BUS.register(this)
        }

        @SubscribeEvent
        fun onTick(event: TickEvent.PlayerTickEvent) {
            val p = event.player
            if (event.phase != TickEvent.Phase.END || p.world.isRemote || p.isDead || now(p) % EntropyTuning.absorbInterval != 0L) return
            val cap = EntropyTuning.absorbCapPerPiece * pieces(p, this).size
            if (cap <= 0f || p.absorptionAmount >= cap) return
            val cost = EntropyTuning.absorbEntropy
            val drained = EntropyChunkReader.ofEntity(p, EntropyRadius.ONE).entropyView.drainEntropy(cost, false)
            if (drained <= 0) return
            p.absorptionAmount = minOf(cap, p.absorptionAmount + EntropyTuning.absorbPoints * drained / cost)
        }
    }

    /**
     * Void rescue: below y = 0 (any dimension) the wearer teleports to his last safe block (last tick on solid ground, y >= 0, not in liquid,
     * same dimension; in the entity data). The first piece with the trait loses [EntropyTuning.voidRescueWearPercent] % of its durability.
     * Without a safe block of this dimension nothing happens. Dragon breath is denied: damage from an area effect cloud with the dragon breath
     * particle (the vanilla dragon cloud, our DragonBreathCloud) and the vanilla DRAGON_BREATH source are cancelled.
     */
    object VoidRescue : AbstractArmorTrait("endreborn_voidrescue", TextFormatting.DARK_PURPLE) {
        private const val SAFE = "endreborn_safe_pos" // [dimension, x, y, z]

        init {
            MinecraftForge.EVENT_BUS.register(this)
        }

        @SubscribeEvent
        fun onTick(event: TickEvent.PlayerTickEvent) {
            val p = event.player
            if (event.phase != TickEvent.Phase.END || p.world.isRemote || p.isDead) return
            if (p.onGround && p.posY >= 0 && !p.isInWater && !p.isInLava) {
                p.entityData.setIntArray(SAFE, intArrayOf(p.dimension, p.position.x, p.position.y, p.position.z))
            } else if (p.posY < 0) rescue(p)
        }

        private fun rescue(p: EntityPlayer) {
            val safe = p.entityData.getIntArray(SAFE)
            if (safe.size != 4 || safe[0] != p.dimension) return
            val index = p.armorInventoryList.indexOfFirst { it in pieces(p, this) }
            if (index < 0) return
            val piece = p.armorInventoryList.elementAt(index)
            p.fallDistance = 0f
            p.motionX = 0.0; p.motionY = 0.0; p.motionZ = 0.0
            p.setPositionAndUpdate(safe[1] + 0.5, safe[2].toDouble(), safe[3] + 0.5)
            ArmorHelper.damageArmor(piece, DamageSource.OUT_OF_WORLD, piece.maxDamage * EntropyTuning.voidRescueWearPercent / 100, p, index)
        }

        @SubscribeEvent
        fun onAttack(event: LivingAttackEvent) {
            val p = event.entityLiving as? EntityPlayer ?: return
            val s = event.source
            val breath = s === DamageSource.DRAGON_BREATH || (s.immediateSource as? EntityAreaEffectCloud)?.particle == EnumParticleTypes.DRAGON_BREATH
            if (breath && !p.world.isRemote && wearing(p, this)) event.isCanceled = true
        }
    }
}

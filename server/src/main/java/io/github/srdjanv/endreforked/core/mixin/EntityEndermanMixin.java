package io.github.srdjanv.endreforked.core.mixin;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.srdjanv.endreforked.common.ModPotions;

@Mixin(EntityEnderman.class)
public class EntityEndermanMixin {

    private EntityEndermanMixin() {
        throw new AssertionError();
    }

    @Inject(method = "shouldAttackPlayer", at = @At("HEAD"), cancellable = true)
    private void shouldAttackPlayer(EntityPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (player.isPotionActive(ModPotions.ENDER_EYES.get())) cir.setReturnValue(false);
    }
}

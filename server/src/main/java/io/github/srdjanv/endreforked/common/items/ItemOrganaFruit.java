package io.github.srdjanv.endreforked.common.items;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler;
import io.github.srdjanv.endreforked.common.items.base.ItemFoodBase;
import io.github.srdjanv.endreforked.common.network.servertoclient.TpParticlePacket;

public class ItemOrganaFruit extends ItemFoodBase {

    public ItemOrganaFruit() {
        super("organa_fruit", 4, 0.3F, false);
        setAlwaysEdible();
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        ItemStack itemstack = super.onItemUseFinish(stack, worldIn, entityLiving);

        if (worldIn.isRemote) return itemstack;

        double x = entityLiving.posX;
        double y = entityLiving.posY;
        double z = entityLiving.posZ;

        final boolean isPlayer = entityLiving instanceof EntityPlayer;
        boolean didTP;

        for (int i = 0; i < 16; ++i) {
            double randX = entityLiving.posX + (entityLiving.getRNG().nextDouble() - 0.5D) * 16.0D;
            double randY = MathHelper.clamp(entityLiving.posY + (double) (entityLiving.getRNG().nextInt(16) - 8), 0.0D,
                    worldIn.getActualHeight() - 1);
            double randZ = entityLiving.posZ + (entityLiving.getRNG().nextDouble() - 0.5D) * 16.0D;
            if (entityLiving.isRiding()) entityLiving.dismountRidingEntity();

            if (isPlayer) {
                didTP = attemptTeleport((EntityPlayerMP) entityLiving, randX, randY, randZ);
            } else didTP = entityLiving.attemptTeleport(randX, randY, randZ);

            if (didTP) {
                worldIn.playSound(null, x, y, z, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0F,
                        1.0F);
                entityLiving.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                break;
            }
        }

        if (entityLiving instanceof EntityPlayerMP player) {
            int flight = 25 * 20;
            player.getCooldownTracker().setCooldown(this, flight - (5 * 20));
            player.getCapability(CapabilityTimedFlightHandler.INSTANCE, null).setFlightDuration(flight);
            player.capabilities.isFlying = true;
            player.sendPlayerAbilities();
        }

        return itemstack;
    }

    public boolean attemptTeleport(EntityPlayerMP player, double targetX, double targetY, double targetZ) {
        double orgX = player.posX;
        double orgY = player.posY;
        double orgZ = player.posZ;

        boolean validSpace = false;
        BlockPos.MutableBlockPos tpPos = new BlockPos.MutableBlockPos();
        tpPos.setPos(targetX, targetY, targetZ);
        World world = player.world;

        if (world.isBlockLoaded(tpPos)) {
            boolean validPos = false;
            while (!validPos && tpPos.getY() > 0) {
                tpPos.move(EnumFacing.DOWN);
                if (world.canSeeSky(tpPos))
                    validPos = true;
            }
            if (!validPos) tpPos.setPos(targetX, targetY, targetZ);
            while (!validPos && tpPos.getY() > 0) {
                tpPos.move(EnumFacing.DOWN);
                IBlockState downState = world.getBlockState(tpPos);
                if (downState.getMaterial().blocksMovement())
                    validPos = true;
            }

            if (validPos) {
                player.setPositionAndUpdate(tpPos.getX(), tpPos.getY(), tpPos.getZ());
                if (world.getCollisionBoxes(player, player.getEntityBoundingBox()).isEmpty() &&
                        !world.containsAnyLiquid(player.getEntityBoundingBox())) {
                    validSpace = true;
                }
            }
        }

        if (!validSpace) {
            player.setPositionAndUpdate(orgX, orgY, orgZ);
            return false;
        } else {
            EndReforked.NET.sendToAll(new TpParticlePacket(false, player, orgX, orgY, orgZ));
            return true;
        }
    }
}

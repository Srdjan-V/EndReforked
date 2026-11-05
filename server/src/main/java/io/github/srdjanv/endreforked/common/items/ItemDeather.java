package io.github.srdjanv.endreforked.common.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.github.srdjanv.endreforked.common.items.tools.ToolSword;
import io.github.srdjanv.endreforked.utils.ItemStackUtils;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ItemDeather extends ToolSword implements InventoryItemModel {

    public ItemDeather(String name, ToolMaterial material) {
        super(name, material);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target,
                                            EnumHand hand) {
        if (player.world.isRemote) return false;
        player.world.playSound(null, player.posX, player.posY, player.posZ,
                SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.PLAYERS, 0.2F,
                player.world.rand.nextFloat() * 0.1F + 0.9F);
        Vec3d dir = player.getPositionVector().subtract(target.getPositionVector());
        if (getMode(stack) == 0) target.addVelocity(dir.x * 0.3, dir.y * 0.3, dir.z * 0.3);
        if (getMode(stack) == 1) target.addVelocity(dir.x * -0.3, dir.y * -0.3, dir.z * -0.3);
        if (!player.capabilities.isCreativeMode) stack.damageItem(1, player);
        return true;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, @Nonnull EnumHand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);

        if (entityplayer.isSneaking()) {
            if (!world.isRemote) {
                toggleMode(itemstack);
                world.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ,
                        SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.5F,
                        world.rand.nextFloat() * 0.1F + 0.9F);
            }

            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }

        return new ActionResult<>(EnumActionResult.PASS, itemstack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(I18n.format("tile.deather.tooltip"));
        tooltip.add(I18n.format("tile.deather_mode.tooltip") + ": " + getModeName(stack));
    }

    public int getMode(ItemStack itemStack) {
        return ItemStackUtils.getInt(itemStack, "mode");
    }

    public String getModeName(ItemStack itemStack) {
        return switch (getMode(itemStack)) {
            case 0 -> I18n.format("tile.deather_mode.off");
            case 1 -> I18n.format("tile.deather_mode.on");
            default -> null;
        };
    }

    public void toggleMode(ItemStack itemStack) {
        ItemStackUtils.setInt(itemStack, "mode", getMode(itemStack) < 1 ? getMode(itemStack) + 1 : 0);
    }
}

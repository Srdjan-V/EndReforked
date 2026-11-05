package io.github.srdjanv.endreforked.common.items;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler;
import io.github.srdjanv.endreforked.common.items.base.ItemFoodBase;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ItemDragoniteTea extends ItemFoodBase implements InventoryItemModel {

    public ItemDragoniteTea() {
        super("dragonite_tea", 2, 1, false);
        setAlwaysEdible();
        setMaxStackSize(16);
    }

    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        super.onItemUseFinish(stack, worldIn, entityLiving);

        if (entityLiving instanceof EntityPlayer player) {
            player.getCooldownTracker().setCooldown(this, 40 * 20);
            player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 50 * 20, 0));
            if (!worldIn.isRemote)
                player.getCapability(CapabilityTimedFlightHandler.INSTANCE, null).setFlightDuration(45 * 20);
        }
        return stack;
    }

    public int getMaxItemUseDuration(ItemStack stack) {
        return 10;
    }

    @SideOnly(Side.CLIENT)
    public ItemStack getDefaultInstance() {
        return PotionUtils.addPotionToItemStack(super.getDefaultInstance(), PotionTypes.HEALING);
    }

    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(I18n.format("tile.tea.tooltip"));
    }
}

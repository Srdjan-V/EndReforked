package io.github.srdjanv.endreforked.common.items;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.github.srdjanv.endreforked.common.ModPotions;
import io.github.srdjanv.endreforked.common.items.base.ItemFoodBase;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ItemEnderFlesh extends ItemFoodBase implements InventoryItemModel {

    public ItemEnderFlesh() {
        super("ender_flesh", 2, 0.4F, false);
        setAlwaysEdible();
        setPotionEffect(new PotionEffect(ModPotions.ENDER_EYES.get(), 90 * 20, 0), 1F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(I18n.format("tile.flesh.tooltip"));
    }
}

package io.github.srdjanv.endreforked.common.items.tools;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.api.entropy.wand.EntropyWandHandler;
import io.github.srdjanv.endreforked.api.entropy.wand.WorldConversion;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ToolEntropyWand extends ItemSword implements InventoryItemModel {

    public ToolEntropyWand(String name, ToolMaterial material) {
        super(material);
        setTranslationKey(name);
        setRegistryName(name);
        setMaxDamage(128);
        setCreativeTab(EndReforked.ENDERTAB);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("tile.wand.tooltip"));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        player.world.playSound(null, player.posX, player.posY, player.posZ,
                SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 0.5F,
                player.world.rand.nextFloat() * 0.1F + 0.9F);
        entity.attackEntityFrom(DamageSource.WITHER, 7);
        return true;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) return EnumActionResult.PASS;
        WorldServer server = (WorldServer) worldIn;
        var block = server.getBlockState(pos);
        var conversions = EntropyWandHandler.getConversions(block.getBlock());
        if (conversions == null) return EnumActionResult.PASS;

        for (WorldConversion worldConversion : conversions) {
            if (worldConversion.getBlockStateMatcher().test(block)) {
                server.setBlockState(pos, worldConversion.getNewState().get());
                player.getHeldItem(hand).damageItem(worldConversion.getItemDamage(), player);
                var callback = worldConversion.getConversionCallback();
                if (callback != null) callback.accept(server, pos);
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }
}

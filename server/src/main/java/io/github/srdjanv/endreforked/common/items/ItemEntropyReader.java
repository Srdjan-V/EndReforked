package io.github.srdjanv.endreforked.common.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider;
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius;
import io.github.srdjanv.endreforked.api.entropy.world.EntropyChunkReader;
import io.github.srdjanv.endreforked.common.items.base.ItemBase;

public class ItemEntropyReader extends ItemBase {

    public ItemEntropyReader() {
        super("entropy_reader");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote && player.isSneaking()) {
            var tile = worldIn.getTileEntity(pos);
            if (tile instanceof EntropyDataProvider provider) {
                provider.getFormatedEntropyData().ifPresent(strings -> {
                    for (String message : strings) {
                        player.sendMessage(new TextComponentString(message));
                    }
                });
            }
        }

        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (!worldIn.isRemote && !playerIn.isSneaking()) {
            var reader = EntropyChunkReader.ofEntity(playerIn, EntropyRadius.ONE);
            for (var entry : reader.getEntropyView().getView()) {
                playerIn.sendMessage(new TextComponentString(entry.getDimPos().toString()));
                entry.getFormatedEntropyData().ifPresent(strings -> {
                    for (String message : strings) {
                        playerIn.sendMessage(new TextComponentString(message));
                    }
                });
            }
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}

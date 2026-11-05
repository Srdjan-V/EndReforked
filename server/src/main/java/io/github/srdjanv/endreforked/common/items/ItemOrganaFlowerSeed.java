package io.github.srdjanv.endreforked.common.items;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

import org.jetbrains.annotations.NotNull;

import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlowerStem;
import io.github.srdjanv.endreforked.common.items.base.BaseSeeds;

public class ItemOrganaFlowerSeed extends BaseSeeds {

    public static final EnumPlantType ORGANA_FLOWER = EnumPlantType.getPlantType("organa_flower");
    protected final BlockOrganaFlowerStem crop;

    public ItemOrganaFlowerSeed() {
        super("organa_flower_seed",
                ModBlocks.ORGANA_FLOWER_STEM_BLOCK.get(),
                ModBlocks.ORGANA_WEED_BLOCK.get());
        crop = ModBlocks.ORGANA_FLOWER_STEM_BLOCK.get();
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = player.getHeldItem(hand);
        IBlockState state = worldIn.getBlockState(pos);
        if (facing == EnumFacing.UP && player.canPlayerEdit(pos.offset(facing), facing, itemstack) &&
                state.getBlock().canSustainPlant(state, worldIn, pos, EnumFacing.UP, crop) &&
                worldIn.isAirBlock(pos.up())) {
            worldIn.setBlockState(pos.up(), getPlant(worldIn, pos), 11);
            itemstack.shrink(1);
            return EnumActionResult.SUCCESS;
        } else return EnumActionResult.FAIL;
    }

    @Override
    public @NotNull EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return ORGANA_FLOWER;
    }
}

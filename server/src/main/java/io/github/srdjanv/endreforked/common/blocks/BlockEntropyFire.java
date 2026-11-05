package io.github.srdjanv.endreforked.common.blocks;

import net.minecraft.block.BlockFire;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel;

public class BlockEntropyFire extends BlockFire implements InventoryBlockModel {

    public BlockEntropyFire() {
        super();
        var name = "entropy_fire";
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReforked.ENDERTAB);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    /*
     * @Override public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
     * if (!worldIn.getGameRules().getBoolean("doFireTick")) return;
     * 
     * if (!worldIn.isAreaLoaded(pos, 2)) return; // Forge: prevent loading unloaded chunks when spreading fire
     * if (!this.canPlaceBlockAt(worldIn, pos)) worldIn.setBlockToAir(pos);
     * 
     * Block block = worldIn.getBlockState(pos.down()).getBlock();
     * boolean flag = block.isFireSource(worldIn, pos.down(), EnumFacing.UP);
     * 
     * int age = state.getValue(AGE);
     * 
     * if (!flag && worldIn.isRaining() && this.canDie(worldIn, pos) && rand.nextFloat() < 0.2F + (float) age * 0.03F) {
     * worldIn.setBlockToAir(pos);
     * } else {
     * if (age < 15) {
     * state = state.withProperty(AGE, Integer.valueOf(age + rand.nextInt(3) / 2));
     * worldIn.setBlockState(pos, state, 4);
     * }
     * 
     * worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn) + rand.nextInt(10));
     * 
     * if (!flag) {
     * if (!this.canNeighborCatchFire(worldIn, pos)) {
     * if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP) || age > 3) {
     * worldIn.setBlockToAir(pos);
     * }
     * 
     * return;
     * }
     * 
     * if (!this.canCatchFire(worldIn, pos.down(), EnumFacing.UP) && age == 15 && rand.nextInt(4) == 0) {
     * worldIn.setBlockToAir(pos);
     * return;
     * }
     * }
     * 
     * boolean flag1 = worldIn.isBlockinHighHumidity(pos);
     * int j = 0;
     * 
     * if (flag1) {
     * j = -50;
     * }
     * 
     * this.tryCatchFire(worldIn, pos.east(), 300 + j, rand, age, EnumFacing.WEST);
     * this.tryCatchFire(worldIn, pos.west(), 300 + j, rand, age, EnumFacing.EAST);
     * this.tryCatchFire(worldIn, pos.down(), 250 + j, rand, age, EnumFacing.UP);
     * this.tryCatchFire(worldIn, pos.up(), 250 + j, rand, age, EnumFacing.DOWN);
     * this.tryCatchFire(worldIn, pos.north(), 300 + j, rand, age, EnumFacing.SOUTH);
     * this.tryCatchFire(worldIn, pos.south(), 300 + j, rand, age, EnumFacing.NORTH);
     * 
     * for (int k = -1; k <= 1; ++k) {
     * for (int l = -1; l <= 1; ++l) {
     * for (int i1 = -1; i1 <= 4; ++i1) {
     * if (k != 0 || i1 != 0 || l != 0) {
     * int j1 = 100;
     * 
     * if (i1 > 1) {
     * j1 += (i1 - 1) * 100;
     * }
     * 
     * BlockPos blockpos = pos.add(k, i1, l);
     * int k1 = this.getNeighborEncouragement(worldIn, blockpos);
     * 
     * if (k1 > 0) {
     * int l1 = (k1 + 40 + worldIn.getDifficulty().getId() * 7) / (age + 30);
     * 
     * if (flag1) {
     * l1 /= 2;
     * }
     * 
     * if (l1 > 0 && rand.nextInt(j1) <= l1 && (!worldIn.isRaining() || !this.canDie(worldIn, blockpos))) {
     * int i2 = age + rand.nextInt(5) / 4;
     * 
     * if (i2 > 15) {
     * i2 = 15;
     * }
     * 
     * worldIn.setBlockState(blockpos, state.withProperty(AGE, Integer.valueOf(i2)), 3);
     * }
     * }
     * }
     * }
     * }
     * }
     * }
     * }
     */
}

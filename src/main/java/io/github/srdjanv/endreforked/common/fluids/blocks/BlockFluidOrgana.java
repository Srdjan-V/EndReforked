package io.github.srdjanv.endreforked.common.fluids.blocks;

import io.github.srdjanv.endreforked.common.ModFluids;
import io.github.srdjanv.endreforked.common.fluids.base.BaseBlockFluid;
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.FluidEntropyTile;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockFluidOrgana extends BaseBlockFluid {
    public BlockFluidOrgana() {
        super("organa", ModFluids.ORGANA.get(), Material.WATER, MapColor.PURPLE);

    }

    @Override public boolean hasTileEntity(IBlockState state) {
        return state.getValue(LEVEL) == 0;
    }

    @Nullable @Override public TileEntity createTileEntity(World world, IBlockState state) {
        return state.getValue(LEVEL) == 0 ? new FluidEntropyTile() : null;
    }
}

package io.github.srdjanv.endreforked.api.fluids;

import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;

import it.unimi.dsi.fastutil.Hash;

public class HashStrategies {

    private HashStrategies() {}

    public static final Hash.Strategy<Block> FLUID_ANY_STATE_HASH_STRATEGY = new Hash.Strategy<>() {

        @Override
        public int hashCode(Block o) {
            return o.hashCode();
        }

        @Override
        public boolean equals(Block a, Block b) {
            return a.equals(b);
        }
    };

    public static final Hash.Strategy<IBlockState> FLUID_SATE_HASH_STRATEGY = new Hash.Strategy<>() {

        @Override
        public int hashCode(IBlockState o) {
            return o.hashCode();
        }

        @Override
        public boolean equals(IBlockState a, IBlockState b) {
            return a.equals(b);
        }
    };

    public static final Hash.Strategy<Class<Entity>> ENTITY_FLUID_HASH_STRATEGY = new Hash.Strategy<>() {

        @Override
        public int hashCode(Class<Entity> o) {
            return o.getName().hashCode();
        }

        @Override
        public boolean equals(Class<Entity> a, Class<Entity> b) {
            if (a == null && b == null) return true;
            if (a == null || b == null) return false;
            return Objects.equals(a.getName(), b.getName());
        }
    };
}

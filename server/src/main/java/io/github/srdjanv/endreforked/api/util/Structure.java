package io.github.srdjanv.endreforked.api.util;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.init.Blocks;

import io.github.srdjanv.endreforked.core.mixin.FactoryBlockPatternAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class Structure {

    private final IBlockState[][][] structure;
    private final BlockPattern pattern;

    private Structure(IBlockState[][][] structure, BlockPattern pattern) {
        this.structure = structure;
        this.pattern = pattern;
    }

    public IBlockState[][][] getStructure() {
        return structure;
    }

    public BlockPattern getPattern() {
        return pattern;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final FactoryBlockPattern factoryBlockPattern = FactoryBlockPattern.start();
        private final FactoryBlockPatternAccessor factoryBlockPatternAccessor = (FactoryBlockPatternAccessor) factoryBlockPattern;

        private final Map<Character, IBlockState> symbolStateMap = new Object2ObjectOpenHashMap<>();

        private IBlockState[][][] structure;
        private BlockPattern pattern;

        private Builder() {
            symbolStateMap.put(' ', Blocks.AIR.getDefaultState());
        }

        /**
         * Adds a single aisle to this pattern, going in the z axis. (so multiple calls to this will increase the z-size
         * by
         * 1)
         */
        public Builder aisle(String... aisle) {
            factoryBlockPattern.aisle(aisle);
            return this;
        }

        public Builder where(char symbol, Block state) {
            return where(symbol, state.getDefaultState());
        }

        public Builder where(char symbol, IBlockState state) {
            if (Objects.nonNull(structure) || Objects.nonNull(pattern)) throw new IllegalStateException();
            factoryBlockPattern.where(symbol, input -> Objects.nonNull(input) && input.getBlockState().equals(state));
            symbolStateMap.put(symbol, state);
            return this;
        }

        public IBlockState[][][] buildStructure() {
            if (Objects.isNull(structure)) structure = initStructure();
            return structure;
        }

        public BlockPattern biildBlockPattern() {
            if (Objects.isNull(pattern)) pattern = factoryBlockPattern.build();
            return pattern;
        }

        public Structure build() {
            return new Structure(Objects.requireNonNull(buildStructure()), Objects.requireNonNull(biildBlockPattern()));
        }

        private IBlockState[][][] initStructure() {
            if (Objects.isNull(pattern)) factoryBlockPatternAccessor.invokeCheckMissingPredicates();
            /*
             * Int2ObjectSortedMap<Int2ObjectSortedMap<List<IBlockState>>> structure = new
             * Int2ObjectLinkedOpenHashMap<>();
             * 
             * for (int y = 0; y < factoryBlockPatternAccessor.getDepth().size(); y++) {
             * var slice = factoryBlockPatternAccessor.getDepth().get(y);
             * for (int z = 0; z < slice.length; z++) {
             * for (int x = 0; x < slice[z].toCharArray().length; x++) {
             * var map = structure.computeIfAbsent(y, $y -> new Int2ObjectLinkedOpenHashMap<>());
             * var list = map.computeIfAbsent(z, $z -> new ArrayList<>());
             * list.add(symbolStateMap.get(slice[z].charAt(x)));
             * }
             * }
             * }
             * 
             * int maxZ = 0, maxX = 0;// remove
             * IBlockState[][][] stateStruct = new IBlockState[structure.size()][][];
             * 
             * for (int y = 0; y < stateStruct.length; y++) {
             * var zxMap = structure.get(y);
             * maxZ = Math.max(maxZ, zxMap.size());
             * IBlockState[][] zx = new IBlockState[zxMap.size()][];
             * for (int z = 0; z < zxMap.size(); z++) {
             * var x = zxMap.get(z);
             * maxX = Math.max(maxX, x.size());
             * zx[z] = x.toArray(new IBlockState[0]);
             * }
             * stateStruct[y] = zx;
             * }
             */

            IBlockState[][][] stateStruct = (IBlockState[][][]) Array.newInstance(IBlockState.class,
                    factoryBlockPatternAccessor.getDepth().size(),
                    factoryBlockPatternAccessor.getAisleHeight(),
                    factoryBlockPatternAccessor.getRowWidth());

            for (int i = 0; i < factoryBlockPatternAccessor.getDepth().size(); ++i) {
                for (int j = 0; j < factoryBlockPatternAccessor.getAisleHeight(); ++j) {
                    for (int k = 0; k < factoryBlockPatternAccessor.getRowWidth(); ++k) {
                        stateStruct[i][j][k] = symbolStateMap
                                .get(((String[]) factoryBlockPatternAccessor.getDepth().get(i))[j].charAt(k));
                    }
                }
            }

            return stateStruct;
        }
    }
}

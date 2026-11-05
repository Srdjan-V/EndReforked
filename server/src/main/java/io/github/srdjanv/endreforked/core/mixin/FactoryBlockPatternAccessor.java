package io.github.srdjanv.endreforked.core.mixin;

import java.util.List;
import java.util.Map;

import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.pattern.FactoryBlockPattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;

@Mixin(FactoryBlockPattern.class)
public interface FactoryBlockPatternAccessor {

    @Accessor
    Joiner getCOMMA_JOIN();

    @Accessor
    List<String[]> getDepth();

    @Accessor
    Map<Character, Predicate<BlockWorldState>> getSymbolMap();

    @Accessor
    int getAisleHeight();

    @Accessor
    int getRowWidth();

    @Invoker
    void invokeCheckMissingPredicates();
}

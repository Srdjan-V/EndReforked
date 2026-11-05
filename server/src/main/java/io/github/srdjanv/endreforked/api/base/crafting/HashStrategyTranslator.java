package io.github.srdjanv.endreforked.api.base.crafting;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.util.ComparingLang;
import io.github.srdjanv.endreforked.api.util.FluidStackHashStrategy;
import io.github.srdjanv.endreforked.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.Hash;

public interface HashStrategyTranslator<S extends Hash.Strategy<?>> {

    S getHashStrategy();

    @Nullable
    default String translateHashStrategy() {
        if (getHashStrategy() instanceof FluidStackHashStrategy fluid) {
            return ComparingLang.FluidLang.translate(fluid, fluid.customCheckLangKey());
        } else if (getHashStrategy() instanceof ItemStackHashStrategy item) {
            return ComparingLang.ItemLang.translate(item, item.customCheckLangKey());
        }
        return null;
    }
}

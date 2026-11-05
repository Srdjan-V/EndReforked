package io.github.srdjanv.endreforked.api.materializer;

import java.util.function.BiFunction;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.base.crafting.recipe.timed.TimedBiRecipe;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public final class MaterializerRecipe extends TimedBiRecipe<ItemStack, ItemStack, ItemStack> {

    private final Int2ObjectLinkedOpenHashMap<WorldEvent> worldEvents;

    public MaterializerRecipe(ItemStack input,
                              ItemStack input2, int ticksToComplete,
                              BiFunction<ItemStack, ItemStack, ItemStack> function) {
        super(input, input2, ticksToComplete, function);
        this.worldEvents = new Int2ObjectLinkedOpenHashMap<>();
    }

    public boolean registerWorldEvent(int progress, WorldEvent event) {
        if (progress <= 0 || progress > 100) return false;
        worldEvents.put(progress, event);
        return true;
    }

    public Int2ObjectLinkedOpenHashMap<WorldEvent> getWorldEvents() {
        return worldEvents;
    }

    @Nullable
    public Int2ObjectMap.Entry<WorldEvent> getNextWorldEvent(int next) {
        if (next < 0 || next > 100) return null;
        Int2ObjectMap.Entry<WorldEvent> ret = null;
        for (var entry : worldEvents.int2ObjectEntrySet())
            if (entry.getIntKey() > next) {
                ret = entry;
                break;
            }
        return ret;
    }
}

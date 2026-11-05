package io.github.srdjanv.endreforked.api.fluids.base;

import java.util.*;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;

import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import it.unimi.dsi.fastutil.objects.*;

public class FluidEntityCollisionHandler {

    private static final List<FluidEntityCollisionHandler> HANDLERS = new ObjectArrayList<>();

    public static List<FluidEntityCollisionHandler> getHandlers() {
        return HANDLERS;
    }

    private final Object2ObjectOpenHashMap<Class<? extends Entity>, ObjectList<EntityFluidRecipe<Entity, Entity>>> registry;
    private final IFluidInteractable interactable;
    private boolean sortNeeded = true;

    protected FluidEntityCollisionHandler(IFluidInteractable interactable) {
        registry = new Object2ObjectOpenHashMap<>();
        HANDLERS.add(this);
        this.interactable = interactable;
    }

    public IFluidInteractable getInteractable() {
        return interactable;
    }

    public Object2ObjectOpenHashMap<Class<? extends Entity>, ObjectList<EntityFluidRecipe<Entity, Entity>>> getRegistry() {
        return registry;
    }

    public void unRegister(Predicate<? super EntityFluidRecipe<?, ?>> remove) {
        registry.values().forEach(list -> {
            if (list.removeIf(remove)) {
                sortNeeded = true;
            }
        });
    }

    public void unRegister(EntityFluidRecipe<?, ?> recipe) {
        var list = registry.computeIfAbsent(recipe.getInput(), k -> new ObjectArrayList<>());
        if (list.remove(recipe)) sortNeeded = true;
    }

    @SuppressWarnings("unchecked")
    public void register(EntityFluidRecipe<?, ?> recipe) {
        var list = registry.computeIfAbsent(recipe.getInput(), k -> new ObjectArrayList<>());
        if (list.add((EntityFluidRecipe<Entity, Entity>) recipe)) sortNeeded = true;
    }

    public <E extends Entity> Iterator<EntityFluidRecipe<Entity, Entity>> getFluidRecipe(final E entity) {
        if (sortNeeded) {
            for (var value : registry.values()) Collections.sort(value);
            sortNeeded = false;
        }
        var list = registry.get(entity.getClass());
        if (list == null) return Collections.emptyIterator();
        return list.stream()
                .filter(rec -> rec.getEntityMather().test(entity))
                .iterator();
    }
}

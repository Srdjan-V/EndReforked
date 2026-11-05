package io.github.srdjanv.endreforked.api.fluids.base;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;

public class EntityFluidRecipeResult<E extends Entity> {

    private final Object result;
    private final Type type;

    public static <E extends Entity> EntityFluidRecipeResult<E> ofEntity(final E entity) {
        return new EntityFluidRecipeResult<>(entity);
    }

    public static <E extends Entity> EntityFluidRecipeResult<E> ofState(final IBlockState state) {
        return new EntityFluidRecipeResult<>(state);
    }

    private EntityFluidRecipeResult(E result) {
        this.result = result;
        this.type = Type.ENTITY;
    }

    private EntityFluidRecipeResult(IBlockState result) {
        this.result = result;
        this.type = Type.STATE;
    }

    public Type getType() {
        return type;
    }

    public IBlockState getStateResult() {
        return (IBlockState) result;
    }

    @SuppressWarnings("unchecked")
    public E getEntityResult() {
        return (E) result;
    }

    public enum Type {
        STATE,
        ENTITY
    }
}

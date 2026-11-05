package io.github.srdjanv.endreforked.compat.jei.fluids;

import java.util.List;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fluids.IFluidBlock;

import org.jetbrains.annotations.NotNull;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.api.fluids.base.FluidAnyStateCollisionHandler;
import io.github.srdjanv.endreforked.utils.LangUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;

public class FluidInteractionEntityCategory implements IRecipeCategory<FluidInteraction.EntityRecipe> {

    public static final String UID = Tags.MODID + ".fluid_entity_interaction";
    protected static final int INPUT_1 = 0;
    protected static final int INPUT_2 = 1;
    protected static final int OUTPUT = 2;

    private final IDrawable background;
    private final IDrawable slot;

    public FluidInteractionEntityCategory(IGuiHelper helper) {
        background = helper.createBlankDrawable(160, 60);
        slot = helper.getSlotDrawable();
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(@NotNull Minecraft minecraft) {}

    @Override
    public @NotNull String getTitle() {
        return LangUtil.translateToLocal("fluid.interaction.entity");
    }

    @Override
    public @NotNull String getModName() {
        return Tags.MODNAME;
    }

    @Override
    public @NotNull String getUid() {
        return UID;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FluidInteraction.EntityRecipe recipeWrapper,
                          IIngredients ingredients) {
        var fluidStacks = recipeLayout.getFluidStacks();
        var itemStacks = recipeLayout.getItemStacks();

        if (recipeWrapper.getInputType().isAssignableFrom(EntityItem.class)) {
            itemStacks.init(0, true, 25, 20);
        }

        fluidStacks.init(0, true, 45, 20);
        switch (recipeWrapper.getRecipeResult().getType()) {
            case ENTITY -> {
                var entity = recipeWrapper.getRecipeResult().getEntityResult();
                if (entity instanceof EntityItem) {
                    itemStacks.init(1, false, 60, 20);
                }
            }

            case STATE -> {
                IBlockState outState = recipeWrapper.getRecipeResult().getStateResult();
                if (outState.getBlock() instanceof IFluidBlock || outState.getBlock() instanceof BlockLiquid) {
                    fluidStacks.init(1, false, 60, 20);
                } else {
                    itemStacks.init(1, false, 60, 20);
                }
            }
        }

        fluidStacks.set(ingredients);
        itemStacks.set(ingredients);
    }

    public static List<FluidInteraction.EntityRecipe> getRecipes() {
        List<FluidInteraction.EntityRecipe> jeiRecipes = new ObjectArrayList<>();
        for (FluidAnyStateCollisionHandler handler : FluidAnyStateCollisionHandler.getHandlers()) {
            jeiRecipes.addAll(FluidInteraction.EntityRecipe.build(handler.getInteractable()));
        }

        return jeiRecipes;
    }
}

package io.github.srdjanv.endreforked.compat.jei.fluids;

import java.util.List;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.IFluidBlock;

import org.jetbrains.annotations.NotNull;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.api.fluids.base.FluidAnyStateCollisionHandler;
import io.github.srdjanv.endreforked.api.fluids.base.FluidCollisionHandler;
import io.github.srdjanv.endreforked.utils.LangUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;

public class FluidInteractionStateCategory implements IRecipeCategory<FluidInteraction.SideRecipe> {

    public static final String UID = Tags.MODID + ".fluid_state_interaction";
    protected static final int INPUT_1 = 0;
    protected static final int INPUT_2 = 1;
    protected static final int OUTPUT = 2;

    private final IDrawable background;
    private final IDrawable slot;

    public FluidInteractionStateCategory(IGuiHelper helper) {
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
        return LangUtil.translateToLocal("fluid.interaction.state");
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
    public void setRecipe(IRecipeLayout recipeLayout, FluidInteraction.SideRecipe recipeWrapper,
                          IIngredients ingredients) {
        var fluidStacks = recipeLayout.getFluidStacks();
        var itemStacks = recipeLayout.getItemStacks();
        fluidStacks.init(0, true, 35, 20);

        IBlockState inIBlockState = recipeWrapper.getInIBlockState();
        if (inIBlockState.getBlock() instanceof IFluidBlock || inIBlockState.getBlock() instanceof BlockLiquid) {
            fluidStacks.init(1, true, 15, 20);
        } else {
            itemStacks.init(1, true, 15, 20);
        }

        IBlockState outIBlockState = recipeWrapper.getOutIBlockState();
        if (outIBlockState.getBlock() instanceof IFluidBlock || outIBlockState.getBlock() instanceof BlockLiquid) {
            fluidStacks.init(3, false, 45, 20);
        } else {
            itemStacks.init(3, false, 45, 20);
        }

        fluidStacks.set(ingredients);
        itemStacks.set(ingredients);
    }

    public static List<FluidInteraction.SideRecipe> getRecipes() {
        List<FluidInteraction.SideRecipe> jeiRecipes = new ObjectArrayList<>();
        for (FluidCollisionHandler handler : FluidCollisionHandler.getHandlers()) {
            jeiRecipes.addAll(FluidInteraction.SideRecipe.buildState(handler.getInteractable()));
        }

        for (FluidAnyStateCollisionHandler handler : FluidAnyStateCollisionHandler.getHandlers()) {
            jeiRecipes.addAll(FluidInteraction.SideRecipe.buildAnny(handler.getInteractable()));
        }

        return jeiRecipes;
    }
}

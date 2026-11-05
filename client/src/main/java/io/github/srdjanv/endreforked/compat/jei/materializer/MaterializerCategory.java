package io.github.srdjanv.endreforked.compat.jei.materializer;

import java.util.List;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.drawable.GuiTextures;
import com.google.common.collect.Lists;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.api.materializer.ItemCatalyst;
import io.github.srdjanv.endreforked.api.materializer.MaterializerHandler;
import io.github.srdjanv.endreforked.utils.LangUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;

public class MaterializerCategory implements IRecipeCategory<MaterializerRecipe> {

    public static final String UID = Tags.MODID + ".materializer";
    protected static final int input1 = 0;
    protected static final int input2 = 1;
    protected static final int output = 2;

    private final IDrawable background;
    private final IDrawable slot;

    public MaterializerCategory(IGuiHelper helper) {
        background = helper.createBlankDrawable(160, 60);
        slot = helper.getSlotDrawable();
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(@NotNull Minecraft minecraft) {
        GuiTextures.PROGRESS_ARROW.getSubArea(0f, 0f, 1f, 0.5f).draw(78, 20, 20, 20);
        float progress = Minecraft.getSystemTime() % 2000 / 2000f; // 2 seconds
        GuiTextures.PROGRESS_ARROW.getSubArea(0f, 0.5f, progress, 1f).draw(78, 20, progress * 20, 20);
        slot.draw(minecraft, 105, 20);
        slot.draw(minecraft, 55, 20);
        slot.draw(minecraft, 35, 20);
        GuiTextures.HELP.draw(142, 42, 16, 16);
    }

    @Override
    public @NotNull String getTitle() {
        return LangUtil.translateToLocal("tile.materializer");
    }

    @Override
    public @NotNull String getModName() {
        return Tags.MODNAME;
    }

    @Override
    public @NotNull String getUid() {
        return UID;
    }

    public void setRecipe(IRecipeLayout recipeLayout, @NotNull MaterializerRecipe recipeWrapper,
                          @NotNull IIngredients ingredients) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
        stacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            if (slotIndex == MaterializerCategory.input1) recipeWrapper.applyInput1Tooltip(tooltip);
            if (slotIndex == MaterializerCategory.input2) recipeWrapper.applyInput2Tooltip(tooltip);
        });
        stacks.init(input1, true, 35, 20);
        stacks.init(input2, true, 55, 20);
        stacks.init(output, false, 105, 20);
        stacks.set(ingredients);
    }

    public static List<MaterializerRecipe> getRecipes() {
        List<MaterializerRecipe> jeiRecipes = Lists.newArrayList();
        for (ItemCatalyst recipeGroping : MaterializerHandler.getInstance().getRecipeGroupings()) {
            for (var rec : recipeGroping.getRecipes().values()) {
                jeiRecipes.add(new MaterializerRecipe(
                        recipeGroping.getGrouping(),
                        MaterializerHandler.getInstance().translateHashStrategy(),
                        rec.getInput(),
                        recipeGroping.translateHashStrategy(),
                        rec.getRecipeFunction().apply(recipeGroping.getGrouping(), rec.getInput())));
            }
        }

        return jeiRecipes;
    }
}

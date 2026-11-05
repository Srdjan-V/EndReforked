package io.github.srdjanv.endreforked.compat.jei.entropywand;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.google.common.collect.Lists;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.api.entropy.wand.EntropyWandHandler;
import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.utils.LangUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;

public class EntropyWandCategory implements IRecipeCategory<EntropyWandRecipe> {

    public static final String UID = Tags.MODID + ".entropywand";
    public static final com.cleanroommc.modularui.api.drawable.IDrawable drawableWandItem = new ItemDrawable(
            new ItemStack(ModItems.ENTROPY_WAND.get()));

    protected static final int input = 0;
    protected static final int output = 1;

    private final IDrawable background;
    private final IDrawable slot;

    public EntropyWandCategory(IGuiHelper helper) {
        background = helper.createBlankDrawable(160, 60);
        slot = helper.getSlotDrawable();
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slot.draw(minecraft, 48, 20);
        drawableWandItem.draw(null, 72, 20, 16, 16);
        slot.draw(minecraft, 96, 20);
    }

    @Override
    public @NotNull String getUid() {
        return UID;
    }

    @Override
    public @NotNull String getTitle() {
        return LangUtil.translateToLocal("item.entropy_wand.name");
    }

    @Override
    public @NotNull String getModName() {
        return Tags.MODNAME;
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, EntropyWandRecipe recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
        stacks.init(input, true, 48, 20);
        stacks.init(output, false, 96, 20);
        stacks.set(ingredients);
    }

    public static List<EntropyWandRecipe> getRecipes() {
        List<EntropyWandRecipe> jeiRecipes = Lists.newArrayList();
        for (var set : EntropyWandHandler.getConverionMap().entrySet()) {
            for (var rec : set.getValue()) {
                jeiRecipes.add(new EntropyWandRecipe(set.getKey(), rec));
            }
        }

        return jeiRecipes;
    }
}

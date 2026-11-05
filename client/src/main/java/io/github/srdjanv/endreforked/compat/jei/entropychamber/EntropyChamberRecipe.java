package io.github.srdjanv.endreforked.compat.jei.entropychamber;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.utils.Color;

import io.github.srdjanv.endreforked.api.entropy.chamber.FluidChamberRecipe;
import io.github.srdjanv.endreforked.api.entropy.chamber.ItemChamberRecipe;
import io.github.srdjanv.endreforked.utils.LangUtil;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class EntropyChamberRecipe implements IRecipeWrapper {

    private final Type type;
    private final ItemStack itemIn;
    private final ItemStack itemOut;

    private final FluidStack fluidIn;
    private final FluidStack fluidOut;
    private final int entropyCost;

    public EntropyChamberRecipe(ItemStack stack, ItemChamberRecipe chamberRecipe) {
        type = Type.ITEM;
        entropyCost = chamberRecipe.getEntropyCost();

        itemIn = stack;
        itemOut = chamberRecipe.getRecipeFunction().apply(stack);

        fluidIn = null;
        fluidOut = null;
    }

    public EntropyChamberRecipe(FluidStack stack, FluidChamberRecipe chamberRecipe) {
        type = Type.FLUID;
        entropyCost = chamberRecipe.getEntropyCost();

        itemIn = null;
        itemOut = null;

        fluidIn = stack;
        fluidOut = chamberRecipe.getRecipeFunction().apply(stack);
    }

    public Type getType() {
        return type;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        minecraft.fontRenderer.drawString(String.valueOf(entropyCost), 76, 47, Color.argb(128, 128, 128, 1));
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= 73 && mouseX <= 84 && mouseY >= 44 && mouseY <= 55) {
            return Collections.singletonList(LangUtil.translateToLocal("endreborn.jei.entropy_wand.item_cost"));
        }

        return IRecipeWrapper.super.getTooltipStrings(mouseX, mouseY);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        switch (type) {
            case FLUID -> {
                ingredients.setInput(VanillaTypes.FLUID, fluidIn);
                ingredients.setOutput(VanillaTypes.FLUID, fluidOut);
            }
            case ITEM -> {
                ingredients.setInput(VanillaTypes.ITEM, itemIn);
                ingredients.setOutput(VanillaTypes.ITEM, itemOut);
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public enum Type {
        FLUID,
        ITEM;
    }
}

package io.github.srdjanv.endreforked.compat.jei;

import net.minecraft.item.ItemStack;

import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.compat.jei.endforge.EndForgeCategory;
import io.github.srdjanv.endreforked.compat.jei.entropychamber.EntropyChamberCategory;
import io.github.srdjanv.endreforked.compat.jei.entropywand.EntropyWandCategory;
import io.github.srdjanv.endreforked.compat.jei.fluids.FluidInteractionEntityCategory;
import io.github.srdjanv.endreforked.compat.jei.fluids.FluidInteractionStateCategory;
import io.github.srdjanv.endreforked.compat.jei.materializer.MaterializerCategory;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        final IJeiHelpers helpers = registry.getJeiHelpers();
        final IGuiHelper gui = helpers.getGuiHelper();

        registry.addRecipeCategories(new EndForgeCategory(gui));
        registry.addRecipeCategories(new MaterializerCategory(gui));
        registry.addRecipeCategories(new EntropyWandCategory(gui));
        registry.addRecipeCategories(new FluidInteractionEntityCategory(gui));
        registry.addRecipeCategories(new FluidInteractionStateCategory(gui));
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipes(EndForgeCategory.getRecipes(), EndForgeCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.BLOCK_END_FORGE.get()), EndForgeCategory.UID);

        registry.addRecipes(MaterializerCategory.getRecipes(), MaterializerCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.MATERIALIZER_BLOCK.get()), MaterializerCategory.UID);

        registry.addRecipes(EntropyWandCategory.getRecipes(), EntropyWandCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModItems.ENTROPY_WAND.get()), EntropyWandCategory.UID);

        registry.addRecipes(EntropyChamberCategory.getRecipes(), EntropyChamberCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.ENTROPY_CHAMBER.get()), EntropyChamberCategory.UID);

        registry.addRecipes(FluidInteractionEntityCategory.getRecipes(), FluidInteractionEntityCategory.UID);
        registry.addRecipes(FluidInteractionStateCategory.getRecipes(), FluidInteractionStateCategory.UID);
    }
}

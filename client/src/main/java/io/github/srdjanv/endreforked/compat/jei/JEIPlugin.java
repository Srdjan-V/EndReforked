package io.github.srdjanv.endreforked.compat.jei;

import net.minecraft.item.ItemStack;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.client.ClientProxy;
import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.compat.jei.endforge.EndForgeCategory;
import io.github.srdjanv.endreforked.compat.jei.entropychamber.EntropyChamberCategory;
import io.github.srdjanv.endreforked.compat.jei.entropywand.EntropyWandCategory;
import io.github.srdjanv.endreforked.compat.jei.fluids.FluidInteractionEntityCategory;
import io.github.srdjanv.endreforked.compat.jei.fluids.FluidInteractionStateCategory;
import io.github.srdjanv.endreforked.compat.jei.materializer.MaterializerCategory;
import io.github.srdjanv.endreforked.common.registry.Registries;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin {

    private static Registries clientRegistries() {
        return ((ClientProxy) EndReforked.getProxy()).getClientRegistries();
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        final IJeiHelpers helpers = registry.getJeiHelpers();
        final IGuiHelper gui = helpers.getGuiHelper();

        registry.addRecipeCategories(new EntropyChamberCategory(gui));
        registry.addRecipeCategories(new EndForgeCategory(gui));
        registry.addRecipeCategories(new MaterializerCategory(gui));
        registry.addRecipeCategories(new EntropyWandCategory(gui));
        registry.addRecipeCategories(new FluidInteractionEntityCategory(gui));
        registry.addRecipeCategories(new FluidInteractionStateCategory(gui));
    }

    @Override
    public void register(IModRegistry registry) {
        var registries = clientRegistries();
        var machineRecipes = registries.getMachineRecipes().getCurrent();
        registry.addRecipes(EndForgeCategory.FEED.load(machineRecipes), EndForgeCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.BLOCK_END_FORGE.get()), EndForgeCategory.UID);

        registry.addRecipes(MaterializerCategory.FEED.load(machineRecipes), MaterializerCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.MATERIALIZER_BLOCK.get()), MaterializerCategory.UID);

        registry.addRecipes(EntropyWandCategory.FEED.load(registries.getWandConversions().getCurrent()),
                EntropyWandCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModItems.ENTROPY_WAND.get()), EntropyWandCategory.UID);

        registry.addRecipes(EntropyChamberCategory.FEED.load(machineRecipes), EntropyChamberCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.ENTROPY_CHAMBER.get()), EntropyChamberCategory.UID);

        registry.addRecipes(
                FluidInteractionEntityCategory.FEED.load(registries.getFluidEntityInteractions().getCurrent()),
                FluidInteractionEntityCategory.UID);
        registry.addRecipes(
                FluidInteractionStateCategory.FEED.load(registries.getFluidBlockInteractions().getCurrent()),
                FluidInteractionStateCategory.UID);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        var jei = runtime.getRecipeRegistry();
        var registries = clientRegistries();
        EntropyChamberCategory.FEED.watch(jei, registries.getMachineRecipes());
        EndForgeCategory.FEED.watch(jei, registries.getMachineRecipes());
        MaterializerCategory.FEED.watch(jei, registries.getMachineRecipes());
        EntropyWandCategory.FEED.watch(jei, registries.getWandConversions());
        FluidInteractionEntityCategory.FEED.watch(jei, registries.getFluidEntityInteractions());
        FluidInteractionStateCategory.FEED.watch(jei, registries.getFluidBlockInteractions());
    }
}

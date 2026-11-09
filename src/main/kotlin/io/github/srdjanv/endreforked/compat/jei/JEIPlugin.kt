package io.github.srdjanv.endreforked.compat.jei

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.client.ClientProxy
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.compat.jei.endforge.EndForgeCategory
import io.github.srdjanv.endreforked.compat.jei.entropychamber.EntropyChamberCategory
import io.github.srdjanv.endreforked.compat.jei.fluids.FluidInteractionEntityCategory
import io.github.srdjanv.endreforked.compat.jei.fluids.FluidInteractionStateCategory
import io.github.srdjanv.endreforked.compat.jei.materializer.MaterializerCategory
import mezz.jei.api.IJeiRuntime
import mezz.jei.api.IModPlugin
import mezz.jei.api.IModRegistry
import mezz.jei.api.recipe.IRecipeCategoryRegistration
import net.minecraft.item.ItemStack

@mezz.jei.api.JEIPlugin
class JEIPlugin : IModPlugin {
    private val clientRegistries get() = (EndReforked.getProxy() as ClientProxy).clientRegistries

    override fun registerCategories(registry: IRecipeCategoryRegistration) {
        val gui = registry.jeiHelpers.guiHelper
        registry.addRecipeCategories(
            EntropyChamberCategory(gui), EndForgeCategory(gui), MaterializerCategory(gui),
            FluidInteractionEntityCategory(gui), FluidInteractionStateCategory(gui),
        )
    }

    override fun register(registry: IModRegistry) {
        val registries = clientRegistries
        val machineRecipes = registries.machineRecipes.current

        registry.addRecipes(EndForgeCategory.FEED.load(machineRecipes), EndForgeCategory.UID)
        registry.addRecipeCatalyst(ItemStack(ModBlocks.BLOCK_END_FORGE), EndForgeCategory.UID)
        registry.addRecipes(MaterializerCategory.FEED.load(machineRecipes), MaterializerCategory.UID)
        registry.addRecipeCatalyst(ItemStack(ModBlocks.MATERIALIZER_BLOCK), MaterializerCategory.UID)
        registry.addRecipes(EntropyChamberCategory.FEED.load(machineRecipes), EntropyChamberCategory.UID)
        registry.addRecipeCatalyst(ItemStack(ModBlocks.ENTROPY_CHAMBER), EntropyChamberCategory.UID)
        registry.addRecipes(FluidInteractionEntityCategory.FEED.load(registries.fluidEntityInteractions.current), FluidInteractionEntityCategory.UID)
        registry.addRecipes(FluidInteractionStateCategory.FEED.load(registries.fluidBlockInteractions.current), FluidInteractionStateCategory.UID)
    }

    override fun onRuntimeAvailable(runtime: IJeiRuntime) {
        val jei = runtime.recipeRegistry
        val registries = clientRegistries
        EntropyChamberCategory.FEED.watch(jei, registries.machineRecipes)
        EndForgeCategory.FEED.watch(jei, registries.machineRecipes)
        MaterializerCategory.FEED.watch(jei, registries.machineRecipes)
        FluidInteractionEntityCategory.FEED.watch(jei, registries.fluidEntityInteractions)
        FluidInteractionStateCategory.FEED.watch(jei, registries.fluidBlockInteractions)
    }
}

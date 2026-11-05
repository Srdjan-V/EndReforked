package io.github.srdjanv.endreforked.compat.jei.entropywand;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.Color;

import io.github.srdjanv.endreforked.api.entropy.wand.WorldConversion;
import io.github.srdjanv.endreforked.common.configs.Configs;
import io.github.srdjanv.endreforked.utils.LangUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class EntropyWandRecipe implements IRecipeWrapper {

    private final List<List<ItemStack>> input = new ObjectArrayList<>(2);
    private final ItemStack output;
    private final int wandDamage;

    public EntropyWandRecipe(Block block, WorldConversion worldConversion) {
        List<ItemStack> input = new ObjectArrayList<>();
        for (IBlockState validState : block.getBlockState().getValidStates())
            if (worldConversion.getBlockStateMatcher().test(validState))
                input.add(new ItemStack(block, 1, block.getMetaFromState(validState)));

        if (!Configs.CLIENT_SIDE_CONFIGS.entropyWandRenderBrokenTextures) {
            input = input.stream().filter(itemStack -> {
                var masher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
                var model = masher.getItemModel(itemStack);
                return masher.getModelManager().getMissingModel() != model;
            }).collect(Collectors.toList());
        }

        this.input.add(input);
        var newState = worldConversion.getNewState().get();
        var newStateBlock = newState.getBlock();
        output = new ItemStack(newStateBlock, 1, newStateBlock.getMetaFromState(newState));
        wandDamage = worldConversion.getItemDamage();
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        minecraft.fontRenderer.drawString(String.valueOf(wandDamage), 76, 47, Color.argb(128, 128, 128, 1));
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
        ingredients.setInputLists(VanillaTypes.ITEM, input);
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }
}

package endreborn.compat.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import endreborn.Reference;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;

public class MaterializerCategory implements IRecipeCategory<MaterializerRecipe> {

    public static final String UID = Reference.MODID + ".materializer";
    public static final ResourceLocation TEXTURES = new ResourceLocation(
            Reference.MODID + ":textures/gui/entropy_user.png");

    protected static final int input1 = 0;
    protected static final int input2 = 1;
    protected static final int output = 3;

    protected final IDrawableStatic staticFlame;
    protected final IDrawableAnimated animatedFlame;
    protected final IDrawableAnimated animatedArrow;
    private final IDrawable background;
    private final String name;

    public MaterializerCategory(IGuiHelper helper) {
        staticFlame = helper.createDrawable(TEXTURES, 176, 3, 40, 10);
        animatedFlame = helper.createAnimatedDrawable(staticFlame, 20, IDrawableAnimated.StartDirection.TOP, true);

        IDrawableStatic staticArrow = helper.createDrawable(TEXTURES, 176, 13, 24, 17);
        animatedArrow = helper.createAnimatedDrawable(staticArrow, 20, IDrawableAnimated.StartDirection.LEFT, false);

        background = helper.createDrawable(TEXTURES, 4, 4, 169, 78);
        name = I18n.format("tile.entropy_user.name");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(@NotNull Minecraft minecraft) {
        animatedFlame.draw(minecraft, 64, 13);
        animatedArrow.draw(minecraft, 106, 39);
    }

    @Override
    public @NotNull String getTitle() {
        return name;
    }

    @Override
    public @NotNull String getModName() {
        return Reference.NAME;
    }

    @Override
    public @NotNull String getUid() {
        return UID;
    }

    public void setRecipe(IRecipeLayout recipeLayout, @NotNull MaterializerRecipe recipeWrapper,
                          @NotNull IIngredients ingredients) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
        stacks.init(input1, true, 64, 38);
        stacks.init(input2, true, 86, 38);
        stacks.init(output, false, 136, 38);
        stacks.set(ingredients);
    }
}